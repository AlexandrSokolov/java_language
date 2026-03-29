### What are the benefits and challenges of preventing collections from being modified?
<details>
<summary>Show answer</summary>

Immutability: the state stays unchanged after creation.
 
Immutability confers a number of advantages on a program:
- Immutable objects are thread-safe
- Immutable objects are perfectly encapsulated, thus removing the need for _defensive copying_
- Immutability guarantees stable lookup in keyed and ordered collections
- Immutability reduces the number of states a program can be in, making it simpler, clearer,
  and easier to understand and reason about.

</details>

---

### Realizing immutability in Java
<details>
<summary>Show answer</summary>

Realizing immutability to get its advantages in a Java program is difficult.

</details>

---

### What is required to keep mutable components effectively immutable?
<details>
<summary>Show answer</summary>

To keep mutable components effectively immutable, you must ensure that 
**every part of the object graph containing those components is under exclusive control**. 
This means:
- No other code can hold references to the mutable objects.
- No other code can modify any nested objects that your component depends on.
- The entire structure must be shielded from outside interference.

This requirement is difficult in practice because objects in Java often reference other objects, 
which in turn reference more objects. If any of those referenced objects can be modified by someone else, 
the outer object is no longer effectively immutable.


In other words, immutability is only guaranteed when the whole object graph is isolated, 
and all its mutable elements are accessed and modified exclusively by the owning component (or not modified at all).

</details>

---


### What terminology is used to describe immutability in collections?
<details>
<summary>Show answer</summary>


Different libraries and documents use different terminology to describe immutability in collections, 
which can be confusing:
- **Guava** and **Eclipse Collections** distinguish between deep immutability (the entire object graph cannot change) 
  and **shallow immutability** (the collection itself cannot be modified, but its elements may be).
- The **Java Collections Framework** uses **immutability** to mean **deep immutability**
  and calls shallow immutability **unmodifiability**.

</details>

---

### What are the advantages of using unmodifiable collections?
<details>
<summary>Show answer</summary>


Unmodifiability - refusing modification **at the first level** - 
that is, an attempt to add, remove, or replace an element.

Its advantages:
- Unmodifiable collections of immutable elements (such as strings or wrapper types) 
  provide all the benefits of true immutability.
- Even shallow immutability reduces the number of possible program states, making code easier to reason about.
- Unmodifiable sets and maps can be backed by compact array‑based structures, offering significant memory savings.

</details>

---

### What options exist for making collections thread‑safe?
<details>
<summary>Show answer</summary>

#### Synchronized Wrapper Collections

Synchronized wrapper collections provide thread safety by synchronizing every operation, 
but this approach has poor performance and is rarely suitable for highly concurrent workloads. 

They were introduced in JDK 1.2 after internal synchronization in JDK 1.0 collections proved too costly, 
making explicit wrappers a way to opt in to this slower, coarse‑grained locking model only when needed.


**They are slow and outdated, and should be avoided in modern concurrent code.**

#### Concurrent Collections

Concurrent collections eliminate the need for client‑side locking, 
because they are designed so that no single external lock can block all operations. 
When atomicity is required — such as inserting a value only if a key is absent — they provide dedicated atomic methods 
like `ConcurrentMap.putIfAbsent`.


For most use cases that require thread safety, concurrent collections deliver 
**far better performance than synchronized wrappers**. 
They avoid the heavy cost of serializing every operation and the high contention overhead of coarse‑grained locks, 
and in multi‑threaded workloads this often results in **massive performance gains**, sometimes by orders of magnitude.

</details>

---

### What mechanisms do concurrent collections use to achieve thread safety?
<details>
<summary>Show answer</summary>


#### copy-on-write

Copy‑on‑write collections store their data in an internal array that is treated as immutable. 
Whenever the collection is modified, a **new array** is created to represent the updated state, 
while the old one remains unchanged.


These classes use synchronization only briefly while creating the new array, but all 
**read operations proceed without locking**, since the underlying array never changes. 
As a result, copy‑on‑write collections perform very well in scenarios dominated by reads and only a few writes.

The collection classes `CopyOnWriteArrayList` and `CopyOnWriteArraySet` use this mechanism.

