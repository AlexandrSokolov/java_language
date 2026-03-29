### Iteration over Collection Elements
<details>
<summary>Show answer</summary>


This ability to iterate over the collection of elements is provided by `java.lang.Iterable` interface.

It exposes three methods:
- `void forEach(Consumer<? super T> action)` - Performs action for each element of the Iterable
- `Iterator<T> iterator()` - Returns an iterator over elements of type T
- `Spliterator<T> spliterator()` - Creates a Spliterator over the elements described by this Iterable
  (for parallel streams)


</details>

---

### Which methods does the Iterable interface provide for traversing its elements?
<details>
<summary>Show answer</summary>

- `Iterator<T> iterator()` - Returns an iterator over elements of type T
- `Spliterator<T> spliterator()` - Creates a Spliterator over the elements described by this Iterable
  (for parallel streams)

</details>

---

### Iterating Through a Collection and Consuming Elements One by One
<details>
<summary>Show answer</summary>

Using `java.lang.Iterable.forEach()` - method, common for all the collections:
```java
coll.forEach(System.out::println);
```

</details>

---

### What Is Used Internally in for Loops?
<details>
<summary>Show answer</summary>


`Iterator` is used:
1. Explicitly:
    ```java
    for (Iterator<String> itr = coll.iterator() ; itr.hasNext() ; ) {
      System.out.println(itr.next());
    }
    ```
2. Internally in `foreach` statement:
    ```java
    for (String s : coll) {
      System.out.println(s);
    }
    ```
   The target of a foreach statement can be an array or any class that implements the interface `Iterable`.
   Since the `Collection` interface extends Iterable, any set, list, or queue can be the target of foreach.


</details>

---

### When Is Explicit Iterator Usage Required in a for Loop?
<details>
<summary>Show answer</summary>


It is necessary when you want to make a structural change to a collection-broadly speaking,
adding or removing elements-in the course of iteration.


</details>

---

### Which Structural Modifications Can Be Made During Iteration?
<details>
<summary>Show answer</summary>


`Iterator` exposes only a method for removal of collection elements,
but its subinterface `ListIterator`, available to `List` implementations,
also provides methods to add and replace elements.


</details>

---

### Applying Structural Changes Without Using Iterators
<details>
<summary>Show answer</summary>


If memory constraints don’t prevent you from making a new copy of the list,
streams offer a neater solution to this problem:
```java
List<String> strings = new ArrayList<>(List.of("alpha", "bravo", "charlie"));
List<String> modifiedStrings = strings.stream()
  .filter(s -> s.contains("r"))
  .toList();
assert modifiedStrings.equals(List.of("bravo", "charlie"));
```


</details>

---

### `java.util.Collection`: Overview of Its API
<details>
<summary>Show answer</summary>


Collection, which exposes the core functionality required of any collection other than a Map.
Its methods support managing elements by:
- adding or removing single or multiple elements
- checking membership of a single - `contains()` or multiple values - `containsAll()`
- inspecting - `isEmpty()`, `size()`
- exporting elements - making elements available for further processing -
  `iterator()`, `spliterator()`, `stream()`, `parallelStream()`, `toArray()`


</details>

---

### Methods in `java.util.Collection` That Expose Elements for Further Processing
<details>
<summary>Show answer</summary>


exporting elements - making elements available for further processing -
- `iterator()`
- `spliterator()`
- `stream()`
- `parallelStream()`
- `toArray()`


</details>

---

### `java.util.SequencedCollection`: Overview of Its API
<details>
<summary>Show answer</summary>


The SequencedCollection interface provides:
- versions of these that can be applied to the first and last element of the collection:
  `addFirst`, `addLast`, `removeFirst`, `removeLast`, `getFirst`, and `getLast`.
- a reverse-ordered view of a collection: `reversed` - that is, a way of working with the collection
  as though the ordering has been reversed.

This simplifies many programming problems and often provides more efficient implementations.


</details>

---

### How Do Changes Affect a Collection Returned by `SequencedCollection.reversed()`?
<details>
<summary>Show answer</summary>


The contract states that any successful modifications to this view must write through to the underlying collection,
but that the inverse - visibility in this view of changes to the underlying collection - is implementation-dependent.
The most commonly used implementation, `ArrayList`, does provide this feature:
modifications to the underlying collection are visible in the reversed view.

</details>

---

### How Can Collections Be Modified? Available Options
<details>
<summary>Show answer</summary>


You should use streams to modify collection and its elements.
Streams are a mechanism for transporting a sequence of values from a source to a destination
through a series of operations, typically implemented as lambdas,
each of which can transform, drop, or insert values on the way

#### Modify via Streams API
```java
Point origin = new Point(0, 0);
List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5);
OptionalDouble maxDistance = intList.stream()
    .map(i -> new Point(i % 3, i / 3))
    .mapToDouble(p -> p.distanceFrom(origin))
    .max();
```
Advantages:
1. more concise and readable,
2. often uses less intermediate storage,
3. handles an empty source gracefully, and
4. can never attempt to mutate the source collection

Disadvantage:
You are making a new copy of the collection.
So streams can be used only if memory constraints don’t prevent you from making a new copy of such collections.

#### Modify without Streams
```java
Point origin = new Point(0, 0);
List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5);
List<Point> pointList = new ArrayList<>();
for (Integer i : intList) {
  pointList.add(new Point(i % 3, i / 3));
}
double maxDistance = 0;
for (Point p : pointList) {
  maxDistance = Math.max(p.distanceFrom(origin), maxDistance);
}
```
Disadvantages:
1. very verbose
2. the intermediate collection `pointList` is an overhead on the operation of the program,
   resulting in increased garbage collection costs or even in heap space exhaustion
