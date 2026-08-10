### Describe a code snippet #41
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Coordinate {
  private final double lat;
  private final double lon;

  @Override public boolean equals(Object o) {
    if (!(o instanceof Coordinate)) return false;
    Coordinate c = (Coordinate) o;
    return lat == c.lat && lon == c.lon;
  }
  @Override public int hashCode() {
    return (int) (lat + lon);
  }
}
```

</details>

<details><summary>Show answer</summary>

Two problems, both in `hashCode`, same scope. First, casting `lat + lon` to `int` throws away the fractional part
— `(1.1, 2.2)` and `(1.4, 1.9)` and thousands of other pairs all hash to `3`, so unequal coordinates pile into a
few buckets. Second, adding the two fields ignores their order, so `(a, b)` and `(b, a)` collide. The result is a
legal but terrible hash that pushes map operations toward linear time.

Fix: use `Double.hashCode` on each field and combine them order-sensitively.

```java
@Override public int hashCode() {
  return Objects.hash(lat, lon);   // handles doubles and keeps order
}
```

Handle: don't cast a sum of doubles to `int` for `hashCode`; use `Objects.hash`, which keeps precision and field
order.

</details>

</details>

---

### Describe a code snippet #42
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class ReportBuilder {
  private String title;
  private List<Section> sections;

  public ReportBuilder title(String t) { this.title = t; return this; }
  public ReportBuilder add(Section s)  { sections.add(s); return this; }

  public Report build() {
    return new Report(title, sections);
  }
}

Report r = new ReportBuilder().title("Q3").build();  // never called add()
```

</details>

<details><summary>Show answer</summary>

Two defects in the builder, same scope (an object that can be built invalid). `sections` is never initialized, so
`add()` throws `NullPointerException`; and `build()` will happily produce a `Report` with a `null` (or empty)
sections and no required fields checked — `build()` lets the object be created in an invalid state. A builder's
job is to guarantee a valid object or fail loudly.

Fix: initialize collections, and validate required fields in `build()` before constructing.

```java
private final List<Section> sections = new ArrayList<>();
public Report build() {
  if (title == null) throw new IllegalStateException("title is required");
  return new Report(title, sections);
}
```

Handle: a builder must initialize its fields and check invariants in `build()`; otherwise it hands back an invalid
object or throws deep inside.

</details>

</details>

---

### Describe a code snippet #43
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Shape {
  public double area() { return 0; }
}

public final class Circle extends Shape {
  private final double r;
  public Circle(double r) { this.r = r; }
  public double area() { return Math.PI * r * r; }
}
```

</details>

<details><summary>Show answer</summary>

`Shape` is a concrete class with a meaningless default `area()` of `0`. Nothing stops `new Shape()`, which creates
a shape with zero area that is not any real shape — a nonsense object. And `area()` returning `0` in the base is a
silent wrong answer waiting for a subclass that forgets to override it. A base with no valid standalone meaning
should not be instantiable.

Fix: make `Shape` abstract and `area()` abstract, so no bare shape exists and every subclass must supply an area.

```java
public abstract class Shape {
  public abstract double area();   // no default, must be implemented
}
```

Handle: make a base class with no standalone meaning `abstract`; a concrete base invites nonsense objects and
silent default answers.

</details>

</details>

---

### Describe a code snippet #44
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public int[] getScores() {
  return scores;   // scores is a private int[] field
}
```

</details>

<details><summary>Show answer</summary>

The getter returns the internal array directly, so a caller can write `obj.getScores()[0] = 999` and change the
object's private data from outside. An array is always mutable — there is no read-only array — so handing back the
live field breaks encapsulation completely. This is the copy-out defect, on an array.

Fix: return a copy, or expose the data another way.

```java
public int[] getScores() {
  return scores.clone();   // caller gets their own array
}
// or expose as a read-only list:
public List<Integer> getScores() { return List.of(/* boxed */); }
```

Handle: never return a private array from a getter; there is no immutable array, so return a `clone` or an
unmodifiable list.

</details>

</details>

---

### Describe a code snippet #45
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public String describe() {
  return "User{" + name + ", ssn=" + ssn + "}";
}
```

</details>

<details><summary>Show answer</summary>

Two things in the same scope (the `toString`-style method's contract). First, the method is named `describe`
rather than overriding `toString`, so logging frameworks, debuggers, and string concatenation that call
`toString` get the useless default (`User@1a2b3c`) instead of this. Second, it puts the `ssn` — sensitive data —
into a string that will land in logs and stack traces. A `toString` should identify the object without leaking
secrets.

Fix: override `toString` and leave sensitive fields out.

```java
@Override public String toString() {
  return "User{name=" + name + "}";   // no ssn
}
```

Handle: override `toString` (not a custom name) so tools use it, and keep secrets out of it — it ends up in logs.

</details>

</details>

---

### Describe a code snippet #46
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public void writeAll(List<String> lines) {
  Writer w = null;
  try {
    w = new FileWriter("out.txt");
    for (String line : lines) w.write(line);
  } finally {
    try {
      w.close();
    } catch (IOException e) {
      return;   // give up on close error
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

If the `try` body throws (say a disk-full `IOException` during `write`), control goes to `finally`, and if
`w.close()` also throws, the `catch` does `return`. A `return` in a `finally` block *discards* the exception that
was propagating from the `try` — the original write failure is swallowed and the method returns normally, so the
caller thinks the write succeeded. A `finally` that returns (or throws) erases the real exception.

Fix: never `return` or `throw` from `finally`. Use try-with-resources, which closes correctly and keeps the
primary exception (attaching the close failure as suppressed).

```java
public void writeAll(List<String> lines) throws IOException {
  try (Writer w = new FileWriter("out.txt")) {
    for (String line : lines) w.write(line);
  }
}
```

Handle: a `return` in `finally` swallows a propagating exception; use try-with-resources instead of a manual
`finally` that can return.

</details>

</details>

---

### Describe a code snippet #47
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Money {
  private final long cents;
  public Money(long cents) { this.cents = cents; }
  // equals / hashCode correct, based on cents
}

public class TaxedMoney extends Money {
  private final double rate;
  public TaxedMoney(long cents, double rate) { super(cents); this.rate = rate; }
  @Override public boolean equals(Object o) {
    if (!(o instanceof TaxedMoney)) return false;
    return super.equals(o) && ((TaxedMoney) o).rate == rate;
  }
}
```

