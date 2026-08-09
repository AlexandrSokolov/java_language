### Design: timing concurrent execution
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

Write a method that times how long it takes to run an action concurrently. It takes an executor to run the work,
a concurrency level (how many threads run the action at once), and the action itself. All worker threads must be
ready before timing starts; the clock starts when the last one is ready and stops when the last one finishes — so
thread startup and scheduling are excluded from the measured time.

```java
public static long time(Executor executor, int concurrency, Runnable action)
    throws InterruptedException {
  // ?
}
```

</details>

<details><summary>Show answer</summary>

Three latches split the run into three checkpoints: all-ready, go, all-done.

```java
public static long time(Executor executor, int concurrency, Runnable action)
    throws InterruptedException {
  CountDownLatch ready = new CountDownLatch(concurrency);  // workers report ready
  CountDownLatch start = new CountDownLatch(1);            // timer releases the gate
  CountDownLatch done  = new CountDownLatch(concurrency);  // workers report finished

  for (int i = 0; i < concurrency; i++) {
    executor.execute(() -> {
      ready.countDown();            // I'm ready
      try {
        start.await();              // wait for the starting gun
        action.run();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();   // reassert, let executor handle it
      } finally {
        done.countDown();           // I'm done
      }
    });
  }

  ready.await();                    // all workers ready
  long startNanos = System.nanoTime();
  start.countDown();                // fire the gun — all workers proceed together
  done.await();                     // all workers finished
  return System.nanoTime() - startNanos;
}
```

- `ready` (count = concurrency): each worker counts it down, then blocks on `start`. When it hits zero every
  worker is parked at the gate — the timer knows startup is over.
- `start` (count = 1): one `countDown` from the timer releases all workers at once, so they run together.
- `done` (count = concurrency): the timer blocks until the last worker finishes, then stops the clock.

Precondition: the executor must be able to run at least `concurrency` threads at once. If it can't, some workers
never reach `start.await`, `ready` never reaches zero, and the method hangs forever.

</details>

</details>