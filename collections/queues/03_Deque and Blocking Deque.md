### What problem does `Deque` solve compared to Queue?
<details><summary>Show answer</summary>


A **Deque** (pronounced “deck”) is a **double‑ended queue** that allows elements to be 
inserted and removed **at both the head and the tail**.


Like a Queue, a Deque can be used as a conduit for passing data between producers and consumers. 
Its additional flexibility, however, enables usage patterns that are not possible with a single‑ended queue.


In particular, the ability to remove elements from the **tail** makes Deque suitable for work‑stealing algorithms — 
a load‑balancing technique in which idle threads steal tasks from busier threads to improve parallel efficiency.


Deques can also be used to model state where updates are naturally applied at either end, such as:
- sliding windows, 
- undo/redo histories, or 
- stack‑like and queue‑like structures within the same abstraction.

</details>

### What is a common problem in parallel systems with multiple threads, and what solutions are used to handle it?
<details><summary>Show answer</summary>

In parallel systems, work is often divided across multiple threads. A common problem is load imbalance:
- Some threads finish their tasks quickly and become idle
- Others are still busy, holding many tasks
- Overall throughput drops because idle threads do nothing

Work‑stealing is a technique that **redistributes work dynamically** to keep all threads busy.

**Core idea of work‑stealing:**

**Idle threads steal work from busy threads instead of waiting.**

Each worker thread maintains its **own deque (double‑ended queue)** of tasks:
- The owning thread:
  - adds new tasks to one end (typically the tail)
  - removes tasks from the same end
- An idle thread:
  - steals a task from the opposite end (typically the head) of another thread’s deque

This separation minimizes contention and keeps stealing efficient.

The double‑ended nature of a Deque enables **two different access patterns**:
- **Local access (fast, uncontended)** - The owning thread pushes and pops tasks from one end.
- **Stealing access (safe, controlled)** - Other threads remove tasks from the opposite end.

With a simple Queue, all threads would compete for the same end, increasing contention and reducing performance.

</details>

### What does system‑level coordination look like in a work‑stealing scheduler?
<details><summary>Show answer</summary>

**The core question restated:**

If each worker thread A,B,C,D has its own deque: QA, QB, QC, QD 
how does an idle worker (say B) know **which other deques exist**, and **how does it safely try to steal from them**?

Each worker’s deque is registered with a shared worker pool, 
allowing idle workers to randomly and safely attempt steals from other workers’ deques without centralized coordination.

- **The deques are private in ownership, but globally registered in a shared worker pool.**
- Workers do not magically discover each other.
- They are explicitly managed by a scheduler / pool (In Java, that scheduler is `ForkJoinPool`)

Conceptually:
```text
WorkerPool
 ├─ Worker A → deque QA
 ├─ Worker B → deque QB
 ├─ Worker C → deque QC
 └─ Worker D → deque QD
```
So when B becomes idle, it already knows:
- “I am worker #1”
- “There are N workers in this pool”
- “Here are references to their deques”

No discovery problem exists — the pool provides visibility.


An idle worker follows a **simple stealing strategy** used by `ForkJoinPool`:
- Pick another worker at random / probes **random victims**
- Attempt to steal one task from the head of that worker’s deque
- If successful → execute task
- If unsuccessful → try another worker
- Repeat until work is found or termination conditions apply

The system relies on **probability**, not strict ordering.

</details>

### Where is work‑stealing commonly used?
<details><summary>Show answer</summary>

- Fork/Join frameworks (e.g., Java’s ForkJoinPool)
- Parallel task schedulers
- Divide‑and‑conquer algorithms
- Reactive and asynchronous execution engines

</details>

### What is the interface hierarchy of `Deque`?
<details><summary>Show answer</summary>

The Deque interface extends both Queue and SequencedCollection, inheriting queue semantics while adding symmetric access to the beginning and end of the collection.

<img src="../../docs/images/Sequenced_Collections_Hierarchy.png" alt="Implementations of Queue in the Collections Framework" width="600"/>


