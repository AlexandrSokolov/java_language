### Describe a bytecode snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```text
public void ?(int);
  0: aload_0
  1: aload_0
  2: getfield      #2   // Field balance:D
  5: iload_1
  6: i2d
  7: dadd
  8: putfield      #2   // Field balance:D
 11: return
```

</details>

<details><summary>Show answer</summary>

**A read-modify-write on one field — the shape that loses updates.** In source this is one line, `balance =
balance + amount`. In bytecode it is three distinct steps: `getfield` reads `balance` onto the stack, `dadd`
computes the new value, `putfield` writes it back. No lock (no `monitorenter`).

Why it's unsafe: the read and the write are separate instructions with a gap between them. Two threads can both
`getfield` the same starting value, both compute, and both `putfield` — the second write overwrites the first,
and one update is lost. Each thread has its own private evaluation stack, so they don't see each other's computed
value.

Fix: make the whole read-modify-write indivisible — a lock around it, or an atomic type:

```java
synchronized (this) { balance += amount; }
// or a lock-free counter type doing getAndAdd as one atomic step
```

Handle: `getfield ... putfield` on the same field with no monitor is a read-modify-write — two threads interleave
between the read and the write and one update vanishes.

</details>

</details>

---

### Describe a bytecode snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```text
// two threads A and B, same object, method with no monitor instructions
A2: getfield   #2   // Field balance:D   -- A reads balance (say 0)
// context switch A -> B
B2: getfield   #2   // Field balance:D   -- B reads the SAME 0
B7: dadd                                 -- B computes 0 + 50
B8: putfield   #2   // Field balance:D   -- B writes 50
// context switch B -> A
A7: dadd                                 -- A computes 0 + 70 (from its stale read)
A8: putfield   #2   // Field balance:D   -- A writes 70, overwriting B's 50
```

</details>

<details><summary>Show answer</summary>

**Lost update, shown in the interleaving.** Both threads `getfield` the balance before either writes, so both
start from the same value (0). B writes 50; then A — still holding its own stale read of 0 on its private stack —
writes 70, erasing B's 50. Two deposits "succeed" (70 + 50 = 120 paid in), but the final balance is 70. Money
vanished.

The tell is the order `A:getfield, B:getfield, B:putfield, A:putfield` (or `A:get, B:get, A:put, B:put`) — two
reads before either write. Any interleaving where a second read happens before the first write loses an update.

Fix: a lock forces the whole `getfield ... putfield` of one thread to finish before the other starts:

```text
A: monitorenter  A: getfield  A: putfield  A: monitorexit
B: monitorenter  B: getfield  B: putfield  B: monitorexit
```

Handle: two `getfield`s before either `putfield` = lost update — the fix is to make each thread's read-through-
write run as one locked unit.

</details>

</details>

---

### Describe a bytecode snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```text
public boolean ?(int);
  0: aload_0
  1: dup
  2: astore_2
  3: monitorenter                        // <-- (a)
  4: aload_0
  5: getfield      #2   // Field balance:D
  8: iload_1
  9: i2d
 10: dcmpl
 11: iflt          29
 14: aload_0
 15: aload_0
 16: getfield      #2   // Field balance:D
 19: iload_1
 20: i2d
 21: dsub
 22: putfield      #2   // Field balance:D
 25: iconst_1
 26: aload_2
 27: monitorexit                          // <-- (b)
 28: ireturn                              // <-- (c) why i-return for a boolean?
 29: aload_2
 30: monitorexit
 31: goto          39
 34: astore_3                             // <-- (d) how is this ever reached?
 35: aload_2
 36: monitorexit
 37: aload_3
 38: athrow
 39: iconst_0
 40: ireturn
```

</details>

<details><summary>Show answer</summary>

**A synchronized block, plus two things worth spotting.**

- **(a)/(b) The lock.** `monitorenter` at 3 acquires the lock on `this` (loaded, `dup`'d, stored so it can be
  released later); `monitorexit` at 27 releases it. That pair is the `synchronized (this) { ... }` block. If
  another thread hits `monitorenter` on the same object, it blocks there until this thread's `monitorexit` runs.
- **(c) `ireturn` for a `boolean`.** There is no boolean return opcode. `boolean`, `byte`, `short`, `char` are all
  represented as `int` in bytecode, so a `boolean` method returns with `ireturn`. (A form of type erasure — the
  same idea people usually meet only with generics.)
- **(d) The unreachable-looking block at 34–38.** No normal path reaches it. It's the **exception path**: several
  instructions (including `monitorenter`) can throw, and the lock must still be released. This block catches any
  throw, runs `monitorexit`, and re-throws (`athrow`) — so the monitor is never left locked on an exception.

The compiler guarantees every path out of a method holding a monitor executes a matching `monitorexit`, and the
classfile verifier rejects any class that violates this at load time.

Handle: `monitorenter`/`monitorexit` is a synchronized block; `ireturn` on a `boolean` is int-erasure; the
"unreachable" `monitorexit` is the exception path that guarantees the lock is released even on a throw.

</details>

</details>

---

### Describe a bytecode snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```text
public synchronized boolean ?(int);
  0: aload_0
  1: getfield      #2   // Field balance:D
  4: iload_1
  5: i2d
  6: dcmpl
  7: iflt          23
 10: aload_0
 ...
 // no monitorenter, no monitorexit anywhere
