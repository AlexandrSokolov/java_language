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

#### For use in single-threaded applications
Use:
- `HashSet` - when you need **fast lookup and insertion** and **do not care about iteration order**.
- `LinkedHashSet` - when you need **predictable iteration order** (usually insertion order) with near‑O(1) performance.
- `TreeSet` - when you need elements to be kept sorted or require range and navigation operations, 
  with `O(log n)` performance.

#### In a multithreaded environment
Use:
- a set backed by `ConcurrentHashMap` as the **default general‑purpose concurrent set** when you need 
  **fast, scalable add, remove, and contains** operations under concurrency.
- `CopyOnWriteArraySet` - when **iterations are very frequent and modifications are rare**,
  and you want **lock‑free, thread‑safe iteration**.
- `ConcurrentSkipListSet` - when you need a thread‑safe, **sorted set** with **navigable and range operations** 
  and predictable `O(log n)` performance.

</details>
