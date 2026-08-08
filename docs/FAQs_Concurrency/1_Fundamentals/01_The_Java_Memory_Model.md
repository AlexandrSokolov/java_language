### What is synchronization?
<details><summary>Show answer</summary>

The `synchronized` keyword lets only one thread run a method or block at a time. 
A thread must take the lock to enter; others wait until it leaves.

Two forms:

```java
synchronized void update() { ... }        // locks on `this` for the whole method

void update() {
  synchronized (lock) { ... }             // locks on a chosen object, for just this block
}
```

The method form locks on the object itself; 
the block form locks on whatever object you name, and guards only the lines inside.

</details>

### What does synchronization guarantee?
<details><summary>Show answer</summary>

- **Mutual exclusion** — one thread at a time, so no thread sees the object half-changed while another is writing it.
  Applied to a single operation, this is atomicity.
- **Visibility** — a thread taking the lock sees every earlier change made under that same lock. 
  Without it, one thread's writes may never reach another.

Synchronization is not only for keeping threads apart, it is also how one thread's changes reliably reach another.

</details>

### Two ways to get synchronization wrong?
<details><summary>Show answer</summary>

Synchronization fails in two opposite directions — too little or too much:

- **Too little — insufficient.** You miss one of the two things safe sharing needs, so updates collide or never
  arrive. [What makes synchronization insufficient](#what-problem-does-synchronization-solve).
- **Too much — excessive.** Two kinds of damage:
  - **Correctness** — deadlock or nondeterministic behavior, from *what* you call under the lock:
    [never cede control to the client while holding a lock](../03_Liveness_Performance_Testing/06.1_Liveness_Problems.md#calling-client-code-while-holding-a-lock).
  - **Performance** — from *how much* you do under the lock:
    [what synchronization costs in performance](../03_Liveness_Performance_Testing/06.2_Performance_&_Scalability.md#what-does-synchronization-cost-in-performance).

One dial, two failure ends: too little breaks correctness, too much breaks liveness and performance.

</details>

### What problem does synchronization solve?
<details><summary>Show answer</summary>

Threads sharing the same mutable data need **safe sharing** —
and [safe sharing needs two things](02_Sharing_Objects.md#sharing-a-mutable-variable--what-to-ensure):
- each update must land in one step, and
- each update must actually reach the other threads.


Miss either and it breaks quietly — no error, just a wrong result:
- **Updates collide.** `count++` is read-modify-write; two threads read the same value, both add one, one
  increment is lost.
- **Updates never arrive.** One thread writes, another keeps reading an old copy forever, because nothing forces
  the write out to it.

Synchronization is the tool that covers both at once: only one thread runs the guarded code at a time, and each
thread taking the lock sees every earlier change made under it. That is [what it guarantees](#what-does-synchronization-guarantee).

</details>

### What problem does synchronization solve?
<details><summary>Show answer</summary>

Threads sharing the same mutable data need **safe sharing** — and [safe sharing needs two things](02_Sharing_Objects.md#sharing-a-mutable-variable--what-to-ensure):
each update must land in one step, and each update must actually reach the other threads. Miss either and it
breaks quietly — no error, just a wrong result:

- **Updates collide.** `count++` is read-modify-write; two threads read the same value, both add one, one
  increment is lost.
- **Updates never arrive.** One thread writes, another keeps reading an old copy forever, because nothing forces
  the write out to it.

Synchronization is the tool that covers both at once: only one thread runs the guarded code at a time, and each
thread taking the lock sees every earlier change made under it. That is [what it guarantees](#what-does-synchronization-guarantee).

</details>

### Calling client code while holding a lock?
<details><summary>Show answer</summary>

Never do it. Inside a `synchronized` method or block, only call code you control. A method you hand off to —
an overridable method, a listener, a function passed in — is **alien**: you don't know what it does while you
hold the lock.

What it can do, all bad:

- **Deadlock.** The alien method tries to take a lock you already hold (or takes a second lock in the opposite
  order another thread uses), and both sides wait forever.
- **Reentered state.** The alien method calls back into your object and sees it mid-update — a half-changed state
  you assumed no one could observe while locked.
- **Nondeterministic failure.** Whether it breaks depends on what the caller's code happens to do, so it passes
  in tests and fails in production.

The fix: do the work that needs the lock, take a snapshot if needed, release the lock, *then* call the alien
method outside the guarded region.

</details>

### What is atomicity?
<details><summary>Show answer</summary>

An operation is **atomic** when it happens in one step — no other thread can see it half-done.

A single read or write of most types is atomic on its own. 
But `i++` is not: it is three steps — read `i`, add one, write it back. 
Two threads can both read the same value, both add one, and both write back the same result — one increment is lost.

Atomicity is mutual exclusion at the smallest scale. Mutual exclusion keeps a whole block indivisible; 
an atomic operation is a single step that is already indivisible. When an operation is not atomic on its own
(like `i++`), you wrap it in a `synchronized` block to make the whole thing indivisible.

</details>

### How do you make reads and writes atomic?
<details><summary>Show answer</summary>

Most types are already atomic — a single read or write of any type 
**except** `long` and `double` cannot be seen half-done, even without synchronization.

`long` and `double` are 64-bit and may be read or written in two halves, so a reader can catch a mix of old and new. 
To make those atomic, guard them with synchronization or declare them `volatile`.

</details>

### Which operation isn't atomic on its own?
<details><summary>Show answer</summary>

Any **read-modify-write** — read the value, change it, write it back. 
`i++`, `i--`, `i += 1`, `x = x + 1` are all this shape. 
Each is three steps, so two threads can read the same value, both change it, and both write back — one update is lost.

Making the type atomic does not help: `volatile int` fixes visibility but the three steps can still interleave. 
Use an atomic type instead, whose increment is one indivisible step:

```java
AtomicInteger count = new AtomicInteger();
count.incrementAndGet();   // read-modify-write done atomically
```

</details>

### What happens when a thread re-takes its own lock?
<details><summary>Show answer</summary>

It succeeds — the thread already owns the lock, so taking it again is free and it doesn't block.

This comes up when a thread holds the lock and, still inside the guarded region, calls a method that takes the
same lock — a synchronized method calling another synchronized method on the same object. The second take would
block forever if the thread had to wait for a lock it itself holds.

A lock that lets the same thread take it again is a **reentrant** lock. Java's `synchronized` locks are reentrant.

</details>

### Why is reentrancy useful?
<details><summary>Show answer</summary>

It stops a thread from deadlocking against itself. A synchronized method that calls another synchronized method
on the same object would otherwise block forever — waiting for a lock it already holds.

```java
class Counter {
  private int count;
  synchronized void incrementBy(int n) {
    for (int i = 0; i < n; i++)
      increment();          // takes the same lock this thread already holds
  }
  synchronized void increment() {
    count++;
  }
}
```

`incrementBy` holds the lock, then calls `increment`, which takes the same lock. Reentrancy lets the second take
succeed; without it the thread would freeze against itself. This pattern is common in object-oriented code, so
reentrancy is what makes locks usable there at all.

</details>

