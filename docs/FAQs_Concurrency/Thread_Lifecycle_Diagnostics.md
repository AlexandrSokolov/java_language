### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Runnable r = () -> {
  try {
    Thread.sleep(1000);
    doWork();
  } catch (InterruptedException e) {
    log.warn("interrupted", e);   // "handled" — then falls through and keeps going
  }
  doMoreWork();
};
```

</details>

<details><summary>Show answer</summary>

**Swallowed interrupt — the stop request is lost.** The dev thinks logging the exception "handles" it. But
catching `InterruptedException` clears the interrupt flag, and this catch does nothing else — so after the catch
the thread no longer looks interrupted, falls through to `doMoreWork()`, and runs on as if never asked to stop.
Code higher up the stack can never tell the interrupt happened.

**Fix — rethrow, or restore the flag and stop:**

```java
} catch (InterruptedException e) {
  Thread.currentThread().interrupt();   // restore the flag the throw cleared
  return;                               // and actually stop this thread's work
}
```

Best of all, declare `throws InterruptedException` and let the caller decide, when the method can propagate it.

Handle: catching `InterruptedException` clears the flag — logging isn't handling; rethrow or restore and stop, or
the interrupt is eaten.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
try {
  Thread.sleep(1000);
} catch (InterruptedException e) {
  e.printStackTrace();
}
// react to interruption later:
if (Thread.currentThread().isInterrupted()) {
  cleanup();                      // never runs
}
```

</details>

<details><summary>Show answer</summary>

**The interrupt check never fires — the flag was already cleared.** The dev correctly wants to react to
interruption after the sleep. But throwing `InterruptedException` clears the interrupt flag as part of the throw —
it's considered handled. So by the time control reaches `isInterrupted()`, the flag is `false`, and `cleanup()`
never runs even though the thread *was* interrupted.

**Fix — restore the flag in the catch, so the later check can see it:**

```java
} catch (InterruptedException e) {
  Thread.currentThread().interrupt();   // put the flag back
}
if (Thread.currentThread().isInterrupted()) {
  cleanup();                            // now runs
}
```

Or handle the interruption directly in the catch instead of checking again later.

Handle: throwing `InterruptedException` clears the flag — a later `isInterrupted()` check sees nothing unless you
restore it in the catch.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
class Worker implements Runnable {
  private boolean running = true;     // plain field, set from another thread

  public void run() {
    while (running) {                 // busy work loop
      process();
    }
  }

  public void stop() {                // called by a different thread
    running = false;
  }
}
```

</details>

<details><summary>Show answer</summary>

**Clean shutdown that never happens — the flag isn't visible across threads.** The dev builds a tidy stop switch:
another thread sets `running = false`, the loop exits. But `running` is a plain field. The write on the calling
thread may sit in that core's cache and never reach the worker's core; the worker keeps reading its own cached
`true` and loops forever. It often *works in testing* (short runs flush by luck) and hangs in production — the
worst kind.

**Fix — make the flag `volatile` so the write publishes:**

```java
private volatile boolean running = true;   // write is flushed, read is fresh
```

Better still, use interruption for cancellation and let blocking calls in `process()` respond to it:

```java
public void run() { while (!Thread.currentThread().isInterrupted()) process(); }
// stop via t.interrupt();
```

Handle: a cancellation flag read by another thread must be `volatile` (or use interruption) — a plain `boolean`
can be cached and never seen.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
var t = new Thread(() -> {
  while (true) {          // heavy compute, no interrupt check
    crunch();
  }
});
t.start();
t.interrupt();           // ask it to stop
t.join();                // expect it to end
```

</details>

<details><summary>Show answer</summary>

**`interrupt()` does nothing here — the loop never checks.** The dev expects `interrupt()` to stop the thread. But
interruption is opt-in: `interrupt()` only sets a flag. The thread stops only if its own code checks that flag, or
calls a blocking method (`sleep`, `wait`, I/O) that checks it and throws. A bare `while(true)` with pure compute
does neither, so the flag is set and ignored, and `join()` blocks forever.

