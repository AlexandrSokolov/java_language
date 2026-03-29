### Java Collections Framework
<details>
<summary>Show answer</summary>


A _collection_ is an object that provides access to a group of objects, allowing them to be processed in a uniform way.

A _collections framework_ provides a uniform view of a set of collection types
specifying and implementing common data structures, following consistent design rules so that they can work together.


</details>

---

### The Main Interfaces of the Java Collections Framework
<details>
<summary>Show answer</summary>


- [`java.lang.Iterable` - in order to be used for an _enhanced for statement_, usually called a _foreach_ statement.](#iteration-via-collection-of-elements)
- [`java.util.Collection` - the core functionality required of any collection other than a Map](#javautilcollection)
- [`java.util.Set` - order is not significant and there can be no duplicates]()
- [`java.util.List` - order is significant and accommodates duplicate elements]()
- [`java.util.Queue` - holds elements for processing, yielding them up in the order in which they are to be processed](queues/faq.queues.md#what-is-specific-about-queues-among-other-java-collections)


- [`java.util.Map` - key-value entries to store and retrieve elements.]()

<img src="../../docs/images/Main_Collection_Interfaces.png" alt="Main Interfaces" width="600">


</details>

---

### Sequenced Collections: Purpose and Comparison with Other Types
<details>
<summary>Show answer</summary>

Sequenced Collections provide a unified API for working with ordered collections and their reversed views.

These sequenced collections differ from:
- `Collection`, `Set`, or `Map` in that they have a defined _order_, called in the documentation an _encounter order_.
- `Queue`, which also has a defined _order_, in that they can be iterated in either direction.

</details>

---

### Sequenced Collections vs Queue
<details>
<summary>Show answer</summary>

`Queue` — although queues have order, sequenced collections allow iteration in both directions.

</details>

---

### Sequenced Collection Ordering Types
<details>
<summary>Show answer</summary>


The ordering of sequenced collections can be derived in two different ways:
1. for some, like `List`, elements retain the order in which they were added (sometimes called externally ordered types),
2. whereas for others, like [`NavigableSet`](sets/faq.sets.md#navigableset)),
   the ordering is dictated by the values of the elements (also known as internally ordered types).

Externally ordered and internally ordered terms reflect the difference between
an order that is arbitrarily imposed on the elements, for example by the order in which they are added,
and an order that is an inherent property of the elements themselves, such as alphabetic ordering on strings.


</details>

---

### Which Interfaces Correspond to Internally Ordered Collections?
<details>
<summary>Show answer</summary>

- `NavigableSet`
- `NavigableMap`

</details>

---

### Hierarchy of Sequenced Collections
<details>
<summary>Show answer</summary>


- [`SequencedCollection`](#sequenced-collections)
- [`SequencedSet`](sets/faq.sets.md#sequencedset)
- [`NavigableSet`](sets/faq.sets.md#navigableset)
- [`Deque`](queues/faq.queues.md#deque)
- [`SequencedMap`](maps/faq.maps.md#sequencedmap)
- [`NavigableMap`](maps/faq.maps.md#navigablemap)

<img src="../../docs/images/Sequenced_Collections_Hierarchy.png" alt="Main Interfaces" width="600">


</details>

---

### Externally Ordered Collections
<details>
<summary>Show answer</summary>


- implementations of `java.util.List`
- implementations of `Deque`
- `LinkedHashSet`
- `LinkedHashMap`

All of them extend or implement `SequencedSet`/`SequencedMap`


</details>

---

### Internally Ordered Collections
<details>
<summary>Show answer</summary>


- Implementations of `NavigableSet`
- Implementations of `NavigableMap`


</details>

---

### Ordered Collections That Do Not Implement `SequencedCollection`
<details>
<summary>Show answer</summary>


The following `Queue` implementations do not extend `SequencedCollection`,
but they yield elements up in the order according to the values of the elements.
- `PriorityQueue`
- `PriorityBlockingQueue`
- `DelayQueue`


</details>

---
