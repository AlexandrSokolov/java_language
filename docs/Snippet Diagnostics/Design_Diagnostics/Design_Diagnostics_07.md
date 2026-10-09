### Describe a code snippet #61
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class CaseInsensitiveKey {
  private final String value;
  public CaseInsensitiveKey(String value) { this.value = value; }

  @Override public boolean equals(Object o) {
    if (!(o instanceof CaseInsensitiveKey)) return false;
    return value.equalsIgnoreCase(((CaseInsensitiveKey) o).value);
  }
  @Override public int hashCode() {
    return value.hashCode();
  }
}
```

</details>

<details><summary>Show answer</summary>

`equals` treats `"ABC"` and `"abc"` as equal (case-insensitive), but `hashCode` uses `value.hashCode()`, which is
case-*sensitive* — so `"ABC"` and `"abc"` are equal yet hash differently. That breaks the core rule that equal
objects must have equal hash codes: put `"ABC"` in a `HashMap` and look up with `"abc"` and the lookup misses,
because the two land in different buckets. `hashCode` must be computed on the same normalized form `equals` uses.

Fix: hash the normalized (lower-cased) value so equal keys hash together.

```java
@Override public int hashCode() {
  return value.toLowerCase().hashCode();   // matches equalsIgnoreCase
}
```

Handle: `hashCode` must be consistent with `equals`; if `equals` ignores case, `hashCode` must normalize case too.

</details>

</details>

---

### Describe a code snippet #62
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Node {
  private Node parent;
  private final List<Node> children = new ArrayList<>();

  public List<Node> children() { return children; }
  public void setParent(Node p) { this.parent = p; }
}
```

</details>

<details><summary>Show answer</summary>

`children()` returns the internal list, so a caller can `add` a child directly — bypassing any code that should
also set that child's `parent`. The two-way link (a child's `parent` must point back to its parent) can now be
broken from outside, leaving the tree inconsistent. Exposing the mutable list lets callers change one side of an
invariant the class is supposed to keep on both sides.

Fix: keep the list private and offer an `addChild` that maintains both sides of the link.

```java
public void addChild(Node c) {
  children.add(c);
  c.parent = this;          // keep the invariant
}
public List<Node> children() { return List.copyOf(children); }
```

Handle: don't expose a mutable collection that guards an invariant; offer a method that updates every side of the
invariant together.

</details>

</details>

---

### Describe a code snippet #63
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public abstract class AbstractProcessor {
  public final void run() {
    step1();
    step2();
  }
  protected abstract void step1();
  protected abstract void step2();
}
```

The library ships this for third parties to extend, with no documentation.

</details>

<details><summary>Show answer</summary>

The class is designed for inheritance — subclasses fill in `step1`/`step2` — but nothing documents the
self-use: a subclass author cannot tell that `run()` calls `step1` before `step2`, whether either may be called
more than once, or what state is set by the time each runs. A class meant to be extended must document how it
calls its own overridable methods, or subclass authors guess and break. (The `final run()` is good — it stops a
subclass from breaking the sequence.)

Fix: document the self-use so overriders know the contract.

```java
/**
 * Calls {@link #step1()} once, then {@link #step2()} once, in that order,
 * on the same thread. Neither is called again after run() returns.
 */
public final void run() { step1(); step2(); }
```

Handle: a class designed for inheritance must document how it calls its own overridable methods; without it,
subclass authors work blind.

</details>

</details>

---

### Describe a code snippet #64
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class OrderService {
  public void place(Order o) {
    validate(o);          // may throw
    charge(o);            // may throw after inventory reserved
    reserveInventory(o);
    o.setStatus(PLACED);
  }
}
```

`charge` throws after `reserveInventory` has already run in a previous line? (Trace the order.)

</details>

<details><summary>Show answer</summary>

Trace the real order: `charge(o)` runs *before* `reserveInventory(o)`. If `charge` succeeds and
`reserveInventory` then throws (out of stock), the customer has been charged but no inventory is reserved and the
status is never set — the operation half-completed and left money taken with nothing reserved. The steps mutate
external state in an order where a later failure strands an earlier commit.

Fix: order the steps so the reversible/checkable ones happen before the hard-to-undo one, or wrap the multi-step
change so a failure rolls back what ran.

```java
public void place(Order o) {
  validate(o);
  reserveInventory(o);   // reserve first (easy to release)
  charge(o);             // charge last; if it throws, release the reservation
  o.setStatus(PLACED);
}
```

Handle: order multi-step operations so a late failure doesn't strand an irreversible earlier step; charge after
you've secured what the charge is for.

</details>

</details>

---

### Describe a code snippet #65
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class StringUtils {
  public StringUtils() { }

  public static boolean isEmpty(String s) {
    return s == null || s.isEmpty();
  }
}
```

</details>

<details><summary>Show answer</summary>

The class holds only static helpers but has a `public` (default) constructor, so callers can write
`new StringUtils()` — a meaningless instance of a class that was never meant to be instantiated. A utility class
of static methods should not be instantiable.

Fix: give it a single `private` constructor so no instance can be made.

```java
public class StringUtils {
  private StringUtils() {
    throw new AssertionError("no instances");  // also blocks reflection-by-accident
  }
  public static boolean isEmpty(String s) { return s == null || s.isEmpty(); }
}
```

Handle: give a static-utility class a private constructor; a default public constructor lets callers make
pointless instances.

</details>

</details>

---

### Describe a code snippet #66
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Wrapper {
  private final Date date;

  public Wrapper(Date date) {
    if (date.after(new Date())) {
      throw new IllegalArgumentException("future date");
    }
    this.date = new Date(date.getTime());   // copy after check
  }
}
```

