## Classes and Interfaces - Inheritance Design Challenges #2

### Design: Square reusing Rectangle's code
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You have a mutable `Rectangle` with `setWidth(int)` and `setHeight(int)`. A `Square` is obviously a rectangle with
equal sides, and reusing the width/height storage and area logic is tempting. Model `Square`.

```java
class Rectangle {
    protected int width, height;
    void setWidth(int w)  { this.width = w; }
    void setHeight(int h) { this.height = h; }
    int area() { return width * height; }
}
```

</details>

<details><summary>Show answer</summary>

**Wrong instinct: `class Square extends Rectangle`.** It passes the English "is-a" and reuses the code, but it is not
substitutable — a caller holding a `Rectangle` reference expects width and height to move independently, and a square
can't keep that promise.

```java
class Square extends Rectangle {
    void setWidth(int w)  { width = w; height = w; }    // forced to also change height
    void setHeight(int h) { width = h; height = h; }    // breaks the parent's contract
}
void grow(Rectangle r) { r.setWidth(r.area() == 0 ? 1 : r.width + 1); }  // misbehaves for Square
```

**Why it breaks:** the parent's mutators promise "set one side, the other is untouched." The override silently revokes
that invariant, so code written against `Rectangle` is now wrong when handed a `Square`. This is a substitutability
(LSP) violation — the subtype alters base behavior.
[→ how a subtype violates substitutability (LSP)](../FAQs/3.3_classes_and_interfaces.md#how-does-a-subtype-violate-substitutability-lsp)

**Fix — the conflict is the mutability, not the geometry.** The contract that breaks is a *setter* contract; remove
the setters and it vanishes. Model both as **immutable** values, and the "set width without touching height" promise
never exists to be broken.

```java
final class Rectangle {
    final int width, height;
    Rectangle(int w, int h) { width = w; height = h; }
    int area() { return width * height; }
}
final class Square {
    final int side;
    Square(int s) { side = s; }
    int area() { return side * side; }
}
```

If they must share an abstraction, let both implement a read-only `Shape` interface (`area()`) — a contract with no
mutator to violate — rather than one extending the other.

</details>

</details>

### Design: Properties as a string-only Hashtable
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You need a `Properties` type that maps string keys to string values and nothing else. A `Hashtable` already does the
storage, lookup, and iteration. Reuse it. Model `Properties`.

</details>

<details><summary>Show answer</summary>

**Wrong instinct: `class Properties extends Hashtable`.** A `Properties` *isn't* a general hashtable — it *has* one.
The is-a is false; it was reached for as a shortcut to reuse storage. This is the actual JDK mistake.
[→ JDK class where inheritance violated the subclass's own invariants](../FAQs/3.3_classes_and_interfaces.md#a-jdk-class-where-inheritance-violated-the-subclasss-own-invariants)

**Why it breaks:** inheriting exposes the *whole* `Hashtable` surface, which fights the type's own rules:

- **Broken invariant** — `Properties` is meant to hold only strings, but inherited `put(Object, Object)` lets any
  client insert arbitrary objects, defeating the string-only guarantee.
- **Conflicting access paths** — inherited `get(key)` behaves differently from the intended `getProperty(key)`, so the
  type has two confusing ways in.

And once clients depend on the leaked methods, the flaw is frozen — unfixable without breaking compatibility.

**Fix — composition: hold the map, expose only what fits.** `Properties` keeps a private `Map` and forwards a small,
string-typed surface. Nothing leaks; the invariant is enforceable.

```java
final class Properties {
    private final Map<String, String> map = new HashMap<>();
    String getProperty(String key)            { return map.get(key); }
    void   setProperty(String key, String val){ map.put(key, val); }     // only strings can enter
}
```

The rule this illustrates: **when you only want *part* of a type's behavior, that's has-a, not is-a — compose.**

</details>

</details>

### Design: a counting Set by extension
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You want a `Set` that tracks how many elements have ever been added. You decide to extend `HashSet` and override the
adding methods to bump a counter. Model it and predict the count.

```java
class CountingSet<E> extends HashSet<E> {
    int added = 0;
    public boolean add(E e)                { added++;          return super.add(e); }
    public boolean addAll(Collection<? extends E> c) { added += c.size(); return super.addAll(c); }
}
new CountingSet<String>().addAll(List.of("a", "b", "c"));   // added == ?
```

</details>

<details><summary>Show answer</summary>

**Wrong instinct: extend `HashSet` and override.** The count comes out **6, not 3.**

**Why it breaks:** `HashSet.addAll` calls `this.add` internally. So three elements are counted once by the overridden
`addAll`, then again by the overridden `add` that `addAll` triggers. The double-count comes from **self-use** — an
implementation detail of `HashSet` that isn't in its public contract and can change between versions. The subclass is
coupled to *how* the parent is built, not just what it promises.
[→ relationship between inheritance and encapsulation](../FAQs/3.3_classes_and_interfaces.md#what-is-the-relationship-between-inheritance-and-encapsulation)

**Fix — composition (a forwarding wrapper).** Hold a `Set`, forward to it, and control your own call graph so no
hidden self-use exists. The wrapper depends only on the `Set` public contract.

```java
final class CountingSet<E> {
    private final Set<E> inner = new HashSet<>();
    int added = 0;
    boolean add(E e)                         { added++; return inner.add(e); }
    boolean addAll(Collection<? extends E> c){ boolean r = false; for (E e : c) r |= add(e); return r; }
}
```

The only safe way to extend by inheritance instead would be a base that **documents its self-use** — and `HashSet`
doesn't.
[→ JDK class whose undocumented self-use breaks subclasses](../FAQs/3.5_classes_and_interfaces.md#a-jdk-class-whose-undocumented-self-use-breaks-subclasses)

</details>

</details>

### Design: a tagged Shape with a kind field
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You're handed a `Shape` class that handles circles and rectangles with a `kind` field and a `switch`. A new shape,
triangle, must be added. Where do you put it?

```java
class Shape {
    enum Kind { RECTANGLE, CIRCLE }
    private final Kind kind;
    private final double width, height;   // only for RECTANGLE
    private final double radius;          // only for CIRCLE
    double area() {
        switch (kind) {
            case RECTANGLE: return width * height;
            case CIRCLE:    return Math.PI * radius * radius;
            default:        throw new AssertionError(kind);
        }
    }
}
```

</details>

<details><summary>Show answer</summary>

**Wrong instinct: add `TRIANGLE` to the enum and another `case` to every `switch`.** That extends the smell instead of
removing it. A tagged class is **a subtype hierarchy emulated badly with a field** — the tag hand-rolls at runtime the
subtyping the language gives for free, paying with branching and fields that apply to only some kinds.
[→ why tagged classes are a design smell](../FAQs/3.7_classes_and_interfaces.md#why-are-tagged-classes-considered-a-design-smell)

**Why it breaks:** every new variant means editing every `switch`, the per-kind fields pile up unused on every
instance, and the compiler can't tell you when you've missed a branch. One class is carrying several concepts.

**Fix — give each variant its own type, so subtyping replaces the tag and the `switch` disappears.** For a closed,
known set, a sealed hierarchy with an exhaustive `switch` is the sharpest — the compiler errors if a case is missing.

```java
sealed interface Shape permits Circle, Rectangle, Triangle { }
record Circle(double r)              implements Shape { }
record Rectangle(double w, double h) implements Shape { }
record Triangle(double b, double h)  implements Shape { }

double area(Shape s) {
    return switch (s) {
        case Circle c    -> Math.PI * c.r() * c.r();
        case Rectangle r -> r.w() * r.h();
        case Triangle t  -> 0.5 * t.b() * t.h();
    };   // no default — permits list makes it exhaustive; adding a variant forces this to be updated
}
```

Open-ended variants → a plain subclass hierarchy instead; a fixed small set carrying data → an enum with behavior.
[→ what to use instead of a tagged class](../FAQs/3.7_classes_and_interfaces.md#what-should-you-use-instead-of-a-tagged-class)

</details>

</details>

### Design: extending a base just to reuse two methods
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You're writing `InvoiceExporter`. An existing `ReportGenerator` already has `formatHeader()` and `writeFile()` that do
exactly what you need — but also `computeTotals()`, `applyTaxRules()`, and a dozen report-specific methods you don't
want. Extending it gives you the two methods for free. Do it?

</details>

<details><summary>Show answer</summary>

**Wrong instinct: `class InvoiceExporter extends ReportGenerator`.** This is inheritance reached for **code reuse
only**, with no real is-a and no intent to use an exporter *as* a report generator. That's accidental, semantically
wrong inheritance — the classic warning sign of "extending a class to reuse half of it."
[→ when inheritance should be used cautiously or avoided](../FAQs/3.3_classes_and_interfaces.md#when-should-inheritance-be-used-cautiously-or-avoided)

**Why it breaks:**

- **The whole unwanted surface leaks** — an `InvoiceExporter` now exposes `applyTaxRules()` and every other report
  method, none of which make sense on it.
- **Substitutability is a lie** — it can't actually stand in for a `ReportGenerator`; callers would have to know it's
  really an exporter, defeating the abstraction.
- **You inherit the base's fragility** — self-use and future changes in `ReportGenerator` can break the exporter for
  reasons unrelated to exporting.

**Fix — depends on what the two methods *are*:**

- If they're **general-purpose helpers**, they don't belong to `ReportGenerator` either — extract them to a small
  shared utility both types use (`ReportFormatting.header(...)`, `Files.write(...)`).
- If the exporter genuinely needs report behavior, **compose**: hold a `ReportGenerator` as a private field and call
  the two methods through it, exposing only the exporter's own API.

```java
final class InvoiceExporter {
    private final ReportFormatting fmt;          // extracted helper, OR a held collaborator
    String export(Invoice inv) { return fmt.header(inv.title()) + body(inv); }
}
```

The rule: **wanting a couple of methods is not an is-a. Reuse via composition or extraction, never via `extends`.**

</details>

</details>

### Design: a feature that fits some subtypes, not all
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

Your hierarchy is `Account` (the base) with three branches already in use: `Checking`, `Savings`, `Loan`. A new
requirement arrives: **overdraft protection** — a balance buffer plus the behavior to draw on it. It applies to
`Checking` and `Savings`, but **not** `Loan`. It isn't on every account, and it isn't confined to one branch. Where
does it go?

```java
abstract class Account { Money balance; abstract void withdraw(Money m); }
class Checking extends Account { ... }
class Savings  extends Account { ... }
class Loan     extends Account { ... }
```

</details>

<details><summary>Show answer</summary>

This is the hard case: a capability that fits a **subset** of the subtypes — more than one branch, fewer than all.
Every *structural* fix is wrong, and seeing why each fails is the whole lesson.

**Put it on `Account` (the base) → wrong.** `Loan` would inherit overdraft state and behavior it must never have. The
base would carry a concern that isn't true of every account — the all-types test fails, and you'd be back to guarding
it with checks like "if this is a Loan, ignore the buffer."

**Add a new branch `OverdraftAccount` → wrong, and this is the subtle one.** Every concrete account is *already* a
`Checking` or a `Savings` — an object lives in exactly **one** leaf of **one** tree. It can't also be an
`OverdraftAccount`. To make a branch work you'd need `OverdraftChecking` *and* `OverdraftSavings` — the overdraft
distinction **times** the existing kind. The capability cuts *across* the existing tree instead of nesting under it,
so it can't be one branch; it forces a class per (kind × hasOverdraft) pairing. That's the explosion from 3.8, here
triggered by a subset.
[→ explosion on varying behavior](../FAQs/3.4_classes_and_interfaces.md#how-does-subclassing-explode-on-varying-behavior)

**Insert an intermediate type between `Account` and the applicable branches → works once, then collapses.** You could
slot `OverdraftCapableAccount` between `Account` and {`Checking`, `Savings`}, and `Loan` extends `Account` directly.
Clean — for *this one* feature. But the moment a **second** crosscutting capability appears (say "interest-bearing,"
fitting `Savings` and `Loan` but not `Checking`), it carves a *different* subset. You'd need a second intermediate
layer — and the two intermediates can't both sit in one single-inheritance chain, because their subsets overlap
partially and neither contains the other. The hierarchy seizes up. **Crosscuts don't layer:** an intermediate type
encodes a subset as a position in the tree, and positions in a single-inheritance tree are totally ordered, while
crosscutting subsets are not.

**The right read:** a subset that cuts across the existing tree is the **same free-combination signal** as 3.8 — the
capability is independent of the kind grouping, so it doesn't belong in the tree at all. The kind tree models *one*
grouping; overdraft is a *different* axis.

**Fix — extract the capability and let the applicable types hold or implement it, off the tree:**

```java
interface OverdraftCapable {                       // the capability as a contract, not a tree position
    Money buffer();
    void  drawOnOverdraft(Money m);
}

class Checking extends Account implements OverdraftCapable {
    private final Overdraft overdraft;             // behavior held as a composed part
    public Money buffer()              { return overdraft.buffer(); }
    public void  drawOnOverdraft(Money m) { overdraft.draw(m); }
}
class Savings  extends Account implements OverdraftCapable { /* holds its own Overdraft */ }
class Loan     extends Account { /* simply does not implement it */ }
```

Now overdraft is an **interface the applicable subtypes implement** (delegating to a small composed `Overdraft`
object), `Loan` just doesn't implement it, and a second capability (`InterestBearing`) is **another independent
interface** applied to *its* own subset — no layering, no multiplication. Each crosscut is its own capability;
membership is "implements it or not," not "sits at this depth in the tree."
[→ what is composition, and what does it solve?](../FAQs/3.4_classes_and_interfaces.md#what-is-composition-and-what-does-it-solve)

The takeaway worth memorizing: **base = all, branch = exactly one, subset across branches = capability (interface +
composition), never a base field, a new branch, or an intermediate layer.**

</details>

</details>


### Design: a base with hooks for subclasses
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You own an import pipeline: read a file, parse rows, validate, write to DB. Every step is the same for all sources
except *parse* — CSV parses one way, XML another. You put the fixed sequence in a base class and leave `parse()`
abstract for subclasses to fill. It works. Now a second requirement lands: validation also varies — some sources need
strict validation, some lenient — and any source may need either. Model it.

```java
abstract class Importer {
    final void run(Path p) {                 // fixed sequence, subclasses cannot reorder
        var raw  = read(p);
        var rows = parse(raw);               // the hole subclasses fill
        validate(rows);
        write(rows);
    }
    protected abstract List<Row> parse(String raw);
    protected void validate(List<Row> rows) { /* default */ }
}
class CsvImporter extends Importer { protected List<Row> parse(String raw) { ... } }
class XmlImporter extends Importer { protected List<Row> parse(String raw) { ... } }
```

</details>

<details><summary>Show answer</summary>

**Wrong instinct: make `validate()` a second abstract hook and let subclasses override it.** The first hook was fine —
one varying step, one tree. The second hook is where it collapses.

**Why it breaks:** a hook binds its variation to the *class*, so two hooks force the class to fix both choices at
once. Strict CSV, lenient CSV, strict XML, lenient XML → `StrictCsvImporter`, `LenientCsvImporter`, … = parse ×
validate subclasses. The two steps vary independently, but the tree has only one dimension to spend, and it is already
spent on parse.
[→ a feature that fits some subtypes, not all](3_9_classes_and_interfaces.md#design-a-feature-that-fits-some-subtypes-not-all)

The deeper reason: **a hook is a strategy whose slot is filled by inheritance instead of by a field.** One hook hides
that — inheritance can supply one implementation for free. Two hooks expose it: you cannot supply two independent
implementations from one `extends`.

**Fix — the fixed sequence stays; the varying steps become injected collaborators, not overridden methods.**

```java
final class Importer {                                   // no longer abstract, no longer extended
    private final Parser    parser;                      // was a hook — now a field
    private final Validator validator;                   // second variation, second field, no multiplication
    Importer(Parser parser, Validator validator) { this.parser = parser; this.validator = validator; }

    void run(Path p) {                                   // same fixed sequence — the template survives
        var rows = parser.parse(read(p));
        validator.validate(rows);
        write(rows);
    }
}
interface Parser    { List<Row> parse(String raw); }      // Csv, Xml — independent axis
interface Validator { void validate(List<Row> rows); }    // Strict, Lenient — independent axis
new Importer(new CsvParser(), new LenientValidator());    // any combination, zero new classes
```

**Where template method is still right:** exactly one varying step, and the base and subclasses ship together in one
module you own. The moment a second independent step varies — or the base is published for outside extension — the
hooks must become fields.

The discriminator: **one hole, closed set, same codebase → template method; two or more independent holes → strategy
fields.** Hooks multiply; fields add.
[→ when is strategy the right tool?](3_4_classes_and_interfaces.md#when-is-strategy-the-right-tool)

</details>

</details>

### Design: a base constructor that calls an override
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

A base class wants each subclass to contribute its own initial data, so the base constructor calls an overridable
method to fetch it. A subclass overrides that method and reads one of its own fields. Predict the output.

```java
class Base {
    Base() { init(); }                                   // base ctor calls an overridable method
    void init() { }
}
class Child extends Base {
    private final int limit = 10;
    private List<String> items = new ArrayList<>();
    @Override void init() { items.add("row-" + limit); } // reads Child's own state
}
new Child();                                             // what happens?
```

</details>

<details><summary>Show answer</summary>

**Wrong instinct: assume the override sees the subclass's fields.** It throws `NullPointerException` — `items` is
still `null` when `init()` runs. (`limit` reads as `0` if it were non-final; a `final` primitive constant may be
inlined, which makes the bug even more confusing to read.)

**Why it breaks — construction order:** the `Child` constructor first calls `super()`, which runs `Base()`, which
calls `init()`. Java dispatches that call **dynamically to `Child.init`** — but `Child`'s field initializers and
constructor body have not run yet. So the override executes against a half-built object: its fields are at their
default values.

**The general rule: a constructor must never call an overridable method.** Same for `clone()` and `readObject()` —
they also produce an object before the subclass's own initialization has completed.

**Fix — make the call non-overridable, or move the work out of construction.**

```java
class Base {
    Base() { init(); }
    private void init() { }                              // private → not overridable → safely callable
}
```

```java
abstract class Base {                                    // or: don't ask the subclass during construction
    protected Base(List<String> seed) { this.items = new ArrayList<>(seed); }   // subclass PASSES the data up
    private final List<String> items;
}
class Child extends Base {
    Child() { super(List.of("row-10")); }                // fully-formed value handed to the base, no callback
}
```

The choices, in order of preference: **make the method `private`/`final`/`static`; pass the value into the base
constructor instead of calling back for it; or give the class a factory method that constructs, then initializes.**

This is a specific instance of the general fragility of inheritance — the subclass is coupled to *when* the base calls
what, not to what the base promises.
[→ relationship between inheritance and encapsulation](3_3_classes_and_interfaces.md#what-is-the-relationship-between-inheritance-and-encapsulation)

</details>

</details>