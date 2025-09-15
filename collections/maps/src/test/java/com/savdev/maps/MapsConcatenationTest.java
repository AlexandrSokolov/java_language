package com.savdev.maps;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class MapsConcatenationTest {

  public static final String NAME1 = "name1";
  public static final String NAME2 = "name2";
  public static final String NAME3 = "name3";
  public static final String NAME4 = "name4";
  public static final String DUPLICATED_NAME = "name1";

  @Test
  public void testUniqueKeysNotNullableValues() {
    var map1 = Map.of(
      NAME1, new Employee(1L, NAME1),
      NAME2, new Employee(2L, NAME2));
    var map2 = Map.of(
      NAME3, new Employee(3L, NAME3));
    var map3 = Map.of(
      NAME4, new Employee(4L, NAME4));

    var combined = Stream.of(map1, map2, map3)
      .flatMap(map -> map.entrySet().stream())
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

    Assertions.assertEquals(4, combined.size());
  }

  @Test
  public void testUniqueKeysNullableValues() {
    var map1 = new HashMap<String, Employee>();
    map1.put(NAME1, new Employee(1L, NAME1));
    map1.put(NAME2, null);

    var map2 = Map.of(
      NAME3, new Employee(3L, NAME3));
    var map3 = Map.of(
      NAME4, new Employee(4L, NAME4));

    var npe = Assertions.assertThrows(
      NullPointerException.class,
      () -> Stream.of(map1, map2, map3)
      .flatMap(map -> map.entrySet().stream())
      .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));

    Assertions.assertEquals(NullPointerException.class, npe.getClass());

    // NPE solution, if null values is acceptable:
    var combined = Stream.of(map1, map2, map3)
      .flatMap(map -> map.entrySet().stream())
      .collect(
        HashMap::new,
        (m,e)-> m.put(e.getKey(), e.getValue()),
        HashMap::putAll);
    Assertions.assertEquals(4, combined.size());
    Assertions.assertNull(combined.get(NAME2));
  }

  @Test
  public void testDuplicatedKeyDefaultMerge() {
    var map1 = Map.of(
      NAME1, new Employee(1L, NAME1),
      NAME2, new Employee(2L, NAME2));
    var map2 = Map.of(
      NAME3, new Employee(3L, NAME3));
    var map3 = Map.of(
      DUPLICATED_NAME, new Employee(4L, DUPLICATED_NAME));

    var e =  Assertions.assertThrows(
      IllegalStateException.class,
      () -> Stream.of(map1, map2, map3)
      .flatMap(map -> map.entrySet().stream())
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));

    Assertions.assertEquals(
      "Duplicate key name1 (attempted merging values Employee[id=1, name=name1] and Employee[id=4, name=name1])",
      e.getMessage());
  }

  @Test
  public void testDuplicatedKeyExplicitMerging() {
    var map1 = Map.of(
      NAME1, new Employee(1L, NAME1),
      NAME2, new Employee(2L, NAME2));
    var map2 = Map.of(
      NAME3, new Employee(3L, NAME3));
    var map3 = Map.of(
      DUPLICATED_NAME, new Employee(4L, DUPLICATED_NAME));

    var combined =  Stream.of(map1, map2, map3)
        .flatMap(map -> map.entrySet().stream())
        .collect(toMap(
          Map.Entry::getKey,
          Map.Entry::getValue,
          //in this test  example we merge id from one employee with the name from another
          (v1, v2) -> new Employee(v1.id(), v2.name())));

    //2 employees with the same keys were merged, so we combined only 3 employees
    Assertions.assertEquals(3, combined.size());
  }

  @Test
  public void testDuplicatedKeyGrouping() {
    var map1 = Map.of(
      NAME1, new Employee(1L, NAME1),
      NAME2, new Employee(2L, NAME2));
    var map2 = Map.of(
      NAME3, new Employee(3L, NAME3));
    var map3 = Map.of(
      DUPLICATED_NAME, new Employee(4L, DUPLICATED_NAME));

    Map<String, List<Employee>> combined =  Stream.of(map1, map2, map3)
      .flatMap(map -> map.entrySet().stream())
      .collect(groupingBy(
        Map.Entry::getKey,
        Collectors.mapping(
          Map.Entry::getValue,
          toList())));

    //2 employees with the same keys were merged, so we combined only 3 employees
    Assertions.assertEquals(3, combined.size());
    Assertions.assertEquals(2, combined.get(DUPLICATED_NAME).size());

  }

  record Employee(Long id, String name) {}
}
