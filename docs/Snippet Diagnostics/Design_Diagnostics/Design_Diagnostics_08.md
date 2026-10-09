### Describe a code snippet #71
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Book {
  private final String isbn;
  private String currentOwner;   // changes over time

  @Override public boolean equals(Object o) {
    if (!(o instanceof Book)) return false;
    Book b = (Book) o;
    return isbn.equals(b.isbn) && currentOwner.equals(b.currentOwner);
  }
  @Override public int hashCode() {
    return Objects.hash(isbn, currentOwner);
  }
}
```

</details>

<details><summary>Show answer</summary>

`equals`/`hashCode` mix a stable identity (`isbn`) with a field that changes (`currentOwner`). Two copies of the
same book are "equal" only while they share an owner, and a book's hash code changes when it is lent out — so a
`Book` placed in a `HashSet` gets lost when `currentOwner` changes, and two records of the same physical book stop
being equal the moment ownership differs. Identity equality should rest only on fields that don't change.

Fix: base `equals`/`hashCode` on the stable key alone.

```java
@Override public boolean equals(Object o) {
  return o instanceof Book b && isbn.equals(b.isbn);
}
@Override public int hashCode() { return isbn.hashCode(); }
```

Handle: build `equals`/`hashCode` from stable identity fields only; including a changing field loses the object in
hash collections and makes equality drift.

</details>

</details>

---

### Describe a code snippet #72
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Circle {
  private final double radius;
  public Circle(double radius) { this.radius = radius; }
  // no toString override
}

Circle c = new Circle(5);
System.out.println("Created: " + c);
```

</details>

<details><summary>Show answer</summary>

With no `toString`, the print shows the default `Circle@1b6d3586` — the class name and a hash, useless for logs or
debugging. Every place the object is concatenated into a string or printed shows this noise, and during an
incident a log line gives no clue what the object held. A class whose instances are logged should provide a
readable `toString`.

Fix: override `toString` to show the meaningful state.

```java
@Override public String toString() {
  return "Circle{radius=" + radius + "}";
}
```

Handle: override `toString` on any class that gets logged or printed; the default `Class@hash` tells you nothing.

</details>

</details>

---

### Describe a code snippet #73
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class BankVault extends HashMap<String, Long> {
  public long totalBalance() {
    return values().stream().mapToLong(Long::longValue).sum();
  }
}
```

</details>

<details><summary>Show answer</summary>

`BankVault extends HashMap` to reuse map behavior, but now a vault *is* a `HashMap`, so callers can call `put`,
`remove`, `clear`, and every other map method directly on it — bypassing any rule the vault should enforce (no
negative balances, audit on change). The public API is the entire `HashMap` surface, forever, and the vault can
never restrict or change it. Extending a collection to reuse it exposes everything you didn't mean to.

Fix: hold a map as a private field and expose only the vault's own operations.

```java
public class BankVault {
  private final Map<String, Long> balances = new HashMap<>();
  public void deposit(String acct, long amt) { /* enforce rules */ }
  public long totalBalance() { /* ... */ }
}
```

Handle: don't extend a collection to reuse it; you inherit its whole mutable API and can't enforce your own
rules — wrap it instead.

</details>

</details>

---

### Describe a code snippet #74
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Point3D extends Point2D {
  private final int z;
  public Point3D(int x, int y, int z) { super(x, y); this.z = z; }

  @Override public boolean equals(Object o) {
    if (!(o instanceof Point3D)) return false;
    return super.equals(o) && ((Point3D) o).z == z;
  }
  @Override public int hashCode() {
    return Objects.hash(super.hashCode(), z);
  }
}
```

`Point2D` is concrete with a value-based `equals`.

</details>

<details><summary>Show answer</summary>

Same equals-across-a-concrete-parent trap as the money/color cases, framed on coordinates. A `Point2D(1,2)` and a
`Point3D(1,2,5)` compare asymmetrically: `p2d.equals(p3d)` runs `Point2D.equals` and returns `true` (same x,y),
while `p3d.equals(p2d)` fails the `instanceof Point3D` check and returns `false`. Adding `z` as a value component
to a subclass of a concrete, value-equal parent can't keep symmetry.

Fix: compose — a 3-D point *has* a 2-D point plus a z — or make the whole thing a single flat immutable type /
record.

```java
public record Point3D(int x, int y, int z) { }   // flat value type, correct equals for free
```

Handle: extending a concrete value-equal class with a new component breaks `equals` symmetry; use composition or a
single flat record.

</details>

</details>

---

### Describe a code snippet #75
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public void printAll(String... items) {
  for (String s : items) System.out.println(s);
}

public int max(int... nums) {
  int m = nums[0];
  for (int n : nums) m = Math.max(m, n);
  return m;
}
```

</details>

<details><summary>Show answer</summary>

`printAll()` with no args is harmless, but `max()` with no args reads `nums[0]` on an empty array and throws
`ArrayIndexOutOfBoundsException`. `max` *requires* at least one number, but varargs lets the caller pass zero, and
the failure only shows at runtime. When a varargs method needs a minimum number of arguments, the bare varargs
form can't express that.

Fix: make the first argument mandatory and let varargs cover the rest.

```java
public int max(int first, int... rest) {
  int m = first;
  for (int n : rest) m = Math.max(m, n);
  return m;
}
```

Handle: if a varargs method needs at least one argument, take one fixed parameter plus varargs; plain varargs
allows an empty call that fails at runtime.

</details>

</details>

---

### Describe a code snippet #76
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Cache {
  private final Map<String, byte[]> store = new HashMap<>();

  public void put(String key, byte[] data) {
    store.put(key, data);
  }
}
```

