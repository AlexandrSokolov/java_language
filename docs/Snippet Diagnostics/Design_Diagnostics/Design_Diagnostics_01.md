### Describe a code snippet #1
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Money {
  private final int amount;
  private final String currency;

  public Money(int amount, String currency) {
    this.amount = amount;
    this.currency = currency;
  }

  @Override public boolean equals(Object o) {
    if (!(o instanceof Money)) return false;
    Money m = (Money) o;
    return amount == m.amount && currency.equals(m.currency);
  }
}
```

</details>

<details><summary>Show answer</summary>

`equals` is overridden but `hashCode` is not. Two `Money` objects that are equal will (almost always) return
different hash codes from the inherited `Object.hashCode`, which is based on identity. Put one in a `HashMap` or
`HashSet` and look it up with an equal-but-different instance and the lookup fails — the map hashes to one bucket
on insert and a different bucket on search, so it reports the key as absent.

Fix: override `hashCode` from the same fields `equals` uses.

```java
@Override public int hashCode() {
  return Objects.hash(amount, currency);
}
```

Handle: override `hashCode` whenever you override `equals`; a hash collection needs both to agree or it loses
your keys.

</details>

</details>

---

### Describe a code snippet #2
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Period {
  private final Date start;
  private final Date end;

  public Period(Date start, Date end) {
    this.start = start;
    this.end = end;
  }

  public Date getStart() { return start; }
  public Date getEnd()   { return end; }
}
```

</details>

<details><summary>Show answer</summary>

`Date` is mutable, and the constructor stores the caller's own `Date` objects directly. The caller keeps a
reference, so after construction they can call `start.setTime(...)` and change the inside of a "final" `Period`.
The `final` on the field only stops the reference from being reassigned; it does nothing to the object the
reference points at.

Fix: copy on the way in, so the `Period` holds its own objects the caller cannot reach.

```java
public Period(Date start, Date end) {
  this.start = new Date(start.getTime());
  this.end   = new Date(end.getTime());
}
```

Copy before any validation, and validate the copies — otherwise a caller could change the values between the
check and the copy. (Getters here still leak the internal `Date` — that is a separate defect, on its own card.)

Handle: store your own copy of a mutable argument; `final` guards the reference, not the object it points to.

</details>

</details>

---

### Describe a code snippet #3
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Point {
  public double x;
  public double y;
}
```

</details>

<details><summary>Show answer</summary>

The fields are `public`, so callers read and write them directly. The class can never change its internal
representation without breaking every caller, can never enforce an invariant (nothing stops `x = Double.NaN`),
and can never make a field read-only or add an action when a value changes. A public field freezes the API to
the current field layout.

Fix: make the fields private and expose accessors, so the class controls access and keeps freedom to change.

```java
public class Point {
  private double x;
  private double y;
  public double getX() { return x; }
  public double getY() { return y; }
}
```

For a plain immutable data carrier, a `record` gives the accessors and keeps the fields hidden. Note: for a
package-private or private nested class, exposing fields is sometimes fine — the blast radius is contained.

Handle: keep fields private behind accessors, so you can change representation and enforce invariants later.

</details>

</details>

---

### Describe a code snippet #4
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Super {
  public Super() {
    overrideMe();
  }
  public void overrideMe() { }
}

public class Sub extends Super {
  private final Instant now = Instant.now();

  @Override public void overrideMe() {
    System.out.println(now);
  }
}
```

</details>

<details><summary>Show answer</summary>

`Super`'s constructor calls `overrideMe()`, which is overridden in `Sub`. When you build a `Sub`, the superclass
constructor runs *before* `Sub`'s field initializers, so `overrideMe()` runs while `now` is still `null` — it
prints `null`, not the timestamp. The subclass method fires before the subclass is fully built.

Fix: never call an overridable method from a constructor. Call only `private`, `static`, or `final` methods,
which a subclass cannot override.

```java
public class Super {
  public Super() { }            // do not call overridable methods here
  public final void safeInit() { }  // final: safe to call
}
```

Handle: a constructor must not call an overridable method; the override runs before the subclass fields exist.

</details>

</details>

---

### Describe a code snippet #5
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Pizza {
  public Pizza(int size, boolean cheese, boolean pepperoni,
               boolean bacon, boolean onion, boolean mushroom) {
    // ...
  }
}

Pizza p = new Pizza(12, true, false, true, false, true);
```

</details>

<details><summary>Show answer</summary>

The call site is unreadable: `new Pizza(12, true, false, true, false, true)` gives no clue which `true` is which
topping, and two booleans swapped by mistake compiles fine and fails silently. As more options appear the
constructor grows more overloads (the telescoping-constructor pattern), each a different length, and callers
guess which one to use.

Fix: a builder, so each value is named at the call site and optional ones can be skipped.

```java
Pizza p = new Pizza.Builder(12)
    .cheese().pepperoni().bacon().mushroom()
    .build();
