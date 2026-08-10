### Describe a code snippet #101
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Cart {
  private final List<Item> items = new ArrayList<>();

  @Override public boolean equals(Object o) {
    if (!(o instanceof Cart)) return false;
    return items == ((Cart) o).items;
  }
}
```

</details>

<details><summary>Show answer</summary>

`equals` compares the two `items` lists with `==`, which tests whether they are the *same list object*, not whether
they hold the same items. Two carts with identical contents are never equal, because each has its own `ArrayList`.
`==` on a reference field asks "same object," but value equality needs "equal contents."

Fix: compare the fields with `.equals` (here `List.equals` already compares element by element).

```java
@Override public boolean equals(Object o) {
  return o instanceof Cart c && items.equals(c.items);
}
@Override public int hashCode() { return items.hashCode(); }
```

Handle: compare reference fields in `equals` with `.equals`, not `==`; `==` tests same-object, not equal-contents.

</details>

</details>

---

### Describe a code snippet #102
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Interval {
  private final LocalDate start;
  private final LocalDate end;

  public Interval(LocalDate start, LocalDate end) {
    this.start = start;
    this.end = end;
  }
  public LocalDate start() { return start; }
  public LocalDate end()   { return end; }
}
```

</details>

<details><summary>Show answer</summary>

Nothing is wrong — this is correct, shown so you can name *why* no defensive copy is needed here. `LocalDate` (like
`String`, `Integer`, and the whole `java.time` value set) is immutable: there are no setters, so the caller cannot
change the object after handing it in, and the getters cannot leak a mutable path. The copy-in/copy-out dance that
`Date` and `List` require is unnecessary for an already-immutable type.

The teaching point is the discriminator: defensive copying is needed only for *mutable* argument/field types. For
immutable ones it is pure waste.

```java
this.start = start;   // safe: LocalDate is immutable, no copy needed
```

Handle: skip defensive copies for immutable types (`LocalDate`, `String`, boxed numbers); copying is only needed
for mutable ones.

</details>

</details>

---

### Describe a code snippet #103
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Account {
  private double balance;   // dollars and cents

  public void deposit(double amount) {
    balance += amount;
  }
}

account.deposit(0.1);
account.deposit(0.2);   // balance is now 0.30000000000000004
```

</details>

<details><summary>Show answer</summary>

`double` cannot represent most decimal fractions exactly, so money math drifts: `0.1 + 0.2` is
`0.30000000000000004`, and errors accumulate over many operations until totals are wrong by cents. Floating point
is the wrong type for exact currency. This is a design defect in the field type, not the arithmetic.

Fix: use `BigDecimal` (or integer cents) for money, so values are exact.

```java
private BigDecimal balance = BigDecimal.ZERO;
public void deposit(BigDecimal amount) {
  balance = balance.add(amount);
}
// construct amounts from strings: new BigDecimal("0.1"), never new BigDecimal(0.1)
```

Handle: represent money as `BigDecimal` or integer cents, never `double`; binary floating point can't hold decimal
fractions exactly.

</details>

</details>

---

### Describe a code snippet #104
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Shape {
  public void draw() { }
}
public class Circle extends Shape {
  public void draw() { drawCircle(); }
}
public class Square extends Shape {
  public void draw() { drawSquare(); }
}

// elsewhere:
if (shape instanceof Circle) ((Circle) shape).draw();
else if (shape instanceof Square) ((Square) shape).draw();
```

</details>

<details><summary>Show answer</summary>

The hierarchy already gives polymorphism — `shape.draw()` calls the right `draw` on its own — but the call site
throws that away with an `instanceof`/cast chain that does exactly what a plain virtual call would. Every new shape
forces another `else if` branch here, and a missed branch silently does nothing. Checking the runtime type to pick
behavior that the method already dispatches is a step backward.

Fix: just call the method; let polymorphism dispatch.

```java
shape.draw();   // the correct draw() runs based on the actual type
```

Handle: call the overridden method and let polymorphism dispatch; an `instanceof`/cast chain re-implements what
the virtual call already does, and breaks on every new subtype.

</details>

</details>

---

