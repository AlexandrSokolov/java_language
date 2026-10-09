package com.savdev.streams;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.stream.Stream;

public class ExtendStreamElementTest {

  @Test
  public void extend_with_anonymous_object() {
    var namesWithCustomField = Stream.of("Alice", "Bob", "Charlie")
      .map(n -> new Object() { // Create an anonymous object to hold element and index
        String name = n;
        String custom = RandomStringUtils.randomAlphabetic(10);
      })
      .toList();
    Assertions.assertEquals(3, namesWithCustomField.size());
    Assertions.assertEquals("Alice", namesWithCustomField.getFirst().name);
    //index of the last element:
    Assertions.assertEquals(10, namesWithCustomField.getFirst().custom.length());
  }

  @Test
  public void extend_with_map_entry() {
    var namesWithCustomField = Stream.of("Alice", "Bob", "Charlie")
      .map(n -> new AbstractMap.SimpleEntry<>(
        n,
        RandomStringUtils.randomAlphabetic(10)))
      .toList();
    Assertions.assertEquals(3, namesWithCustomField.size());
    Assertions.assertEquals("Alice", namesWithCustomField.getFirst().getKey());
    //index of the last element:
    Assertions.assertEquals(10, namesWithCustomField.getFirst().getValue().length());
  }
}
