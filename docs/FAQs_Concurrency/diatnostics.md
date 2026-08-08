
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