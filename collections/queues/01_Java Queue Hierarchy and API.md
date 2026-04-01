### Queue, its purpose
<details><summary>Show questions</summary>


```text
A queue is a collection designed to hold elements for processing, yielding them up in the order in which they are to be processed. 
```

"yielding them up" - giving the elements out when someone asks for them.

</details>

### What is specific about queues among other Java collections?
<details><summary>Show questions</summary>


Queue is different in kind from the other collections.
Sets, lists, and maps are typically
["owned" by another object and form part of its state](../faq.collections.md#respect-the-ownership-of-collections).


By contrast, queues are not usually owned by a single object,
but are used for transmission of values from producers to consumers.

A queue can have multiple producers and multiple consumers; these can be objects, or threads, or processes.

</details>

### Hierarchy of `Queue` in the Collections Framework
<details><summary>Show questions</summary>

<img src="../../docs/images/Implementations_of_Queue.png" alt="Implementations of Queue in the Collections Framework" width="600"/>

</details>

### `Queue` vs `List`
<details><summary>Show questions</summary>


The methods of `Queue` are useful to us only if the head element is actually one that we want.

For example, it might help to know something about all the outstanding tasks before you choose the next one.
Otherwise, in a situation of limited time with an entirely queue-based to-do manager,
you might end up going for coffee until the meeting starts.

As an alternative, you could consider using `List` interface, which provides more flexible means
of accessing its elements but has the drawback that its implementations provide much less support for multithread use.

Note: True, the class `PriorityQueue` allows us to provide a comparator that will order the queue elements
so that the one we want is at the head, but that may not be a particularly good way of expressing the algorithm
for choosing the next task.

</details>

### What is the main difference in `Queue<>` implementations
<details><summary>Show questions</summary>


`ordering` - in choosing a Queue implementation, you’re also choosing the ordering of elements (tasks) processing.
Different implementations embodying different rules about what the order should be in which elements are to be processed.

</details>

### Queue orders examples
<details><summary>Show questions</summary>


- FIFO (first in, first out) - the rule that tasks are to be processed in the order in which they were submitted.
  Examples: `ArrayDeque`, `LinkedBlockingQueue`
- LIFO (last in, first out, also known as stacks)
- priority queues order elements according to a supplied comparator - `PriorityQueue`
- hold elements until their delay has expired - `DelayQueue`

</details>

### Queue Implementation Options. What does affect the choice?
<details><summary>Show questions</summary>


- [order](#queue-orders-examples)
- thread-safe - most of them are thread-safe
  (except `PriorityQueue`, `ArrayDeque`, `LinkedList` - the ones that are not located under `java.util.concurrent`)
- blocking facilities (that is, operations that wait for conditions to be right for them to execute) - most of them
  are blocking queues (except `PriorityQueue`, `ConcurrentLinkedQueue`)
- a synchronization facility [see `SynchronousQueue`](#blockingqueue-implementations)

</details>

### Queue attributes (properties)
<details><summary>Show questions</summary>


- `tail` - each time task (queue element) is added to the queue, it joins the tail of the queue
- `head` - each added task (element) waits until it reaches the queue head,
  when the tasks are assigned to the next consumer who becomes free.
- `bound` - a maximum size of a queue (only if it is a bounded - queue - capacity-restricted)

</details>

### What functionality does `Queue` interface methods offer?
<details><summary>Show questions</summary>


1. add an element to the tail of the queue
2. inspect the element at its head (only to retrieve)
3. remove the element at its head (retrieve and remove)

</details>

### Queue Interface Methods, what must you remember about
<details><summary>Show questions</summary>


Each of Queue operations comes in two forms:
- one that returns either null or false, depending on the operation to indicate failure
- one that throws an exception.

</details>

### Adding an Element to a Queue
<details><summary>Show questions</summary>


When you add an element you must think about:
- If you use a bounded queue, you must think about how to handle the case, when queue is full.
- Adding `null` as a queue element. Because methods that return/remove element and return null to
  signify that the queue is empty, you should avoid using `null` as a queue element.
  In general, the use of `null` as a queue element is discouraged by `Queue` interface;
  in the JDK, the only implementation that allows it is the legacy class `LinkedList`.

Adding an Element to a Queue:
- `boolean add(E e)` - returns true upon success and throws `IllegalStateException` if no space is currently available.
- `boolean offer(E e)` - returns true if the element was added to this queue, else false

When using a capacity-restricted queue, `offer(E e)` is generally preferable to `add(E e)`,
which can fail to insert an element only by throwing an exception.

Note: reaching capacity is an exceptional situation, as a result I suppose `add(E e)` is more preferable.

</details>

### Retrieving an Element from a Queue
<details><summary>Show questions</summary>


Throws exception:
- `E element()` retrieve but do not remove the head element
- `E remove()` retrieve and remove the head element

The methods that return null for an empty queue are:
- `E peek()` retrieve but do not remove the head element
- `E poll()` retrieve and remove the head element

</details>

### Bounded/unbounded `Queue` implementations
<details><summary>Show questions</summary>


Unbounded:
- `ArrayDeque` (FIFO), `ConcurrentLinkedQueue` (FIFO)
  Bounded:
- `LinkedBlockingQueue` (FIFO)

</details>

### `Queue` implementations that supports priority ordering
<details><summary>Show questions</summary>


- `PriorityQueue` - not thread-safe, nor does it provide blocking behavior
- `PriorityBlockingQueue` thread-safe version of `PriorityQueue`

</details>

### What must you care about when use `PriorityQueue`, its alternatives
<details><summary>Show questions</summary>


`PriorityQueue` is not designed primarily for concurrent use.
It is not thread-safe, nor does it provide blocking behavior (`PriorityBlockingQueue` - thread-safe alternative).

`PriorityQueue` gives up its elements for processing according to an ordering:
1. either the natural order of its elements if they implement `Comparable`,
2. or the ordering imposed by a `Comparator` supplied when the `PriorityQueue` is constructed.

`PriorityQueue` vs `NavigableSet`
- if it needs to examine and manipulate the set of waiting tasks, use `NavigableSet` (and uniqueness via `equal`);
- if its main requirement is efficient access to the next task to be performed, use `PriorityQueue` (accommodates duplicates).

`PriorityQueue` gives no guarantee of how it presents multiple elements with the same value.
So if several tasks are tied for the highest priority in the queue,
it will choose one of them arbitrarily as the head element.

</details>

### How are `PriorityQueue` usually implemented? What data structure is behind?
<details><summary>Show questions</summary>


Priority queues are usually efficiently implemented by `priority heaps`.
A priority heap is a binary tree somewhat like `TreeSet`, but with two differences:
1. the only ordering constraint is that each node in the tree should be ordered with respect to its children:
    - either smaller, in the case of a min heap (which is Java’s default for naturally ordered elements),
    - or larger, in the case of a max heap.
2. the tree should be complete at every level except possibly the lowest;
   if the lowest level is incomplete, the nodes it contains must be grouped together at the left.

![Adding an element to a PriorityQueue](../../docs/images/Adding_to_PriorityQueue.png)

To add a new element to a priority heap, it is first attached at the leftmost vacant position.
Then it is repeatedly exchanged with its parent until it reaches a parent that has higher priority -
that is, has a smaller value.

![Removing the head of a PriorityQueue](../../docs/images/Removing_head_of_PriorityQue.png)

Getting the highest-priority element from a priority heap is trivial: it is the root of the tree.
But when that has been removed, the two separate trees that result must be reorganized into a priority heap again.
This is done by first placing the rightmost element from the bottom row into the root position.
Then - in the reverse of the procedure for adding an element - it is repeatedly exchanged
with the smaller of its children until it has a higher priority than either, or until it has become a leaf.

</details>

### `Queue` implementation that is thread-safe but without blocking facility
<details><summary>Show questions</summary>


`ConcurrentLinkedQueue` - an unbounded, thread-safe, FIFO-ordered queue.

</details>

### How is `ConcurrentLinkedQueue` implemented? What data structure is behind?
<details><summary>Show questions</summary>


It uses a linked structure, similar to the one in `ConcurrentSkipListSet` as the basis for skip lists
and in `HashSet` for hash table overflow chaining.

One of the main attractions of linked structures is that the insertion and removal operations implemented by
pointer rearrangements are performed in constant time.
This makes them especially useful as FIFO queue implementations,
where these operations are always required on nodes at the ends of the structure -
that is, nodes that do not need to be located using the slow sequential search of linked structures.

</details>

### Concurrent algorithm used by `ConcurrentLinkedQueue`
<details><summary>Show questions</summary>


`ConcurrentLinkedQueue` uses a `CAS`-based wait-free algorithm—that is, one that guarantees that every thread
will make progress over time, regardless of the state of other threads accessing the queue.
It executes queue insertion and removal operations in constant time, but requires linear time to execute size.
This is because the algorithm, which relies on cooperation between threads for insertion and removal,
does not keep track of the queue size and has to iterate over the queue to calculate it when it is required.

</details>
