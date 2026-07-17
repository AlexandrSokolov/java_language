## Classes and Interfaces - Inheritance Design Challenges #3

### Design: equals between a base and its subclass
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

`Point` is a value class with `equals`. `ColorPoint` adds a colour and wants equality to consider it. Both are
instantiable. Make `equals` work across the pair.

```java
class Point {
    final int x, y;
    @Override public boolean equals(Object o) {
        if (!(o instanceof Point p)) return false;
        return p.x == x && p.y == y;
    }
}
class ColorPoint extends Point {
    final Color color;
    // equals = ?
}
```

</details>

<details><summary>Show answer</summary>

**There is no correct `equals` here.** Adding a value component to a subclass of an instantiable class with a value
`equals` cannot preserve the contract. Every attempt fails a different clause:

**Attempt 1 — `ColorPoint.equals` requires the colour to match.** Breaks **symmetry**: `p.equals(cp)` is `true` (the
base only looks at x/y), but `cp.equals(p)` is `false` (`p` has no colour). Two objects disagree about whether they
are equal.

**Attempt 2 — ignore colour when comparing against a plain `Point`.** Symmetry is restored, **transitivity** dies:
`red.equals(p)` and `p.equals(blue)` are both `true`, yet `red.equals(blue)` is `false`. A plain `Point` becomes a
bridge between two unequal colours.