</details>

### What functionality is exposed by the `Deque` API?
<details><summary>Show answer</summary>

The Deque interface extends Queue with symmetric methods that operate explicitly on both the head and the tail.

#### Collection-like methods

- `boolean removeFirstOccurrence(Object o)` remove the first occurrence of o
- `boolean removeLastOccurrence(Object o)` remove the last occurrence of o
  These methods similar to `Collection::removeIf`

#### Methods inherited from `SequencedCollection`
Of the seven methods of `SequencedCollection`, six are in fact promoted from `Deque`:
throw an exception for a full deque:
- `void addFirst(E e)` insert e at the head if there is enough space
- `void addLast(E e)` insert e at the tail if there is enough space
  throw an exception for an empty deque:
- `E getFirst()` retrieve but do not remove the first element (a synonym for `Queue.element`)
- `E getLast()` retrieve but do not remove the last element
- `E removeFirst()` retrieve and remove the first element (a synonym for `Queue.remove`)
- `E removeLast()` retrieve and remove the last element

#### Queue-like methods
- `boolean offerFirst(E e)` insert e at the head if the deque has space
- `boolean offerLast(E e)` insert e at the tail if the deque has space (a synonym for `Queue.offer`)
  return null for an empty deque:
- `E peekFirst()` retrieve but do not remove the first element (a synonym for `Queue.peek`)
- `E peekLast()` retrieve but do not remove the last element
- `E pollFirst()` retrieve and remove the first element (a synonym for `Queue.poll`)
- `E pollLast()` retrieve and remove the last element

#### Stack-like methods
- `void push(E e)` insert e at the head if there is enough space (a synonym for `Deque.addFirst` provided for stack use)
- `E pop()` retrieve and remove the first element (a synonym for `Deque.removeFirst` provided for stack use)

#### Methods that return elements in revers order:
- `Iterator<E> descendingIterator()` get an iterator, returning deque elements in reverse order
- `Deque<E> reversed()` return a reverse-ordered view of this `Deque` -
  a covariant override of the `SequencedCollection` method, returning a `Deque`

</details>

### What implementations of `Deque` are provided in Java?
<details><summary>Show answer</summary>

