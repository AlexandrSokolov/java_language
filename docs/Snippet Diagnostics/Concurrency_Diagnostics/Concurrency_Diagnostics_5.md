### Describe a code snippet #41
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Settings {
  private Map<String, String> map = new HashMap<>();

  public void reload(Map<String, String> fresh) {
    map.clear();
    map.putAll(fresh);
  }

  public String get(String k) {
    return map.get(k);
  }
}
```

</details>

<details><summary>Show answer</summary>

`reload` clears then refills the same `HashMap` while `get` reads it with no lock. During the gap between
`clear()` and `putAll(...)`, a reader sees an empty or half-filled map and gets `null` for keys that exist. On
top of that, concurrent write and read on a plain `HashMap` can corrupt its structure. So a reader can get wrong
answers *and* the map itself can break.

Fix: build the new map fully, then swap it in with one atomic reference write. Readers see either the whole old
map or the whole new one, never a half-empty one.

```java
private volatile Map<String, String> map = Map.of();

public void reload(Map<String, String> fresh) {
  map = Map.copyOf(fresh); // build complete, then publish in one volatile write
}
public String get(String k) { return map.get(k); }
```

The swap turns a mutate-in-place into a replace, so there is no window where the map is partly built, and
`volatile` publishes the finished immutable map safely.

Handle: mutating shared state in place has a half-updated window; build the new value and swap the reference in
one volatile write.

</details>

</details>

### Describe a code snippet #42
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Counter {
  private int count;

  public void add() {
    synchronized (this) {
      count++;
    }
  }

  public int get() {
    synchronized (Counter.class) {
      return count;
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

`add` locks on `this` (the instance); `get` locks on `Counter.class` (the class). Two different monitors, so
they do not exclude each other at all — `get` can run in the middle of `add` and see a value being updated, and
the lock gives no visibility edge between the writer and the reader either. Locking exists here but protects
nothing, because the two sides hold different locks.

Fix: both methods must take the *same* lock.

```java
public void add() { synchronized (this) { count++; } }
public int get()  { synchronized (this) { return count; } }
```

The instance-vs-class mix is the trap: `synchronized (this)` and `synchronized (Counter.class)` and a
`synchronized static` method are three different locks. State shared by instance methods is guarded by the
instance monitor; only `static` state uses the class monitor. Mixing them silently drops mutual exclusion.

Handle: two methods guard shared state only if they lock the same monitor; `this` and `SomeClass.class` are
different locks.

</details>

</details>

### Describe a code snippet #43
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Pipeline {
  private final BlockingQueue<Item> q = new ArrayBlockingQueue<>(100);

  public void produce(Item i) throws InterruptedException {
    q.put(i);
  }

  public void consume() {
    while (true) {
      Item i = q.take();
      handle(i);
    }
  }
}
```

Called on a pool thread; the app wants a clean shutdown.

</details>

<details><summary>Show answer</summary>

Producer/consumer plumbing is correct — `put`/`take` block properly, capacity gives backpressure, and `take`
blocks instead of spinning. The gap is shutdown. `consume()` loops on `while (true)` and `q.take()` throws
`InterruptedException`, but nothing catches it, so an interrupt sent during shutdown throws out of `handle`'s
frame uncaught and, depending on the pool, is logged and lost while the loop is simply gone — there is no clean
stop path, and no way to drain remaining items.

Fix: make the loop honor interruption and exit cleanly.

```java
public void consume() {
  try {
    while (!Thread.currentThread().isInterrupted()) {
      handle(q.take());
    }
  } catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // restore, then fall out to stop
  }
}
```

To drain on shutdown, switch to `poll(timeout)` after interruption and process what remains, or use a poison-pill
item that tells the consumer to stop. The queue is fine; the missing piece is the cancellation contract.

Handle: a `while(true)` consumer over a blocking queue needs an interruption exit and a drain plan, or shutdown
leaves work stuck and swallows the interrupt.

</details>

</details>

