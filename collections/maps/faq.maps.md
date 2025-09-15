
### Collecting streams of elements into the Map

When you collect stream(s) of elements into the Java map you need to think about if:
- keys might be duplicated
- values might contain null

1. If the elements are under your control, static and gets created programmatically, you can use:
    ```java
    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    ```
   [See `MapsConcatenationTest.testUniqueKeysNotNullableValues()`](src/test/java/com/savdev/maps/MapsConcatenationTest.java)
2. If value might contain null, you cannot use `toMap` collector, you get NPE, use more generic collector instead:
    ```java
    .collect(
        HashMap::new,
        (m,e)-> m.put(e.getKey(), e.getValue()),
        HashMap::putAll);
    ```
   [See `MapsConcatenationTest.testUniqueKeysNullableValues()`](src/test/java/com/savdev/maps/MapsConcatenationTest.java)
3. If keys might contain duplicated keys, you need to decide:

    either you can merge duplicated elements into a single one:
    ```java
    .collect(toMap(
      Map.Entry::getKey,
      Map.Entry::getValue,
      //in this test  example we merge id from one employee with the name from another
      (v1, v2) -> new Employee(v1.id(), v2.name())));
    ```
   [See `MapsConcatenationTest.testDuplicatedKeyExplicitMerging()`](src/test/java/com/savdev/maps/MapsConcatenationTest.java)
    or you create map, where value is a list of elements with:
    ```java
    .collect(groupingBy(SomeObject::someField);
    ```
    or to get a better control of values, use mapping:
    ```java
    .collect(groupingBy(
        Map.Entry::getKey,
        Collectors.mapping(
          Map.Entry::getValue,
          toList())));
    ```
   [See `MapsConcatenationTest.testDuplicatedKeyGrouping()`](src/test/java/com/savdev/maps/MapsConcatenationTest.java)