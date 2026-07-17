## Classes and Interfaces - Inheritance Design Challenges #1

### Which t-shirt properties belong to every shirt vs one kind?
<details><summary>Show answer</summary>

**Draft answer — answers only the narrow question, not yet correct OOP.** 
It shows the typical first instinct: put shared state on the parent, branch state on the branch.

```java
class TShirt { Size size; Color color; }              // every shirt
class CrewNeck extends TShirt { }                      // no collar, no buttons
class Polo     extends TShirt { int buttonCount; }     // only this branch
```

- `size` (S/M/L), `color` — every shirt has them, picked freely → **belong to every type** → on the parent.
- `buttonCount` — exists only on `Polo`; a crew-neck has no button count at all → **branch-local** → on the branch.

**Why this draft is not the final design:**

- **`equals` contract.** Adding a value field (`buttonCount`) to a branch of an *instantiable* parent that has a
  reasonable `equals` cannot preserve the contract: a `TShirt` and a `Polo` can never be equal both ways without
  breaking symmetry or transitivity.
- **Composition is not the fix here.** "Favor composition over inheritance" targets `has-a`. A `Polo` *is-a*
  `TShirt` and is fully substitutable for one — it does **not** contain a t-shirt. This is a real subtype, so
  inheritance is the correct tool; the defect is the *shape of the base*, not the use of inheritance.
- **A concrete `TShirt` is wrong for this domain.** A bare `TShirt` would be a shirt that is neither crew-neck nor
  polo — no such object exists in reality; every real shirt is one specific kind. A concrete parent also invites
  `new TShirt(...)`, producing a meaningless half-object, and is a concrete class being extended without being
  designed for it.

**Correct design:** make `TShirt`:
- **`abstract`** so no bare shirt can exist. Abstract also removes the `equals` problem: 
  with no instantiable parent, there is no cross-boundary comparison to break.
- **`sealed`** so the set of kinds is closed and compiler-checked. 

```java
sealed abstract class TShirt permits CrewNeck, Polo {   // closed set — only these two kinds
    Size size; Color color;                             // every shirt has these
}
final class CrewNeck extends TShirt {}                  // adds no state of its own
final class Polo extends TShirt {
    int buttonCount;                                    // only this branch has buttons
}
```

**Alternative design (record-based, for immutability):** a `record` is implicitly `final`, so it cannot be a
parent — but it *can* implement a `sealed interface`. The base becomes an interface; each kind is a record.

```java
sealed interface TShirt permits CrewNeck, Polo {        // closed set, but carries no state
    Size size();                                        // shared accessors only — no fields to inherit
    Color color();
}
record CrewNeck(Size size, Color color) implements TShirt {}          // immutable, free equals/hashCode
record Polo(Size size, Color color, int buttonCount) implements TShirt {}  // re-declares shared components
```

- **Buys:** immutability and correct `equals`/`hashCode` for free — a `CrewNeck` and a `Polo` are different types
  and never compare equal.
- **Costs:** an interface holds no state, so `size`/`color` cannot be declared once — every record repeats them.
  Fine at two shared fields; painful if the shared part is large.

You only truly choose between: **shared state implemented once** (abstract class) vs **free immutability + value
semantics** (records on a sealed interface). For a small closed domain like this, the record-based version is the
modern default.

</details>

### Property: `teamSize` on a manager — parent or branch?
<details><summary>Show answer</summary>

**Branch-local.** `teamSize` means something only for a manager; a non-manager has no team size, so it cannot sit
on the shared base.