### Describe a code snippet #44
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Lazy {
  private Value value;

  public Value get() {
    if (value == null) {
      synchronized (this) {
        value = new Value();
      }
    }
    return value;
  }
}
```

</details>

<details><summary>Show answer</summary>

This looks like double-checked locking but is missing half of it. The first `value == null` read is outside the
lock, and there is no *second* check inside the lock, so two threads can both pass the outer check, then enter
the `synchronized` block one after the other and each build a new `Value` — the second overwrites the first.
The lock serializes them but does not stop the double creation. And `value` is not `volatile`, so a thread can
see a non-null reference to a half-built `Value`.

Fix: real double-checked locking needs a `volatile` field *and* the inner re-check.

```java
private volatile Value value;
public Value get() {
  Value v = value;
  if (v == null) {
    synchronized (this) {
      v = value;
      if (v == null) value = v = new Value(); // second check, inside the lock
    }
  }
  return v;
}
```

Simpler, if `Value` can be created eagerly or held statically: the holder idiom avoids the whole pattern.

Handle: DCL needs both the inner re-check and a `volatile` field; missing the re-check lets two threads both
build the value.

</details>

</details>

### Describe a code snippet #45
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Aggregator {
  private final List<Integer> results = new ArrayList<>();

  public List<Integer> run(List<Integer> input) {
    input.parallelStream().forEach(n -> results.add(n * 2));
    return results;
  }
}
```

</details>

<details><summary>Show answer</summary>

`forEach` on a parallel stream runs its action on many threads at once, and they all call `results.add(...)` on a
plain `ArrayList`, which is not thread-safe. Concurrent `add` corrupts the list, drops elements, or throws — the
output count comes out wrong or the run blows up. Reaching into a shared mutable collection from a parallel
`forEach` is the textbook misuse of streams.

Fix: do not mutate shared state from the pipeline; let the stream build the result with a collector.

```java
public List<Integer> run(List<Integer> input) {
  return input.parallelStream()
              .map(n -> n * 2)
              .collect(Collectors.toList());
}
```

The collector handles the parallel merge safely, so there is no shared list to guard. Second point: for work this
trivial, `parallelStream` almost certainly costs more than the sequential version — the fix is also faster as a
plain `stream()` unless `input` is huge.

Handle: a parallel `forEach` that writes a shared collection is a race; produce results with a collector instead
of mutating shared state.

</details>

</details>

### Describe a code snippet #46
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Timer {
  private long startNanos;
  private volatile boolean running;

  public void start() {
    startNanos = System.nanoTime();
    running = true;
  }

  public long elapsed() {
    if (!running) return 0;
    return System.nanoTime() - startNanos;
  }
}
```

</details>

<details><summary>Show answer</summary>

`running` is `volatile` but `startNanos` is not. In `start()`, the plain write to `startNanos` happens before the
`volatile` write to `running`, so a reader that sees `running == true` is guaranteed to see the matching
`startNanos` — that ordering is actually safe. The subtle defect is different: a reader can pass the
`if (!running)` check and then, if `start()` runs again concurrently, read a `startNanos` from a *different*
start, giving a wrong or negative elapsed time. The two fields are read at two moments with no snapshot.

This card's teaching point is the safe half: piggybacking a plain field on a later `volatile` write *does*
publish it — a common thing people wrongly call a bug. The real issue is the non-atomic read of two fields that
should move together.

Fix: read both fields as one snapshot under a lock, or hold both in one immutable object published by a single
`volatile` write.

```java
private volatile long[] state; // [running(0/1), startNanos] published together
```

Handle: a plain field written before a `volatile` write *is* published by it; the real risk is reading two
related fields at two moments instead of one snapshot.

</details>

</details>

### Describe a code snippet #47
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Job {
  public void run() {
    for (int i = 0; i < items.size(); i++) {
      if (Thread.interrupted()) {
        cleanup();
      }
      process(items.get(i));
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

`Thread.interrupted()` *clears* the interrupt flag as it reads it, and after calling `cleanup()` the loop falls
straight through to `process(...)` and keeps going. So on interrupt the job cleans up but then resumes work with
the flag already wiped — the cancel is detected, acted on halfway, and then thrown away. A cancelled job that
keeps processing is the bug.

Note the difference between the two check methods: `Thread.interrupted()` (static) reads *and clears*;
`isInterrupted()` (instance) reads *without* clearing. Using the clearing one and then not stopping loses the
signal.

Fix: on interrupt, clean up and actually stop — and preserve the flag if callers upstream need it.

```java
if (Thread.currentThread().isInterrupted()) {
  cleanup();
  Thread.currentThread().interrupt(); // keep the flag for callers
  return;                             // stop, do not process more
}
```

Handle: `Thread.interrupted()` clears the flag; detect, then actually stop and restore the flag — don't fall
through to more work.

</details>

</details>

### Describe a code snippet #48
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

public Session getOrCreate(long id) {
  Session s = sessions.get(id);
  if (s == null) {
    s = new Session(id);
    sessions.put(id, s);
  }
  return s;
}
```

