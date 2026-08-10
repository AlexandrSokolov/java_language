### Describe a code snippet #31
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Once {
  private boolean done;

  public void perform() {
    if (done) return;
    done = true;
    runExpensiveSetup();
  }
}
```

</details>

<details><summary>Show answer</summary>

Two threads can both read `done == false` before either sets it, so both pass the guard and both run
`runExpensiveSetup()` — the exact opposite of "once." `done` also has no visibility promise, so a second thread
may never see the `true` written by the first even after it returns, making a later call run setup a third time.

Fix: make the check-and-set one atomic step with `AtomicBoolean.compareAndSet`, which lets exactly one caller win.

```java
private final AtomicBoolean done = new AtomicBoolean(false);
public void perform() {
  if (done.compareAndSet(false, true)) {
    runExpensiveSetup();
  }
}
```

`compareAndSet(false, true)` returns true for the one thread that flips it and false for all others, so setup
runs once. A plain `boolean` plus `synchronized` also works but is heavier for a single flag.

Handle: "run once" needs an atomic test-and-set; a plain flag lets several threads pass the check together.

</details>

</details>

### Describe a code snippet #32
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Grid {
  private final Object lockA = new Object();
  private final Object lockB = new Object();

  public void left()  { synchronized (lockA) { synchronized (lockB) { work(); } } }
  public void right() { synchronized (lockB) { synchronized (lockA) { work(); } } }
}
```

</details>

<details><summary>Show answer</summary>

`left` takes A then B; `right` takes B then A. One thread in `left` holding A and one thread in `right` holding
B each wait for the lock the other holds — deadlock. This is the lock-ordering deadlock in its purest form: the
two lock objects are fine, the two methods are fine alone, but together they take the same pair in opposite
orders.

Fix: impose one global order on the two locks and make both methods follow it.

```java
public void left()  { synchronized (lockA) { synchronized (lockB) { work(); } } }
public void right() { synchronized (lockA) { synchronized (lockB) { work(); } } } // A before B, always
```

If different call sites genuinely need different logical operations, they can still all agree to acquire A before
B. The rule is not "which lock first makes sense here" but "one order, everywhere."

This is the same deadlock as the two-account transfer, stripped to bare locks so the order flip is impossible to
miss.

Handle: two locks taken in opposite orders by two methods deadlock; fix by one global acquire order for the pair.

</details>

</details>

### Describe a code snippet #33
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Buffer {
  private final List<byte[]> chunks = new ArrayList<>();
  private volatile int size = 0;

  public void add(byte[] b) {
    chunks.add(b);
    size += b.length;
  }
}
```

</details>

<details><summary>Show answer</summary>

`size` is `volatile`, which makes it look guarded, but two independent unsafe things happen here. First,
`chunks` is a plain `ArrayList` written by multiple threads with no lock — concurrent `add` can corrupt it or
lose elements. Second, `size += b.length` is a read-modify-write, and `volatile` does not make `+=` atomic, so
concurrent adds lose length updates and `size` drifts away from the real byte total.

`volatile` on one field of a two-field update fixes neither problem. Both the list and the counter need real
guarding, and they need it *together* since they must stay consistent.

Fix: one lock covering both fields.

```java
public synchronized void add(byte[] b) {
  chunks.add(b);
  size += b.length;
}
// make size a plain int now; the lock publishes it
```

Handle: `volatile` on one field does not guard a two-field update; a compound change to related fields needs one
lock over both.

</details>

</details>

### Describe a code snippet #34
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
synchronized (lock) {
  if (queue.isEmpty()) {
    lock.wait();
  }
  Object item = queue.remove();
  process(item);
}
```

</details>

<details><summary>Show answer</summary>

`wait()` is guarded by `if`, not `while`. That is the classic bug: after `wait()` returns, the code assumes the
queue is non-empty, but a thread can wake up when the condition is no longer (or was never) true — a spurious
wakeup, or another consumer grabbed the item first. Then `queue.remove()` runs on an empty queue and throws.

