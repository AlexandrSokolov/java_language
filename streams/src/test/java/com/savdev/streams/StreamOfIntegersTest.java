package com.savdev.streams;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamOfIntegersTest {

  /**
   * `IntStream.rangeClosed` - from one value (inclusive) to another (inclusive)
   *  `boxed` - to convert to a list of Integer
   */
  @Test
  public void testIntegerRangeInclusive2Inclusive() {
    Assertions.assertEquals(
      Stream.of(5, 6, 7, 8, 9, 10).toList(),
      IntStream.rangeClosed(5, 10).boxed().toList()
    );
  }

  /**
   * `IntStream.range` - from one value (inclusive) to another (exclusive)
   *  `boxed` - to convert to a list of Integer
   */
  @Test
  public void testIntegerRangeInclusive2Exclusive() {
    Assertions.assertEquals(
      Stream.of(5, 6, 7, 8, 9, 10).toList(),
      IntStream.range(5, 11).boxed().toList()
    );
  }

  /**
   * Conversion to objects via `mapToObj`
   *
   */
  @Test
  public void testIntegerRange2Objects() {
    Assertions.assertEquals(
      Stream.of("5", "6", "7").toList(),
      IntStream.rangeClosed(5, 7)
        .mapToObj(String::valueOf)
        .toList()
    );
  }




}