**Fix — check the flag in the loop:**

```java
while (!Thread.interrupted()) {   // exits when interrupted
  crunch();
}
```

**Not guaranteed to stop instantly even then** — the loop finishes the current `crunch()` before the next check.
Interruption is cooperative, so the thread stops at its next check point, not mid-operation.

Handle: `interrupt()` only sets a flag — a compute loop must poll `Thread.interrupted()`, or the request is
ignored and `join()` hangs.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
var t = new Thread(task);
t.start();
t.setDaemon(true);   // want the JVM to exit without waiting for this thread
t.join();
```

</details>

<details><summary>Show answer</summary>

**`IllegalThreadStateException` at `setDaemon` — configured too late.** The dev wants the thread to be a daemon so
it won't keep the JVM alive. But daemon status can only be set *before* the thread starts. Calling `setDaemon`
after `start()` throws `IllegalThreadStateException` at runtime.

**Fix — set daemon status (and name, priority, handlers) before `start()`:**

```java
var t = new Thread(task);
t.setDaemon(true);   // before start
t.start();
```

The same "configure before start" rule applies to `setName`, `setPriority`, and `setUncaughtExceptionHandler` —
some throw if set on a running thread, and others simply have no effect.

Handle: a thread's daemon status must be set before `start()` — configuring it after throws
`IllegalThreadStateException`.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
var t = new Thread(() -> {
  risky();                 // may throw a RuntimeException
});
t.start();

try {
  t.join();
} catch (Exception e) {
  handleWorkerFailure(e);  // expects to catch the worker's exception here
}
```

</details>

<details><summary>Show answer</summary>

**The worker's exception never reaches this catch — exceptions don't cross thread boundaries.** The dev wraps
`join()` in a try/catch expecting to receive whatever the worker threw. But an exception from `risky()` belongs to
the worker thread; it kills that thread and is gone. `join()` only waits for the thread to finish — it does not
re-throw the worker's exception. `handleWorkerFailure` never runs, and the failure is invisible. (The catch here
would only fire on an `InterruptedException` in the joining thread — unrelated to the worker's crash.)

**Fix — set an uncaught-exception handler before starting the thread:**

```java
var t = new Thread(() -> risky());
t.setUncaughtExceptionHandler((thread, e) -> handleWorkerFailure(e));   // before start
t.start();
```

For richer control, run the work through an `ExecutorService` and inspect the returned `Future` — `future.get()`
*does* rethrow the task's exception (wrapped in `ExecutionException`) to the caller.

Handle: an exception in a thread dies with it — `join()` doesn't rethrow it; catch it with an uncaught-exception
handler, or use a `Future`.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Runnable r = () -> {
  while (!Thread.currentThread().isInterrupted()) {
    try {
      Thread.sleep(1000);
      poll();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();   // restore the flag — good habit
      // ... but no break/return, so the loop continues
    }
  }
};
```

</details>

<details><summary>Show answer</summary>

**Restore is correct but incomplete — the loop can't make progress and spins.** The dev does the right thing
restoring the interrupt flag in the catch. But there's no `break`/`return`, so control loops back. The loop
condition now sees the restored flag and exits — *if* it re-checks first. The subtler failure is when the restore
is followed by more blocking work in the same iteration: the very next `sleep()` sees the set flag and throws
`InterruptedException` immediately, so the thread thrashes through throw-restore-throw instead of stopping
cleanly.

**Fix — after restoring, actually leave the loop; don't run more interruptible work:**

```java
} catch (InterruptedException e) {
  Thread.currentThread().interrupt();   // preserve the request for callers
  break;                                // and stop now — no further blocking calls
}
```

Restoring the flag is for *callers up the stack*; it does not mean "keep going." The thread that was interrupted
must still wind down.

Handle: restoring the interrupt flag preserves the request but doesn't stop the thread — pair it with a
`break`/`return`, and don't re-enter a blocking call that will instantly re-throw.

</details>

</details>

---