### Describe a code snippet #105
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Server {
  private Config config;

  public Server() { }

  public void setConfig(Config c) { this.config = c; }

  public void start() {
    listen(config.port());   // NPE if setConfig was not called
  }
}
```

</details>

<details><summary>Show answer</summary>

The no-arg constructor builds a `Server` with `config == null`, and the object is only usable *after* someone
remembers to call `setConfig`. Forget it, and `start()` throws `NullPointerException` on `config.port()`. The type
allows a half-built, invalid object to exist, and relies on the caller following an unwritten "set this before you
use it" rule. Required state belongs in the constructor.

Fix: take the required dependency in the constructor, so a `Server` cannot exist without it.

```java
public class Server {
  private final Config config;
  public Server(Config config) {
    this.config = Objects.requireNonNull(config);
  }
  public void start() { listen(config.port()); }
}
```

Handle: require mandatory state in the constructor, not a later setter; a no-arg constructor plus a setter lets an
invalid, half-built object exist.

</details>

</details>

---

### Describe a code snippet #106
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Inventory {
  private final Map<String, Integer> stock = new HashMap<>();

  public void remove(String item, int qty) {
    int current = stock.get(item);
    stock.put(item, current - qty);
  }
}
```

</details>

<details><summary>Show answer</summary>

Two in-scope defects around the same operation. `stock.get(item)` returns `null` for an unknown item, and unboxing
`null` to `int` throws `NullPointerException`. And nothing stops `current - qty` from going negative, so removing
more than exists leaves a negative stock count — an invalid state the class should forbid. The method neither
checks the item exists nor guards the invariant.

Fix: check presence and the quantity before mutating.

```java
public void remove(String item, int qty) {
  Integer current = stock.get(item);
  if (current == null) throw new NoSuchElementException(item);
  if (qty < 0 || qty > current) throw new IllegalArgumentException("bad qty: " + qty);
  stock.put(item, current - qty);
}
```

Handle: check a map lookup for `null` before unboxing, and guard the invariant before mutating; neither is
automatic.

</details>

</details>

---

### Describe a code snippet #107
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public abstract class Validator {
  public abstract boolean isValid(String input);
  public abstract String errorMessage();
  public abstract int maxLength();
  public abstract void reset();
  public abstract List<String> history();
}
```

</details>

<details><summary>Show answer</summary>

The abstraction is bloated: a *validator* only needs to answer "is this valid," but the interface also demands an
error message, a max length, a `reset`, and a history. Every implementation must supply all five even when four
make no sense for it, so simple validators fill methods with stubs or throw `UnsupportedOperationException`. A wide
interface forces implementers to support things they don't have.

Fix: keep the core interface minimal (ideally one method, so it can be a lambda), and split the extras into
separate, optional interfaces.

```java
@FunctionalInterface
public interface Validator {
  boolean isValid(String input);
}
// separate: interface DescribesError { String errorMessage(); }  — implemented only where it applies
```

Handle: keep an interface small so implementers aren't forced to support methods they don't need; split extra
capabilities into their own interfaces.

</details>

</details>

---

### Describe a code snippet #108
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class OrderProcessor {
  public void process(Order o) {
    try {
      validate(o);
      charge(o);
      ship(o);
    } catch (Exception e) {
      // log and continue
      log.info("problem with order");
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

Catching `Exception` this broadly swallows everything — a validation failure, a charge decline, a bug like a
`NullPointerException`, even an `InterruptedException` — and treats them all as one, logging a vague message and
continuing as if nothing happened. The order may be charged but not shipped, or a real bug is hidden. Catching
`Exception` (or `Throwable`) blanket-hides failures you should handle differently or not at all.

Fix: catch the specific exceptions you can actually handle, and let the rest propagate.

```java
try {
  validate(o); charge(o); ship(o);
} catch (ValidationException e) {
  log.warn("invalid order {}", o.id(), e);   // handle the one you know
}
// PaymentException, bugs, etc. propagate to a caller that can decide
```

Handle: catch the specific exception you can handle, not `Exception`; a blanket catch hides bugs and unrelated
failures behind one vague path.

</details>

</details>

---

### Describe a code snippet #109
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Money {
  private final long cents;
  private final String currency;

  public Money add(Money other) {
    return new Money(this.cents + other.cents, this.currency);
  }
}
```

</details>

<details><summary>Show answer</summary>

