### Describe a code snippet #11
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Team {
  private final List<Player> players;

  public Team(List<Player> players) {
    this.players = List.copyOf(players); // copied in — good
  }

  public List<Player> getPlayers() {
    return players;
  }
}
```

</details>

<details><summary>Show answer</summary>

The copy on the way *in* is correct, but `getPlayers()` returns the internal list directly. Here `players` is an
immutable `List.copyOf`, so a caller cannot mutate it — but if the field were an `ArrayList` (a common variant of
this bug), the getter would hand the caller a live reference to the class's own list, and the caller could add or
remove players from outside. This card drills the *out* side of defensive copying, the twin of copy-in.

Fix (when the field is mutable): return a copy or an unmodifiable view.

```java
public List<Player> getPlayers() {
  return List.copyOf(players);       // snapshot the caller cannot change
}
// or, if callers must see later changes:
return Collections.unmodifiableList(players); // live view, but read-only
```

Since this field is already immutable, returning it directly is fine — the card's point is the *pattern*: a
getter must not leak a mutable internal collection.

Handle: copy or wrap a mutable collection on the way out; a getter that returns the live field lets callers
mutate your internals.

</details>

</details>

---

### Describe a code snippet #12
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Widget {
  public Widget(EventBus bus) {
    bus.register(this); // hand 'this' to another object mid-construction
    this.name = loadName();
  }
  private final String name;
}
```

</details>

<details><summary>Show answer</summary>

The constructor passes `this` to `bus.register` before construction finishes. The bus — possibly on another
thread — now holds a reference to a half-built `Widget` whose `name` field is not yet set, so any callback that
reads `name` sees `null`. Letting `this` escape during construction publishes an object that isn't finished.

Fix: finish construction first, then register in a separate step (a static factory that builds, then registers).

```java
public static Widget create(EventBus bus) {
  Widget w = new Widget();   // fully built
  bus.register(w);           // now safe to publish
  return w;
}
```

Handle: don't let `this` escape from a constructor; a listener can see the object before its fields are set.

</details>

</details>

---

### Describe a code snippet #13
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class InstrumentedHashSet<E> extends HashSet<E> {
  private int addCount = 0;

  @Override public boolean add(E e) {
    addCount++;
    return super.add(e);
  }
  @Override public boolean addAll(Collection<? extends E> c) {
    addCount += c.size();
    return super.addAll(c);
  }
  public int getAddCount() { return addCount; }
}

var s = new InstrumentedHashSet<String>();
s.addAll(List.of("a", "b", "c"));
```

</details>

<details><summary>Show answer</summary>

`getAddCount()` returns 6, not 3. `HashSet.addAll` is written to call `this.add` internally for each element, so
each of the three elements is counted twice: once by the overridden `addAll` (`+= c.size()`) and once by the
overridden `add` it ends up invoking. The subclass depends on a self-use detail of the superclass that is not
part of its contract and can change between versions.

Fix: don't extend a class to reuse it when you rely on its internals. Use composition — wrap a `HashSet` and
forward to it.

```java
public class InstrumentedSet<E> {
  private final Set<E> s = new HashSet<>();
  private int addCount = 0;
  public boolean add(E e)                        { addCount++; return s.add(e); }
  public boolean addAll(Collection<? extends E> c) { addCount += c.size(); return s.addAll(c); }
}
```

Handle: subclassing a concrete class ties you to its internal self-calls; wrap and forward instead of extending.

</details>

</details>

---

### Describe a code snippet #14
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Color {
  public static final Color[] PALETTE = {
    new Color(255, 0, 0),
    new Color(0, 255, 0),
    new Color(0, 0, 255)
  };
}
```

</details>

<details><summary>Show answer</summary>

`PALETTE` is `public static final`, but the `final` only fixes the *reference* — the array contents are wide
open. Any caller can write `Color.PALETTE[0] = null` or replace a color, and every other part of the program that
reads the shared array sees the change. A public array constant is never immutable.

Fix: keep the array private and expose an unmodifiable list, or a copy.

```java
private static final Color[] PALETTE = { /* ... */ };
public static final List<Color> palette = List.of(PALETTE);   // read-only view
// or: public static Color[] palette() { return PALETTE.clone(); } // fresh copy each call
```

Handle: a `public static final` array is mutable; `final` locks the reference, not the elements — expose a
read-only list or a copy.

</details>

</details>

---

### Describe a code snippet #15
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Order {
  private String status;

  public void setStatus(String status) {
    this.status = status;
  }
}

