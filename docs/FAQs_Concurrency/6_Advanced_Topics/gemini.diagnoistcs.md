### Describe a code snippet #6: The Silent Resource Drain

```java
// Spring MVC default async timeout is typically 30 seconds
@GetMapping("/heavy-report")
public CompletableFuture<Report> heavyReport() {
  return CompletableFuture.supplyAsync(() -> dataWarehouse.runComplexAggregation(), reportPool); 
}

```

**Defect:** If the data warehouse takes 45 seconds to respond, the web framework gives up at 30 seconds and sends a `503 Service Unavailable` to the client. However, the `reportPool` thread **does not stop**. It continues grinding away for the full 45 seconds, holding the database connection open and computing a report that will simply be thrown into the void when it finally finishes. Under a traffic spike, the web server stays responsive, but the backend `reportPool` and the database fill up with "zombie" tasks working for clients that have already disconnected.

**Fix:** The future must fail *before* the web framework times out, so the thread is released and the DB query is ideally cancelled.

```java
return CompletableFuture.supplyAsync(() -> dataWarehouse.runComplexAggregation(), reportPool)
    .orTimeout(25, TimeUnit.SECONDS)  // Fails at 25s, freeing the pool thread
    .exceptionally(ex -> new Report("Timeout fallback")); 

```

**Where it matters:** In heavy read-only APIs or reports. If the client leaves, the work must stop. (Note: standard JDBC does not automatically kill the DB query on thread interrupt, so the database might still do the work, but at least your JVM thread is freed).

The web framework's timeout protects the client; the future's timeout protects your server.

### Describe a code snippet #7: The Stale Work Queue

```java
// A bulkhead pool meant to protect the system from a slow downstream API
private final ExecutorService apiPool = Executors.newFixedThreadPool(50);

public CompletableFuture<Data> fetch() {
  return CompletableFuture.supplyAsync(() -> slowApi.call(), apiPool);
}

```

**Defect:** `Executors.newFixedThreadPool(50)` uses an unbounded `LinkedBlockingQueue` under the hood. It caps the threads at 50, but the queue can grow to `Integer.MAX_VALUE`. If the downstream API stalls, 50 threads get stuck. Incoming requests queue up infinitely in memory.
The fatal trap: If 10,000 requests queue up, they will sit there for hours. By the time the 50 threads slowly work through the backlog, the clients timed out hours ago. The server spends CPU and network resources processing stale requests that nobody is waiting for anymore.

**Fix:** Use a custom `ThreadPoolExecutor` with a strictly bounded queue and a rejection policy.

```java
private final ExecutorService apiPool = new ThreadPoolExecutor(
    50, 50, 0L, TimeUnit.MILLISECONDS,
    new ArrayBlockingQueue<>(200),        // Max 200 waiting tasks
    new ThreadPoolExecutor.AbortPolicy()  // Reject task 201 immediately
);

```

**Where it matters:** Any system under sudden burst load. Failing fast (`AbortPolicy`) allows the web tier to immediately return a `503` so upstream load balancers can retry elsewhere, rather than silently swallowing the request into a black hole.

An unbounded queue does not prevent failure; it just delays it until recovery is impossible.

### Scenario: The Phantom Rollback

A senior developer is tasked with speeding up a slow, blocking user registration flow. It currently creates the user, generates a default workspace, and sends a welcome email. They refactor it to run the workspace generation and email concurrently using a custom pool.

```java
@Transactional
public void registerUser(UserDTO dto) {
    User user = userRepository.save(new User(dto)); 

    var workspaceTask = CompletableFuture.runAsync(() -> workspaceService.create(user), pool);
    var emailTask = CompletableFuture.runAsync(() -> emailService.sendWelcome(user), pool);

    CompletableFuture.allOf(workspaceTask, emailTask).join();
}

```

During a production outage, the email service goes down and throws exceptions. Later, the database shows newly created users *without* workspaces, violating data integrity.

Why did the `@Transactional` rollback fail?

**The Root Cause:**
The `@Transactional` annotation binds the database connection and the transaction state to the **original request thread**.

1. The request thread saves the `User`.
2. It launches two async tasks on the `pool`.
3. If `emailTask` fails, `allOf().join()` throws an exception on the request thread.
4. Spring intercepts the exception and rolls back the transaction, undoing `userRepository.save()`.

