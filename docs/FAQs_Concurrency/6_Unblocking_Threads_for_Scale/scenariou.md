

### Credit endpoint — 5,000 requests at once
<details><summary><strong>Show details</strong></summary>

<details><summary>Show scenario</summary>

A Spring Boot credit service (MVC on Tomcat) runs on Java 21 with 8 cores. Each request:

1. loads the person from the DB (~5 ms),
2. calls a partner credit-bureau API with the person (~1 s),
3. loads the person's liabilities from the DB (~5 ms),
4. computes the credit (~1 ms of CPU).

Today: plain blocking code, 200 Tomcat threads, a JDBC pool of 100. A campaign is coming: up to 5,000 requests in
flight at once, each answered in under 2 s. A load test at 1,000 in flight already shows ~5 s response times, while the
CPU stays under 5%.

What do you change, and why?

</details>

<details><summary>Show answer</summary>

**Clarifying questions (the numbers answer most of them):**
- **Where does the time go?** About 1 s waiting and 1 ms of CPU. The CPU is idle, so threads run out, not CPU. 200
  threads × ~1 s each ≈ 200 requests/s; the other 800 wait in the queue for ~4 s.
- **Can the downstream take 5,000 in flight?** That is ~4,950 requests/s. DB: 2 queries × 5 ms ≈ 50 busy
  connections, so the pool of 100 fits. CPU: ~5 of 8 cores, which fits. Partner API: it must accept ~5,000 calls at
  once, and so must your local HTTP client's connection pool. If that pool is capped at 100 connections, 4,900
  requests just wait for a free connection, and the 2 s target is lost. Confirm this first, or nothing below helps.
- **Must the client get the credit in the same call?** Yes, in under 2 s, so keep the synchronous contract.

**Options:**
1. **Bad: raise `maxThreads` to 5,000.** 5,000 OS threads means ~5 GB of reserved stack and heavy OS scheduling. It
   pays for the waiting instead of removing it, and the next growth needs even more threads.
2. **Bad: wrap the calls in `supplyAsync`.** The wait only moves to another pool. You still have ~5,000 blocked threads,
   or a frozen common pool.
3. **Bad here: async API (`202` + polling).** It changes the contract for every client, and it is not needed when the
   answer fits in 2 s.
4. **Works, but costly: non-blocking rewrite** (`CompletableFuture` or WebFlux, R2DBC, a non-blocking HTTP client).
    - **Buys:** a few threads carry 5,000 waits; works on any Java version; supports back-pressure and streaming.
    - **Costs:** every layer is rewritten (drivers, transactions, error handling), stack traces become harder to read,
      and the team must learn the style.
5. **Right: virtual threads.** Set `spring.threads.virtual.enabled=true` (Spring Boot 3.2+) and keep the code as is.
   Each waiting request costs a small heap object instead of an OS thread. Tomcat's default `maxConnections` (8,192)
   already covers 5,000 open connections; virtual threads remove the need for 5,000 matching OS threads. One check on
   Java 21: `synchronized` around blocking I/O pins the OS thread (fixed in JDK 24).

**The test that decides:** find what runs out first. Here it is threads (waits dominate, CPU idle), and the code is
blocking on Java 21, so virtual threads free the threads without a rewrite. On older Java, or when you need streaming
and back-pressure, use the non-blocking rewrite. If the partner or the DB runs out first, fix that instead (limits,
caching, or an async API); no thread model helps there.

</details>

</details>


### One server, three kinds of work
<details><summary><strong>Show details</strong></summary>

<details><summary>Show scenario</summary>

A Spring Boot service (MVC on Tomcat, 200 request threads) runs on Java 17 with 8 cores. It serves three endpoints:

- `/quote`: pure CPU work, ~20 ms. The busiest endpoint.
- `/orders`: two DB queries of ~10 ms each. The JDBC pool has 20 connections.
- `/score`: one call to a partner credit API, usually ~300 ms. The partner allows 100 calls at once and sometimes
  slows to 10 s.

