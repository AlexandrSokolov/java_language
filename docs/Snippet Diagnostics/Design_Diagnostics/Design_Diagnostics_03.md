### Describe a code snippet #21
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class PhoneNumber {
  private final short areaCode, prefix, lineNum;

  @Override public boolean equals(Object o) {
    if (!(o instanceof PhoneNumber)) return false;
    PhoneNumber pn = (PhoneNumber) o;
    return pn.lineNum == lineNum && pn.prefix == prefix
        && pn.areaCode == areaCode;
  }
  // no hashCode
}
```

</details>

<details><summary>Show answer</summary>

`equals` compares the three number parts, but there is no `hashCode`, so equal phone numbers get different
identity-based hash codes. Use a `PhoneNumber` as a `HashMap` key, then look it up with an equal instance built
separately, and the map says the key is not there. This is the missing-`hashCode` trap again, framed on a value
type that is a natural map key — the failure is a lookup that silently misses.

Fix: derive `hashCode` from the same three fields.

```java
@Override public int hashCode() {
  return Objects.hash(areaCode, prefix, lineNum);
}
```

Handle: a value type used as a map key must override `hashCode` alongside `equals`, or lookups miss.

</details>

</details>

---

### Describe a code snippet #22
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Config {
  private final Properties props;

  public Config(Properties props) {
    this.props = props;
  }

  public String get(String key) {
    return props.getProperty(key);
  }
}
```

</details>

<details><summary>Show answer</summary>

`Properties` is mutable, and the constructor stores the caller's object directly. The caller keeps their
reference and can call `props.setProperty(...)` after construction, changing what `Config.get` returns from the
outside — the `Config` is not really holding fixed configuration. Same defensive-copy-in defect as a `Date` or
`List`, here on a `Properties`.

Fix: copy the incoming properties into the object's own store.

```java
public Config(Properties props) {
  this.props = new Properties();
  this.props.putAll(props);   // our own copy; caller's later edits don't reach us
}
```

Handle: copy a mutable argument of any type on the way in; storing the caller's `Properties` lets them edit your
state.

</details>

</details>

---

### Describe a code snippet #23
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Logger {
  private static Logger instance;

  private Logger() { }

  public static Logger getInstance() {
    if (instance == null) instance = new Logger();
    return instance;
  }
}
```

(Single-threaded use — ignore concurrency here.)

</details>

<details><summary>Show answer</summary>

Even leaving threads aside, this singleton is breakable. `getInstance()` is fine, but the private constructor
does not stop reflection (`setAccessible(true)` then `newInstance()`) from building a second `Logger`, and if the
class is ever made `Serializable`, deserializing produces another instance too. "There is exactly one" is not
actually enforced.

Fix: a single-element `enum` is the strongest singleton — the language guarantees one instance, and it is safe
against reflection and serialization.

```java
public enum Logger {
  INSTANCE;
  public void log(String msg) { /* ... */ }
}
```

Handle: an `enum` singleton is guaranteed one instance; a private-constructor singleton can be broken by
reflection and serialization.

</details>

</details>

---

### Describe a code snippet #24
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Registry {
  public void register(String key) { /* ... */ }
  public void register(Object key) { /* ... */ }
}

Registry r = new Registry();
String s = null;
r.register(s);
```

</details>

<details><summary>Show answer</summary>

Which `register` runs is chosen at *compile* time from the declared type of the argument, not the runtime value.
`s` is declared `String`, so `register(String)` is picked — even though `s` is `null`. Callers reading
`r.register(someObjectThatIsAString)` will often guess wrong about which overload fires, because overload
resolution follows the static type. Overloading two methods that could both accept the same argument is a trap.

Fix: don't overload on types where one could be the other; give the methods different names.

```java
public void registerKey(String key)  { /* ... */ }
public void registerItem(Object key) { /* ... */ }
```

Handle: overloads are chosen by the compile-time argument type, not the runtime object; distinct names remove the
guesswork.

</details>

</details>

---

### Describe a code snippet #25
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Temperature {
  private final double celsius;
  private Temperature(double celsius) { this.celsius = celsius; }

  public static Temperature ofCelsius(double c)   { return new Temperature(c); }
  public static Temperature ofFahrenheit(double f) { return new Temperature((f - 32) / 1.8); }
}
```

</details>

<details><summary>Show answer</summary>

Nothing is wrong here — this is the correct pattern, shown so you can name *why* it beats constructors. Two
constructors `Temperature(double)` would clash: both take one `double`, so you cannot have one for Celsius and
one for Fahrenheit. Static factory methods have names, so `ofCelsius` and `ofFahrenheit` read clearly at the call
site and can carry different conversions. Factories can also return a cached instance or a subtype, which a
constructor cannot.

The teaching point: a constructor is forced to be named after the class and cannot distinguish two same-shaped
inputs; a static factory can.

```java
Temperature t = Temperature.ofFahrenheit(98.6); // says what the number means
```

Handle: a static factory has a name and can differentiate same-typed inputs and return cached instances; a
constructor can do neither.

</details>

</details>

---

### Describe a code snippet #26
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class User {
  private final String email;

  public User(String email) {
    this.email = email;
  }

  public boolean sameAs(User other) {
    return this.email.equals(other.email);
  }
}
```

