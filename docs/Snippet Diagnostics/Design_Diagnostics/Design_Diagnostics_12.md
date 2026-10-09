### Describe a code snippet #121
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Product {
  private final String sku;
  private int viewCount;   // changes constantly

  public Product(String sku) { this.sku = sku; }

  @Override public boolean equals(Object o) {
    if (!(o instanceof Product)) return false;
    Product p = (Product) o;
    return sku.equals(p.sku) && viewCount == p.viewCount;
  }
  @Override public int hashCode() { return Objects.hash(sku, viewCount); }
}
```

</details>

<details><summary>Show answer</summary>

Identity is meant to be the `sku`, but `equals`/`hashCode` also fold in `viewCount`, which changes on every page
view. So the same product stops being equal to itself after a view, and its hash code changes — losing it in any
`HashSet`/`HashMap`. A field that changes constantly must never take part in `equals`/`hashCode`.

Fix: base both on the stable key alone.

```java
@Override public boolean equals(Object o) {
  return o instanceof Product p && sku.equals(p.sku);
}
@Override public int hashCode() { return sku.hashCode(); }
```

Handle: keep volatile, fast-changing fields out of `equals`/`hashCode`; identity must rest on stable fields only.

</details>

</details>

---

### Describe a code snippet #122
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class UserProfile {
  private final String name;
  private final String email;
  private final Address address;
  private final List<Role> roles;

  public UserProfile(String name) {
    this(name, null, null, null);
  }
  public UserProfile(String name, String email) {
    this(name, email, null, null);
  }
  public UserProfile(String name, String email, Address address) {
    this(name, email, address, null);
  }
  public UserProfile(String name, String email, Address address, List<Role> roles) {
    // ...
  }
}
```

</details>

<details><summary>Show answer</summary>

This is the telescoping-constructor pattern: a chain of constructors of growing length. A caller who wants to set
`name` and `roles` but not `email`/`address` has no constructor for that — they must pass `null` for the middle
ones, and every added optional field doubles the awkwardness. The call site also can't tell which `null` means
what. Telescoping constructors don't scale to several optional fields.

Fix: a builder, so each optional field is named and only the ones you want are set.

```java
UserProfile u = new UserProfile.Builder("Ada")
    .roles(List.of(Role.ADMIN))     // set roles, skip email/address
    .build();
```

Handle: replace telescoping constructors with a builder when there are several optional fields; the constructor
chain forces `null`-passing and can't express arbitrary combinations.

</details>

</details>

---

### Describe a code snippet #123
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Cache<K, V> {
  private final Map<K, V> map = new HashMap<>();

  public V get(K key) { return map.get(key); }
  public void put(K key, V value) { map.put(key, value); }

  @Override protected void finalize() {
    map.clear();
  }
}
```

</details>

<details><summary>Show answer</summary>

The class uses `finalize()` to clean up. Finalizers are unpredictable — there is no guarantee they ever run, no
guarantee *when*, they slow down garbage collection, and they can resurrect objects; they are deprecated for
removal. Worse, `map.clear()` in a finalizer does nothing useful: once the `Cache` is unreachable, its `map` is
unreachable too and will be collected anyway. Relying on `finalize` for cleanup is broken.

Fix: don't use finalizers. If the class holds a resource that must be released, implement `AutoCloseable` and free
it in `close()`, used with try-with-resources. Here, delete the method entirely — there is nothing to release.

```java
// remove finalize() completely; for real resources:
public class Cache<K,V> implements AutoCloseable {
  @Override public void close() { /* release real resources here */ }
}
```

Handle: never use `finalize()` for cleanup — it may never run and is deprecated; use `AutoCloseable` +
try-with-resources for resources.

</details>

</details>

---

### Describe a code snippet #124
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class EventEmitter {
  private final List<Listener> listeners = new ArrayList<>();

  public void addListener(Listener l) { listeners.add(l); }

  public void emit(Event e) {
    for (Listener l : listeners) {
      l.onEvent(e);   // a listener may call removeListener during this
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

`emit` iterates `listeners` and calls out to each. If a listener's `onEvent` calls back into `addListener` or a
`removeListener` (common — a one-shot listener removes itself), the list is modified while the for-each iterates
it, throwing `ConcurrentModificationException`. Iterating a collection while foreign code you call can modify it is
the trap, and callbacks make it likely.

Fix: iterate over a snapshot, so re-entrant changes don't touch the list being walked.

```java
public void emit(Event e) {
  for (Listener l : List.copyOf(listeners)) {   // walk a copy
    l.onEvent(e);
  }
}
```

`CopyOnWriteArrayList` is the standard structure for a listener list for exactly this reason — its iterator walks a
stable snapshot.

Handle: iterate a snapshot when the loop calls out to code that can modify the collection; a re-entrant add/remove
during iteration throws.

</details>

</details>

---

### Describe a code snippet #125
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Temperature {
  private final int degrees;
  public Temperature(int degrees) { this.degrees = degrees; }

  public Temperature warmer(int by) {
    degrees += by;    // compile error? trace it
    return this;
  }
}
```

</details>

<details><summary>Show answer</summary>

`degrees` is `final`, so `degrees += by` does not compile — you cannot reassign a final field. Beyond the compile
error, the intent is wrong for an immutable value: a method like `warmer` should return a *new* `Temperature`, not
try to change this one. The design mixes "immutable value" with "mutate in place," and the `final` field correctly
blocks it.

Fix: return a new instance; leave the original unchanged.

```java
public Temperature warmer(int by) {
  return new Temperature(degrees + by);   // new value, original untouched
}
```

