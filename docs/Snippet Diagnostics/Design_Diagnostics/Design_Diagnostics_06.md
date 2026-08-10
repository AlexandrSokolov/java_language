### Describe a code snippet #51
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Version {
  private final int[] parts;   // e.g. {1, 4, 2}

  public Version(int[] parts) {
    this.parts = parts;
  }

  public int[] parts() {
    return parts;
  }
}
```

</details>

<details><summary>Show answer</summary>

Both ends leak, same scope (an "immutable" value that isn't). The constructor stores the caller's array, so the
caller can mutate `parts` afterward; and `parts()` returns the same live array, so any caller can change it too.
The class calls itself `final` and holds a `final` field, but the array inside is fully mutable from two
directions.

Fix: copy in and copy out.

```java
public Version(int[] parts) {
  this.parts = parts.clone();       // in
}
public int[] parts() {
  return parts.clone();             // out
}
```

Handle: a `final` array field is not immutable; copy on the way in and on the way out to make the value truly
fixed.

</details>

</details>

---

### Describe a code snippet #52
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Button {
  private List<Listener> listeners = new ArrayList<>();

  public List<Listener> getListeners() {
    return listeners;
  }
}

button.getListeners().add(myListener);   // "registering" a listener
```

</details>

<details><summary>Show answer</summary>

Callers register by reaching into the internal list through the getter and calling `add` on it. That exposes the
class's own collection as the API, so callers can also `clear()` it, remove others' listeners, or iterate it while
another thread changes it. The class has no control over its own listener set. Handing out the mutable internal
list *as* the mechanism is broken encapsulation.

Fix: give explicit methods and keep the list private.

```java
public void addListener(Listener l)    { listeners.add(Objects.requireNonNull(l)); }
public void removeListener(Listener l) { listeners.remove(l); }
```

Handle: expose add/remove methods, not the internal collection; a getter that returns the live list makes callers
co-owners of your state.

</details>

</details>

---

### Describe a code snippet #53
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Rectangle {
  protected int width, height;
  public void setWidth(int w)  { this.width = w; }
  public void setHeight(int h) { this.height = h; }
  public int area() { return width * height; }
}

public class Square extends Rectangle {
  @Override public void setWidth(int w)  { width = height = w; }
  @Override public void setHeight(int h) { width = height = h; }
}
```

</details>

<details><summary>Show answer</summary>

`Square extends Rectangle` looks natural ("a square is a rectangle") but breaks substitutability. Code written
against `Rectangle` assumes setting width leaves height alone — `r.setWidth(5); r.setHeight(4); assert area == 20`.
Pass a `Square` and that assertion fails, because setting one side changes both. The subclass violates the
superclass's contract, so a `Square` cannot stand in for a `Rectangle` (a Liskov violation).

Fix: don't model `Square` as a subtype of a mutable `Rectangle`. Make them separate types, or use immutable shapes
where the constraint holds by construction.

```java
public final class Square {
  private final int side;   // no setters, no broken contract
  public int area() { return side * side; }
}
```

Handle: a subclass must honor the superclass contract; `Square` under a mutable `Rectangle` breaks it and can't
substitute for one.

</details>

</details>

---

### Describe a code snippet #54
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Boolean flag = new Boolean(true);
Integer count = new Integer(5);
String label = new String("active");
```

</details>

<details><summary>Show answer</summary>

Every line allocates a new object where a shared or literal one would do. `new Boolean(true)` makes a fresh
`Boolean` instead of using `Boolean.TRUE`; `new Integer(5)` skips the cached small-integer instances; `new
String("active")` makes a second `String` that duplicates the interned literal. In a loop these become millions of
needless objects. (These constructors are also deprecated for exactly this reason.)

Fix: use the literal or the valueOf/autoboxing form, which reuse cached instances.

```java
boolean flag = true;              // or Boolean.TRUE
Integer count = 5;                // autoboxes via Integer.valueOf, cached for small ints
String label = "active";          // the interned literal
```

Handle: don't `new` a `Boolean`/`Integer`/`String`; use literals or `valueOf`, which reuse cached instances
instead of allocating.

</details>

</details>

---

### Describe a code snippet #55
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public void connect() throws IOException, SQLException,
                             TimeoutException, InterruptedException {
  // ...
}
```

Callers mostly can't do anything different for each type.

</details>

<details><summary>Show answer</summary>

Four unrelated checked exceptions in one signature force every caller to write four catch blocks (or declare all
four upward), even though almost no caller can act differently on a `SQLException` than on an `IOException` — they
all mean "connect failed." The signature pushes ceremony onto callers for distinctions they can't use. This is the
case *for* translating low-level checked exceptions into one meaningful type.

Fix: catch the low-level types at the boundary and throw one exception that means "connection failed," keeping the
cause.

```java
public void connect() throws ConnectionException {
  try {
    // ...
  } catch (IOException | SQLException | TimeoutException e) {
    throw new ConnectionException("connect failed", e);
  }
}
```

Note the reverse mistake exists too: collapsing exceptions that callers *do* branch on into one type loses
information they needed. The test is whether callers act on the difference.

Handle: translate a pile of low-level checked exceptions into one meaningful type when callers can't act on the
difference; keep the cause.

</details>

</details>

---

### Describe a code snippet #56
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Duration {
  private final long millis;
  private Duration(long millis) { this.millis = millis; }

  public static Duration ofMillis(long m) { return new Duration(m); }
  public static Duration ZERO = ofMillis(0);
}
```

