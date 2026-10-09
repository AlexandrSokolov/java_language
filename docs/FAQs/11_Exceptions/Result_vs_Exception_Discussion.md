# Result vs Exception — a discussion document

> This is a **thinking document**, not a deck. Prose, not cards. It captures where the discussion landed so we can
> pick it up again with a fresh mind. Candidate card stems are collected at the very end — those are the only
> question-shaped things here. Everything above them is us reasoning, not final answers.
>
> **Two things below are marked `[UNVERIFIED]`.** They are the exact name of the fold-with-failure structure, and
> the precise Cats/Vavr type names. They were not web-checked in this session. Do not memorise them until verified.

---

## 0. The one-line problem

A method can either **throw** on failure or **return** a value that wraps success-or-failure. Both are defensible.
The whole topic is: *when is each the right tool, what does each cost, and — if we pick "return a result" as the
default — what must we build so that using it doesn't collapse into hand-checking every call?*

---

## 1. The historical flow (the frame the whole topic hangs off)

This is not a straight line where each stage beats the last. It is a **pendulum**. Each swing fixes the previous
stage's pain and gives back something the previous stage had.

**Stage 1 — error codes (return an int / status).**
The earliest style. A function returns a code; the caller inspects it. The pain: **the caller must handle the
result at the call site, every time.** Five calls means five checks. And you can only inspect it in the *direct*
caller — the outcome cannot travel up to a frame higher in the stack on its own. If you want it handled three
frames up, every frame in between must pass the code up by hand.

**Stage 2 — exceptions.**
Invented to cure exactly that. The one gift: **the failure travels up the stack on its own, and you choose the
frame where you catch it.** The direct caller, or ten frames up — your choice. Every frame in between stays blind
and clean; it never mentions the failure. This is the freedom error codes lacked.
The rule that came with it: *don't use exceptions for ordinary control flow* — they are for the exceptional case,
not for loops and branches.

**Stage 3 — return a Result (wrap error + success, even for void).**
After years of experience the field swung back. Two lessons drove it:
- exceptions used for flow control are a known defect (stage 2's own rule), **and**
- even used correctly, an exception is an invisible side channel — a throw can silently fly past a frame that
  *should* have handled it, and you don't find out until it lands somewhere wrong or crashes.

So the answer became: put the outcome back **in the return value** — a `Result` carrying either the value or the
failure. The failure is now visible in the signature; it cannot be silently skipped.

**The catch nobody says out loud:** stage 3 **re-inherited stage 1's problem.** A Result is a value; a value has to
be handled where it sits. It does not travel up on its own. So we are back to "the caller must deal with it here" —
the exact thing exceptions were invented to escape. Result did not defeat exceptions. It traded *back* the
travel-up freedom to buy *back* the visibility.

**The shape to remember:** each stage trades the **same two properties** in opposite directions —
*"the caller must handle it here"* vs *"the caller can handle it wherever it wants, middle stays clean."*
- error codes: handle here, no travel.
- exceptions: travel freely, middle blind — but the failure is invisible and skippable.
- Result: visible and unskippable — but handle here, no free travel.

---

## 2. Benefits and restrictions of each (side by side)

**Exceptions — benefit.**
- Travel up the stack alone; catch at the frame you choose.
- Intermediate frames don't mention the failure — signatures stay clean.
- Free — built into the language, nothing to construct.
- Java's *only* built-in way to thread a fail-fast sequence without writing the plumbing. In this one respect an
  exception is Java's missing `for`-comprehension: throw at each step, one `catch` around the block, the happy path
  reads as straight-line calls.

**Exceptions — restriction.**
- Invisible side channel: a throw can skip a frame that should have handled it; you learn at runtime, not compile
  time.
- Checked exceptions leak into the API and freeze the owner (see the exceptions deck — separate axis).
- Break streams and lambdas outright.
- Encourage "recovery" that is really a mechanical rethrow.

**Result — benefit.**
- The failure is a **value**: visible in the signature, impossible to skip silently.
- Because it's a value, you can **collect, filter, partition, and gather many failures** — a batch reports every
  error, not just the first. An exception can only carry the first.
- Composes with streams cleanly (partition by outcome, map the two sides).
- No exceptions-for-control-flow.

**Result — restriction (the heart of the discussion).**
- It is a value, so it must be handled **where it sits**. It does not travel up on its own.
- To handle it N frames up, **every method on the return path must carry the Result up** — each intermediate method
  now has a `Result` return type it wouldn't otherwise need. (Note the precise version in §4 — it's the *methods on
  the return path*, not the *called methods*.)
