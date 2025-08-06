package com.savdev.collections.queues;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TasksSingleConsumerManagerTest {

  private static final Logger logger = LogManager.getLogger();

  @Test
  public void testAddingEnoughCapacity() {
    var tasksManager = new TasksSingleConsumerManager<Integer>(2);
    tasksManager.addTask(1);
    tasksManager.addTask(2);
    var head = tasksManager.getHead().orElseThrow(() -> new AssertionError("head is null"));
    Assertions.assertEquals(1, head);
  }

  @Test
  public void testAddingNotEnoughCapacity() {
    var tasksManager = new TasksSingleConsumerManager<Integer>(2);
    var e = assertThrows(
      IllegalStateException.class,
      () -> tasksManager.addTasks(List.of(1, 2, 3)));
    Assertions.assertEquals(
      "Tasks queue is full. Cannot add more elements. Remaining queue capacity: 2. Number of elements to be added: 3",
      e.getMessage());
  }

  @Test
  public void testAddingNotEnoughCapacityIntoNonEmptyQueue() {
    var tasksManager = new TasksSingleConsumerManager<Integer>(2);
    tasksManager.addTask(1);
    var e = assertThrows(
      IllegalStateException.class,
      () -> tasksManager.addTasks(List.of(1, 2, 3)));
    Assertions.assertEquals(
      "Tasks queue is full. Cannot add more elements. Remaining queue capacity: 1. Number of elements to be added: 3",
      e.getMessage());
  }

  @Test
  public void testAddingNotEnoughCapacityMultiThread() {
    //each thread of 10 tries to add 3 elements into the queue with a capacity = 10
    //the 3 of 10 are successful
    //the other 7 - throw an exception
    var tasksManager = new TasksSingleConsumerManager<Integer>(10);
    var exceptionsNumber = new AtomicInteger(0);
    runInMultipleThreadsAtTheSameTime(
      10,
      /* blocking threads not expected*/ 0,
      () -> tasksManager.addTasks(List.of(1, 2, 3)),
      (e) -> {
        exceptionsNumber.getAndIncrement();
        logger.error(e::getMessage);
        Assertions.assertTrue(e.getMessage().startsWith("Tasks queue is full. Cannot add more elements."));
      }
    );
    Assertions.assertEquals(7, exceptionsNumber.get());
  }

  @Test
  public void testGetHeadFromEmptyQueue() {
    //run 5 threads to take the head from the empty queue
    //the first thread is waiting blocking (this is expected)
    //the other 4 - complete immediately
    //in test we do not wait until that blocking thread finishes.
    var tasksManager = new TasksSingleConsumerManager<Integer>(2);
    var threadsNumber =  5;
    var numberOfImmediatelyCompleted = new AtomicInteger(0);
    runInMultipleThreadsAtTheSameTime(
      threadsNumber,
      5,
      () -> {
        tasksManager.getHead();
        numberOfImmediatelyCompleted.incrementAndGet();
      },
      (e) -> {
        Assertions.fail(e.getMessage());
      }
    );
    var expectedNumberOfImmediatelyCompletedThreads = threadsNumber - 1;
    Assertions.assertEquals(expectedNumberOfImmediatelyCompletedThreads, numberOfImmediatelyCompleted.get());
  }



  private void runInMultipleThreadsAtTheSameTime(
    Integer threadsNumber,
    Integer timeoutInSeconds,
    Runnable threadTask,
    Consumer<Exception> exceptionHandler) {
    //to control and wait in the test that all non-blocking threads complete their work:
    CountDownLatch latch = new CountDownLatch(threadsNumber);
    //to control threads starting at the same time when all of them are submitted;
    final CyclicBarrier gate = new CyclicBarrier(threadsNumber);
    try (var executorService = Executors.newFixedThreadPool(threadsNumber)) {
      List<Future<Void>> futures = new ArrayList<>();
      for (int i = 0; i < threadsNumber; i++) {
        futures.add(executorService.submit(() -> {
          try {
            //to start threads at the same time
            gate.await();
            threadTask.run();
          } catch (Exception e) {
            exceptionHandler.accept(e);
          } finally {
            //let us wait when all the threads are completed
            //latch.countDown();
          }
        }, null));
      }
      futures.forEach(f -> {
        try {
          //min timeout should be 1, otherwise we get not the original exception, but `java.lang.InterruptedException`
          f.get(Math.max(timeoutInSeconds, 1), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
          f.cancel(true); // Attempt to interrupt the task
        } catch (Exception e) {
          exceptionHandler.accept(e);
        } finally {
          latch.countDown();
        }
      });
      //wait when all the threads complete
      latch.await();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
