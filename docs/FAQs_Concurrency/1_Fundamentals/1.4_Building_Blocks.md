### Concurrent vs synchronized collections — when each?
<details><summary>Show answer</summary>

Default to concurrent — they make synchronized collections largely obsolete. 

Synchronized collections still fit two narrow cases:

- No concurrent version exists for the shape you need. Example: a write-heavy list whose writes are not at the
  ends. 
  - Rare writes → `CopyOnWriteArrayList`; 
  - heavy writes at the ends → `LinkedBlockingDeque` / `ConcurrentLinkedDeque`; 
  - neither fits, so `Collections.synchronizedList` is left.
- You must hold the lock yourself across several operations — `synchronized (coll) { ... }` to make a compound
  action atomic, which a concurrent collection won't let you do. 
  Often a sign to redesign rather than the best choice.

</details>

### Synchronizing on a concurrent collection?
<details><summary>Show answer</summary>

Pointless — every concurrent collection manages its own synchronization internally.

```java
synchronized (map) {   // your lock — buys nothing
  map.put(k, v);       // still takes its OWN internal lock too
}
```

Each method already locks internally, so wrapping it adds a second lock over the same work and only slows the program. 

</details>

### Composing method calls on a concurrent collection atomically?
<details><summary>Show answer</summary>

You can't — and don't need to.

You can't lock a concurrent collection to freeze it across two calls, so you can't fuse `containsKey` then `put`
into one atomic step yourself. That composition is already built in: 
single methods that do check-then-act atomically — `putIfAbsent`, `computeIfAbsent`, `compute`, `merge`. 
Reach for one of those instead of trying to combine calls under your own lock.

</details>

### Making a producer or consumer wait until it can proceed?
<details><summary>Show answer</summary>

Use blocking operations — methods that wait until the action can succeed, thread-safe. 
The consumer's `take` waits while there's nothing to take; the producer's `put` waits while there's no room. 
Provided by the `BlockingQueue` interface and its implementations.

</details>

### Making threads wait for each other?
<details><summary>Show answer</summary>

Use a synchronizer — an object that lets threads wait for one another so they can coordinate their work. It holds
shared state (a count, permits, a meeting point) that decides when a waiting thread is released.

</details>

### Which synchronizers exist?
<details><summary>Show answer</summary>

Five, from most to least used, ending with the most flexible:

