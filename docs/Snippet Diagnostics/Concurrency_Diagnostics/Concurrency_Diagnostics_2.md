### Describe a code snippet #11
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Flag {
  private boolean ready = false;
  private int data = 0;

  void writer() {
    data = 42;
    ready = true;
  }

  int reader() {
    while (!ready) { }
    return data;
  }
}
```

</details>

<details><summary>Show answer</summary>

Two problems. First, `reader()` may spin forever: `ready` has no visibility promise, so the reader can keep
seeing its cached `false`. Second, even if it does see `ready` become true, it can still read `data == 0` —
without a happens-before edge, the two writes can appear to the reader in the other order, or `data`'s write may
not be visible yet.

Fix: make `ready` `volatile`. A `volatile` write publishes everything the writer did before it, so a reader that
sees `ready == true` is guaranteed to see `data == 42`. One `volatile` on the flag covers the plain field
written before it.

```java
private volatile boolean ready = false;
private int data = 0; // no volatile needed: the ready write publishes it
```

Also worth noting: the busy `while (!ready) {}` spin burns a core doing nothing. Correct once `ready` is
`volatile`, but a real design would block on a latch or a condition instead of spinning.

Handle: a `volatile` write is a fence — it publishes the plain writes that came before it, fixing both the spin
and the stale `data`.

</details>

</details>

### Describe a code snippet #12
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Registry {
  private final Set<String> names = new HashSet<>();

  public synchronized void add(String n) {
    names.add(n);
  }

  public boolean contains(String n) {
    return names.contains(n);
  }
}
```

</details>

<details><summary>Show answer</summary>

Only `add` holds the lock; `contains` reads the same `HashSet` with no lock at all. A read running while another
thread is mid-`add` can see the set during a resize and return a wrong answer, or in the worst case walk broken
internal links. Locking one half of the accesses to shared state is the same as not locking — both sides must
use the same lock.

Fix: lock both methods on the same monitor.

```java
public synchronized boolean contains(String n) {
  return names.contains(n);
}
```

Better rewrite: drop the manual locking and use a concurrent set, so reads never block writes.

```java
private final Set<String> names = ConcurrentHashMap.newKeySet();
// add / contains need no synchronized
```

Handle: a lock protects shared state only if every access takes it; guarding writes but not reads guards nothing.

</details>

</details>

