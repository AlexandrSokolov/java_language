### What’s the complexity of hash table operations?
<details><summary>Show answer</summary>

Assuming no hash collisions, insertion and lookup have O(1) time complexity/take constant time.

</details>

### What is the probability of a collision in a hash table?
<details><summary>Show answer</summary>


As a hash table fills up, collisions become more likely.

Assuming a good hash function, the probability of a collision in a lightly loaded table 
is proportional to its load factor, 
defined as the number of stored elements divided by the table’s capacity (the number of buckets).


</details>

### What happens when a collision occurs?
<details><summary>Show answer</summary>


When a collision occurs, an overflow structure — such as a linked list or a tree — must be created and 
subsequently traversed, which adds extra cost to insertion.

The overflow structure is typically a linked list; 
however, if the list for a given bucket grows beyond a certain threshold, it is converted into a red‑black tree.


If the size of the hash table is fixed, performance degrades as more elements are added and the load increases. 

To avoid this, the table is resized through rehashing — copying all elements into a newly allocated, larger table — 
once the load reaches a predefined threshold (the load factor).

</details>

### How does iteration over a hash table work?
<details><summary>Show answer</summary>

Iterating over a hash table requires examining each bucket to determine whether it is occupied. 
As a result, the total execution cost is proportional to 
the capacity of the hash table plus the number of elements it contains.


Because the iterator processes buckets sequentially, the order in which elements are returned 
depends on their hash codes. Consequently, a standard hash table provides 
no guarantee about the iteration order of its elements.


`LinkedHashSet` is a variant of `HashSet` whose iterator instead returns elements in their insertion order.”

</details>

### What is the time complexity of `EnumSet` operations?
<details><summary>Show answer</summary>


The `add`, `remove`, and `contains` operations are implemented using bit manipulations 
and therefore **run in constant time**. 


Bit manipulation on a single machine word is extremely fast, and a single long value can represent an `EnumSet` 
for enum types with **up to 64 distinct values**.

</details>

### What happens if `addFirst` or `addLast` is called with an element that is already present in a `LinkedHashSet`?
<details><summary>Show answer</summary>

Calling these methods will reposition an element as the first or last one.

</details>

### What happens if `addFirst` or `addLast` is called with an element that is already present in a `NavigableSet`
<details><summary>Show answer</summary>

`addFirst` and `addLast` cannot fulfill their contract, and accordingly throw `UnsupportedOperationException`.

</details>

### What functionality is exposed by the `NavigableSet` API?
<details><summary>Show answer</summary>

#### Retrieving the comparator

`Comparator<? super E> comparator()` - This is the method to retrieve the set’s comparator,
if it has been given one at construction time.
If the set uses the natural ordering of its elements, this method returns null.

#### Inspecting the first and last elements
- `E first()`
- `E last()`
  If the set is empty, these operations throw `NoSuchElementException`.

#### Removing the first and last elements
- `E pollFirst()` - retrieve and remove the first (lowest) element, or return null if this set is empty
- `E pollLast()` - retrieve and remove the last (highest) element, or return null if this set is empty

#### Getting range views
- `subSet`
- `headSet`
- `tailSet`

#### Getting closest matches
- `E ceiling(E e)`
  return the least element x in this set such that x≥e, or null if there is no such element
- `E floor(E e)`
  return the greatest element x in this set such that x≤e, or null if there is no such element
- `E higher(E e)`
  return the least element x in this set such that x>e, or null if there is no such element
- `E lower(E e)`
  return the greatest element x in this set such that x<e, or null if there is no such element

These methods are useful for short-distance navigation.

</details>

### Why does the `NavigableSet` API provide `pollFirst()` and `pollLast()` when `removeFirst()` and `removeLast()` exist?
<details><summary>Show answer</summary>

Both method pairs **remove and return** the first or last element when the set is not empty.


The difference appears **when the set is empty**:
- `removeFirst()` / `removeLast()` (from `SequencedCollection`) - throw `NoSuchElementException` if empty
- `pollFirst()` / `pollLast()` (from `NavigableSet`) - return null if the set is empty, do not throw an exception


This mirrors the long‑standing Java convention from Queue / Deque:
- **poll**: safe, null if empty
- **remove**: strict, exception if empty


Therefore:
- `NavigableSet` aligns with queue/deque semantics
- `SequencedCollection` aligns with collection semantics

</details>

