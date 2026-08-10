### Describe a code snippet #31
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Name {
  private final String first;
  private final String last;

  @Override public int hashCode() {
    return Objects.hash(first, last);
  }
  // no equals
}
```

</details>

<details><summary>Show answer</summary>

`hashCode` is overridden but `equals` is not, so `equals` still uses identity. Two `Name` objects with the same
first and last land in the same bucket (their hashes match), but the bucket's `equals` check compares identity and
says they differ — so a `HashSet` keeps both, and a `HashMap` treats them as separate keys. Overriding one half of
the pair and not the other is as broken as the reverse.

Fix: add `equals` over the same fields.

```java
@Override public boolean equals(Object o) {
  return o instanceof Name n && first.equals(n.first) && last.equals(n.last);
}
```

Handle: `equals` and `hashCode` come as a pair; a matching hash without a matching `equals` still stores
duplicates.

</details>

</details>

---

### Describe a code snippet #32
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class TextBox {
  public void render(boolean bold, boolean italic, boolean underline) {
    // ...
  }
}

box.render(true, false, true);
```

</details>

<details><summary>Show answer</summary>

`render(true, false, true)` is unreadable at the call site — nothing says which flag is which, and swapping two
`boolean`s compiles fine and renders wrong. Three `boolean` parameters is a signal that the method is really
carrying several independent on/off choices the caller can't name.

Fix: replace the booleans with a small set of named options — an `EnumSet` of styles reads clearly and can't be
mis-ordered.

```java
public void render(Set<Style> styles) { /* ... */ }
box.render(EnumSet.of(Style.BOLD, Style.UNDERLINE)); // names each choice
```

For a single flag, two methods (`renderBold()` / `renderPlain()`) or an enum parameter beat a bare `boolean`.

Handle: several `boolean` parameters read as a puzzle and swap silently; name the choices with an enum or an
`EnumSet`.

</details>

</details>

---

### Describe a code snippet #33
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Vehicle {
  void startEngine() { /* ... */ }
}

public class ParkingSpot {
  private Vehicle vehicle;
  void park(Vehicle v) { this.vehicle = v; }
}

public class ElectricCar extends ParkingSpot {  // reuse park()/vehicle
  void charge() { /* ... */ }
}
```

</details>

<details><summary>Show answer</summary>

`ElectricCar extends ParkingSpot` to reuse `park` and the `vehicle` field, but a car is not a kind of parking
spot — the "is-a" relationship is false. Inheritance was used purely to grab code, so an `ElectricCar` now
wrongly *is* a `ParkingSpot` everywhere the type is used, and can be passed to anything expecting a spot. This is
inheritance for reuse where composition was meant.

Fix: if the car needs a spot, it *has* one — hold it as a field.

```java
public class ElectricCar extends Vehicle {
  private ParkingSpot spot;   // has-a, not is-a
  void charge() { /* ... */ }
}
```

Handle: extend only when "is-a" is truly true; reaching for `extends` just to reuse code creates a false subtype.

</details>

</details>

---

### Describe a code snippet #34
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Report {
  private final String title;
  private final List<Row> rows;

  public Report(String title, List<Row> rows) {
    this.title = title;
    this.rows = Collections.unmodifiableList(rows);
  }
}
```

</details>

<details><summary>Show answer</summary>

`Collections.unmodifiableList` wraps the caller's list but does not copy it. The wrapper blocks changes made
*through the wrapper*, but the caller still holds the original mutable list and can add or remove rows through it
— and the `Report` sees those changes. An unmodifiable *view* over a live list is not an immutable snapshot.

Fix: copy first, so the `Report` owns the data; `List.copyOf` both copies and makes it unmodifiable.

```java
this.rows = List.copyOf(rows);   // independent, unmodifiable copy
```

Handle: `unmodifiableList` is a read-only *view* of a live list, not a copy; use `List.copyOf` to get an
independent snapshot.

</details>

</details>

---

### Describe a code snippet #35
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public boolean isValid(String input) {
  try {
    parse(input);
    return true;
  } catch (ParseException e) {
    return false;
  }
}
```

`parse` is called in a tight loop over millions of inputs, most invalid.

</details>

<details><summary>Show answer</summary>

The method uses an exception as the normal signal for "invalid," and most inputs are invalid, so the hot path
throws and catches millions of times. Building an exception fills in a stack trace, which is expensive, so this
turns a validity check into a performance sink. Exceptions are for exceptional cases, not for a result you expect
routinely.

Fix: give `parse` (or a sibling) a way to report failure as a return value, and branch on that.

```java
public boolean isValid(String input) {
  return tryParse(input).isPresent();   // no throw on the common path
}
```

Handle: don't use exceptions for expected outcomes; a throw-per-item on a hot path pays the stack-trace cost every
time.

</details>

</details>

---

### Describe a code snippet #36
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Settings {
  private final Map<String, String> values;

  public Settings(Map<String, String> values) {
    this.values = new HashMap<>(values);   // copied in — good
  }

  public Map<String, String> getValues() {
    return values;
  }
}
```