`add` sums the cents but ignores whether the two amounts are in the same currency — adding 100 USD cents to 100
JPY cents returns "200" in this object's currency, a meaningless number presented as a real total. The operation
silently produces a wrong result instead of rejecting an operation that makes no sense. A value type must guard
operations that its invariant forbids.

Fix: check the currencies match and reject the mismatch.

```java
public Money add(Money other) {
  if (!this.currency.equals(other.currency)) {
    throw new IllegalArgumentException("currency mismatch: " + currency + " vs " + other.currency);
  }
  return new Money(this.cents + other.cents, this.currency);
}
```

Handle: guard operations a value type's invariant forbids; adding two `Money` values without checking currency
returns a silently wrong total.

</details>

</details>

---

### Describe a code snippet #110
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Snapshot {
  private final List<Event> events;

  public Snapshot(List<Event> events) {
    this.events = List.copyOf(events);   // Event is mutable
  }

  public List<Event> events() {
    return events;   // unmodifiable list...
  }
}
```

</details>

<details><summary>Show answer</summary>

`List.copyOf` makes the *list* unmodifiable and independent — good — but the `Event` objects inside are mutable and
are the same objects the caller passed in. A caller can hold an `Event`, put it in a `Snapshot`, then call
`event.setX(...)` and change what the "snapshot" shows. Copying the collection protects the list structure, not
its elements; a shallow copy leaves mutable elements shared.

Fix: for a true snapshot of mutable elements, copy each element too (a deep copy), or store immutable elements.

```java
this.events = events.stream()
    .map(Event::copy)          // defensive copy of each element
    .collect(Collectors.toUnmodifiableList());
```

Best: make `Event` immutable, so the shallow list copy is enough.

Handle: an unmodifiable copy of a list still shares its mutable elements; deep-copy the elements or make them
immutable for a real snapshot.

</details>

</details>

---

### Describe a code snippet #111
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Line {
  private final Point start;
  private final Point end;

  @Override public boolean equals(Object o) {
    if (!(o instanceof Line)) return false;
    Line l = (Line) o;
    return start.equals(l.start) && end.equals(l.end);
  }
  // no hashCode; Point has correct equals AND hashCode
}
```

</details>

<details><summary>Show answer</summary>

`equals` is built correctly on the two `Point` fields, but there is no `hashCode`, so equal `Line` objects get
identity hash codes and are lost in hash collections — the missing-`hashCode` trap, framed on a type composed of
other value objects. The fact that `Point` has a correct `hashCode` does not help; `Line` must define its own by
combining its fields' hashes.

Fix: derive `hashCode` from the same fields (their `hashCode`s combine cleanly through `Objects.hash`).

```java
@Override public int hashCode() {
  return Objects.hash(start, end);
}
```

Handle: a value type composed of other value objects still needs its own `hashCode` combining its fields;
`equals` alone is not enough.

</details>

</details>

---

### Describe a code snippet #112
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public enum Operation {
  PLUS, MINUS, TIMES, DIVIDE;

  public int apply(int a, int b) {
    switch (this) {
      case PLUS:  return a + b;
      case MINUS: return a - b;
      case TIMES: return a * b;
      case DIVIDE: return a / b;
    }
    throw new AssertionError("unknown op: " + this);
  }
}
```

</details>

<details><summary>Show answer</summary>

The behavior for each constant lives in a `switch` inside a shared method, so adding a new constant (say `MODULO`)
compiles fine but silently falls through to the `AssertionError` at runtime unless you remember to edit the
switch. The enum can't force you to supply behavior for a new constant. Constant-specific behavior expressed as a
switch is a maintenance trap.

Fix: give each constant its own implementation of an abstract method, so the compiler *requires* behavior for every
constant.

```java
public enum Operation {
  PLUS  { public int apply(int a, int b) { return a + b; } },
  MINUS { public int apply(int a, int b) { return a - b; } },
  TIMES { public int apply(int a, int b) { return a * b; } },
  DIVIDE{ public int apply(int a, int b) { return a / b; } };
  public abstract int apply(int a, int b);
}
```

Handle: give enums constant-specific behavior via an abstract method per constant, not a `switch`; the switch lets
a new constant compile with no behavior.

</details>

</details>

---

### Describe a code snippet #113
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Repository<T> {
  private final List<T> items = new ArrayList<>();

  public void add(T item) { items.add(item); }

  public T get(int i) { return items.get(i); }

  public boolean equals(Object o) {
    return items.equals(((Repository) o).items);
  }
}
```

