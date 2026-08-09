
### Describe a code snippet — stop flag
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class StopThread {
  private static boolean stopRequested;

  public static void main(String[] args) throws InterruptedException {
    Thread backgroundThread = new Thread(() -> {
      int i = 0;
      while (!stopRequested)
        i++;
    });
    backgroundThread.start();
    TimeUnit.SECONDS.sleep(1);
    stopRequested = true;
  }
}
```

</details>

<details><summary>Show answer</summary>

**Defect — visibility.** `stopRequested` is shared but not synchronized. The write in `main` is not
guaranteed to ever reach the background thread. The JIT is allowed to hoist the check out of the loop,
turning it into `if (!stopRequested) while (true) i++;` — so the loop **may run forever**. Not certain:
it is allowed to hang, not guaranteed to. Atomicity is fine here — a `boolean` read/write is atomic —
so this is purely a visibility failure.

**Not a defect — `i++`.** Looks like an unsynchronized read-modify-write, but `i` is local to the
lambda: one thread only, no sharing. It is fine.

**Fix (keep this one) — `volatile`.** Makes every read of the flag see the latest write:

```java
private static volatile boolean stopRequested;
```

Visibility is the only thing missing, and `volatile` gives exactly that.

**Also works, but heavier — `synchronized`.** Guarding both read and write with a lock fixes visibility
too, but adds mutual exclusion you do not need for a single flag. Correct, not the one to keep.

</details>

</details>

### Describe a code snippet — synced stop flag
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class StopThread {
  private static boolean stopRequested;

  private static synchronized void requestStop() {
    stopRequested = true;
  }
  private static synchronized boolean stopRequested() {
    return stopRequested;
  }

  public static void main(String[] args) throws InterruptedException {
    Thread backgroundThread = new Thread(() -> {
      int i = 0;
      while (!stopRequested())
        i++;
    });
    backgroundThread.start();
    TimeUnit.SECONDS.sleep(1);
    requestStop();
  }
}
```

</details>

<details><summary>Show answer</summary>

This version is correct. Three points:

**The trap — both sides must be synchronized.** The write (`requestStop`) and the read (`stopRequested`)
are both synchronized, and both must be. Synchronizing only the write is the common mistake: visibility
holds only when read and write take the same lock. Sync one side and it may appear to work on some
machines — that is luck, not correctness.

**The sync here is not for mutual exclusion.** A `boolean` read/write is already atomic, so nothing needs
protecting from half-done state. The lock is used only to make the write visible to the reader — the
communication effect, not exclusion.

**Not a defect — `i++`.** This is a read-modify-write, which is not atomic — but `i` is a local variable,
touched by one thread only. Nothing is shared, so there is no race and no need for an atomic type like
`AtomicInteger`. A read-modify-write is only a problem when the value is shared across threads.

**Better fix — `volatile`.** Since exclusion is not needed, declare the flag `volatile` instead:

```java
private static volatile boolean stopRequested;
```

`volatile` does no locking, but guarantees every read sees the most recent write. Same visibility, less
code, likely faster — no lock taken each loop pass.

</details>

</details>

### Describe a code snippet — serial number
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class SerialNumber {
  private static volatile int nextSerialNumber = 0;

  public static int generateSerialNumber() {
    return nextSerialNumber++;
  }
}
```

</details>

<details><summary>Show answer</summary>

**Defect — read-modify-write, not atomic.** `nextSerialNumber++` is two steps: read the field, then write
back old + 1. A second thread reading between those steps sees the same old value and returns the same
serial number. `volatile` does not save it — `volatile` gives visibility, not atomicity, and the two steps
can still interleave. This is a safety failure: wrong results, not just a stale view.

**Fix — `synchronized`.** Guard the method so calls cannot interleave; then remove `volatile` (the lock
already gives visibility):

```java
private static int nextSerialNumber = 0;
public static synchronized int generateSerialNumber() {
  return nextSerialNumber++;
}
```

**Better fix — `AtomicLong`.** Does the read-modify-write in one atomic step, no lock, and gives both
visibility and atomicity. Use `long` (or this) so the counter does not silently wrap at `int` max:

```java
private static final AtomicLong nextSerialNumber = new AtomicLong();
public static long generateSerialNumber() {
  return nextSerialNumber.getAndIncrement();
}
```

</details>

</details>

### Describe a code snippet #1
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private final List<SetObserver<E>> observers = new ArrayList<>();

private void notifyElementAdded(E element) {
  synchronized (observers) {
    for (SetObserver<E> observer : observers)
      observer.added(this, element);        // alien call, inside the lock
  }
}

// usage: an observer that removes itself
set.addObserver(new SetObserver<>() {
  public void added(ObservableSet<Integer> s, Integer e) {
    System.out.println(e);
    if (e == 23)
      s.removeObserver(this);               // calls observers.remove(...)
  }
});
```

