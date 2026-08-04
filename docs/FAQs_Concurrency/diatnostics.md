
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