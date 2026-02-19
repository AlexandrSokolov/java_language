### 
The stack example does leak, but not unboundedly — it leaks up to the high-water mark, and that retained memory stays forever unless overwritten, which may never happen.

bounded vs unboundedn memory leaks, more examples, see also:
[When does logical (bounded) leak become a real problem?](1_faq_objects_creating_destroying.md#when-does-logical-bounded-leak-become-a-real-problem)

### Memory leak, what is it?
<details><summary>Show answer</summary>

A memory leak is a situation where a program unintentionally holds onto memory that it no longer needs,
preventing that memory from being reclaimed by the garbage collector.

Having memory leaks can silently manifest itself as reduced performance due to:
- increased garbage collector activity or
- increased memory footprint
- In extreme cases, such memory leaks can cause disk paging and even program failure with an `OutOfMemoryError`,
  but such failures are relatively rare.

</details>

### What happens in a memory leak?
<details><summary>Show answer</summary>

- Your application allocates memory for objects or data structures.
- Later, those objects are no longer needed, but references to them still exist
  (e.g., in a static field, collection, cache, or listener).
- Because the garbage collector sees these references, it cannot free the memory.
- Over time, this unused memory accumulates, leading to:
    - Increased heap usage.
    - Slower performance.
    - Eventually, `OutOfMemoryError` or system instability.

</details>

### Why a garbage collector cannot fix the issue with memory leaks?
<details><summary>Show answer</summary>

A garbage collector (GC) only frees memory for objects that are truly unreachable,
but a memory leak occurs when objects are still reachable (via references)
even though they are logically no longer needed.

</details>

### Common causes in Java
<details><summary>Show answer</summary>

Static collections (e.g., Map, List) that keep growing and never cleared.
Listeners or callbacks registered but never removed.
ThreadLocal variables not cleaned up in thread pools.
Caches without eviction policies.
Improper use of singletons holding large object graphs.

</details>

### How to detect memory leaks?
<details><summary>Show answer</summary>

- Use tools like VisualVM, JProfiler, or Eclipse MAT to analyze heap dumps.
- Look for objects that should have been garbage collected but remain referenced.

</details>

### How to fix memory leaks?
<details><summary>Show answer</summary>

- Removing unnecessary references.
- Implementing proper cleanup (e.g., removeListener()).
- Using weak references (WeakHashMap) for caches.
- Applying proper lifecycle management.

</details>

### Memory leak
<details><summary>Show answer</summary>

```java
public class Stack {
  private Object[] elements;
  private int size = 0;
  private static final int DEFAULT_INITIAL_CAPACITY = 16;

  public Stack() {
    elements = new Object[DEFAULT_INITIAL_CAPACITY];
  }

  public void push(Object e) {
    ensureCapacity();
    elements[size++] = e;
  }

  public Object pop() {
    if (size == 0)
      throw new EmptyStackException();
    return elements[--size];
  }

  /**
   * Ensure space for at least one more element, roughly
   * doubling the capacity each time the array needs to grow.
   */
  private void ensureCapacity() {
    if (elements.length == size)
      elements = Arrays.copyOf(elements, 2 * size + 1);
  }
}
```
If a stack grows and then shrinks, the objects that were popped off the stack will not be garbage collected,
even if the program using the stack has no more references to them.
This is because the stack maintains obsolete references to these objects.
An obsolete reference is simply a reference that will never be dereferenced again.
In this case, any references outside of the _"active portion"_ of the element array are obsolete.
The active portion consists of the elements whose index is less than size.

null out references once they
become obsolete. In the case of our Stack class, the reference to an item
becomes obsolete as soon as it’s popped off the stack:

```java
public Object pop() {
  if (size == 0)
    throw new EmptyStackException();
  Object result = elements[--size];
  elements[size] = null; // Eliminate obsolete reference
  return result;
}
```
An added benefit of nulling out obsolete references is that if they are
subsequently dereferenced by mistake, the program will immediately fail with a
NullPointerException, rather than quietly doing the wrong thing. It is
always beneficial to detect programming errors as quickly as possible.

When programmers are first stung by this problem, they may overcompensate
by nulling out every object reference as soon as the program is finished using it.
This is neither necessary nor desirable; it clutters up the program unnecessarily.
Nulling out object references should be the exception rather than the norm.
The best way to eliminate an obsolete reference is to let the variable that
contained the reference fall out of scope. This occurs naturally if you define each
variable in the narrowest possible scope

So when should you null out a reference? What aspect of the Stack class
makes it susceptible to memory leaks? Simply put, it manages its own memory.
The storage pool consists of the elements of the elements array (the object
reference cells, not the objects themselves). The elements in the active portion of
the array (as defined earlier) are allocated, and those in the remainder of the
array are free. The garbage collector has no way of knowing this; to the garbage
collector, all of the object references in the elements array are equally valid.
Only the programmer knows that the inactive portion of the array is unimportant.
The programmer effectively communicates this fact to the garbage collector by
manually nulling out array elements as soon as they become part of the inactive
portion.
Generally speaking, whenever a class manages its own memory, the
programmer should be alert for memory leaks. Whenever an element is freed,
any object references contained in the element should be nulled out.

</details>

### Describe the following code
<details><summary>Show answer</summary>

#

</details>

### String instances creation
<details><summary>Show answer</summary>

```java
String s = new String("bikini");
```

The statement creates a new String instance each time it is executed, and none of those object creations is necessary.
The argument to the String constructor ("bikini") is itself a String instance,
functionally identical to all of the objects created by the constructor.
If this usage occurs in a loop or in a frequently invoked method,
millions of String instances can be created needlessly.
The improved version is simply the following:
```java
String s = "bikini";
```
This version uses a single String instance, rather than creating a new one each time it is executed.

</details>