</details>

<details><summary>Show answer</summary>

`equals` casts `o` to the raw type `Repository` with no `instanceof` check, so passing `null` or an unrelated type
throws `ClassCastException` (or NPE) instead of returning `false`, which `equals` must never do. The raw cast also
drops generics, and there is no `hashCode`. The core defect in scope: `equals` skips the type guard and blows up on
the wrong argument.

Fix: guard the type with `instanceof` first, then compare.

```java
@Override public boolean equals(Object o) {
  return o instanceof Repository<?> r && items.equals(r.items);
}
@Override public int hashCode() { return items.hashCode(); }
```

Handle: start `equals` with an `instanceof` guard; casting without it throws on `null` or a foreign type instead of
returning `false`.

</details>

</details>

---

### Describe a code snippet #114
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Logger {
  public void log(String message, int level, boolean toFile,
                  boolean toConsole, String category, boolean timestamp) {
    // ...
  }
}

logger.log("started", 2, true, false, "system", true);
```

</details>

<details><summary>Show answer</summary>

Six positional parameters, several `boolean` and same-typed, make the call site unreadable and easy to get wrong:
`log("started", 2, true, false, "system", true)` gives no clue which flag is which, and swapping two booleans
compiles and misbehaves. This is the long-parameter-list / flag-argument problem on a logging call.

Fix: gather the options into a small object built with named methods.

```java
logger.log("started", LogOptions.builder()
    .level(Level.INFO).toFile(true).category("system").timestamp(true).build());
```

Even simpler for the common case, offer a short overload (`log(String message)`) with sensible defaults so most
callers pass one argument.

Handle: collapse a long list of positional and boolean parameters into a named options object; positional flags
read as a puzzle and swap silently.

</details>

</details>

---

### Describe a code snippet #115
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class TreeNode {
  private TreeNode left, right;
  private int value;

  @Override public int hashCode() {
    return value + (left != null ? left.hashCode() : 0)
                 + (right != null ? right.hashCode() : 0);
  }
}
```

`TreeNode` is mutable — children and value can change.

</details>

<details><summary>Show answer</summary>

`hashCode` walks the whole subtree, so it is O(n) on every call — and for a `hashCode`, which hash collections call
constantly, that turns every map operation into a tree walk. Worse, the node is mutable, so a `hashCode` built from
changing children breaks the object's place in any hash collection the moment the tree changes. A recursive
`hashCode` over mutable, unbounded structure is both slow and unstable.

