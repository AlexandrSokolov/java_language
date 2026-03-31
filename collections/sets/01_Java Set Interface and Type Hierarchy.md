### Sets, its purpose
<details><summary>Show answer</summary>


A _set_ is a collection of items that cannot contain duplicates;
adding an item that is already present in the set has no effect.


</details>

### What does define a duplicate in sets?
<details><summary>Show answer</summary>


It depends on implementations
#### Objects equality with _equivalence relation_

`HashSet`, for which the equivalence relation is the `equals` method
and two objects are duplicates if and only if the `equals` method,
called on one with the other as its argument, returns `true`.

#### Objects equality with _identity relation_

A set using that relation will contain a reference to every unique object that has been added to it.
Examples:
- `EnumSet` since enums are singletons, the result of the `equals` method matches
  the result of the identity relation for all comparisons
- the set view of the keys of an `IdentityHashMap`
- any set created from an `IdentityHashMap` using the `Collections.newSetFromMap` method
- Explicitly create set view from identity map:
    ```java
    Set<Integer> concurrentIntegerSet = Collections.newSetFromMap(new IdentityHashMap<Integer,Boolean>());
    ```

#### Objects equality with _ordering relation_

`NavigableSet` - maintains its elements in sorted order using an ordering relation
provided by either its natural order or a `Comparator`.

It defines two objects as equivalent if, using it, they compare as equal -
that is, if the comparison method returns 0 - regardless of whether they satisfy the equality relation.


</details>

### What are consequences of using different equivalence relations?
<details><summary>Show answer</summary>


- sets may contain duplicate elements that satisfy `equals` or, conversely,
  that they may elide occurrences of ones that don’t.
- to determine whether two sets A and B are equal,
  A must test each member of B to discover whether it is equivalent to a member of A.
  If the roles are reversed, and if A and B are using different equivalence relations,
  the results may be different, so set equality loses symmetry.


</details>

### Containing by set multiple objects, that return true when compared to each other by `equals`?
<details><summary>Show answer</summary>


It is possible for `NavigableSet` that uses _ordering relation_ to check equality.


</details>

### How to determine that 2 sets with equivalence relation are equal?
<details><summary>Show answer</summary>


The `equals` method is overridden:
the `Set` contract states that a `Set` can only ever be equal to another `Set`,
and then only if:
- they are the same size and
- contain equal elements.

The `hashCode` method is also overridden, as should always be the case when equals is overridden.
The hash code of a Set is the sum of the hash codes of its elements.


</details>

### `Set` direct implementations
<details><summary>Show answer</summary>


