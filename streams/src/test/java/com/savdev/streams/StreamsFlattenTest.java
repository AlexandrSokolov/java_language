package com.savdev.streams;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * `Stream.flatMap()` transforms (like `Stream.map()` and flatten a stream of elements into a single stream.
 *
 *  Especially useful, when you convert/map 1 element into a collection of elements (in form of list, set, map)
 *  Get a stream from this collection and then apply `Stream.flatMap()` on it
 */
public class StreamsFlattenTest {

  @Test
  public void testFlatten() {
    var list = List.of(1, 2, 3, 4, 5);
    var r = list.stream()
      .flatMap(e -> {
        //we want to apply `int2List` only to the values > 1
        //the 1st element should be untouched
        if (e > 1) {
          //`int2List` returns list from a single element, we must convert it into Stream
          return int2List(e).stream();
        }
        return Stream.of(e);
      })
      .toList();
    Assertions.assertEquals(
      List.of(1,
        11, 12, //for 2
        11, 12, 13,  // for 3
        11, 12, 13, 14, // for 4
        11, 12, 13, 14, 15 //for 5
      ),
      r
    );
  }

  /**
   * Converts integer N into a list of elements incremented by 10 from 1 to N
   *
   * For 5, it returns: 11, 12, 13, 14, 15
   */
  private List<Integer> int2List(Integer i) {
    return IntStream.rangeClosed(1, i).boxed()
      .map(v -> v + 10)
      .toList();
  }
}
