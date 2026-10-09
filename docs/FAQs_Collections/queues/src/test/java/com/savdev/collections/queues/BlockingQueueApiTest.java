package com.savdev.collections.queues;

import com.savdev.collections.queues.dto.PriorityTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.IllegalFormatException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class BlockingQueueApiTest {

  @Test
  public void testTimedOutApi() throws InterruptedException {
    BlockingQueue<Integer> taskQueue =  new LinkedBlockingQueue<>(2);
    Assertions.assertTrue(taskQueue.offer(1, 10L, TimeUnit.SECONDS));
    Assertions.assertTrue(taskQueue.offer(2, 10L, TimeUnit.SECONDS));

    //queue is capped, cannot add the 3rd element
    //it waits for 3 seconds, does not throw an exception, returns false:
    Assertions.assertFalse(taskQueue.offer(3, 5L, TimeUnit.SECONDS));

    var head =  taskQueue.poll(10L, TimeUnit.SECONDS);
    Assertions.assertEquals(1, head);
    Assertions.assertEquals(1, taskQueue.size());
  }

  @Test
  public void testAddException() throws InterruptedException {
    BlockingQueue<Integer> taskQueue =  new LinkedBlockingQueue<>(2);
    Assertions.assertTrue(taskQueue.offer(1, 10L, TimeUnit.SECONDS));
    Assertions.assertTrue(taskQueue.offer(2, 10L, TimeUnit.SECONDS));
    //queue is capped, cannot add the 3rd element
    //throws an exception immediately
    var e = assertThrows(
      IllegalStateException.class,
      () -> taskQueue.add(4));
    Assertions.assertEquals("Queue full", e.getMessage());
    var head =  taskQueue.poll(10L, TimeUnit.SECONDS);
    Assertions.assertEquals(1, head);
    Assertions.assertEquals(1, taskQueue.size());
  }

}
