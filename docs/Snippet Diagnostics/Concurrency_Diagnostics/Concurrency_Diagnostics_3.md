### Describe a code snippet #21
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Stats {
  private long total;
  private int count;

  public synchronized void record(long v) {
    total += v;
    count++;
  }

  public double average() {
    return (double) total / count;
  }
}
```

</details>

<details><summary>Show answer</summary>

`record` updates two fields under the lock, which is right — they must move together. But `average` reads both
fields with no lock, so it can read `total` after an update and `count` before the matching update (or the
reverse), and compute an average from two fields that never existed together. It can also read a stale `long`
that is not even torn but simply old. Reading a set of related fields without the lock that keeps them in step
breaks the invariant the lock exists to protect.

Fix: read both fields under the same lock, as one snapshot.

```java
public synchronized double average() {
  return count == 0 ? 0.0 : (double) total / count;
}
```

The `count == 0` guard is a second, separate defect: the original divides by zero before any record, returning
`NaN`. Fixing the locking does not fix the empty case; both belong on this card.

Handle: fields that update together must be read together under the same lock, or the reader sees a mix that
never held at once.

</details>

</details>

### Describe a code snippet #22
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
class A {
  synchronized void m(B b) { b.n(); }
}
class B {
  synchronized void n() { }
  synchronized void call(A a) { a.m(this); }
}
```

Thread 1 calls `a.m(b)`; thread 2 calls `b.call(a)`.

</details>

<details><summary>Show answer</summary>

`synchronized` on an instance method locks `this`. Thread 1 in `a.m` holds `a`'s lock and then calls `b.n()`,
needing `b`'s lock. Thread 2 in `b.call` holds `b`'s lock and then calls `a.m`, needing `a`'s lock. Each holds
what the other wants — deadlock. The locks are hidden inside the `synchronized` keyword, so the cycle is harder
to see than two explicit `synchronized (x)` blocks, but it is the same lock-ordering deadlock.

Fix: do not call another synchronized object's method while holding your own lock. Shrink the locked region so
the outward call happens after releasing the lock (an "open call").

```java
void m(B b) {
  // do the guarded work under the lock, then call out unlocked
  synchronized (this) { /* local state only */ }
  b.n();
}
```

Handle: calling a foreign locked method while holding your own lock builds a lock cycle; release before calling
out (open call).

</details>

</details>

### Describe a code snippet #23
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Event {
  private String message;

  public void publish(String m) {
    this.message = m;
  }

  public String read() {
    return message;
  }
}
```

Threads call `publish` and `read` with no other synchronization.

</details>

<details><summary>Show answer</summary>

A reference write and read of `message` will not tear (references are always read and written as one unit), so
`read()` never sees a garbage half-pointer. But with no `volatile` and no lock, a reader can keep seeing an old
value long after `publish` ran on another thread — there is no promise the new reference is ever made visible.
The bug is staleness, not tearing.

Fix: make the field `volatile`, which is exactly enough for a single reference that one thread writes and others
read.

```java
private volatile String message;
```

Note the contrast with a `long`/`double` field: those can also *tear* without `volatile` on a 32-bit view (two
halves written separately), so `volatile` there fixes tearing *and* staleness. For a reference it fixes only
staleness, because references never tear. Same keyword, two different guarantees depending on the type.

Handle: a reference never tears but can go stale; `volatile` fixes the staleness. A `long`/`double` can also
tear, and `volatile` fixes both.

</details>

</details>

### Describe a code snippet #24
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
BlockingQueue<Task> queue = new LinkedBlockingQueue<>();

// producer
queue.offer(task);

// consumer
Task t = queue.poll();
if (t != null) {
  t.run();
}
```

</details>

<details><summary>Show answer</summary>

The queue is thread-safe, so there is no data race here. The bug is the choice of methods. `offer` on an
unbounded `LinkedBlockingQueue` never rejects, so it is fine, but `poll()` returns `null` immediately when the
queue is empty — so the consumer, if it loops, spins hot on `null` doing nothing, burning a core. If it does not
loop, it processes at most one task and misses the rest. Either way the blocking queue's main feature, waiting
for work, is unused.