Yesterday the partner slowed down, and `/quote`, which never calls the partner, stopped answering too. The CPU stayed
under 10%.

Design the thread use so that a slow partner cannot take down the rest.

</details>

<details><summary>Show answer</summary>

**Clarifying questions:**
- **Why did `/quote` stop?** Each `/score` request held a Tomcat thread for 10 s. A few hundred of them filled all 200
  threads, and `/quote` requests queued behind them while the CPU sat idle. The problem is isolation, not capacity.
- **What can each dependency take?** DB: 20 connections. Partner: 100 calls at once. CPU: 8 cores.
- **Must clients get the result in the same call?** Yes, so the synchronous contract stays.

**Options:**
1. **Bad: raise `maxThreads`.** A 10 s slowdown fills any number of threads; it only delays the failure.
2. **Bad: one shared async pool for DB and partner.** The partner takes the shared pool, and `/orders` fails with it
   ([snippet #6](#describe-a-code-snippet-6)).
3. **Right on Java 17: one pool per dependency; the handlers return futures.**
    - `/score`: `bureauPool` with 100 threads (the partner's limit), a bounded queue, the default abort policy (`503` on
      overflow), a 2 s read timeout on the client, and `orTimeout` for a fast fallback.
    - `/orders`: `dbPool` with 20 threads (the JDBC pool) and a bounded queue.
    - `/quote`: stays on the Tomcat thread. It is CPU work; a hop to another pool would add nothing.
    - Security and logging context is copied per task, and no transaction spans a hop.
    - **Buys:** a slow partner costs at most 100 threads plus its queue; `/quote` and `/orders` keep working.
    - **Costs:** pools to size and watch, context copying, and async code in two endpoints.
4. **Add-on: a circuit breaker on the partner.** When the partner is slow, calls fail at once instead of filling
   `bureauPool`. It complements the pools; it does not replace them.
5. **On Java 21: virtual threads, plus a `Semaphore` per dependency** (100 permits for the partner, 20 for the DB). Same
   isolation, with plain blocking code.
6. **Costly here: a non-blocking client for the partner.** It removes the 100 waiting threads, but the partner still
   takes only 100 calls at once, so the gain is small.

**The test that decides:** the failure was one slow dependency borrowing everyone's threads. Give each dependency its
own limit, sized to that dependency, and reject fast when it is full.

</details>

</details>


### Whole service down, CPU idle
<details><summary><strong>Show details</strong></summary>

<details><summary>Show scenario</summary>

At 14:05 a Spring Boot service (MVC on Tomcat) stops answering on every endpoint, including `/actuator/health`. The
CPU is at 3%. A thread dump shows:

- all 200 `http-nio-8080-exec-*` threads waiting in `CompletableFuture.join`, called from `CreditController.credit`;
- all 50 `bureau-pool-*` threads blocked in a socket read inside `RestTemplate.getForObject`;
- the bureau pool's queue holding 3,000 tasks.

What went wrong, and what do you fix, in which order?

</details>

<details><summary>Show answer</summary>

**Reading the dump:**
- **Request threads in `join`:** the controller waits on the future itself ([snippet #1](#describe-a-code-snippet-1)).
  The async code frees nothing.
- **Pool threads in a socket read:** the partner call hangs and nothing stops it, because the `RestTemplate` has no
  read timeout ([snippet #12](#describe-a-code-snippet-12)).
- **3,000 queued tasks:** the queue is unbounded ([snippet #8](#describe-a-code-snippet-8)), so work keeps piling up
  for clients who already gave up.
- **Health down too:** a health request also needs a Tomcat thread, and there are none.

**The chain:** the partner hangs → pool threads hang with it → every request thread waits on a pool task → Tomcat has
no free thread → everything stops, health included. If the health check is a liveness probe, the platform restarts the
service, which hides the cause and throws away the queued work.

**Fixes, most important first:**
1. **Read and connect timeouts on the `RestTemplate`:** stops the hang at its source and frees the pool threads.
2. **Return the future instead of calling `join()`:** request threads stop waiting on the partner.
3. **A bounded queue with the default abort policy:** overflow becomes a fast `503` for score calls only.
4. **`orTimeout` plus a fallback:** clients get an answer before the async timeout.

**The test that decides:** in a thread dump, look at where each pool's threads are stuck. Request threads in `join`
mean the async boundary is broken; pool threads in a socket read mean a client timeout is missing.

</details>

</details>


### DB endpoints slow when the partner is slow
<details><summary><strong>Show details</strong></summary>

<details><summary>Show scenario</summary>

A Spring Boot service uses PostgreSQL through HikariCP (20 connections). When the partner credit API slows from 300 ms
to 5 s, even `/orders/{id}`, a single 2 ms query, starts taking seconds, and some calls fail with
`SQLTransientConnectionException`.

DB monitoring during the slowdown: DB CPU at 5%, almost no queries running, and `pg_stat_activity` shows about 20
connections in the state `idle in transaction`.

What is happening, and what do you change?

</details>

<details><summary>Show answer</summary>

**Reading the clues:**
- **DB CPU low, almost no queries running:** the DB is not slow. Connections are held, not used.
- **`idle in transaction`:** a transaction is open, but no query runs on it. The app holds the connection while doing
  something else.
- **The slowdown follows the partner:** that something else is the partner call.

**Cause:** a `@Transactional` method calls the partner inside the transaction
([snippet #18](#describe-a-code-snippet-18)). Each call holds a connection for the partner's 5 s, 20 of them take the
whole pool, and `/orders` waits for a connection.

**Options:**
1. **Bad: raise the pool to 200.** In PostgreSQL each connection is a server process the DB pays for, and the next
   slowdown fills 200 as easily as 20.
2. **Bad: a shorter HikariCP timeout.** Requests fail faster; nothing is fixed.
3. **Right: move the partner call out of the transaction.** A short read, the call with no connection held, then a
   short write guarded by optimistic locking.
4. **Plus: a read timeout on the partner client,** so no call holds anything for long.

**The test that decides:** a connection that is `idle in transaction` means the transaction lasts longer than its DB
work. Find what else runs inside it.

</details>

</details>

---

### Customer sees another customer's report
<details><summary><strong>Show details</strong></summary>

<details><summary>Show scenario</summary>

A multi-tenant service stores the tenant ID from each request in a `TenantContext` (a `ThreadLocal`). The repository
layer reads it to add `WHERE tenant_id = ?` to every query. Last sprint, report generation moved to `reportPool`
(20 threads).

Since then, rarely, a customer gets a report with another customer's data. It never happens in tests, only under
load, and the wrong tenant is always one that requested a report a few minutes earlier.

What is the cause, and what is the fix?

</details>

<details><summary>Show answer</summary>

**Reading the clues:**
- **Only in async reports:** the thread hop is involved.
- **The wrong tenant made a request earlier:** a stale value is left on a reused pool thread.
- **Never in tests:** tests rarely run many tenants through the same threads.

**Likely causes (both leave a stale tenant on a pool thread):**
- `TenantContext` uses an `InheritableThreadLocal`. A pool thread created during tenant A's request keeps tenant A for
  its whole life.
- The task sets the tenant but never clears it, and some path (a retry, a missing header) runs without setting it, so
  it reads whatever the last task left.

**Fix:** copy the tenant per task, and clear it when the task ends.

```java
executor.setTaskDecorator(task -> {
  String tenant = TenantContext.get();  // captured on the submitting thread
  return () -> {
    TenantContext.set(tenant);
    try {
      task.run();
    } finally {
      TenantContext.clear();            // the pool thread leaves clean
    }
  };
});
```

Also make the repository fail when no tenant is set, instead of using whatever is there.

**The test that decides:** a value from an earlier request means a per-thread value outlived its task. Look for a
missing clear or an inheritable `ThreadLocal`.

</details>

</details>

---

### A report takes 2 minutes
<details><summary><strong>Show details</strong></summary>

<details><summary>Show scenario</summary>

`GET /reports/yearly` builds a PDF from about a million rows, which takes 1–3 minutes. The service sits behind a load
balancer with a 60 s idle timeout. Users get a `504` after 60 s, while the server keeps building the report.

The team proposes two fixes: raise every timeout to 5 minutes, or return a `CompletableFuture` from the endpoint.

What do you do?

</details>

<details><summary>Show answer</summary>

**Clarifying questions:**
- **Must the user get the file in the same call?** No: they can come back for it.
- **How often?** A few dozen reports a day.
- **Can a failed report be restarted?** Yes.

**Options:**
1. **Bad: raise every timeout to 5 minutes.** The load balancer, proxies, client, and async timeout all have to change,
   and one dropped mobile connection still throws away 3 minutes of work.
2. **Bad: return a future.** It frees the request thread, but the HTTP call still lasts 2 minutes, and the load
   balancer still cuts it at 60 s. The problem is the length of the call, not the thread.
3. **Right: an async API.** `POST /reports` answers `202 Accepted` with a job URL. A background worker builds the
   report, and the client polls the job URL (or gets an email or webhook), then downloads the file.
    - **Buys:** no long HTTP call; survives disconnects; jobs can be limited and restarted.
    - **Costs:** a job table, a status endpoint, cleanup of old files, and clients that poll.
4. **Only for some data: stream the result.** It works when bytes can be sent as they are produced (CSV), not for a
   PDF that is complete only at the end.

**The test that decides:** when the work lasts longer than the timeouts on its path, no thread model helps; change the
contract.

</details>

</details>

---

### An import slows the API
<details><summary><strong>Show details</strong></summary>

<details><summary>Show scenario</summary>

One service runs a REST API and an hourly import. The import saves 200,000 rows one by one on an executor with 30
threads, using the same HikariCP pool (30 connections) as the API. The import must stay hourly.

During each import, API response times jump from 50 ms to 4 s. HikariCP's metrics show many threads waiting for a
connection, and DB CPU is at 60%.

What do you change?

</details>

<details><summary>Show answer</summary>

**Reading the clues:**
- **Threads waiting for connections:** the import's 30 threads hold all 30 connections, and API requests queue behind
  them.
- **DB CPU at 60%:** the DB is busy with 200,000 single-row inserts, one round trip each.

**Options:**
1. **Bad: a bigger shared pool.** The API gets connections again, but still competes with the import for DB CPU, and
   the next time someone adds import threads, they take the connections back.
2. **Right: separate limits for the import.** It gets its own small data source (e.g. 4 connections) and a 4-thread
   executor, while the API keeps its 30 connections.
3. **Plus: batch the inserts.** JDBC batching of a few hundred rows per round trip lets 4 connections do the work of
   30, and cuts the DB load.
4. **Stronger isolation: run the import as a separate deployment.** It then cannot touch the API's pools or threads at
   all.

**The test that decides:** two workloads on one resource need separate limits on that resource. Keep the less urgent
one small.

</details>

</details>

---

### Retries after a short partner outage
<details><summary><strong>Show details</strong></summary>

<details><summary>Show scenario</summary>

A mobile app calls your API with a 10 s timeout and tries up to 3 times. Your service calls a partner API with a
`RestTemplate` (10 s read timeout) and also tries up to 3 times.

The partner is down for 20 s. After it comes back, your service stays overloaded for another 10 minutes, and the
partner's logs show 9 times the normal call rate.

What went wrong, and how do you set timeouts and retries?

</details>

<details><summary>Show answer</summary>

**Reading the clues:**
- **9 times the rate:** 3 app tries × 3 service tries = up to 9 partner calls for one user action. Retries at two
  layers multiply.
- **Overload after the partner is back:** the retries arriving from all those users keep the load high long after the
  outage ends.
- **Equal 10 s timeouts:** by the time your first call times out, the app has already given up and sent a new request.
  Your retries then run for a client who is gone.

**Options:**
1. **Bad: more retries or longer timeouts.** Both add load exactly when the partner is weakest.
2. **Right: a timeout budget.** Each inner timeout must be shorter than the one outside it. For example: a 2 s partner
   timeout with at most 2 tries fits well inside the app's 10 s.
3. **Right: retry at one layer only, with growing delays plus a random part (jitter),** so retries don't arrive in waves.
   Retry only calls that are safe to repeat.
4. **Right: a circuit breaker on the partner.** After repeated failures, calls fail at once for a while, giving the
   partner room to recover.

**The test that decides:** count how many calls one user action can cause across all layers. Retries multiply, so
timeouts must shrink from the outside in.

</details>

</details>

---

### Memory grows during a partner outage
<details><summary><strong>Show details</strong></summary>

<details><summary>Show scenario</summary>

During a 30-minute partner outage, heap use climbs from 1 GB to 7 GB, and the service dies with an
`OutOfMemoryError`. The heap dump shows:

- 1.8 million `CompletableFuture` objects;
- 1.8 million tasks in the `LinkedBlockingQueue` of `bureau-pool`, each holding a request object of about 3 KB.

A thread dump taken earlier showed all 50 `bureau-pool` threads stuck trying to connect to the partner.

What went wrong, and what do you fix?

</details>

<details><summary>Show answer</summary>

**Reading the clues:**
- **`LinkedBlockingQueue` with 1.8 million tasks:** the pool's queue has no limit
  ([snippet #8](#describe-a-code-snippet-8)). Work arrived faster than it left, for 30 minutes.
- **Threads stuck connecting:** no connect timeout. A call to a host that does not answer can hang for minutes, so the
  50 threads barely move.
- **Futures that match the tasks one to one:** every task still waits for a client who gave up long ago.

**Fixes, most important first:**
1. **Connect and read timeouts on the client:** each call now ends in bounded time.
2. **A bounded queue with the default abort policy:** overflow becomes a fast `503` instead of heap.
3. **A circuit breaker:** during the outage, calls fail at once and never reach the queue.
4. **A fallback answer** where the business allows one.

**The test that decides:** in a heap dump, a huge queue of tasks means work arrives faster than it leaves. Bound the
queue, and make every task finish or fail in bounded time.

</details>

</details>



### Async registration leaves half-saved data
<details><summary><strong>Show details</strong></summary>

<details><summary>Show scenario</summary>

To speed up user registration, a developer moves the workspace creation and the welcome email to a pool:

```java
@Transactional
public void registerUser(UserDto dto) {
  User user = userRepository.save(new User(dto));

  var workspaceTask = CompletableFuture.runAsync(() -> workspaceService.create(user), pool);
  var emailTask = CompletableFuture.runAsync(() -> emailService.sendWelcome(user), pool);

  CompletableFuture.allOf(workspaceTask, emailTask).join();
}
```

During an email outage, the database fills with workspaces whose user does not exist (the `workspaces` table has no
foreign key to `users`). On other days, some people get a welcome email for an account that was never created.

What is happening, and how should registration be built?

</details>

<details><summary>Show answer</summary>

**Reading the clues:**
- **Workspaces without users:** `workspaceService.create` runs on a pool thread, outside the request thread's
  transaction, with its own connection and its own commit. When the email task fails, `join()` throws and the user
  insert rolls back, but the workspace is already committed.
- **Emails for accounts that don't exist:** the email is sent before the user's transaction commits. If anything fails
  after that, the user rolls back and the email has already gone out.
- **Clarifying question: what is actually slow?** The email, a network call. The workspace is a fast local insert, so
  moving it off the thread saves nothing and breaks the transaction.

**Options:**
1. **Bad (the current code): writes split across threads.** Two transactions that cannot succeed or fail together.
2. **Bad here: saga with compensation** (delete the workspace when the email fails). Compensation is for writes in
   different services. Here both writes hit the same DB, so one local transaction is simpler and correct.
3. **Right: user and workspace in one transaction on the request thread; the email after the commit.** Send it from an
   after-commit listener (`@TransactionalEventListener(phase = AFTER_COMMIT)`), or, if it must never be lost, write it
   to an outbox table in the same transaction and let a separate job send it.

With a foreign key on `workspaces`, the pool thread could not see the uncommitted user at all: depending on the DB, the
insert fails or waits for the user's transaction, which is itself waiting in `join()`.

**The test that decides:** which writes must succeed or fail together? Keep those on one thread, in one transaction.
Only side effects outside the DB go async, and only after the commit.

</details>

</details>

### Describe a code snippet #22
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public CompletableFuture<Data> load(long id) {
  Data hit = cache.getIfPresent(id);
  return hit != null
      ? CompletableFuture.completedFuture(hit)                       // cache hit: already complete
      : CompletableFuture.supplyAsync(() -> repo.find(id), dbPool);  // miss: completes later, on dbPool
}

@GetMapping("/calculate/{id}")
public CompletableFuture<Result> calculate(@PathVariable long id) {
  return load(id).thenApply(data -> heavyCpuCalculation(data));  // ~200 ms of CPU
}
```

</details>

<details><summary>Show answer</summary>

**Defect:** a plain `thenApply` runs on whichever thread completes the future, or on the calling thread if the future
is already complete. So the CPU work lands in two wrong places, depending on the cache:

- **Cache hit:** the future is already complete, so the Tomcat request thread runs 200 ms of CPU. That's exactly the
  thread the future was meant to free.
- **Cache miss:** the `dbPool` thread that finished the query runs it. That pool is sized to the DB connections, and
  while it computes, DB calls wait for a thread.

**Fix:** name the pool for any stage that does real work.

```java
return load(id).thenApplyAsync(data -> heavyCpuCalculation(data), cpuPool);  // always on cpuPool
```

**Where it matters:** the thread changes with the hit rate, so a test with a warm cache sees only the first case, and
one with a cold cache only the second. A plain `thenApply` is safe for short steps, like mapping to a DTO.

A non-async stage runs on whichever thread completes the future, or on the caller if it is already complete; name the
pool for any stage that does real work.

</details>

</details>

---

### Some exports show admin-only data
<details><summary><strong>Show details</strong></summary>

<details><summary>Show scenario</summary>

To pass the user into pool tasks, a developer writes a wrapper that copies the security context:

```java
public class SecurityContextAwareRunnable implements Runnable {
  private final Runnable delegate;
  private final SecurityContext context;

  public SecurityContextAwareRunnable(Runnable delegate) {
    this.delegate = delegate;
    this.context = SecurityContextHolder.getContext();  // captured on the request thread
  }

  @Override
  public void run() {
    SecurityContextHolder.setContext(context);  // applied on the pool thread
    delegate.run();
  }
}
```

Most code wraps its tasks with it. A week later: rarely, a regular user's export contains admin-only data. When the
same user retries, they get an access error instead. The export code turns out to call
`pool.execute(() -> exportService.export(filter))` without the wrapper.

What is happening, and what do you fix?

</details>

<details><summary>Show answer</summary>

**Reading the clues:**
- **Admin data one time, an access error the next:** the result depends on which pool thread runs the task, so a
  per-thread value is involved.
- **The export task is not wrapped:** it runs with whatever context its pool thread was left with.
- **The wrapper never clears:** after an admin's wrapped task, the thread keeps the admin's context. An unwrapped task
  on that thread runs as the admin; on a clean thread it runs as nobody and fails.

**Fixes:**
1. **Clean up after every task:** restore the thread's previous context in `finally`. A thread must leave a task as
   clean as it came.
2. **Wrap the executor, not each task:** `new DelegatingSecurityContextExecutorService(pool)` (Spring Security's own
   wrapper, which also cleans up) covers every submit, so no call site can forget.
3. **Fail closed:** with the cleanup in place, a task that arrives with no user is refused instead of borrowing
   someone's rights.

**The test that decides:** a stale `ThreadLocal` shows up as behavior that changes with the thread running the task.
Clean up per task, and put the copying on the executor, so no call site can skip it.

</details>

</details>