Fix: for a mutable node, base identity/hash on a stable key (or don't override `hashCode` and use identity). If it
must be value-based, make the structure immutable and consider caching the computed hash.

```java
private final int id;                    // stable identity
@Override public int hashCode() { return Integer.hashCode(id); }
```

Handle: don't compute `hashCode` by recursing over a mutable, unbounded structure; it's O(n) per call and changes
when the structure does.

</details>

</details>

---

### Describe a code snippet #116
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class ConnectionPool {
  private final Queue<Connection> pool = new LinkedList<>();

  public Connection acquire() {
    return pool.poll();
  }

  public void release(Connection c) {
    pool.offer(c);
  }
}
```

</details>

<details><summary>Show answer</summary>

`acquire()` returns `null` when the pool is empty, handing the caller a `null` connection that becomes an NPE the
moment they use it — the empty-pool case is signaled by `null` instead of being handled. And `release` takes any
`Connection`, including one that was never from this pool or was already released, with no check. The API's
contract for "no connection available" is a silent `null`.

Fix: make the empty case explicit — block, time out, or throw — rather than returning `null`.

```java
public Connection acquire(Duration timeout) throws TimeoutException {
  Connection c = pool.poll();
  if (c == null) throw new TimeoutException("no connection available");
  return c;
}
```

(A real pool usually blocks up to the timeout waiting for a release.)

Handle: don't signal "resource unavailable" with a `null` return; block, time out, or throw so the caller can't
NPE on it.

</details>

</details>

---

### Describe a code snippet #117
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Email {
  private final String address;

  public Email(String address) {
    this.address = address;
  }
}

Email e = new Email("not-an-email");
```

</details>

<details><summary>Show answer</summary>

The constructor accepts any string, so `"not-an-email"` builds an `Email` that isn't one. The whole point of a
dedicated `Email` value type is that holding one *means* the address is valid — but with no validation, an `Email`
guarantees nothing, and every consumer must re-check. A value type with an invariant must enforce it at
construction, or it is just a `String` with a fancy name.

Fix: validate in the constructor and reject invalid input.

```java
public Email(String address) {
  Objects.requireNonNull(address, "address");
  if (!address.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
    throw new IllegalArgumentException("invalid email: " + address);
  }
  this.address = address;
}
```

Handle: a value type must validate its invariant in the constructor; without it, the type guarantees nothing and
every caller must re-check.

</details>

</details>

---

### Describe a code snippet #118
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Base {
  List<String> getNames() { return new ArrayList<>(); }
}
public class Derived extends Base {
  @Override ArrayList<String> getNames() { return new ArrayList<>(); }  // narrower return
}
```

Callers of `Base.getNames()` sometimes wrap the result in an unmodifiable list; a new subclass returns
`List.of(...)`.

</details>

<details><summary>Show answer</summary>

This one actually compiles and is legal — an override may return a *subtype* of the declared return type
(covariant returns), so `ArrayList<String>` narrowing `List<String>` is allowed. The trap it drills is the mirror
mistake: an override may **not** *widen* the return type (returning `Object` where the base returns `String`) or
*broaden* a `throws` clause, and it may not reduce visibility (a `public` method can't become `protected` in the
subclass). Those break the base contract and won't compile — but the direction confuses people.

```java
// legal: narrower return (covariant)
@Override ArrayList<String> getNames() { ... }
// illegal: wider return, broader checked throws, or reduced visibility
```

Handle: an override may narrow the return type (covariant) but never widen the return, broaden checked exceptions,
or reduce visibility — those break the base contract.

</details>

</details>

---

### Describe a code snippet #119
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Session implements AutoCloseable {
  private final Connection conn;

  public Session(Connection conn) { this.conn = conn; }

  @Override public void close() {
    conn.close();
    cache.clear();
    notifyListeners();
  }
}
```

`conn.close()` can throw.

</details>

<details><summary>Show answer</summary>

If `conn.close()` throws, `close()` exits before `cache.clear()` and `notifyListeners()` run, so the rest of the
cleanup is skipped and resources or listeners leak. A `close()` method must release everything even if one step
fails — one throwing step shouldn't abandon the others. The cleanup steps are chained so a failure early on strands
the later ones.

Fix: make each step independent so one failure doesn't skip the rest; a small helper or nested try/finally works.

```java
@Override public void close() {
  try {
    conn.close();
  } finally {
    cache.clear();       // runs even if conn.close() threw
    notifyListeners();
  }
}
```

Handle: in `close()`, make each cleanup step run even if an earlier one throws; chaining them lets one failure skip
the rest.

</details>

</details>

---

### Describe a code snippet #120
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class Result<T> {
  private final T value;
  private final String error;

  public Result(T value, String error) {
    this.value = value;
    this.error = error;
  }

  public static <T> Result<T> ok(T value)     { return new Result<>(value, null); }
  public static <T> Result<T> fail(String e)  { return new Result<>(null, e); }
}
```

</details>

<details><summary>Show answer</summary>

The public constructor lets a caller build a `Result` in states the type should forbid: `new Result<>(value,
"error")` (both set) or `new Result<>(null, null)` (neither), each of which contradicts "a result is either a
value or an error." The static factories create only valid states, but leaving the constructor public means the
invalid ones are still reachable. When factories enforce an invariant, the constructor must not offer a way around
them.

Fix: make the constructor `private`, so the only way to build a `Result` is through `ok`/`fail`.

```java
private Result(T value, String error) {
  this.value = value;
  this.error = error;
}
// only ok(...) and fail(...) can construct — invalid states unreachable
```

Handle: when static factories enforce valid states, make the constructor `private`; a public constructor leaves the
invalid states reachable.

</details>

</details>
