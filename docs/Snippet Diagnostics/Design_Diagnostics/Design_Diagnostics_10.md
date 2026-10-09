### Describe a code snippet #91
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Fraction {
  private final int num, den;
  public Fraction(int num, int den) { this.num = num; this.den = den; }

  @Override public boolean equals(Object o) {
    if (!(o instanceof Fraction)) return false;
    Fraction f = (Fraction) o;
    return num == f.num && den == f.den;
  }
  @Override public int hashCode() { return Objects.hash(num, den); }
}
```

`new Fraction(1, 2)` and `new Fraction(2, 4)` are expected to be equal.

</details>

<details><summary>Show answer</summary>

`equals` compares the raw numerator and denominator, so `1/2` and `2/4` — the same value — are not equal, and hash
to different buckets. For a value type, equality should reflect the *value*, not the unreduced representation. The
class stores fractions in unnormalized form, so two equal fractions look different to `equals`.

Fix: normalize in the constructor (reduce by the GCD, fix sign), so equal values have identical fields, and
`equals`/`hashCode` then work as written.

```java
public Fraction(int num, int den) {
  int g = gcd(Math.abs(num), Math.abs(den));
  int sign = den < 0 ? -1 : 1;
  this.num = sign * num / g;
  this.den = sign * den / g;   // 2/4 and 1/2 now store identically
}
```

Handle: normalize a value type's fields at construction so equal values store identically; comparing unreduced
representations makes equal values unequal.

</details>

</details>

---

### Describe a code snippet #92
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Document {
  private String content = "";

  public void append(String text) {
    content = content + text;
  }
}

// called in a loop thousands of times
for (String chunk : chunks) doc.append(chunk);
```

</details>

<details><summary>Show answer</summary>

Each `append` builds a whole new `String` by concatenation, copying all existing content every time. Over
thousands of calls that is quadratic work and a mountain of throwaway `String` objects — the classic
`String`-in-a-loop cost. For repeated appends, an accumulating buffer is the right tool.

Fix: hold a `StringBuilder` and append to it.

```java
public class Document {
  private final StringBuilder content = new StringBuilder();
  public void append(String text) { content.append(text); }
  public String content() { return content.toString(); }
}
```

Handle: use `StringBuilder` for repeated appends; `String + String` in a loop copies everything each time and
allocates on every step.

</details>

</details>

---

### Describe a code snippet #93
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Animal {
  public String describe() { return "an animal"; }
}
public class Dog extends Animal {
  public String describe(Object o) { return "a dog"; }
}

Animal a = new Dog();
System.out.println(a.describe());
```

</details>

<details><summary>Show answer</summary>

`Dog.describe(Object)` takes a parameter, so it does **not** override `Animal.describe()` — it overloads it under
the same name. `a.describe()` therefore calls `Animal`'s version and prints "an animal", not "a dog", which is
almost certainly not what the author intended. Without `@Override`, the compiler never flags that the signatures
don't match. A silent overload-instead-of-override.

Fix: match the signature and add `@Override`, which makes the compiler reject a mismatch.

```java
public class Dog extends Animal {
  @Override public String describe() { return "a dog"; }
}
```

Handle: always put `@Override` on an intended override; a changed parameter list makes it a silent overload the
compiler won't catch otherwise.

</details>

</details>

---

### Describe a code snippet #94
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Registry {
  private static final Map<String, Service> SERVICES = new HashMap<>();

  public static void register(String name, Service s) {
    SERVICES.put(name, s);
  }
  public static Map<String, Service> all() {
    return SERVICES;
  }
}
```

</details>

<details><summary>Show answer</summary>

`all()` returns the static, shared `SERVICES` map directly, so any caller can `Registry.all().clear()` and wipe
the global registry for the entire application, or add entries that skip `register`. A leaked reference to global
mutable state means every caller co-owns it. (The static mutable map is also a concurrency hazard, but the
in-scope defect here is the leak.)

Fix: return an unmodifiable view (or a copy), so callers can read but not alter the registry.

```java
public static Map<String, Service> all() {
  return Collections.unmodifiableMap(SERVICES);
}
```

Handle: never return a mutable reference to shared/global state; hand back an unmodifiable view so callers can't
clear or bypass it.

</details>

</details>

---

