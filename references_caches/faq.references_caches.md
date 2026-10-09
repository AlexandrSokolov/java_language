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

- Use `WeakReference` or [`WeakHashMap`](../collections/maps/faq.maps.md#weakhashmap-its-purpose)
  for caches where objects can be garbage collected when not strongly referenced elsewhere.
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

### `WeakReference`

A `WeakReference` introduces an extra level of indirection in reaching an object.

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