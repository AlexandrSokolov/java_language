
- [Queues in Java collections hierarchy](#what-is-specific-about-queues-among-other-java-collections)
- [`Queue` vs `List`](#queue-vs-list)
- [Queue implementations, the main difference](#what-is-the-main-difference-in-queue-implementations)
- [Queue orders examples](#queue-orders-examples)
- [Queue Implementations, what affects the choice](#queue-implementations)
- [Queue attributes (properties)](#queue-attributes-properties)
- [Bounded/unbounded `Queue` implementations](#boundedunbounded-queue-implementations)
- [`Queue` implementations that support priority ordering](#queue-implementations-that-supports-priority-ordering)
- [`Queue` implementations designed for use in producer/consumer scenarios](#queue-implementations-designed-for-use-in-producerconsumer-scenarios)
- [What must you care about when use methods of `BlockingQueue`?](#what-must-you-care-about-when-use-methods-of-blockingqueue)
- [`Queue` implementation that supports ordering based on the delay time](#queue-implementation-that-supports-ordering-based-on-the-delay-time)
- [`Queue` implementation that is thread-safe but without blocking facility](#queue-implementation-that-is-thread-safe-but-without-blocking-facility)
- [You want to exchange information between a producer and a consumer. What options are available?](#you-want-to-exchange-information-between-a-producer-and-a-consumer-what-options-are-available)
- [You want to use both synchronous and asynchronous messages in producer/consumer scenarios](#you-want-to-use-both-synchronous-and-asynchronous-messages-in-producerconsumer-scenarios)
- [What must you remember about when use `Queue` interface methods](#queue-interface-methods-what-must-you-remember-about)
- [What functionality do `Queue` interface methods offer?](#what-functionality-do-queue-interface-methods-offer)
- [Adding an Element to a Queue, what must you care about?](#adding-an-element-to-a-queue)
- [Retrieving an Element from a Queue](#retrieving-an-element-from-a-queue)
- [What functionality do `BlockingQueue` interface methods offer?](#what-functionality-do-blockingqueue-interface-methods-offer)
- [How it works if you add an element into a bounded blocking queue that has reached capacity](#how-it-works-if-you-add-an-element-into-a-bounded-blocking-queue-that-has-reached-capacity)
- [Retrieving and removing the head of an empty blocking queue](#retrieving-and-removing-the-head-of-an-empty-blocking-queue)
- [How do blocking queues manage blocked requests? What is the issue with it?](#how-do-blocking-queues-manage-blocked-requests-what-is-the-issue-with-it)
- [How to share a blocking queue in multithreaded contexts between producers and consumers?](#how-to-share-a-blocking-queue-in-multithreaded-contexts-between-producers-and-consumers)
- [You use a thread-safe, blocking queue in multithreaded contexts. Are you safe?](#you-use-a-thread-safe-blocking-queue-in-multithreaded-contexts-are-you-safe)
- [What must you care about when use `PriorityQueue`, its alternatives](#what-must-you-care-about-when-use-priorityqueue-its-alternatives)
- [`BlockingQueue` Implementations](#blockingqueue-implementations)
- [How are `PriorityQueue` implemented? What data structure is behind?](#how-are-priorityqueue-usually-implemented-what-data-structure-is-behind)
- [How is `ConcurrentLinkedQueue` implemented? What data structure is behind?](#how-is-concurrentlinkedqueue-implemented-what-data-structure-is-behind-)
- [Concurrent algorithm used by `ConcurrentLinkedQueue`](#concurrent-algorithm-used-by-concurrentlinkedqueue)
- [`PriorityBlockingQueue`, what must you care about?](#priorityblockingqueue-what-must-you-care-about)

### What is specific about queues among other Java collections?

```text
A queue is a collection designed to hold elements for processing, yielding them up in the order in which they are to be processed. 
```

### `Queue` vs `List`

`Queue` inspection and removal methods:
```java
Task nextTask = taskQueue.peek();
if (nextTask instanceof PhoneTask) {
  taskQueue.remove();
  // process nextTask
}
```
are a major benefit of the `Queue` interface; Collection has nothing like them (though `NavigableSet` does). 

The price we pay for this benefit is that the methods of `Queue` are useful to us 
only if the head element is actually one that we want. 

For example, it might help to know something about all the outstanding tasks before you choose the next one.
Otherwise, in a situation of limited time with an entirely queue-based to-do manager,
you might end up going for coffee until the meeting starts.

As an alternative, you could consider using `List` interface, which provides more flexible means 
of accessing its elements but has the drawback that its implementations provide much less support for multithread use.

Note: True, the class `PriorityQueue` allows us to provide a comparator that will order the queue elements
so that the one we want is at the head, but that may not be a particularly good way of expressing the algorithm
for choosing the next task.


### What is the main difference in `Queue<>` implementations

`ordering` - in choosing a Queue implementation, you’re also choosing the ordering of elements (tasks) processing.
Different implementations embodying different rules about what the order should be in which elements are to be processed.

### Queue orders examples

- FIFO (first in, first out) - the rule that tasks are to be processed in the order in which they were submitted. 
  Examples: `ArrayDeque`, `LinkedBlockingQueue`
- LIFO (last in, first out, also known as stacks)
- priority queues order elements according to a supplied comparator - `PriorityQueue`
- hold elements until their delay has expired - `DelayQueue`

### Queue Implementations

- [order](#queue-orders-examples) 
- thread-safe - most of them are thread-safe 
  (except `PriorityQueue`, `ArrayDeque`, `LinkedList` - the ones that are not located under `java.util.concurrent`)
- blocking facilities (that is, operations that wait for conditions to be right for them to execute) - most of them 
  are blocking queues (except `PriorityQueue`, `ConcurrentLinkedQueue`)
- a synchronization facility

### Queue attributes (properties)

- `tail` - each time task (queue element) is added to the queue, it joins the tail of the queue 
- `head` - each added task (element) waits until it reaches the queue head, 
   when the tasks are assigned to the next consumer who becomes free.
- `bound` - a maximum size of a queue (only if it is a bounded - queue - capacity-restricted) 

### Bounded/unbounded `Queue` implementations

Unbounded:
- `ArrayDeque` (FIFO), `ConcurrentLinkedQueue` (FIFO)
Bounded:
- `LinkedBlockingQueue` (FIFO)

### `Queue` implementations that supports priority ordering

- `PriorityQueue` - not thread-safe, nor does it provide blocking behavior
- `PriorityBlockingQueue` thread-safe version of `PriorityQueue`

### `Queue` implementations designed for use in producer/consumer scenarios

`BlockingQueue<E>` - is designed primarily for use in producer/consumer scenarios.

Blocking facilities - that is, operations that wait for conditions to be right for them to execute.

### What must you care about when use methods of `BlockingQueue`?

`BlockingQueue` guarantees that the queue operations of its implementations will be thread-safe and atomic. 


But this guarantee doesn’t extend to the bulk operations inherited from Collection - 
`addAll`, `containsAll`, `retainAll`, and `removeAll` - unless the individual implementation provides it. 
So it is possible, for example, for `addAll` to fail, throwing an exception, 
after adding only some of the elements in a collection.

### `Queue` implementation that supports ordering based on the delay time

`DelayQueue` - is a specialized priority queue, in which the ordering is based on the delay time for each element -
the time remaining before the element will be ready to be taken from the queue.

If all elements have a positive delay time - that is, none of their associated delay times has expired -
an attempt to poll the queue will return null.

If one or more elements has an expired delay time, the one with the longest-expired delay time will be
at the head of the queue.

### `Queue` implementation that is thread-safe but without blocking facility

`ConcurrentLinkedQueue` - an unbounded, thread-safe, FIFO-ordered queue.

### You want to exchange information between a producer and a consumer. What options are available?

You need to exchange information between threads in a thread-safe manner. You have 2 options.

1. Using a Shared Variable (`AtomicInteger` for instance) and `CountDownLatch` for coordinating processing:
    ```java
    ExecutorService executor = Executors.newFixedThreadPool(2);
    AtomicInteger sharedState = new AtomicInteger();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    
    Runnable producer = () -> {
      Integer producedElement = ThreadLocalRandom
        .current()
        .nextInt();
      sharedState.set(producedElement);
      countDownLatch.countDown();
    };
    
    Runnable consumer = () -> {
      try {
        countDownLatch.await();
        Integer consumedElement = sharedState.get();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    };
    ...
    executor.execute(producer);
    executor.execute(consumer);
    
    executor.awaitTermination(500, TimeUnit.MILLISECONDS);
    executor.shutdown();
    assertEquals(countDownLatch.getCount(), 0);
    ```
    As a result we use a lot of code to implement such a simple functionality as exchanging an element between two threads
2. Using the SynchronousQueue:
    ```java
    ExecutorService executor = Executors.newFixedThreadPool(2);
    SynchronousQueue<Integer> queue = new SynchronousQueue<>();
    
    Runnable producer = () -> {
      Integer producedElement = ThreadLocalRandom
        .current()
        .nextInt();
      try {
        queue.put(producedElement);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    };
    
    Runnable consumer = () -> {
      try {
        Integer consumedElement = queue.take();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    };
    ...
    executor.execute(producer);
    executor.execute(consumer);
    
    executor.awaitTermination(500, TimeUnit.MILLISECONDS);
    executor.shutdown();
    assertEquals(queue.size(), 0);
    ```

- With the 1st solution, based on `AtomicInteger` shared variable and `CountDownLatch`
  you must coordinate data putting and getting with both variables. 
- With `SynchronousQueue`-based solution you only manage to put and get data from the queue.

See also: [A Guide to Java `SynchronousQueue`](https://www.baeldung.com/java-synchronous-queue)

Note: A common application for `SynchronousQueue` is in work-sharing systems in which the design ensures that 
there are enough consumer threads to guarantee that producer threads can hand tasks over without having to wait. 
In this situation, it allows safe transfer of task data between threads without incurring 
the `BlockingQueue` overhead of enqueuing, then dequeuing, each task being transferred.

A thread that wants to add an element to a `SynchronousQueue` must wait until another thread is ready 
to simultaneously take it off, and the same is true — in reverse - for a thread that wants to 
take an element off the queue. So `SynchronousQueue` has the function that its name suggests: 
that of a rendezvous - a mechanism for synchronizing two threads.

### You want to use both synchronous and asynchronous messages in producer/consumer scenarios.

`TransferQueue<E>` - provides producers with a way of choosing between enqueuing data synchronously and asynchronously.

1. As an extension of `BlockingQueue`, it provides a system with the ability to throttle production by 
    blocking producers from adding indefinitely to a bounded queue.
2. In addition, however, it exposes a new method, `transfer`, which a producer can call if it wishes to block 
    until the enqueued element has been taken by a consumer - 
    a synchronous handshake like that provided by `SynchronousQueue`.

- `void transfer(E e)` - transfer the element to a consumer, waiting as long as necessary
- `boolean tryTransfer(E e);` - transfer the element to a consumer if possible
- `boolean tryTransfer(E e, long timeout, TimeUnit unit)` - transfer the element to a consumer, waiting up to the timeout.

It also exposes two helper methods that provide a rough metric of the waiting consumer count:
- `boolean hasWaitingConsumer();` - return true if there is at least one waiting consumer
- `int getWaitingConsumerCount();` - return an estimate of the number of waiting consumers

The JDK offers one implementation of `TransferQueue`, `LinkedTransferQueue`. This is an unbounded FIFO queue, 
with some interesting properties: it is lock-free, like `ConcurrentLinkedQueue` but with 
the blocking methods that that class lacks; it supports the transfer methods of its interface via a `dual queue` 
whose nodes can represent either enqueued data or outstanding dequeue requests; 
and, unusually among concurrent classes, it provides fairness without degrading performance. 
In fact, it outperforms `SynchronousQueue` even in the latter’s `nonfair` mode.

### Queue Interface Methods, what must you remember about

Each of Queue operations comes in two forms: 
- one that returns either null or false, depending on the operation to indicate failure 
- one that throws an exception.

### What functionality do `Queue` interface methods offer?

1. add an element to the tail of the queue
2. inspect the element at its head (only to retrieve)
3. remove the element at its head (retrieve and remove)

### Adding an Element to a Queue

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

### Retrieving an Element from a Queue

Throws exception:
- `E element()` retrieve but do not remove the head element
- `E remove()` retrieve and remove the head element

The methods that return null for an empty queue are:
- `E peek()` retrieve but do not remove the head element
- `E poll()` retrieve and remove the head element

### What functionality do `BlockingQueue` interface methods offer?

`BlockingQueue` - A `Queue` that additionally supports operations that wait for the queue 
to become non-empty when retrieving an element, and wait for space to become available 
in the queue when storing an element.

|         | Throws exception | Special value | Blocks indefinitely  | Times out            |
|:--------|:-----------------|:--------------|:---------------------|:---------------------|
| Insert  | add(e)           | offer(e)      | put(e)               | offer(e, time, unit) |
| Remove  | remove()         | poll()        | take()               | poll(time, unit)     |
| Examine | element()        | peek()        | not applicable       | not applicable       |

#### Retrieving or querying the contents of the blocking queue:
- `int drainTo(Collection<? super E> c)` - clear the queue into c
- `int drainTo(Collection<? super E> c, int maxElements)` - clear at most the specified number of elements into c
- `int remainingCapacity()` - return the number of elements that would be accepted without blocking, 
    or `Integer.MAX_VALUE` if unbounded

### How it works if you add an element into a bounded blocking queue that has reached capacity

`add(e)` and `offer(e)` - the methods inherited from `Queue` - fail immediately: 
- `add` by throwing an exception, 
- `offer` by returning false

The blocking methods are more patient:
- `offer(e, time, unit)` waits for a time specified using `java.util.concurrent.TimeUnit`
- `put(e)` will block indefinitely.

Timed methods such as `poll(long timeout, TimeUnit unit)` and `offer(E e, long timeout, TimeUnit unit)` 
do not throw an exception when the timeout expires because 
their design prioritizes indicating success or failure through their return value.

### Retrieving and removing the head of an empty blocking queue

You have:
- `remove()` and `poll()` are inherited from `Queue` and fail immediately, might not be the best choice
- `poll(time, unit)` waits for time out and returns value, you need to implement, how to retrigger retrieving logic again
- `put(e)` blocks until the queue is not empty, 
  but you need to make sure that you do not have multiple threads that invoke this method and are blocked.
  You could use `Semaphore` for this check.

### How do blocking queues manage blocked requests? What is the issue with it?

Some blocking queue implementations allow an argument to control how the queue will handle multiple blocked requests. 
These will occur when multiple threads attempt to remove items from an empty queue or add items to a full one. 

When the queue becomes able to service one of these requests, which one should it choose? 

The alternatives are to provide a guarantee that the queue will choose the request that has been waiting longest - 
that is, to implement a fair scheduling policy - or to allow the implementation to choose one. 

Fair scheduling sounds like the better alternative, since it avoids the possibility that an unlucky thread 
might be delayed indefinitely, but in practice, the benefits it provides are rarely important enough to 
justify incurring the large overhead that it imposes on a queue’s operation.

Example of such an argument: `fair` in `ArrayBlockingQueue` constructor:
`ArrayBlockingQueue(int capacity, boolean fair)`

### How to share a blocking queue in multithreaded contexts between producers and consumers?

1. You could pass a shared queue into a producer/consumer:
    ```java
    class Producer implements Runnable {
      private final BlockingQueue<Object> queue;
    
      public Producer(BlockingQueue<Object> queue) {
        this.queue = queue;
      }
    }
    ```
    ```java
    class Consumer implements Runnable {
      private final BlockingQueue<Object> queue;
    
      public Consumer(BlockingQueue<Object> queue) {
        this.queue = queue;
      }
    }
    ```
    ```java
    BlockingQueue<Object> queue = new LinkedBlockingQueue<>(10);
    
    var producer = new Producer(queue);
    var consumer = new Consumer(queue);
    ```
2. Wrap a queue by a concurrent task manager, that will control the state and expose its API
    [`StoppableTaskQueue`](src/main/java/com/savdev/collections/queues/StoppableTaskQueue.java)

### You use a thread-safe, blocking queue in multithreaded contexts. Are you safe?

The thread-safe (and blocking) collection itself takes care of the problems arising 
from the interaction of different threads in adding items to or removing them from the queue.

But when we go on to use the queues in a larger system, we will need to be able to stop daily task queues 
without losing task information.

Achieving graceful shutdown can often be a problem in concurrent systems.

[`StoppableTaskQueue` solves the problem of providing an orderly shutdown mechanism.](/src/main/java/com/savdev/collections/queues/StoppableTaskQueue.java)

### What must you care about when use `PriorityQueue`, its alternatives

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

### `BlockingQueue` Implementations

1. `LinkedBlockingQueue` - FIFO-ordered queue, based on a linked node structure. Not bounded, but you can set capacity.
2. `ArrayBlockingQueue` - FIFO-ordered, bounded queue, based on a circular array - 
    a linear structure in which the first and last elements are logically adjacent.
3. `PriorityBlockingQueue` - a thread-safe, blocking version of `PriorityQueue`
4. `DelayQueue` - a specialized priority queue, in which the ordering is based on the delay time for each element
5. `SynchronousQueue` - a mechanism for synchronizing two threads. In work-sharing systems in which the design ensures 
    that there are enough consumer threads to guarantee that producer threads can hand tasks over without having to wait. 
    In this situation, it allows safe transfer of task data between threads without incurring the BlockingQueue 
    overhead of enqueuing, then dequeuing, each task being transferred.



### How are `PriorityQueue` usually implemented? What data structure is behind?

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

### How is `ConcurrentLinkedQueue` implemented? What data structure is behind? 

It uses a linked structure, similar to the one in `ConcurrentSkipListSet` as the basis for skip lists 
and in `HashSet` for hash table overflow chaining. 

One of the main attractions of linked structures is that the insertion and removal operations implemented by 
pointer rearrangements are performed in constant time. 
This makes them especially useful as FIFO queue implementations, 
where these operations are always required on nodes at the ends of the structure - 
that is, nodes that do not need to be located using the slow sequential search of linked structures.

### Concurrent algorithm used by `ConcurrentLinkedQueue`

`ConcurrentLinkedQueue` uses a `CAS`-based wait-free algorithm—that is, one that guarantees that every thread 
will make progress over time, regardless of the state of other threads accessing the queue. 
It executes queue insertion and removal operations in constant time, but requires linear time to execute size. 
This is because the algorithm, which relies on cooperation between threads for insertion and removal, 
does not keep track of the queue size and has to iterate over the queue to calculate it when it is required.

### `PriorityBlockingQueue`, what must you care about?

`PriorityBlockingQueue` - its iterators are fail-fast, so they throw `ConcurrentModificationException` 
under multithread access; only if the queue is quiescent will they succeed. 
To iterate safely over a `PriorityBlockingQueue`, transfer the elements to an array and iterate over that instead.

f