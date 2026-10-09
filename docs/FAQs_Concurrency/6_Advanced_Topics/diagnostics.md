

### Describe a code snippet #3
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
// asyncClient: Netty-based HTTP client
@GetMapping("/report/{id}")
public CompletableFuture<Report> report(@PathVariable Long id) {
  return asyncClient.fetchAsync(id)
      .thenApply(data -> renderPdf(data));  // ~300 ms of CPU
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** a plain `thenApply` runs on the thread that completed the future: here, a Netty event-loop thread. That
thread serves many connections. For the 300 ms of `renderPdf`, every connection on that loop stalls: other requests'
replies, reads, and writes all wait. There is no error and no warning; unrelated requests just get slow, and some time
out.

**Fix:** move the CPU work to a CPU pool.

```java
return asyncClient.fetchAsync(id)
    .thenApplyAsync(data -> renderPdf(data), cpuPool);  // CPU work leaves the event loop
```

**Where it matters:** the stage runs on the event loop only if the future is still pending when `thenApply` is
attached. That is the usual case, since the reply comes later. If the future is already complete, the calling thread
runs the stage instead.

An event-loop thread serves many connections; anything slow on it stalls all of them.

</details>

</details>

### Describe a code snippet #3
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
// asyncClient: Netty-based HTTP client
@GetMapping("/report/{id}")
public CompletableFuture<Report> report(@PathVariable Long id) {
  return asyncClient.fetchAsync(id)
      .thenApply(data -> renderPdf(data));  // ~300 ms of CPU
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** a plain `thenApply` runs on the thread that completed the future: here, a Netty event-loop thread. That
thread serves many connections. For the 300 ms of `renderPdf`, every connection on that loop stalls: other requests'
replies, reads, and writes all wait. There is no error and no warning; unrelated requests just get slow, and some time
out.

**Fix:** move the CPU work to a CPU pool.

```java
return asyncClient.fetchAsync(id)
    .thenApplyAsync(data -> renderPdf(data), cpuPool);  // CPU work leaves the event loop
```

**Where it matters:** the stage runs on the event loop only if the future is still pending when `thenApply` is
attached. That is the usual case, since the reply comes later. If the future is already complete, the calling thread
runs the stage instead.

An event-loop thread serves many connections; anything slow on it stalls all of them.

</details>

</details>

### Describe a code snippet #2
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public CompletableFuture<Person> findPersonAsync(Long id) {
  return CompletableFuture.supplyAsync(() -> personDao.findById(id));  // personDao uses plain JDBC
}
```

</details>

<details><summary>Show answer</summary>

**Defect 1 (nothing becomes non-blocking):** `findById` still blocks on JDBC. A thread still waits for the full DB
call; it is just a common-pool thread instead of the request thread.

**Defect 2 (the wait lands in the common pool):** with no executor argument, `supplyAsync` uses
`ForkJoinPool.commonPool()`, which is sized for CPU work (cores − 1). A handful of slow queries fill it, and every other
`supplyAsync` and parallel stream in the JVM stalls behind them.

**Minimal fix (a dedicated pool):** keeps the damage inside one pool (bulkhead). Still blocking: one thread per
waiting query.

```java
return CompletableFuture.supplyAsync(() -> personDao.findById(id), dbPool);
```

**Rewrite (a non-blocking driver such as R2DBC):** no thread waits. Only this one actually saves threads.

**Where it matters:** if the common pool's parallelism is below 2 (a 1–2 core machine), `CompletableFuture` starts a
new thread per task instead. Then nothing freezes, but there is no limit on threads either.

Wrapping a blocking call moves the wait to another thread; it does not remove it.

</details>

</details>


### Describe a code snippet #6
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private final ExecutorService ioPool = new ThreadPoolExecutor(50, 50, 0, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(500));

public CompletableFuture<Person> person(Long id) {
  return CompletableFuture.supplyAsync(() -> personDao.findById(id), ioPool);       // DB, ~5 ms
}

public CompletableFuture<Score> score(Person p) {
  return CompletableFuture.supplyAsync(() -> bureauClient.fetchScore(p), ioPool);  // partner API, ~1 s
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** one pool serves two dependencies with very different speeds. When the partner API slows down, its calls
take all 50 threads, and the 5 ms DB calls wait in the queue behind them until the queue fills and rejects them too.
The DB is healthy, yet every DB-backed request fails along with the partner. The pool protects the request threads; it
does not protect the dependencies from each other.

**Fix:** one pool per dependency, each sized to that dependency (`boundedPool` = fixed threads + a bounded queue).

```java
private final ExecutorService dbPool = boundedPool(10, 100);      // 10 threads for the 10 JDBC connections
private final ExecutorService bureauPool = boundedPool(50, 200);  // sized to what the partner allows
```

**Where it matters:** at normal load one shared pool looks fine. It fails only when one dependency slows down.

A pool isolates only what sits on the other side of its wall: give each dependency its own.

</details>

</details>

---

### Describe a code snippet #7
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
// application.properties: spring.datasource.hikari.maximum-pool-size=10
private final ExecutorService dbPool = new ThreadPoolExecutor(200, 200, 0, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100));

public CompletableFuture<Person> person(Long id) {
  return CompletableFuture.supplyAsync(() -> personDao.findById(id), dbPool);  // personDao uses plain JDBC
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** 200 threads for 10 connections. At most 10 queries run at once; under load the other 190 threads wait
inside `getConnection()`, and after HikariCP's connection timeout (30 s by default) they fail with
`SQLTransientConnectionException`. Worse, those 190 waiting threads soak up the overload, so the bounded queue never
fills and never rejects: requests fail slowly after 30 s instead of fast.

**Fix:** as many threads as connections.

```java
private final ExecutorService dbPool = new ThreadPoolExecutor(10, 10, 0, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100));  // the overload now reaches the queue, which rejects it fast
```

**Where it matters:** only under load. With little traffic, 10 of the 200 threads do all the work and the rest sit
idle, so nothing looks wrong.

Threads beyond the connection count only wait for a connection, and they hide the overload a bounded queue would have
rejected.

</details>

</details>

---

### Describe a code snippet #8
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private final ExecutorService dbPool = Executors.newFixedThreadPool(10);  // 10 threads for 10 connections

@GetMapping("/person/{id}")
public CompletableFuture<Person> person(@PathVariable Long id) {
  return CompletableFuture.supplyAsync(() -> personDao.findById(id), dbPool);  // personDao uses plain JDBC
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** `newFixedThreadPool` puts an unbounded queue in front of its threads. When the DB slows down, tasks pile up
in memory. Clients reach the async timeout (Tomcat: 30 s) and get a `503`, but their tasks stay in the queue and still
run later. The DB then does work for clients who already left, which keeps it slow, and client retries add even more
tasks. Memory grows until the slowdown ends or the JVM runs out.

**Fix:** a bounded queue that rejects the extra work at once.

```java
private final ExecutorService dbPool = new ThreadPoolExecutor(10, 10, 0, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100));  // full queue: supplyAsync throws RejectedExecutionException; answer 503
```

**Where it matters:** size the queue by time, not by count. The longest wait in it is about queue size ÷ threads ×
query time: 100 ÷ 10 × 5 ms = 50 ms. If queries slow to 1 s, the same queue means a 10 s wait; the bound keeps even
that from growing further.

An unbounded queue turns a slowdown into work for clients who already left.

</details>

</details>

---

### Describe a code snippet #9
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private final ExecutorService dbPool = new ThreadPoolExecutor(10, 10, 0, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());

@GetMapping("/person/{id}")
public CompletableFuture<Person> person(@PathVariable Long id) {
  return CompletableFuture.supplyAsync(() -> personDao.findById(id), dbPool);  // personDao uses plain JDBC
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** when the queue is full, caller-runs executes the task on the thread that submitted it: the Tomcat request
thread, which then blocks on JDBC. The queue fills exactly when the DB slows down, so from that moment request threads
are pulled into the DB wait one by one. Soon no request thread is free, and endpoints that never touch the DB stop
too. The isolation fails at the moment it was built for.

**Fix:** keep the default abort policy and answer the overflow with a `503`.

```java
@ExceptionHandler(RejectedExecutionException.class)
ResponseEntity<Void> busy() {
  return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();  // fast answer; the request thread stays free
}
```

**Where it matters:** caller-runs is a good policy when the caller is a producer that may slow down, such as a batch
job feeding a pool: it acts as natural back-pressure. It is wrong only when the caller is a thread you meant to protect.

Caller-runs pushes the overflow back onto the caller; when the caller is a request thread, the isolation is gone.

</details>

</details>

---

### Describe a code snippet #10
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private final ExecutorService dbPool = new ThreadPoolExecutor(10, 10, 0, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100));

public CompletableFuture<Credit> credit(Long id) {
  return CompletableFuture.supplyAsync(() -> {
    Person p = personDao.find(id);
    Assets a = CompletableFuture.supplyAsync(() -> assetsDao.find(p), dbPool).join();
    return calculate(p, a);
  }, dbPool);
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** each outer task holds a pool thread while it waits in `join()` for an inner task queued in the same pool.
When 10 requests arrive together, all 10 threads are outer tasks waiting, and their inner tasks sit in the queue with
no thread left to run them. This is a thread-starvation deadlock, with no exception and no timeout: the requests hang
until the async timeout, and the pool stays stuck for good.

**Fix:** chain the steps instead of waiting inside a task.

```java
return CompletableFuture.supplyAsync(() -> personDao.find(id), dbPool)
    .thenCompose(p -> CompletableFuture.supplyAsync(() -> assetsDao.find(p), dbPool)
        .thenApply(a -> calculate(p, a)));  // no pool thread ever waits for another pool task