### Describe a code snippet #13
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Printer {
  private final Integer lock = 0;

  public void print(String s) {
    synchronized (lock) {
      System.out.println(s);
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

Locking on a boxed `Integer` is unsafe. Small `Integer` values come from a shared cache (`Integer.valueOf`
caches −128..127), so `0` here is the *same object* every other piece of code that boxes `0` also locks on —
unrelated code can block your print, and you can block it. Worse, if the field were ever reassigned the lock
object would change and two threads would lock on different monitors, so the block would stop excluding at all.
The same trap applies to `String` literals (interned, shared) and boxed `Boolean`.

Fix: lock on a private, final, plain `Object` that nothing else can reach.

```java
private final Object lock = new Object();
```

Handle: lock on a private object you own; boxed values and interned strings are shared, so locking on them locks
on something the rest of the program shares too.

</details>

</details>

### Describe a code snippet #14
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
synchronized (queue) {
  while (queue.isEmpty()) {
    queue.wait();
  }
  Object item = queue.remove();
  process(item);
}
```

</details>

<details><summary>Show answer</summary>

This one is actually correct on the point most people get wrong — `wait()` sits in a `while` loop, not an `if`,
so a spurious wakeup or a lost race just re-checks `isEmpty()` and waits again. That is the right shape.

The real problem is `process(item)` running while still holding the queue's lock. Any slow work — I/O, a
callback — is done with the monitor held, so no other thread can add to or take from the queue during it. Work
that does not need the lock must not be inside the locked region.

Fix: take the item under the lock, then release the lock before processing.

```java
Object item;
synchronized (queue) {
  while (queue.isEmpty()) queue.wait();
  item = queue.remove();
}
process(item); // outside the lock
```

Also: `wait()` throws `InterruptedException`; this block must handle or declare it, and a real consumer usually
loops forever, so cancellation has to break the outer loop.

Handle: hold the lock only for the shared-state part; never run slow work with a monitor held.

</details>

</details>

### Describe a code snippet #15
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public void process(List<Task> tasks) {
  ExecutorService pool = Executors.newFixedThreadPool(8);
  for (Task t : tasks) {
    pool.submit(t::run);
  }
  // method returns
}
```

</details>

<details><summary>Show answer</summary>

`newFixedThreadPool` creates non-daemon threads, and the pool is never shut down. Each call to `process` leaks
eight live threads that keep the JVM from exiting and pile up over time. The method also returns before the
tasks finish, with no way to know when they are done or whether any failed.

Fix: shut the pool down and wait for the tasks, in a `finally` so it happens even on error.

```java
ExecutorService pool = Executors.newFixedThreadPool(8);
try {
  for (Task t : tasks) pool.submit(t::run);
} finally {
  pool.shutdown();
  pool.awaitTermination(1, TimeUnit.MINUTES);
}
```

Design note: creating a fresh pool per call is wasteful — a pool is a long-lived resource, usually made once and
shared. And `submit` swallows any exception into the returned `Future`; if you never inspect the `Future`,
failures vanish silently. Collect the futures and check them, or use `execute` with an uncaught-exception
handler.

Handle: a pool is a resource you must close; leaving it open leaks non-daemon threads and hides task failures.

</details>

</details>

### Describe a code snippet #16
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class BankAccount {
  private int balance;

  public synchronized void deposit(int n) { balance += n; }
  public synchronized void withdraw(int n) { balance -= n; }

  public int getBalance() { return balance; }
}
```

</details>

<details><summary>Show answer</summary>

`deposit` and `withdraw` are guarded, but `getBalance` is not. A reader can see a stale `balance` — the value
from before a completed `deposit` on another thread — because nothing forces the writer's change to be visible
to an unsynchronized reader. There is no torn-value risk for an `int` read, but there is a staleness risk, which
is enough to return a wrong balance.

Fix: synchronize the reader on the same lock, so it sees the latest committed value.

```java
public synchronized int getBalance() { return balance; }
```

Note on why not just `volatile`: making `balance` volatile would fix this read's staleness, but `deposit`/
`withdraw` are read-modify-write and still need the lock, so the lock is already there — reusing it for the read
is simpler than mixing `volatile` and `synchronized` on one field.

Handle: a read of guarded state must take the same lock, or it can see a value from before the last write.

</details>

</details>

### Describe a code snippet #17
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Service {
  public void start() {
    Thread t = new Thread(() -> {
      while (true) {
        poll();
      }
    });
    t.start();
  }
}
```

</details>

<details><summary>Show answer</summary>

The thread loops on `while (true)` with no way to stop it and no interruption check, so it runs until the JVM
dies. `start()` can be called many times, each spawning another unkillable thread. The thread is also anonymous
and unnamed, so a stack dump during an incident cannot tell you what it is. And because it is a raw `new Thread`,
nothing bounds how many exist.

Fix: check the interrupt flag so the thread can be stopped, name it, and manage it through a pool or a stored
reference rather than a raw thread.

```java
private volatile boolean running = true;
Thread t = new Thread(() -> {
  while (running && !Thread.currentThread().isInterrupted()) {
    poll();
  }
}, "service-poller");
```

Better: submit the polling task to a `ScheduledExecutorService` and keep the returned handle to cancel it — that
replaces the hand-rolled loop and the lifecycle problem at once.

Handle: a `while(true)` thread with no interrupt check and no owner cannot be stopped; give every thread a stop
signal, a name, and an owner.

</details>

</details>

### Describe a code snippet #18
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
if (!map.containsKey(key)) {
  map.put(key, compute(key));
}
```

`map` is a `ConcurrentHashMap`.

</details>

<details><summary>Show answer</summary>

Each call on a `ConcurrentHashMap` is atomic on its own, but `containsKey` then `put` is two calls with a gap
between them. Two threads can both find the key missing, both `compute`, and both `put` — so `compute` runs
twice for one key and the second result overwrites the first. Using a concurrent map does not make a sequence of
its methods atomic; only single methods are atomic.

Fix: one atomic call that checks and inserts together.

```java
map.computeIfAbsent(key, this::compute);
```

`computeIfAbsent` runs `compute` at most once per absent key and returns the existing value otherwise. Caveat:
the mapping function runs while a bin is locked, so it must be quick and must not update the same map.

This is the check-then-act trap again, framed on a concurrent map to make the point that "concurrent" fixes the
single call, not the pair.

Handle: a concurrent map makes each method atomic, not a check-then-act pair; fold the pair into one atomic call.

</details>

</details>

### Describe a code snippet #19
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Holder {
  private Map<String, String> data;

  public Holder() {
    data = new HashMap<>();
    data.put("k", "v");
  }

  public String read(String key) {
    return data.get(key); // called from many threads, no writes after construction
  }
}
```

</details>

<details><summary>Show answer</summary>

The map is filled only in the constructor and never written again, so the reads themselves are fine — a
read-only `HashMap` is safe to share. The gap is publication: if a `Holder` is handed to other threads through a
data race (for example stored in a plain field another thread reads without synchronization), a reader can see
the `Holder` reference before the constructor's writes are visible, and read a null or half-built `data`.

The object is safe; how it is shared is not. The fix lives at the sharing point, not in this class.

Fix options, at the field that publishes the `Holder`:

```java
// any one of these gives safe publication:
private volatile Holder holder;              // volatile field
// or a final field set in a constructor
// or storing it in a ConcurrentHashMap / AtomicReference
```

Cheapest self-contained fix: make `data` `final`. Final fields written in the constructor are guaranteed visible
to any thread that sees the object, with no other synchronization — this closes the hole from inside the class.

```java
private final Map<String, String> data;
```

Handle: an immutable-after-construction object is still unsafe if it is published through a race; `final` fields
or a safe hand-off close the gap.

</details>

</details>

### Describe a code snippet #20
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Pool {
  private final ThreadLocal<SimpleDateFormat> fmt =
      ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

  public String format(Date d) {
    return fmt.get().format(d);
  }
}
```

</details>

<details><summary>Show answer</summary>

The intent is right: `SimpleDateFormat` is not thread-safe, so giving each thread its own copy through
`ThreadLocal` avoids sharing one across threads. That part is correct.

The trap is lifecycle. On a thread pool the threads live forever, so each pooled thread holds its
`SimpleDateFormat` for the life of the JVM, and the value is never removed. If the `Pool` object is
short-lived while the threads outlive it, the `ThreadLocal` entries pin objects that should have been collected
— a slow leak that only shows under a pool. `ThreadLocal` on pooled threads must be cleaned with `remove()` when
the work unit ends, or the value simply never goes away.

Better rewrite: the whole `ThreadLocal` is unnecessary on modern Java — use the immutable, thread-safe
`DateTimeFormatter`, share one instance, and the problem disappears.

```java
private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
```

Handle: `ThreadLocal` on a pooled thread lives as long as the thread unless you `remove()` it; prefer an
immutable shared formatter and skip the per-thread copy.

</details>

</details>
