package com.savdev.streams;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;


public class StreamsConcatenationTest {

  /**
   * If you have only 2 streams to concatenate
   */
  @Test
  public void concat2Streams() {
    var a = Stream.of("a", "b");
    var b = Stream.of("c", "d");
    var r = Stream.concat(a, b);
    Assertions.assertEquals(List.of("a", "b",   "c", "d"), r.toList());
  }

  /**
   * If you have only 3 or more streams to concatenate for simple streams
   *  `Stream.reduce` - could be a cause of StackOverflowError
   *  Accessing an element of a deeply concatenated stream can result in deep call chains, or even StackOverflowError.
   */
  @Test
  public void concat3_orMoreStreams_via_reduce() {
    var a = Stream.of("a", "b");
    var b = Stream.of("c", "d");
    var c = Stream.of("e", "f");
    var r = Stream.of(a, b, c)
      .reduce(Stream::concat)
      .orElseGet(Stream::empty);
    Assertions.assertEquals(List.of("a", "b",   "c", "d", "e", "f"), r.toList());
  }

  /**
   * If you have only 3 or more small streams (up to 32 elements) to concatenate
   *  `Stream.flatMap` - have no StackOverflowError problem
   *  This generally outperforms the solutions based on Stream.concat when each input stream contains
   *  fewer than 32 elements. As we increase the element count past 32,
   *  `flatMap` performs comparatively worse and worse as the element count rises.
   */
  @Test
  public void concat3_orMoreStreams_via_flatMap() {
    var a = Stream.of("a", "b");
    var b = Stream.of("c", "d");
    var c = Stream.of("e", "f");
    var r = Stream.of(a, b, c)
      .flatMap(s -> s);
    Assertions.assertEquals(List.of("a", "b",   "c", "d", "e", "f"), r.toList());
  }

}
