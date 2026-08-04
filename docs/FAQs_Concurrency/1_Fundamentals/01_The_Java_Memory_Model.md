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