- It is **achievable, not free**: you must *build* the structure (the type, the chaining `map`, the throwing-
  function adapter, the message/describe accessors) before Result is pleasant to use.
- Its clean chain is one-in-one-out only. A step needing several prior results at once has no clean built-in form
  (see §5).

---

## 3. When each wins — the markers (the interview answer)

The naive answer is "exception vs Result, pick a side." The real answer is a set of markers. Two matter most.

### Marker A — distance between where the failure happens and where it's handled

This is the sharpest one, and it's a direct consequence of §1's travel property.

- **Handler is near the failure** (direct caller, or a shallow chain) → **Result.** Its forced locality is a
  *feature* here: the failure can't be silently skipped, threading it is trivial, there's no deep middle to
  pollute. You bought safety and paid almost nothing.
- **Handler is far, with a deep middle** (failure born at the bottom, handled at the top, many frames between that
  don't care) → **throw.** Result would force all those middle frames to carry a `Result` in their signatures for
  a failure they have nothing to do with — that is stage 1's error-code disease returning. The exception's
  travel-up freedom is worth more than the visibility here.

So the badness of losing "catch anywhere" is a **function of distance**. Near → the loss is actually a safety gain.
Far + deep middle → the loss is real and the throw wins.

### Marker B — independent vs dependent, and combine-many

Straight from the existing deck card *"Why does Scala's for-loop stop at first error?"*.

- **Independent checks you want to accumulate** (validate every field, report all errors) → **Result, always.** An
  exception carries only the first failure; a Result is a value you can gather. This is the applicative /
  fail-slow case.
- **Dependent sequential steps** where step 2 needs step 1's unwrapped value → fail-fast is forced by the logic.
  In Scala `flatMap` hides the threading. In plain Java with no structure, Result forces a manual unwrap-or-bail
  ladder — here a `throws` + single `catch` reads better *unless you have built the chaining structure* (§4).

### The honest summary of the markers

Once you have built the chaining structure (§4), "no structure exists" stops being a reason to throw — structure is
always *achievable*. What survives is:
- **distance** (Marker A) — the real, permanent reason to prefer a throw, and
- **the arity wall** (§5) — where the built structure itself runs out.

Everything else ("Result is more modern," "exceptions are old") is noise.

---

## 4. The structure requirement — what you must BUILD to make Result pleasant

Result is only smooth when a structure exists to **thread the value through so you never hand-check a return.**
Streams and Scala's `for` feel effortless not because of the Result type but because *they are that structure.* In
plain sequential Java you must supply it yourself — by putting the threading **inside the type.**

### The chaining `map` is the structure

The user's own `Result` already is it. `map` is the `flatMap`-equivalent: thread the value forward, short-circuit
on the first failure, catch a thrown exception at the boundary and fold it into the Result.

```java
public record Result<T>(T value, Exception error, Supplier<String> context) {

  public static <T> Result<T> start(T value, Supplier<String> context) {
    return new Result<>(value, null, context);
  }

  public boolean isOk() { return error == null; }

  public <R> Result<R> map(ThrowingFunction<T, R> f) {
    if (error != null) return new Result<>(null, error, context);   // already failed — skip the rest
    try {
      return new Result<>(f.apply(value), null, context);
    } catch (Exception e) {
      return new Result<>(null, e, context);                        // checked exception dies here, at the boundary
    }
  }

  public String message()  { /* context + root-cause message, or a support line for bugs */  ... }
  public String describe() { /* context + throw site of every link in the cause chain */      ... }
}
```

The clever part: `map` catches `Exception` and stuffs it in the Result, so a checked `IOException` from a leaf call
**never appears in any signature** — the whole "wrap checked into one neutral thing at the boundary" convention,
done automatically by the structure. `[UNVERIFIED: confirm ThrowingFunction declares `throws Exception` so the
catch actually covers checked types — need the ThrowingFunction source to be sure.]`

### It works with OR without a stream — the stream is just one place to drop the chain

In a stream (the user's real example):

```java
Map<Boolean, List<Result<Integer>>> byOutcome = jobs.stream()
    .map(job -> Result.start(job.path(), () -> "tenant " + job.tenantId() + " nightly run")
        .map(this::readFile)     // step 1 — may throw checked IOException
        .map(this::parseJson))   // step 2 — skipped if step 1 failed
    .collect(Collectors.partitioningBy(Result::isOk));

List<Integer> ok            = byOutcome.get(true ).stream().map(Result::value)  .toList();
List<String>  failForCust   = byOutcome.get(false).stream().map(Result::message).toList();  // for customer/queue
List<String>  failForDev    = byOutcome.get(false).stream().map(Result::describe).toList();  // for developer
```

Standalone, for the five-dependent-calls case that has no pipeline:

```java
Result<Report> r = Result.start(path, () -> "tenant " + id + " nightly run")
    .map(this::readFile)
    .map(this::parseJson)
    .map(this::validate)
    .map(this::transform)
    .map(this::buildReport);

if (r.isOk()) use(r.value());
else          log(r.describe());
```

Five dependent calls, each may throw, **no `try` between them, one check at the end.** The manual ladder is gone.
This is the answer to "what structure lets me avoid exceptions without a stream": **the Result's own `map` is the
structure.** Java has no `for`-comprehension, so you build the threading into the type and every call site gets it
for free.

### Turning consumers into functions

A step that is really a side-effecting *consumer* (no useful return) can still ride the chain: make it a function
explicitly by returning the input (or a marker) so the next stage has something to receive. You do this by hand —
put the required data in, return what the next call needs.

---

## 5. The restriction of THIS structure — the arity wall

The chain is clean because each step is **one-in, one-out**: the Result carries a single value, `map` hands that
one value to the next step. "One value" is not a real limit while every step needs only the *immediately previous*
value.

**It breaks the moment a step needs two or more earlier results at once** — step 4 needs both step 1's and step 2's
outputs, not just step 3's. The single carried value can't hold both. Two escape routes, both the manual work we
were trying to avoid:

- **Carry a tuple/record forward.** Widen the payload to a record holding everything downstream might need; each
  step reads its fields. The type grows with the pipeline; each step maps into and out of it by hand.
- **Nest.** Compute result 1, then *inside* its success compute result 2, then compute step 4 from both. This is
  `flatMap` nesting and it staircases — the exact ladder we escaped.

This is a **fundamental structural limit, not a gap in the implementation.** Proof: Scala/Cats has a *separate*
tool for precisely this. `flatMap`/`map` handle one-in-one-out sequential; to combine several independent results
into one step you switch to the **applicative** (`mapN` / `tupled`) `[UNVERIFIED: exact Cats method names]`. The
user's `Result.map` is the `flatMap` half and has **no applicative half** — so the multi-argument combine is what
it structurally cannot express cleanly, and Java gives nothing ergonomic to build it from. This is the same split
the deck already names in *"Why does Scala's for-loop stop at first error?"* — monad = dependent/sequential,
applicative = independent/combine-many — now seen from the Java side.

---

## 6. The user's solution to the arity wall — generalize the stage

The insight: the arity problem was never real. It came from `map` **fusing three jobs and hard-coding the third.**
`map` does: (1) pull the value out, (2) call the function, (3) put the function's result back as the new carried
value. Job 3 is a *decision* — "the next value is the function's output" — and `map` freezes it. Unfreeze it and
the wall goes.

Generalize a stage into three explicit parts:

- **Stage input** — one value, but it may be a record/tuple, so "one" is not a restriction. This is the
  **accumulator.**
- **Extract + call** — a function `stageInput -> functionResult` that itself decides which fields to pull out of
  the record and how to feed a multi-argument method. The multi-arg mess is hidden *inside* this function; from the
  pipeline's view the stage is still one-in-one-out. **(user's task 1)**
- **Merge** — a `BiFunction<StageInput, FunctionResult, NextStageInput>` deciding what the next stage carries: the
  function's output alone, the old input, or the two merged into a bigger record. **(user's task 2)**

