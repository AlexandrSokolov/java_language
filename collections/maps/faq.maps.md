### A Map, its purpose

A Map stores key-to-value associations, or entries, in which the keys are unique. 
Its implementations provide very fast - ideally, constant-time - operations 
to look up the value corresponding to a given key.

A Map is considered as a Set of map entries (key-value pairs).
A map entry is defined by the `Map.Entry` interface.

### How to determine whether two Java maps are equal?

A `Map` can only ever be equal to another Map, and then only if they are the same size and contain equal entries.

### The hash code of a Map

The hash code of a Map is the sum of the hash codes of its entries.

### Map Interface Methods, groups

The methods of `Map` can be divided according to the two ways in which a map can be seen: 
- [as a set of entries](#-map-interface-methods-group-in-which-a-map-can-be-seen-as-a-set-of-entries) - 
  the operations to consider correspond broadly to the operations of `Iterable` and `Collection`.
- [as a lookup mechanism](#map-interface-methods-group-in-which-a-map-can-be-seen-as-a-lookup-mechanism) - 
  view-creating operations, compound operations, and factory methods.

### Map Interface Methods group, in which a map can be seen as a set of entries
- [Iterable-like Operations](#iterable-like-operations)
- [Collection-like Operations](#collection-like-operations)

### Iterable-like Operations
- `void forEach(BiConsumer<K,V> action)` - perform action on each entry in the map, 
  in the iteration order of the entry set if that is specified.
  In the same way that `Iterable::forEach`

### Collection-like Operations

#### Adding or replacing associations:
- `V put(K key, V value)` - add or replace a key-value association; 
  return the old value if the key was present, otherwise null 
- `void putAll(Map<K,V> m)` - copy all the mappings in m into the receiver
- `void replaceAll(BiFunction<K,V,V> remapper)` - replace each value with the result of invoking remapper
#### Removing associations:
- `void clear()` - remove all associations from this map
- `V remove(Object key)` - remove the association, if any, with the given key; 
  return the value with which it was associated if any, and otherwise null
#### Querying the contents of a map:
- `V get(Object k)` return the value corresponding to k, or null if k is not present as a key
- `boolean containsKey(Object k)` return true if k is present as a key
- `boolean containsValue(Object v)` return true if v is present as a value
- `int size()` return the number of associations
- `boolean isEmpty()` return true if there are no associations

### Map Interface Methods group, in which a map can be seen as a lookup mechanism
- [Providing Collection Views of the Keys, Values, or Entries](#providing-collection-views-of-the-keys-values-or-entries)
- [Compound Operations](#compound-operations)
- [Factory Methods](#factory-methods)

### Providing Collection Views of the Keys, Values, or Entries
- `Set<Map.Entry<K,V>> entrySet()` - return a Set view of the associations
- `Set<K> keySet()` - return a Set view of the keys
- `Collection<V> values()` - return a Collection view of the values

### Compound Operations

They fall into two groups, corresponding to the two different styles of locking, pessimistic and optimistic:
- [Pessimistic-style atomic operations](#pessimistic-style-atomic-operations)
- [Optimistic-style atomic operations](#optimistic-style-atomic-operations)

### Pessimistic-style atomic operations
These are essential in multithreaded environments to avoid unsafe test-then-act operation sequences.
They are also very useful as convenience methods for non-thread-safe maps.
- `V getOrDefault(Object k, V defaultValue)` - return the value to which k is mapped, 
  or defaultValue if this map contains no mapping for the key
- `V putIfAbsent(K key, V value)` - create a key-value mapping if the key is absent or mapped to null; 
  in these cases, return null, and otherwise return the current value
- `V computeIfAbsent(K k, Function<K,V> mapper)` apply the mapper function to `k` 
  and use the result to create a key-value pair, unless either `k` is currently mapped to a non-`null` value 
  or the result is `null`; 
  return the value now associated with `k`, or `null` if `k` is not now present in the map
- `V merge(K k, V newValue, BiFunction<V,V,V> remapper)` - if `k` is not present or is mapped to `null`, 
   associate it with `newValue`; otherwise, apply `remapper` to the existing value and `newValue` 
   and associate `k` with the result if it is non-`null`, or otherwise remove `k`
- `V compute(K k, BiFunction<K,V,V> remapper)` - use the result computed by `remapper` from `k` 
   and the existing value (or null if `k` is not present) to create or modify a key-value pair, and return the result; 
   if the computed result is `null`, remove any existing entry and return `null`
- `V computeIfPresent(K k, BiFunction<K,V,V> remapper)` - if k is currently mapped to `null`, return `null`; 
   otherwise, use the result computed by `remapper` from `k` and the existing value to replace that value, 
   and return the result; 
   if the computed result is `null`, remove the existing entry and return `null`

### Optimistic-style atomic operations, idea

These methods support the optimistic style of concurrent locking. 
See [“Mechanisms of concurrent collections”](todo). 
The uses of these conditional update methods are probably fairly narrow in non-concurrent systems,
but they may be convenient for some applications.

For example, a caller may fetch a key’s value, value `A`, and work on a task based on that value. 
When the task is complete, the caller will want to update the value to value `B`. 
However, a second concurrent caller might also fetch value `A` and start working, 
and eventually it may want to update the value to value `C`; 
thus, the callers’ updates are racing.

In order to avoid conflicting updates, the callers can use the replace(K,V,V) method. 
This performs an update and returns true only when the current value in the map matches the first argument. 
Otherwise, the method does nothing and returns false. 
Callers are expected to pass the previous expected value as the first argument. 
The caller that “wins” the race will see the original value, as expected, and its update will be performed. 
The caller that “loses” will see a different value from its original value, no update will be performed, 
and the method will return false. At that point the “losing” caller is expected to retry its operation, 
possibly discarding work that it might have performed based on the original value.

### Optimistic-style atomic operations
- `V replace(K k, V newValue)` - replace existing value for k, provided k is currently in the map; 
  return the old value, or null if k was not present
- `boolean replace(K k, V oldValue, V newValue)` - replace existing value for k, 
  provided it is currently mapped to oldValue; return true if oldValue was replaced
- `boolean remove(Object key, Object value)` - remove the entry for this key if it is mapped to this value, 
  returning true if the operation succeeded

### Rule of Thumb on optimistic-style atomic operations
- `replace(K,V,V)` - performs an update and returns true only when the current value 
   in the map matches the first argument. Otherwise, the method does nothing and returns false.
- `replace(K,V)` - performs an unconditional state change, 
   modifying an existing mapping without the risk of creating a new one. 
   Its complement is `putIfAbsent`, which will not modify an existing mapping but only create a new one.
- `remove(K,V)` - effects a conditional state change to a terminal state in which the mapping is removed

### Factory Methods

These methods create unmodifiable Map objects. See [`UnmodifiableMap`](todo)


### How to get the old value and put the new one into the map?
`V put(K key, V value)` add or replace a key-value association; 
return the old value if the key was present, otherwise null.

### Using null values in a Map

The contracts for `put`, `remove`, and `get` present a problem with `null`-tolerant maps: 
a returned `null` for a map key can signify:
- either that a null value had been associated with that key, or 
- that the key was not present. 

The method `containsKey` can be used to distinguish between these situations. 
This problem normally arises only with non-concurrent maps, as concurrent maps usually do not accept nulls. 

If you want to write to a null-tolerant map conditionally on a key being absent or present, 
one of the compound methods may be useful. 

However, with non-concurrent maps they cannot be relied on to act atomically, 
so they may appear to behave inconsistently if used on such a map when it is under concurrent access.

This is [the _TOCTOU_ problem](todo). 

The design decision to allow null values in maps is evaluated in [nulls](todo).

### Views of the Keys, Values, or Entries, what must you remember about when apply changes?

The view collections returned by `entrySet`, `keySet`, `values` methods are backed by the map, 
so they reflect changes to the map. 

The connection in the opposite direction is more limited: 
you can remove elements from the view collections, 
but attempting to add elements will result in an `UnsupportedOperationException`. 

### Removing a key from the `keyset` and a value from the collection returned by `values`

Removing a key from the `keyset` removes the single corresponding key-value association.

Removing a value from the collection returned by values, on the other hand,
removes only one of the associations mapping to it;
the value may still be present as part of an association with a different key.

### Using an iterator over the view

An iterator over the view will become undefined if the backing map is concurrently modified.

### Compound Operations, history

Because maps are very often used in multithreaded environments, 
the interface exposes a variety of compound actions. 
These fuse a conditional test - whether a key is absent or present, possibly with a specific value - 
either with a supplied value or with an action, represented by a lambda, to compute the value lazily. 


The primary intended use of these methods is to perform conditional transactions on the state of a concurrent map. 

Some methods of this group originally formed the `ConcurrentMap` interface 
when that was introduced in the `java.util.concurrent` package. 

Later, the introduction of default methods in Java 8 allowed the `Map` interface to be extended to include them, 
and new ones were added to both `Map` and `ConcurrentMap`.

### You want to use a default value for all the keys without storing that value in the map

If a map has values only for certain keys, the `getOrDefault` method allows you to use a default value 
for all other keys without having to store that value in the map against them all.

### You want to write something the first time you see it but not thereafter

`putIfAbsent` is useful if you want to write something the first time you see it but not thereafter. 
For example, to record the timestamp corresponding to the first occurrence of a particular kind of event, 
you could write:

```java
Map<EventKind,Long> firstOccurrenceMap = ... ;
...
firstOccurrenceMap.putIfAbsent(event.getKind(), System.currentTimeMillis());
```

### What issue exist with `putIfAbsent`?
```java
Map<EventKind,Long> firstOccurrenceMap = ... ;
...
firstOccurrenceMap.putIfAbsent(event.getKind(), System.currentTimeMillis());
```
In this example, the overhead of boxing the timestamp into a Long will be incurred for every event, not only the first. 
You will usually want to avoid this performance cost by using another compound method, `computeIfAbsent`, 
which computes the new value lazily:

```java
firstOccurrenceMap.computeIfAbsent(event.getKind(),
  key -> System.currentTimeMillis());
```

### Create a map from a key to a list of multiple values and put a value into that list associated with the key

```java
map.computeIfAbsent(key, k -> new ArrayList<V>()).add(newValue);
```
- For the missing key k, create a new empty `ArrayList<V>` and associate it with that key.
  So after `computeIfAbsent` call, you are guaranteed that `map.get(key)` returns a non-null `ArrayList<V>`.
- The result of `computeIfAbsent` is the list associated with the key. 
  Then `.add(newValue)` adds the new element to that list.

### How to apply some logic and remove entry? Give an example.

In the same way that `computeIfAbsent` is most useful where it may be necessary to add a new key, 
`computeIfPresent` can be used to remove an existing one. 
In an earlier example, we saw how to use `putIfAbsent` to produce a map 
from each kind of event to the timestamp of its first occurrence. 

Now suppose that we later want to process a different phase of the event stream so that the first event - 
and only the first - of each kind in this later phase triggers 
the writing of a log entry with the previously recorded timestamp. 

We can do that as follows:
```java
firstOccurrenceMap.computeIfPresent(event.getKind(), (kind, timestamp) -> {
  log.info("first occurrence of event " + kind + " was at " + timestamp);
  return null;
});
```
Returning `null` from the lambda ensures that the key will be removed from the map 
so that the log message can only be triggered once for each kind of event.

### How to either initialize an entry value to a given string `msg` or append a new value to an existing one?

```java
map.merge(key, msg, String::concat);
```

### What used for accumulating values based on the key’s properties and any previous value?

`Map.compute`

For instance, to map each word to the total character count that all its occurrences contribute to the text length

```java
map.compute(word, (s,i) -> s.length() + (i == null ? 0 : i));
```

Full example:
```java
Map<String, Integer> map = new HashMap<>();
map.compute("apple", (s, i) -> s.length() + (i == null ? 0 : i));
map.compute("banana", (s, i) -> s.length() + (i == null ? 0 : i));
// map ==> {banana=6, apple=5}
```

### `Map.merge` vs `Map.compute`

`compute`, is similar to `merge`, but with the difference that instead of an initial value, 
it allows the contents of the `key` to be used in a lazy computation of the new value:
```java
map.compute(word, (s,i) -> s.length() + (i == null ? 0 : i));
```
- Use `merge` when you want to add or combine values (like counters or lists).
- Use `compute` when you need full control and possibly use the key in the logic.

| Feature          | compute                              | merge                                   |
|------------------|--------------------------------------|-----------------------------------------|
| Function params  | (key, oldValue)                      | (oldValue, newValue)                    |
| When key absent  | Function called with oldValue = null | Puts the given value directly           |
| Typical use case | Complex logic based on key and value | Accumulating or combining values easily |

### `Map.compute` vs `Map.computeIfPresent` vs `Map.computeIfAbsent`
- `computeIfAbsent` - Use when you want to initialize a value if it’s missing
- `computeIfPresent` - Use when you want to update an existing value only if it’s present.
- `compute` - Use when you want full control over the value regardless of presence.

### The Interface `Map.Entry`, its methods

The members of the set returned by `entrySet` implement the interface `Map.Entry`, representing a key-value association. 
This interface exposes:
- factory methods for creating `Comparators` by key and value and 
- instance methods for accessing the components of the entry.
- An optional `setValue` method can be used to change the value in an entry if the backing map is modifiable, 
  and if so will write changes through. 

### Iteration over entries, returned by `entrySet`, recommendations

According to the Javadoc, a `Map.Entry` object obtained by iterating over a set returned by `entrySet` 
retains its connection only for the duration of the iteration, 
but in fact this is only a guaranteed minimum: 
implementation behaviors vary, some preserving the connection indefinitely. 

However, modern idioms for map processing depend less than before on durable `Map.Entry` objects: 
where in the past you might have used a `Map.Entry` set iterator to remove some entries, 
you would now be more likely to call `entrySet.removeIf`. 

Alternatively, and also for use cases in which you want to change association values, 
the instance methods of `Map` provide a variety of ways to remove mappings or alter their values.

### How to create `Map.Entry`?

You can create `Map.Entry` objects using the `Map.entry` factory method; 
this is most commonly useful for creating [unmodifiable Maps](todo).

### Hierarchy of Map Implementations

<img src="../../docs/images/Map_Implementations_Hierarchy.png" alt="Map implementations" width="600">

### SequencedMap

A `SequencedMap` is a Map that maintains its entries in a defined order.
Its keys form a `SequencedSet`.
Some implementations of `SequencedMap` (like `TreeMap`) sort their entries automatically according to a key ordering.

### NavigableMap

A `NavigableMap` is a `SequencedMap` whose keys form a `NavigableSet` so that its entries are automatically sorted 
by the key ordering and its methods can find keys and key-value pairs adjacent to a target key value.

### Collecting streams of elements into the Map

When you collect stream(s) of elements into the Java map you need to think about if:
- keys might be duplicated
- values might contain null

1. If the elements are under your control, static and gets created programmatically, you can use:
    ```java
    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    ```
   [See `MapsConcatenationTest.uniqueKeys_NotNullableValues()`](src/test/java/com/savdev/maps/MapsConcatenationTest.java)
2. If value might contain null, you cannot use `toMap` collector, you get NPE, use more generic collector instead:
    ```java
    .collect(
        HashMap::new,
        (m,e)-> m.put(e.getKey(), e.getValue()),
        HashMap::putAll);
    ```
   [See `MapsConcatenationTest.uniqueKeys_NullableValues()`](src/test/java/com/savdev/maps/MapsConcatenationTest.java)
3. If keys might contain duplicated keys, you get NPE by default.
    
    See [`.MapsConcatenationTest.duplicatedKeys_DefaultMerge()`](src/test/java/com/savdev/maps/MapsConcatenationTest.java)

    Possible solutions:

    either you can merge duplicated elements into a single one:
    ```java
    .collect(toMap(
      Map.Entry::getKey,
      Map.Entry::getValue,
      //in this test  example we merge id from one employee with the name from another
      (v1, v2) -> new Employee(v1.id(), v2.name())));
    ```
   [See `MapsConcatenationTest.duplicatedKey_ExplicitMerging()`](src/test/java/com/savdev/maps/MapsConcatenationTest.java)
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
   [See `MapsConcatenationTest.duplicatedKey_Grouping()`](src/test/java/com/savdev/maps/MapsConcatenationTest.java)