### Which sets should be used in a multithreaded context?
<details><summary>Show answer</summary>


- [`CopyOnWriteArraySet`](#what-are-the-characteristics-and-operation-costs-of-copyonwritearrayset-and-how-does-it-differ-from-hashset)
- [Concurrent set backed by `ConcurrentHashMap`](#concurrent-set-via-a-concurrent-map)
- [`ConcurrentSkipListSet`](#what-is-concurrentskiplistset-and-how-is-it-implemented)

</details>

### What are the characteristics and operation costs of `CopyOnWriteArraySet`, and how does it differ from HashSet?
<details><summary>Show answer</summary>

The functional specification of `CopyOnWriteArraySet` is derived directly from the `Set` contract,
but its performance characteristics differ significantly from those of `HashSet`.


`CopyOnWriteArraySet` is implemented as a thin wrapper around a `CopyOnWriteArrayList`, which is backed by an array. 
This array is treated as immutable: 
any modification to the set results in the creation of a new array containing the updated elements.


As a consequence:
- `add` runs in `O(N)` time, since it requires copying the entire array
- `remove` runs in O(N) time, because it also creates a new array without the removed element
- `contains` also runs in `O(N)` time, as it is implemented via a linear search
- **iteration is very efficient**, with an `O(1)` cost per element and no need for synchronization


**`CopyOnWriteArraySet` is optimized for concurrency semantics, not algorithmic performance**.
- Conceptually close to a list
- Enforces set semantics
- It trades write performance for safe, lock‑free iteration, 
  while other read operations such as contains still run in linear time

</details>

### When is it appropriate to use `CopyOnWriteArraySet`?
<details><summary>Show answer</summary>

`CopyOnWriteArraySet` is not suitable when searches or updates are performed frequently, 
as both insertion and lookup are linear‑time operations. 
Its strength lies in iteration: iterating costs `O(1)` per element and is thread‑safe without locking.


This makes it well suited for read‑heavy, write‑light scenarios, 
such as shared configuration in a multithreaded application. 
For example, a server may maintain a set of configuration values 
that many threads read frequently but update only rarely. 


With `CopyOnWriteArraySet`, read operations proceed without synchronization overhead, 
while the cost of maintaining thread safety is entirely borne by the infrequent write operations.


</details>

### Concurrent Set via a Concurrent Map
<details><summary>Show answer</summary>


```java
Set<Integer> concurrentIntegerSet = Collections.newSetFromMap(new ConcurrentHashMap<Integer,Boolean>());
```
This creates a **thread-safe `Set` implementation backed by a `ConcurrentHashMap`**.

The set is essentially a view over the map’s keys; 
each element of the set is stored as a key in the map, with a dummy Boolean value.

</details>

### Which concurrent set implementation should be used when sorted order is required?
<details><summary>Show answer</summary>

[`ConcurrentSkipListSet`](#what-is-concurrentskiplistset-and-how-is-it-implemented)

</details>

### What is `ConcurrentSkipListSet`, and how is it implemented?
<details><summary>Show answer</summary>

`ConcurrentSkipListSet` is backed by a skip list, a probabilistic, ordered data structure that serves 
as a modern alternative to balanced binary search trees.


A skip list consists of multiple levels of linked lists: the bottom level contains all elements in sorted order, 
while higher levels provide “express lanes” that allow faster traversal. 
Each node stores a value and references to the next nodes at one or more levels.


Insertion and removal are performed by rearranging pointers in the affected lists, 
yielding expected `O(log n)` time complexity while allowing efficient concurrent updates.


Iterators produced by `ConcurrentSkipListSet` are weakly consistent: 
- they do not throw `ConcurrentModificationException` 
- may reflect some, but not necessarily all, concurrent modifications


</details>

### How does searching work in a skip list?
<details><summary>Show answer</summary>

In a skip list, each node stores one forward (“next”) reference for every level it participates in, 
implemented as an array of forward pointers. 
Every node always appears at level 0 and therefore has at least one forward reference, 
while higher‑level participation is determined randomly at insertion time. 
When searching, “dropping down” does not require following a separate vertical pointer: 
the algorithm stays on the same node and simply switches to a lower index in the node’s forward‑pointer array. 
This design allows constant‑time movement between levels, keeps memory usage linear on average, 
and is the key mechanism that enables efficient O(log n) search without maintaining explicit up or down links.


Conceptually, a skip list looks like multiple linked lists stacked on top of each other, 
but physically there is only one set of nodes. There are no separate list objects per level. 
Instead, each node stores multiple “next” references, one per tier (level) it participates in. 
When you follow all next references at level k, you can view that as a linked list for level k, 
but that list is implicit, not a standalone structure. 
It exists only as a projection of the same nodes using a particular index in their forward‑pointer arrays. 
So the “multiple lists” are really just different views over the same nodes, 
created by choosing different `next[level]` references, 
which is why we often call them logical or pseudo‑lists rather than actual lists.


Consider a skip list consisting of three levels, labeled 0, 1, and 2.


The bottom level (level 0) contains all elements of the set, stored in sorted order — 
either according to their natural ordering or the set’s comparator. 
Each higher level contains a subset of the elements from the level below, 
chosen randomly according to a fixed probability.


In this example, assume a probability of 0.5, so that, on average, 
each higher level contains about half the elements of the level beneath it.


Since moving between linked nodes takes constant time, searching proceeds by:
- starting at the leftmost node of the top level, 
- moving forward as far as possible at each level, 
- and then dropping down to the next level when further progress is not possible. 

This strategy minimizes traversal work and enables efficient search.

<img src="../../docs/images/SkipListSearching.png" alt="Searching a skip list" width="600">

</details>

### How does insertion work in a skip list?
<details><summary>Show answer</summary>

**Inserting an element into a skip list always begins by inserting it at level 0**.


Once this is done, the algorithm decides whether the element should also be inserted at level 1. 
Since level 1 is expected to contain roughly half the elements of level 0, 
this decision is made randomly — typically by tossing a coin with probability 0.5.

**If the element is promoted to level 1, the same process is repeated for level 2, and so on, 
until the random choice fails**.


As a result, each element appears in a number of levels determined by these independent random decisions. 
When an element is removed, it is deleted from every level in which it appears.


**In the worst case, unlucky coin tosses could produce extremes — for example, 
upper levels that are either empty or as full as level 0 — but such cases are exceedingly unlikely**.


Probabilistic analysis shows that, with high probability, 
skip lists achieve performance comparable to balanced binary search trees: 
search, insertion, and removal operations all run in `O(log N)` time.


**A key advantage of skip lists in concurrent environments is 
that they admit efficient lock‑free insertion and deletion algorithms**, 
whereas no comparable lock‑free algorithms are known for balanced binary trees.

</details>

### `Collections.newSetFromMap` with `ConcurrentHashMap` vs. `CopyOnWriteArraySet` vs. `ConcurrentSkipListSet`
<details><summary>Show answer</summary>

They all are thread-safe sets.

- `ConcurrentSkipListSet` - sorted set with `O(log n)` operations, 
  backed by a skip list and supporting navigable order operations
- `CopyOnWriteArraySet` - optimized for frequent iteration and rare updates and rare searches, 
  using copy‑on‑write snapshots
- Concurrent set backed by `ConcurrentHashMap` - a high‑performance, thread‑safe set 
  with fast **`O(1)` `add`, `remove`, and `contains`**, 
  and **the default choice** when you need a general‑purpose concurrent set

</details>