**The fatal flaw:** `workspaceService.create(user)` ran on a *different* thread. It could not see the request thread's transaction. It opened its own independent connection, started its own transaction, and committed its data permanently. Rolling back the outer transaction did nothing to the workspace transaction.

**Fixes:**

1. **Remove the async boundary for writes:** If data integrity is required, keep writes on the same thread. Make only the email (external side-effect) async, and use a reliable outbox pattern.
2. **Saga / Compensating Transactions:** If you must split writes across threads, you lose ACID guarantees. You must write explicit rollback logic in a `.exceptionally()` block to manually delete the workspace if the email fails.

Transactions are strictly thread-bound; cross-thread data integrity requires distributed patterns, not just futures.

### Describe a code snippet #8: The Self-Deadlock

```java
private final ExecutorService orderPool = Executors.newFixedThreadPool(10);

public CompletableFuture<Order> process(Long id) {
  return CompletableFuture.supplyAsync(() -> {
      // Fetch the order (takes 1 thread)
      Order order = db.getOrder(id);
      
      // Fetch user details async on the SAME pool, and wait for it
      User user = CompletableFuture.supplyAsync(() -> db.getUser(order.getUserId()), orderPool).join();
      
      return enrichOrder(order, user);
  }, orderPool);
}

```

**Defect:** This is a classic thread starvation deadlock. The pool has exactly 10 threads. If 10 `process(id)` requests come in simultaneously, they take all 10 threads.
Each of those 10 threads then submits a second task (`db.getUser`) to the *same* pool and calls `.join()`, halting to wait for the result.
Because the pool is completely full of waiting parent tasks, the 10 child tasks are stuck in the queue forever. None of the parent threads can move forward until the children finish, and the children can't start until a parent finishes. The pool is permanently deadlocked.

**Fix:** Never block (`join()` or `get()`) on a thread pool while running inside that same pool. Chain them asynchronously instead:

```java
return CompletableFuture.supplyAsync(() -> db.getOrder(id), orderPool)
    .thenCompose(order -> CompletableFuture.supplyAsync(
        () -> enrichOrder(order, db.getUser(order.getUserId())), orderPool));

```

With `thenCompose`, the thread is released back to the pool between the two steps, preventing the deadlock.

**Where it matters:** Microservice orchestration. When tasks are nested, a fixed thread pool becomes a ticking time bomb under load.

### Describe a code snippet #9: The Hijacked Web Thread

```java
@GetMapping("/calculate")
public CompletableFuture<Result> calculate() {
    return CompletableFuture.supplyAsync(() -> fastCache.get(), cachePool)
        .thenApply(data -> heavyCpuCalculations(data)); 
}

```

**Defect:** Which thread executes `heavyCpuCalculations`? It is dangerously unpredictable.
`thenApply` (without the `Async` suffix) executes on whatever thread completes the previous stage.

* If the cache is slow, the `cachePool` thread finishes the fetch and immediately continues into the CPU work.
* **The Trap:** If the cache is fast, the future might complete *before* the web thread even finishes attaching the `.thenApply` block. In that case, the Java runtime forces the **calling thread** (the Tomcat web thread) to execute `heavyCpuCalculations`.

Under load, your fast cache inadvertently hijacks your web threads to do heavy mathematical lifting, paralyzing the web server.

**Fix:** If a chained task is CPU-intensive or blocking, always force an explicit thread handoff using the `*Async` variant.

```java
return CompletableFuture.supplyAsync(() -> fastCache.get(), cachePool)
    .thenApplyAsync(data -> heavyCpuCalculations(data), cpuPool); 

```

Synchronous chaining (`thenApply`, `thenAccept`) is only safe for trivial, non-blocking transformations (like mapping a DTO).

### Scenario: The Impersonator (Context Poisoning)

A developer realizes that Spring Security context is lost when using a custom thread pool. To fix it, they write a simple `Runnable` wrapper that copies the context from the web thread to the background thread before executing the task.

```java
public class SecurityContextAwareRunnable implements Runnable {
    private final Runnable delegate;
    private final SecurityContext context;

    public SecurityContextAwareRunnable(Runnable delegate) {
        this.delegate = delegate;
        this.context = SecurityContextHolder.getContext(); // Capture on Web Thread
    }

    @Override
    public void run() {
        SecurityContextHolder.setContext(context); // Apply on Pool Thread
        delegate.run();
    }
}

```

