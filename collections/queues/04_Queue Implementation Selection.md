### How does the performance of different `Queue` and `Deque` implementations compare?
<details><summary>Show answer</summary>


|                       | offer    | peek | poll     | size |
|:----------------------|:---------|:-----|:---------|:-----|
| PriorityQueue         | O(log N) | O(1) | O(log N) | O(1) |
| ConcurrentLinkedQueue | O(1)     | O(1) | O(1)     | O(N) |
| ArrayBlockingQueue    | O(1)     | O(1) | O(1)     | O(1) |
| LinkedBlockingQueue   | O(1)     | O(1) | O(1)     | O(1) |
| PriorityBlockingQueue | O(log N) | O(1) | O(log N) | O(1) |
| DelayQueue            | O(log N) | O(1) | O(log N) | O(1) |
| LinkedTransferQueue   | O(1)     | O(1) | O(1)     | O(N) |
| LinkedList            | O(1)     | O(1) | O(1)     | O(1) |
| ArrayDeque            | O(1)     | O(1) | O(1)     | O(1) |
| LinkedBlockingDeque   | O(1)     | O(1) | O(1)     | O(1) |

</details>

### How do you choose a Queue implementation?
<details><summary>Show answer</summary>

- [Thread safety needed?](#do-you-need-thread-safety)
  - No → [Which ordering?](#which-ordering-without-thread-safety) *(last gate)*
  - Yes → [Blocking behaviour needed?](#do-you-need-blocking-behaviour)
    - No → [Which ordering?](#which-non-blocking-concurrent-ordering) *(last gate)*
    - Yes → [Direct handoff wanted?](#do-you-want-direct-handoff)
      - Handoff → handoff-only vs handoff-plus-async — two implementations, no ordering choice
      - No → [Which ordering?](#which-ordering-for-decoupled-blocking)
        - FIFO → [Bounded or unbounded?](#linkedblockingqueue-vs-arrayblockingqueue) *(only FIFO forks again)*
        
        For the next one implementation each, no further choice:
        - LIFO  
        - time 
        - sorted

</details>

### Do you need thread safety?
<details><summary>Show answer</summary>

- No → stay with plain implementations, only [ordering](#which-ordering-without-thread-safety) is left to decide.
- Yes → enter the concurrent family, then ask about [blocking behaviour](#do-you-need-blocking-behaviour).

</details>

### Which ordering without thread safety?
<details><summary>Show answer</summary>

- FIFO → `ArrayDeque`
- LIFO (stack) → `ArrayDeque`
- Sorted → `PriorityQueue`

</details>

### Do you need blocking behaviour?
<details><summary>Show answer</summary>

- No → [which non-blocking concurrent ordering?](#which-non-blocking-concurrent-ordering)
- Yes → [do you want direct handoff?](#do-you-want-direct-handoff)

</details>

### Which non-blocking concurrent ordering?
<details><summary>Show answer</summary>

- FIFO → `ConcurrentLinkedQueue`
- LIFO → **`ConcurrentLinkedDeque`** *(lock-free deque, Java 7 — the easily-forgotten one)*

No sorted option here: sorted concurrent ordering only exists as a blocking queue (`PriorityBlockingQueue`).

</details>

### Do you want direct handoff?
<details><summary>Show answer</summary>

Handoff means an insert waits until another thread takes the element — no element is ever stored.

- Handoff only → `SynchronousQueue` (zero internal capacity, pure handoff)
- Handoff **and** normal async queuing → `LinkedTransferQueue` (the only `TransferQueue`)
- Neither — fully decoupled queuing → [which ordering for decoupled blocking?](#which-ordering-for-decoupled-blocking)

Both handoff choices are single classes with no ordering question. Ordering only reappears once you drop handoff.

</details>

### Which ordering for decoupled blocking?
<details><summary>Show answer</summary>

- FIFO → `LinkedBlockingQueue` or `ArrayBlockingQueue` → [bounded or unbounded?](#linkedblockingqueue-vs-arrayblockingqueue)
- LIFO → `LinkedBlockingDeque`
- Time-based → `DelayQueue`
- Sorted → `PriorityBlockingQueue`

</details>

### `LinkedBlockingQueue` vs `ArrayBlockingQueue`
<details><summary>Show answer</summary>

Both are FIFO blocking queues. Decide in two steps.

**Gate — can you bound the size?**
- No realistic upper bound → `LinkedBlockingQueue` is the only option (grows dynamically).
- `ArrayBlockingQueue` is always bounded; capacity is fixed at construction and cannot change.

**If both are viable, weigh three trade-offs:**
- *Locking* — `LinkedBlockingQueue` uses separate put/take locks, so producers and consumers barely contend. 
  `ArrayBlockingQueue` uses one lock for both, so they contend under concurrent load.
- *Memory* — `ArrayBlockingQueue` preallocates its array: fixed, predictable, no per-insert allocation.
  `LinkedBlockingQueue` allocates a node per element: dynamic size, but ongoing GC pressure.
- *Cache* — the array gives good locality; the linked nodes cause pointer-chasing and cache misses.

**Verdict:** 
- `LinkedBlockingQueue` usually wins throughput under heavy producer/consumer concurrency (less lock contention); 
- `ArrayBlockingQueue` wins on predictable memory and cache efficiency.

</details>
