package com.savdev.streams;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * You need to attach to each element a certain value that should be incremented for each element in the stream.
 *
 * Java Streams do not inherently provide direct access to element indices
 * during standard stream operations like `map` or `filter`.
 *
 * However, there are several common approaches to work with element indices
 */
public class StreamElementIndicesTest {

  /**
   * The key point - it is not a true stream, it is a collection in memory, but you treat it as a stream
   */
  @Test
  public void indexOfElement_in_collection() {
    var names = List.of("Alice", "Bob", "Charlie");
    var namesWithIndexes = IntStream.range(0, names.size())
      .mapToObj(i -> new Object() { // Create an anonymous object to hold element and index
        String name = names.get(i);
        int index = i;
      })
      .toList();
    Assertions.assertEquals(names.size(), namesWithIndexes.size());
    Assertions.assertEquals(names.getFirst(), namesWithIndexes.getFirst().name);
    //index of the last element:
    Assertions.assertEquals(2, namesWithIndexes.getLast().index);
  }

  /**
   * You work with a stream that has not performed the final operation,
   *  it is a true stream and you cannot calculate its size, to create range as in the `indexOfElement_in_collection`
   */
  @Test
  public void indexOfElement_in_stream() {
    AtomicInteger i = new AtomicInteger(1);
    var namesWithIndexes = Stream.of("Alice", "Bob", "Charlie")
      .map(e -> new Object() { // Again, an anonymous object for pairing
        String name = e;
        int index = i.getAndIncrement();
      })
      .toList();
    Assertions.assertEquals(3, namesWithIndexes.size());
    Assertions.assertEquals("Alice", namesWithIndexes.getFirst().name);
    //index of the last element (it was started with 1, but not with 0):
    Assertions.assertEquals(3, namesWithIndexes.getLast().index);
  }
}
