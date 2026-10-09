### Describe a code snippet #81
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Tag {
  private final String label;
  public Tag(String label) { this.label = label; }

  @Override public boolean equals(Object o) {
    return o.toString().equals(this.toString());
  }
  @Override public String toString() { return label; }
}
```

</details>

<details><summary>Show answer</summary>

Two defects in one scope (a broken `equals`). First, `o` is never null-checked or type-checked, and `o.toString()`
throws `NullPointerException` when `o` is `null` — `equals` must return `false` for `null`, not throw. Second,
comparing by `toString()` means a `Tag` equals *any* object whose `toString()` yields the same string, so a `Tag("5")`
equals an unrelated object that prints `"5"` — equality leaks across types. `equals` must compare type and fields,
not string forms.

Fix: standard type-check-then-compare-fields.

```java
@Override public boolean equals(Object o) {
  return o instanceof Tag t && label.equals(t.label);
}
```

Handle: implement `equals` by checking type then comparing fields; comparing `toString()` throws on null and makes
unrelated types equal.

</details>

</details>

---

### Describe a code snippet #82
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class UserService {
  private final Database db = new Database("prod-host", 5432);

  public User find(long id) { return db.load(id); }
}
```

</details>

<details><summary>Show answer</summary>

`UserService` builds its own `Database` inside the class, hard-coding the host and port. There is no way to give
it a test database, a different environment, or a mock — the dependency is welded in. Creating a collaborator with
`new` inside the class makes the class impossible to test in isolation and impossible to reconfigure. The
dependency should be handed in.

Fix: inject the dependency through the constructor.

```java
public class UserService {
  private final Database db;
  public UserService(Database db) {
    this.db = Objects.requireNonNull(db);
  }
  public User find(long id) { return db.load(id); }
}
```

Handle: inject collaborators through the constructor instead of `new`-ing them inside; a hard-wired dependency
can't be swapped for tests or other environments.

</details>

</details>

---

### Describe a code snippet #83
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Base {
  public void process() {
    prepare();   // template method calls hook
    execute();
  }
  protected void prepare() { }
  protected void execute() { }
}

public class Sub extends Base {
  public void process() {   // note: no @Override, and redefines process
    execute();              // skips prepare()
    prepare();
  }
}
```

</details>

<details><summary>Show answer</summary>

`Sub.process()` re-declares `process` (with no `@Override`) and reverses the order, calling `execute()` before
`prepare()`. Because `Base.process` was not `final`, a subclass can override the very method that defines the
required sequence, breaking the invariant the base set up (prepare must run before execute). A base method that
enforces an order must not be overridable.

Fix: make the sequencing method `final` so subclasses fill in the hooks but can't reorder them.

```java
public final void process() {   // subclasses can't override the sequence
  prepare();
  execute();
}
```

Handle: make a template method that enforces an order `final`; if subclasses can override it, they can break the
sequence the base guarantees.

</details>

</details>

---

### Describe a code snippet #84
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Temperature {
  private final double value;
  private Temperature(double value) { this.value = value; }

  public static Temperature of(double value) {
    return new Temperature(value);
  }
}
```

Callers frequently create `Temperature.of(0)` and `Temperature.of(100)`.

</details>

<details><summary>Show answer</summary>

Nothing is broken, but the static factory is not using one of its main advantages: it always calls `new`, so every
`Temperature.of(0)` allocates a fresh object even though the value is immutable and commonly repeated. A
constructor is forced to return a new instance; a static factory can return a cached one. Here the factory throws
that away.

Fix: cache common instances and return them, so repeated calls share one object.

```java
private static final Map<Double, Temperature> CACHE = new ConcurrentHashMap<>();
public static Temperature of(double value) {
  return CACHE.computeIfAbsent(value, Temperature::new);
}
```

(Cache only a bounded, common set — an unbounded cache of every value asked for is its own leak.)

Handle: a static factory can return cached instances for immutable values; always calling `new` wastes the main
advantage over a constructor.

</details>

</details>

---

### Describe a code snippet #85
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class FileProcessor {
  public void process(String path) {
    FileInputStream in = new FileInputStream(path);
    try {
      read(in);
    } catch (IOException e) {
      log.error("read failed");
    }
    in.close();
  }
}
```

</details>

<details><summary>Show answer</summary>

`in.close()` sits *outside* the `try`, so if `read(in)` throws, control leaves before `close()` and the stream
leaks — one leaked file handle per failed call, until the process runs out. The resource is not released on the
error path. (Also, the `catch` logs a bare message with no cause, hiding what failed — a secondary point in the
same scope.)

Fix: use try-with-resources, which closes the stream on every path, success or failure.

```java
public void process(String path) throws IOException {
  try (FileInputStream in = new FileInputStream(path)) {
    read(in);
  }
}
```

Handle: acquire resources in try-with-resources; a `close()` after the `try` is skipped when the body throws, and
the resource leaks.

</details>

</details>

---

### Describe a code snippet #86
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Configuration {
  private final Map<String, String> settings;

  public Configuration(Map<String, String> settings) {
    this.settings = Map.copyOf(settings);   // copied in
  }

  public Configuration withOverride(String key, String value) {
    settings.put(key, value);               // mutate and return this-ish
    return this;
  }
}
```

</details>

<details><summary>Show answer</summary>