```

Handle: many same-typed constructor args read as a puzzle at the call site; a builder names each one.

</details>

</details>

---

### Describe a code snippet #6
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public List<String> getActiveUsers() {
  List<String> result = query();
  if (result.isEmpty()) {
    return null;
  }
  return result;
}
```

</details>

<details><summary>Show answer</summary>

Returning `null` for the empty case forces every caller to write a null check before looping, and the one caller
who forgets gets a `NullPointerException` at `for (String u : getActiveUsers())`. An empty result is not a
special case that needs its own signal — an empty list already means "no active users."

Fix: return an empty collection, never `null`.

```java
public List<String> getActiveUsers() {
  return query(); // query() itself should return empty, not null
}
// or, if a source can produce null:
return result == null ? List.of() : result;
```

Handle: return an empty collection instead of `null`, so callers can loop without a guard.

</details>

</details>

---

### Describe a code snippet #7
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public void save(User u) throws Exception {
  if (u == null) throw new Exception("null user");
  repository.write(u);
}
```

</details>

<details><summary>Show answer</summary>

The method declares `throws Exception` — the widest checked type there is. Every caller must now catch or declare
`Exception`, which also swallows `RuntimeException`s and any other failure, so a caller cannot tell a bad argument
apart from a disk error, and cannot catch just the case it can handle. Throwing raw `Exception` erases all
information the type could carry.

Fix: throw a specific type that names the failure. A bad argument is a programming error, so an unchecked
`IllegalArgumentException` (or `NullPointerException`) fits, and it does not force `throws` on every caller.

```java
public void save(User u) {
  Objects.requireNonNull(u, "user");
  repository.write(u);
}
```

Handle: throw the most specific exception that fits; raw `Exception` forces callers to catch everything and tells
them nothing.

</details>

</details>

---

### Describe a code snippet #8
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Range {
  private final int low;
  private final int high;

  public Range(int low, int high) {
    this.low = low;
    this.high = high;
  }

  @Override public boolean equals(Object o) {
    if (!(o instanceof Range)) return false;
    Range r = (Range) o;
    return low == r.low && high == r.high;
  }

  @Override public int hashCode() {
    return low + high;
  }
}
```

</details>

<details><summary>Show answer</summary>

`equals` and `hashCode` are both present and consistent, so this is not broken — but `hashCode` is weak.
`low + high` gives the same hash to `(1, 4)`, `(2, 3)`, and `(4, 1)`, so many unequal ranges collide into the
same bucket and hash-map operations slow toward linear time. Addition throws away the order of the fields.

Fix: combine the fields in a way that keeps their order and spreads values.

```java
@Override public int hashCode() {
  return Objects.hash(low, high);
}
```

Handle: a `hashCode` that adds its fields collapses distinct keys into one bucket; combine them so order matters.

</details>

</details>

---

### Describe a code snippet #9
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Stack {
  public Object[] elements;
  private int size = 0;

  public Stack(int capacity) {
    elements = new Object[capacity];
  }
  // push / pop ...
}
```

</details>

<details><summary>Show answer</summary>

`elements` is `public`, so any caller can read or replace the whole backing array, resize it, null it out, or
read slots past `size` that hold stale references. The class cannot protect its own invariant (`size` matching
the live region), and can never change how it stores elements without breaking callers.

Fix: make the array private; expose only the stack operations.

```java
public class Stack {
  private Object[] elements;
  private int size = 0;
  public void push(Object e) { /* ... */ }
  public Object pop()        { /* ... */ }
}
```

Handle: never expose a backing array as a public field; callers can break every invariant the class relies on.

</details>

</details>

---

### Describe a code snippet #10
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
try {
  return Integer.parseInt(s);
} catch (NumberFormatException e) {
  throw new IllegalArgumentException("bad number");
}
```

</details>

<details><summary>Show answer</summary>

The new exception is thrown without the original as its cause, so the stack trace of the `NumberFormatException`
— which says what actually failed and where — is thrown away. When this surfaces in a log, whoever reads it sees
"bad number" with no trace back to the real parse failure.

Fix: pass the original exception as the cause.

```java
} catch (NumberFormatException e) {
  throw new IllegalArgumentException("bad number: " + s, e);
}
```

Handle: when translating an exception, keep the original as the cause, or you delete the trail to the real
failure.

</details>

</details>
