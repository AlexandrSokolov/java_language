package com.savdev.collections.queues;

import com.savdev.collections.queues.dto.PriorityTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * A concurrent task manager, that solves the problem of providing an orderly shutdown mechanism (graceful shutdown).
 * Once the shutdown method has stopped the queue and is draining or has drained it,
 *  no other threads can subsequently gain access to it to add further tasks.
 */
public class StoppableTaskQueue {
  private final int MAXIMUM_PENDING_OFFERS = Integer.MAX_VALUE;
  private final BlockingQueue<PriorityTask> taskQueue = new PriorityBlockingQueue<>();
  //semaphore with the largest possible number of permits, which in practice will never all be required.
  private final Semaphore semaphore = new Semaphore(MAXIMUM_PENDING_OFFERS);

  //either working or stopped:
  private volatile boolean isStopping;

  /**
   * Performs atomically — it adds all of its tasks or none of them
   * @param task
   * @return
   *   - true if the task was successfully placed on the queue,
   *   - false if the queue is being shut down
   */
  public boolean addTask(PriorityTask task) {
    return addTasks(List.of(task));
  }

  /**
   *
   * @param tasks
   * @return
   *   - true if the tasks in the supplied collection were successfully placed on the queue,
   *   - false if the queue is being shut down - StoppableTaskQueue is stopping/stopped
   */
  public boolean addTasks(Collection<PriorityTask> tasks) {
    if (isStopping) return false;
    if (! semaphore.tryAcquire()) {
      return false;
    } else {
      taskQueue.addAll(tasks);
      semaphore.release();
      return true;
    }
  }


  /**
   * It does not block
   *
   * @return
   *  - the head task from the queue,
   *  - null - if no task is available
   */
  public PriorityTask getFirstTask() {
    return taskQueue.poll();
  }

  /**
   *  Steps:
   *  - stop the queue,
   *  - wait for producers to finish (all pending addTasks operations are completed)
   *  - then drains the StoppableTaskQueue and returns its contents.
   *
   * @return the unprocessed tasks
   */
  public Collection<PriorityTask> shutDown() {
    //set the isStopping flag so that no new addTasks method executions can begin.
    isStopping = true;
    //call acquireUninterruptibly, specifying that it needs all the permits;
    //blocks until all outstanding addTasks() calls have completed / all released by the producer threads
    semaphore.acquireUninterruptibly(MAXIMUM_PENDING_OFFERS);
    List<PriorityTask> returnCollection = new ArrayList<>();
    taskQueue.drainTo(returnCollection);
    return returnCollection;
  }
}
