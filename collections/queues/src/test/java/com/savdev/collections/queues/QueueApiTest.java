package com.savdev.collections.queues;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class QueueApiTest {

  @Test
  public void addElementToBoundedQueueException() {
    Queue<Integer> taskQueue = new LinkedBlockingQueue<>(3);
    taskQueue.add(1);
    taskQueue.add(2);
    taskQueue.add(3);
    //adding 4th element into the bounded queue
    var e = assertThrows(IllegalStateException.class, () -> {
      taskQueue.add(4);
    });
    Assertions.assertEquals(3, taskQueue.size());
    Assertions.assertEquals("Queue full", e.getMessage());
  }

  @Test
  public void addElementToBoundedQueueBoolean() {
    Queue<Integer> taskQueue = new LinkedBlockingQueue<>(3);
    Assertions.assertTrue(taskQueue.offer(1));
    Assertions.assertTrue(taskQueue.offer(2));
    Assertions.assertTrue(taskQueue.offer(3));
    //next invocation returns false:
    Assertions.assertFalse(taskQueue.offer(4));
    Assertions.assertEquals(3, taskQueue.size());
  }

  /**
   * Throws exception:
   *  - `E element()` retrieve but do not remove the head element
   *  - `E remove()` retrieve and remove the head element
   * The methods that return null for an empty queue are:
   *  - `E peek()` retrieve but do not remove the head element
   *  - `E poll()` retrieve and remove the head element
   */
  @Test
  public void retrieveAndRemoveElement() {
    //Retrieve, examine and only then remove
    Queue<Integer> taskQueue = new LinkedBlockingQueue<>(3);
    taskQueue.offer(1);
    taskQueue.offer(2);
    taskQueue.offer(3);
    var tail =  taskQueue.peek();
    if (Objects.equals(1, tail)) {
      Assertions.assertEquals(1, taskQueue.poll());
    }
    Assertions.assertEquals(2, taskQueue.size());
  }

  @Test
  public void retrieveAndRemoveElementException() {
    //Retrieve, examine and only then remove
    Queue<Integer> taskQueue = new LinkedBlockingQueue<>(3);
    taskQueue.add(1);
    var tail =  taskQueue.element();
    if (Objects.equals(1, tail)) {
      Assertions.assertEquals(1, taskQueue.remove());
    }
    Assertions.assertTrue(taskQueue.isEmpty());
    var e = assertThrows(NoSuchElementException.class, taskQueue::remove);
    Assertions.assertNotNull(e);
  }
}
