
### `java.util.Collections`, its purpose, methods groups

The class `java.util.Collections` consists entirely of static methods that operate on or return collections. 
There are three main categories: 
- [generic algorithms](#generic-algorithms)
- [methods that return empty or prepopulated collections](#collection-factories)
- [methods that create wrappers](#wrappers)

All the methods of Collections are public and static.

### Generic Algorithms
- [Changing the order of list elements](#changing-the-order-of-list-elements)
- [Changing the contents of a list](#changing-the-contents-of-a-list)
- [Finding extreme values in a collection](#finding-extreme-values-in-a-collection)
- [Finding specific values in a list](#finding-specific-values-in-a-list)

### What does affect the choice of algorithm used by the methods that act on Lists?

The choice of algorithm used by the methods that act on Lists often 
depends on whether the `List` being processed implements the marker interface `RandomAccess`. 
Classes implement this interface to indicate to generic methods that a long list of that class 
is more efficiently processed using `get` than by using an `iterator`. 
`ArrayList` implements `RandomAccess`; `LinkedList` does not.

### Changing the Order of List Elements
- `void reverse(List<?> list)` reverse the order of the elements
- `void rotate(List<?> list, int distance)` rotate the elements of the list; 
  the element at index i is moved to index (distance + i) % list.size()
- `void shuffle(List<?> list)` randomly permute the list elements
- `void shuffle(List<?> list, Random rnd)` randomly permute the list using the randomness source rnd
- `void shuffle(List<?> list, RandomGenerator rndGen)` randomly permute the list using the randomness generator rndGen
- `<T extends Comparable<? super T>> void sort(List<T> list)` sort the supplied list using natural ordering
- `<T> void sort(List<T> list, Comparator<? super T> c)` sort the supplied list using the supplied ordering
- `void swap(List<?> list, int i, int j)` swap the elements at the specified positions

### Changing the Order of List Elements, performance
- `swap` - exchanges two elements and, in the case of a `List` that implements `RandomAccess`, executes in constant time. 
- `sort` - transfers the list elements into an array, sorts them in a worst-case time of `O(N log N)`, 
  and then returns them to the list.
- All the remaining methods execute in time `O(N)`.

### Algorithms used in methods that change the order of List elements

For each of these methods (except `sort` and `swap`), there are two algorithms:
- one using `ListIterator` and 
- the other using `get` and `set` (random access)

#### `Collections::sort` algorithm

The method `sort` delegates to `List::sort`, which in the default implementation transfers the list elements to an array, 
although `ArrayList` overrides this to `sort` in place. 
The array is then _sorted_ using - the [timsort algorithm](todo), with a worst-case time of `N log N`.

#### `Collections::swap` algorithm

The method `swap` always uses random access.

#### other methods algorithm
The standard implementations for the other methods use either iteration or random access, 
depending on whether the list implements the `RandomAccess` interface.

The random-access algorithm is used when:
- list implementations implement `RandomAccess`
- always - even for a list that does not implement `RandomAccess` - 
  if the list size is below a given threshold determined on a per-method basis by performance testing.

### Changing the Contents of a List
- `<T> void copy(List<? super T> dest, List<? extends T> src)` copy all the elements from one list into another
- `<T> void fill(List<? super T> list, T obj)` replace every element of list with obj
- `<T> boolean replaceAll(List<T> list, T oldVal, T newVal)` replace all occurrences of oldVal in list with newVal

### Explain `Collections::copy` signature

`<T> void copy(List<? super T> dest, List<? extends T> src)` signature can be explained using 
[_the Get and Put Principle_](../generics/faq.generics.md#the-get-and-put-principle).
The method gets values out of the source `src`, so it is declared with an `extends` wildcard,
and it puts values into the destination `dst`, so it is declared with a `super` wildcard.
So the types of these lists are, respectively, `? extends T` and `? super T`. 

### Explain `Collections::fill` signature

`<T> void fill(List<? super T> list, T obj)` signature can be explained using
[_the Get and Put Principle_](../generics/faq.generics.md#the-get-and-put-principle).
This principle dictates that you should use `super` if you are putting values into a parameterized collection.

### Explain `Collections::replaceAll` signature

`<T> boolean replaceAll(List<T> list, T oldVal, T newVal)` signature can be explained using
[_the Get and Put Principle_](../generics/faq.generics.md#the-get-and-put-principle).
It states that if you are putting values into and getting values out of the same structure,
you should not use wildcards at all.

### Limitations of `Collections::copy` and `Collections::fill` methods, an alternative

These methods are not often used in practice. 
`fill` and `copy` rely on the destination list already existing with the right number of elements. 
If you need to create a new list to receive the contents, a convenient alternative for copy is:

```java
var dest = new ArrayList<>(src);
```
and for fill you can write:
```java
var dest = new ArrayList<>(Collections.nCopies(N, obj));
```

### `Collections::replaceAll` alternative

The method `replaceAll` has largely been supplanted by the default method `List::replaceAll`.

### Finding Extreme Values in a Collection
- `<T extends Object & Comparable<? super T>> T max(Collection<? extends T> coll)`
  return the maximum element using natural ordering
- `<T> T max(Collection<? extends T> coll, Comparator<? super T> comp)`
  return the maximum element using the supplied comparator
- `<T extends Object & Comparable<? super T>> min(Collection<? extends T> coll)`
  return the minimum element using natural ordering
- `<T> T min(Collection<? extends T> coll, Comparator<? super T> comp)`
  return the minimum element using the supplied comparator

### Finding Specific Values in a List

- `<T> int binarySearch(List<? extends Comparable<? super T>> list, T key)` search for key using binary search
- `<T> int binarySearch(List<? extends T> list, T key, Comparator<? super T> c)` search for key using binary search
- `int indexOfSubList(List<?> source, List<?> target)` find the first sublist of source that matches target
- `int lastIndexOfSubList(List<?> source, List<?> target)` find the last sublist of source that matches target

### `Collections::binarySearch` methods, their signatures

- `<T> int binarySearch(List<? extends Comparable<? super T>> list, T key)` 
  The signature of this `binarySearch` overload says that you can use it to search for a key of type `T` 
  in a list of objects that can have any type that can be compared with objects of type `T`.  
- `<T> int binarySearch(List<? extends T> list, T key, Comparator<? super T> c)`
  The second signature is like the Comparator overloads of `min` and `max`, 
  except that in this case the type parameter of the Collection must be a subtype of the type of the key, 
  which in turn must be a subtype of the type parameter of the Comparator.

### Binary search algorithm

Binary search requires a sorted list for its operation. 
At the start of a search, the range of indices in which the search value may occur corresponds to the entire list. 
The binary search algorithm samples an element in the middle of this range, 
using the value of the sampled element to determine whether the new range should be the part of the old range above 
or the part below the index of the element. 
A third possibility is that the sampled value is equal to the search value, in which case the search is complete. 
Since each step halves the size of the range, 
m steps are required to find a search value in a list of length 2m,
and the time complexity for a RandomAccess list of length N is O(log N). 
If the list does not implement RandomAccess, binarySearch uses iteration, with linear complexity.

### `Collections::indexOfSubList` and `Collections::lastIndexOfSubList` signatures

- `int indexOfSubList(List<?> source, List<?> target)` find the first sublist of source that matches target
- `int lastIndexOfSubList(List<?> source, List<?> target)` find the last sublist of source that matches target

Their signatures allow the source and target lists to contain elements of any type 
(remember that the two wildcards may stand for two different types). 
The design decision behind these signatures is the same as that 
behind the `Collection` methods `containsAll`, `retainAll`, and `removeAll` 
(see ["Bounded or Unbounded?"](../generics/faq.generics.md#bounded-or-unbounded)).

### Collection Factories
Empty views:
- `<T> List<T> emptyList()` return an empty List
- `<K,V> Map<K,V> emptyMap()` return an empty Map
- `<T> Set<T> emptySet()` return an empty Set
- `<T> Iterator<T> emptyIterator()` return an empty Iterator
- `<T> ListIterator<T> emptyListIterator()` return an empty ListIterator
- `<T> NavigableSet<T> emptyNavigableSet()` return an empty NavigableSet
- `<K,V> NavigableMap<K,V> emptyNavigableMap()` return an empty NavigableMap
Singleton views:
- `<T> Set<T> singleton(T o)` return an immutable set containing only the specified object
- `<T> List<T> singletonList(T o)` return an immutable list containing only the specified object
- `<K,V> Map<K,V> singletonMap(K key, V value)` return an immutable map, mapping only the key K to the value V
View containing a number of copies of a given object:
- `<T> List<T> nCopies(int n, T o)` return an immutable list containing n references to the object o

### Empty views issue

The methods `emptyList`, `emptyMap`, and `emptySet` have themselves become less useful 
as their functions have been duplicated by the unmodifiable factory method parameterless 
overloads of `Set::of`, `List::of`, and `Map::of`.

### Singleton views issue

They can be useful in providing a single input value to a method that is written to accept a Collection of values, 
and again, they have been duplicated by the unmodifiable factory methods.

### Synchronized Collections Wrappers, related issue

- `<T> Collection<T> synchronizedCollection(Collection<T> c)`
- `<T> Set<T> synchronizedSet(Set<T> s)`
- `<T> List<T> synchronizedList(List<T> list)`
- `<K,V> Map<K,V> synchronizedMap(Map<K,V> m)`
- `<T> NavigableSet<T> synchronizedNavigableSet(NavigableSet<T> s)`
- `<K,V> NavigableMap<K,V> synchronizedNavigableMap(NavigableMap<K,V> m)`

The thread safety offered by these wrappers depends on no unsynchronized access being made to the underlying collection. 
A common usage pattern is to wrap a collection at construction time 
without retaining any reference to the wrapped collection:
```java
var synchList = Collections.synchronizedList(new ArrayList<>( ... ));
```
The classes that provide these synchronized views are conditionally thread-safe; 
although each of their operations is guaranteed to be atomic, 
you may need to synchronize multiple method calls on the wrapper object itself in order to obtain consistent behavior. 

In particular, iterators must be created and used entirely within a code block synchronized on the collection; 
otherwise, the result will at best be failure with `ConcurrentModificationException`. 
This is very coarse-grained synchronization; 
if your application makes heavy use of synchronized collections, 
its effective concurrency will be greatly reduced. 
In this situation, you should turn to the appropriate concurrent collection.

### Unmodifiable Collections Wrappers, purpose
- `<T> Collection<T> unmodifiableCollection(Collection<? extends T> c)`
- `<T> Set<T> unmodifiableSet(Set<? extends T> s)`
- `<T> List<T> unmodifiableList(List<? extends T> list)` 
- `<K,V> Map<K,V> unmodifiableMap(Map<? extends K, ? extends V> m)` 
- `<T> NavigableSet<T> unmodifiableNavigableSet(NavigableSet<? extends T> s)`
- `<K,V> NavigableMap<K,V> unmodifiableNavigableMap(NavigableMap<K,? extends V> m)`
- `<T> SequencedCollection<T> unmodifiableSequencedCollection(SequencedCollection<? extends T> c)`
- `<K,V> SequencedMap<K,V> unmodifiableSequencedMap(SequencedMap<? extends K,? extends V> m)`
- `<T> SequencedSet<T> unmodifiableSequencedSet(SequencedSet<? extends T> s)`

An unmodifiable collection will throw `UnsupportedOperationException` 
in response to any attempt to change its structure or the elements that compose it. 

### Limitation of unmodifiable collections wrappers, solution

Used with care, this can be useful when you want to allow clients read access to an internal data structure; 
passing the structure in an unmodifiable wrapper will prevent a client from changing it, 
**but it will not prevent the client from changing the objects it contains, if they are modifiable.** 


Often, you will have **to protect your internal data structure** by providing clients instead 
**with a defensive copy** made for that purpose, or by placing these objects in unmodifiable wrappers.

### Checked Collections Wrappers, purpose

The following methods each create an object containing an instance of the appropriate interface, 
together with a type token for the element. 
All operations are delegated to the instance, but before a new element is added to the collection, 
it is first tested against the type token, and a `ClassCastException` is thrown if it does not match:
- `<E> Collection<E> checkedCollection(Collection<E> c, Class<E> elementType)`
- `<E> List<E> checkedList(List<E> c, Class<E> elementType)`
- `<E> Set<E> checkedSet(Set<E> c, Class<E> elementType)`
- `<E> NavigableSet<E> checkedNavigableSet(NavigableSet<E> c, Class<E> elementType)`
- `<K,V> Map<K,V> checkedMap(Map<K,V> c, Class<K> keyType, Class<V> valueType)`
- `<K,V> NavigableMap<K,V> checkedNavigableMap(NavigableMap<K,V> c, Class<K> keyType, Class<V> valueType)`
- `<E> Queue<E> checkedQueue(Queue<E> c, Class<E> elementType)`

Unchecked warnings from the compiler are a signal to take special care to avoid runtime type violations. 
For example, after we have passed a typed collection reference to an ungenerified library method, 
we can’t be sure that it has added only correctly typed elements to the collection. 
Instead of losing confidence in the collection’s type safety, we can pass in a checked wrapper, 
which will test every element added to the collection for membership of the type supplied when it was created. 

[_Enforce Type Safety When Calling Untrusted Code_](todo) shows an example of this technique.

### A convenient and efficient way of initializing a collection with individual elements

`boolean addAll(Collection<? super T> c, T... elements)`
adds all the specified elements to the specified collection

### Get LIFO Queue

`<T> Queue<T> asLifoQueue(Deque<T> deque)`
returns a view of a Deque as a last in, first out (LIFO) Queue

while queues can impose various different orderings on their elements, 
there is no standard Queue implementation that provides LIFO ordering. 
`Deque` implementations, on the other hand, 
all support LIFO ordering if elements are removed from the same end of the deque as they were added. 
The method `asLifoQueue` allows you to use this functionality through the conveniently concise Queue interface.

### Check if two collections have no elements in common

`boolean disjoint(Collection<?> c1, Collection<?> c2)`
returns true if c1 and c2 have no elements in common

### Using `Collections::disjoint` issue

As with equality of sets, you should avoid using disjoint on sets with different equivalence relations. 
[_Inconsistent with equals_](todo) 
discusses this issue in detail.

### How to get `Enumeration`?

`Enumeration`, a legacy version of `Iterator`. 

`<T> Enumeration<T> enumeration(Collection<T> c)`
returns an enumeration over the specified collection.
The `Enumeration` it returns yields the same elements, in the same order, as the `Iterator` provided by `c`.

### Calculate how many times a certain element exists in a collection

`int frequency(Collection<?> c, Object o)`
returns the number of elements in `c` that are equal to `o`

### Get a list of items from `Enumeration`, why could be used?

`<T> ArrayList<T> list(Enumeration<T> e)`
returns an `ArrayList` containing the elements returned by the specified `Enumeration`.
This method is provided for interoperation with APIs whose methods return results of type Enumeration, 
a legacy version of Iterator. 
The `ArrayList` that it returns contains the same elements, in the same order, as provided by the Enumeration e. 
This method forms a pair with the method enumeration, which creates an `ArrayList` from an `Enumeration`.

### Create a set with a certain map characteristics

- `<E> SequencedSet<E> newSequencedSetFromMap (SequencedMap<E,Boolean> map)`
  returns a SequencedSet backed by the specified SequencedMap
- `<E> Set<E> newSetFromMap(Map<E,Boolean> map)` returns a Set backed by the specified Map

### `Collections::newSetFromMap` purpose

Many sets (such as `TreeSet` and `ConcurrentSkipListSet`) are implemented by maps and share 
their ordering, concurrency, and performance characteristics. 

Some maps, however (such as `WeakHashMap` and `IdentityHashMap`), do not have standard set equivalents. 
The purpose of the method `newSetFromMap` is to provide equivalent set implementations for such maps. 
The method `newSetFromMap` wraps its argument, 
which must be empty when supplied and should never be subsequently accessed directly. 
This code shows the standard idiom for using it to create a weak `HashSet`, 
one whose elements are held via weak references:
```java
Set<Object> weakHashSet = Collections.newSetFromMap(new WeakHashMap<Object, Boolean>());
```

### `Collections::newSequencedSetFromMap` purpose

At first sight, `newSequencedSetFromMap` seems to have limited usefulness within the Collections Framework since, 
unlike the `Map` implementations, all `SequencedMap` implementations in the JDK have standard set equivalents. 
However, `LinkedHashSet`, which corresponds to `LinkedHashMap`, 
lacks one important feature that the corresponding map provides: 
the ability to define an eviction policy (or a callback) to be invoked every time a new entry is made. 
This ability can be provided to a `SequencedSet` by creating it from a `LinkedHashMap`. 

For example, once this set has grown to contain five elements, 
adding a new one will cause the oldest one to be discarded:
```java
SequencedSet<String> set = Collections.newSequencedSetFromMap(
  new LinkedHashMap<String,Boolean>() {
    protected boolean removeEldestEntry(Map.Entry<String,Boolean> e) {
      return this.size() > 5;
    }
  });
```

### Sorting or maintaining a collection of objects in reverse natural order, requirement

`<T> Comparator<T> reverseOrder()` returns a comparator that reverses natural ordering

The `reverseOrder` method provides a simple way of 
sorting or maintaining a collection **of Comparable objects** in reverse natural order. 

Here is an example of its use:

```java
SortedSet<Integer> s = new TreeSet<>(Collections.reverseOrder());
Collections.addAll(s, 1, 2, 3);
assert s.equals(new TreeSet<>(Set.of(1, 2, 3)).reversed());
```

