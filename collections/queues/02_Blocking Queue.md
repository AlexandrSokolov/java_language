### Queue blocking facilities
<details><summary>Show questions</summary>


`BlockingQueue<E>` - is designed primarily for use in producer/consumer scenarios.

Blocking facilities - that is, operations that wait for conditions to be right for them to execute.

</details>

### What functionality does `BlockingQueue` interface methods offer?
<details><summary>Show questions</summary>


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

</details>

### How it works if you add an element into a bounded blocking queue that has reached capacity
<details><summary>Show questions</summary>


`add(e)` and `offer(e)` - the methods inherited from `Queue` - fail immediately:
- `add` by throwing an exception,
- `offer` by returning false

The blocking methods are more patient:
- `offer(e, time, unit)` waits for a time specified using `java.util.concurrent.TimeUnit`
- `put(e)` will block indefinitely.

Timed methods such as `poll(long timeout, TimeUnit unit)` and `offer(E e, long timeout, TimeUnit unit)`
do not throw an exception when the timeout expires because
their design prioritizes indicating success or failure through their return value.

</details>

### Retrieving and removing the head of an empty blocking queue
<details><summary>Show questions</summary>


You have:
- `remove()` and `poll()` are inherited from `Queue` and fail immediately, might not be the best choice
- `poll(time, unit)` waits for time out and returns value, you need to implement, how to retrigger retrieving logic again
- `put(e)` blocks until the queue is not empty,
  but you need to make sure that you do not have multiple threads that invoke this method and are blocked.
  You could use `Semaphore` for this check.

</details>

### How do blocking queues manage blocked requests? What is the issue with it?
<details><summary>Show questions</summary>


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

</details>

### What must you care about when use methods of `BlockingQueue`?
<details><summary>Show questions</summary>


`BlockingQueue` guarantees that the queue operations of its implementations will be thread-safe and atomic.


But this guarantee doesn’t extend to the bulk operations inherited from Collection -
`addAll`, `containsAll`, `retainAll`, and `removeAll` - unless the individual implementation provides it.
So it is possible, for example, for `addAll` to fail, throwing an exception,
after adding only some of the elements in a collection.

</details>

### `BlockingQueue` Implementations
<details><summary>Show questions</summary>


1. `LinkedBlockingQueue` - FIFO-ordered queue, based on a linked node structure. Not bounded, but you can set capacity.
2. `ArrayBlockingQueue` - FIFO-ordered, bounded queue, based on a circular array -
   a linear structure in which the first and last elements are logically adjacent.
3. `PriorityBlockingQueue` - a thread-safe, blocking version of `PriorityQueue`
4. `DelayQueue` - a specialized priority queue, in which the ordering is based on the delay time for each element
5. `SynchronousQueue` - a mechanism for synchronizing two threads. In work-sharing systems in which the design ensures
   that there are enough consumer threads to guarantee that producer threads can hand tasks over without having to wait.
   In this situation, it allows safe transfer of task data between threads without incurring the BlockingQueue
   overhead of enqueuing, then dequeuing, each task being transferred.

</details>

### `ArrayBlockingQueue`, based on what data structure is it implemented?
<details><summary>Show questions</summary>


`ArrayBlockingQueue` - is based on a circular array -
a linear structure in which the first and last elements are logically adjacent.

<img src="../../docs/images/circular_array.png" alt="A circular array" width="600"/>

The position labeled “head” indicates the head of the queue; each time the head element is removed from the queue,
the head index is advanced.
Similarly, each new element is added at the tail position, resulting in that index being advanced.
When either index needs to be advanced past the last element of the array, it gets the value 0.
If the two indices have the same value, the queue is either full or empty,
so an implementation must separately keep track of the count of elements in the queue.

</details>

### `PriorityBlockingQueue`, what must you care about?
<details><summary>Show questions</summary>


`PriorityBlockingQueue` - its iterators are fail-fast, so they throw `ConcurrentModificationException`
under multithread access; only if the queue is quiescent will they succeed.
To iterate safely over a `PriorityBlockingQueue`, transfer the elements to an array and iterate over that instead.

</details>

### `Queue` implementation that supports ordering based on the delay time
<details><summary>Show questions</summary>


`DelayQueue` - is a specialized priority queue, in which the ordering is based on the delay time for each element -
the time remaining before the element will be ready to be taken from the queue.

If all elements have a positive delay time - that is, none of their associated delay times has expired -
an attempt to poll the queue will return null.

If one or more elements has an expired delay time, the one with the longest-expired delay time will be
at the head of the queue.

</details>

### You want to exchange information between a producer and a consumer. What options are available?
<details><summary>Show questions</summary>


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

</details>

### Purpose of `SynchronousQueue`
<details><summary>Show questions</summary>


See also: [A Guide to Java `SynchronousQueue`](https://www.baeldung.com/java-synchronous-queue)

Note: A common application for `SynchronousQueue` is in work-sharing systems in which the design ensures that
there are enough consumer threads to guarantee that producer threads can hand tasks over without having to wait.
In this situation, it allows safe transfer of task data between threads without incurring
the `BlockingQueue` overhead of enqueuing, then dequeuing, each task being transferred.

A thread that wants to add an element to a `SynchronousQueue` must wait until another thread is ready
to simultaneously take it off, and the same is true — in reverse - for a thread that wants to
take an element off the queue. So `SynchronousQueue` has the function that its name suggests:
that of a rendezvous - a mechanism for synchronizing two threads.

</details>

### You want to use both synchronous and asynchronous messages in producer/consumer scenarios.
<details><summary>Show questions</summary>


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
whose nodes can represent either enqueued data or outstanding deque requests;
and, unusually among concurrent classes, it provides fairness without degrading performance.
In fact, it outperforms `SynchronousQueue` even in the latter’s `nonfair` mode.

</details>

### How to share a blocking queue in multithreaded contexts between producers and consumers?
<details><summary>Show questions</summary>


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

</details>

### You use a thread-safe, blocking queue in multithreaded contexts. Are you safe?
<details><summary>Show questions</summary>


The thread-safe (and blocking) collection itself takes care of the problems arising
from the interaction of different threads in adding items to or removing them from the queue.

But when we go on to use the queues in a larger system, we will need to be able to stop daily task queues
without losing task information.

Achieving graceful shutdown can often be a problem in concurrent systems.

[`StoppableTaskQueue` solves the problem of providing an orderly shutdown mechanism.](/src/main/java/com/savdev/collections/queues/StoppableTaskQueue.java)

</details>