But it cannot simply be added as a field on a `Manager` that extends a concrete `Employee` — that repeats the
defect covered in full on the [t-shirt card](#which-t-shirt-properties-belong-to-every-shirt-vs-one-kind): a value
field on a branch of an instantiable parent breaks `equals`, and a bare `Employee` is a meaningless half-object.

The trap specific to this domain: it is tempting to let the base `Employee` *double as* the non-manager, with
`Manager` as its subtype. Don't — the base must never be one of the variants. Force **"non-manager" to become its
own named type** (`IndividualContributor`), and force the base to be **`abstract` + `sealed`**, so no bare employee
can exist and the set of kinds is closed and compiler-checked.

```java
sealed interface Employee permits Manager, IndividualContributor {   // closed set — no bare employee
    String name();
    int id();                                                        // shared accessors only
}
record Manager(String name, int id, int teamSize) implements Employee {}    // teamSize lives only here
record IndividualContributor(String name, int id) implements Employee {}    // the former "non-manager"
```

Now `teamSize` sits on exactly the branch it has meaning for, records give correct `equals`/`hashCode` for free, and
no instantiable parent exists to break the contract. The t-shirt card carries the detailed reasoning for each of
these moves.

</details>

### Property: pay type (salaried / hourly) — subtype or field?
<details><summary>Show answer</summary>

**A field, not a subtype.** Pay type combines freely with every role — a Manager can be salaried or hourly, an
Engineer can be salaried or hourly, and the role never fixes the choice. Every (role, payType) pairing is valid.

Model it by subclassing and you get `SalariedManager`, `HourlyEngineer`, … = role × payType subclasses → explosion.
[The free combination across every branch](../FAQs/3.4_classes_and_interfaces.md#how-does-subclassing-explode-on-varying-state)
is the signal: it belongs to every type, so it rides on the type as a field, not in the tree.

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

</details>

### Does "every object has one" put it on the base?
<details><summary>Show answer</summary>

**No — universal presence is necessary but not sufficient.** Two independent tests hide behind "it applies to all":

- **Presence** — does every object have some value (never null)? This is about *existence*, not placement.
- **Independence from type** — can the value vary while the object's type stays fixed, with every pairing still a
  legal object? This is the real placement test.

A property can be universally present yet have its value *pinned by the type*. Presence being universal tells you
nothing about placement; only the second test does.

**Cause (present-but-type-fixed → not a base field):** every employee has a bonus formula, but the formula is fixed
by role — a Manager always gets the manager formula, an IC the IC formula, and "senior-manager formula on an entry
IC" is not a legal object. Presence is universal; the value is a function of the type. So every type *answers* for
the property, but each supplies its **own fixed value** — it is not a slot the caller fills.

```java
sealed interface Employee permits Manager, IndividualContributor {
    BonusFormula bonusFormula();                 // every type answers this — value is fixed per type
}
record Manager(String name, int id) implements Employee {
    public BonusFormula bonusFormula() { return BonusFormula.MANAGER; }   // pinned, not passed in
}
record IndividualContributor(String name, int id) implements Employee {
    public BonusFormula bonusFormula() { return BonusFormula.IC; }        // pinned
}
```

**Contrast (present-and-free → base field):** every employee has a bonus formula, and HR may attach *any* formula to
*any* employee — manager on the flat one, IC on the tiered one, all legal. Presence is universal *and* the value is
independent of type, so it becomes a real slot the caller sets, declared once as a shared component.

```java
sealed interface Employee permits Manager, IndividualContributor {
    BonusFormula bonusFormula();                 // real slot — the value is chosen, not pinned
}
record Manager(String name, int id, BonusFormula bonusFormula) implements Employee {}              // caller chooses
record IndividualContributor(String name, int id, BonusFormula bonusFormula) implements Employee {} // caller chooses
```

Both cases expose `bonusFormula()` on every type — the difference is upstream: **who decides the value.** Type-fixed
→ the type pins it (constant per branch). Free → the caller sets it (a shared field). "Belongs on the base" means a
freely-set slot, not merely "appears on all types."

**Diagnostic:** hold the type fixed and try to vary the property. If it still varies freely → base. If varying it
forces a different type or yields an impossible object → type-fixed.

</details>

### Property: `shiftType` — subtype or field?
<details><summary>Show answer</summary>

- Rule: *managers never work nights.* → the role **constrains** the value; not freely combinable → **branch-local**.
- Rule: *any role can be assigned either shift.* → the value combines freely with every branch → **field on the
  type**.

Identical property, opposite verdict, decided only by whether the domain permits free combination. 
The sharpest proof that placement lives in the domain rules, never in the property name.

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
[→ parent or branch?](../FAQs/3.4_classes_and_interfaces.md#what-decides-whether-a-property-belongs-to-the-parent-or-a-branch)

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
  [→ when is strategy the right tool?](../FAQs/3.4_classes_and_interfaces.md#when-is-strategy-the-right-tool) 
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

**The property name can't decide it. Ask two things — not just "does every kind have one?"** Universal presence
alone is not the test; the value must also be freely set, independent of the notification type.
[→ does "every object has one" put it on the base?](#does-every-object-has-one-put-it-on-the-base)

**Case A — only Email has a subject (the usual rule).** SMS and push have none, so `subject` is **branch-local** and
lives on the `Email` subclass. This is safe *because `Notification` is `abstract`* — no one can create a bare
`Notification`, so there is no instantiable parent whose `equals` a value field on `Email` could break.
[→ why the base must be abstract](#which-t-shirt-properties-belong-to-every-shirt-vs-one-kind)

```java
class Email extends Notification {
    String subject;             // branch-local — only Email, safe because the base is abstract
    ...
}
class Sms  extends Notification { ... }   // no subject
class Push extends Notification { ... }   // no subject
```

**Case B — every kind carries a subject, and the value is free.** Presence is universal *and* the subject is a
free-text string the caller sets on any kind — no type pins its value. That is the real base-field condition, so it
goes on the **parent**.

```java
abstract class Notification {
    String subject;             // on the base — present on every kind AND freely set, independent of type
}
```

`subject` is always a plain **field** either way — free text has no fixed set to switch on, so it can never drive
control flow; `send()` reading it is *use*, not branching. No strategy, no new tree. The only open question is
placement, and placement turns on **presence + independence from type**, not presence alone.

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
subclass, not the parent — same as any one-branch property, and safe because the base is abstract (no bare
`Notification` exists to break `equals`).

**Platform (Android / iOS) must not become a tree.** Platform only exists where the token does — inside the Push
branch — so the free-combination test runs *within Push*, not across the whole hierarchy. There it is free: the
platform value varies independently of anything else Push carries. It's tempting to split `AndroidPush` / `IosPush`
because the format differs, but a platform tree under Push would cross Push's own grouping and start the same
multiplication a branch deep → `AndroidPush`, `IosPush`, repeated for every later Push variation → explosion, just
scoped to a branch.

Keep platform as a **field**. The token *format* is not free — it is **pinned by the platform value** (Android
format for `ANDROID`, iOS format for `IOS`); a value decided by another value, not freely set. Handle that with a
strategy keyed off `platform` — still a field, still no second tree.
[→ value decided by type/value vs freely set](#does-every-object-has-one-put-it-on-the-base)

```java
class Push extends Notification {
    String   deviceToken;       // branch-local to Push, safe because Notification is abstract
    Platform platform;          // ANDROID / IOS — a free field, NOT a subclass split
    ...
}
enum Platform { ANDROID, IOS }
```

The format difference is real, but it's handled by the field's value (or a strategy keyed off it), never by a second
tree.

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
- [→ present on every kind AND freely set](#does-every-object-has-one-put-it-on-the-base)
- [→ explosion on varying behavior](../FAQs/3.4_classes_and_interfaces.md#how-does-subclassing-explode-on-varying-behavior)

**A subclass tree is wrong** — it would cross the kind tree and multiply: `TransactionalEmail`, `MarketingEmail`,
`TransactionalSms`, `MarketingSms`, `TransactionalPush`, `MarketingPush` = kind × category. Two trees, the classic
explosion.

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
[→ when is strategy the right tool?](../FAQs/3.4_classes_and_interfaces.md#when-is-strategy-the-right-tool)

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
[→ what rules out subclassing](../FAQs/3.4_classes_and_interfaces.md#what-rules-out-subclassing-as-a-modeling-tool)

**A strategy field is wrong too** — a strategy swaps *one* behavior for another, one active per slot (the `SendPolicy`
in #4 was exactly one policy at a time). Here the transforms are not alternatives; footer **and** redact **and** sign
all run together. Picking one would lose the rest.
[→ when is strategy the right tool?](../FAQs/3.4_classes_and_interfaces.md#when-is-strategy-the-right-tool)

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
[→ when is decorator the right tool?](../FAQs/3.4_classes_and_interfaces.md#when-is-decorator-the-right-tool)
[→ strategy or decorator — which fits?](../FAQs/3.4_classes_and_interfaces.md#strategy-or-decorator--which-fits)

</details>

</details>
