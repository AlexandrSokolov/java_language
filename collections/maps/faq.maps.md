## Java Maps, its API
- [A Map, its purpose and performance characteristics](#a-map-its-purpose-and-performance-characteristics)
- [How to determine whether two Java maps are equal?](#how-to-determine-whether-two-java-maps-are-equal)
- [The hash code of a Map](#the-hash-code-of-a-map)
- [Groups of `Map` Interface Methods](#groups-of-map-interface-methods)
- [Iterable-like operations of `Map` interface](#iterable-like-operations-of-map-interface)
- [Collection-like operations of `Map` interface](#collection-like-operations-of-map-interface)
- [Compound operations of `Map` interface](#compound-operations-of-map-interface)
- [Pessimistic-style atomic operations](#pessimistic-style-atomic-operations)
- [Optimistic-style atomic operations, idea](#optimistic-style-atomic-operations-idea)
- [Optimistic-style atomic operations](#optimistic-style-atomic-operations)
- [Rule of Thumb on optimistic-style atomic operations](#rule-of-thumb-on-optimistic-style-atomic-operations)
- [Views methods of `Map` interface](#views-methods-of-map-interface)
- [Factory Methods of `Map` interface](#factory-methods-of-map-interface)
- [How to get the old value and put the new one into the map?](#how-to-get-the-old-value-and-put-the-new-one-into-the-map)
- [Using null values in a Map](#using-null-values-in-a-map)
- [Views, what must you remember about when apply changes?](#views-what-must-you-remember-about-when-apply-changes)
- [Removing a key from the set returned by `keySet`](#removing-a-key-from-the-set-returned-by-keyset)
- [Removing a value from the collection returned by `values`](#removing-a-value-from-the-collection-returned-by-values)
- [Using an iterator over the view](#using-an-iterator-over-the-view)
- [Compound Operations, history](#compound-operations-history)
- [You want to use a default value for all the keys without storing that value in the map](#you-want-to-use-a-default-value-for-all-the-keys-without-storing-that-value-in-the-map)
- [You want to write something the first time you see it but not thereafter](#you-want-to-write-something-the-first-time-you-see-it-but-not-thereafter)
- [What issue exist with `putIfAbsent`?](#what-issue-exist-with-putifabsent)
- [Create a map from a key to a list of multiple values and put a value into that list associated with the key](#create-a-map-from-a-key-to-a-list-of-multiple-values-and-put-a-value-into-that-list-associated-with-the-key)
- [How to apply some logic and remove entry? Give an example](#how-to-apply-some-logic-and-remove-entry-give-an-example)
- [How to either initialize an entry value to a given string `msg` or append a new value to an existing one?](#how-to-either-initialize-an-entry-value-to-a-given-string-msg-or-append-a-new-value-to-an-existing-one)
- [What used for accumulating values based on the key’s properties and any previous value?](#what-used-for-accumulating-values-based-on-the-keys-properties-and-any-previous-value)
- [`Map.merge` vs `Map.compute`](#mapmerge-vs-mapcompute)
- [`Map.compute` vs `Map.computeIfPresent` vs `Map.computeIfAbsent`](#mapcompute-vs-mapcomputeifpresent-vs-mapcomputeifabsent)
- [`Map.Entry` interface, its methods](#mapentry-interface-its-methods)
- [Iteration over entries, returned by `entrySet`, recommendations](#iteration-over-entries-returned-by-entryset-recommendations)
- [How to create `Map.Entry`?](#how-to-create-mapentry)

## Java Maps interfaces and implementations
- 

### A Map, its purpose and performance characteristics

A Map stores key-to-value associations, or entries, in which the keys are unique. 
Its implementations provide very fast - ideally, constant-time - operations 
to look up the value corresponding to a given key.

A Map is considered as a Set of map entries (key-value pairs).
A map entry is defined by the `Map.Entry` interface.

### How to determine whether two Java maps are equal?

A `Map` can only ever be equal to another Map, and then only if they are the same size and contain equal entries.

### The hash code of a Map

The hash code of a Map is the sum of the hash codes of its entries.

### Groups of `Map` Interface Methods
- [Iterable-like operations](#iterable-like-operations-of-map-interface)
- [Collection-like operations of `Map` interface](#collection-like-operations-of-map-interface)
- [Compound operations of `Map` interface](#compound-operations-of-map-interface)
- [Views methods of `Map` interface](#views-methods-of-map-interface)
- [Factory Methods of `Map` interface](#factory-methods-of-map-interface)

Mnemonic by first letters:
`I-C-C-V-F` -> _I Collect Cool View Factories._
(Imagine a collector who loves cool views and factories.)

### Iterable-like operations of `Map` interface
- `void forEach(BiConsumer<K,V> action)` - perform action on each entry in the map, 
  in the iteration order of the entry set if that is specified.
  In the same way that `Iterable::forEach`

### Collection-like operations of `Map` interface

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

### Compound operations of `Map` interface

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

### Views methods of `Map` interface
- `Set<Map.Entry<K,V>> entrySet()` - return a Set view of the associations
- `Set<K> keySet()` - return a Set view of the keys
- `Collection<V> values()` - return a Collection view of the values

### Factory Methods of `Map` interface

TODO add factory methods for all maps classes
These methods create unmodifiable Map objects. See [`UnmodifiableMap`](todo)
static <K,V> LinkedHashMap<K,V> newLinkedHashMap(int numMappings)
Map.of(...)
Map.ofEntries(...)
Map.copyOf(Map<? extends K, ? extends V> map)

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

### Views, what must you remember about when apply changes?

The view collections returned by `entrySet`, `keySet`, `values` methods are backed by the map, 
so they reflect changes to the map. 

The connection in the opposite direction is more limited: 
you can remove elements from the view collections, 
but attempting to add elements will result in an `UnsupportedOperationException`. 

### Removing a key from the set returned by `keySet`

Removing a key from the `keyset` removes the single corresponding key-value association.

### Removing a value from the collection returned by `values`
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

### How to apply some logic and remove entry? Give an example

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

### `Map.Entry` interface, its methods

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

### Constructors for Map implementations

`HashMap`, `LinkedHashMap`, and `WeakHashMap` have other constructors for configuration purposes, 
but these have been replaced in practice by factory methods, 
for the reasons described in ["creation of `HashSet`"](../sets/faq.sets.md#creation-of-hashset).

`static <K,V> HashMap<K,V> HashMap::newHashMap(int numElements)`
static <K,V> WeakHashMap<K,V> newWeakHashMap(int numMappings)

### `HashMap`, its performance

`HashMap` implemented based on hash tables with the related performance.

In particular, `HashMap` provides constant-time performance for `put` and `get`. 
Although in principle constant-time performance is only attainable with no collisions, 
it can be closely approached by the use of rehashing to control the load and 
thereby to minimize the number of collisions.

Iteration over a collection of keys or values requires time proportional 
to the capacity of the map plus the number of key-value mappings that it contains. 

The iterators are fail-fast.


### Types of references in Java

- [strong references](#problem-of-strong-references-in-java-give-a-code-example)
- weak references
- [soft references]


| Feature           | Strong Reference                                        | Weak Reference                                            | Soft Reference                                                  |
|-------------------|---------------------------------------------------------|-----------------------------------------------------------|-----------------------------------------------------------------|
| **Definition**    | Default reference type in Java                          | Reference that does not prevent GC                        | Reference that does not prevent GC but is kept longer than weak |
| **GC Behavior**   | Object is **never collected** while strongly referenced | Object is collected **as soon as no strong refs exist**   | Object is collected **only under memory pressure**              |
| **Use Case**      | Normal object usage                                     | Caches, maps where entries should disappear automatically | Memory-sensitive caches                                         |
| **Example Class** | `Object obj = new Object();`                            | `WeakReference<T>` or `WeakHashMap`                       | `SoftReference<T>`                                              |
| **Survival**      | Survives until all strong refs are gone                 | Very short-lived after strong refs are gone               | Longer-lived than weak, but not guaranteed                      |
| **Risk**          | Memory leaks if not cleared                             | Possible frequent GC, objects disappear quickly           | May still cause OOM if cache grows too large                    |


### Problem of strong references in Java, give a code example

Strong references in Java are the default type of reference,
and they can lead to memory leaks if objects are unintentionally kept alive because something still holds 
a strong reference to them. 
The garbage collector (GC) cannot reclaim memory for an object as long as there 
is at least one strong reference pointing to it.

Why is this a problem?

If you store objects in collections like `Map` or `List` and forget to remove them when they are no longer needed, 
they remain strongly referenced.
This prevents GC from freeing memory, even if the object is logically "unused".
In long-running applications (e.g., servers), this can cause `OutOfMemoryError`.

### Memory leak example related to strong references usage

```java
public class StrongReferenceLeak {
  static Map<String, byte[]> cache = new HashMap<>();

  public static void main(String[] args) {
    for (int i = 0; i < 100000; i++) {
      // Each entry holds 1 MB
      cache.put("key" + i, new byte[1024 * 1024]); // 1 MB per entry
      System.out.println("Added: " + i);
    }
    System.out.println("Cache size after loop: " + cache.size());
  }
}
```
What happens here?
- We keep adding large byte arrays to a HashMap.
- The cache map holds strong references to all arrays.
- GC cannot reclaim any of them because the map still references them.
- Eventually, the JVM runs out of heap space - `java.lang.OutOfMemoryError`.

### How to solve the problem with strong references? Give an example

- Use `WeakReference` or [`WeakHashMap`] for caches where objects can be garbage collected 
  when not strongly referenced elsewhere.
- Or implement proper eviction strategies (e.g., LRU cache).

```java
public class WeakReferenceExample {
  public static void main(String[] args) {
    Map<String, byte[]> cache = new WeakHashMap<>();

    for (int i = 0; i < 100000; i++) {
      String key = new String("key" + i); // Important: new String ensures no interned strong reference
      cache.put(key, new byte[1024 * 1024]); // 1 MB per entry
      System.out.println("Added: " + i);
      // Suggest GC occasionally
      if (i % 1000 == 0) {
        System.gc();
      }
    }
    System.out.println("Cache size after loop: " + cache.size());
  }
}
```
Key Points:
- WeakHashMap uses weak references for keys. 
- When a key is no longer strongly referenced elsewhere, the entry is eligible for GC.
- The `byte[]` values will also be collected because the map entry disappears when the key is collected.
- If you use interned strings (like `"key" + i` without `new String()`),
  they stay strongly referenced in the string pool - entries won’t be cleared.


### `WeakHashMap`, its purpose

Most maps keep ordinary (“strong”) references to all the objects they contain. 
That means that even when a key has become unreachable by any means other than through the map itself, 
its mapping cannot be garbage collected.
So preserving entries unnecessarily has the potential to degrade garbage collection performance and create memory leaks. 
The idea behind `WeakHashMap` is to avoid this situation by allowing a mapping and its referenced objects 
to be reclaimed once the key is no longer reachable in the application.

### `WeakHashMap` implementation details

Internally, `WeakHashMap` holds references to its key objects through objects of the class `java.lang.ref.WeakReference`. 
A WeakReference introduces an extra level of indirection in reaching an object. 

A weak reference does not protect an object from garbage collection; then it is eligible for garbage collection. 
If an object in map is not reachable with a normal reference from anywhere else in the application, 
then it is eligible for garbage collection.
The map detects this and removes the entry, 
with the effect that the entire map entry will seem to have spontaneously disappeared.

The iterators over collections of keys and values returned by WeakHashMap are fail-fast.

### What is a `WeakHashMap` good for?

Imagine you have a program that allocates some transient system resource - a buffer, 
for example - on request from a client. 
Besides passing a reference to the resource back to the client, 
your program might also need to store information about it locally - for example, 
associating the buffer with the client that requested it. 
That could be implemented by means of a map from resource to client objects. 
But with a strong reference, then even after the client has disposed of the resource, 
the map will still hold a reference that will prevent the resource object from being garbage collected. 
Memory will gradually be used up by resources that are no longer in use. 
On the other hand, if the reference is weak, held by a `WeakHashMap`, 
the garbage collector will be able to reclaim the objects once they are no longer strongly referenced, 
so the memory leak is prevented.

### What a `WeakHashMap` might be not the best choice for?

A more general use is in those applications - for example, caches - where you don’t mind information disappearing 
if memory is low. `WeakHashMap` isn’t perfect for this purpose. 
1. One of its drawbacks is that it weakly references the map’s keys rather than its values, 
    which usually occupy much more memory; so even after the garbage collector has reclaimed a key, 
    the real benefit in terms of available memory will not be experienced until the map has removed the stale entry. 
2. A second drawback is that weak references are too weak: 
   the garbage collector is liable to reclaim a weakly reachable object at any time, 
   and the programmer cannot influence this in any way. 

A sister class of `WeakReference`, `java.lang.ref.SoftReference`, is treated differently: 
the garbage collector postpones reclaiming these until it is under severe memory pressure. 
A `SoftReference`-based map that will work better as a cache.

### `SoftReference`-based map as a cache example

```java
public class SoftReferenceCache {
  private final Map<String, SoftReference<byte[]>> cache = new HashMap<>();

  public void put(String key, byte[] data) {
    cache.put(key, new SoftReference<>(data));
  }

  public byte[] get(String key) {
    SoftReference<byte[]> ref = cache.get(key);
    return (ref != null) ? ref.get() : null;
  }

  public static void main(String[] args) {
    SoftReferenceCache softCache = new SoftReferenceCache();

    for (int i = 0; i < 100000; i++) {
      softCache.put("key" + i, new byte[1024 * 1024]); // 1 MB per entry
      if (i % 1000 == 0) {
        System.out.println("Added: " + i + ", Cache size: " + softCache.cache.size());
        System.gc(); // Suggest GC
      }
    }
    // Try to retrieve some data
    byte[] data = softCache.get("key500");
  }
}
```
- `SoftReference` objects allow the GC to reclaim memory only under memory pressure.
  This means cached objects stay longer than weak references, but they are not guaranteed to persist forever.
  Ideal for memory-sensitive caches where you prefer to keep data if possible, but avoid OutOfMemoryError.
- If the JVM has enough memory, the cache entries remain.
  When memory is low, GC clears `SoftReference` objects first before throwing `OutOfMemoryError`.

### What problem exists with `Map<String, SoftReference<byte[]>>` cache? Solution

The map key is a strong reference, which means the entry itself will remain in the `HashMap` even if the value
(wrapped in `SoftReference`) is cleared by the GC.
This does not prevent the value from being garbage collected, 
because the GC only cares about strong references to the actual object (`the byte[]`), not the wrapper (`SoftReference`). 
However, the map will still hold the key and the empty `SoftReference` object, 
which can lead to a _"stale entry" problem_ - the cache grows with dead references.

Why this happens:
- `SoftReference<byte[]>` can be cleared by GC under memory pressure.
- But the HashMap still holds the key and the SoftReference instance.
- So the map size doesn’t shrink automatically - potential memory overhead.

How to fix it:
You need a cleanup mechanism to remove entries whose `SoftReference.get()` returns null. For example:
```java
public class SoftReferenceCache {
  private final Map<String, SoftReference<byte[]>> cache = new HashMap<>();

  public void put(String key, byte[] data) {
    cache.put(key, new SoftReference<>(data));
  }

  public byte[] get(String key) {
    SoftReference<byte[]> ref = cache.get(key);
    return (ref != null) ? ref.get() : null;
  }

  public void cleanup() {
    Iterator<Map.Entry<String, SoftReference<byte[]>>> it = cache.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, SoftReference<byte[]>> entry = it.next();
      if (entry.getValue().get() == null) {
        it.remove(); // Remove stale entry
      }
    }
  }

  public static void main(String[] args) {
    SoftReferenceCache softCache = new SoftReferenceCache();

    for (int i = 0; i < 100000; i++) {
      softCache.put("key" + i, new byte[1024 * 1024]); // 1 MB per entry
      if (i % 1000 == 0) {
        System.out.println("Added: " + i + ", Cache size: " + softCache.cache.size());
        System.gc();
        softCache.cleanup(); // Remove cleared references
      }
    }
    System.out.println("Final cache size: " + softCache.cache.size());
  }
}
```
Now the cache stays clean because we remove entries whose values have been cleared by GC.

###

```java
public class SoftReferenceCache {
  private final Map<String, SoftValue> cache = new HashMap<>();
  private final ReferenceQueue<byte[]> refQueue = new ReferenceQueue<>();

  // Custom SoftReference that remembers its key
  private static class SoftValue extends SoftReference<byte[]> {
    private final String key;

    SoftValue(String key, byte[] value, ReferenceQueue<byte[]> queue) {
      super(value, queue);
      this.key = key;
    }
  }

  public void put(String key, byte[] data) {
    cleanUp(); // Remove cleared references before adding new ones
    cache.put(key, new SoftValue(key, data, refQueue));
  }

  public byte[] get(String key) {
    SoftValue ref = cache.get(key);
    return (ref != null) ? ref.get() : null;
  }

  private void cleanUp() {
    SoftValue ref;
    while ((ref = (SoftValue) refQueue.poll()) != null) {
      cache.remove(ref.key);
      System.out.println("Cleaned up key: " + ref.key);
    }
  }

  public static void main(String[] args) {
    SoftReferenceCache softCache = new SoftReferenceCache();

    for (int i = 0; i < 100000; i++) {
      softCache.put("key" + i, new byte[1024 * 1024]); // 1 MB per entry
      if (i % 1000 == 0) {
        System.out.println("Added: " + i + ", Cache size: " + softCache.cache.size());
        System.gc(); // Suggest GC
      }
    }

    System.out.println("Final cache size: " + softCache.cache.size());
  }
}
```

### What issues exist with SoftReference?

Why libraries often avoid SoftReference?
- **Unpredictable GC behavior**: JVM decides when to clear soft references, 
  which can lead to inconsistent cache performance.
- Libraries like `Caffeine` and `Guava` prefer explicit eviction policies (LRU, size-based) for better control.

### `WeakHashMap` performance

`WeakHashMap` performs similarly to `HashMap`, 
though more slowly because of the overheads of the extra level of indirection for keys. 
The cost of clearing out unwanted key-value associations before each operation is proportional 
to the number of associations that need to be removed because the garbage collector has reclaimed the key. 

### Solutions you can use for cache?

- Guava `Cache` (by Google).
  Provides `CacheBuilder` with options for automatic eviction and memory-sensitive behavior.
  While it doesn’t use `SoftReference` by default, you can configure it with `weakKeys()` or `softValues()`
  for GC-friendly caches.
- `Ehcache` - caching library for Java applications. 
  Supports various eviction policies and can be tuned for memory-sensitive caching.
- `Caffeine` - 
  A high-performance caching library that uses size-based eviction and adaptive strategies.
  Does not rely on SoftReference but provides better predictability and performance than GC-based approaches.
- [Java’s Built-in SoftReference](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/ref/SoftReference.html)
  You can implement your own cache using SoftReference and ReferenceQueue (as shown in our example).
  This is the simplest approach but requires manual cleanup logic.

### `IdentityHashMap`, its purpose

`IdentityHashMap` for the equivalence relation on its keys, it uses the identity relation. 
In other words, every physically distinct object is a distinct key.

An `IdentityHashMap` differs from an ordinary `HashMap` in that two keys are considered `equal` 
only if they are physically the same object: `identity`, rather than `equals`, is used for key comparison. 
That sets the contract for `IdentityHashMap` at odds with the contract for the interface it implements, namely `Map`, 
which specifies that equality should be used for key comparison. 

The Javadoc for `Map` assumes that the equivalence relation for maps is always defined by the `equals` method, 
whereas in fact `IdentityHashMap` uses a different relation, 
as do all implementations of `NavigableMap`

The main purpose of `IdentityHashMap` is to support operations in which a graph 
has to be traversed and information stored about each node. 
Serialization is one such operation. 
The algorithm used for traversing the graph must be able to check, for each node it encounters, 
whether that node has already been seen; 
otherwise, graph cycles could be followed indefinitely. 
For cyclic graphs, we must use identity rather than equality to check whether nodes are the same. 
Calculating equality between two graph node objects requires calculating the equality of their fields, 
which in turn means computing all their successors—and we are back to the original problem. 
An `IdentityHashMap`, by contrast, will report a node as being present only 
if that same node has previously been put into the map.

Use Case: 
- Object Graph Serialization
- Detect Cycles in Object Graph

### EnumMap

Implementing a mapping from an enumerated type is straightforward and very efficient.
In an array implementation, the `ordinal` value of each enumerated type constant can serve 
as the index of the corresponding value. 
The basic operations of `get` and `put` can be implemented as array accesses, in constant time. 
An iterator over the key set takes time proportional to the number of constants 
in the enumerated type and returns the keys in their natural order (the order in which the enum constants are declared). 

Iterators over the collection views of this class are weakly consistent.

### UnmodifiableMap, idea, creation, performance

The properties of the members of this family are described in the Javadoc for Map:
- They are unmodifiable; keys and values cannot be added or removed.
  Calling any mutator method will always cause `UnsupportedOperationException` to be thrown.
- They are null-hostile. Attempts to create them with null keys or values will result in a `NullPointerException`.
- They reject duplicate keys at creation time.
  Duplicate keys passed to a factory method result in an `IllegalArgumentException`.

Creating unmodifiable maps:
- `Map.of` - limited number of entries
- `Map.ofEntries` and `Map.entry` - unlimited number of entries
- `Map.copyOf`

Like the other unmodifiable collections, unmodifiable maps are backed by fixed-length arrays. 
They have the advantages over hashed collections of:
- reduced memory footprint
- faster iteration  
- better spatial locality. 

With a `hashCode` function that provides good distribution, lookup is `O(1)`. 
As with `UnmodifiableSet`, and for the same reason, 
the order of iteration over the entry or key set is randomly determined for each virtual machine instance.

### Hierarchy of `SequencedMap` and related types 

- [`SequencedMap`](#sequencedmap)
- [`NavigableMap`](#navigablemap)

<img src="../../docs/images/SequencedMap_Hierarchy.png" alt="SequencedMap and related types" width="600">

### SequencedMap

A `SequencedMap` is a Map that maintains its entries in a defined order.

### `SequencedMap` API

#### Adding or updating entries
- `V  putFirst(K k, V v)` - insert the given mapping, or updates it if it is already present
- `V  putLast(K k, V v)` - insert the given mapping, or updates it if it is already present

#### Inspecting entries
- `Map.Entry<K,V> firstEntry()`
- `Map.Entry<K,V> lastEntry()`

#### Removing entries
- `Map.Entry<K,V> pollFirstEntry()` remove and return the first entry, or null if the map is empty
- `Map.Entry<K,V> pollLastEntry()` remove and return the last entry, or null if the map is empty

#### View-generating methods
- `SequencedMap<K,V> reversed()` return a reverse-ordered view of the map
- `SequencedSet<Map.Entry<K,V>> sequencedEntrySet()` return a `SequencedSet` view of the map’s entrySet
- `SequencedSet<K> sequencedkeySet()` return a `SequencedSet` view of the map’s keySet
- `SequencedCollection<V> sequencedValues()` return a `SequencedCollection` view of the map’s values collection

### `SequencedMap` view-generating methods
[See view-generated methods](#view-generating-methods) 

### Direct `SequencedMap` implementations
- [`LinkedHashMap`](#linkedhashmap)

### `LinkedHashMap`

Like `LinkedHashSet`, the class `LinkedHashMap` refines the contract of its parent class, `HashMap`, 
by guaranteeing the order in which iterators return its elements. 
Also like `LinkedHashSet`, it implements the sequenced subinterface (`SequencedMap`) of its main interface.

### `LinkedHashMap` iteration, compare with `LinkedHashSet`

Unlike `LinkedHashSet`, however, `LinkedHashMap` offers a choice of iteration orders; 
elements can be returned either:
- in the order in which they were inserted in the map—the default or
- in the order in which they were accessed (from least recently to most recently accessed). 

 
An access-ordered `LinkedHashMap` is created by supplying an argument of true for the last parameter of the constructor:
```java
public LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder)
```
Supplying false will give an insertion-ordered map. 
The other constructors, which are just like those of `HashMap`, also produce insertion-ordered maps. 
As with `LinkedHashSet`, iteration over a `LinkedHashMap` takes time proportional 
only to the number of elements in the map, not its capacity.

### Iteration over a collection of keys or values

Iteration over a collection of keys or values returned by a `LinkedHashMap` is linear in the number of elements. 
The iterators over such collections are fail-fast.

### An access-ordered `LinkedHashMap`, its purpose

Access-ordered maps are especially useful for constructing least recently used (LRU) caches. 
A cache is an area of memory that stores frequently accessed data for fast access. 
In designing a cache, the key issue is
the choice of algorithm that will be used to decide what data to remove in order to conserve memory. 
When an item from a cached data set needs to be found, the cache will be searched first. 
Typically, if the item is not found in the cache, it will be retrieved from the main store and added to the cache. 
But the cache cannot be allowed to continue growing indefinitely, 
so a strategy must be chosen for removing the least useful item from the cache when a new one is added. 
If the strategy chosen is LRU, the entry removed will be the one least recently used. 
This simple strategy is suitable for situations in which access of an element 
increases the probability of further access in the near future of the same element. 
**Its simplicity and speed have made it the most popular caching strategy**.


`LinkedHashMap` exposes a method specifically designed to make it easy to use as an LRU cache:
```java
protected boolean removeEldestEntry(Map.Entry<K,V> eldest);
```
The name of this method is misleading. It is not usually used to itself modify the map: 
instead, it is called from within the code of `put` or `putAll` each time an element is added. 
The value it returns is an indication to the calling method of whether it should remove the first entry in the map - 
that is, the one least recently accessed 
(or, if some entries have never been accessed, the one amongst these that was least recently added).

The implementation in `LinkedHashMap` simply returns `false` - an indication to the calling method 
that no action is needed. 
But you can subclass `LinkedHashMap` and override `removeEldestEntry` to return `true` under specific circumstances.

### Caches strategies
- [FIFO](#fifo-cache-example)
- [LRU - ](#lru-cache-example)
- [MRU - discards the most recently used entry](#mru-cache-example)

### FIFO cache example

### LRU Cache example

### LRU vs FIFO caches, give an example

_LRU_ cache example:
```java
class BoundedSizeMap<K,V> extends LinkedHashMap<K,V> {
  private final int maxEntries;
  public BoundedSizeMap(int maxEntries) {
    //create access-ordered map:
    super(16, 0.75f, true);
    this.maxEntries = maxEntries;
  }
  protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
    return size() > maxEntries;
  }
}
```

Because in an insertion-ordered `LinkedHashMap` the eldest entry will be the one 
that was least recently added to the map, overriding `removeEldestEntry` as shown here will implement a FIFO strategy. 
_FIFO_ caching has often been used in preference to _LRU_ because it is much simpler to
implement in maps that do not offer access ordering. 

However, _LRU_ is usually more effective than _FIFO_, 
because the reduced cost of cache refreshes outweighs the overhead of maintaining access ordering.

### MRU cache example

In some applications recent access to an entry reduces rather than increases the likelihood 
of it being accessed again soon. In that case, the best strategy is _most recently used_ (MRU), 
which discards the most recently used entry. 
This is straightforward to implement in a `SequencedMap`, 
which exposes a method `pollLastEntry` analogous to `SequencedSet.removeLast`:

```java
protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
  if (size() > maxEntries) {
    pollLastEntry();
  }
  return false;
}
```

### NavigableMap

A `NavigableMap` is a `SequencedMap` whose keys form a `NavigableSet` so that its entries are automatically sorted 
by the key ordering and its methods can find keys and key-value pairs adjacent to a target key value.

The interface NavigableMap adds to SequencedMap a guarantee that its iterator will traverse the 
map in ascending key order and, like NavigableSet, adds further methods to 
find the entries adjacent to a target key value. 
Also like NavigableSet, NavigableMap extends and in effect replaces an older interface, 
SortedMap, which imposes an ordering on its keys: either their natural ordering or that of a Comparator. 
The equivalence relation on the keys of a NavigableMap is again defined by the ordering relation; 
two keys that compare as equal—​that is, 
for which the comparison method returns 0—will be regarded as duplicates by a NavigableMap

### `SequencedMap::putFirst` for `TreeMap`

In the case of an internally ordered implementation like `TreeMap`, 
both `SequencedMap::putFirst` and `SequencedMap::putLast` methods will throw `UnsupportedOperationException`.

### `NavigableMap` methods API

#### Retrieving the Comparator
- `Comparator<? super K> comparator()` return the map’s key comparator if it has been given one, 
  instead of relying on the natural ordering of the keys; otherwise, return null

#### Getting Range Views
- `SortedMap<K,V> subMap(K fromKey, K toKey)`
  return a view of the portion of this map whose keys range from fromKey, inclusive, to toKey, exclusive 
- `SortedMap<K,V> headMap(K toKey)`	return a view of the portion of this map whose keys are strictly less than toKey
- `SortedMap<K,V> tailMap(K fromKey)` 
  return a view of the portion of this map whose keys are greater than or equal to fromKey
- `NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive)`
  return a view of the portion of this map whose keys range from fromKey to toKey
- `NavigableMap<K,V> headMap(K toKey, boolean inclusive)`
  return a view of the portion of this map whose keys are less than (or equal to, if inclusive is true) toKey
- `NavigableMap<K,V> tailMap(K fromKey, boolean inclusive)`	
  return a view of the portion of this map whose keys are greater than (or equal to, if inclusive is true) fromKey

#### Getting Closest Matches
- `Map.Entry<K,V> ceilingEntry(K Key)` return a key-value mapping associated with 
  the least key greater than or equal to the given key, or null if there is no such key
- `K ceilingKey(K Key)`	return the least key greater than or equal to the given key, or null if there is no such key
- `Map.Entry<K,V> floorEntry(K Key)` return a key-value mapping associated with
  the greatest key less than or equal to the given key, or null if there is no such key 
- `K floorKey(K Key)` return the greatest key less than or equal to the given key, or null if there is no such key
- `Map.Entry<K,V> higherEntry(K Key)` return a key-value mapping associated with 
  the least key strictly greater than the given key, or null if there is no such key
- `K higherKey(K Key)` return the least key strictly greater than the given key, or null if there is no such key
- `Map.Entry<K,V> lowerEntry(K Key)` return a key-value mapping associated with 
  the greatest key strictly less than the given key, or null if there is no such key
- `K lowerKey(K Key)` return the greatest key strictly less than the given key, or null if there is no such key

#### Other views:
- `NavigableMap<K,V> descendingMap()` return a reverse-order view of the map
- `NavigableSet<K> descendingKeySet()` return a reverse-order key set
- `NavigableSet<K> navigableKeySet()` return a forward-order key set

### NavigableMap methods that return range views

Each of the methods in this group appears in two overloads:
- one inherited from `SortedMap` and returning a half-open SortedMap view, and 
- one defined in `NavigableMap` and returning a `NavigableSet` view that can be open, half-open, or closed 
  according to the user’s choice - provide more flexibility. 

### NavigableMap methods that return entries and key views in different orders
- `NavigableMap<K,V> descendingMap()` return a reverse-order view of the map
- `NavigableSet<K> descendingKeySet()` return a reverse-order key set
- `NavigableSet<K> navigableKeySet()` return a forward-order key set

### Why `keySet` method, inherited from `Map`, could not simply be overridden using a covariant return type to return a `NavigableSet`?

Indeed, the platform implementations of `NavigableMap::keySet` do return a `NavigableSet`. 
But there is a compatibility concern: 
if `TreeMap::keySet` were to have its return type changed from `Set` to `NavigableSet`, 
any existing `TreeMap` subclasses that override that method would fail to compile 
unless they too changed their return type.
[See _Maintain Binary Compatibility_](todo)

### NavigableMap implementations
- [`TreeMap`](#treemap)

### `TreeMap`

In fact, the internal representation of a `TreeSet` is just a `TreeMap` in which every key 
is associated with the same standard value, 
so the explanation of the mechanism and performance of red-black trees 
[given there](../sets/faq.sets.md#what-data-structure-is-backed-by-treeset-its-properties)
applies equally here.

### `TreeMap` construction

The constructors for `TreeMap` include, besides the standard ones, 
one that allows you to supply a `Comparator` and one that allows you to create a `TreeMap` from another `NavigableMap` 
(strictly speaking, from a `SortedMap`), using both the same comparator and the same mappings:
- `public TreeMap(Comparator<? super K> comparator)`
- `public TreeMap(SortedMap<K, ? extends V> m)`


Notice that the second of these constructors, 
which is defined so as to allow the new map to accept the ordering of the one supplied, 
suffers from a similar problem to the corresponding constructor of `TreeSet`: 
the standard conversion constructor - the one that takes a `Map` as its argument - 
always uses the natural ordering of the keys, 
so if you supply a reference to a `SortedMap` to the conversion constructor of `TreeMap`, 
the ordering of the constructed map will depend on the static type of that reference.

### `TreeMap` performance and its iterators.

`TreeMap` has similar performance characteristics to `TreeSet`: 
the basic operations (get, put, and remove) perform in O(log N) time. 

The collection view iterators are fail-fast.

### Map interfaces for concurrent environment

- [`ConcurrentMap`](#concurrentmap)
- [`ConcurrentNavigableMap`](#concurrentnavigablemap)


### ConcurrentMap
`ConcurrentMap` - It provided declarations for four methods:
`putIfAbsent`, `remove`, and two overloads of `replace` - that perform compound operations atomically. 
At Java 8, new [compound operations](#compound-operations) were introduced, 
and `ConcurrentMap` was provided with default implementations for these. 
However, the existing four compound methods were promoted to the `Map` interface, 
so `ConcurrentMap` no longer exposes any new functionality

### Concurrent map implementations
- [`ConcurrentHashMap`](#concurrenthashmap) - implementation of `ConcurrentMap`
- [`ConcurrentSkipListMap`](#concurrentskiplistmap-idea-performance-iterators) - implementation of `ConcurrentNavigableMap`


### `ConcurrentHashMap`

The class `ConcurrentHashMap` provides an implementation of `ConcurrentMap` and offers an effective solution 
to the problem of reconciling throughput with thread safety. 
It is optimized for reading, so retrievals do not block even while the table is being updated. 
To allow for this, the contract states that the results of retrievals will reflect the latest update operations 
completed before the start of the retrieval. 
Concurrent updates can proceed safely, even while the table is being resized.

### `ConcurrentHashMap` creation issue

The constructors for `ConcurrentHashMap` are similar to those of `HashMap`, 
but with an extra one that provides the programmer 
with the ability to hint to the implementation the expected number of concurrently updating threads 
(its concurrency level):
- `ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel)`

### When `ConcurrentHashMap` should be used? What must you care about?

`ConcurrentHashMap` is a useful implementation of `Map` in any concurrent application 
where it is unnecessary to lock the entire table; 
this is the one capability of synchronized maps that it does not support. 
This affects the results of aggregate status methods such as `size`, `isEmpty`, and `containsValue`. 
A map cannot be locked as a whole, so if it is undergoing concurrent updates it will be changing 
while these methods are working. 
In that case, their results will not reflect any consistent state and **should be treated as approximations**.

### `ConcurrentHashMap` performance and its iterators

Disregarding locking overheads, the cost of the operations of `ConcurrentHashMap` are similar to those of `HashMap`. 
The collection views return weakly consistent iterators.

### `ConcurrentNavigableMap`, purpose

`ConcurrentNavigableMap` inherits from both `ConcurrentMap` and `NavigableMap`. 
It contains just the methods of these two interfaces, with a few changes to make the return types more precise.
The range-view methods inherited from `SortedMap` and `NavigableMap` now return views of type `ConcurrentNavigableMap`.

### Range-view methods of `ConcurrentNavigableMap` vs `NavigableMap`

The compatibility concerns that prevented `NavigableMap` from overriding the methods of `SortedMap` 
don’t apply to overriding the range-view methods of `NavigableMap` or `SortedMap`; 
because neither of these has any implementations that have been retrofitted to the new interface, 
the danger of breaking implementation subclasses does not arise. 
For the same reason, it is now possible to override `keySet` to return `NavigableSet`.

### `ConcurrentNavigableMap` implementations
- [`ConcurrentSkipListMap`](#concurrentskiplistmap-idea-performance-iterators)

### `ConcurrentSkipListMap`, idea, performance, iterators

The relationship between `ConcurrentSkipListMap` and `ConcurrentSkipListSet` is like that 
between `TreeMap` and `TreeSet`. 
A `ConcurrentSkipListSet` is implemented by a `ConcurrentSkipListMap` 
in which every key is associated with the same standard value, 
so the mechanism and performance of the skip list implementation applies equally here: 
the basic operations (get, put, and remove) have O(log N) complexity, 
and iterators over the collection views execute next in constant time. 

These iterators are weakly consistent.

### Comparing Map Implementations


| Map Type              | get      | containsKey | next     | Notes                     |
|-----------------------|----------|-------------|----------|---------------------------|
| HashMap               | O(1)     | O(1)        | O(h/N)   | *h* is the table capacity |
| WeakHashMap           | O(1)     | O(1)        | O(h/N)   | *h* is the table capacity |
| LinkedHashMap         | O(1)     | O(1)        | O(h/N)   | *h* is the table capacity |
| IdentityHashMap       | O(1)     | O(1)        | O(h/N)   | *h* is the table capacity |
| EnumMap               | O(1)     | O(1)        | O(1)     |                           |
| TreeMap               | O(log N) | O(log N)    | O(log N) |                           |
| ConcurrentHashMap     | O(1)     | O(1)        | O(h/N)   | *h* is the table capacity |
| ConcurrentSkipListMap | O(log N) | O(log N)    | O(1)     |                           |


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