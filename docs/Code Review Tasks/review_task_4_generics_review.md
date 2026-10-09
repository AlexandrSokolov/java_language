# Review report — `Box<T>` generics & exceptions

A record of the review of the `Box<T>` snippet, the full back-and-forth, what we agreed, and what to
revisit. This was your weakest review result so far, so the value is not the verdict — it is the map of
*where* the reasoning slipped and where it held. Re-run the snippet cold in a month; measure against this.

---

## 0. The snippet under review

```java
import java.util.ArrayList;
import java.util.List;

public class Box<T> {

  private List<T> contents = new ArrayList<>();

  public void add(T item) {
    contents.add(item);
  }

  public T get(int i) {
    return contents.get(i);
  }

  public void addAll(List<T> items) {
    for (T item : items) {
      contents.add(item);
    }
  }

  public void copyFrom(Box other) {              // raw type
    for (Object o : other.contents) {
      contents.add((T) o);                       // unchecked cast
    }
  }

  public static double sumSizes(List<Box> boxes) {   // raw type in the element
    double total = 0;
    for (Box b : boxes) {
      total += b.contents.size();
    }
    return total;
  }
}
```

---

## 1. Scoreboard — what was right, wrong, or a defensible choice

| # | Site | Your call | Verdict |
|---|------|-----------|---------|
| 1 | `add` | Add `? extends T` on the single item | **Wrong** — the one flat error. Direction is backwards; no wildcard belongs on a bare `T` at all. |
| 2 | `add` return | Return `boolean` for consistency with collections | **Defensible choice**, but the *reason* you gave was wrong and got repaired. |
| 3 | `get` validation | Add a bounds check with size in the message | **Right on the message** (I was wrong to call it redundant). **Wrong on the type** for `Box`. Guard was one-sided. |
| 4 | `copyFrom` | `Box<? extends T>`, loop var typed `T` | **Right.** No correction. |
| 5 | `addAll` | `List<? extends T>`, PECS producer | **Right.** No correction. |
| 6 | `sumSizes` | `List<Box<?>>` for any box; drop/stream it | **Right on `<?>` and the read-only reasoning.** Bounded `<T>` version was pointless; stream version didn't compile as written. |

Net: **one flat conceptual error** (the `add` wildcard). Everything else was either correct or a design
choice with a fixable justification. That distribution matters for the interview-fear section at the end —
this is not a review full of noise.

---

## 2. Case-by-case — the full ping-pong

### #1 — `add`: consumer takes what it's given; no wildcard on a bare `T`

**Your move:** thought `Box<Number> b; b.add(new Double())` wouldn't compile; proposed `? extends T` on the
item. Noted the syntax "resisted" for a single item vs. a container.

**The correction:**
- `add` is a **consumer** of `T`, not a producer. PECS: `extends` for what you read *out of*, `super` for
  what you write *into*. You write the item in → the wildcard direction would be `super`, never `extends`.
- But you don't want a wildcard on a single-item parameter **at all.** `void add(T item)` is already correct.
- `Box<Number>` means `T = Number`. `add(Double)` compiles because `Double` **is** a `Number` — ordinary
  widening, same as `Number n = Double.valueOf(2.0);`. The imagined failure compiles today.
- The syntax resisting you was the tell: the construct you were reaching for **does not exist** for a bare
  parameter. A plain type parameter already accepts every subtype in an argument slot.

**Conclusion:** wildcards earn their place on **container** parameters with their own type parameter
(`addAll`, `copyFrom`), not on a single `T`. This is the one thing to not memorize wrong.

> Aside: `new Double(...)` is deprecated for removal — use `Double.valueOf(...)`. Doesn't affect the generics
> point.

### #2 — `add` return type: convention vs. return-value honesty

**Your move:** `void` breaks consistency; collections return `boolean`; better to mirror the family.

**The exchange, in order:**
1. First pushback: the `boolean` carries no information here — a `List`-backed box never refuses, so it's
   always `true`. So "consistency" buys a dead value.
2. Your counter: consistency is a feature on its own; a human can't remember every collection's quirks, and
   the `boolean` shape is a learned marker worth following even when its value is dead.
