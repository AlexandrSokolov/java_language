### Why isn’t there a single “best” implementation for each collection interface?
<details>
<summary>Show answer</summary>


That would certainly make life simpler.
If an implementation is a greyhound for some operations, Murphy’s law tells us that it will be a tortoise for others.
Because there is no “best” implementation of any of the interfaces **for every situation**.


</details>

---

### How do you choose the right collection implementation?
<details>
<summary>Show answer</summary>


You always have to make a trade-off,
judging which operations are used most frequently in your application and
choosing the implementation that optimizes those operations.


</details>

---

### What are the main kinds of operations required by most collection interfaces?
<details>
<summary>Show answer</summary>


The three main kinds of operations that most collection interfaces require:
- insertion and removal of elements by position
- retrieval of elements by content
- iteration over the collection elements

The various implementations provide many variations on these operations,
but the main differences among them can be discussed in terms of how they carry out these three.


</details>

---

### What data structures form the basis of collection implementations?
<details>
<summary>Show answer</summary>


#### Arrays

Because arrays are implemented directly in hardware, they have the properties of random-access memory:
very fast for accessing elements by position and for iterating over them,
but slower for inserting and removing elements at arbitrary positions
(because that may require adjusting the position of other elements).

They also form an important part of the mechanism for implementing hash tables.

#### Linear linked lists

These consist of chains of linked cells.
Each cell contains a reference to data and a reference to the next cell in the list
(and, in some implementations, the previous cell).
Linked lists perform quite differently from arrays:
accessing elements by position is slow,
because you have to follow the reference chain from the start of the list,
but insertion and removal operations can be performed in constant time by rearranging the cell references.

#### Other (nonlinear) linked data structures

Linked structures are particularly suitable for representing nonlinear types like trees and skip lists
(see “ConcurrentSkipListSet”), especially if they need to be rearranged as new elements are added.
Such structures provide an inexpensive way of maintaining sorted order in their data,
allowing fast searching by content.

#### Hash tables

These provide a way of storing elements indexed on their content rather than on an integer-valued index, as with lists.
In contrast to arrays and linked lists, hash tables provide no support for accessing elements by position,
but access by content is usually very fast, as are insertion and removal.


</details>

---

### Which collection implementations are backed by arrays?
<details>
<summary>Show answer</summary>


Arrays are used in the Collections Framework as the backing structure for:
- ArrayList
- CopyOnWriteArrayList
- EnumSet
- EnumMap and
- for many of the Queue and Deque implementations.


</details>

---

### Which collection implementations are backed by linear linked lists?
<details>
<summary>Show answer</summary>


Linked lists are the primary backing structure used for the classes:
- ConcurrentLinkedQueue
- LinkedBlockingQueue
- LinkedList


</details>

---

### Which collection implementations use nonlinear linked data structures?
<details>
<summary>Show answer</summary>


- Trees are the backing structures for `TreeSet` and `TreeMap`.
- Skip lists are used in `ConcurrentSkipListSet` and `ConcurrentSkipListMap`.


</details>

---

### Which collection implementations are backed by hash tables?
<details>
<summary>Show answer</summary>


Hash tables are the backing structure for many Set and Map implementations, including:
- `HashSet`
- `LinkedHashSet`
- `HashMap`
- `LinkedHashMap`
- `WeakHashMap`
- `IdentityHashMap`
- `ConcurrentHashMap`


</details>

---

### Arrays vs Linked Lists
<details>
<summary>Show answer</summary>


Arrays are very fast for accessing elements by position and for iterating over them,
but slower for inserting and removing elements at arbitrary positions
(because that may require adjusting the position of other elements).

Linked lists - accessing elements by position is slow,
because you have to follow the reference chain from the start of the list,
but insertion and removal operations can be performed in constant time by rearranging the cell references.


</details>

---

### Arrays / Linked Lists vs. Hash Tables
<details>
<summary>Show answer</summary>


In contrast to arrays and linked lists, hash tables provide no support for accessing elements by position,
but access by content is usually very fast, as are insertion and removal.


</details>

---

### What must you be aware of when using hash‑table‑based collections?
<details>
<summary>Show answer</summary>


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

Ideally objects must be ideally immutable, or at least cannot be changed after adding to the collection.
Changing the element attributes will make the element not accessible via the hash-table based collection.


</details>

---

### Working with native arrays
<details>
<summary>Show answer</summary>


Suppose we want to test for the presence of a particular object in an array.
One obvious way to do this is to iterate over the array elements,
testing each for equality with the search target.

An alternative would be to use `contains` method of `List` interface to do that work instead.
An array is not a `List`, though, so how can a `List` method be useful in handling it?

We might well want to avoid the overhead of creating a new `ArrayList` object,
physically copying all the elements of the array into a new collection.

In this situation, a better answer is to get a `List` view of the array - an object that "looks like" a `List`,
but implements all its operations **directly on the underlying array**.

The method `asList` of the utility class `Arrays` provides such a view:
```java
Integer[] arr = {1, 2, 3};
var list = Arrays.asList(arr);
```

The data “of” the view actually resides in the underlying structure,
so changes made to that structure are immediately visible in the view, and vice versa.
For example, the following code compiles and runs without errors:

```java
Integer[] arr = {1, 2, 3};
var list = Arrays.asList(arr);
list.set(0, 3);                 // change the list view...
assert arr[0] == 3;             // and the underlying array changes
arr[2] = 0;                     // now change the underlying array...
assert list.get(2) == 0;        // and the list view changes
```


