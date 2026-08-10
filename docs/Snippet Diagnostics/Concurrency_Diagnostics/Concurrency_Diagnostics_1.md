### Describe a code snippet #1
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Worker {
  private boolean stopped = false;

  public void run() {
    while (!stopped) {
      doWork();
    }
  }

  public void stop() {
    stopped = true;
  }
}
```

</details>

<details><summary>Show answer</summary>

The thread inside `run()` may never see `stopped` turn true, so the loop can run forever after `stop()` is
called from another thread. Nothing forces the writing thread's change to become visible to the reading
thread — the reader is allowed to keep using a cached copy of `stopped`. The JIT can also hoist the read out
of the loop, turning it into `if (!stopped) while (true) doWork();`.

Fix: make the field `volatile`. That gives the write a happens-before edge to every later read, so the change
is seen and the read is not hoisted.

```java
private volatile boolean stopped = false;
```

Not a fix: `synchronized` on both methods would also work, but it is heavier than needed here — a single flag
with one writer and one reader is exactly what `volatile` is for. Reach for a lock only when more than one
field must change together.

Handle: a plain field carries no visibility promise; `volatile` is the cheapest tool that adds one.

</details>

</details>

### Describe a code snippet #2
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Counter {
  private int count = 0;

  public void increment() {
    count++;
  }

  public int get() {
    return count;
  }
}
```

</details>

<details><summary>Show answer</summary>

`count++` is three steps — read, add one, write back — not one. Two threads can both read the same value, both
add one, both write it back, and one increment is lost. The final count comes out lower than the number of
calls. `get()` has a second, separate problem: with no synchronization it may read a stale value even after
`increment()` returns on another thread.

`volatile` does **not** fix this. `volatile` gives visibility, not atomicity — the read-add-write is still
three steps and can still interleave.

Fix (single field): use `AtomicInteger`, whose `incrementAndGet()` is one atomic step.

```java
private final AtomicInteger count = new AtomicInteger();
public void increment() { count.incrementAndGet(); }
public int get()        { return count.get(); }
```

Fix (if more fields ever join the update): a lock, so the whole compound action is one unit.

Handle: read-modify-write on a shared value is not atomic; making it visible does not make it atomic.

</details>

</details>