```java
// Sketch — not final. The generalized stage: extract args, call (may throw), merge into the next stage input.
public <A, R, N> Result<N> step(
    Function<T, A> extract,          // pull args out of the stage input          (task 1)
    ThrowingFunction<A, R> f,        // call the real method — may throw
    BiFunction<T, R, N> merge) {     // build the next stage input                (task 2)
  if (error != null) return new Result<>(null, error, context);
  try {
    A args = extract.apply(value);
    R out  = f.apply(args);
    return new Result<>(merge.apply(value, out), null, context);
  } catch (Exception e) {
    return new Result<>(null, e, context);
  }
}
```

`map` is now just the special case `extract = identity`, `merge = (input, output) -> output`.

**Why this is exactly the collector shape.** `collect(supplier, accumulator, combiner)` and
`reduce(identity, accumulator, combiner)` work because they don't hard-code "the result is the function's output" —
they take a **combiner** that says how to fold a new piece into the running state. The generalized `step` gives
the fallible chain the same knob: a merge (combiner) chosen per stage, with the stage input as the accumulator.
The multi-argument method call lives inside `extract`/`f`; the pipeline never holds more than one value because the
**merge decides what one value survives.** The arity problem became an accumulator problem, and the accumulator is
the stage input.

**What this is, named:** a state-threading fold with a failure short-circuit — a `foldLeft` over stages where the
state is the record and any stage may fail. `[UNVERIFIED: confirm the standard name — "fold with a failure/error
channel" / relation to Cats' Writer/State/EitherT — before putting a label in a card.]`

