### Comparing Set Implementations
<details><summary>Show answer</summary>


|                         | `add`    | `contains` | `next`   | Notes                   |
|:------------------------|:---------|:-----------|:---------|:------------------------|
| `HashSet`               | O(1)     | O(1)       | O(h/N)   | h is the table capacity |
| `LinkedHashSet`         | O(1)     | O(1)       | O(1)     |                         |
| `CopyOnWriteArraySet`   | O(N)     | O(N)       | O(1)     |                         |
| `EnumSet`               | O(1)     | O(1)       | O(1)     |                         |
| `TreeSet`               | O(log N) | O(log N)   | O(log N) |                         |
| `ConcurrentSkipListSet` | O(log N) | O(log N)   | O(1)     |                         |


In the EnumSet implementation for enum types with more than 64 values,
next has worst-case complexity of O(log m), where m is the number of elements in the enumeration.


</details>

### Sets implementation choice
<details><summary>Show answer</summary>


#### For use in single-threaded applications:
- `HashSet`
- `LinkedHashSet`
- `TreeSet`

They are not thread-safe, so they can only be used in multithreaded code either in conjunction with client-side locking
or wrapped in Collection.synchronizedSet.
When there is no requirement for the set to be sorted, your choice is between `HashSet` and `LinkedHashSet`.
If your application will be frequently iterating over the set,
`LinkedHashSet` is the implementation of choice.
If the set needs to support the methods of NavigableSet, use TreeSet.

#### In a multithreaded environment the choice is between:
- the set view provided by `Collections::newSetFromMap` with `ConcurrentHashMap` - the default choice, on efficiency grounds
- `ConcurrentSkipListSet` - the second supports the methods of NavigableSet
- `CopyOnWriteArraySet` don’t use it in a context where you were expecting many searches or insertions.
  But iteration costs O(1) per element, and it provides thread safety without adding to the cost of read operations
  (using `copy-on-write` algorithm).

</details>
