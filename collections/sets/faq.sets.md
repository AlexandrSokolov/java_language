- [Sets](#sets)
- [When two sets are equal?](#when-two-sets-are-equal)
- [What does define a duplicate in sets?](#what-does-define-a-duplicate-in-sets)
- [What are consequences of using different equivalence relations?](#what-are-consequences-of-using-different-equivalence-relations)
- [Containing by set multiple objects, that return true when compared to each other by `equals`?](#containing-by-set-multiple-objects-that-return-true-when-compared-to-each-other-by-equals)
- [`Set` direct implementations](#set-direct-implementations)
- [`HashSet`](#hashset)
- [Elements position in a hash table](#elements-position-in-a-hash-table)
- [Which operations and what complexity do hash tables have?](#elements-position-in-a-hash-table)
- [SequencedSet and NavigableSet](#sequencedset-and-navigableset)

### Sets

A _set_ is a collection of items that cannot contain duplicates; 
adding an item that is already present in the set has no effect.

### When two sets are equal?

The `equals` method is overridden: 
the `Set` contract states that a `Set` can only ever be equal to another `Set`, 
and then only if:
- they are the same size and 
- contain equal elements. 

The `hashCode` method is also overridden, as should always be the case when equals is overridden. 
The hash code of a Set is the sum of the hash codes of its elements.

### What does define a duplicate in sets?

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

#### Objects equality with _ordering relation_

`NavigableSet` - maintains its elements in sorted order using an ordering relation 
provided by either its natural order or a `Comparator`.

It defines two objects as equivalent if, using it, they compare as equal - 
that is, if the comparison method returns 0 - regardless of whether they satisfy the equality relation.

### What are consequences of using different equivalence relations?

- sets may contain duplicate elements that satisfy `equals` or, conversely, 
  that they may elide occurrences of ones that don’t.
- to determine whether two sets A and B are equal, 
  A must test each member of B to discover whether it is equivalent to a member of A. 
  If the roles are reversed, and if A and B are using different equivalence relations, 
  the results may be different, so set equality loses symmetry.

### Containing by set multiple objects, that return true when compared to each other by `equals`?

It is possible for `NavigableSet` that uses _ordering relation_ to check equality.

### `Set` direct implementations

- [`HashSet`](#hashset)
- [`CopyOnWriteArraySet`](#copyonwritearrayset)

### `HashSet`

`HashSet` - the most commonly used, implemented by a _hash table_,
an array in which elements are stored at a position derived from their contents

`HashSet` is unsychronized and not thread-safe; its iterators are fail-fast.

### Elements position in a hash table

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

### Which operations and what complexity do hash tables have?

As long as there are no _collisions_, the cost of inserting or retrieving an element is **constant**.

### The probability of a collision

As the hash table fills, collisions become more likely; 
assuming a good hash function, the probability of a collision in a lightly loaded table is proportional to its load, 
defined as the number of elements in the table divided by its capacity (the number of _buckets_). 

### How it works if a collision does take place

If a collision does take place, an overflow structure - a linked list or tree - has to be created 
and subsequently traversed, adding an extra cost to insertion.

The overflow structure used is normally a linear list, 
but if for a given bucket the list grows in length past a certain threshold, 
it is converted to a red-black binary tree.

If the size of the hash table is fixed, performance will worsen as more elements are added and the load increases. 
To prevent this from happening, the table size is increased by rehashing - 
copying all elements to a newly allocated, larger table - when the load reaches a specified threshold (its load factor).

### Iterating over a hash table

Iterating over a hash table requires each bucket to be examined to see whether it is occupied and 
therefore requires an execution count proportional to the capacity of the hash table 
plus the number of elements it contains. 

Since the iterator examines each bucket in turn, the order in which elements are returned depends on their hash codes, 
so there is no guarantee as to the order in which the elements will be returned.

`LinkedHashSet`, a variant of `HashSet` implementation with an iterator that 
instead returns elements in their insertion order.

### `HashSet` advantages and disadvantages

Advantages: the constant-time performance (for lightly loaded tables and with a good hash function) 
of the basic operations of `add`, `remove`, `contains`, and `size`

Its main performance disadvantages are: 
- the poor performance of heavily loaded tables and 
- the iteration performance: iterating through the table involves examining every bucket, 
  so the cost includes a factor attributable to the table length, regardless of the size of the set it contains.

### `HashSet` constructors, when should you use them and why?

Additional `HashSet` constructors: 
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
static <T> HashSet<T> newHashSet(int numElements);
```

### `CopyOnWriteArraySet`



### SequencedSet and NavigableSet

A SequencedSet is an externally or internally ordered Set that also exposes the methods of SequencedCollection. 
A NavigableSet is an internally ordered SequencedSet that therefore also automatically sorts its elements, 
and provides additional methods to find elements adjacent to a target value.