```

</details>

<details><summary>Show answer</summary>

**A synchronized method — and you can't tell from the body.** There are no monitor opcodes at all, yet the method
*is* synchronized. The `synchronized` modifier on a method is not compiled to `monitorenter`/`monitorexit`; it
becomes a flag on the method, `ACC_SYNCHRONIZED`, in the method's metadata.

When the interpreter executes an `invoke` for this method, it checks that flag first and, if set, acquires the
lock before running the body — a different code path from an unsynchronized call. So the locking is real; it's
just recorded as a flag, not as opcodes.

The trap: reading only the opcode stream, a synchronized method looks identical to an unsynchronized one. You must
look at the method's access flags (the header line shows `synchronized`) — the body gives nothing away.

Handle: no monitor opcodes but still locked — a synchronized method is the `ACC_SYNCHRONIZED` flag, checked by the
interpreter at call time, invisible in the body.

</details>

</details>

---

### Describe a bytecode snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```text
// Thread A runs a SYNCHRONIZED withdraw; Thread B runs an UNSYNCHRONIZED deposit
A3:  monitorenter                         // A takes the lock
A16: getfield   #2   // balance:D         // A reads balance
// context switch A -> B
B2:  getfield   #2   // balance:D         // B reads balance — NO monitorenter first
B7:  dadd
B8:  putfield   #2   // balance:D         // B writes (unsynchronized)
B11: return
// context switch B -> A
A22: putfield   #2   // balance:D         // A writes, overwriting B's deposit
A27: monitorexit                          // A releases the lock
```

</details>

<details><summary>Show answer</summary>

**Lost update again — because only one method locks.** A holds the lock (`monitorenter` at A3). But B's method has
**no `monitorenter`**, so B never checks the lock and runs straight through its read-modify-write while A is mid-
operation. B writes; then A writes, overwriting B's deposit. B's money is lost.

The point: a lock only excludes threads that *try to acquire it*. A monitor held by A stops another synchronized
method, but an unsynchronized method ignores it completely — `putfield` doesn't consult any monitor. One
unguarded path defeats all the synchronized ones.

Fix: every method that touches the shared field must synchronize on the same lock — no exceptions, including reads.

Handle: a lock guards a field only if *every* accessing method takes it — one method with no `monitorenter` walks
past the lock and the update is lost.

</details>

</details>

---

### Describe a bytecode snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```text
public boolean ?(Account, int);
  3: monitorenter                          // (1) lock THIS
 14: aload_0
 16: getfield   #2   // balance:D
 22: putfield   #2   // balance:D          // debit this
 25: aload_1
 26: dup
 27: astore     4
 29: monitorenter                          // (2) lock OTHER, while still holding THIS
 30: aload_1
 31: iload_2
 32: invokevirtual #6  // Method rawDeposit:(I)V
 37: monitorexit                           // release OTHER
 ...
 51: monitorexit                           // release THIS
```

</details>

<details><summary>Show answer</summary>

**Nested locks in fixed source order — a deadlock waiting for the opposite call.** Two `monitorenter`s: (1) locks
`this`, then (2) locks `other` *while still holding* `this`. Between them the field is debited. Two locks held at
once, acquired in the order the caller's objects happen to be.

The deadlock: thread A transfers a→b (locks a, wants b); thread B transfers b→a (locks b, wants a) at the same
time. A holds a and blocks at its second `monitorenter` for b; B holds b and blocks for a. `A0 == B1` and
`A1 == B0` — the two objects are the same pair in opposite roles, so each thread's second `monitorenter` waits on
the lock the other holds. Neither reaches its `monitorexit`; both hang forever.

Fix: acquire the two locks in one global order (e.g. by account id, lowest first) so both threads take the same
lock first and the cycle can't form.

Handle: two `monitorenter`s with the second taken while holding the first, in caller order, deadlocks when two
threads lock the same pair in opposite order — impose one global lock order.

</details>

</details>

---

### Describe a bytecode snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```text
private volatile boolean shutdown;   // field is declared volatile

public void shutdown();
  0: aload_0
  1: iconst_1
  2: putfield   #2   // Field shutdown:Z     // ordinary putfield
  5: return

public void run();
  0: aload_0
  1: getfield   #2   // Field shutdown:Z     // ordinary getfield
  4: ifne       10
  7: goto       0
 10: return
```

</details>

<details><summary>Show answer</summary>

**The Volatile Shutdown pattern — and `volatile` is invisible in the opcodes.** `run()` loops while `shutdown` is
false (`getfield`, `ifne` to exit, `goto 0` to repeat); `shutdown()` sets it true. Another thread flips the flag,
and the loop sees it on its next read and exits cleanly — a graceful shutdown, the safe replacement for the
deprecated `stop()`.

The thing to notice: `shutdown` is `volatile`, but the accesses use plain `getfield` / `putfield` — the same
opcodes as any field. The volatile nature shows up *only* in the field declaration, not in the instruction stream.
The interpreter takes a different path for a volatile field (going to main memory, with the memory barrier), but
the opcode is identical.

Why it's correct: a volatile write happens-before every later read of that field, so once `shutdown()` writes
`true`, the running thread is guaranteed to see it on the next loop check — not stuck reading a cached `false`
forever (which is exactly what a plain `boolean` here would risk).

Handle: Volatile Shutdown is a `volatile` flag polled in a loop; the flag is invisible in the opcodes (plain
`getfield`/`putfield`) — the visibility guarantee lives in the field, not the instructions.

</details>

</details>

---