</details>

<details><summary>Show answer</summary>

The copy is made *after* the validation, but the check reads the caller's original object. Between the `after(...)`
check and the `new Date(...)` copy, another thread holding the same `Date` could change it — so the object could
pass the "not in the future" check and then be copied as a future date. The check must run on the copy the object
will keep, not on the caller's mutable original (a time-of-check/time-of-use gap).

Fix: copy first, then validate the copy.

```java
public Wrapper(Date date) {
  Date snapshot = new Date(date.getTime());          // copy first
  if (snapshot.after(new Date())) throw new IllegalArgumentException("future date");
  this.date = snapshot;                              // validated copy
}
```

Handle: defensively copy *before* validating, then check the copy; validating the caller's original leaves a
change-after-check gap.

</details>

</details>

---

### Describe a code snippet #67
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Analyzer {
  public Stats analyze(List<Integer> data) {
    Collections.sort(data);          // sorts the caller's list
    return computeStats(data);
  }
}
```

</details>

<details><summary>Show answer</summary>

`Collections.sort(data)` sorts the caller's own list in place. The caller passed the data to be *analyzed*, not
reordered, and now their list comes back with its order changed — a side effect they never asked for, which
breaks any code that relied on the original order. A method that takes a collection should not mutate the caller's
copy.

Fix: work on a copy, leaving the argument untouched.

```java
public Stats analyze(List<Integer> data) {
  List<Integer> sorted = new ArrayList<>(data);
  Collections.sort(sorted);
  return computeStats(sorted);
}
```

Handle: don't mutate a collection passed as an argument; copy it first, so the caller's data keeps its order.

</details>

</details>

---

### Describe a code snippet #68
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Timer {
  public void schedule(Runnable task, int seconds) { /* ... */ }
  public void schedule(Runnable task, long millis)  { /* ... */ }
}

timer.schedule(job, 5);
```

</details>

<details><summary>Show answer</summary>

Two overloads differ only by `int` vs `long`, and `timer.schedule(job, 5)` picks `schedule(Runnable, int)` because
`5` is an `int` literal — so the caller who meant "5 milliseconds" or "5 seconds" cannot tell which they got, and a
value that overflows `int` silently changes which method binds. Overloading on numeric primitive types that
auto-convert into each other is a trap: the compiler's choice surprises the caller.

Fix: give the two meanings different names (they *are* different units anyway).

```java
public void scheduleAfterSeconds(Runnable task, long seconds) { /* ... */ }
public void scheduleAfterMillis(Runnable task, long millis)   { /* ... */ }
```

Handle: don't overload on numeric primitives that convert into each other; name the methods so the unit and the
binding are unambiguous.

</details>

</details>

---

### Describe a code snippet #69
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class ImmutablePoint {
  private final int x, y;
  public ImmutablePoint(int x, int y) { this.x = x; this.y = y; }
  public int getX() { return x; }
  public int getY() { return y; }
}
```

The class is named "immutable" and is used as a shared constant across threads. It is `public` and not `final`.

</details>

<details><summary>Show answer</summary>

The fields are `final` and there are no setters, so instances are immutable *as written* — but the class is not
`final`, so someone can extend it and add mutable state, then pass that subtype where an `ImmutablePoint` is
expected. Callers relying on "this is immutable" (for caching, for sharing across threads without synchronization)
lose that guarantee, because the actual object might be a mutable subclass. An immutability promise leaks unless
the class is closed.

Fix: make the class `final` (or make the constructor private and hand out instances via a static factory), so no
subclass can add mutability.

```java
public final class ImmutablePoint { /* ... */ }
```

Handle: mark an immutable class `final`; if it can be subclassed, a subclass can add mutable state and break the
promise callers rely on.

</details>

</details>

---

### Describe a code snippet #70
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public int[] toArray(Collection<Integer> c) {
  return (int[]) c.toArray();
}
```

</details>

<details><summary>Show answer</summary>

`Collection.toArray()` returns an `Object[]`, and an `Object[]` cannot be cast to `int[]` — this throws
`ClassCastException` at runtime. Beyond that specific bug, returning a raw array from an API ties callers to an
array (which is always mutable and awkward to grow) where a `List` would be safer and clearer. The cast is wrong
and the array return type is a weaker choice than a collection.

Fix: build the primitive array explicitly, or return a `List<Integer>`.

```java
public int[] toArray(Collection<Integer> c) {
  return c.stream().mapToInt(Integer::intValue).toArray();
}
// or prefer returning List<Integer> and skip the array entirely
```

Handle: `Collection.toArray()` gives `Object[]`, not a typed or primitive array; build the primitive array
explicitly, and prefer returning a `List`.

</details>

</details>