They wrap all async tasks in this class. A week later, a low-privilege user randomly gains "Admin" permissions for a split second, modifying data they shouldn't be able to access.

Why did this security breach happen?

**The Root Cause:**
Thread pools **reuse** threads.
When the admin's async task finished, the background thread was returned to the pool *with the admin's `SecurityContext` still firmly attached to its `ThreadLocal` storage*.

Five minutes later, a standard user triggers an async task. If that task gets assigned to the same "dirty" thread, and the new task forgets to overwrite the security context (or checks permissions before the new context is fully applied), the standard user is executing code with the previous Admin's credentials.

**The Fix:** You must guarantee that `ThreadLocal` data is wiped clean the exact millisecond the task finishes, using a `try-finally` block.

```java
    @Override
    public void run() {
        SecurityContextHolder.setContext(context); 
        try {
            delegate.run();
        } finally {
            SecurityContextHolder.clearContext(); // CRITICAL: Scrub the thread clean
        }
    }

```

When passing context across threads, how you clean up the thread is far more important than how you set it up. A leaked connection crashes your app; a leaked `ThreadLocal` breaches your security.


### Describe a code snippet #10: The Silent Failure (Swallowed Exceptions)

```java
public void triggerBackgroundProcess(String jobId) {
    CompletableFuture.runAsync(() -> {
        log.info("Starting job {}", jobId);
        jobService.executeHeavyTask(jobId); // This throws a NullPointerException internally
        log.info("Finished job {}", jobId);
    }, backgroundPool);
}

```

**Defect:** If `jobService.executeHeavyTask()` throws an unchecked exception (like an NPE), the task dies, but the application logs absolutely nothing about the error.
Because the caller does not wait on the future (no `.join()` or `.get()`), and the chain has no `.exceptionally()` block, the `CompletableFuture` catches the exception internally and stores it in the future's state. Since nobody ever asks the future for its state, the exception is swallowed forever.

**Fix:** If you fire-and-forget an async task, you must explicitly attach an exception handler at the end of the chain to at least log the failure.

```java
CompletableFuture.runAsync(() -> {
    jobService.executeHeavyTask(jobId);
}, backgroundPool).exceptionally(ex -> {
    log.error("Background job {} failed", jobId, ex);
    return null;
});

```

**Where it matters:** Common in "fire-and-forget" events like sending an email or triggering an audit log. A silent failure here means you won't know the feature is broken until users complain.

### Scenario: The Parallel Stream Collision

A team loves functional programming. They use `CompletableFuture.supplyAsync()` for parallelizing outgoing HTTP calls. In a completely separate module, another developer uses Java's `list.parallelStream().map(...)` to process large data collections faster.

During testing, both features work perfectly. In production under load, the CPU-bound data processing suddenly drops to a crawl, taking 10x longer than a normal, single-threaded stream.

Why did the parallel stream break?

**The Root Cause:**
Both `CompletableFuture.supplyAsync()` (when called without a custom `Executor`) and `parallelStream()` share the exact same underlying thread pool: **`ForkJoinPool.commonPool()`**.

This pool is intentionally small, sized to the number of CPU cores minus one (e.g., 3 threads on a 4-core machine).
When the HTTP calls arrive, they consume the `commonPool` threads and immediately block waiting for network responses. Because the pool is now exhausted by idle, blocking tasks, the `parallelStream()` has no threads available. It is forced to degrade, processing elements sequentially on whatever single thread called it, or freezing entirely until an HTTP timeout frees a thread.

**The Fix:**
Never mix I/O and CPU work.

1. **I/O:** Always pass a custom thread pool to `CompletableFuture` (`supplyAsync(task, customIoPool)`).
2. **CPU/Functional pipelines:** Leave the `commonPool` strictly reserved for CPU-bound tasks like parallel streams.

Sharing a global thread pool creates hidden, cross-module dependencies where a slow API call paralyzes your data processing pipelines.

### Describe a code snippet #11: The Side-Effect Trap (Mutable Lambda Captures)