3. The premise-fix (the part worth carrying): collections do **not** uniformly return "was it added."
   - `Collection.add` → `true` if the collection changed.
   - `List.add` → **always `true`**; the boolean is already dead there.
   - `Set.add` → `false` on a duplicate. *This* is the case the boolean exists for.
   - `Queue.add` → `true` or throws; `offer` is the one that returns `false`.
   So the signal is a `Set`/capacity thing that `List` carries as a permanent `true` only to share the
   `Collection` signature. Your `List`-backed `Box` is in `List`'s bucket: always-`true`.
4. Where we landed: your justification shifted — and improved — from "the boolean tells the caller
   something" (false for a `List`-backed box) to "the boolean is a **learned shape**; honoring it saves the
   reader from reloading which collections return what." That second reason **stands.** Convention that
   lowers orientation cost is a real caller-facing feature.

**Conclusion — the rule that survived:**
- Design the return for **truth** when the value informs a decision (a `Set`-like box that can refuse →
  meaningful `boolean`).
- Design it for **familiarity** when the value is dead but the shape is load-bearing (`List`-like box that
  can't refuse → `boolean` as convention, ignored like `List.add`'s).

What we killed was only the reason "the `true` is informative." Keeping `boolean` as a familiarity marker is
legitimate; keeping it because it "tells the caller" is not.

### #3 — `get`: right message, wrong type (for this class), one-sided guard

**Your move:** add a bounds check; put the current size in the message so the caller sees *both* the bad
index and the actual size; tag which method it came from; you argued a bare `IndexOutOfBoundsException`
"gives nothing" and that relying on stack traces is not maintainable.

**The two claims, split:**

- **Message (Claim A): you were right, I was wrong.** A bare index error can't tell you the thing you need —
  *is the index wrong, or is the collection smaller than the caller assumed?* Those are different bugs
  (extraction logic off vs. upstream data missing). Your one-line "index = X, size = Y" localizes the fault
  at throw time, no debugger, no repro — which is exactly the case that bites with external data in
  production. Conceded fully. The rich message earns its place.

- **Type (Claim B): for `Box`, `IllegalStateException` is wrong.** `Box` is a container. Its state is fine,
  the list is fine; the caller handed an `i` that doesn't fit → a bad **argument**, and the JDK subtype for
  that exact fault is `IndexOutOfBoundsException`. `IllegalStateException` would claim "the box is in a bad
  state," which isn't true and reads as a semantic lie to a JDK-literate reviewer.

- **Guard was one-sided.** Original only checked the high end; a negative `i` skips your rich message and
  hits the JDK's plain one — reintroducing the very inconsistency you were arguing against. Widen to
  `i < 0 || i >= size()`.

**Corrected form:**

```java
public T get(int i) {
  if (i < 0 || i >= contents.size()) {
    throw new IndexOutOfBoundsException(
      "get(): index = " + i + " is out of range. Current size = " + contents.size());
  }
  return contents.get(i);
}
```

Keep the rich message, keep the method tag, fix the type and the guard.

### #3 continued — the exception discussion (the important part)

This is where the session stopped being about `Box` and became the thing worth a whole card cluster.

**Your position, stated precisely:** in your apps a bad index/id is almost never a coding bug — it's the
fingerprint of upstream data that didn't arrive or doesn't line up. A state problem wearing an argument's
clothes. So `IllegalStateException` reports the *true cause*, and catching it early beats a generic index
error three layers up. You also clarified: you don't use `IllegalStateException` for "the **object** is in
the wrong state" — you use it for "the **application** / a workflow **step** is in the wrong state."

**Where we converged:**
- For **`Box` specifically** you conceded it: a generic container has no app state and no knowledge of where
  the index came from. It thinks like `ArrayList`. Answer: `IndexOutOfBoundsException`. That was the actual
  `Box` question and it's closed.
- The distinction that resolves the general case is **not** app-vs-library. It's **caller-context-present vs.
  caller-context-absent** — *where the value is judged, not what type it has*:
  - At the **point of lookup / validation**, where you know "this id came from an external feed and the feed
    is inconsistent," you have the context to name a cause. (This is your own null-validation rule: validate
    where the context lives.)
  - Inside a **dumb accessor** like `Box.get`, that context does not exist. The method knows only "argument
    doesn't fit my size." It cannot honestly claim "external state is wrong" because it has no idea whether it
    is → `IndexOutOfBoundsException`.
  - This is why the library answer and the app answer **coincide** for `Box.get`, and why you felt the pull
    to concede it.