### Describe a code snippet #3
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public void transfer(Account from, Account to, int amount) {
  synchronized (from) {
    synchronized (to) {
      from.debit(amount);
      to.credit(amount);
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

Two threads calling `transfer(a, b, ...)` and `transfer(b, a, ...)` at the same time can deadlock. The first
grabs `a` and waits for `b`; the second grabs `b` and waits for `a`; neither lets go. The bug is that the two
locks are taken in an order that depends on the arguments, so different calls take them in different orders.

Fix: always take the two locks in the same global order, no matter the argument order. Order by a stable key
such as an account id.

```java
Account first  = from.id() < to.id() ? from : to;
Account second = from.id() < to.id() ? to   : from;
synchronized (first) {
  synchronized (second) {
    from.debit(amount);
    to.credit(amount);
  }
}
```

Edge case: if `from.id() == to.id()` (same account, or a duplicate id), add a tie-break or reject the call, or
two threads can still line up badly. Real money systems usually route this through one ordered lock manager
rather than locking the objects directly.

Handle: nested locks deadlock when their order is not fixed; pick one global order and never break it.

</details>

</details>

### Describe a code snippet #4
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<String> list = Collections.synchronizedList(new ArrayList<>());
// ... filled from several threads ...

for (String s : list) {
  process(s);
}
```

</details>

<details><summary>Show answer</summary>

Each single call on a `synchronizedList` is safe, but iteration is a series of calls, and the lock is dropped
between them. If another thread adds or removes during the loop, the iterator sees the list change under it and
throws `ConcurrentModificationException`.

That exception is best-effort, not guaranteed — it is raised on a modification count check that may or may not
notice the change. So the loop can also just return wrong results without throwing. Do not treat "it didn't
throw" as "it was safe."

Fix: hold the list's own lock for the whole loop.

```java
synchronized (list) {
  for (String s : list) {
    process(s);
  }
}
```

Better rewrite: use `CopyOnWriteArrayList` if reads far outnumber writes — its iterator walks a fixed snapshot
and never throws, and you drop the manual lock. It costs a full array copy on every write, so it is wrong for
write-heavy lists.

Handle: a thread-safe collection makes each call safe, not a loop of calls; iteration needs its own lock or a
snapshot collection.

</details>

</details>

### Describe a code snippet #5
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public void handle() {
  try {
    Thread.sleep(1000);
  } catch (InterruptedException e) {
    // ignore
  }
  doWork();
}
```

</details>

<details><summary>Show answer</summary>

`Thread.sleep` clears the interrupt flag when it throws `InterruptedException`. Swallowing the exception throws
away the one signal that someone asked this thread to stop, so the code sleeps, wakes, and keeps working as if
nothing happened. A task like this can no longer be cancelled cleanly — a pool trying to shut down will hang on
it.

Fix (if this method cannot itself throw): restore the flag so code higher up can see it.

```java
} catch (InterruptedException e) {
  Thread.currentThread().interrupt();
  return; // stop the work, do not fall through to doWork()
}
```

Better fix: let `InterruptedException` propagate by declaring it on the method, so the caller decides. Swallow
it only at the top level that owns the thread, and even there restore the flag.

Also wrong here: even after restoring the flag, the original code falls through to `doWork()` — a cancelled task
should not do more work. The `return` above matters as much as the `interrupt()`.

Handle: interruption is a request to stop; catching it and doing nothing deletes the request.

</details>

</details>

### Describe a code snippet #6
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
ExecutorService pool = Executors.newFixedThreadPool(4);

Future<Integer> f = pool.submit(() -> {
  Future<Integer> inner = pool.submit(() -> compute());
  return inner.get() + 1;
});
```

</details>

<details><summary>Show answer</summary>

The outer task runs on a pool thread and then blocks on `inner.get()`, waiting for a second task that also
needs a pool thread. With only four threads, four outer tasks can each hold a thread while all four wait for
inner tasks that can never start — every thread is parked waiting for work that has nowhere to run. The pool is
stuck. This is thread-starvation deadlock, and it does not need two locks — one pool waiting on itself is enough.

Fix: do not submit a dependent task into the same bounded pool and then block on it. Either compute inline:

```java
Future<Integer> f = pool.submit(() -> compute() + 1);
```

or, if the two stages genuinely need separate pools, give the inner work its own pool so the outer thread waits
on a different resource.

Also worth noting: `newFixedThreadPool` uses an unbounded queue, so unrelated flooding of tasks grows memory
without bound — a separate risk in the same line, not the cause of this deadlock.

Handle: a task that blocks on another task in the same bounded pool can starve the pool; keep dependent stages
off the same fixed pool.

</details>

</details>

### Describe a code snippet #7
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Cache {
  private Map<String, Data> map = new HashMap<>();

  public Data get(String key) {
    Data d = map.get(key);
    if (d == null) {
      d = load(key);
      map.put(key, d);
    }
    return d;
  }
}
```

</details>

<details><summary>Show answer</summary>

A plain `HashMap` written by several threads with no lock can corrupt its own internal structure — a resize
during a concurrent `put` can link buckets into a loop and hang a reader in an endless walk, or silently drop
entries. That is the worst defect and it is not best-effort: the map's internal state is simply broken.

Second defect, present even if the map were thread-safe: `get`-then-`put` is check-then-act. Two threads can
both miss, both `load`, and both `put` — duplicate loads, and the second overwrites the first.

Fix: `ConcurrentHashMap` with an atomic compute, which both makes the structure safe and collapses the
check-then-act into one step that loads a key at most once.

```java
private final Map<String, Data> map = new ConcurrentHashMap<>();
public Data get(String key) {
  return map.computeIfAbsent(key, this::load);
}
```

Note: `computeIfAbsent` holds a bin lock while the mapping function runs, so `load` must not try to update the
same map, or it can deadlock or throw.

Handle: a `HashMap` under concurrent writes is unsafe at the structure level; `computeIfAbsent` on a concurrent
map fixes both the safety and the double-load.

</details>

</details>

### Describe a code snippet #8
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Config {
  private static Config instance;

  public static Config getInstance() {
    if (instance == null) {
      instance = new Config();
    }
    return instance;
  }
}
```

</details>

<details><summary>Show answer</summary>

Two threads can both see `instance == null`, both build a `Config`, and hand back two different objects that
were meant to be one. There is also a visibility hole: a thread can see a non-null `instance` whose fields were
not yet fully written by the constructor, so it reads a half-built object.

Fix (simplest and correct): the holder idiom. The class loader guarantees the inner class loads once, on first
use, with no locking in the hot path.

```java
public class Config {
  private Config() {}
  private static class Holder {
    static final Config INSTANCE = new Config();
  }
  public static Config getInstance() {
    return Holder.INSTANCE;
  }
}
```

Fix if the field must stay a field (double-checked locking): the field **must** be `volatile`, or the
half-built-object read above stays possible. Without `volatile` the pattern is broken, not merely slow.

```java
private static volatile Config instance;
```

Handle: lazy singletons need both one-time creation and safe publication; the holder idiom gives both for free,
DCL only works with `volatile`.

</details>

</details>

### Describe a code snippet #9
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Latch {
  private final CountDownLatch latch = new CountDownLatch(1);

  void waitForReady() throws InterruptedException {
    latch.await();
  }

  void reset() {
    // reopen the gate for the next round
    latch.countDown();
  }
}
```

</details>

<details><summary>Show answer</summary>

A `CountDownLatch` is one-shot. Once its count reaches zero the gate stays open forever — `countDown()` does not
reset it, and there is no method that does. So `reset()` cannot start a new round; after the first release the
latch is spent and every later `await()` returns at once. Naming the method `reset` hides a design bug: the type
chosen cannot do what the code needs.

Fix: pick a resettable tool. For a gate that opens and closes over many rounds, `CyclicBarrier` (resets itself
after each trip) or a `Phaser` fits. If the need is really "wait until a condition holds again," a lock plus a
`Condition` is the direct tool.

```java
// rounds-based rendezvous: reusable, unlike a latch
private final CyclicBarrier barrier = new CyclicBarrier(parties);
```

Handle: a latch counts down once and never comes back; anything that must repeat needs a cyclic tool.

</details>

</details>

### Describe a code snippet #10
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Sequence {
  private volatile long value = 0;

  public long next() {
    return value++;
  }
}
```

</details>

<details><summary>Show answer</summary>

`volatile` makes each read and each write visible, but `value++` is still read-add-write — three steps. Two
threads can read the same value and both return it, so `next()` hands out duplicates and skips numbers. A
sequence generator that repeats values is broken. `volatile` here buys nothing over a plain field for this use,
because the problem is atomicity, not visibility.

Fix: `AtomicLong.getAndIncrement()`, one atomic step, and drop the `volatile` field entirely.

```java
private final AtomicLong value = new AtomicLong();
public long next() { return value.getAndIncrement(); }
```

This card is the same root as the counter case but framed as ID generation, where a duplicate is not just a
wrong total but a broken unique key — the failure is louder and the fix is identical.

Handle: `volatile` + `++` still hands out duplicates; a unique sequence needs an atomic increment.

</details>

</details>