---

## 7. Restrictions of the user's solution (all of them)

1. **It does not replace `map` — it sits beside it.** For a plain one-in-one-out chain, `extract` and `merge` are
   pure overhead; `map` is better there. Same as the collector overloads: the three-arg `reduce` exists but you
   reach for the simple form when you don't need the combiner. So the type grows *two tiers* — `map` for the common
   case, `step` for the multi-input case — and you must know which to reach for.
2. **Per-stage boilerplate at the multi-input stages.** Every generalized stage needs a hand-written `extract` and
   `merge`. You've moved the complexity out of the pipeline's *shape* and into the *lambdas you pass* — cleaner,
   but not free.
3. **The accumulator record grows.** Carrying everything downstream might need means a widening record threaded
   through the whole chain. Long pipelines get a fat accumulator, and each stage must know its field names.
4. **Still no fail-slow accumulation from this shape.** `step` is a *sequential* fold — it short-circuits on the
   first failure like `flatMap`. It does **not** gather multiple independent failures into one report; that is the
   applicative job (Marker B / §5), a different combine. If you need "report every error," `step` is the wrong tool
   and you're back to the applicative gap.
5. **Hand-rolled arities if you go the `map2`/`map3` route instead.** You *could* add `map2(Result, Result,
   BiFunction)`, `map3(...)`, etc. — that is literally how Cats builds its applicative — but each arity is a
   separate method written by hand; there is **no variadic clean form in Java.** This is why nobody does it past a
   point. `[UNVERIFIED: confirm this is how Cats' applicative is assembled under the hood.]`
6. **It's infrastructure you own.** Every line of `Result`, `ThrowingFunction`, `step`, `message`, `describe` is
   code you write, test, and maintain. Exceptions are free in the language. The whole approach is a bet that the
   visibility + collectability is worth carrying a small framework.

---

## 8. Where to go to avoid these restrictions — the libraries

The honest end of the road: **everything above is rebuilding, by hand, what functional libraries already ship.**
Reaching the arity wall and the applicative gap by yourself is the strongest evidence that these libraries exist
for real reasons, not fashion.

- **Vavr** — the pragmatic Java choice. `Either<L, R>` for success/failure as a value; `Validation<Seq<E>, T>` for
  the fail-slow, accumulate-every-error case (the direct analog of the Scala/Cats accumulating type). Already noted
  in the deck's validation card. Vavr is *the* answer to §7's applicative gap in Java without leaving Java.
  `[UNVERIFIED: exact Vavr type/method names — Either, Validation, combine/ap — confirm before a card.]`