- Common: [CountDownLatch](#countdownlatch--purpose), [Semaphore](#semaphore--purpose)
- Less common: [CyclicBarrier](#cyclicbarrier--purpose), [Exchanger](#exchanger--purpose)
- Most powerful: [Phaser](#phaser--purpose)

Trigger: two everyday ones (latch, semaphore), two niche ones (barrier, exchanger), one that does it all (phaser).

</details>

### CountDownLatch — purpose?
<details><summary>Show answer</summary>

A one-shot gate. Built with a count N; threads call `await` and block until the count reaches zero. Other threads
call `countDown` to decrement it. Once it hits zero it stays open — it never resets.

Use: let one or more threads wait until N events have finished, once. "Don't start until all N workers are ready."

</details>

### Semaphore — purpose?
<details><summary>Show answer</summary>

Hands out a fixed number of permits. A thread calls `acquire` to take one (waiting if none are free) and `release`
to give it back. Caps how many threads run a section at once.

Use: limit concurrent access to a resource pool — e.g. at most 10 threads hitting a service at a time.

</details>

### CyclicBarrier — purpose?
<details><summary>Show answer</summary>

A meeting point for a fixed number of threads. Each calls `await` and blocks; when the last one arrives, all are
released together. Then it resets and can be used again for the next round — that's the "cyclic".

Use: repeated phases where all threads must finish one step before any starts the next.

</details>

### Exchanger — purpose?
<details><summary>Show answer</summary>

A meeting point for exactly two threads that also swaps data. Each calls `exchange(x)`, blocks for the other, then
both return holding the other's object.

Use: hand data back and forth between two threads — e.g. a filling buffer swapped for an empty one.

</details>

### Phaser — purpose?
<details><summary>Show answer</summary>

The most flexible synchronizer. Like a reusable barrier, but the number of participating threads can change at
runtime — threads `register` and `deregister` — and it advances through numbered phases.

Use: multi-phase work where how many threads take part varies from phase to phase.

</details>

### CountDownLatch vs CyclicBarrier?
<details><summary>Show answer</summary>

- CountDownLatch: one-shot, never resets. Waiters block on a count driven by *other* threads' `countDown` calls —
  they wait for an event to happen, not for each other.
- CyclicBarrier: reusable, resets each round. The threads that wait *are* the ones being counted — they wait for
  each other to all arrive, then proceed together.

Handle: latch = wait for N things to happen (once); barrier = N threads wait for each other (repeatedly).

</details>

### Get the existing value or insert a new one, atomically?
<details><summary>Show answer</summary>

Use `putIfAbsent`: it inserts your value if the key was free and returns `null`, or leaves the map alone and
returns the value already there. Wrap it so the caller always gets the value that's now in the map, never `null`.

On `ConcurrentHashMap`, do a plain `get` first — it's tuned for reads — and only call `putIfAbsent` on a miss:

```java
public static String intern(String s) {
  String result = map.get(s);          // fast path: already present
  if (result == null) {
    result = map.putIfAbsent(s, s);     // miss — try to insert
    if (result == null)                 // we won the race; our value is now the one
      result = s;
    // else: another thread inserted first — putIfAbsent gave us theirs, use it
  }
  return result;
}
```

`get`-first skips the costlier `putIfAbsent` on the common path; the null-check after it stays because two threads
can race on the same new key and the loser gets the winner's value back.

</details>

### What is `CopyOnWriteArrayList` for?
<details><summary>Show answer</summary>

For a list that is read often and written rarely — an observer list is the classic fit.

Every write makes a fresh copy of the whole backing array. Because the array is never changed in place, reads take
no lock and never see a half-changed state — many threads can iterate at once, fast. The cost sits entirely on
writes: each one copies the entire array.

So it wins when reads vastly outnumber writes, and is a bad choice when writes are frequent — copying the whole
array every write is far too expensive then.

</details>

### Measuring how long something took?
<details><summary>Show answer</summary>

Use `System.nanoTime`, not `System.currentTimeMillis`.

`currentTimeMillis` is wall-clock time and can jump mid-measurement — an NTP correction, a manual clock change,
daylight-saving — even backwards, so subtracting two readings can give a wrong or negative duration.

`nanoTime` is a steady counter with no calendar meaning: its absolute value is useless, but the difference between
two readings is a true elapsed time, unaffected by any clock adjustment. It's also finer-grained. Use it for every
interval measurement (wall-clock vs steady counter).

</details>

### wait — the standard idiom?
<details><summary>Show answer</summary>

Call `wait` only inside a `synchronized` block on the same object, and always from a `while` loop that tests the
condition:

```java
synchronized (obj) {
  while (conditionDoesNotHold())
    obj.wait();          // releases the lock, reacquires it on wakeup
  // condition holds — do the work
}
```

`wait` must hold the object's lock (it releases it while waiting, retakes it on wakeup). Never call it outside a
loop — a woken thread must recheck the condition before proceeding.

</details>

### Why call wait in a loop, not an if?
<details><summary>Show answer</summary>

The loop rechecks the condition before and after waiting; one axis, two failures it prevents:

- Test *before* waiting: if the condition already holds and the `notify` already fired, skipping the wait avoids
  waiting for a signal that will never come again — the thread would hang forever (liveness).
- Test *after* waking: a thread can wake with the condition still false; looping back to wait again stops it from
  acting on a state that doesn't hold and wrecking the invariant the lock guards (safety).

An `if` does neither recheck, so it fails both ways.

</details>

### Why might a thread wake when its condition is false?
<details><summary>Show answer</summary>

A wakeup does not promise the condition holds. Four reasons it may not:

- Another thread grabbed the lock and changed the guarded state between the `notify` and this thread waking.
- Another thread called `notify` when the condition didn't hold — by accident or malice. Waiting on a publicly
  reachable object exposes you to this: any code can notify it.
- The notifier was over-generous — called `notifyAll` when only some waiters actually had their condition met.
- A spurious wakeup: the thread woke with no `notify` at all (rare, but real at the OS level).

This is exactly why `wait` lives in a `while` loop — the recheck absorbs every one of these.

</details>

### notify vs notifyAll?
<details><summary>Show answer</summary>

- `notifyAll` wakes every waiting thread; `notify` wakes one.
- Default to `notifyAll` — it's safe. Extra threads that wake, recheck their condition, find it false, and go back
  to waiting, so correctness holds.
- `notify` is an optimization, valid only when all threads in the wait-set wait on the *same* condition and only
  one can proceed when it becomes true.
- Even then `notifyAll` may be safer: it protects against an unrelated thread waiting on the same object and
  "swallowing" the single `notify`, leaving the real recipient stuck forever.

</details>

### Spurious wakeup?
<details><summary>Show answer</summary>

A waiting thread waking without any `notify`/`notifyAll` call. Rare, but permitted by the platform (an OS-level
reality). It's one of the reasons `wait` must always run in a `while` loop that rechecks the condition — the code
must never assume a wakeup means the condition is now true.

</details>
