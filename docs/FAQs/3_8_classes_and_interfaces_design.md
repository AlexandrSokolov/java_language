
### Which t-shirt properties belong to every shirt vs one kind?
<details><summary>Show answer</summary>

A t-shirt comes in two kinds — `CrewNeck` and `Polo`:

```java
class TShirt { Size size; Color color; }              // every shirt
class CrewNeck extends TShirt { }                      // no collar, no buttons
class Polo     extends TShirt { int buttonCount; }     // only this branch
```

- `size` (S/M/L), `color` — every shirt has them, picked freely → **belong to every type** → on the parent.
- `buttonCount` — exists only on `Polo`; a crew-neck has no button count at all → **branch-local** → on the
  branch.

The cut: a property either applies to *every* object (parent) or to *only one branch* and is meaningless on the
others (that branch).
[→ parent or branch?](3.4_classes_and_interfaces.md#what-decides-whether-a-property-belongs-to-the-parent-or-a-branch)

</details>

### Property: `teamSize` on a manager — parent or branch?
<details><summary>Show answer</summary>

**Branch-local data.** `teamSize` only means something inside the Manager branch; a non-manager has no team size, so
it can't sit on the parent.

Adding it as a field in `Manager` is ordinary specialization — a subtype is expected to carry more state than its
parent. One value on one branch, no multiplication.
[→ parent or branch?](3.4_classes_and_interfaces.md#what-decides-whether-a-property-belongs-to-the-parent-or-a-branch)

</details>

### Property: pay type (salaried / hourly) — subtype or field?
<details><summary>Show answer</summary>

**A field, not a subtype.** Pay type combines freely with every role — a Manager can be salaried or hourly, an
Engineer can be salaried or hourly, and the role never fixes the choice. Every (role, payType) pairing is valid.

Model it by subclassing and you get `SalariedManager`, `HourlyEngineer`, … = role × payType subclasses → explosion.
The free combination across every branch is the signal: it belongs to every type, so it rides on the type as a field,
not in the tree.
[→ explosion on varying state](3.4_classes_and_interfaces.md#how-does-subclassing-explode-on-varying-state)

</details>

### Property: `bonusFormula` — subtype or field?
<details><summary>Show answer</summary>

**It depends on the domain rule — there's no fixed answer.**

- If the role **fixes** the formula (the Manager formula exists *because* you're a Manager, and pairing a
  senior-manager formula with an entry engineer is meaningless) → **branch-local**, a field on the branch.
- If **any formula can attach to any role** as a free, independent choice → it belongs to every type → hold it
  on the type as a field (a strategy field if the formula carries behavior).

The name "bonusFormula" tells you nothing. Only the combination rule does. The trap: "every employee has *a* bonus
formula" feels like it applies to all — but applies-to-all is not the test; **free combination** is.
[→ parent or branch?](3.4_classes_and_interfaces.md#what-decides-whether-a-property-belongs-to-the-parent-or-a-branch)

</details>

### Property: `shiftType` — subtype or field?
<details><summary>Show answer</summary>

- Rule: *managers never work nights.* → the role **constrains** the value; not freely combinable → **branch-local**.
- Rule: *any role can be assigned either shift.* → the value combines freely with every branch → **field on the
  type**.

Identical property, opposite verdict, decided only by whether the domain permits free combination. The sharpest proof
that placement lives in the domain rules, never in the property name.
[→ parent or branch?](3.4_classes_and_interfaces.md#what-decides-whether-a-property-belongs-to-the-parent-or-a-branch)

</details>

### Notification design #0 — the starting tree
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

We model notifications. The one thing we subtype on is *kind of notification* — each kind sends differently and
carries different data, so a subclass tree is justified. Here is the starting hierarchy:

```java
abstract class Notification {
    Recipient recipient;        // every notification has one
    abstract void send();
}
class Email extends Notification { ... }   // the ONE tree: kind of notification
class Sms   extends Notification { ... }
class Push  extends Notification { ... }
```

</details>

<details><summary>Show answer</summary>

One tree, one grouping (kind). Everything we add from here, we place by asking: **field on the parent, field on a
branch, or a second tree?** — and a second tree is the thing we must avoid.
[→ what rules out subclassing](3.4_classes_and_interfaces.md#what-rules-out-subclassing-as-a-modeling-tool)

</details>

</details>

### Notification design #1 — adding `priority`
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

Every notification needs a `priority` — `LOW`, `NORMAL`, or `HIGH`. Any notification of any kind can be any priority.
Model it for the existing `Notification` hierarchy.

</details>

<details><summary>Show answer</summary>

`priority` combines freely with every kind — any kind can be any priority — so it belongs to every type and rides on
the type, never a tree. But *how* you hold it splits on one question: **does `send()` itself branch on the value, or
does only the surrounding system read it?**
[→ parent or branch?](3.4_classes_and_interfaces.md#what-decides-whether-a-property-belongs-to-the-parent-or-a-branch)

**Case A — passive data: `send()` ignores it, the invoker reads it.** This is the usual case. The notification just
carries the value; the infrastructure around it reads `priority` to order, schedule, or drop — sorting/comparison
reads, not behavior inside `Notification`. A value others read for ordering is the textbook plain field, same shape
as `Comparable`.

```java
abstract class Notification {
  Recipient recipient;
  Priority priority;         // LOW / NORMAL / HIGH — passive value, read by the dispatcher

  abstract void send();       // send() does NOT look at priority
}

// The invoker — not the notification — acts on priority:
class Dispatcher {
  private final PriorityQueue<Notification> queue =
          new PriorityQueue<>(Comparator.comparing(Notification::priority).reversed());  // HIGH first

  void submit(Notification n) { queue.add(n); } // ordering decided here, by reading the field

  void drainWithinBudget(int maxPerMinute) {
    int sent = 0;
    while (!queue.isEmpty() && sent < maxPerMinute) {
      queue.poll().send();                       // HIGH drains first; LOW dropped when budget runs out
      sent++;
    }
  }
}
```

`send()` never switches on `priority`; the value only steers the queue. Plain field, no branching, no strategy.

**Case B — diverging send behavior: `send()` itself acts on the value.** If different priorities mean different code
paths *inside* the notification (`if (priority == HIGH) … else …`), you have behavior keyed off a value, and a field
with branching is the smell. Two sub-cases:

- **Small and stable** (HIGH retries 3×, others once) → keep the field plus a tiny lookup; 
  one `if` is not an explosion.
- **Real, diverging, growing behavior** → lift it into a strategy keyed off priority — a `RetryPolicy` /
  `DeliveryPolicy` held as a field, the same move as transactional vs marketing in #4.

```java
abstract class Notification {
    Priority       priority;    // still a field for the value
    DeliveryPolicy delivery;    // behavior lifted off the field — strategy, not if/else in send()
    abstract void send();
}
interface DeliveryPolicy { int maxAttempts(); Duration backoff(); }
```

The value is always a field; only its *behavior* moves. 
- Read by others for ordering → Case A. 
  [→ when is strategy the right tool?](3.4_classes_and_interfaces.md#when-is-strategy-the-right-tool) 
- Drives send-time behavior → Case B, strategy field. 
  [→ category as a strategy field (#4)](#notification-design-4--transactional-vs-marketing)

</details>

</details>

### Notification design #2 — adding `subject`
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

We need to add a `subject` to notifications. Where does it go in the existing `Notification` hierarchy?

</details>

<details><summary>Show answer</summary>

**First ask the domain question — does *every* kind have a subject, or only some?** Placement depends entirely on the
answer; the property name alone can't decide it.
[→ parent or branch?](3.4_classes_and_interfaces.md#what-decides-whether-a-property-belongs-to-the-parent-or-a-branch)

**Case A — only Email has a subject (the usual rule).** SMS and push have none, so `subject` is **branch-local** and
lives on the `Email` subclass.

```java
class Email extends Notification {
    String subject;             // branch-local — only Email
    ...
}
class Sms  extends Notification { ... }   // no subject
class Push extends Notification { ... }   // no subject
```

**Case B — every kind carries a subject** (e.g. your system shows one in the SMS/push title). Then it applies to all
objects → it goes on the **parent**.

```java
abstract class Notification {
    String subject;             // on the base — every kind has one
}
```

The deciding test is "does it exist on *every* object, or only one branch?" — every type → parent; one branch →
that subclass. Either way it's still just a **field**: no new tree, no multiplication.

</details>

</details>

### Notification design #3 — adding `deviceToken`
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

A push notification needs a `deviceToken`. A push can target either Android or iOS, and the token format differs by
platform. Where does this go in the existing `Notification` hierarchy?

</details>

<details><summary>Show answer</summary>

Two things arrive together — separate them.

**The token itself is branch-local to Push.** Email and SMS have no device token, so it lives on the `Push`
subclass, not the parent — same as any one-branch property.
[→ parent or branch?](3.4_classes_and_interfaces.md#what-decides-whether-a-property-belongs-to-the-parent-or-a-branch)

**Platform (Android / iOS) must not become a tree.** Platform only exists where the token does — inside the Push
branch — so the free-combination test runs *within Push*, not across the whole hierarchy. There it is free: format
varies independently of anything else Push carries. It's tempting to split `AndroidPush` / `IosPush` because the
format differs, but a platform tree under Push would cross Push's own grouping and start the same multiplication a
branch deep → `AndroidPush`, `IosPush`, repeated for every later Push variation → explosion, just scoped to a branch.
[→ what rules out subclassing](3.4_classes_and_interfaces.md#what-rules-out-subclassing-as-a-modeling-tool)

Keep platform as a **field**, and if the format genuinely needs different behavior, hold that behavior as a strategy
object — still a field, still no second tree.

```java
class Push extends Notification {
    String   deviceToken;       // branch-local to Push
    Platform platform;          // ANDROID / IOS — a field, NOT a subclass split
    ...
}
enum Platform { ANDROID, IOS }
```

The format difference is real, but it's handled by the field's value (or a strategy keyed off it), never by a second
tree.
[→ how to extend without subclassing](3.4_classes_and_interfaces.md#how-do-you-extend-a-type-without-subclassing)

</details>

</details>

### Notification design #4 — transactional vs marketing
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

Every notification can be either **transactional** (password reset, receipt) or **marketing** (promo, newsletter).
The two differ in send-time rules: marketing must honor unsubscribe and quiet hours; transactional ignores both.
This applies to email, SMS, and push alike. Where does this go in the existing `Notification` hierarchy?

</details>

<details><summary>Show answer</summary>

This is a **second, freely-combining behavior** — *category* (transactional / marketing) — and it cuts across
*every* kind: any kind can be either, freely combined. That free combination is the warning sign.
[→ explosion on varying behavior](3.4_classes_and_interfaces.md#how-does-subclassing-explode-on-varying-behavior)

**A subclass tree is wrong** — it would cross the kind tree and multiply: `TransactionalEmail`, `MarketingEmail`,
`TransactionalSms`, `MarketingSms`, `TransactionalPush`, `MarketingPush` = kind × category. Two trees, the classic
explosion.
[→ what rules out subclassing](3.4_classes_and_interfaces.md#what-rules-out-subclassing-as-a-modeling-tool)

**A plain enum field is not enough either** — the categories carry *different behavior* (honor unsubscribe + quiet
hours, or skip both), not just a label. A bare `category` field would push that logic into `if`/`switch` branches
inside `send()`.

**So hold the behavior as a strategy on a field** — the second concern rides as a delegated part, not a tree:

```java
abstract class Notification {
    Priority    priority;
    SendPolicy  policy;         // second concern as a STRATEGY field — not a subclass
    abstract void send();
}
interface SendPolicy { boolean maySendNow(Recipient r); }   // honor unsubscribe + quiet hours, or skip
class TransactionalPolicy implements SendPolicy { ... }     // ignores both
class MarketingPolicy     implements SendPolicy { ... }     // enforces both
```

Now kind stays the one tree, category is a field holding behavior, and the two compose: any kind × any policy with no
new classes. The general fix — a freely-combining behavior becomes a **strategy field**.
[→ when is strategy the right tool?](3.4_classes_and_interfaces.md#when-is-strategy-the-right-tool)

</details>

</details>

### Notification design #5 — adding content transforms
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

Before a notification goes out, its content may be passed through optional transforms: add a standard footer, redact
personal data, translate to the recipient's language, sign the body. Any subset can apply, in combination, and the
same transforms apply to email, SMS, and push alike. Where does this go in the existing `Notification` hierarchy?

</details>

<details><summary>Show answer</summary>

This is the case neither a field nor a strategy covers — **many independent transforms, freely combined, several
active at once**. The clue is the shape: not *one* choice from alternatives, but an open set that stacks.

**A subclass tree is wrong** — transforms combine, so a tree gives a class per subset: `FooterEmail`,
`FooterRedactedEmail`, `FooterRedactedSignedEmail`, … = the powerset, far worse than the product in #4.
[→ what rules out subclassing](3.4_classes_and_interfaces.md#what-rules-out-subclassing-as-a-modeling-tool)

**A strategy field is wrong too** — a strategy swaps *one* behavior for another, one active per slot (the `SendPolicy`
in #4 was exactly one policy at a time). Here the transforms are not alternatives; footer **and** redact **and** sign
all run together. Picking one would lose the rest.
[→ when is strategy the right tool?](3.4_classes_and_interfaces.md#when-is-strategy-the-right-tool)

**So stack them as decorators** — each transform wraps the content step and delegates the rest, so any subset layers
in any order with no class per combination:

```java
interface Content { String render(); }                 // the thing being transformed

abstract class ContentDecorator implements Content {   // wraps one Content, adds its bit
    protected final Content inner;
    protected ContentDecorator(Content inner) { this.inner = inner; }
}
class Footer  extends ContentDecorator {
    Footer(Content c) { super(c); }
    public String render() { return inner.render() + "\n-- Sent by Acme"; }
}
class Redact  extends ContentDecorator {
    Redact(Content c) { super(c); }
    public String render() { return stripPii(inner.render()); }
}
// Sign, Translate likewise

// Any subset, any order — composed, not subclassed:
Content body = new Sign(new Footer(new Redact(raw)));   // redact, then footer, then sign
```

`send()` calls `render()` once and stays unaware of which transforms are present. Adding a new transform is a new
decorator class, never a new combination. Kind stays the one tree; transforms ride as a stacked, composed part.

The discriminator across #4 and #5: **one behavior chosen from alternatives → strategy; many behaviors combining at
once → decorator.**
[→ when is decorator the right tool?](3.4_classes_and_interfaces.md#when-is-decorator-the-right-tool)
[→ strategy or decorator — which fits?](3.4_classes_and_interfaces.md#strategy-or-decorator--which-fits)

</details>

</details>

### Can you give an example of the Decorator pattern?
<details><summary>Show answer</summary>

```java
// Common interface
public interface Coffee {
  double cost();
  String description();
}

// Base implementation
public class SimpleCoffee implements Coffee {
  @Override public double cost()        { return 2.0; }
  @Override public String description() { return "Simple coffee"; }
}

// Abstract decorator — holds and implements the same interface
public abstract class CoffeeDecorator implements Coffee {
  protected final Coffee coffee;
  protected CoffeeDecorator(Coffee coffee) { this.coffee = coffee; }
}

// Concrete decorators — add behavior, delegate the rest
public class MilkDecorator extends CoffeeDecorator {
  public MilkDecorator(Coffee coffee) { super(coffee); }
  @Override public double cost()        { return coffee.cost() + 0.5; }
  @Override public String description() { return coffee.description() + ", milk"; }
}

public class SugarDecorator extends CoffeeDecorator {
  public SugarDecorator(Coffee coffee) { super(coffee); }
  @Override public double cost()        { return coffee.cost() + 0.2; }
  @Override public String description() { return coffee.description() + ", sugar"; }
}
```

Wrap a base object in successive decorators; each adds its bit and delegates the rest:

```java
Coffee coffee = new SimpleCoffee();
coffee = new MilkDecorator(coffee);
coffee = new SugarDecorator(coffee);

coffee.description();  // "Simple coffee, milk, sugar"
coffee.cost();         // 2.7

// equivalently, nested in one expression:
coffee = new SugarDecorator(new MilkDecorator(new SimpleCoffee()));
```

`Milk` and `Sugar` combine freely without a `MilkSugarCoffee` class — the add-ons layer without a class per
combination. Each decorator depends only on the `Coffee` interface, never on another decorator's internals, so
encapsulation holds.
[→ when is decorator the right tool?](3.4_classes_and_interfaces.md#when-is-decorator-the-right-tool)

</details>

### What does functional decoration look like in code?
<details><summary>Show answer</summary>

Model the decorated thing as an immutable value and each decoration as a `T -> T` transform, then **compose** the
transforms. No interface, no abstract decorator, no wrapper classes:

```java
// The decorated thing is a value, not a behavioral object
record Coffee(double cost, String description) { }

// Each decoration is a Coffee -> Coffee transform
UnaryOperator<Coffee> milk  = c -> new Coffee(c.cost() + 0.5, c.description() + ", milk");
UnaryOperator<Coffee> sugar = c -> new Coffee(c.cost() + 0.2, c.description() + ", sugar");

// Stacking decorations becomes andThen composition
Coffee result = milk.andThen(sugar).apply(new Coffee(2.0, "Simple coffee"));
// result -> Coffee[cost=2.7, description="Simple coffee, milk, sugar"]
```

Each `andThen` is one more layer; adding a feature is a **new function, not a new class**. Compare the OOP Decorator
for the same example — interface, abstract `CoffeeDecorator`, and a concrete class per feature — all of which
collapse here into two lambdas, *because* `Coffee` is pure data. The instant `Coffee` needed identity or state, this
would break and the wrapper-class version would be the right one.
[→ functional decoration vs the OOP Decorator](3.4_classes_and_interfaces.md#functional-decoration-vs-the-oop-decorator)

</details>
