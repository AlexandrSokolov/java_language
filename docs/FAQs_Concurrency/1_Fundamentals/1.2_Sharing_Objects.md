### Sharing a mutable variable — best approach?
<details><summary>Show answer</summary>

Avoid sharing the same mutable object where you can. Ways, in order of preference:

- **Confine it to one thread** — if only one thread ever touches the data, there is no sharing and nothing
  to synchronize.
- **Give each thread its own copy** — a defensive copy hands out state disconnected from yours, so one
  thread's changes can't reach another's. Works when:
  - the object is cheap to copy and 
  - threads don't need to share the *result*, only to avoid colliding on the input.
- **Share it immutable** — data that never changes after construction is safe for any number of threads to
  read at once.

If none of these fit and you are forced to share the same mutable object live, you cannot remove the
problem — you have to manage it, and [there are two things you must ensure](#what-does-safe-sharing-require).

</details>

### What does safe sharing require?
<details><summary>Show answer</summary>

- **Atomicity** — a read or write completes in one step, so no thread sees it half-done.
- **Visibility** — a write by one thread actually reaches the threads that read it.

Atomicity alone fails: the value is whole, but a reader may keep seeing an old copy forever.
Visibility alone fails: the update reaches readers, but a read-modify-write (like `i++`) still lets two
threads collide. Sharing safely means covering both.

</details>

### Safe sharing vs synchronization?
<details><summary>Show answer</summary>

Different levels — one is the goal, the other is one way to reach it:

- **Safe sharing** — the goal: many threads use the same mutable data without corruption.
- **Synchronization** — one mechanism that delivers both, for any block of code.

Synchronization is not the only way to share safely — it is the one that covers both guarantees at once.
Others cover part or sidestep the need entirely:
- `volatile` gives visibility only,
- an atomic type gives both for a single variable.

Reach for synchronization when you must share live mutable state and need both — visibility and atomicity — at once.

</details>

### Sharing a mutable object without ensuring atomicity or visibility?
<details><summary>Show answer</summary>

Possible when the object stops changing before it is shared. 
One thread builds it by modifying it freely, then stops, and shares the reference. 
If it is never changed again, other threads read it and you need neither atomicity nor visibility on those reads.


Both requirements fall away for the same reason — there are no writes after publication. 
Nothing to make atomic (no read-modify-write is happening), and nothing to miss (the value never changes again). 
You cover visibility once, on the hand-off, so readers see a fully-built object; after that every read is free.

The object was mutable while built, but from publication on it never changes, so readers treat it as immutable. 
Such an object is called **effectively immutable**.

</details>

### Effectively immutable — what does it mean?
<details><summary>Show answer</summary>

An object that is not immutable by construction, but whose state is never changed after a point — so it
behaves as immutable and can be shared for reads without synchronization.

Two ways the "never changed" holds:

- **Nobody writes it after publication.** The object is shared, but by agreement no thread modifies it once handed over. 
  Unchanged by promise.
- **Nobody else can reach it.** The owner keeps exclusive control — the state is never exposed or published, 
  so no outside code holds a reference to change it. Unchanged because it is unreachable — see 
  [exclusive control of the object graph](../../FAQs/04_Classes_&_Interfaces/4.2_classes_and_interfaces.md#what-is-required-to-keep-mutable-components-effectively-immutable)

Same result — state that never changes — reached two ways: a promise in the first, no access in the second.

</details>

### Passing a built object between threads safely?
<details><summary>Show answer</summary>

Hand the reference over in a way that guarantees the receiving thread sees the object fully built — all its
fields written, not half-constructed. This correct hand-off is called **safe publication**.

How you actually do it: [safe publication needs the right kind of hand-off](#how-do-you-safely-publish-a-reference).
When it goes wrong: [unsafe publication](#how-can-publication-go-wrong).

</details>

### How do you safely publish a reference?
<details><summary>Show answer</summary>

Any hand-off that forces the writes-then-reference to become visible together — each way carries that
guarantee, so you don't memorize a list, you recognize the mechanism:

- **Static field set during class init** — the JVM guarantees class initialization is seen by all threads.
- **`final` field** — the memory model guarantees a `final` field is visible once the constructor finishes.
- **`volatile` field** — a write is visible to any thread that later reads it.
- **Field guarded by a lock** — the reader takes the same lock, so it sees the writer's earlier writes.
- **A concurrent collection** — putting the reference in guarantees a safe hand-off to whoever takes it out.

The through-line: every one draws a happens-before line at the moment of sharing, so the writes that
built the object land before the reader touches it.

</details>

### How can publication go wrong?
<details><summary>Show answer</summary>

Making the reference visible is not enough on its own. The writes that built the object and the write of the
reference are separate — another thread can see the reference before it sees those field writes, and read an
object that is only half-constructed.

The reference arrives, but the fields it points to are still being written. So the reader gets a valid-looking
pointer to a broken object — no error, just wrong data. This is **unsafe publication**, and it's what
[safe publication](#passing-a-built-object-between-threads-safely) exists to prevent.

</details>

### Safe publication vs effectively immutable?
<details><summary>Show answer</summary>

They answer different questions, and you usually need both:

- **Effectively immutable** — a property of the *object*: its state never changes after a point, 
  so  readers need no synchronization to read it.
- **Safe publication** — a property of the *hand-off*: 
  the reference is passed so the receiving thread sees the object fully built, not half-made.

They combine, not compete. Effectively immutable makes the object safe to read; 
safe publication makes sure the reader gets the finished object in the first place. 
An effectively immutable object published *unsafely* is still unsafe — 
a thread can see the reference before the object's writes, and read a half-built object. 
Immutable-after-a-point removes the need to lock later reads; 
safe publication is what makes that first hand-off correct.

</details>


### Does the synchronized modifier document thread safety?
<details><summary>Show answer</summary>

No. `synchronized` on a method is an implementation detail, not part of the API — Javadoc doesn't even emit it,
and its presence doesn't reliably mean the method is safe to call concurrently.

The deeper mistake is treating thread safety as all-or-nothing. It has levels, and a class must state in prose (or
a thread-safety annotation) which level it offers. See
[the levels of thread safety](#what-are-the-levels-of-thread-safety). The `synchronized` keyword plays no part in
that documentation.

</details>

### What are the levels of thread safety?
<details><summary>Show answer</summary>

A spectrum, from "never needs synchronization" to "can't be made safe at all":

- Immutable — instances never change, so no synchronization is ever needed. `String`, `Long`, `BigInteger`.
- Unconditionally thread-safe — mutable, but enough internal synchronization that callers need none.
  `AtomicLong`, `ConcurrentHashMap`.
- Conditionally thread-safe — safe, except some sequences need external locking by the caller. The
  `Collections.synchronized…` wrappers, whose iterators must be locked by the client.
- Not thread-safe — the caller must wrap every call (or call sequence) in its own synchronization. `ArrayList`,
  `HashMap`.
- Thread-hostile — unsafe even if the caller synchronizes every call. Usually from unsynchronized mutation of
  static state; such classes get fixed or deprecated, never written on purpose.

Trigger: no sync needed → caller syncs some sequences → caller syncs everything → sync can't save it.

</details>

### What must a class document about thread safety?
<details><summary>Show answer</summary>

State the class's thread-safety level in its doc comment — in prose or as a thread-safety annotation.

- A conditionally thread-safe class must also say *which* call sequences need external locking and *which lock* the
  client acquires for them (usually the instance, sometimes another object).
- A method with its own special thread-safety behavior documents it on the method, not only on the class.
- A static factory must document the returned object's thread safety unless the return type makes it obvious — as
  `Collections.synchronizedMap` does.
- Enum immutability needs no mention; it's a given.

</details>