</details>

---

### What can and cannot be done with a list created via `Arrays.asList(arr)`?
<details>
<summary>Show answer</summary>


The simple view that it returns supports some List operations, such as contains,
and methods like get and set that access or replace the array elements,
but it won’t allow you to make structural modifications,
like adding or removing elements, which aren’t supported by the underlying array.

</details>

---

### Why do collection methods that copy elements into an array exist?
<details>
<summary>Show answer</summary>


- `Object[] toArray()` return a new created array containing the elements of this collection
- `<T> T[] toArray(T[] t)` copy contents to an existing `T[]` array (for any T)
- `<T> T[] toArray(IntFunction<T[]> generator)` copy contents to a new `T[]` array,
  created by a function producing a new `T[]` of a given size

These methods are important because many APIs - principally older ones and those
for which performance is especially important - expose methods that accept or return arrays.

The arguments of the last two methods are required in order to provide the virtual machine
with the reifiable type of the array.

The new array will be created during the method execution.
If the array supplied as the argument to the second overload of `toArray` is long enough,
it is used to receive the elements of the collection, overwriting its existing elements.


</details>

---

### Why is any type allowed for T in the `toArray` method declarations?
<details>
<summary>Show answer</summary>


- `<T> T[] toArray(T[] t)` copy contents to an existing `T[]` array (for any T)
- `<T> T[] toArray(IntFunction<T[]> generator)` copy contents to a new `T[]` array,
  created by a function producing a new `T[]` of a given size

The type variable `T` is unrelated to the collection type parameter `E`,
permitting errors at run time that it seems should have been caught at compile time.

```java
List.of(1, 2, 3).toArray(new String[0])    // array store exception
```
compiles successfully but throws `ArrayStoreException` at run time.

Why not restrict the array component type to exactly `E`, the parametric type of the collection?
The principal reason is to allow the possibility of giving the array a more specific component type
than that of the collection, when the elements of the collection all happen to belong to the same subtype:
```java
List<Object> l = List.of("zero", "one");
String[] a = l.toArray(new String[0]);
```
Here, a list of objects happens to contain only strings, so it can be converted into a `String[]`.
If the list contains an object that is not a string, the error is caught at run time rather than compile time:

```java
List<Object> l = List.of("zero", "one", 2);
String[] a = l.toArray(new String[0]);      // throws ArrayStoreException
```

In general, you may want to copy a collection of a given type into:
- an array of a more specific type (for instance, copying a list of objects into an array of strings, as just shown) or
- of a more general type (for instance, copying a list of strings into an array of objects).


</details>

---

### What issues exist with the design of `toArray`?
<details>
<summary>Show answer</summary>


One drawback of this design is that, applied to collections of wrapper types,
it doesn’t accommodate automatic unboxing into the corresponding array of primitives:

```java
List<Integer> l = List.of(0, 1, 2);
int[] a = l.toArray(new int[0]);  // compile-time error
```
This is illegal because the parameter T in the method call must - as for any type parameter - be a reference type.

Solutions:
1. resort to copying the array explicitly:
    ```java
    jshell> List<Integer> integers = List.of(0, 1, 2);
    integers ==> [0, 1, 2]
    jshell> int[] ints = new int[integers.size()];
    ints ==> int[3] { 0, 0, 0 }
    jshell> for (int i=0; i<integers.size(); i++) { ints[i] = integers.get(i); }
    jshell> ints
    ints ==> int[3] { 0, 1, 2 }
    ```
2. using the Stream API:
    ```java
    jshell> int[] ints = integers.stream()
       ...>     .mapToInt(Integer::intValue)
       ...>     .toArray();
    ints ==> int[3] { 0, 1, 2 
    ```


</details>

---


### What should you consider when storing objects in a Set, Map, or internally ordered Queue?
<details>
<summary>Show answer</summary>


Whenever you are storing objects in a Set, a Map, or an internally ordered Queue,
ensure that the fields used by the collection to organize its contents are immutable.


</details>

---

### Views in the Collections Framework: Purpose
<details>
<summary>Show answer</summary>


Views allows to avoid the overhead of creating an object copy,
physically copying all the elements of one collection into a new collection.

Each of these views has different rules
dictating which modifications they will accept and reflect into the backing collection.
In descending order of permissiveness, they may allow:

- All changes
- Some structural and all nonstructural modifications
- Only nonstructural modifications
- No modifications at all (fully unmodifiable)

So the interfaces that these views implement have some of their operations labeled `optional`.


</details>

---

### Examples of Views in the Collections Framework
<details>
<summary>Show answer</summary>


The Collections API exposes many methods returning views.
For example, the keys of a `Map` can be viewed as a `Set`, as can its entries;
collections can be viewed as unmodifiable, and so on.

Views can generally be composed for read operations and are often commutative - that is, can be applied in any order.
For example:

```java
List<String> names = List.of("alpha", "bravo", "charlie", "delta");
List<String> reverseThenSublist = names.reversed().subList(1, 3);
List<String> subListThenReverse = names.subList(1, 3).reversed();
assert reverseThenSublist.equals(subListThenReverse);
```


</details>

---

### How can you remember when Java uses toXxx() versus asXxx() for converting or viewing collections?
<details>
<summary>Show answer</summary>

Rule of thumb:
- `toXxx()` - creates something new.
  `List::toArray()` - **create a new array** (or fill one you pass in)
- `asXxx()` - creates a view of something that already exists. 
  `Arrays.asList` - returns a **view backed by the original array**.

</details>