</details>

<details><summary>Show answer</summary>

Throws `ConcurrentModificationException` after printing 0–23.

`notifyElementAdded` iterates `observers` under the lock. The alien `added` call runs *during* that iteration and
calls back into `removeObserver`, which does `observers.remove(...)` — modifying the list while the loop is still
iterating it. That is the illegal modify-during-iteration, caught by the iterator.

- **The `synchronized` block does not help.** It stops *another thread* from modifying the list, but the offending
  modification comes from the *same thread*, calling back in through the alien method. The lock never guards against
  self-reentry.
- **Not guaranteed, but typical.** `ConcurrentModificationException` is best-effort — the iterator raises it when it
  notices the change, not always.

Fix: make the alien call an [open call](#how-do-you-call-an-alien-method-safely-when-a-lock-is-needed) — snapshot
the list under the lock, then iterate the copy outside the lock.

</details>

</details>

### Describe a code snippet #2
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private void notifyElementAdded(E element) {
  synchronized (observers) {
    for (SetObserver<E> observer : observers)
      observer.added(this, element);        // alien call, inside the lock
  }
}

// usage: an observer that unsubscribes via a background thread
set.addObserver(new SetObserver<>() {
  public void added(ObservableSet<Integer> s, Integer e) {
    System.out.println(e);
    if (e == 23) {
      ExecutorService exec = Executors.newSingleThreadExecutor();
      try {
        exec.submit(() -> s.removeObserver(this)).get();   // waits for the removal
      } catch (ExecutionException | InterruptedException ex) {
        throw new AssertionError(ex);
      } finally {
        exec.shutdown();
      }
    }
  }
});
```

</details>

<details><summary>Show answer</summary>

Deadlocks after printing 0–23.

The main thread holds the lock on `observers` (inside `notifyElementAdded`) and calls the alien `added`. That
method hands `removeObserver` to a background thread and blocks on `.get()` waiting for it. The background thread
tries to take the lock on `observers` to do the removal — but the main thread still holds it and is blocked waiting
on the background thread. Each waits for the other, forever.

- **Reentrancy can't save this one.** In snippet #1 the same thread re-entered and succeeded (reentrant lock). Here
  the removal runs on a *different* thread, so reentrancy doesn't apply — the second thread genuinely can't get the
  lock, and it's a true deadlock, not an exception.
- **The background thread is pointless.** There's no reason to offload the unsubscribe; it's contrived. But the
  deadlock it exposes is real — alien calls under a lock cause exactly this in real systems.

Fix: [open call](#how-do-you-call-an-alien-method-safely-when-a-lock-is-needed) — don't hold the lock across the
alien call, so no other thread is ever blocked waiting on a lock you hold while you wait on it.

</details>

</details>

### Describe a code snippet #3
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private void notifyElementAdded(E element) {
  List<SetObserver<E>> snapshot;
  synchronized (observers) {
    snapshot = new ArrayList<>(observers);    // copy under the lock
  }
  for (SetObserver<E> observer : snapshot)    // iterate outside the lock
    observer.added(this, element);            // alien call, now lock-free
}
```

</details>

<details><summary>Show answer</summary>

Correct — no exception, no deadlock. This is the fix for snippets #1 and #2.

The lock is held only long enough to copy the list. The alien `added` call then runs on the copy with no lock held
— an [open call](#how-do-you-call-an-alien-method-safely-when-a-lock-is-needed).

- **Fixes #1.** The loop now iterates `snapshot`, a private copy. If the observer calls `removeObserver` and changes
  the real `observers` list, the copy being iterated is untouched — no modify-during-iteration.
- **Fixes #2.** No lock is held during the alien call, so a background thread taking the lock to remove an observer
  gets it immediately — nothing to deadlock against.
- **Extra win: concurrency.** The lock is held only for the fast copy, not for the whole notify loop. Other threads
  wait far less, even when an observer's `added` runs long.

An alternative to the snapshot is a `CopyOnWriteArrayList`, which makes iteration lock-free without copying by hand.

</details>

</details>

### Describe a code snippet #12
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private static final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

public static String intern(String s) {
  String previousValue = map.putIfAbsent(s, s);
  return previousValue == null ? s : previousValue;
}
```

</details>

<details><summary>Show answer</summary>

Correct and thread-safe — but not the fastest form on `ConcurrentHashMap`.

`putIfAbsent` is called on every invocation, including when the key is already present. `ConcurrentHashMap` is
optimized for `get`, so the hot path (value already interned) pays for the heavier operation each time.

Faster: try `get` first, call `putIfAbsent` only on a miss.

```java
public static String intern(String s) {
  String result = map.get(s);
  if (result == null) {
    result = map.putIfAbsent(s, s);
    if (result == null)
      result = s;
  }
  return result;
}
```

The `putIfAbsent` return check stays: two threads can race on the same new key, and the loser must take the
winner's value.

</details>

</details>

### Describe a code snippet #13
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
long start = System.currentTimeMillis();
action.run();
long elapsed = System.currentTimeMillis() - start;
```

</details>

<details><summary>Show answer</summary>

Wrong clock for measuring elapsed time.

`currentTimeMillis` reads wall-clock time, which can jump while `action` runs — an NTP correction, a manual clock
change, daylight-saving — and can even move backwards. The subtraction then gives a wrong or negative `elapsed`
(clock time vs a steady counter).

Use `nanoTime`, a steady counter meant for intervals:

```java
long start = System.nanoTime();
action.run();
long elapsed = System.nanoTime() - start;
```

Its absolute value means nothing, but the difference is a true elapsed time, unaffected by clock adjustments.

</details>

</details>

### Describe a code snippet #14
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
synchronized (obj) {
  if (queueIsEmpty())
    obj.wait();
  process(obj.removeFirst());
}
```

</details>

<details><summary>Show answer</summary>

`wait` in an `if`, not a `while`.

If the thread wakes with the queue still empty — a stale wakeup, an over-generous `notifyAll`, a stray notify, or
a spurious wakeup — the `if` doesn't recheck, so `removeFirst` runs on an empty queue and the invariant breaks.

Minimal fix — recheck in a loop:

```java
synchronized (obj) {
  while (queueIsEmpty())
    obj.wait();
  process(obj.removeFirst());
}
```

Better: don't hand-write `wait`/`notify` at all in new code. A `BlockingQueue` gives this exact
producer-consumer wait with `take`, correctly and without the loop; `Condition` or a latch covers the other
cases. `wait`/`notify` is low-level plumbing to maintain in legacy code, not to write fresh.

</details>

</details>

### Describe a code snippet #15
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public synchronized void awaitReady() throws InterruptedException {
  while (!ready)
    wait();          // waits on 'this' — a publicly reachable object
}
```

</details>

<details><summary>Show answer</summary>

Waiting on `this`, which any outside code can reach.

A `synchronized` instance method locks `this`, and `wait()` waits on `this`. Because the object is public, unrelated
code can `synchronized (obj) { obj.notify(); }` on it — accidentally or maliciously — waking this thread when
`ready` is still false, or worse, swallowing a real notification. The `while` loop absorbs the false wakeup, but the
exposed lock is still a design flaw.

Minimal fix — wait on a private lock nobody else can touch:

```java
private final Object lock = new Object();
public void awaitReady() throws InterruptedException {
  synchronized (lock) {
    while (!ready)
      lock.wait();
  }
}
```

Better: skip `wait`/`notify` for new code. A `CountDownLatch` expresses "wait until ready" directly and can't be
tampered with through a public monitor; the higher-level utilities don't expose this surface at all.

</details>

</details>

### Describe a code snippet #1
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Counter {
  private int count;
  public synchronized void increment() { count++; }
  public synchronized int get() { return count; }
}
```

</details>

<details><summary>Show answer</summary>

The class is thread-safe, but it locks on `this` — a publicly reachable lock.

`synchronized` methods lock the instance, and the instance is visible to everyone. A client can
`synchronized (counter) { … }` and hold the lock indefinitely, stalling every other caller — a denial-of-service,
by accident or on purpose. A subclass can also lock `this` for an unrelated reason and collide with these methods
on the same lock.

Fix — lock on a private final object nobody else can see:

```java
public class Counter {
  private final Object lock = new Object();
  private int count;
  public void increment() { synchronized (lock) { count++; } }
  public int get()        { synchronized (lock) { return count; } }
}
```

This works because the class is unconditionally thread-safe — it publishes no lock for clients to acquire. A
conditionally thread-safe class couldn't do this: it must expose the lock it asks clients to hold.

</details>

</details>

### Describe a code snippet #1
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private volatile FieldType field;
private FieldType getField() {
  FieldType result = field;
  if (result == null)
    field = result = computeFieldValue();
  return result;
}
```

</details>

<details><summary>Show answer</summary>

This is the single-check idiom, and it's correct only for a field that tolerates being initialized more than once.

There's no lock and no second check, so two threads can both see `field == null` and both run `computeFieldValue`.
Each stores and returns its own value; different callers may briefly get different objects. Nothing throws — the
behavior is a repeated init, not an error. That's fine only if repeat init is harmless.

If the field must be initialized exactly once (the usual case), this is the wrong idiom — use double-check, which
adds the lock and the second check:

```java
private volatile FieldType field;
private FieldType getField() {
  FieldType result = field;
  if (result == null) {
    synchronized (this) {
      if (field == null)
        field = result = computeFieldValue();
    }
  }
  return result;
}
```

`volatile` is required in both. In single-check it's what makes a computed value visible to other threads; dropping
it gives the racy single-check variant, valid only for non-`long`/`double` primitives when recomputation per thread
is acceptable.

</details>

</details>

### Describe a code snippet #1
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class SlowCountDownLatch {
  private int count;

  public SlowCountDownLatch(int count) {
    if (count < 0) throw new IllegalArgumentException(count + " < 0");
    this.count = count;
  }

  public void await() {
    while (true) {
      synchronized (this) {
        if (count == 0) return;
      }
    }
  }

  public synchronized void countDown() {
    if (count != 0) count--;
  }
}
```

</details>

<details><summary>Show answer</summary>

`await` busy-waits: it loops forever, taking and releasing the lock on every pass just to check `count`. A waiting
thread stays runnable the whole time, so each waiter pins a processor doing nothing. With many waiters this is
drastically slower than a latch that sleeps until signalled — roughly ten times slower for a thousand waiters.

The thread should block until woken, not spin. The real fix is not to hand-write this at all — use
`java.util.concurrent.CountDownLatch`, which parks waiting threads and wakes them when the count hits zero. If you
must implement waiting yourself, wait on the monitor and signal it:

```java
public synchronized void await() throws InterruptedException {
  while (count != 0)
    wait();
}
public synchronized void countDown() {
  if (count != 0 && --count == 0)
    notifyAll();
}
```

Now waiters consume no CPU while parked, and `countDown` wakes them only when the latch opens.

</details>

</details>