#### compare-and-set (CAS)

Compare‑and‑set (CAS) is a low‑level atomic operation that lets a thread update a value 
**only if** it has not been changed by another thread. A thread reads the current value, performs its work locally, 
and then uses CAS to replace the value only if it still matches the original. 
If another thread modified it in the meantime, the CAS fails and the operation can be retried.


Because CAS avoids locking and does not block other threads, it enables **optimistic concurrency**, 
unlike traditional synchronization which forces threads to wait. 
This makes CAS‑based collections highly scalable under contention.

Collections using CAS include `ConcurrentLinkedQueue` and `ConcurrentSkipListMap`.

#### Lock‑based concurrency - fine‑grained locking

Some concurrent collections use explicit `java.util.concurrent.locks.Lock` implementations, 
which provide more flexible locking than the built‑in synchronized keyword. 
A lock can be acquired under additional conditions — such as trying only if it’s free, waiting with a timeout, 
or allowing interruptible acquisition — and is held until `unlock()` is called, even across different methods.


This flexibility lets collections split their internal structure into independently locked regions, 
improving concurrency. For example, `LinkedBlockingQueue` uses separate locks for the head and tail 
so that inserts and removals can proceed in parallel. 
Many `BlockingQueue` implementations rely on this lock‑based approach.

</details>

---

### How do collection iterators behave in multithreaded environments?
<details>
<summary>Show answer</summary>


#### Fail-Fast Iterators

Fail‑fast iterators are the default for non‑concurrent collections.

On each access, they check whether the collection has been structurally modified by 
comparing its modification counter with the iterator’s own snapshot. 
If a mismatch is detected — that is, the collection was changed outside of the iterator — 
the iterator immediately throws a `ConcurrentModificationException` instead of continuing with unpredictable results.

This behavior exists to help detect bugs, but it is not guaranteed by the collection contract.


#### Snapshot iterators

Snapshot iterators are used by copy‑on‑write collections. 
These collections store their data in arrays that never change in place; 
whenever the collection is modified, a new array is created. 
Because iterators read from the old, unmodified array, 
they can iterate safely without being affected by concurrent updates. 
As a result, snapshot iterators never throw `ConcurrentModificationException`.

#### Weakly consistent iterators

Weakly consistent iterators can reflect some of the changes made to the underlying collection 
after the iterator is created, but they do not guarantee that all modifications will be visible. 
Updates like removals or changes to existing elements are typically seen as the iterator progresses, 
while insertions may or may not appear.

These iterators never throw `ConcurrentModificationException`, and they are used by collections that rely on:
- CAS‑based concurrency or 
- lock‑based implementations

</details>

---

### What should you keep in mind when working with fail‑fast iterators?
<details>
<summary>Show answer</summary>

Their behavior exists to help detect bugs, but it is not guaranteed by the collection contract.

In practice this means:
- The iterator **will try** to detect unexpected modifications and fail quickly.
- But the specification does **not** require it to detect all such cases.
- The iterator **may or may not** throw the exception depending on timing, JVM optimizations, or implementation details.
- Relying on fail‑fast behavior in production logic is unsafe — it is **not a contractual guarantee**,
  only a “best‑effort check.”

So the iterator **usually** throws the exception to help you identify bugs,

</details>

---

### How do collections support distributed workloads?
<details>
<summary>Show answer</summary>

Collections support distributed workloads through **recursive decomposition**, 
a technique where a data set is progressively split into smaller parts that can be processed independently 
and then combined into a final result.


Each collection provides a `Spliterator` that knows how to divide its data efficiently, 
enabling the runtime to determine how to break the work into manageable chunks.


The Stream API hides the complexity of implementing **recursive decomposition** manually.
When a stream is created as parallel — either directly or via `.parallel()` — the framework uses 
the collection’s `Spliterator` to decompose the data across multiple threads, process each segment independently, 
and merge the results:

```java
OptionalDouble maxDistance = intList.stream()
    .parallelStream()
    .map(i -> new Point(i % 3, i / 3))
    .mapToDouble(p -> p.distance(0, 0))
    .max();
```
Under compute‑heavy workloads with minimal I/O, this approach can achieve near‑ideal parallelism.

</details>

---
