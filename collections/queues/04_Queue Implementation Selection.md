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

### What is the first question to ask when choosing the right Queue implementation?
<details><summary>Show answer</summary>

The first question to ask is whether the chosen implementation needs to support concurrent access.

</details>

### Which Queue implementations do not require concurrent access?
<details><summary>Show answer</summary>

- `ArrayDeque` - for FIFO ordering
- `PriorityQueue` - for priority ordering

</details>

### If thread safety is required, what is the next question you should ask when choosing a Queue implementation?
<details><summary>Show answer</summary>

After deciding that thread safety is required, there are two independent concerns you must consider next:
- [What ordering semantics are required?](#which-threadsafe-queue-implementations-support-different-ordering-guarantees)
- Is blocking behavior needed?

</details>

### Which thread‑safe Queue implementations support different ordering guarantees?
<details><summary>Show answer</summary>

- `ConcurrentLinkedQueue` - FIFO ordering without blocking
- `PriorityBlockingQueue` - blocking priority ordering
- `DelayQueue` - blocking delay ordering

</details>

### Which thread‑safe Queue implementations support non-blocking behavior?
<details><summary>Show answer</summary>

- `ConcurrentLinkedQueue` - FIFO ordering without blocking

</details>

### Which queue implementations provide thread‑safe blocking behavior?
<details><summary>Show answer</summary>

- `LinkedBlockingQueue` and `ArrayBlockingQueue` - for classic producer–consumer scenarios
- `SynchronousQueue` - blocking queue with no internal capacity, providing **direct handoff** between producer and consumer
- `LinkedTransferQueue` - supports both asynchronous enqueuing and synchronous handoff

</details>

### `LinkedBlockingQueue` vs `ArrayBlockingQueue`
<details><summary>Show answer</summary>

If you **cannot define a realistic upper bound** for the queue size, `LinkedBlockingQueue` is the only viable choice, 
since `ArrayBlockingQueue` is always bounded and requires its capacity to be fixed at construction time.


When a bounded queue is acceptable, the choice between the two is primarily driven by performance and memory trade‑offs, 
especially under concurrent load. While both queues provide constant‑time insertion and removal in theory, 
their behavior differs significantly in practice due to design choices.


Several factors influence their relative performance:
- **Locking strategy**
  - `LinkedBlockingQueue` uses separate locks for enqueueing and dequeueing, 
    allowing producers and consumers to operate concurrently with minimal contention.
  - `ArrayBlockingQueue`, by contrast, uses a single lock, 
    which can increase contention when producers and consumers are active simultaneously.
- **Memory allocation behavior**
  - `ArrayBlockingQueue` **preallocates all required storage upfront**, resulting in fixed and predictable 
    memory usage and avoiding any allocation during runtime. Because elements are stored in a reusable array, 
    insertions do not require creating new objects, which can improve performance under high throughput.
  - `LinkedBlockingQueue`, in contrast, allocates a new node for each inserted element. 
    This allows memory usage to grow and shrink dynamically with the queue size, 
    but it comes at the cost of ongoing object allocation and garbage collection overhead, 
    which can negatively impact performance under heavy load.
- **Cache locality**
  - Array‑based structures like `ArrayBlockingQueue` typically exhibit better cache locality than linked structures.
  - Linked structures such as `LinkedBlockingQueue` are more prone to cache misses due to pointer chasing, 
    which can become a dominant performance factor on modern CPUs.


**Practical guidance**

* Prefer LinkedBlockingQueue when:
  - the maximum queue size is unknown or difficult to estimate
  - minimizing producer–consumer contention is important
  - adaptability matters more than raw memory predictability
* Prefer ArrayBlockingQueue when:
  - a fixed capacity is acceptable or desired
  - predictable memory usage is important
  - high throughput and cache efficiency are priorities

</details>