Fix: re-check the condition in a loop, so a wakeup that finds nothing simply waits again.

```java
synchronized (lock) {
  while (queue.isEmpty()) {
    lock.wait();
  }
  Object item = queue.remove();
}
process(item); // outside the lock
```

Two more points on the same card: `wait()` must be called on the same object you synchronized on (`lock` here,
which is consistent — good), and `process` should run outside the lock so slow work does not block other
threads. The `if`→`while` change is the headline; the open-call move is the second real fix.

Handle: always wait in a `while` loop that re-checks the condition; an `if` trusts a wakeup that may be spurious
or stolen.

</details>

</details>

### Describe a code snippet #35
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Metrics {
  private long requests;

  public void record() { requests++; }
  public long snapshot() { return requests; }
}
```

No synchronization anywhere.

</details>

<details><summary>Show answer</summary>

Two defects, one per method. `record()` does `requests++`, a read-modify-write, so concurrent calls lose
increments and the count runs low. `snapshot()` reads a `long` with no synchronization: on a JVM that does not
treat `long` reads as atomic, it can read a *torn* value — the high 32 bits from before an update and the low 32
from after — a number that never existed. So this is not only a lost-update bug; it can also return garbage.

Fix: `AtomicLong`, which makes the increment atomic and the read a clean, non-torn read.

```java
private final AtomicLong requests = new AtomicLong();
public void record()   { requests.incrementAndGet(); }
public long snapshot() { return requests.get(); }
```

The torn-read angle is why this card uses a `long`, not an `int`: an `int` read is always atomic, but a `long`
without `volatile` or a lock can split. `AtomicLong` removes both the lost update and the tear.

Handle: `long++` loses updates *and* a plain `long` read can tear; `AtomicLong` fixes both.

</details>

</details>

### Describe a code snippet #36
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<Future<Integer>> futures = new ArrayList<>();
for (Callable<Integer> task : tasks) {
  futures.add(pool.submit(task));
}
int total = 0;
for (Future<Integer> f : futures) {
  total += f.get();
}
```

</details>

<details><summary>Show answer</summary>

This is broadly correct — submit all, then collect — and does not deadlock as long as `pool` is not the same
pool the tasks themselves submit into. The gaps are error handling, not races. `f.get()` throws
`ExecutionException` if a task failed and `InterruptedException` if this thread is interrupted while waiting;
neither is handled, so one failed task aborts the whole loop and leaves the remaining futures uncollected and
their tasks possibly still running.

Fix: handle both checked exceptions, and decide the policy — fail fast, or gather results and failures.

```java
try {
  total += f.get();
} catch (ExecutionException e) {
  // one task failed: log/collect its e.getCause(), keep going or rethrow
} catch (InterruptedException e) {
  Thread.currentThread().interrupt();
  break;
}
```

Second point: there is no timeout on `get()`, so a task that hangs hangs the collector forever. `f.get(timeout,
unit)` bounds the wait. Whether to add it depends on whether tasks are trusted to terminate.

Handle: collecting futures needs to handle `ExecutionException`/`InterruptedException` per task and usually a
timeout; the happy-path loop hides a failed or hung task.

</details>

</details>

### Describe a code snippet #37
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Node {
  int value;
  Node next;
}