</details>

<details><summary>Show answer</summary>

Adding the value field `rate` to a subclass of an instantiable `Money` cannot preserve the `equals` contract. A
`Money(100)` and a `TaxedMoney(100, 0.2)` compare asymmetrically: `money.equals(taxed)` uses `Money.equals` and
returns `true` (same cents), while `taxed.equals(money)` fails the `instanceof TaxedMoney` check and returns
`false`. Symmetry is broken. There is no way to extend a concrete class with a new value component and keep
`equals` correct.

Fix: favor composition — `TaxedMoney` *has* a `Money` rather than *is* one.

```java
public final class TaxedMoney {
  private final Money base;
  private final double rate;
  // equals compares base and rate; no inheritance, no asymmetry
}
```

Handle: adding a value field in a subclass of a concrete class breaks `equals` symmetry; compose instead of
extend.

</details>

</details>

---

### Describe a code snippet #48
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Parser {
  public List<Token> parse(String src, boolean strict, boolean trace) {
    // ...
  }
}
```

</details>

<details><summary>Show answer</summary>

Two `boolean` parameters mean the caller writes `parse(src, true, false)` with no hint which flag is which, and a
swap compiles and misbehaves silently. Each flag is an independent mode; bundling them as positional booleans hides
their meaning and multiplies the ways to call the method wrong.

Fix: name each mode. Separate enums, or a small options object, make the call self-describing.

```java
public List<Token> parse(String src, Mode mode, Tracing tracing) { /* ... */ }
parser.parse(src, Mode.STRICT, Tracing.OFF);   // reads itself
```

Handle: replace boolean mode flags with named enums or an options object; positional booleans read as a puzzle and
swap silently.

</details>

</details>

---

### Describe a code snippet #49
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Session {
  public Session() {
    connect();
  }
  private void connect() { /* opens a socket */ }
}
```

No `close()` and no cleanup path shown; `Session` is created per request.

</details>

<details><summary>Show answer</summary>

The constructor opens a socket but the class offers no way to release it — no `close()`, and it does not implement
`AutoCloseable`. Every `new Session()` leaks a socket (and its file descriptor), and under load the process runs
out of descriptors and fails. A class that acquires a resource must give the caller a way to release it, and make
that release fit try-with-resources.

Fix: implement `AutoCloseable` and release in `close()`, so callers can use try-with-resources.

```java
public class Session implements AutoCloseable {
  private final Socket socket = /* ... */;
  @Override public void close() { /* close socket */ }
}
// caller:
try (Session s = new Session()) { /* ... */ }
```

Handle: a class that acquires a resource must implement `AutoCloseable` and free it in `close()`; otherwise every
instance leaks.

</details>

</details>

---

### Describe a code snippet #50
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Temperature implements Comparable<Temperature> {
  private final double celsius;

  @Override public int compareTo(Temperature o) {
    return celsius < o.celsius ? -1 : (celsius > o.celsius ? 1 : 0);
  }

  @Override public boolean equals(Object o) {
    if (!(o instanceof Temperature)) return false;
    return Double.compare(celsius, ((Temperature) o).celsius) == 0;
  }
  // hashCode consistent with equals
}
```

</details>

<details><summary>Show answer</summary>

`compareTo` uses raw `<`/`>` on `double`, which mishandles the same edge cases `==` does: `Double.NaN` compared to
anything gives `0` here (neither `<` nor `>` is true), so a `NaN` temperature is treated as "equal in order" to
every other value, and `-0.0` vs `0.0` also disagree with what `Double.compare` (used in `equals`) says. That makes
`compareTo` inconsistent with `equals`, which breaks `TreeSet`/`TreeMap` — they use `compareTo`, not `equals`, and
will misplace or drop entries.

Fix: use `Double.compare` in `compareTo` too, so ordering and equality agree.

```java
@Override public int compareTo(Temperature o) {
  return Double.compare(celsius, o.celsius);
}
```

Handle: compare `double` in `compareTo` with `Double.compare`, matching `equals`; raw `<`/`>` mishandles `NaN` and
breaks sorted collections.

</details>

</details>