```java
public CompletableFuture<List<String>> fetchAllUserRoles(List<Long> userIds) {
    List<String> allRoles = new ArrayList<>(); // Standard mutable list
    
    List<CompletableFuture<Void>> futures = userIds.stream()
        .map(id -> CompletableFuture.supplyAsync(() -> roleClient.getRoles(id), apiPool)
            .thenAccept(roles -> allRoles.addAll(roles))) // Mutating shared state inside a lambda
        .toList();
        
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> allRoles);
}

```

**Defect:** This code violates the core rule of functional composition: lambdas running in concurrent contexts must be side-effect free.
`ArrayList` is not thread-safe. Multiple custom pool threads are completing their network calls and simultaneously executing `.thenAccept(roles -> allRoles.addAll(roles))`. This causes race conditions, resulting in lost data, null elements, or `ConcurrentModificationException`.

**Fix (The Functional Way):** Instead of using a callback to mutate an outer variable, keep the data immutable. Have each future *return* its result, and gather them safely at the end.

```java
public CompletableFuture<List<String>> fetchAllUserRoles(List<Long> userIds) {
    List<CompletableFuture<List<String>>> futures = userIds.stream()
        .map(id -> CompletableFuture.supplyAsync(() -> roleClient.getRoles(id), apiPool))
        .toList();
        
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream()
            .map(CompletableFuture::join) // Safe because allOf ensures they are done
            .flatMap(List::stream)
            .toList());
}

```

**Where it matters:** Anywhere lambdas and concurrency meet. If you find yourself capturing a mutable collection from the outer scope to gather async results, you are introducing a race condition. Always favor immutability and `flatMap` over mutating shared state.


### Describe a code snippet #12: The Cancellation Delusion

```java
public void processWithTimeout(String fileId) {
    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        fileService.parseGiganticCsv(fileId); // Takes 10 minutes, mostly CPU and I/O
    }, workerPool);

    try {
        future.get(5, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
        log.warn("Parsing took too long, cancelling.");
        future.cancel(true); // Attempt to interrupt the thread
    }
}

```

**Defect:** `CompletableFuture.cancel(true)` **does not interrupt the underlying thread**.
Unlike traditional `Future.cancel(true)` returned directly by an `ExecutorService`, a `CompletableFuture` is just a state machine. Calling `cancel(true)` transitions the future's state to "Cancelled" so downstream dependents (`thenApply`, etc.) don't run, but the `workerPool` thread executing `parseGiganticCsv` has no idea the future was cancelled. It will run for the full 10 minutes, wasting CPU and memory on a discarded result.

**Fix:** If you need true thread interruption, you cannot rely solely on `CompletableFuture`. You must either check `Thread.currentThread().isInterrupted()` periodically inside your heavy task, or keep a reference to the actual `Future` returned by the executor:

```java
// If you must be able to kill the thread, bypass CompletableFuture for the submission
Future<?> rawFuture = workerPool.submit(() -> fileService.parseGiganticCsv(fileId));
// ... later ...
rawFuture.cancel(true); // This actually sends an interrupt signal to the thread

```

**Where it matters:** Heavy batch processing or user-triggered cancellations. If a user clicks "Cancel Export", but your code uses `CompletableFuture.cancel()`, the server is still doing the work in secret.

### Describe a code snippet #13: The "Synchronous Async" Loop

```java
public List<UserDTO> enrichUsers(List<User> users) {
    return users.stream()
        .map(user -> CompletableFuture.supplyAsync(() -> remoteApi.fetchDetails(user.getId()), apiPool)
            .join()) // Wait for the async task to finish before continuing the stream
        .toList();
}

```

**Defect:** This completely defeats the purpose of asynchronous programming. Streams process elements one at a time.
Because `.join()` is called *inside* the `map` phase, the main thread submits the first user to the `apiPool`, immediately blocks waiting for it to finish, and only *then* moves to the second user. It executes sequentially. You paid the performance overhead of thread context switching and thread pool management, but achieved zero concurrency.

**Fix:** You must split the stream pipeline into two distinct phases: one to trigger all tasks concurrently, and a second to wait for them.