order.setStatus("SHIPPED");
order.setStatus("shipped");
order.setStatus("banana");
```

</details>

<details><summary>Show answer</summary>

`status` is a free `String`, so any value is accepted: a typo (`"shipped"` vs `"SHIPPED"`), a wrong case, or
nonsense (`"banana"`) all set the field with no complaint, and the bug shows up far later when some `switch`
fails to match. The type allows states that should be impossible.

Fix: use an `enum`, so only the defined states compile and the compiler catches typos.

```java
public enum Status { NEW, PAID, SHIPPED, DELIVERED }
private Status status;
public void setStatus(Status status) { this.status = status; }
```

Handle: model a fixed set of values as an `enum`, not a `String`; a string field accepts every typo as valid.

</details>

</details>

---

### Describe a code snippet #16
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public boolean equals(Object o) {
  if (!(o instanceof Point)) return false;
  Point p = (Point) o;
  return p.x == x && p.y == y;
}
```

`x` and `y` are `double`.

</details>

<details><summary>Show answer</summary>

Comparing `double` fields with `==` breaks two ways. `Double.NaN == Double.NaN` is `false`, so a point holding
`NaN` is not even equal to itself — it vanishes from a `HashSet`. And `0.0 == -0.0` is `true`, though their bit
patterns differ, so `equals` and `hashCode` can disagree if `hashCode` uses the bits. `==` on floating point does
not match the field-equality the contract needs.

Fix: compare with `Double.compare` (or `Float.compare`), which handles `NaN` and signed zero the way `hashCode`
does.

```java
return Double.compare(p.x, x) == 0 && Double.compare(p.y, y) == 0;
```

Handle: compare `double`/`float` in `equals` with `Double.compare`, not `==`; `NaN` and `-0.0` break raw `==`.

</details>

</details>

---

### Describe a code snippet #17
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public static Long sum(long[] values) {
  Long total = 0L;
  for (long v : values) {
    total += v;
  }
  return total;
}
```

</details>

<details><summary>Show answer</summary>

`total` is the boxed type `Long`, not the primitive `long`. Every `total += v` unboxes `total` to a `long`, adds,
and boxes the result back into a new `Long` object. Over a large array that is one object allocated per element —
huge, needless garbage that slows the loop far below the primitive version. One wrong character (`Long` vs
`long`) turns a tight loop into an allocation storm.

Fix: use the primitive for the accumulator.

```java
long total = 0L;
for (long v : values) total += v;
return total;
```

Handle: use primitives for loop accumulators; a boxed `Long`/`Integer` allocates a new object every iteration.

</details>

</details>

---

### Describe a code snippet #18
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Account {
  private long balance;

  public void transfer(long amount, Account to) {
    this.balance -= amount;      // (1)
    if (to.isFrozen()) {
      throw new IllegalStateException("target frozen");  // (2)
    }
    to.balance += amount;        // (3)
  }
}
```

</details>

<details><summary>Show answer</summary>

If `to` is frozen, the method throws at (2) — but (1) has already run, so this account's balance is reduced while
the target never received the money. The object is left in a broken state after the exception. A method that
fails should leave the object as it was.

Fix: check everything that can fail *before* changing any state, so a throw happens before the first mutation.

```java
public void transfer(long amount, Account to) {
  if (to.isFrozen()) throw new IllegalStateException("target frozen"); // validate first
  this.balance -= amount;       // mutate only after all checks pass
  to.balance += amount;
}
```

Handle: check all failure conditions before mutating; a throw after a partial change leaves the object broken
(failure atomicity).

</details>

</details>

---

### Describe a code snippet #19
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Base {
  protected int[] data;
  protected int cursor;
}

public class Reader extends Base {
  public int next() { return data[cursor++]; }
}
```

</details>

<details><summary>Show answer</summary>

`data` and `cursor` are `protected`, so every subclass can read and write them directly — and so can any class in
the same package. The base class can no longer guarantee that `cursor` stays in range or that `data` is non-null,
because a subclass can set them to anything. `protected` fields make the internal state part of the API the base
must keep forever.

Fix: keep the fields private and give subclasses only the operations they need, through protected *methods*.

```java
public class Base {
  private int[] data;
  private int cursor;
  protected int currentAndAdvance() { return data[cursor++]; } // controlled access
}
```

Handle: expose behavior to subclasses through protected methods, not protected fields; a protected field is
internal state the base can no longer protect.

</details>

</details>

---

### Describe a code snippet #20
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public Connection connect(String host) {
  try {
    return doConnect(host);
  } catch (IOException e) {
    return null;
  }
}
```

</details>

<details><summary>Show answer</summary>

The `catch` block swallows the `IOException` and returns `null`. The real failure — host unreachable, timeout,
refused — is gone, with no log and no cause, and the caller gets a `null` that turns into a
`NullPointerException` somewhere unrelated. An empty catch that hides the error is one of the worst habits in the
language: the failure still happened, you just deleted the evidence.

Fix: don't swallow it. Let it propagate, or translate it with the cause kept.

```java
public Connection connect(String host) throws IOException {
  return doConnect(host);
}
// or, if a runtime type is wanted:
catch (IOException e) {
  throw new UncheckedIOException("connect failed: " + host, e);
}
```

Handle: never swallow an exception into a `null` return; propagate or translate it with the cause so the failure
is visible.

</details>

</details>