3. the intent of the program is hard to discern,
   because the crucial operations are interspersed with the code for collection handling

Advantage:
The only collection is used. (if we consider operations like `add`, `remove`, but not the example above)
Applying structural modifications doesn't create a new collection, but they are applied on the collection itself.
So this is a preferable approach, when you do have memory constraints when you from making a new copy of such collections.


</details>

---

### Collection Removal API
<details>
<summary>Show answer</summary>


- `void clear()` - removes all elements
- `boolean remove(Object o)` - remove an element o
- `boolean removeAll(Collection<?> c)` - remove all occurrences of the elements in c
- `boolean retainAll(Collection<?> c)` - remove the elements not in c
- `boolean removeIf(Predicate<? super E> p)` remove the elements for which p is true


</details>

---

### Removing Elements Using the Stream API
<details>
<summary>Show answer</summary>


```java
Collection<Task> tuesdayNonPhoneTasks = tuesdayTasks.stream()
  .filter(t -> ! phoneTasks.contains(t))
  .collect(Collectors.toSet());
```


</details>

---

### Adding vs. Removing Elements: Differences in Method Signatures
<details>
<summary>Show answer</summary>


- `boolean add(E e)` - add the element e
- `boolean remove(Object o)` - remove an element o

If the argument `o` to remove is `null`, the method will remove a single `null` from the collection if one is present.
Otherwise, if any elements `e` are present for which `o.equals(e)`, it removes one;
if not, it leaves the collection unchanged and returns false.

Why isn't `Collection.remove(Object o)` generic?

The signatures of `Collection` methods `contains`, `remove`, and `retain`,
each of which take a parameter of type `Object`, and the corresponding collection-oriented versions
`containsAll`, `removeAll`, and `retainAll`, which take a `Collection<?>`.
Why are these methods not generified to take parameters of type `E` or (respectively) `Collection<E>`?

A moment’s thought shows that these methods, which remove elements from the collection or test for membership,
do not have the potential to compromise its type safety - unlike `add` and `addAll`,
whose signatures do indeed specify the type `E`.
So the designers could safely choose these parameter types. But why did they?
Different reasons have been proposed (including by the designers themselves), including:

- Backward compatibility. To minimize the number of breaking changes caused by the introduction of generics,
  the **only methods generified were those that were necessary to ensure type safety**, like `add` and `addAll`.
- Allowing the use of bounded wildcard types.
  Suppose that a collection parameter to a method is given a type `Set<? extends Foo>`.
  If `contains` required that parametric type, it could never be used in this situation,
  since no argument could be supplied of type `? extends Foo` (except null).
  Giving the parameter of contains the type `Object` ensures that it can be used with collections of any type.
- Accommodating unrelated types. The most compelling reason is that in many situations,
  such as calculating the intersection of two collections, the types of the elements may be completely unrelated.
  In that example, the intersection being calculated was between a collection of type `PhoneTask`
  and a collection of its supertype `Task`, but even this relationship is unnecessary.
  For example, think of calculating the intersection of two collections of List objects.
  Two Lists are equal if they contain the same elements,
  so `retainAll` could plausibly be used to preserve in a `Collection<ArrayList>` only those `ArrayLists`
  that were equal to some element of a different `Collection<LinkedList>`.
  The only parameter type for `retainAll` that makes that possible is `Collection<?>`.

The trade-off between the two alternatives for these collection method types is quite subtle.
On the one hand, it can be argued that using `Object` as the parameter type means that some errors will be missed
that would be caught by the more precise typing.
After all, one of the key advantages of generics is that their precise typing enables more errors
to be caught at compile time.
But in cases like those mentioned here, where the precise typing would be inappropriate,
it would have to be evaded with additional unchecked casts - one of the major sources of imprecision
in the generics type system.

Whether you feel that the designers made the right or the wrong choice,
it is worth knowing the issues that lay behind the decision.


</details>

---

### Adding Multiple Elements to an Existing Collection: Options
<details>
<summary>Show answer</summary>


#### using `java.util.Collections.addAll(Collection<? super T> c, T... elements)` utility:

```java
Collections.addAll(phoneTasks, mikePhone, paulPhone);
Collections.addAll(codingTasks, databaseCode, guiCode, logicCode);
```

#### using `java.util.Collection.addAll(Collection<? extends E> c);` instance method:

This option requires to create another collection for the elements:
```java
phoneTasks.addAll(List.of(mikePhone, paulPhone));
codingTasks.addAll(List.of(databaseCode, guiCode, logicCode));
```

</details>

---

### Combining Existing Collections
<details>
<summary>Show answer</summary>


- using `java.util.Collection.addAll(Collection<? extends E> c);`
- via Stream API:
    ```java
    Collection<Task> allTasks_2 = Stream.of(mondayTasks,tuesdayTasks)
      .flatMap(Collection::stream)
      .collect(Collectors.toSet());
    ```


</details>

---

### Controlling the Resulting Implementation with Stream API Collectors
<details>
<summary>Show answer</summary>

```java
Collection<Task> allTasks_2 = Stream.of(mondayTasks,tuesdayTasks)
  .flatMap(Collection::stream)
  .collect(Collectors.toSet());
```
The contract for `Collectors::toSet` provides no guarantees on the type of the set returned.
At Java 21, the OpenJDK returns a `HashSet`.
But it is possible, if unlikely, that future implementations could return, for example, an unmodifiable set.

If you want to specify the type of the returned set precisely, use `Collectors::toCollection`,
supplying a collection constructor:
```java
Collection<Task> allTasks_2 = Stream.of(mondayTasks,tuesdayTasks)
  .flatMap(Collection::stream)
  .collect(Collectors.toCollection(HashSet::new));
```


</details>

---