### Describe a code snippet #95
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class ImageLoader {
  public Image load(String path) {
    try {
      return readImage(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

The `IOException` is translated, and the cause *is* kept (good) — but the new type is a bare `RuntimeException`,
the most generic unchecked type. A caller who wants to handle "image failed to load" specially can't catch it
without catching every other `RuntimeException`, including bugs. When translating a checked exception to unchecked,
throwing the generic base type loses the ability to catch this failure precisely.

Fix: translate to a specific unchecked type (the JDK even has `UncheckedIOException` for exactly this).

```java
} catch (IOException e) {
  throw new UncheckedIOException("load failed: " + path, e);
}
// or a domain type: throw new ImageLoadException(path, e);
```

Handle: translate to a specific unchecked type, not bare `RuntimeException`, so callers can catch this failure
without catching every runtime error.

</details>

</details>

---

### Describe a code snippet #96
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Builder {
  private String host;
  private int port;

  public Builder host(String h) { this.host = h; return this; }
  public Builder port(int p)    { this.port = p; return this; }

  public Server build() {
    return new Server(host, port);
  }
}

Server s = new Builder().host("localhost").build();   // port never set
```

</details>

<details><summary>Show answer</summary>

`build()` constructs a `Server` without checking that required fields were set. `port` defaults to `0`, so this
returns a `Server` bound to port 0 — a silently wrong object, not an error. A builder's value is that `build()`
guarantees a valid result; skipping the required-field check throws that guarantee away and pushes the failure
downstream.

Fix: validate required fields in `build()` and fail there with a clear message.

```java
public Server build() {
  if (host == null) throw new IllegalStateException("host is required");
  if (port == 0)    throw new IllegalStateException("port is required");
  return new Server(host, port);
}
```

Handle: check required fields in `build()`; a builder that skips validation hands back a half-configured object
that fails later.

</details>

</details>

---

### Describe a code snippet #97
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Range {
  private final int min;
  private final int max;

  public Range(int min, int max) {
    this.min = min;
    this.max = max;
  }
}

Range r = new Range(10, 5);   // min > max
```

</details>

<details><summary>Show answer</summary>

The constructor accepts `min > max`, building a range that is logically impossible. Every method that later
assumes `min <= max` (iteration, `contains`, size) misbehaves on this object, and the failure appears far from the
`new Range(10, 5)` that created it. A constructor must reject arguments that violate the type's invariant.

Fix: check the invariant at construction.

```java
public Range(int min, int max) {
  if (min > max) throw new IllegalArgumentException("min > max: " + min + " > " + max);
  this.min = min;
  this.max = max;
}
```

Handle: enforce a type's invariant in its constructor; accepting invariant-violating arguments creates a broken
object that fails elsewhere.

</details>

</details>

---

### Describe a code snippet #98
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Base {
  int value = 10;
  int getValue() { return value; }
}
public class Derived extends Base {
  int value = 20;   // hides Base.value
  int getValue() { return value; }
}

Base b = new Derived();
System.out.println(b.value + " " + b.getValue());
```

</details>

<details><summary>Show answer</summary>

`Derived` declares its own `value` field, which *hides* `Base.value` rather than overriding it (fields are not
polymorphic; only methods are). `b.value` is resolved by the *declared* type `Base`, so it prints `10`, while
`b.getValue()` is a real override and prints `20` — the output is `10 20`, a confusing split. Shadowing a field in
a subclass creates two fields with the same name and surprising access rules.

Fix: don't redeclare the field. Reuse the base field, or make it private with an accessor so subclasses can't
shadow it.

```java
public class Base {
  private int value = 10;
  protected void setValue(int v) { this.value = v; }
  int getValue() { return value; }
}
public class Derived extends Base {
  Derived() { setValue(20); }   // one field, no shadowing
}
```

Handle: don't redeclare a field in a subclass; fields hide rather than override, so `obj.field` and a getter can
disagree.

</details>

</details>

---

### Describe a code snippet #99
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class PaymentProcessor {
  public void charge(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      return;   // silently ignore non-positive amounts
    }
    doCharge(amount);
  }
}
```

</details>

<details><summary>Show answer</summary>

A non-positive amount is silently ignored — the method returns as if it did something, but no charge happened and
the caller gets no signal. A bug that passes `0` or a negative amount looks successful, and the missing charge
surfaces later as a reconciliation mismatch. Silently swallowing an invalid argument hides the caller's error.

Fix: reject the invalid argument so the caller learns immediately.

```java
public void charge(BigDecimal amount) {
  if (amount.compareTo(BigDecimal.ZERO) <= 0) {
    throw new IllegalArgumentException("amount must be positive: " + amount);
  }
  doCharge(amount);
}
```

Handle: reject an invalid argument with an exception, don't silently `return`; a quiet no-op hides the caller's
mistake until it surfaces downstream.

</details>

</details>

---

### Describe a code snippet #100
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public interface Stack<E> {
  E pop();
  void push(E e);
  boolean isEmpty();
  int size();
  Object[] toArray();
  void sort(Comparator<E> c);   // ?
}
```

</details>

<details><summary>Show answer</summary>

`sort` does not belong on a stack. A stack's contract is last-in-first-out access — `push`, `pop`, `peek` — and
sorting its contents contradicts that (a sorted stack is no longer LIFO-ordered by insertion). Putting `sort` in
the interface forces every implementation to support an operation that breaks the abstraction, and lets callers do
things a stack shouldn't allow. An interface should hold only the operations that fit its concept.

Fix: drop the out-of-concept method; if callers need sorted access, that is a different type or a copy into a
`List`.

```java
public interface Stack<E> {
  E pop();
  void push(E e);
  E peek();
  boolean isEmpty();
  int size();
}
```

Handle: keep an interface to the operations that fit its concept; a method that contradicts the abstraction (sort
on a stack) forces every implementer to support the wrong thing.

</details>

</details>