The cache lives for the whole app run and keys are never removed.

</details>

<details><summary>Show answer</summary>

Entries go in and nothing ever comes out — no size limit, no expiry, no eviction. Over a long run the map grows
without bound and holds every `byte[]` ever cached, so it is a memory leak wearing the name "cache." A real cache
has to bound what it keeps.

Fix: use a cache with eviction — a size/time-bounded library cache, or at minimum a `LinkedHashMap` in
access-order mode with a `removeEldestEntry` cap.

```java
private final Map<String, byte[]> store =
    new LinkedHashMap<>(16, 0.75f, true) {
      @Override protected boolean removeEldestEntry(Map.Entry<String, byte[]> e) {
        return size() > MAX_ENTRIES;   // evict once over the cap
      }
    };
```

Handle: an unbounded cache is a memory leak; give it a size or time limit so old entries are evicted.

</details>

</details>

---

### Describe a code snippet #77
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Grade {
  private final int score;
  public Grade(int score) { this.score = score; }
}

Grade g = new Grade(150);   // out of 100
Grade h = new Grade(-20);
```

</details>

<details><summary>Show answer</summary>

The constructor takes any `int`, so `150` and `-20` build "grades" that are outside any valid range. The invalid
object exists from the moment of construction and spreads through the system, and the failure — a report that
shows 150% — surfaces far from the bad `new Grade(...)` call. A constructor should reject arguments that would
make an invalid object.

Fix: validate in the constructor and fail at the point of creation.

```java
public Grade(int score) {
  if (score < 0 || score > 100) {
    throw new IllegalArgumentException("score must be 0..100, was " + score);
  }
  this.score = score;
}
```

Handle: validate constructor arguments and reject invalid ones immediately; an unchecked constructor lets a broken
object escape and fail later.

</details>

</details>

---

### Describe a code snippet #78
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public interface Repository {
  void save(Object o) throws SQLException;
}
```

</details>

<details><summary>Show answer</summary>

The interface method declares `throws SQLException` — a low-level, storage-specific checked type — in its
contract. Every caller of *any* `Repository` must now handle `SQLException`, and the interface is welded to SQL
databases: an in-memory or file-based implementation still has to declare or wrap a SQL exception it never throws.
Leaking an implementation's exception type into an abstraction ties every caller and every implementation to that
one technology.

Fix: declare a storage-neutral exception (usually unchecked) in the interface; each implementation translates its
own low-level failure into it.

```java
public interface Repository {
  void save(Object o);   // implementations throw a DataAccessException (unchecked), cause preserved
}
```

Handle: don't leak an implementation's exception type through an interface; use a technology-neutral exception so
callers and other implementations aren't bound to one backend.

</details>

</details>

---

### Describe a code snippet #79
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Counter {
  private int count;
  public Counter increment() {
    count++;
    return this;   // for chaining
  }
}

Counter c = new Counter();
Counter result = c.increment().increment();
```

</details>

<details><summary>Show answer</summary>

`increment()` mutates `count` and returns `this`, mixing two styles: it looks like a fluent/immutable builder
(returning a value to chain) but actually changes the object in place. A caller who writes
`Counter result = c.increment()` reasonably thinks `result` is a new counter and `c` is unchanged — but `result`
and `c` are the same object, both now mutated. Returning `this` from a mutating method invites that
misread.

Fix: pick one style. Either mutate and return `void` (clearly in-place), or make it immutable and return a new
object.

```java
// in-place, honest:
public void increment() { count++; }
// or immutable:
public Counter increment() { return new Counter(count + 1); }
```

Handle: don't return `this` from a method that mutates in place; the chained call looks like it produces a new
object but shares the mutated one.

</details>

</details>

---

### Describe a code snippet #80
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
enum Planet {
  MERCURY, VENUS, EARTH, MARS;
}

public double gravity(int planetCode) {
  switch (planetCode) {
    case 0: return 3.7;
    case 1: return 8.9;
    // ...
  }
  return 0;
}

double g = gravity(2);   // caller must know 2 == EARTH
```

</details>

<details><summary>Show answer</summary>

`gravity` takes an `int` code that the caller must map to a planet by hand, so `gravity(2)` requires knowing
`2 == EARTH`, and `gravity(99)` compiles and returns `0` (a silent wrong answer). The `enum` exists but the API
uses raw `int`s instead of it, throwing away the type safety the enum offers.

Fix: take the enum, so only valid planets compile and the mapping is explicit.

```java
public double gravity(Planet p) {
  return switch (p) {
    case MERCURY -> 3.7;
    case VENUS   -> 8.9;
    case EARTH   -> 9.8;
    case MARS    -> 3.7;
  };
}
double g = gravity(Planet.EARTH);
```

Handle: pass an `enum`, not an `int` code, when a fixed set is defined; an int parameter accepts invalid codes and
forces callers to memorize the mapping.

</details>

</details>