// shared, built by one thread, read by others with no synchronization
Node head = buildList();
```

Another thread reads `head.next.next.value`.

</details>

<details><summary>Show answer</summary>

The `Node` objects are mutable and published through a plain field with no synchronization. A reader that sees
`head` can see it before the builder's writes to `value`/`next` are visible, so `head.next` may read as `null`
(NPE) or `value` may read as `0` even though the builder set them. The objects are fine; the hand-off has no
happens-before edge, so the reader sees a half-built structure.

Fix: publish `head` safely so seeing the reference guarantees seeing the writes. Any one of: a `volatile` field,
a `final` field set in a constructor, or handing it off through a concurrent collection.

```java
private volatile Node head;
head = buildList(); // now a reader that sees head sees the whole list built before this write
```

Caveat: `volatile` on `head` publishes everything written *before* the assignment. If another thread keeps
*mutating* nodes after publication, that is a separate ongoing race the `volatile` does not cover — safe
publication covers the build, not later edits.

Handle: mutable objects shared through a plain field can be seen half-built; publish the root safely (volatile/
final/concurrent hand-off).

</details>

</details>

### Describe a code snippet #38
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Gate {
  private boolean open = false;

  public synchronized void openGate() {
    open = true;
    notify();
  }

  public synchronized void await() throws InterruptedException {
    while (!open) {
      wait();
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

The `while` loop around `wait()` is correct. The bug is `notify()` where `notifyAll()` is needed. `notify()`
wakes one waiting thread; if several threads are blocked in `await()`, only one is released and the rest stay
parked forever even though the gate is open. `notify()` is safe only when every waiter is interchangeable *and*
each `notify` corresponds to exactly one thing one waiter can consume — a gate that stays open for everyone does
not fit that.

Fix: wake all waiters.

```java
public synchronized void openGate() {
  open = true;
  notifyAll();
}
```

Rule of thumb: default to `notifyAll()`. Use `notify()` only when you can prove exactly one waiter should wake
and all waiters wait on the same condition — otherwise a woken-but-wrong thread can swallow the signal and leave
the right one asleep.

Handle: `notify()` wakes one waiter and can strand the rest; use `notifyAll()` unless you can prove one wake is
enough.

</details>

</details>

### Describe a code snippet #39
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Downloader {
  private final ExecutorService pool = Executors.newCachedThreadPool();

  public void downloadAll(List<URL> urls) {
    for (URL u : urls) {
      pool.execute(() -> fetch(u));
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

`newCachedThreadPool` has no upper bound on threads — it creates a new thread for every task when none is free.
Hand it a large `urls` list and it spawns a thread per URL, which can exhaust memory or hit the OS thread limit
and crash. It fits short, bursty, low-count work, not a big batch. That is the main defect.

Second: `execute` sends any exception from `fetch` to the thread's uncaught-exception handler, where it is easy
to miss. If you need to know which downloads failed, `submit` and inspect the futures instead.

Fix: bound the pool so the number of threads is capped and extra work queues.

```java
private final ExecutorService pool =
    new ThreadPoolExecutor(0, 32, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
```

Also, the pool is never shut down — same leak as any long-lived pool that outlives its need. Close it on
shutdown.

Handle: a cached pool is unbounded and can spawn a thread per task; bound the pool for any batch you cannot size.

</details>

</details>

### Describe a code snippet #40
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class SharedState {
  private volatile List<String> items = new ArrayList<>();

  public void add(String s) {
    items.add(s);
  }

  public List<String> getItems() {
    return items;
  }
}
```

</details>

<details><summary>Show answer</summary>

`volatile` on the *field* only publishes reassignments of the reference, not changes *inside* the list. The code
never reassigns `items`; it calls `items.add(...)` on a plain `ArrayList` from many threads, which is unguarded
and can corrupt the list or lose elements. `volatile` here protects the one thing that never changes and misses
the thing that does. Second defect: `getItems()` returns the live internal list, so a caller can iterate it
while another thread adds, hitting `ConcurrentModificationException` or a wrong read.

Fix: use a thread-safe list and hand out a copy or an unmodifiable view.

```java
private final List<String> items = new CopyOnWriteArrayList<>();
public List<String> getItems() { return List.copyOf(items); } // snapshot for the caller
```

`CopyOnWriteArrayList` suits read-heavy use; for write-heavy, use `Collections.synchronizedList` and lock around
iteration, or a concurrent structure.

Handle: `volatile` on a collection field guards swapping the reference, not mutating the contents; guard the
contents and don't leak the internal list.

</details>

</details>
