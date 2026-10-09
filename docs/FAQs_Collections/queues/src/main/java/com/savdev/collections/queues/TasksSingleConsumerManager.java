package com.savdev.collections.queues;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class TasksSingleConsumerManager<E> {

  private static final Logger logger = LogManager.getLogger();

  public static final int QUEUE_CAPACITY = 10000;
  private final BlockingQueue<E> TASKS_QUEUE;

  //semaphore with the largest possible number of permits
  private final Semaphore GRACEFUL_SHUTDOWN = new Semaphore(Integer.MAX_VALUE);
  //either working or stopped:
  private volatile boolean isStopping;

  //number of elements, planned to be added by `addAll`, but not added yet
  private final AtomicInteger NUMBER_TO_BE_ADDED = new AtomicInteger(0);
  private final Semaphore NUMBER_TO_BE_ADDED_SEMAPHORE = new Semaphore(1);

  private final Semaphore INFINITE_WAITING_SEMAPHORE = new Semaphore(1);

  public TasksSingleConsumerManager() {
    TASKS_QUEUE = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    logger.debug(() -> "Created queue with default '" + QUEUE_CAPACITY + "' capacity.");
  }

  public TasksSingleConsumerManager(Integer queueCapacity) {
    TASKS_QUEUE = new LinkedBlockingQueue<>(queueCapacity);
    logger.debug(() -> "Created queue with custom '" + queueCapacity + "' capacity.");
  }

  /**
   * Performs atomically — it adds all of its tasks or none of them
   * @param task
   * @return
   *   - true if the task was successfully placed on the queue,
   *   - false if the queue is being shut down
   *   - if queue is full, throws an exception
   */
  public boolean addTask(E task) {
    return addTasks(List.of(task));
  }

  /**
   *
   * @param tasks
   * @return
   *   - true if the tasks in the supplied collection were successfully placed on the queue,
   *   - false if the queue is being shut down - StoppableTaskQueue is stopping/stopped
   */
  public boolean addTasks(Collection<E> tasks) {
    if (isStopping) {
      logger.debug(() -> "Tasks manager is stopping. No more elements can be added.");
      return false;
    }
    if (! GRACEFUL_SHUTDOWN.tryAcquire()) {
      logger.debug(() -> "Tasks manager is gracefully stopping and acquired all the permits. No more elements can be added.");
      return false;
    }
    try {
      checkCapacity(tasks.size());
      TASKS_QUEUE.addAll(tasks);
      NUMBER_TO_BE_ADDED.addAndGet(
        Math.negateExact(tasks.size()));
      return true;
    } finally {
      GRACEFUL_SHUTDOWN.release();
    }
  }

  private void checkCapacity(Integer tasksSize) {
    try {
      NUMBER_TO_BE_ADDED_SEMAPHORE.acquire();
      if (NUMBER_TO_BE_ADDED.get() + tasksSize > TASKS_QUEUE.remainingCapacity()) {
        var m = "Tasks queue is full. Cannot add more elements. " +
          "Remaining queue capacity: " + TASKS_QUEUE.remainingCapacity() +
          ". Number of elements to be added: " + (NUMBER_TO_BE_ADDED.get() + tasksSize);
        throw new IllegalStateException(m);
      }
      NUMBER_TO_BE_ADDED.addAndGet(tasksSize);
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    } finally {
      NUMBER_TO_BE_ADDED_SEMAPHORE.release();
    }
  }


  /**
   * Only a single consumer is expected
   * if queue is empty, it just waits and blocked
   * all the other tries to consume from an empty queue - are not expected, they are not blocked, but get finishes immediately
   *
   * @return
   *  - the head task from the queue,
   *  - null - if no task is available
   */
  public Optional<E> getHead() {
    if (INFINITE_WAITING_SEMAPHORE.tryAcquire()) {
      try {
        return Optional.of(TASKS_QUEUE.take());
      } catch (InterruptedException e) {
        throw new IllegalStateException("Could not get an element from the queue", e);
      } finally {
        INFINITE_WAITING_SEMAPHORE.release();
      }
    }
    logger.debug(() -> "Acquired by another thread");
    return Optional.empty();
  }

  /**
   *  Steps:
   *  - stop the queue,
   *  - wait for producers to finish (all pending addTasks operations are completed)
   *  - then drains the StoppableTaskQueue and returns its contents.
   *
   * @return the unprocessed tasks
   */
  public Collection<E> shutDown() {
    //set the isStopping flag so that no new addTasks method executions can begin.
    isStopping = true;
    //call acquireUninterruptibly, specifying that it needs all the permits;
    //blocks until all outstanding addTasks() calls have completed / all released by the producer threads
    GRACEFUL_SHUTDOWN.acquireUninterruptibly(Integer.MAX_VALUE);
    var returnCollection = new ArrayList<E>();
    TASKS_QUEUE.drainTo(returnCollection);
    return returnCollection;
  }
}