Handle: an operation on an immutable value returns a new instance; you can't reassign a `final` field, and you
shouldn't want to.

</details>

</details>

---

### Describe a code snippet #126
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public interface PaymentGateway {
  boolean charge(BigDecimal amount);
}
```

Implementations return `false` when a charge fails.

</details>

<details><summary>Show answer</summary>

`charge` reports failure by returning `false` — a single bit that says "it failed" but not *why*: declined,
network error, invalid card, insufficient funds. The caller can't tell a retryable failure from a permanent one,
and can't show the user a useful message. A boolean return collapses every distinct failure into one, and callers
routinely ignore a returned `boolean` anyway.

Fix: return a result type that carries the outcome, or throw a specific exception per failure kind.

```java
public interface PaymentGateway {
  ChargeResult charge(BigDecimal amount);   // SUCCESS / DECLINED / NETWORK_ERROR / ...
}
```

Handle: don't report a rich failure as a bare `boolean`; return a result type (or throw a specific exception) so
callers can tell failures apart and act.

</details>

</details>

---

### Describe a code snippet #127
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Path {
  private final List<String> segments;

  public Path(List<String> segments) {
    this.segments = new ArrayList<>(segments);   // copied in
  }

  public Path append(String segment) {
    segments.add(segment);   // mutates internal list
    return new Path(segments);
  }
}
```

</details>

<details><summary>Show answer</summary>

`append` is shaped like an immutable operation (it returns a new `Path`), but first it mutates *this* `Path`'s
internal `segments` by calling `add`. So calling `p.append("x")` changes `p` as a side effect, even though the
class is `final` with a copied-in list and looks immutable. The new `Path` and the old one also briefly share
state through the mutation. An immutable type's "derive a new value" method must not touch the current instance.

Fix: build the new list without mutating the current one.

```java
public Path append(String segment) {
  List<String> copy = new ArrayList<>(segments);
  copy.add(segment);
  return new Path(copy);        // this Path is untouched
}
```

Handle: a derive-new-value method on an immutable type must not mutate the current instance; build a fresh list and
pass that to the new object.

</details>

</details>

---

### Describe a code snippet #128
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Vehicle {
  private final Engine engine = new GasEngine();

  public void start() { engine.start(); }
}
```

The team needs electric and hybrid vehicles too.

</details>

<details><summary>Show answer</summary>

The `Vehicle` hard-codes `new GasEngine()`, so it can never be an electric or hybrid vehicle without editing this
class, and it can't be tested with a fake engine. The class picks its own collaborator instead of being given one,
which welds it to a single implementation. The dependency should be supplied from outside.

Fix: inject the `Engine` through the constructor (program to the interface, not the concrete type).

```java
public class Vehicle {
  private final Engine engine;
  public Vehicle(Engine engine) {
    this.engine = Objects.requireNonNull(engine);
  }
  public void start() { engine.start(); }
}
// new Vehicle(new ElectricEngine());
```

Handle: inject a collaborator through the constructor and depend on its interface; `new`-ing a concrete
implementation inside welds the class to one variant.

</details>

</details>

---

### Describe a code snippet #129
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Matrix {
  private final int[][] data;
  public Matrix(int[][] data) {
    this.data = deepCopy(data);
  }

  public int[][] raw() {
    return data;
  }
}
```

</details>

<details><summary>Show answer</summary>

The constructor deep-copies on the way in (good), but `raw()` returns the internal 2-D array directly, so a caller
can write `m.raw()[0][0] = 9` and mutate the matrix's private data — including the inner rows. The careful copy-in
is undone by the copy-out leak, and a shallow copy here wouldn't be enough either, since the rows are shared. This
is the copy-out defect on nested arrays.

Fix: deep-copy on the way out too (or expose read-only access instead of the raw array).

```java
public int[][] raw() {
  int[][] copy = new int[data.length][];
  for (int i = 0; i < data.length; i++) copy[i] = data[i].clone();
  return copy;
}
// better: expose int get(int r, int c) and never hand out the array
```

Handle: deep-copy a 2-D array on the way out, or don't expose it; a getter returning the internal array undoes the
defensive copy-in.

</details>

</details>

---

### Describe a code snippet #130
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class TaxCalculator {
  public BigDecimal calculate(Order o) {
    if (o.country().equals("US")) {
      // ... 30 lines of US rules
    } else if (o.country().equals("UK")) {
      // ... 30 lines of UK rules
    } else if (o.country().equals("DE")) {
      // ... 30 lines of DE rules
    }
    throw new IllegalArgumentException("no rules for " + o.country());
  }
}
```

</details>

<details><summary>Show answer</summary>

All tax logic for every country lives in one growing `if`/`else` chain keyed on a country string. Every new country
edits this method, the file grows without bound, a typo'd country code silently falls through to the exception, and
the rules for different countries can't be tested or shipped independently. This is a place where distinct
behaviors keyed on a value should be separate objects. (Same shape as the string-tagged `Shape`, framed on
strategy selection.)

Fix: put each country's rules behind a `TaxRule` interface and look up the right one — a strategy per country.

```java
public interface TaxRule { BigDecimal calculate(Order o); }
private final Map<String, TaxRule> rules = Map.of(
    "US", new UsTaxRule(), "UK", new UkTaxRule(), "DE", new DeTaxRule());

public BigDecimal calculate(Order o) {
  TaxRule rule = rules.get(o.country());
  if (rule == null) throw new IllegalArgumentException("no rules for " + o.country());
  return rule.calculate(o);
}
```

Handle: replace a growing `if`/`else` chain of behaviors keyed on a value with a strategy per value looked up in a
map; the chain doesn't scale and swallows typos.

</details>

</details>