</details>

<details><summary>Show answer</summary>

`ConcurrentHashMap` makes each call atomic, but `get` then `put` is a check-then-act with a gap. Two threads
asking for the same new `id` can both get `null`, both build a `Session`, and both `put` — two `Session` objects
for one id, and whichever `put` lands second wins while the other caller walks away holding an orphan. For
sessions that usually means duplicate state, lost data, or two logins for one user.

Fix: `computeIfAbsent`, which builds and inserts atomically, once per absent id.

```java
public Session getOrCreate(long id) {
  return sessions.computeIfAbsent(id, Session::new);
}
```

If `Session` construction is expensive or has side effects you do not want to run under the bin lock, build a
cheap placeholder with `computeIfAbsent` and finish initialization outside, or use `putIfAbsent` and discard the
loser — but never leave the `get`/`put` gap.

This is the check-then-act race one more time, framed on session creation so the cost of a duplicate is a real
user-facing bug, not just a wrong count.

Handle: `get`-then-`put` on a concurrent map can create two objects for one key; `computeIfAbsent` makes it one.

</details>

</details>

### Describe a code snippet #49
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Latch {
  private int remaining;

  public Latch(int n) { remaining = n; }

  public synchronized void countDown() {
    remaining--;
    if (remaining == 0) notifyAll();
  }

  public void await() throws InterruptedException {
    while (remaining > 0) {
      wait();
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

`countDown` is synchronized but `await` is not, yet `await` calls `wait()`. `wait()` may only be called by a
thread that holds the object's monitor; here `await` holds nothing, so it throws
`IllegalMonitorStateException` immediately. On top of that, `await` reads `remaining` with no lock, so even
without the `wait()` crash it could loop on a stale value.

Fix: `await` must be synchronized on the same monitor, so it holds the lock required by `wait()` and reads
`remaining` safely.

```java
public synchronized void await() throws InterruptedException {
  while (remaining > 0) {
    wait();
  }
}
```

The `while` loop is already correct. The single missing `synchronized` on `await` is what breaks both the
monitor rule and the read. In real code, prefer the built-in `CountDownLatch` over hand-rolling this.

Handle: `wait()` requires holding the monitor; a `wait()` in an unsynchronized method throws
`IllegalMonitorStateException`.

</details>

</details>

### Describe a code snippet #50
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class OrderService {
  private int nextId = 1;
  private final Map<Integer, Order> orders = new HashMap<>();

  public synchronized int create(Order o) {
    int id = nextId++;
    orders.put(id, o);
    return id;
  }

  public Order find(int id) {
    return orders.get(id);
  }
}
```

</details>

<details><summary>Show answer</summary>

`create` is synchronized, so id generation and the `put` are safe against each other. `find` is the hole: it
reads the same `HashMap` with no lock while `create` writes it. Concurrent read and write on a plain `HashMap`
can corrupt buckets or spin a reader forever during a resize — and even short of that, `find` can miss an entry
`create` just added because the write is not published to an unsynchronized reader. Guarding writes but not reads
leaves the map unsafe.

Fix: guard `find` with the same lock, or switch to a concurrent map so reads need no lock.

```java
// option A: same lock
public synchronized Order find(int id) { return orders.get(id); }

// option B: concurrent map, no lock on the read path
private final Map<Integer, Order> orders = new ConcurrentHashMap<>();
```

With `ConcurrentHashMap`, keep `create` synchronized only if `nextId++` needs it (it does) — or make `nextId` an
`AtomicInteger` and drop the method lock entirely.

Handle: writes under a lock and reads without one still race on a `HashMap`; guard both sides or use a concurrent
map.

</details>

</details>