**Where I still push (open, not settled):** even at a boundary where you *do* know it's external state,
`IllegalStateException`'s JDK meaning is "this **object** is in the wrong state for this call." A missing
upstream row isn't the box's state being wrong — it's a data precondition unmet. That's often better modeled
as your **own typed unchecked exception** (`MissingRecordException`, the `AppException` family) than as
`IllegalStateException`. Your single-catch-at-the-end architecture doesn't need the JDK type to carry the
signal; a project type lands in the same handler and says what it means. This matches your own exceptions
notes: `IllegalStateException` as a universal wrapper is a small semantic lie; a project base type costs one
class and says the truth.

**Your self-diagnosis (worth keeping verbatim in spirit):** you may overuse `IllegalStateException` because
in your practice the cause really *is* external state / wrong workflow step — never a developer bug. That's a
coherent, consistent meaning. The only gap is that the JDK reserves that name for **object**-state, so a
literate reviewer reads a *narrower* promise than you intend. The fix is one sentence, not a change in how
you think: *"I model workflow-state violations; I've been spelling that with `IllegalStateException`, but a
project type names it without colliding with the JDK's object-state meaning."* Your `Job` state machine
(`IMPORTED / UPLOADED / META_UPDATED`) is literally an application-state model — "wrong state for this step"
is exactly what you mean.

**The card this produces (the reusable idea, not the `Box` instance):**
> The exception type is chosen by **what the throwing site can honestly know about the cause** — not by what
> you suspect the real-world cause usually is. `Box.get` knows only "argument doesn't fit" →
> `IndexOutOfBoundsException`. A validator at the feed boundary knows "row missing from an inconsistent feed"
> → a type that says *that*. Same bad id, two sites, two honest types. Overusing `IllegalStateException` is
> the **boundary type leaking down** into sites that don't have the boundary's knowledge.

### #6 — `sumSizes`: `<?>` right, bounded `<T>` pointless, stream didn't compile

**Your move:** `List<Box<?>>` for "any box," with the reasoning that boxes are read-only here so
`List<Box<Object>>` isn't needed; a bounded `<T>` version for "related boxes only"; and a preference to drop
the method or express it with streams.

**The corrections:**
- `List<Box<?>>` — **right.** Sharpen the justification: `List<Box<Object>>` would reject
  `List<Box<String>>` because generics are **invariant**, so `<?>` is the only element type that accepts a
  mixed list at all. Your "read-only, so no `Object`" instinct points the right way; the precise reason is
  invariance.
- Bounded `<T> ... List<Box<? extends T>>` — **pointless.** `T` is never used and never inferred from
  anything meaningful; callers can't pin it. It reduces to `List<Box<?>>` with ceremony. No "related boxes"
  constraint is actually enforced. Drop it.
- Stream version didn't compile as written:
  - `Stream` has no `sum()` — need the int-specialized stream via `mapToInt`.
  - `contents` is private with no accessor → `Box::contents` doesn't exist.
  - Result is `int`/`long`, not `double` — the `double` return on a count of sizes was always odd, and
    questioning it is fair.

**Compiling form:**

```java
return boxes.stream().mapToInt(b -> b.contents.size()).sum();
```

---

## 3. Fully corrected `Box<T>` (reference)

```java
import java.util.ArrayList;
import java.util.List;

public class Box<T> {

  private List<T> contents = new ArrayList<>();

  public void add(T item) {            // consumer: bare T is correct, no wildcard
    contents.add(item);
  }

  public T get(int i) {
    if (i < 0 || i >= contents.size()) {   // both bounds
      throw new IndexOutOfBoundsException(  // honest type for a container
        "get(): index = " + i + " is out of range. Current size = " + contents.size());
    }
    return contents.get(i);
  }

  public void addAll(List<? extends T> items) {   // PECS producer
    for (T item : items) {
      contents.add(item);
    }
  }

  public void copyFrom(Box<? extends T> other) {  // no raw type, no unchecked cast
    for (T o : other.contents) {
      contents.add(o);
    }
  }

  public static double sumSizes(List<Box<?>> boxes) {  // <?> for any box
    return boxes.stream().mapToInt(b -> b.contents.size()).sum();
    // double return is questionable for a count of sizes — reconsider the signature
  }
}
```

