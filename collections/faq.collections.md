
- [Java Collections Framework](#java-collections-framework)
- [The Main Interfaces of the Java Collections Framework](#the-main-interfaces-of-the-java-collections-framework)
- [Sequenced Collections, the purpose](#sequenced-collections)
- [Iteration via collection of elements](#iteration-via-collection-of-elements)
- [Iterate over a collection and consume its elements one-by-one](#iterate-over-a-collection-and-consume-its-elements-one-by-one)
- [What is used in `for` loops?](#what-is-used-in-for-loops)
- [When `for` loop via explicit use of an iterator is necessary?](#when-for-loop-via-explicit-use-of-an-iterator-is-necessary-)
- [What structural changes you could apply when iterate via `for`](#what-structural-changes-you-could-apply-when-iterate-via-for)
- [Alternative way to applying structural changes, requirement](#alternative-way-to-applying-structural-changes-requirement)
- [`java.util.Collection`](#javautilcollection)
- [`java.util.SequencedCollection`](#sequencedcollection)
- [There are several ways of implementing each of these interfaces. Why doesn’t the framework just use the best implementation for each interface?](#there-are-several-ways-of-implementing-each-of-these-interfaces-why-doesnt-the-framework-just-use-the-best-implementation-for-each-interface)
- [What is used to choose the right implementation?](#what-is-used-to-choose-the-right-implementation)
- [The main kinds of operations that most collection interfaces require](#the-main-kinds-of-operations-that-most-collection-interfaces-require)
- [Data structures used as the basis of the implementations](#data-structures-used-as-the-basis-of-the-implementations)
- [Arrays vs Linked Lists](#arrays-vs-linked-lists)
- [Arrays/Linked Lists vs Hash tables](#arrayslinked-lists-vs-hash-tables)
- [What is used internally for implementations based on hash tables, requirements](#what-is-used-internally-for-implementations-based-on-hash-tables-requirements)
- [Comment the following code](#comment-the-following-code)

### Java Collections Framework

A _collection_ is an object that provides access to a group of objects, allowing them to be processed in a uniform way.
A _collections framework_ provides a uniform view of a set of collection types 
specifying and implementing common data structures, following consistent design rules so that they can work together.

### The Main Interfaces of the Java Collections Framework

- [`java.lang.Iterable` - in order to be used for an _enhanced for statement_, usually called a _foreach_ statement.](#iteration-via-collection-of-elements)
- [`java.util.Collection` - the core functionality required of any collection other than a Map](#javautilcollection)
- [`java.util.Set` - order is not significant and there can be no duplicates]()
- [`java.util.List` - order is significant and accommodates duplicate elements]()
- [`java.util.Queue` - holds elements for processing, yielding them up in the order in which they are to be processed](queues/faq.queues.md#what-is-specific-about-queues-among-other-java-collections)

- [`java.util.Map` - key-value entries to store and retrieve elements.]()

<img src="../docs/images/Main_Collection_Interfaces.png" alt="Main Interfaces" width="600">

### Sequenced Collections

These sequenced collections differ from `Collection`, `Set`, or `Map` in that they have a defined _order_, 
called in the documentation an _encounter order_.

They differ from Queue, which also has a defined _order_, in that they can be iterated in either direction. 
The ordering of sequenced collections can be derived in two different ways: 
1. for some, like `List`, elements retain the order in which they were added (sometimes called externally ordered types), 
2. whereas for others, like `NavigableSet` (see [`SequencedSet` and `NavigableSet`](sets/faq.sets.md#sequencedset-and-navigableset)), 
   the ordering is dictated by the values of the elements (also known as internally ordered types).  

Externally ordered and internally ordered terms reflect the difference between 
an order that is arbitrarily imposed on the elements, for example by the order in which they are added,
and an order that is an inherent property of the elements themselves, such as alphabetic ordering on strings.

- [`SequencedCollection`](#sequenced-collections)
- [`SequencedSet` and `NavigableSet`](sets/faq.sets.md#sequencedset-and-navigableset)
- [`Deque`](queues/faq.queues.md#deque)
- [`SequencedMap`](maps/faq.maps.md#sequencedmap)
- [`NavigableMap`](maps/faq.maps.md#navigablemap)

<img src="../docs/images/Sequenced_Collections.png" alt="Main Interfaces" width="600">

### Iteration via collection of elements

This ability to iterate over the collection of elements is provided by `java.lang.Iterable` interface.

It exposes three methods:
- `void forEach(Consumer<? super T> action)` - Performs action for each element of the Iterable
- `Iterator<T> iterator()` - Returns an iterator over elements of type T
- `Spliterator<T> spliterator()` - Creates a Spliterator over the elements described by this Iterable
  (for parallel streams)

### Iterate over a collection and consume its elements one-by-one

Using `java.lang.Iterable.forEach()` - method, common for all the collections:
```java
coll.forEach(System.out::println);
```

### What is used in `for` loops?

Using `Iterator`:
1. Via explicit use of an iterator:
    ```java
    for (Iterator<String> itr = coll.iterator() ; itr.hasNext() ; ) {
      System.out.println(itr.next());
    }
    ```
2. The previous iteration way is awkward compared to the `foreach` statement, which uses `Iterator` internally:
    ```java
    for (String s : coll) {
      System.out.println(s);
    }
    ```
   The target of a foreach statement can be an array or any class that implements the interface `Iterable`. 
   Since the `Collection` interface extends Iterable, any set, list, or queue can be the target of foreach.
   
### When `for` loop via explicit use of an iterator is necessary? 

It is necessary when you want to make a structural change to a collection-broadly speaking, 
adding or removing elements-in the course of iteration.

### What structural changes you could apply when iterate via `for`

`Iterator` exposes only a method for removal of collection elements, 
but its subinterface `ListIterator`, available to `List` implementations, 
also provides methods to add and replace elements.

### Alternative way to applying structural changes, requirement

If memory constraints don’t prevent you from making a new copy of the list, 
streams offer a neater solution to this problem:
```java
List<String> strings = new ArrayList<>(List.of("alpha", "bravo", "charlie"));
List<String> modifiedStrings = strings.stream()
  .filter(s -> s.contains("r"))
  .toList();
assert modifiedStrings.equals(List.of("bravo", "charlie"));
```

### `java.util.Collection`

Collection, which exposes the core functionality required of any collection other than a Map. 
Its methods support managing elements by:
- adding or removing single or multiple elements, 
- checking membership of a single or multiple values, and 
- inspecting and 
- exporting elements.

### SequencedCollection

The SequencedCollection interface provides versions of these 
that can be applied to the first and last element of the collection: 
`addFirst`, `addLast`, `removeFirst`, `removeLast`, `getFirst`, and `getLast`. 

In addition, SequencedCollection provides a reversed view - that is, a way of working with the collection 
as though the ordering has been reversed. 

This simplifies many programming problems and often provides more efficient implementations.

### There are several ways of implementing each of these interfaces. Why doesn’t the framework just use the best implementation for each interface?

That would certainly make life simpler.
If an implementation is a greyhound for some operations, Murphy’s law tells us that it will be a tortoise for others.
Because there is no “best” implementation of any of the interfaces **for every situation**.

### What is used to choose the right implementation?

You always have to make a trade-off, 
judging which operations are used most frequently in your application and 
choosing the implementation that optimizes those operations.

### The main kinds of operations that most collection interfaces require

The three main kinds of operations that most collection interfaces require:
- insertion and removal of elements by position
- retrieval of elements by content
- iteration over the collection elements

The various implementations provide many variations on these operations, 
but the main differences among them can be discussed in terms of how they carry out these three.

### Data structures used as the basis of the implementations

#### Arrays

Because arrays are implemented directly in hardware, they have the properties of random-access memory: 
very fast for accessing elements by position and for iterating over them, 
but slower for inserting and removing elements at arbitrary positions 
(because that may require adjusting the position of other elements). 

Arrays are used in the Collections Framework as the backing structure for:
- ArrayList
- CopyOnWriteArrayList
- EnumSet
- EnumMap and 
- for many of the Queue and Deque implementations. 

They also form an important part of the mechanism for implementing hash tables.

#### Linear linked lists

These consist of chains of linked cells. 
Each cell contains a reference to data and a reference to the next cell in the list 
(and, in some implementations, the previous cell). 
Linked lists perform quite differently from arrays: 
accessing elements by position is slow, 
because you have to follow the reference chain from the start of the list, 
but insertion and removal operations can be performed in constant time by rearranging the cell references. 

Linked lists are the primary backing structure used for the classes:
- ConcurrentLinkedQueue
- LinkedBlockingQueue
- LinkedList

#### Other (nonlinear) linked data structures

Linked structures are particularly suitable for representing nonlinear types like trees and skip lists 
(see “ConcurrentSkipListSet”), especially if they need to be rearranged as new elements are added. 
Such structures provide an inexpensive way of maintaining sorted order in their data, 
allowing fast searching by content. 

- Trees are the backing structures for `TreeSet` and `TreeMap`. 
- Skip lists are used in `ConcurrentSkipListSet` and `ConcurrentSkipListMap`.

#### Hash tables

These provide a way of storing elements indexed on their content rather than on an integer-valued index, as with lists. 
In contrast to arrays and linked lists, hash tables provide no support for accessing elements by position, 
but access by content is usually very fast, as are insertion and removal. 

Hash tables are the backing structure for many Set and Map implementations, including:
- `HashSet`
- `LinkedHashSet`
- `HashMap`
- `LinkedHashMap`
- `WeakHashMap`
- `IdentityHashMap`
- `ConcurrentHashMap`

### Arrays vs Linked Lists

Arrays are very fast for accessing elements by position and for iterating over them,
but slower for inserting and removing elements at arbitrary positions
(because that may require adjusting the position of other elements).

Linked lists - accessing elements by position is slow,
because you have to follow the reference chain from the start of the list,
but insertion and removal operations can be performed in constant time by rearranging the cell references.

### Arrays/Linked Lists vs Hash tables

In contrast to arrays and linked lists, hash tables provide no support for accessing elements by position,
but access by content is usually very fast, as are insertion and removal.

### What is used internally for implementations based on hash tables, requirements

The content-based indexing of hashed collections depends on two Object methods, `hashCode` and `equals`, 
which are applied in succession to position an element for insertion or to locate it for retrieval. 
The requirements on the relationship between these methods are defined in the contract for `hashCode`. 

The crucial requirement, sometimes overlooked - with disastrous consequences - by Java programmers, 
is that if two objects are equal according to the `equals` method, 
then the result of calling `hashCode` on each must be the same. 

If `hashCode` depends on an instance field - or any other data about an object - that is not used by the `equals` method, 
then the first stage of a retrieval operation will very likely be misdirected. 

One way this can occur is if you do not override `Object::hashCode` at all; 
the value that it returns in this case will be implementation-dependent (in OpenJDK, it is usually randomly generated) 
but is in any case highly unlikely to be the same for two different instances.

### Respect the ‘Ownership’ of Collections

todo

###

### Comment the following code

#### structural changes during iteration
```java
List<String> strings = new ArrayList<>(List.of("alpha", "bravo", "charlie"));
for (String s : strings) {
  if (! s.contains("r")) {
    strings.remove(s);      
  }
}
```
You get `ConcurrentModificationException`:
```java
strings.remove(s);      // throws ConcurrentModificationException
```
Solution:
```java
List<String> strings = new ArrayList<>(List.of("alpha", "bravo", "charlie"));
for (Iterator<String> itr = strings.iterator() ; itr.hasNext() ; ) {
  String s = itr.next();
  if (! s.contains("r")) {
    itr.remove();
  }
}
```
If memory constraints don’t prevent you from making a new copy of the list,
streams offer a neater solution to this problem:
```java
List<String> strings = new ArrayList<>(List.of("alpha", "bravo", "charlie"));
List<String> modifiedStrings = strings.stream()
  .filter(s -> s.contains("r"))
  .toList();
assert modifiedStrings.equals(List.of("bravo", "charlie"));
```

#### Using implementations based on hash tables

```java
class Person {
  private final String name;
  public Person(String name) {
    this.name = name;
  }
  @Override
  public boolean equals(Object o) {
    return o instanceof Person p && name.equals(p.name);
  }
}
Set<Person> people = new HashSet<>();
people.add(new Person("Alice"));
if (people.contains(new Person("Alice"))) {
  //do something  
}
```
This class should override `hashCode` so that it depends only on the same field that equals depends on (i.e., name). 
But it does not; as a result, hashed collections will not work correctly.


Solution - implement `hashCode()`:
```java
class Person {
  private final String name;
  public Person(String name) {
    this.name = name;
  }
  @Override
  public boolean equals(Object o) {
    return o instanceof Person p && name.equals(p.name);
  }
  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
```

### fds