```

**Where it matters:** only when concurrent requests reach the pool size. At low load it works, so it passes tests and
hangs in production.

A task that waits for another task in its own pool can deadlock that pool; chain the steps instead.

</details>

</details>

---

### Describe a code snippet #11
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
@GetMapping("/scores")
public List<Score> scores(@RequestParam List<Long> ids) {
  return ids.parallelStream()
      .map(id -> restTemplate.getForObject("/score/{id}", Score.class, id))  // blocking HTTP call
      .toList();
}
```

</details>

<details><summary>Show answer</summary>

**Defect 1 (the shared pool):** a parallel stream runs on `ForkJoinPool.commonPool()` plus the calling thread.
Blocking HTTP calls hold those threads for the whole wait, so every other parallel stream, and every `supplyAsync`
without an executor, in the JVM waits behind them. It is the [common pool trap](#describe-a-code-snippet-2) through a
different door.

**Defect 2 (little speed gain):** the common pool has about one thread per core. 100 IDs on 8 cores run about 8 calls
at a time, so most of the waits still happen one after another.

**Fix:** a pool sized for the partner, and all calls started before any result is awaited.

```java
@GetMapping("/scores")
public CompletableFuture<List<Score>> scores(@RequestParam List<Long> ids) {
  List<CompletableFuture<Score>> calls = ids.stream()
      .map(id -> CompletableFuture.supplyAsync(() -> fetchScore(id), bureauPool))  // all calls start now
      .toList();
  return CompletableFuture.allOf(calls.toArray(CompletableFuture[]::new))
      .thenApply(done -> calls.stream().map(CompletableFuture::join).toList());   // join never waits here
}
```

**Where it matters:** parallel streams are fine for CPU work on in-memory data. The trouble is only blocking I/O
inside them.

Parallel streams are for CPU work; blocking I/O inside them ties up the JVM's shared pool.

</details>

</details>

---

### Describe a code snippet #12
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private final RestTemplate restTemplate = new RestTemplate();  // default settings

public CompletableFuture<Score> score(Person p) {
  return CompletableFuture.supplyAsync(() -> restTemplate.getForObject(URL, Score.class, p.id()), bureauPool)
      .orTimeout(2, TimeUnit.SECONDS)
      .exceptionally(ex -> Score.UNKNOWN);
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** a `RestTemplate` created with default settings has no read timeout: it waits for the partner's reply
forever. When the partner stops answering, `orTimeout` gives the client a fallback after 2 s, but the `bureauPool`
thread stays blocked in the socket read. Each hung call takes a thread for good. The pool drains, and new calls wait in
the queue until it fills and rejects them. Clients see only fast fallbacks, so the drain stays hidden until the pool is
empty.

**Fix:** a timeout on the client itself; keep `orTimeout` for the fast answer.

```java
var factory = new SimpleClientHttpRequestFactory();
factory.setConnectTimeout(500);   // ms
factory.setReadTimeout(2_000);    // the call itself gives up, and the thread is freed
var restTemplate = new RestTemplate(factory);
```

**Where it matters:** every blocking client needs its own timeout: a query timeout for JDBC, a request timeout for the
JDK `HttpClient`, and so on. Without one, a hung dependency keeps the thread.

A timeout on the future answers the client; only a timeout on the client frees the thread.

</details>

</details>

---

### Describe a code snippet #13
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
@Transactional
public CompletableFuture<Void> transfer(long fromId, long toId, BigDecimal amount) {
  accountDao.debit(fromId, amount);
  return CompletableFuture.runAsync(() -> accountDao.credit(toId, amount), dbPool);
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** the transaction is bound to the calling thread. `credit` runs on a pool thread outside it, with its own
connection and its own commit. If `credit` fails, `debit` is already committed: the money leaves one account and never
arrives. The order is not fixed either: the transaction commits when the method returns, and `credit` may run before or
after that.

**Fix:** keep the whole transaction on one thread. If it must run in the background, move all of it into the task.

```java
public CompletableFuture<Void> transfer(long fromId, long toId, BigDecimal amount) {
  return CompletableFuture.runAsync(() -> transfers.transferNow(fromId, toId, amount), dbPool);
}
```

`transferNow` is a `@Transactional` method on another bean that does both steps. Called on `this`, the proxy would be
skipped and no transaction would start.

**Where it matters:** without failures it works, and tests usually have none. It breaks only when the second step fails
or runs late.

A transaction cannot cross a thread hop: keep every step of one transaction on one thread.

</details>

</details>

---

### Describe a code snippet #14
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
@GetMapping("/orders/{id}")
public CompletableFuture<Order> order(@PathVariable Long id) {
  return CompletableFuture.supplyAsync(() -> orderService.find(id), dbPool);
}

// in OrderService
@PreAuthorize("hasRole('USER')")
public Order find(Long id) {
  return orderDao.findById(id);
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** Spring Security keeps the logged-in user on the request thread (a `ThreadLocal`). The pool thread has no
user, so `@PreAuthorize` finds no authentication and the call fails with `AuthenticationCredentialsNotFoundException`,
even for a logged-in user.

**Fix:** wrap the pool so each task carries the context of the thread that submitted it.

```java
private final ExecutorService dbPool =
    new DelegatingSecurityContextExecutorService(rawDbPool);  // copies the submitter's user into each task
```

**Wrong fix:** switching the holder to `MODE_INHERITABLETHREADLOCAL`. An inheritable `ThreadLocal` is copied only when
a thread is created. Pool threads are created once and reused, so each keeps the user of whichever request created it,
and later tasks run as that user: one user's identity leaks into other users' calls.

Thread-bound context must be copied per task, not per thread; pool threads outlive the request that created them.

</details>

</details>

### Describe a code snippet #15
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public CompletableFuture<Score> score(Person p) {
  return CompletableFuture.supplyAsync(() -> bureauClient.fetchScore(p), bureauPool)
      .exceptionally(ex -> {
        if (ex instanceof BureauUnavailableException) {
          return Score.UNKNOWN;
        }
        throw new IllegalStateException(ex);
      });
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** the fallback never runs. When a task in `supplyAsync` throws, the future stores the exception wrapped in a
`CompletionException`, and that wrapper is what `exceptionally` receives. `ex instanceof BureauUnavailableException` is
false, so every partner outage becomes an error instead of `Score.UNKNOWN`.

**Fix:** unwrap before checking.

```java
.exceptionally(ex -> {
  Throwable cause = ex instanceof CompletionException ce ? ce.getCause() : ex;
  if (cause instanceof BureauUnavailableException) {
    return Score.UNKNOWN;
  }
  throw new IllegalStateException(cause);
});
```

**Where it matters:** both forms reach you. An exception thrown inside a stage arrives wrapped. A future failed directly
with `completeExceptionally` (as `orTimeout` does with its `TimeoutException`) passes its exception unwrapped to a
stage attached right to it. Unwrapping handles both.

Exceptions in a future chain usually arrive wrapped in `CompletionException`; unwrap before you check the type.

</details>

</details>

---

### Describe a code snippet #16
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
@PostMapping("/orders")
public Order create(@RequestBody Order o) {
  Order saved = orderService.save(o);
  CompletableFuture.runAsync(() -> auditClient.send(saved), auditPool);  // result ignored
  return saved;
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** the returned future is dropped. If `send` throws, the exception is stored in a future nobody reads: no log,
no retry, no alert. Audit events disappear silently. With `executor.execute(...)`, an uncaught exception would at least
reach the thread's handler and be logged; `runAsync` captures it instead.

**Fix (minimum):** attach error handling to every fire-and-forget future.

```java
CompletableFuture.runAsync(() -> auditClient.send(saved), auditPool)
    .whenComplete((ok, ex) -> {
      if (ex != null) {
        log.warn("audit send failed for order {}", saved.id(), ex);
      }
    });
```

**Where it matters:** logging only tells you an event was lost. If the audit must never be lost, a background thread is
the wrong tool: write the event to an outbox table in the same transaction as the order, and let a separate job send
it (the outbox pattern).

A dropped future swallows its exception; every fire-and-forget needs its own error handling.

</details>

</details>

---

### Describe a code snippet #17
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
@Service
public class ReportService {

  public CompletableFuture<Report> reportFor(long id) {
    return buildAsync(id);
  }

  @Async("reportPool")
  public CompletableFuture<Report> buildAsync(long id) {
    return CompletableFuture.completedFuture(build(id));  // ~3 s of DB work
  }
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** `@Async` works through the Spring proxy, and a call on `this` skips the proxy. So `buildAsync` runs on the
caller's thread (the request thread) for the full 3 s, then returns a future that is already complete. It looks async
and is not. There is no error and no warning.

**Fix:** call it through the proxy (from another bean, e.g. the controller calls `buildAsync` directly), or drop the
annotation and hop explicitly:

```java
public CompletableFuture<Report> reportFor(long id) {
  return CompletableFuture.supplyAsync(() -> build(id), reportPool);  // the hop is visible in the code
}
```

**Where it matters:** only thread names in the logs, or a profiler, show where the work really runs.

Annotation-driven async works only through the proxy; a call on `this` runs on the caller's thread.

</details>

</details>

---

### Describe a code snippet #18
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
@Transactional
public void approve(long orderId) {
  Order order = orderRepo.findById(orderId).orElseThrow();
  Score score = bureauClient.fetchScore(order.customerId());  // partner API, ~1 s, sometimes 10 s
  order.approve(score);
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** the transaction holds its DB connection from start to end, including the partner call. Each approval keeps
a connection for 1–10 s while doing nothing with it. With 20 connections, 20 slow approvals take the whole pool, and
every other DB request, even a 2 ms read, waits for a connection. A slow partner now slows the DB layer. Any row locks
the transaction takes are held just as long.

**Fix:** call the partner outside the transaction; keep the transaction for the DB work only.

```java
public void approve(long orderId) {
  long customerId = orders.customerIdOf(orderId);     // short read
  Score score = bureauClient.fetchScore(customerId);  // no connection held during the call
  orders.approve(orderId, score);                     // short @Transactional write, on another bean
}
```

**Where it matters:** the order can change between the read and the write. Guard the write with optimistic locking (a
`@Version` field), and retry or reject when it fails.

A transaction holds its connection for its whole length; keep remote calls out of it.

</details>

</details>

---

### Describe a code snippet #19
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private final ExecutorService bureauPool = boundedPool(100, 200);  // the partner allows 100 calls at once

private final RestTemplate restTemplate = new RestTemplate(
    new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()));

public CompletableFuture<Score> score(Person p) {
  return CompletableFuture.supplyAsync(() -> restTemplate.getForObject(URL, Score.class, p.id()), bureauPool);
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** 100 threads share an HTTP connection pool that allows 5 connections per host (Apache HttpClient 5's
default). At most 5 partner calls run at once, and the other 95 threads wait for a free connection. The partner would
take 100 and the thread pool is sized for 100, but the real limit is a default nobody set.

**Fix:** size the connection pool to match.

```java
var connections = PoolingHttpClientConnectionManagerBuilder.create()
    .setMaxConnPerRoute(100)
    .setMaxConnTotal(100)
    .build();
var restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(
    HttpClients.custom().setConnectionManager(connections).build()));
```

**Where it matters:** in your metrics it looks like a slow partner, because each call's time includes the wait for a
connection. The partner's own metrics show low traffic.

Every pool on a call's path caps it; the smallest one sets the real limit.

</details>

</details>

---
### Describe a code snippet #20
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
@GetMapping("/heavy-report")
public CompletableFuture<Report> heavyReport() {
  return CompletableFuture.supplyAsync(() -> warehouse.runAggregation(), reportPool);  // up to ~45 s at peak
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** the query can run longer than the framework's async timeout (30 s). At 30 s the client gets a `503`, but
the task keeps running: its pool thread and DB connection stay busy for the full 45 s, for a client who is already
gone. Under a spike, `reportPool` and the connection pool fill with such tasks, and the warehouse does work nobody will
read.

**Fix:** a timeout on the query itself, shorter than the async timeout. Add `orTimeout` only for a fast answer to the
client.

```java
jdbcTemplate.setQueryTimeout(25);  // seconds: the query is cancelled, the call fails, and the thread is freed

return CompletableFuture.supplyAsync(() -> warehouse.runAggregation(), reportPool)
    .orTimeout(25, TimeUnit.SECONDS)            // answers the client; does not stop the query
    .exceptionally(ex -> Report.timedOut());
```

**Where it matters:** `orTimeout` alone looks like a fix, because the client gets its fallback, while the thread and
the query keep running. How the query is cancelled depends on the driver; most send a cancel to the DB when the timeout
hits. If the report regularly takes longer than any timeout allows, it needs an async API, not a timeout.

Only a timeout on the work itself stops the work; timeouts on the future or the request only stop the waiting.

</details>

</details>

---

### Describe a code snippet #21
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
@Bean
ThreadPoolTaskExecutor apiPool() {
  var pool = new ThreadPoolTaskExecutor();
  pool.setCorePoolSize(10);
  pool.setMaxPoolSize(50);  // "grows to 50 under load"
  return pool;
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** the queue capacity is not set, so the queue is unbounded. A thread pool adds threads above its core size
only when its queue is full, and an unbounded queue is never full. So the pool never grows past 10 threads: the max of
50 is dead configuration, and under load, tasks pile up in the queue behind 10 threads, as in
[snippet #8](#describe-a-code-snippet-8).

**Fix:** decide what you want and configure exactly that.

```java
pool.setCorePoolSize(50);
pool.setMaxPoolSize(50);     // a fixed 50 threads
pool.setQueueCapacity(100);  // then at most 100 waiting; the rest are rejected
```

**Where it matters:** plain `ThreadPoolExecutor` follows the same rule, since `ThreadPoolTaskExecutor` wraps it. With a
bounded queue the growth still comes late: the pool first fills the queue and only then adds threads, so under a spike
tasks wait in the queue while threads could still be added. That's why core = max is the usual choice for I/O pools.

A pool grows past its core size only when its queue is full; with an unbounded queue, the max size is never used.

</details>

</details>