**Attempt 3 — `getClass() != o.getClass()` instead of `instanceof`.** Symmetric and transitive, but it breaks
**substitutability**: a `ColorPoint` can no longer be equal to any `Point`, so subclass instances silently stop
matching in collections keyed by the base type. This trades one broken contract for another.
[→ how a subtype violates substitutability (LSP)](3_3_classes_and_interfaces.md#how-does-a-subtype-violate-substitutability-lsp)

**Why it is unfixable, not merely hard:** `equals` on the base publishes a promise — *"any two Points with the same
x/y are equal."* The subclass wants to add a condition to that promise. Strengthening an inherited promise contradicts
it; there is no arrangement of code that both keeps it and narrows it.

**Fix — compose instead of extend, and expose the base as a view.** `ColorPoint` is not a `Point`; it *has* one.

```java
final class ColorPoint {
    private final Point point;                          // has-a, not is-a
    private final Color color;
    Point asPoint() { return point; }                   // the view, when a Point is genuinely wanted
    @Override public boolean equals(Object o) {
        return o instanceof ColorPoint cp && cp.point.equals(point) && cp.color.equals(color);
    }
}
```

Now no cross-type comparison exists to break. **The other legal shape: make the base `abstract`** — with no
instantiable parent, `Point` and `ColorPoint` instances can never meet in an `equals` call, so a value field on a
branch is safe. That is exactly why the t-shirt and notification hierarchies force an abstract base.
[→ why the base must be abstract](3_8_classes_and_interfaces_design.md#which-t-shirt-properties-belong-to-every-shirt-vs-one-kind)

The rule worth memorizing: **you cannot add a value component to a subclass of an instantiable value class. Either the
base is abstract, or the relationship is composition.**

</details>

</details>

### Design: wrappers that must survive identity checks
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You wrap `Content` in decorators to add a footer, redact, and sign. It works. Then three things break in production:
a cache keyed by content object misses every time; a downstream `if (c instanceof SignedContent)` never fires; and a
transform registers itself with a callback that hands back the *unwrapped* object. Diagnose and model the fix.

```java
Content body = new Sign(new Footer(new Redact(raw)));
cache.get(body);                                    // miss — why?
if (body instanceof Redact) { ... }                 // false — why?
```

</details>

<details><summary>Show answer</summary>

**Wrong instinct: assume a decorator is transparent.** It is transparent to the *interface* and to nothing else. Three
distinct leaks, each with its own cause:

**1. Identity / `equals` — the wrapper is a different object.** `new Footer(raw)` is not `equals` to `raw` unless the
decorator forwards `equals`, and forwarding `equals` from a wrapper is itself unsound (it breaks symmetry against the
unwrapped object — the same defect as the ColorPoint card). So caches, sets, and maps keyed on the object see wrapper
and wrappee as unrelated.
[→ equals between a base and its subclass](#design-equals-between-a-base-and-its-subclass)

**2. `instanceof` / type inspection — only the outermost layer is visible.** `body` is a `Sign`; `Redact` is buried
two layers down. Any code that asks "what *is* this?" instead of "what can this *do*?" sees only the top wrapper.
Decorators are unusable with type-inspecting clients; that is a design constraint, not a bug to patch.

**3. The SELF problem — an inner layer hands out `this`, and `this` is the unwrapped object.** When the wrappee passes
itself to a callback, listener, or registry, the wrappers vanish from that reference. Everything reached through that
callback bypasses the decoration entirely.

```java
class Raw implements Content {
    void register(Registry r) { r.add(this); }      // `this` = the RAW object — Footer/Redact/Sign are gone
}
```

**Fix — the interface must be the only contract, and identity must move off the object.**

```java
interface Content {
    String render();
    default boolean has(Class<? extends Content> type) { return type.isInstance(this); }  // opt-in introspection
}
abstract class ContentDecorator implements Content {
    protected final Content inner;
    public boolean has(Class<? extends Content> type) {                 // ask the chain, not the top layer
        return type.isInstance(this) || inner.has(type);
    }
}
```

- **Identity:** key the cache on a **value** the content produces (`render()`, a content hash, an explicit `id()`
  field), never on the object reference. Do not forward `equals` through a wrapper.
- **`instanceof`:** if clients must ask what is applied, make it a **query on the interface** (above) — the chain
  answers, not the outermost layer. If they need it often, the shape is wrong: that is a capability list, and a
  `Set<Transform>` field beats a wrapper stack.
- **SELF:** the wrappee must never leak `this`. Either the decorated object is the only reference anyone ever holds
  (construct it wrapped, register the wrapper), or the callback is inverted — the *outermost* wrapper registers.

**When to stop using decorator:** ordering matters and clients must control it; layers must be inspected or removed;
or `equals`/identity is load-bearing. Those all want a **list of transforms applied by a pipeline**, not a stack of
wrappers.
[→ when is decorator the right tool?](3_4_classes_and_interfaces.md#when-is-decorator-the-right-tool)

</details>

</details>

### Design: adding a method to a published interface
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You publish `PaymentGateway` as a library interface; a dozen external teams implement it. A new requirement: every
gateway must support `refund(Transaction)`. Adding an abstract method breaks every implementation at compile time.
Adding a `default` method compiles everywhere — so ship the default. Sound?

```java
public interface PaymentGateway {
    Receipt charge(Money m, Card c);
    // add: Refund refund(Transaction t);
}
```

</details>

<details><summary>Show answer</summary>

**Wrong instinct: `default Refund refund(Transaction t) { throw new UnsupportedOperationException(); }`.** It compiles
everywhere and breaks everywhere *at runtime instead* — the failure moves from a build the implementer sees to a
production call the caller sees. That is worse, not better.

**Why the "safe" default is a trap:** a `default` is a promise that a *reasonable* behavior exists for every
implementer. If no sane default exists, the method has no business being a default — you have used a compatibility
mechanism to hide a real incompatibility. The classic tell: a default whose body throws, returns `null`, or silently
does nothing.

**The second failure — defaults can't know the implementer's invariants.** `Collection.removeIf` is the JDK's own
example: it is a correct default over `iterator()`, and it is wrong for any implementation with synchronization or
extra bookkeeping the default cannot see. A default is code injected into classes you have never read.
[→ relationship between inheritance and encapsulation](3_3_classes_and_interfaces.md#what-is-the-relationship-between-inheritance-and-encapsulation)

**Fix — decide by whether a genuine default behavior exists:**

- **A real default exists** (`refund` is expressible over existing methods, correct for everyone) → ship the default,
  document exactly what it does and which invariants it assumes.
- **No real default exists, refund is optional** → **a separate capability interface.** Implementers opt in; callers
  test for the capability, not for a runtime exception. Same move as the overdraft subset — a capability is a
  contract, not a position in a tree.
  [→ a feature that fits some subtypes, not all](3_9_classes_and_interfaces.md#design-a-feature-that-fits-some-subtypes-not-all)
- **No real default and refund is mandatory** → **a new interface version** (`PaymentGatewayV2 extends
  PaymentGateway`), with a migration window. The break is real; make it visible at compile time rather than at 3am.

```java
public interface PaymentGateway { Receipt charge(Money m, Card c); }

public interface Refundable {                        // capability — implemented only where it makes sense
    Refund refund(Transaction t);
}
// caller:
if (gateway instanceof Refundable r) r.refund(tx);   // capability tested, not an exception caught
```

**Interface vs abstract class, the version worth saying out loud:** the choice is not "many vs one" — it is **who owns
evolution.** An abstract class can add a concrete method safely *because you own every subclass in the same release*.
A published interface cannot, *because you don't*. Defaults narrow that gap; they do not close it.

</details>

</details>

### Design: composition when the whole surface must forward
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You accept the advice: don't extend `List`, wrap it. Your `AuditedList` must still be a `List` to pass to existing
APIs, so it forwards all ~25 methods. Two problems appear: writing 25 forwarders per wrapper, and a second wrapper
(`SynchronizedList`) that must stack on the first. Model it.

</details>

<details><summary>Show answer</summary>

**The instinct is right; the cost is real.** Composition's price is forwarding boilerplate, and pretending otherwise
is how the advice gets rejected in real reviews.

**Fix — split the wrapper in two: a reusable forwarding class, and the thin class that adds behavior.**

```java
public class ForwardingList<E> implements List<E> {       // written ONCE, reusable by every wrapper
    private final List<E> inner;
    public ForwardingList(List<E> inner) { this.inner = inner; }
    public boolean add(E e)      { return inner.add(e); }
    public E get(int i)          { return inner.get(i); }
    // ...one forwarder per method — mechanical, no logic
}

public class AuditedList<E> extends ForwardingList<E> {   // the interesting part is now ~5 lines
    private final Audit log;
    public AuditedList(List<E> inner, Audit log) { super(inner); this.log = log; }
    @Override public boolean add(E e) { log.record("add", e); return super.add(e); }
}
```

`AuditedList(new SynchronizedList(raw))` — wrappers stack, because each depends only on the `List` contract, never on
the next one's internals. That is exactly what `extends ArrayList` cannot do.
[→ a counting Set by extension](3_9_classes_and_interfaces.md#design-a-counting-set-by-extension)

**Note the subtlety:** `AuditedList extends ForwardingList` *is* inheritance — and it is safe, because
`ForwardingList` is a class **designed for extension**: no self-use, no invariants, every method a documented pure
forward. The rule was never "never extend"; it was "never extend a class not designed for it."

**The three costs you must be able to name:**

- **Boilerplate** — solved by the forwarding class (one per interface, reused by every wrapper).
- **Identity and `instanceof`** — the wrapper is a different object; `equals`, caches, and type checks see through
  nothing. Same leak as the decorator card.
  [→ wrappers that must survive identity checks](#design-wrappers-that-must-survive-identity-checks)
- **SELF (the callback problem)** — if the wrapped object passes `this` to a callback or registry, that reference
  skips the wrapper, and the auditing silently stops. This is why wrappers are unsuitable for callback-heavy types
  (listener registries, frameworks that hand `this` around).

**When composition genuinely does not fit:** the type has no interface to forward to (a concrete class with no
contract), or it is callback-based. Then the honest answers are: extract an interface first, or accept inheritance
against a base **explicitly designed and documented for extension** — and say which one you are doing.

</details>

</details>

### Design: a switch over types instead of polymorphism
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You have `sealed interface Shape permits Circle, Rectangle, Triangle`. Two features are requested: `area()`, and a
new `render(GraphicsContext)`. A reviewer says every operation should be a method on the type — that's polymorphism;
a `switch` over types is a smell. Where does each operation go?

```java
double area(Shape s) {
    return switch (s) {
        case Circle c    -> Math.PI * c.r() * c.r();
        case Rectangle r -> r.w() * r.h();
        case Triangle t  -> 0.5 * t.b() * t.h();
    };
}
```

</details>

<details><summary>Show answer</summary>

**Wrong instinct: "a switch on types is always a smell, move it into the classes."** Not always — it depends on which
direction the code is expected to grow, and the sealed hierarchy exists precisely to make one direction safe.

**The axis: which changes more often — the set of types, or the set of operations?**

- **Types change often, operations are stable** → put the operation **on the type** as a method. A new type is one
  new class implementing the interface, and no existing code is touched. A `switch` here would mean editing every
  switch on every new type.
- **Types are closed, operations grow** → keep the operation **outside**, as a `switch` over the sealed set. A new
  operation is one new function; the types are never touched. Forcing it into the classes means editing every class
  for every operation.

This is the classic trade-off with a real name: the **expression problem**. Polymorphism makes adding types cheap and
adding operations expensive; switching makes the reverse. One tool cannot make both cheap.
[→ what to use instead of a tagged class](3_7_classes_and_interfaces.md#what-should-you-use-instead-of-a-tagged-class)

**Why `sealed` changes the verdict.** A switch over an *open* hierarchy is a genuine smell — the compiler cannot tell
you when a case is missing, so a new type silently falls into `default`. `sealed` makes the set closed and the switch
**exhaustive**: add a permitted type and every switch stops compiling until it is handled. The compiler now does the
job the smell was warning about.

**So the placement rule for this case:**

- `area()` — intrinsic to the shape, defined by its own data, no external dependency → **a method on the type.**
- `render(GraphicsContext)` — needs a graphics library the domain types must not depend on, and is one of many future
  operations (`serialize`, `hitTest`, `boundingBox`) → **an external exhaustive switch**, keeping the
  graphics dependency out of the model.

```java
sealed interface Shape permits Circle, Rectangle, Triangle {
    double area();                                       // intrinsic — belongs on the type
}
record Circle(double r) implements Shape { public double area() { return Math.PI * r * r; } }

// external operation — model stays free of the graphics dependency; new operations cost one function
final class ShapeRenderer {
    void render(Shape s, GraphicsContext g) {
        switch (s) {
            case Circle c    -> g.drawCircle(c.r());
            case Rectangle r -> g.drawRect(r.w(), r.h());
            case Triangle t  -> g.drawTriangle(t.b(), t.h());
        }   // exhaustive — a fourth shape breaks this at compile time, by design
    }
}
```

The mistake the tagged-class card warns about is a switch on a **hand-rolled tag field** inside one class — the
subtyping is faked. This is a switch on **real types the compiler checks**, deliberately placed outside. Different
thing.

**The senior answer in one line:** *"A switch over a sealed hierarchy isn't a tagged class — it's the visitor pattern
with compiler support. Use it when the operations grow and the types don't."*

</details>

</details>

### Design: a strategy that needs the host's internals
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You lift a pricing rule out of `Order` into a `PricingStrategy` field. The rule needs the order's line items, its
customer tier, and the running subtotal. The simplest way to give it access is to pass the order itself. Model it.

```java
interface PricingStrategy { Money price(Order o); }      // hand it the whole host
class Order {
    private final PricingStrategy pricing;
    Money total() { return pricing.price(this); }         // pass `this`
}
```

</details>

<details><summary>Show answer</summary>

**Wrong instinct: pass `this` so the strategy can reach whatever it needs.** It compiles and it is the most common
way strategy rots into an inheritance-shaped coupling without the `extends` keyword.

**Why it breaks:**

- **The strategy now depends on `Order`'s whole surface** — every public method is reachable, so any change to `Order`
  can break a strategy, and a strategy can call back into `Order` mid-calculation (including `total()` itself →
  infinite recursion, or an inconsistent read of a half-updated order).
- **The strategy is no longer reusable or testable in isolation** — testing it needs a whole `Order`, not the three
  values it actually reads. The `PricingStrategy` interface claims a small contract and hides a large one.
- **It leaks a partially-built object** if `total()` can be reached during construction — the same hazard as the base
  constructor calling an override, arriving through a field instead of a hook.
  [→ a base constructor that calls an override](#design-a-base-constructor-that-calls-an-override)

**Fix — pass the data the strategy needs, not the object that holds it.** The parameter list becomes the real
contract, and it is now visible.

```java
record PricingInput(List<LineItem> items, Tier tier, Money subtotal) { }   // exactly what the rule reads

interface PricingStrategy { Money price(PricingInput in); }                // small, honest, testable contract

class Order {
    private final PricingStrategy pricing;
    Money total() {
        return pricing.price(new PricingInput(items(), customer.tier(), subtotal()));  // host decides what to expose
    }
}
```

Now the strategy cannot reach back, cannot see a half-built order, and its test is three values wide.

**The general rule: a strategy takes values, not its host.** If the argument list grows uncomfortable, that is
signal — either the "strategy" is really several rules (split it), or those fields belong together as a value type
(name it, as `PricingInput` above). An uncomfortable parameter list is information; `this` hides it.

**When passing the host is defensible:** the callee is genuinely a *part* of the host with a stable, narrow, mutual
contract — and even then, pass a **narrow interface the host implements** (`PricingView` exposing three accessors),
never the concrete class. The point is to keep the dependency the size of the actual need.
[→ when is strategy the right tool?](3_4_classes_and_interfaces.md#when-is-strategy-the-right-tool)

</details>

</details>

### Design: changing a base class already in use
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You own `HttpClientBase`, a published class with `send(Request)`. Twelve teams extend it. You need to add retry: every
send should retry on timeout. Adding it inside `send()` is one line. What breaks?

```java
public class HttpClientBase {
    public Response send(Request r) { return transport.execute(r); }
}
class BillingClient extends HttpClientBase {
    @Override public Response send(Request r) { audit(r); return super.send(r); }   // one of twelve
}
```

</details>

<details><summary>Show answer</summary>

**Wrong instinct: add the retry loop inside `send()` and ship it.** You cannot see the subclasses, so you cannot know
what the change does to them. Every one of these is now possible:

- **A subclass that overrides `send()` never gets the retry** — `BillingClient` calls `super.send(r)` once per its own
  call, so retry behavior depends on whether a subclass overrode a method. The base's promise now varies by subclass.
- **A subclass that overrode `send()` to add its own retry now retries 3 × 3 = 9 times.** Its behavior changed with no
  edit on its side — the base silently multiplied it.
- **A subclass whose `send()` is not idempotent** (audits, increments a counter, publishes an event) now does that
  side effect three times per logical send.
- **Anything that overrides `send()` and does not call `super`** loses the feature entirely and cannot be told.

**The real defect: `send()` is simultaneously the public API and the extension point.** The base cannot change a
method's behavior when subclasses may have overridden it, because the base does not know whether it is calling its own
code. This is the fragile base class problem — and it is the mirror image of the counting-set card, where the
*subclass* was broken by the base's self-use.
[→ a counting Set by extension](3_9_classes_and_interfaces.md#design-a-counting-set-by-extension)

**Fix — separate the invariant public entry point from the documented extension point.**

```java
public class HttpClientBase {
    public final Response send(Request r) {              // FINAL — the promise the base controls; retry lives here
        for (int attempt = 1; ; attempt++) {
            try { return doSend(r); }                    // calls the hook, not the API
            catch (TimeoutException e) { if (attempt == 3) throw e; }
        }
    }
    /** Performs exactly ONE transport attempt. Called once per retry by {@link #send}. Must be idempotent. */
    protected Response doSend(Request r) { return transport.execute(r); }   // the documented extension point
}
class BillingClient extends HttpClientBase {
    @Override protected Response doSend(Request r) { audit(r); return super.doSend(r); }  // retried, as documented
}
```

`send()` is `final`, so the retry cannot be bypassed or doubled. `doSend()` is the only hook, and its **self-use is
documented** — the contract states it is called once per attempt and must be idempotent. A subclass can now reason
about the change; before, it could only be surprised by it.

**The migration reality (the lead-level half of the answer):** `send()` was not final, so making it final breaks the
twelve teams at compile time. That is the correct trade — a compile break they can see beats a behavior change they
cannot. Sequence it: ship `doSend()` and deprecate overriding `send()`, give a release window, then seal it. The
alternative — a new class the teams migrate to — is right when the existing subclasses are too varied to converge.

**The rule:** *design for inheritance and document it, or prohibit it.* A class published for extension owes its
subclasses a documented self-use contract; if you are not willing to write that document and freeze it, make the class
`final` and expose the variation as a field instead.

</details>

</details>

### Design: splitting a shipped hierarchy with live callers
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

`Employee` has grown into a god class: pay calculation, tax rules, scheduling, notification preferences, and an ORM
mapping — 2,000 lines, with `SalariedEmployee`, `HourlyEmployee`, and `ContractEmployee` under it. Four services and a
public REST API depend on it. You are asked to fix the design. What is the plan?

</details>

<details><summary>Show answer</summary>

**Wrong instinct: design the correct target model and do a big-bang refactor.** The correct model is the easy half;
this question is not about the model, it is about **changing a type that callers depend on without a coordinated
release.** An answer that goes straight to the class diagram fails the question.

**Diagnose first — the god class is not one problem:**

- **Multiple axes fused into one tree.** Pay type (salaried/hourly/contract) is being modeled as a *subtype*, but it
  combines freely with role, department, and tenure. That tree is spending its one dimension on the wrong thing.
  [→ pay type — subtype or field?](3_8_classes_and_interfaces_design.md#property-pay-type-salaried--hourly--subtype-or-field)
- **Unrelated responsibilities co-located.** Tax rules, scheduling, and notification preferences change for different
  reasons, on different release cadences, driven by different teams. They are separate concerns sharing a class only
  because they share an identifier.
- **Persistence is entangled with the domain.** The ORM mapping is the reason nobody splits it — the class's shape is
  pinned by the table.

**Fix — the target shape:** a small `Employee` identity/core, with each concern extracted as its own type the core
holds or is keyed by (`PayTerms`, `TaxProfile`, `Schedule`, `NotificationPrefs`). Pay type stops being a subclass and
becomes a `PayTerms` **strategy field**; the tree either disappears or is spent on a genuine single grouping.

**The plan — this is the actual answer:**

1. **Freeze the class.** No new fields or methods on `Employee` while the split runs. Without this, the migration
   races the feature work and loses.
2. **Characterize before moving.** The hierarchy is under-tested precisely where it is risky. Write tests against the
   current *observable* behavior — including the bugs — so a behavior change is visible, not discovered.
3. **Extract by seam, not by class diagram.** Take one concern (`TaxProfile` — the one with the clearest boundary and
   the fewest callers), move it behind a new type, and have the old `Employee` methods **delegate** to it. The public
   surface is unchanged; the internals moved. Ship this. Repeat.
4. **Keep the old API as a facade during migration.** `employee.calculateTax()` stays and forwards. Callers migrate to
   `taxProfile.calculate()` on their own schedule; you deprecate with a date, not with a hope.
5. **The REST API is a separate contract — do not let it move.** It is versioned independently and mapped from the
   domain by an explicit DTO layer. If the API shape is currently the class shape, that coupling is the first thing to
   break, before any domain split.
6. **Take persistence last.** The ORM mapping is the hardest constraint; splitting the domain while the table is
   unchanged is possible (mapping several types to one table), and it is a smaller step than doing both at once.

**The judgment the interviewer is testing:** *strangler, not big bang* — every step ships, each step is reversible,
and the old and new shapes coexist. Also the willingness to say **what you would not do**: a hierarchy that works and
is merely ugly, with no active change pressure, may be left alone. Refactoring buys the ability to change; if nothing
needs to change there, the purchase is a cost with no return.

The one-line version: *"I'd freeze it, characterize it, extract one concern at a time behind a delegating facade,
version the API separately, and take the ORM last — and I'd check first that the pain is real, not aesthetic."*

</details>

</details>