- **Cats (Scala)** — the reference model, and the user's favourite (admittedly not applied in his own Java work).
  `Either` + `flatMap` for sequential fail-fast; `Validated` / `ValidatedNel` + `mapN` for fail-slow accumulation;
  `for`-comprehension sugar to hide the threading the user builds by hand; the applicative/monad split the whole
  discussion keeps rediscovering. `EitherT` / `Writer` / `State` are the names near the "state-threading fold with
  a failure channel" of §6. `[UNVERIFIED: all Cats type names above — confirm before any card.]`
- **Language-level escape hatches elsewhere (for interview defence).** Rust's `?` operator and Kotlin's `Result` +
  `getOrElse` are the built-in versions of exactly the threading the user hand-builds. The honest line: *yes, other
  languages solve this at the language level; Java lacks that operator, which is why in Java the exception is still
  the better tool for the deep-distance case.* `[UNVERIFIED: Kotlin Result API specifics.]`

**The takeaway for the deck:** the user's structure is the right *understanding* — building it is how you learn why
the libraries are shaped the way they are — but in production the answer to §7's restrictions is Vavr (in Java) or
Cats (in Scala), not a growing home-grown framework. Home-grown is worth it only for a small, closed chain where a
dependency isn't justified.

---

## 9. Candidate card stems — the whole "return vs exception" topic

> Questions only. No answers here by design — we discuss and draft later, probably over several passes. Each stem
> is a single axis; several are deliberately narrow so they don't fuse. Grouped by cluster; a chapter router should
> sit on top once the cluster lands. Stems are provisional and will get the 5-variations treatment when we draft.

**Cluster: the history / the frame**
- Error codes → exceptions → what pendulum?
- What did exceptions give that error codes couldn't?
- Why did the field swing back to returning a result?
- What problem did returning a result bring back?

**Cluster: the trade (benefits / restrictions)**
- What does a thrown failure let you skip?
- Why is a returned failure impossible to ignore?
- What can a returned failure do that a thrown one can't? (gather many)
- What is a thrown failure's hidden danger?

**Cluster: the markers (when each wins)**
- Result or throw — what decides it? (router)
- How does handler distance pick throw vs result?
- When is losing "catch anywhere" actually a gain?
- Independent vs dependent failures — which tool?
- Why is throwing Java's missing for-comprehension?

**Cluster: the structure you build**
- What makes a returned-result chain not need manual checks?
- Where does a checked exception die in the chain?
- Does the result chain need a stream?
- How does a consumer join a result chain? (return input / marker)

**Cluster: the structure's limit + the fix**
- When does the result chain break? (arity wall)
- Why can't map thread two prior results?
- What three jobs does map fuse?
- How does a per-stage merge remove the arity wall?
- Why is the generalized stage the collector shape?
- What does the generalized stage NOT solve? (fail-slow)

**Cluster: the libraries**
- What does Vavr Validation do that flatMap can't?
- Monad vs applicative — which accumulates?
- What does Rust's ? operator replace?
- When is home-grown Result worth it over Vavr?

---

## 10. Open items to resolve before any of this enters the deck

- **Verify every `[UNVERIFIED]`** above (fold-with-failure name; Cats `Validated`/`ValidatedNel`/`mapN`/`EitherT`
  names; Vavr `Either`/`Validation` API; Kotlin `Result`; that `map2/map3` is how Cats assembles its applicative;
  that `ThrowingFunction` declares `throws Exception`). Web-check before writing, per the deck's volatile-facts
  rule.
- **Decide the split.** This is 4–5 axes: history, the trade, the markers, the structure + limit + fix, the
  libraries. Almost certainly a multi-card cluster with a chapter router, not one card — revisit when we draft.
- **Cross-link** to the existing cards: *"Why does Scala's for-loop stop at first error?"* (the monad/applicative
  split this keeps hitting), the validation-collecting cards, and the streams `Result` cards (`source`/`stage`/
  `describe()` design is the same `context`/`message`/`describe` shown here).
- **Fit against the exceptions deck.** The checked-exception encapsulation axis (owner frozen across time) and this
  Result axis (handler frozen across stack distance) are siblings — "a failure leaking into places that would
  rather not know." Worth a lateral link once both land.