- [`HashSet`](#hashset)
- [`CopyOnWriteArraySet`](#copyonwritearrayset-its-operations-compare-with-hashset)
- [`EnumSet`](#enumset)


</details>

### `HashSet`
<details><summary>Show answer</summary>


`HashSet` - the most commonly used, implemented by a _hash table_,
an array in which elements are stored at a position derived from their contents

`HashSet` is unsychronized and not thread-safe; its iterators are fail-fast.


</details>

### Hash table data structure
<details><summary>Show answer</summary>


An element’s position in a hash table is calculated by a hash function of its contents.
Hash functions are designed to give, as far as possible, an even spread of results (hash codes)
from the element values that might be stored.

Unless your table has more locations than there are values that might be stored in it,
sometimes two distinct values will hash to the same location in the hash table (this is called a _collision_).
We can minimize the problem with a good hash function - one that spreads the elements out equally in the table - but,
when collisions do occur, we need to have a way of keeping the colliding elements
at the same table location, or _bucket_.
This is often done by storing them in a linked structure - a list or a tree:

<img src="../../docs/images/Hash_Table.png" alt="A hash table with chained overflow" width="600">


</details>

### `HashSet` advantages and disadvantages
<details><summary>Show answer</summary>


Advantages: the constant-time performance (for lightly loaded tables and with a good hash function)
of the basic operations of `add`, `remove`, `contains`, and `size`

Its main performance disadvantages are:
- the poor performance of heavily loaded tables and
- the iteration performance: iterating through the table involves examining every bucket,
  so the cost includes a factor attributable to the table length, regardless of the size of the set it contains.


</details>

### Creation of `HashSet`
<details><summary>Show answer</summary>


Additional `HashSet` constructors to no-arguments constructors:
- `HashSet(int initialCapacity)`
- `HashSet(int initialCapacity, float loadFactor)`

Both of these constructors create an empty set but allow some control over the size of the underlying table,
creating one at least as large as the supplied capacity and, optionally, with the desired load factor.

You can use these constructors to create a table large enough to store all the elements you expect it to hold
without requiring expensive resizing operations.

In practice, however, they have proved confusing and difficult to use - confusing because their int parameter,
often misunderstood to be the expected number of entries,
is in fact used to compute the table size, and difficult to use because
computing the argument correctly from the expected maximum number of entries
is implementation-dependent and error-prone.

So Java 19 added static factory methods, which take only a parameter signifying the expected maximum number of entries.
These methods are recommended as being easier to use and less subject to implementation changes.
The factory method for `HashSet` is `newHashSet`:

```java
public class HashSet<E> implements Set<E> {
  static <T> HashSet<T> newHashSet(int numElements) {
    
  }
}
```


</details>

### `EnumSet`
<details><summary>Show answer</summary>


`EnumSet` should always be preferred over any other Set implementation when we are storing enum values.

This class exists to take advantage of the efficient implementations that are possible when:
- the maximum number of possible elements is fixed and
- a unique index can be assigned to each

These two conditions hold for a set of elements of the same Enum class;
the number of keys is fixed by the constants of the enumerated type,
and the `ordinal` method returns values that are guaranteed to be unique to each constant.
In addition, the values that `ordinal` returns form a compact range, starting from zero -
ideal, in fact, for use as array indices or, in the standard implementation, indices of a bit vector.



</details>

### `UnmodifiableSet`, how to create? Its properties, pros and cons
<details><summary>Show answer</summary>


You won’t find any reference to the name `UnmodifiableSet<E>` in the Javadoc or
in the code of the Collections Framework.
It’s a name invented for a family of package-private classes that client programmers can never access by name,
but that are important because they provide the implementation of the unmodifiable sets obtained
from the various overloads of the factory methods `Set.of` and `Set.copyOf`.

The properties of the members of this family are described in the Javadoc for Set:
- They are unmodifiable: elements cannot be added or removed.
  Calling any mutator method will always cause UnsupportedOperationException to be thrown.
- They are null-hostile. Attempts to create them with null elements result in `NullPointerException`.
- They reject duplicate elements at creation time.
  Duplicate elements passed to a factory method result in an `IllegalArgumentException`.

Advantages:
- The classes that make up `UnmodifiableSet` use fixed-length arrays as the backing structures.
  Without the overhead of empty table buckets or linked overflow structures,
  these implementations _require much less space_ than a hashed structure.
- Iteration is also correspondingly more efficient, with the added benefit of improved spatial locality

Disadvantage:
The trade-off for faster iteration is that containment can only be determined by a linear search, O(N) in complexity.


</details>

### Set Views of Maps, main purpose
<details><summary>Show answer</summary>


In the Collections Framework, many sets are implemented as wrappers around a corresponding map,
although the maps are encapsulated and invisible to the client.
However, the converse does not hold: some maps do not have a corresponding set:
`WeakHashMap`, `IdentityHashMap`, and `ConcurrentHashMap`.

To obtain a set for one of these (weak references, identity-equality based, concurrency) with the same
ordering, concurrency, and performance characteristics as the backing map,
you can call the method `Collections::newSetFromMap` on an empty map with a type `Map<E,Boolean>`,
where `E` is the element type of the set that you want to create.

For example, to create a concurrent set of `Integer`, you could write:

```java
Set<Integer> concurrentIntegerSet = Collections.newSetFromMap(new ConcurrentHashMap<Integer,Boolean>());
```

This idiom guarantees that no direct access to the backing map can take place after the set view is created,
as required by the specification of `newSetFromMap`.


</details>

### How to create a set of items with identity relation?
<details><summary>Show answer</summary>

```java
Set<Integer> concurrentIntegerSet = Collections.newSetFromMap(new IdentityHashMap<Integer,Boolean>());
```


</details>

### `SequencedSet`
<details><summary>Show answer</summary>


A SequencedSet is an externally or internally ordered Set that also exposes the methods of SequencedCollection.
It combines the methods of these two interfaces, adding to them in only one respect:
it provides a covariant override of the method `reversed` of `SequencedCollection`
in order to return a value of type `SequencedSet`

<img src="../../docs/images/SequencedSet_Hierarchy.png" alt="SequencedSet and related types" width="600">


</details>

### `SequencedSet` direct implementation
<details><summary>Show answer</summary>


`LinkedHashSet`


</details>

### `LinkedHashSet` idea, compare with `HashSet`
<details><summary>Show answer</summary>


`LinkedHashSet` implements `SequencedSet` by maintaining a linked list of its elements.
The iterators of a `LinkedHashSet` return their elements in insertion order.


The linked structure also has a useful consequence in terms of improved performance for iteration:
next executes in constant time, as the linked list can be used to visit each element in turn.
This is in contrast to `HashSet`, for which every bucket in the hash table must be visited,
whether it is occupied or not.

This class is unsychronized and not thread-safe; its iterators are fail-fast.


</details>

### `NavigableSet`
<details><summary>Show answer</summary>


The interface `NavigableSet` adds to the `SequencedSet` contract a guarantee that its iterator will
traverse the set in ascending element order, and adds further methods to find the elements adjacent to a target value.
Unlike `LinkedHashSet`, its elements are ordered internally by
the comparison method of its natural order or of its comparator.


</details>

### What set interface was before `NavigableSet` has been introduced? Recommendations
<details><summary>Show answer</summary>


Prior to the introduction of `NavigableSet`, the only subinterface of `Set` was an interface called `SortedSet`,
which guarantees iteration order but does not expose the closest-match methods.
`SortedSet` is still in the JDK - it extends `SequencedSet` and is in turn extended by `NavigableSet` -
but it is no longer of any great interest, since it has no direct implementations in the platform.


</details>

### Using `NavigableSet` as a queue, available approaches, compare them
<details><summary>Show answer</summary>


#### `NavigableSet` vs `Deque`
`E pollFirst()` and `E pollLast()` - are analogous to the methods of the same names in `Deque`
and help to support the use of NavigableSet in applications that require queue functionality.

#### `NavigableSet` vs `PriorityQueue`
- if it needs to examine and manipulate the set of waiting tasks, use `NavigableSet`
  (and uniqueness via `equal` - todo, not sure if it is a right statement);
- if its main requirement is efficient access to the next task to be performed,
  use `PriorityQueue` (accommodates duplicates).


</details>

### `NavigableSet`, how could its elements viewed?
<details><summary>Show answer</summary>


When you have to work with an ordered set of values, a useful way to view them is as a range.
For example, given a set of timestamped events, you might like to inspect all those
that happened within a certain time period.
In the case of PriorityTasks, we might want to process all those that fall within a range of priorities -
high and medium, say.

Changes in the view - including structural changes - are reflected in the underlying set.


</details>

### `SequencedSet` vs `NavigableSet`
<details><summary>Show answer</summary>


A SequencedSet is an externally or internally ordered Set that also exposes the methods of SequencedCollection.
A NavigableSet is an internally ordered SequencedSet that therefore also automatically sorts its elements,
and provides additional methods to find elements adjacent to a target value.


</details>

### Implementations of `NavigableSet`
<details><summary>Show answer</summary>


- [`java.util.TreeSet`](#treeset)
- [`ConcurrentSkipListSet`](#concurrentskiplistset)


</details>

### `TreeSet`
<details><summary>Show answer</summary>


Trees are the data structure you would choose for an application that needs
fast insertion and retrieval of individual elements,
but which also requires that elements can be retrieved in sorted order.

TreeSet is unsychronized and not thread-safe; its iterators are fail-fast.


</details>

### What data structure is backed by `TreeSet`? Its properties
<details><summary>Show answer</summary>


It is backed by a tree.

A tree is a branching structure that represents hierarchy.
An important class of tree often used in computing is a binary tree -
one in which each node can have at most two children.

TODO binary tree property


</details>