</details>

<details><summary>Show answer</summary>

The intent — a static factory plus a shared `ZERO` constant — is good, but `ZERO` is a mutable `public static`
field: any code can reassign `Duration.ZERO = ofMillis(999)`, and every later reader sees the wrong value.
A well-known constant that can be reassigned is a global variable in disguise.

Fix: make the constant `final`.

```java
public static final Duration ZERO = ofMillis(0);
```

Since `Duration` itself is immutable, the `final` reference now gives a truly constant shared value — which is
exactly what a static factory returning cached instances is meant to enable.

Handle: make shared constants `public static final`; a non-final `public static` constant can be reassigned by
anyone.

</details>

</details>

---

### Describe a code snippet #57
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class EventLog {
  private final List<String> entries = new ArrayList<>();

  public void add(String e) { entries.add(e); }

  public Iterator<String> iterator() {
    return entries.iterator();
  }
}
```

</details>

<details><summary>Show answer</summary>

Handing out `entries.iterator()` exposes a *removing* iterator: a caller can call `it.remove()` and delete log
entries, even though the class offers no remove method and clearly means the log to be append-only. The iterator
is a back door into the internal list. Exposing the internal iterator leaks a mutation path the API never intended.

Fix: return an iterator over an unmodifiable view, whose `remove()` throws.

```java
public Iterator<String> iterator() {
  return Collections.unmodifiableList(entries).iterator();
}
// or implement Iterable and return the same
```

Handle: an internal `iterator()` lets callers `remove()`; wrap in an unmodifiable view to keep an append-only
collection append-only.

</details>

</details>

---

### Describe a code snippet #58
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Matrix {
  private final double[][] data;
  public Matrix(double[][] data) { this.data = data; }

  public double get(int r, int c) { return data[r][c]; }
}

double[][] input = readInput();
Matrix m = new Matrix(input);
input[0][0] = 999;   // changes the matrix
```

</details>

<details><summary>Show answer</summary>

The constructor stores the caller's 2-D array by reference, so `input[0][0] = 999` after construction changes what
the `Matrix` returns. Even a `clone()` here would only copy the outer array and still share the rows — a 2-D array
needs a deep copy. This is the defensive-copy-in defect on nested arrays, where the shallow fix is itself a trap.

Fix: deep-copy every row.

```java
public Matrix(double[][] data) {
  this.data = new double[data.length][];
  for (int i = 0; i < data.length; i++) {
    this.data[i] = data[i].clone();   // copy each row
  }
}
```

Handle: copy a 2-D array row by row on the way in; storing it directly (or a shallow `clone`) leaves the rows
shared with the caller.

</details>

</details>

---

### Describe a code snippet #59
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Api {
  public void fetch(String url, int timeout, boolean retry,
                    int retries, boolean cache, String userAgent) {
    // ...
  }
}
```

</details>

<details><summary>Show answer</summary>

Six positional parameters, several the same type, is unreadable and fragile at the call site:
`fetch(u, 30, true, 3, false, "ua")` gives no clue what each value means, and the two `int`s or two `boolean`s can
be swapped without a compile error. A long positional parameter list is a call-site hazard.

Fix: gather the optional/related settings into a small options object (or a builder), so each value is named.

```java
public void fetch(String url, RequestOptions opts) { /* ... */ }

api.fetch(url, RequestOptions.builder()
    .timeout(30).retries(3).cache(false).userAgent("ua").build());
```

Handle: collapse a long positional parameter list into a named options object or builder; positional args of the
same type swap silently.

</details>

</details>

---

### Describe a code snippet #60
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Repository {
  public User findById(long id) {
    User u = db.query(id);
    return u;   // returns null when not found
  }
}

User u = repo.findById(42);
String name = u.getName();
```

</details>

<details><summary>Show answer</summary>

`findById` returns `null` when the user is absent, and the caller uses the result straight away, so a missing user
becomes a `NullPointerException` at `u.getName()` — far from the lookup, with a message that says nothing about
"user 42 not found." Returning `null` for "not found" makes absence easy to forget and the failure land elsewhere.

Fix: return an `Optional<User>`, which makes the caller handle the absent case.

```java
public Optional<User> findById(long id) {
  return Optional.ofNullable(db.query(id));
}
User u = repo.findById(42).orElseThrow(() -> new NotFoundException(42));
```

Handle: return `Optional` for a lookup that can miss, not `null`; a bare `null` return defers the failure to a
later NPE.

</details>

</details>
