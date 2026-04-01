### Comparative performance of different Queue and Deque implementations
<details><summary>Show questions</summary>


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

### First question to ask to choose the right implementation
<details><summary>Show questions</summary>


the first question to ask is whether the implementation you choose needs to support concurrent access.

</details>

### Queue implementations that do not need concurrent access
<details><summary>Show questions</summary>


- `ArrayDeque` - for FIFO ordering
- `PriorityQueue` - for priority ordering

</details>

### Your application does demand thread safety. What is the next question to ask?
<details><summary>Show questions</summary>


If your application does demand thread safety, you next need to consider ordering.

</details>

### Queue implementations that demand thread safety
<details><summary>Show questions</summary>


- `PriorityBlockingQueue` - priority ordering
- `DelayQueue` - delay ordering
- [for FIFO ordering, you need consider several options](#queue-implementations-that-demand-thread-safety-and-fifo-ordering)

</details>

### Queue implementations that demand thread safety and FIFO ordering
<details><summary>Show questions</summary>


If FIFO ordering is acceptable, the third question is whether you need blocking methods,
as you usually will for producer/consumer problems
(either because the consumers must handle an empty queue by waiting,
or because you want to constrain demand on them by bounding the queue, and then producers must sometimes wait).

- `ConcurrentLinkedQueue` - wait-free implementation - if you don’t need blocking methods or a bound on the queue size.
- [If you do need a blocking queue, consider additional options](#blocking-queue-implementations-that-demand-thread-safety-and-fifo-ordering)

</details>

### Blocking Queue implementations that demand thread safety and FIFO ordering
<details><summary>Show questions</summary>


If you do need a blocking queue, because your application requires support for producer/consumer cooperation,
**pause to consider whether you really need to buffer data**,
or whether all you need is a safe hand-off of data between the threads.

If you can do without buffering
(usually because you are confident that there will be enough consumers to prevent data from piling up),
then `SynchronousQueue` is an efficient alternative to the remaining FIFO-blocking implementations,
`LinkedBlockingQueue` and `ArrayBlockingQueue`.

</details>

### `LinkedBlockingQueue` vs `ArrayBlockingQueue`
<details><summary>Show questions</summary>


If you cannot fix a realistic upper bound for the queue size,
then you must choose `LinkedBlockingQueue`, as `ArrayBlockingQueue` is always bounded.

For bounded use, you will choose between the two on the basis of performance.
Their performance characteristics are the same, but these are only the formulas for sequential access;
how they perform in concurrent use is a different question.

A number of factors combine to influence their relative performance:
- Having separate locks on the head and the tail means that producer and consumer threads
  do not need to contend with each other for `LinkedBlockingQueue`.
  `ArrayBlockingQueue` uses a single lock.
- An upside of the bounded nature of `ArrayBlockingQueue` is that its use of memory is predictable:
  it never allocates, unlike `LinkedBlockingQueue`.
  On the other hand, preallocation means that it may be using more memory than it needs, unlike `LinkedBlockingQueue`,
  whose allocation will more or less match the queue size.
- Conversely, an `ArrayBlockingQueue` does not have to allocate new objects with each insertion,
  unlike a `LinkedBlockingQueue`.
- Linked data structures generally have much worse cache behavior than array-based ones.
  As we saw in [_Memory_](todo), cache misses can be the dominant factor in an algorithm’s performance.

</details>