The class is designed as immutable (final, copied-in map) but `withOverride` calls `settings.put(...)` on it. Since
`Map.copyOf` returns an *unmodifiable* map, this throws `UnsupportedOperationException` at runtime. Even if the map
were mutable, mutating in a method named like an immutable "with" builder would violate the immutability the class
promises. The method's shape (a `withX` that returns a config) says "make a new one," but the body mutates.

Fix: build and return a *new* configuration, leaving the original untouched.

```java
public Configuration withOverride(String key, String value) {
  Map<String, String> copy = new HashMap<>(settings);
  copy.put(key, value);
  return new Configuration(copy);   // new instance, original unchanged
}
```

Handle: a `withX` method on an immutable type must return a new instance, not mutate; mutating an unmodifiable copy
also throws at runtime.

</details>

</details>

---

### Describe a code snippet #87
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Shape {
  private final String type;
  public Shape(String type) { this.type = type; }

  public double area() {
    if (type.equals("circle")) return /* ... */ 0;
    if (type.equals("square")) return /* ... */ 0;
    throw new IllegalArgumentException("unknown: " + type);
  }
}
```

</details>

<details><summary>Show answer</summary>

Shape kind is carried as a `String` and dispatched with an `if`/`else` chain on that string. Adding a new shape
means editing this method (and every other method that switches on `type`), a typo like `"cirlce"` compiles and
fails at runtime, and the data a circle needs (radius) versus a square (side) has no place to live. Modeling a
type hierarchy as a string tag pushes all behavior into string switches.

Fix: use a subtype per kind (or a sealed hierarchy), so each shape carries its own data and behavior and the
compiler enforces coverage.

```java
sealed interface Shape permits Circle, Square { double area(); }
record Circle(double r) implements Shape { public double area() { return Math.PI*r*r; } }
record Square(double s) implements Shape { public double area() { return s*s; } }
```

Handle: model distinct kinds as subtypes, not a `String` tag with `if`/`else`; a string type field scatters
switches and accepts typos.

</details>

</details>

---

### Describe a code snippet #88
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public boolean equals(Object o) {
  if (this == o) return true;
  if (o == null || getClass() != o.getClass()) return false;
  Animal a = (Animal) o;
  return name.equals(a.name);
}
```

`Animal` has subclasses `Dog` and `Cat` that add no value fields, and callers compare a `Dog` with an `Animal`
holding the same name expecting equality.

</details>

<details><summary>Show answer</summary>

Using `getClass() != o.getClass()` means an `Animal("Rex")` and a `Dog("Rex")` are never equal, even though the
subclasses add no new value component — the comparison demands the exact same runtime class. That breaks
substitutability: a `Dog` cannot be equal to the `Animal` it is, so code that mixes the base and subtype in a set
sees them as distinct. When subclasses add no value fields, `instanceof` is the right check, not `getClass`.

Fix: use `instanceof`, which lets a subtype with no new fields compare equal to the base.

```java
public boolean equals(Object o) {
  if (!(o instanceof Animal a)) return false;
  return name.equals(a.name);
}
```

(There is a real trade-off: `getClass` is chosen when subclasses *do* add value fields and must never compare
equal to the base. The point of this card is that `getClass` breaks substitutability when they don't.)

Handle: use `instanceof` in `equals` so value-equal subtypes match; `getClass` forbids a subclass from equaling
its base even when it adds nothing.

</details>

</details>

---

### Describe a code snippet #89
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Order {
  private final List<Item> items = new ArrayList<>();
  public List<Item> getItems() { return items; }
  public BigDecimal total() {
    return items.stream().map(Item::price).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
```

`total` is documented as "the authoritative order total."

</details>

<details><summary>Show answer</summary>

`getItems()` returns the live internal list, so a caller can add or remove items through it — and `total()` then
reflects a list the `Order` never validated (no stock check, no price rules, no audit). The class claims `total`
is authoritative, but its inputs can be changed from outside without going through the order's own methods. The
leaked collection lets callers bypass the class's rules.

Fix: keep the list private, mutate only through validated methods, and expose a read-only view.

```java
public void addItem(Item i) { /* validate */ items.add(i); }
public List<Item> getItems() { return List.copyOf(items); }   // read-only
```

Handle: don't leak a mutable collection that feeds an invariant; force changes through validated methods and hand
out a read-only view.

</details>

</details>

---

### Describe a code snippet #90
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public Optional<User> findUser(long id) {
  User u = db.load(id);
  if (u == null) {
    return null;   // "no user"
  }
  return Optional.of(u);
}
```

</details>

<details><summary>Show answer</summary>

The method returns `Optional<User>` — whose whole purpose is to represent "maybe absent" — and then returns
`null` for the absent case. A caller who correctly writes `findUser(id).ifPresent(...)` gets a
`NullPointerException` on the `null` Optional itself. Returning `null` from a method that returns `Optional`
defeats the type and is worse than returning a plain `null`, because callers trust the Optional not to be null.

Fix: return an empty Optional, never a null one.

```java
public Optional<User> findUser(long id) {
  return Optional.ofNullable(db.load(id));
}
```

Handle: a method returning `Optional` must never return `null`; use `Optional.empty()`/`ofNullable`, or callers
NPE on the Optional itself.

</details>

</details>