Fix: use the blocking `take()`, which parks the consumer until an item arrives.

```java
Task t = queue.take(); // blocks until an item is available
t.run();
```

If the producer side needs backpressure, give the queue a capacity and have the producer use `put` (blocks when
full) instead of `offer` (drops silently on a bounded queue). Picking `offer`/`poll` vs `put`/`take` is picking
whether the code drops work or waits for it.

Handle: `poll`/`offer` never block, `take`/`put` do; using the non-blocking pair on a blocking queue spins or
drops work.

</details>

</details>

### Describe a code snippet #25
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private final Map<String, Integer> counts = new ConcurrentHashMap<>();

public void increment(String key) {
  Integer current = counts.get(key);
  counts.put(key, current == null ? 1 : current + 1);
}
```

</details>

<details><summary>Show answer</summary>

The map is concurrent, but `get` then `put` is a check-then-act with a gap. Two threads can read the same
`current`, both compute `current + 1`, and both write it back — one increment lost, exactly like `count++` but
spread across two map calls. The counts come out too low under load.

Fix: fold the read-modify-write into one atomic map operation.

```java
counts.merge(key, 1, Integer::sum);
```

`merge` applies the combine function atomically per key. `compute` works too. `getAndIncrement` on an
`AtomicInteger` value would also work but changes the value type.

This is the lost-update bug again — first seen on a single `int`, here on a per-key counter in a map — to drill
that "concurrent collection" plus "read then write" is still a race.

Handle: per-key read-modify-write on a concurrent map is not atomic; `merge`/`compute` make it one step.

</details>

</details>

### Describe a code snippet #26
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Task implements Runnable {
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      Object item = fetchBlocking(); // may throw InterruptedException, caught inside
      handle(item);
    }
  }

  private Object fetchBlocking() {
    try {
      return queue.take();
    } catch (InterruptedException e) {
      return null;
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

The loop checks `isInterrupted()`, which looks correct, but `queue.take()` clears the interrupt flag when it
throws `InterruptedException`, and `fetchBlocking` swallows the exception and returns `null`. So after an
interrupt the flag is gone, the loop condition sees "not interrupted," and it keeps running — the cancel is
lost. The `handle(null)` that follows is a second bug: it passes `null` downstream on every interrupt.

Fix: restore the flag on catch so the loop condition sees it, and break out rather than returning `null`.

```java
private Object fetchBlocking() throws InterruptedException {
  return queue.take(); // let it propagate
}
// in run(): catch InterruptedException around the loop and exit
```

Or, keeping the method shape, restore and signal:

```java
} catch (InterruptedException e) {
  Thread.currentThread().interrupt(); // put the flag back so the loop stops
  return null;
}
```

Handle: a blocking call that throws `InterruptedException` clears the flag; swallowing it makes the loop's own
interrupt check useless.

</details>

</details>

### Describe a code snippet #27
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Toggle {
  private boolean on = false;

  public synchronized void flip() { on = !on; }

  public boolean isOn() { return on; }
}
```

</details>

<details><summary>Show answer</summary>

`flip` is guarded, `isOn` is not. Because `on` is a `boolean`, the read cannot tear, but it can be stale — a
reader can keep seeing `false` after another thread flipped it to `true`, since nothing publishes the change to
an unsynchronized reader. A status flag that lags reality is still a bug.

Two valid fixes, and the choice is the teaching point:

```java
// option A: synchronize the reader on the same lock
public synchronized boolean isOn() { return on; }

// option B: make the field volatile and drop synchronized on flip? -- NO
```

Option B is a trap: `flip` does `on = !on`, a read-modify-write, so `volatile` alone would let two flips race and
lose one. `volatile` is enough only when the writer just *assigns*; here the writer *transforms*, so the lock has
to stay and the reader should share it. Same-looking flag, but the writer's operation decides whether `volatile`
is sufficient.

Handle: `volatile` is enough for a plain assignment, not for a flip (`x = !x`); a transforming write needs the
lock on both sides.

</details>

</details>