</details>

<details><summary>Show answer</summary>

The class defines its own `sameAs` for value equality instead of overriding `equals`. So `user1.equals(user2)`
still uses identity (the inherited `Object.equals`), which means `HashSet`, `HashMap`, `List.contains`,
`removeAll`, and every library that relies on `equals` treat two users with the same email as different. A custom
comparison method the collections don't know about buys nothing.

Fix: override `equals` (and `hashCode`) so the whole platform sees the value equality.

```java
@Override public boolean equals(Object o) {
  return o instanceof User u && email.equals(u.email);
}
@Override public int hashCode() { return email.hashCode(); }
```

Handle: put value equality in `equals`/`hashCode`, not a private method; collections only consult `equals`.

</details>

</details>

---

### Describe a code snippet #27
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Grid {
  private final int[][] cells;

  public Grid(int[][] cells) {
    this.cells = cells.clone();
  }
}
```

</details>

<details><summary>Show answer</summary>

`cells.clone()` copies only the outer array — the inner `int[]` rows are shared with the caller. The caller keeps
references to those rows and can still write `cells[0][0] = 9` and change the grid's data. `clone` on a nested
array is a *shallow* copy: it protects the top level and leaves the levels below exposed.

Fix: copy each row too (a deep copy).

```java
public Grid(int[][] cells) {
  this.cells = new int[cells.length][];
  for (int i = 0; i < cells.length; i++) {
    this.cells[i] = cells[i].clone();  // copy each row
  }
}
```

Handle: `clone` on a multi-dimensional array copies only the outer array; copy each inner array for a real defensive
copy.

</details>

</details>

---

### Describe a code snippet #28
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Cache<K, V> {
  private final Map<K, V> map = new HashMap<>();

  public V get(K key, Supplier<V> loader) {
    if (map.containsKey(key)) {
      return map.get(key);
    }
    V value = loader.get();
    map.put(key, value);
    return value;
  }
}
```

</details>

<details><summary>Show answer</summary>

Leaving threads aside, the `containsKey` then `get` is a wasteful double lookup on the hit path — the map is
searched twice for the same key on every cached read. And a stored `null` value is indistinguishable from a
missing key, so a legitimately cached `null` reloads every time. The two-call check is both slower and subtly
wrong.

Fix: one call that returns the value or loads it, so the map is searched once and `null` is handled.

```java
public V get(K key, Supplier<V> loader) {
  return map.computeIfAbsent(key, k -> loader.get());
}
```

Handle: replace check-then-get on a map with one `computeIfAbsent`; the two-call form double-searches and
mishandles stored nulls.

</details>

</details>

---

### Describe a code snippet #29
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Base {
  boolean equals(Base other) {
    return this.id == other.id;
  }
  int id;
}
```

</details>

<details><summary>Show answer</summary>

This defines `equals(Base)`, not `equals(Object)`, so it does **not** override `Object.equals` — it *overloads*
it. Collections call `equals(Object)`, which still uses identity, so this method is never the one the platform
invokes, and two `Base` objects with the same `id` are treated as different in a `HashSet`. Without `@Override`,
the compiler stays silent about the wrong signature.

Fix: take `Object` and add `@Override`, which makes the compiler reject a wrong signature.

```java
@Override public boolean equals(Object o) {
  return o instanceof Base b && id == b.id;
}
```

Handle: `equals` must take `Object`; `equals(MyType)` is an overload the collections never call — `@Override`
catches it.

</details>

</details>

---

### Describe a code snippet #30
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public void process(int[] data) {
  int first = data[0];
  // ...
}
```

`process` is a public API method.

</details>

<details><summary>Show answer</summary>

The public method reads `data[0]` with no checks. A `null` argument throws `NullPointerException` at `data[0]`; an
empty array throws `ArrayIndexOutOfBoundsException`. Both blow up *inside* the method with a message that points
at the implementation, not at the caller's mistake, so the caller cannot tell what they did wrong. A public method
must state and check what it requires of its arguments.

Fix: validate at the top, with a message naming the requirement.

```java
public void process(int[] data) {
  Objects.requireNonNull(data, "data");
  if (data.length == 0) throw new IllegalArgumentException("data must be non-empty");
  int first = data[0];
}
```

Handle: validate a public method's parameters up front; an unchecked bad argument fails deep inside with a
confusing message.

</details>

</details>