---

## 4. Note — exceptions need a real cluster, not the high-level pass we did

Flagged for a dedicated session. What we have in the deck so far is the **obvious** layer: bad argument →
`IllegalArgumentException`, dev bug → `NullPointerException` / `IndexOutOfBoundsException`, wrap-and-report at
a single handler. That's the surface. This session exposed that the **hard** question is untouched: *which
exception is the right one, and why*, judged by what the throwing site can honestly know.

Topics the cluster must cover (single-axis each, drafted properly later):
- **Type is chosen by site-knowledge, not by suspected real-world cause.** The `Box.get` vs. feed-boundary
  contrast is the worked example.
- **`IllegalStateException`: JDK object-state meaning vs. your application/workflow-state usage.** Name the
  collision; decide when a project type (`AppException` family) says it better. Tie to the `Job` state
  machine.
- **`IllegalArgumentException` vs. `IllegalStateException`** — your "with context vs. without context"
  framing — reconciled with the site-knowledge rule above (they're the same idea from two angles).
- **When the JDK subtype is mandatory** (`IndexOutOfBoundsException`, `NullPointerException`) vs. when a
  project type is the honest choice.
- **The message is load-bearing** — index + size, method tag, cause preserved — reusing the
  `source`/`stage`/`describe()` pattern from the streams `Result` work.
- **Boundary type leaking down** — the failure mode where a boundary-only type (`IllegalStateException`)
  gets used in dumb accessors that lack the boundary's context.

Cross-link to the existing exceptions notes: the "unchecked but **typed**" convergence (Hibernate/Spring
kept subtypes), and "`IllegalStateException` as a universal wrapper is a semantic lie."

---

## 5. Interview framing — the fear about over-producing

**The fear:** producing a lot of nuanced reasoning in a review, where a high-level interviewer sees strong
thinking but a short-conclusion interviewer stamps it "wrong / false" and moves on.

**What actually happened here, against that fear:** one flat error (`add` wildcard). Everything else was
right or a defensible choice with a repairable justification. That's not a noisy reviewer — that's sound
reasoning whose *labels* sometimes don't match the JDK's. An interviewer worth passing for distinguishes
those. You are not over-producing noise.

**The lever is order, not volume.** Lead with the crisp canonical answer; offer the depth as an optional
second move:

> "For `Box.get` — `IndexOutOfBoundsException`. It's a bad index; the container has no state to be wrong.
> *In app code at a data boundary I'd think harder about whether a missing id is really a state problem — but
> that's a different site.*"

First sentence satisfies the short-conclusion interviewer completely (right answer, fast, no hedging). Second
sentence is bait the deep interviewer takes and the shallow one ignores. Un-fail-able by either — and you
produced *no less* thinking, you just sequenced it. This is your own card rule turned on your speech:
concrete mechanical fact first, general principle as the closing handle.

**The reassurance underneath:** an interviewer who hears "here's the standard answer, and here's the one case
I'd question it" and still concludes "он думает, значит может ошибаться, значит нет" would have failed you for
anything short of parroting. You can't design an answer that survives someone who penalizes thinking. You
*can* make sure the canonical answer is never absent — a sequencing fix, not a think-less fix. Don't sand
down the thing that's actually your strength.

---

## 6. One-line takeaways to test cold in a month

1. Consumer parameter takes a bare `T` — **no wildcard**; `extends`/`super` are for container parameters and
   for what you read out vs. write in. Syntax resisting a wildcard on a single item = the construct doesn't
   exist.
2. `boolean` on `add` is a **familiarity marker**, not an informative value on a `List`-backed box — keep it
   for the shape if you keep it, never for "it tells the caller."
3. `get` — rich message (index + size + method tag) is worth it; type for a **container** is
   `IndexOutOfBoundsException`; guard **both** bounds.
4. Exception type = **what the site can honestly know about the cause**, not the cause you suspect. Boundary
   knowledge doesn't exist inside a dumb accessor.
5. `<?>` accepts a mixed list because generics are **invariant**; a bounded `<T>` that's never inferred is
   ceremony.
6. `Stream` has no `sum()` → `mapToInt(...).sum()`; a private field has no method reference.
7. Interview: **canonical answer first, nuance as optional second move.** Order, not volume.