```java
public List<UserDTO> enrichUsers(List<User> users) {
    // Phase 1: Submit all tasks immediately (Fan-out)
    List<CompletableFuture<UserDTO>> futures = users.stream()
        .map(user -> CompletableFuture.supplyAsync(() -> remoteApi.fetchDetails(user.getId()), apiPool))
        .toList();
        
    // Phase 2: Wait for all of them
    return futures.stream()
        .map(CompletableFuture::join)
        .toList();
}

```

**Where it matters:** N+1 API calls. A developer assumes that just because `supplyAsync` is in the code, it runs in parallel. `join()` is a blocking barrier; place it only after all tasks are in flight.

### Scenario: The Orphaned Workers (Fail-Fast Leak)

An orchestration service gathers data from three independent microservices to build a dashboard. It demands all three succeed.

```java
CompletableFuture<Inventory> invTask = CompletableFuture.supplyAsync(api::getInventory, pool);
CompletableFuture<Pricing> priceTask = CompletableFuture.supplyAsync(api::getPricing, pool);
CompletableFuture<Reviews> reviewTask = CompletableFuture.supplyAsync(api::getReviews, pool);

CompletableFuture.allOf(invTask, priceTask, reviewTask).join();

```

Under load, `getPricing()` fails almost immediately (in 50ms) with a `500 Internal Server Error`. The `allOf().join()` call throws an exception immediately, failing the request fast. The web thread is freed and returns an error to the user.

What is happening in the background?

**The Root Cause:**
`CompletableFuture.allOf()` is fail-fast in its result, but **it does not cancel the sibling tasks**.
When `priceTask` fails at 50ms, `allOf` completes exceptionally. But `invTask` and `reviewTask` (which might take 2 seconds each) continue running to completion on the `pool`.

Because the parent request already failed and returned an error to the user, the results of the inventory and review tasks will be completely ignored when they finally finish. Under high error rates, your thread pools and downstream services get hammered by "orphan" tasks doing heavy work for requests that have already been aborted.

**The Fix:** You must explicitly handle sibling cancellation on failure.

```java
CompletableFuture.allOf(invTask, priceTask, reviewTask)
    .exceptionally(ex -> {
        // If anything fails, explicitly cancel the others
        invTask.cancel(true);
        priceTask.cancel(true);
        reviewTask.cancel(true);
        throw new CompletionException(ex);
    }).join();

```

*(Note: As seen in Card #12, this only stops downstream chained stages. If true thread interruption is needed, the tasks must be coded to respect thread interrupts.)*

### Describe a code snippet #14: The Virtual Thread Pinning Trap

```java
// Moving to Java 21+ Virtual Threads to eliminate standard thread pools
private final ObjectMapper mapper = new ObjectMapper();

public String processSafely(Data data) {
    // A synchronized block to prevent concurrent modification of shared state
    synchronized (this) {
        String json = mapper.writeValueAsString(data);
        return remoteApi.send(json); // Blocking I/O call
    }
}

```

*Note: This is invoked by a web framework using Virtual Threads (e.g., Spring Boot 3.2+ with `spring.threads.virtual.enabled=true`).*

**Defect:** This causes "Thread Pinning," which destroys the entire benefit of Virtual Threads.
Virtual threads achieve massive scalability by unmounting from the underlying OS "carrier thread" whenever they hit a blocking I/O call (like `remoteApi.send()`), allowing the carrier thread to serve other requests.
However, **if a virtual thread blocks while inside a `synchronized` block or method**, the JVM currently cannot unmount it. The virtual thread is "pinned" to the OS thread. The OS thread goes to sleep waiting for the network, entirely defeating the virtual thread architecture. If you have 200 OS threads, just 200 concurrent calls to this method will exhaust the server, exactly like the old blocking model.

**Fix:** Replace the `synchronized` keyword with a `ReentrantLock`. The JVM knows how to unmount virtual threads that block while holding `java.util.concurrent.locks`.

```java
private final ReentrantLock lock = new ReentrantLock();

public String processSafely(Data data) {
    lock.lock();
    try {
        String json = mapper.writeValueAsString(data);
        return remoteApi.send(json);
    } finally {
        lock.unlock();
    }
}

```

**Where it matters:** The exact moment you step off the intermediate `CompletableFuture` ladder and transition to the final step of modern Java concurrency: Virtual Threads. Old codebase habits (`synchronized`) become fatal architectural bottlenecks.