### Describe a code snippet #28
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
ExecutorService pool = Executors.newSingleThreadExecutor();
pool.submit(() -> {
  process();
});
pool.shutdownNow();
```

</details>

<details><summary>Show answer</summary>

`shutdownNow` is called right after `submit`, so the task may be interrupted before or during `process()` and
never finish. `shutdownNow` tries to stop running tasks by interrupting them and returns the tasks that never
started — it is not "finish what is queued, then stop." If the intent was to let the submitted work complete,
this throws it away.

The two shutdown methods differ, and picking the wrong one is the bug:

```java
pool.shutdown(); // stop accepting new tasks, let submitted ones finish
pool.awaitTermination(30, TimeUnit.SECONDS);
```

Use `shutdown` + `awaitTermination` to drain gracefully; use `shutdownNow` only when you want to abandon
in-flight work and cancel now. Also, `shutdownNow` only *requests* interruption — a task that ignores its
interrupt flag keeps running, so "now" is best-effort, not guaranteed.

Handle: `shutdown` drains, `shutdownNow` interrupts and abandons; calling `shutdownNow` right after submit can
kill the work before it runs.

</details>

</details>

### Describe a code snippet #29
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Range {
  private int low = 0;
  private int high = 10;

  public synchronized void setLow(int n) {
    if (n > high) throw new IllegalArgumentException();
    low = n;
  }

  public synchronized void setHigh(int n) {
    if (n < low) throw new IllegalArgumentException();
    high = n;
  }
}
```

</details>

<details><summary>Show answer</summary>

Each setter is synchronized and checks the invariant `low <= high`, which looks safe. It is not, because the two
setters use the *same* lock but the check and the set in a single setter are fine — the real hole is that this
pattern is a classic even with locking: it holds. Trace it carefully. `setLow` reads `high` and writes `low`
under the lock; `setHigh` reads `low` and writes `high` under the same lock. Because both take the same monitor,
no interleaving is possible — this code is actually correct.

The trap this card drills: the *same* structure with **two different locks** (or one setter unsynchronized) is
broken. If `setLow` and `setHigh` locked on different objects, thread 1 in `setLow` could pass its `n <= high`
check just as thread 2 in `setHigh` lowers `high` below it, leaving `low > high`. The single shared lock is
exactly what saves it.

```java
// broken variant: different locks let the checked invariant slip
public void setLow(int n)  { synchronized (lockA) { if (n > high) throw ...; low = n; } }
public void setHigh(int n) { synchronized (lockB) { if (n < low)  throw ...; high = n; } }
```

Handle: a two-field invariant is safe only when every method touching either field takes the *same* lock; split
the lock and the check no longer holds.

</details>

</details>

### Describe a code snippet #30
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Resource {
  private volatile boolean initialized = false;
  private Connection conn;

  public Connection get() {
    if (!initialized) {
      conn = openConnection();
      initialized = true;
    }
    return conn;
  }
}
```

</details>

<details><summary>Show answer</summary>

`initialized` is `volatile`, which looks like a fix, but the sequence is still a check-then-act. Two threads can
both see `initialized == false`, both call `openConnection()`, and the second overwrite leaks the first
connection. `volatile` gives visibility of the flag, not one-time execution of the block it guards.

There is also an ordering point: even for a single thread it works, but under two threads a reader can see
`initialized == true` set by one thread while `conn` from that thread is what it reads — here `conn` is written
before `initialized` under the same thread, so a `volatile` write of `initialized` does publish `conn`. So the
visibility is fine; the defect is the double-open, not a torn read.

Fix: guard the whole check-and-init so it runs once. Double-checked locking with a `volatile` field, or simpler,
the holder idiom if the value can be static.

```java
private volatile Connection conn;
public Connection get() {
  Connection c = conn;
  if (c == null) {
    synchronized (this) {
      c = conn;
      if (c == null) conn = c = openConnection();
    }
  }
  return c;
}
```

Handle: a `volatile` flag makes the check visible but not atomic; one-time init needs a lock around check-and-set,
not just a visible flag.

</details>

</details>
