### Which operations and what complexity do hash tables have?
<details><summary>Show answer</summary>


As long as there are no _collisions_, the cost of inserting or retrieving an element is **constant**.


</details>

### The probability of a collision
<details><summary>Show answer</summary>


As the hash table fills, collisions become more likely;
assuming a good hash function, the probability of a collision in a lightly loaded table is proportional to its load,
defined as the number of elements in the table divided by its capacity (the number of _buckets_).


</details>

### How it works if a collision takes place
<details><summary>Show answer</summary>


If a collision does take place, an overflow structure - a linked list or tree - has to be created
and subsequently traversed, adding an extra cost to insertion.

The overflow structure used is normally a linear list,
but if for a given bucket the list grows in length past a certain threshold,
it is converted to a red-black binary tree.

If the size of the hash table is fixed, performance will worsen as more elements are added and the load increases.
To prevent this from happening, the table size is increased by rehashing -
copying all elements to a newly allocated, larger table - when the load reaches a specified threshold (its load factor).


</details>

### Iterating over a hash table
<details><summary>Show answer</summary>


Iterating over a hash table requires each bucket to be examined to see whether it is occupied and
therefore requires an execution count proportional to the capacity of the hash table
plus the number of elements it contains.

Since the iterator examines each bucket in turn, the order in which elements are returned depends on their hash codes,
so there is no guarantee as to the order in which the elements will be returned.

`LinkedHashSet`, a variant of `HashSet` implementation with an iterator that
instead returns elements in their insertion order.


</details>

### `EnumSet` operations complexity
<details><summary>Show answer</summary>


`add`, `remove`, and `contains` are implemented as bit manipulations, with constant-time performance.
Bit manipulation on a single word is extremely fast, and a long value can be used to represent
EnumSets over enum types with up to 64 values


</details>

### Calling `addFirst` or `addLast` on an element that is already present in `LinkedHashSet`
<details><summary>Show answer</summary>


Calling these methods will reposition an element as the first or last one.


</details>

### Calling `addFirst` or `addLast` on an element that is already present in `NavigableSet`
<details><summary>Show answer</summary>


`addFirst` and `addLast` cannot fulfill their contract, and accordingly throw `UnsupportedOperationException`.


</details>

### The Methods of `NavigableSet`
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

### `NavigableSet` API methods to work with range views
<details><summary>Show answer</summary>


Each of the methods in this group appears in two overloads,
one inherited from `SortedSet` that returns a _half-open_ `SortedSet` view,
and one defined in `NavigableSet` returning a `NavigableSet` view that can be _open_, _half-open_, or _closed_
according to the user’s choice:

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

### Types of intervals in a range
<details><summary>Show answer</summary>


An interval such as a range view can be open, half-open, or closed,
depending on how many of its limit points it contains.

For example, the range of numbers x for which `0 ≤ x ≤ 1` is closed, because it contains both limit points 0 and 1.
The ranges `0 ≤ x <` 1 and `0 < x ≤ 1` are half-open because they contain only one of the limit points,
and the range `0 < x < 1` is open because it contains neither.


</details>

### Navigating the set in reverse order
<details><summary>Show answer</summary>


- `NavigableSet<E> descendingSet()` return a reverse-order view of the elements in this set
- `Iterator<E> descendingIterator()` return a reverse-order iterator

Methods of this group make traversing a `NavigableSet` equally easy in the descending (that is, reverse) ordering.


</details>

### `SequencedSet` methods compared with their `NavigableSet` equivalents
<details><summary>Show answer</summary>


Although NavigableSet has been retrofitted to extend SequencedSet,
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
the first six `SequencedSet` methods were copied from Deque.

Prior to the introduction of SequencedSet in Java 21,
`SortedSet` and `NavigableSet` had several methods that were similar to the `Deque` methods,
but with different names and somewhat different behavior:
- `NavigableSet::first` and `NavigableSet::last`, inherited from `SortedSet`, are the same as
  `SequencedSet::getFirst` and `SequencedSet::getLast`, throwing `NoSuchElementException` if the collection is empty.
- `NavigableSet::pollFirst` and `NavigableSet::pollLast` remove and return the respective element.
  However, they differ from `SequencedSet::removeFirst` and `SequencedSet::removeLast` in that the poll methods
  return null on an empty collection instead of throwing `NoSuchElementException`.


</details>

### Cost of insertion into a binary tree
<details><summary>Show answer</summary>


The cost of retrieving or inserting an element is proportional to the depth of the tree.


</details>

### How deep is a tree that contains N elements?
<details><summary>Show answer</summary>


The complete binary tree with two levels has three elements (that’s `2^2 – 1`),
and the one with three levels has seven elements (`2^3 – 1`).
In general, a binary tree with N complete levels will have 2^N – 1 elements,
and the depth of a tree with N elements will be bounded by `log N` (since 2^(log N) = N).


</details>

### Trees performance, what might affect it, how it could be solved?
<details><summary>Show answer</summary>


Not all binary trees will have this nice performance, though.
A balanced binary tree—one in which each node has an equal number of descendants (or as near as possible) on each side.
An unbalanced tree can give much worse performance—in the worst case, as bad as a linked list.
TreeSet uses a data type called a red-black tree,
which has the advantage that if it becomes unbalanced through insertion or removal of an element,
it can always be rebalanced in O(log N) time.


</details>

### A tree vs. a hash table vs. a list regarding elements retrieval functionality
<details><summary>Show answer</summary>


- A hash table can’t return its elements in sorted order.
- A list can’t retrieve its elements quickly by their content.
- A tree can do both.


</details>

### A tree vs. a hash table vs. a list regarding performance
<details><summary>Show answer</summary>

The cost of retrieving or inserting an element is proportional to the depth of the tree.
The depth of a tree with N elements will be bounded by `log N`.
Just as `N` grows much more slowly than `2^N`, `log N` grows much more slowly than `N`,
so `contains` on a large tree is much faster than on a list containing the same elements.

It’s still not as fast as a hash table - whose operations can ideally work in constant time -
but a tree has the big advantage over a hash table in that its iterator can return its elements in sorted order.


</details>