</details>

<details><summary>Show answer</summary>

The copy-in is right, but `getValues()` returns the internal `HashMap` live. A caller can call
`settings.getValues().clear()` and wipe the settings from outside — the defensive copy on the way in is undone by
the leak on the way out. Both directions have to be closed; guarding only one leaves the object mutable.

Fix: hand back a read-only view or a copy.

```java
public Map<String, String> getValues() {
  return Collections.unmodifiableMap(values);   // read-only view
}
// or return Map.copyOf(values) for an independent snapshot
```

Handle: copy in *and* wrap out; a defensive copy on the constructor is useless if the getter returns the live
map.

</details>

</details>

---

### Describe a code snippet #37
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Task {
  private Runnable action;

  public Task(Runnable action) {
    if (action == null) {
      // allow null, default later
    }
    this.action = action;
  }

  public void run() {
    action.run();
  }
}
```

</details>

<details><summary>Show answer</summary>

A `null` `action` is accepted by the constructor and stored, and the failure is deferred to `run()`, which throws
`NullPointerException` far from the real mistake — the caller who passed `null`. For a parameter that is *stored*
and used later, the check belongs at the point of storage, so the bad call fails immediately with a clear message
naming the parameter.

Fix: reject `null` where it is stored.

```java
public Task(Runnable action) {
  this.action = Objects.requireNonNull(action, "action");
}
```

Note the contrast with a *pass-through* parameter (used once, right away): there the immediate use throws on its
own, so an explicit check adds little. The stored-parameter case is the one that needs the up-front guard.

Handle: null-check a parameter you store in the constructor; deferring it makes the failure surface later, far from
the caller.

</details>

</details>

---

### Describe a code snippet #38
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Employee {
  private String name;

  @Override public boolean equals(Object o) {
    if (!(o instanceof Employee)) return false;
    return name.equals(((Employee) o).name);
  }
  @Override public int hashCode() {
    return name.hashCode();
  }
}
```

`name` has no setter shown, but is not `final`, and the class allows a `setName`.

</details>

<details><summary>Show answer</summary>

`equals` and `hashCode` are built on `name`, but `name` is mutable. Put an `Employee` into a `HashSet`, then
change its `name`, and its hash code changes — the object is now in the wrong bucket, so `set.contains(sameEmployee)`
returns `false` even though the object is in the set. A mutable field driving `hashCode` breaks hash collections
the moment the field changes.

Fix: base `equals`/`hashCode` only on fields that do not change after construction — ideally make them `final`.

```java
private final String name;   // stable key; hashCode stays valid in a set
```

Handle: don't compute `hashCode` from a mutable field; changing it after insertion loses the object in a hash
collection.

</details>

</details>

---

### Describe a code snippet #39
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class HttpClient {
  public String fetch(String url) {
    // ...
    if (status == 404) {
      throw new RuntimeException("not found");
    }
    // ...
  }
}
```

</details>

<details><summary>Show answer</summary>

A 404 is a normal, expected outcome of an HTTP call, but it is thrown as a bare `RuntimeException` — the most
generic unchecked type. A caller who wants to handle "not found" specially cannot catch it without catching every
other `RuntimeException` too (including bugs), and cannot tell this failure apart from any other. The type carries
no information and forces callers to match on the message string.

Fix: throw a specific type callers can catch precisely — or, since "not found" is expected, return it as a result.

```java
public Optional<String> fetch(String url) { /* empty for 404 */ }
// or, if it must throw:
throw new NotFoundException(url);   // a specific, catchable type
```

Handle: throw a specific exception type callers can catch, not a bare `RuntimeException`; an expected outcome may
belong in the return type instead.

</details>

</details>

---

### Describe a code snippet #40
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Base {
  public Base() {
    init();
  }
  protected void init() { }
}

public class Derived extends Base {
  private String config = "default";

  @Override protected void init() {
    this.config = load();
  }
}
```

</details>

<details><summary>Show answer</summary>

Same root as the constructor-calls-overridable-method trap, framed as a lifecycle-hook `init()`. `Base()` calls
`init()`, which `Derived` overrides. The base constructor runs before `Derived`'s field initializers, so inside
the overridden `init()` the assignment `this.config = load()` happens — and then, *after* the constructor chain,
`Derived`'s field initializer runs `config = "default"` and overwrites it. The hook's work is silently undone.

Fix: don't call an overridable "init" hook from the constructor. Initialize in the subclass constructor, or use a
static factory that constructs then calls `init` explicitly.

```java
public static Derived create() {
  Derived d = new Derived();  // fully constructed, field initializers done
  d.init();                   // now the hook runs and sticks
  return d;
}
```

Handle: an overridable `init()` called from a constructor runs before subclass fields initialize — and their
initializers then overwrite its work.

</details>

</details>