### How does the `NavigableSet` API support range views?
<details><summary>Show answer</summary>

Each method in this group has two overloads:
- one inherited from `SortedSet` that returns a _half-open_ `SortedSet` view,
- one defined in `NavigableSet` returning a `NavigableSet` view that can be _open_, _half-open_, or _closed_
  according to the user’s choice:


`NavigableSet` Range‑View API:
- `SortedSet<E> subSet(E fromValue, E toValue)`
  return a view of the portion of this set ranging from fromValue, inclusive, to toValue, exclusive
- `SortedSet<E> headSet(E toValue)`
  return a view of the portion of this set up to but excluding toValue
- `SortedSet<E> tailSet(E fromValue)`
  return a view of the portion of this set whose elements are greater than or equal to fromValue
- `NavigableSet<E> subSet(E fromValue, boolean fromInclusive, E toValue, boolean toInclusive)`
  return a view of the portion of this set ranging from fromValue to toValue
- `NavigableSet<E> headSet(E toValue, boolean inclusive)` - return a view of the portion of this set up to toValue
- `NavigableSet<E> tailSet(E fromValue, boolean inclusive)` - return a view of the portion of this set from fromValue

</details>

### What are the different types of intervals in a range?
<details><summary>Show answer</summary>

An interval, such as a range view, may be open, half‑open, or closed, 
depending on how many of its boundary (limit) points it includes..


For example:
- the range of numbers `x` for which `0 ≤ x ≤ 1` is closed, because it contains both boundary points 0 and 1
- the ranges `0 ≤ x < 1` and `0 < x ≤ 1` are half-open because they contain only one of the limit points
- the range `0 < x < 1` is open, as it includes neither boundary point


</details>

### How do you navigate a set in reverse order?
<details><summary>Show answer</summary>

- `NavigableSet<E> descendingSet()` return a reverse-order view of the elements in this set
- `Iterator<E> descendingIterator()` return a reverse-order iterator

</details>

### How do `SequencedSet` methods compare to their `NavigableSet` equivalents?
<details><summary>Show answer</summary>

Although `NavigableSet` has been retrofitted to extend `SequencedSet`,
none of the new methods provide any different functionality;
they are just renamed versions of existing methods.

| SequencedSet |                     NavigableSet                      |
|:------------:|:-----------------------------------------------------:|
|   getFirst   |           first (inherited from SortedSet)            |
|   getLast    |            last (inherited from SortedSet)            |
| removeFirst  |                       pollFirst                       |
|  removeLast  |                       pollLast                        |
|   addFirst   | Unsupported method for internally ordered collections |
|   addLast    | Unsupported method for internally ordered collections |
|   reversed   |                     descendingSet                     |

The reason for the duplication between methods of `SequencedSet` and `NavigableSet` is that
the first six `SequencedSet` methods were copied from `Deque`.

</details>

### What is the cost of inserting into a binary tree?
<details><summary>Show answer</summary>

The cost of retrieving or inserting an element is proportional to the depth of the tree.

</details>

### How deep is a tree that contains N elements?
<details><summary>Show answer</summary>

The complete binary tree with two levels has three elements (that’s `2^2 – 1`),
and the one with three levels has seven elements (`2^3 – 1`).


In general, a binary tree with N complete levels will have `2^N – 1` elements,
and the depth of a tree with N elements will be bounded by `log N` (since `2^(log N) = N`).

</details>

### What factors influence tree performance, and how can they be improved?”
<details><summary>Show answer</summary>


Not all binary trees exhibit this favorable performance. 
A balanced binary tree — one in which each node has roughly the same number of descendants on both sides — 
maintains good performance characteristics.


An unbalanced tree, however, can perform much worse; in the extreme case, 
its performance degrades to that of a linked list.


`TreeSet` uses a red‑black tree, a self‑balancing binary search tree that guarantees logarithmic performance. 
If the tree becomes unbalanced due to insertion or removal, it can be rebalanced in `O(log n)` time.

</details>

### What are the trade‑offs between lists, hash tables, and trees with respect to search performance and iteration order?
<details><summary>Show answer</summary>

- A list supports ordered iteration but has slow lookup by value. 
- A hash table supports very fast lookup — ideally in constant time — but does not maintain sorted order. 
- A tree lies between the two: its operations run in `O(log n)` time, 
  which is slower than a hash table but much faster than a list, and it can return its elements in sorted order.

</details>