- [`ArrayDeque`](#what-is-arraydeque)
- [`LinkedList`](#linkedlist-as-implementation-of-deque)

</details>

### What is ArrayDeque?
<details><summary>Show answer</summary>

`ArrayDeque` fills an important gap among Queue implementations.

Previously, a FIFO queue in a single‑threaded context had to be implemented using `LinkedList`, 
or by accepting unnecessary synchronization overhead from concurrent implementations 
such as `ArrayBlockingQueue` or `LinkedBlockingQueue`.


`ArrayDeque` provides a lightweight, non‑thread‑safe alternative and is now the general‑purpose implementation of choice 
for both deques and FIFO queues in single‑threaded or externally synchronized environments.

</details>

### What underlying data structure does `ArrayDeque` use, and why?
<details><summary>Show answer</summary>


`ArrayDeque` is based on a **circular array**, similar to `ArrayBlockingQueue`.

This design allows the head and tail indices to advance continuously and wrap around the array, 
enabling efficient insertion and removal at both ends.


Using a circular array is better suited for a deque than a linear array because it avoids shifting elements. 
In a non‑circular array, removing the head would require relocating all remaining elements 
to maintain a contiguous layout, which is inefficient.


Using a circular array is often preferable to a linked‑list–based structure because it avoids 
the overhead of per‑element object allocation and pointer traversal, 
providing better cache locality and generally superior performance for insertion and removal at either end.

</details>

### What are the performance characteristics of `ArrayDeque`, and how do its iterators behave?
<details><summary>Show answer</summary>

Head and tail insertions and removals run in constant time, as in a circular array.

The iterators are fail-fast.

</details>

### What is `LinkedList` as an implementation of `Deque`
<details><summary>Show answer</summary>

As an implementation of Deque, **`LinkedList` is rarely the preferred choice**. 
Its primary advantage — constant‑time insertion and removal at the head and tail — is also provided by `ArrayDeque`, 
which is generally superior due to lower memory overhead and better cache locality.


The main reason to use `LinkedList` as a queue or deque is when the use case requires **frequent insertions or removals 
in the middle of the collection**, in addition to operations at the ends — an uncommon requirement 
for queue‑ or deque‑oriented algorithms.


Even in that scenario, `LinkedList` comes with a significant drawback: 
locating an element in the middle requires a linear traversal, resulting in `O(n)` time complexity 
before the constant‑time insertion or removal can occur. 
As a result, its practical advantages over `ArrayDeque` are limited in most real‑world use cases.

Its iterators are fail-fast.

</details>

### `LinkedList` vs `ArrayDeque` as implementations of `Deque`
<details><summary>Show answer</summary>

When comparing `LinkedList` and `ArrayDeque` as implementations of Deque, `ArrayDeque` is generally the better choice.


`ArrayDeque` is backed by a **circular array**, which avoids the per‑element object allocation and 
pointer traversal inherent in the linked‑node structure used by LinkedList. 
As a result, `ArrayDeque` offers better **cache locality**, lower memory overhead, and typically 
**superior performance** for insertion, removal, and iteration at both ends of the deque.


`LinkedList` does provide constant‑time insertion and removal at the head and tail, 
but this advantage is largely matched by `ArrayDeque` without incurring the costs of additional object 
creation and pointer chasing. While `LinkedList` supports insertions and removals in the middle of the list, 
accessing those positions requires linear traversal (`O(n)`), which limits the practical benefit of this flexibility 
for typical deque use cases.


In practice, **`ArrayDeque` is the preferred general‑purpose implementation of Deque**, 
while `LinkedList` is mainly appropriate when middle‑of‑list modifications are genuinely required and 
performance is less critical.

</details>

### What blocking‑specific operations are introduced by the BlockingDeque interface?
<details><summary>Show answer</summary>


`BlockingQueue` adds blocking variants of queue operations, 
allowing elements to be enqueued or dequeued either indefinitely or for a fixed timeout. 
These operations apply to a single end of the queue.


`BlockingDeque` extends this idea by providing blocking operations for both ends of a Deque. 
For each blocking queue operation, it introduces head‑ and tail‑specific variants, giving full symmetric control.

Blocking insertion methods
- `void putFirst(E e)` – inserts the element at the head, waiting indefinitely if necessary
- `void putLast(E e)` – inserts the element at the tail, waiting indefinitely if necessary
- `boolean offerFirst(E e, long timeout, TimeUnit unit)` – inserts at the head, waiting up to the specified timeout
- `boolean offerLast(E e, long timeout, TimeUnit unit)` – inserts at the tail, waiting up to the specified timeout

Blocking removal methods:
- `E takeFirst()` – removes and returns the head, blocking until an element becomes available
- `E takeLast()` – removes and returns the tail, blocking until an element becomes available
- `E pollFirst(long timeout, TimeUnit unit)` – removes from the head, waiting up to the specified timeout
- `E pollLast(long timeout, TimeUnit unit)` – removes from the tail, waiting up to the specified timeout

</details>

### What implementations of `BlockingDeque` are available, and what are their characteristics?
<details><summary>Show answer</summary>

`BlockingDeque` has a single implementation in the JDK: `LinkedBlockingDeque`.


`LinkedBlockingDeque` is backed by a **doubly linked list** structure, similar to `LinkedList`. 
It can be created as either **unbounded** or **bounded**; 
in addition to the default constructors, 
it provides a constructor that allows an explicit **capacity limit** to be specified.


Its performance characteristics closely mirror those of `LinkedBlockingQueue`: 
insertion and removal operations at either end run in **constant time**, 
while operations that require traversal — such as `contains` — have **linear time complexity**. 


The iterators are **weakly consistent**, 
meaning they are safe for concurrent use but do not provide a snapshot view of the deque.

</details>
