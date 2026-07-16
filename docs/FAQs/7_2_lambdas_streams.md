## Lambdas and Streams - Streams

### Why were streams added to Java?
<details><summary>Show answer</summary>

To make bulk work over sequences simpler to express: 
- describe *what* the computation is as a chain of stages, instead of hand-writing the loop that walks the elements. 
- The same description also runs in parallel without being rewritten. 
- Sources are deliberately unrestricted — collections, arrays, files, matchers, random generators, other streams — 
  so one style covers any finite or infinite sequence.

</details>

### What parts make up a stream pipeline?
<details><summary>Show answer</summary>

Three kinds of stage, in a fixed order: one source, then zero or more intermediate operations, then exactly one
terminal operation.

- **Source** — produces the elements.
- **Intermediate** — takes a stream, returns a stream. Element type may change (map) or stay (filter). Since the
  output is again a stream, they chain freely.
- **Terminal** — takes the last stream and produces something that is *not* a stream: a collection, a single
  element, a side effect like printing. It ends the chain.

Trigger: only the terminal stage leaves stream-land, so the shape is forced — one entry, N same-type links, one exit.

</details>

### When is a pipeline evaluated?
<details><summary>Show answer</summary>

Stream pipelines are evaluated lazily: evaluation doesn’t start until the terminal operation is invoked. 

</details>

### How can a stream be infinite?
<details><summary>Show answer</summary>

Lazy evaluation makes infinite sources usable. Elements are pulled on demand, not produced up front: the terminal
operation asks for exactly as many as it needs, and anything beyond that is never computed. A source that could go on
forever is fine as long as some stage stops asking (`limit`, `findFirst`, a short-circuiting match).

```java
Stream.iterate(1, i -> i + 1)   // endless source: 1, 2, 3, ...
      .map(i -> i * i)          // records the step, computes nothing yet
      .limit(5)                 // stops asking after 5 elements
      .forEach(System.out::println);   // terminal: pulls, so work starts here
// prints 1 4 9 16 25 — i = 6 is never generated
```

Without `limit`, `forEach` would keep asking and the program would run forever — laziness removes the *up-front* cost, 
not the need for someone to stop the pull.

</details>

### When do streams stop paying off?
<details><summary>Show answer</summary>

Streams can express almost any computation, so "possible" is never the test — readability is. There is no rule, only
signs that a pipeline has gone too far. Concrete signs to look for:

- **A lambda body grows past one expression** — it needs a block, a local variable, or a comment to be understood.
- **You cannot name the intermediate values.** A pipeline forces `a -> b -> c` with no place to say what `b` *is*;
  a loop gives it a variable name.
- **The same value is needed twice at different stages** — a stream has to recompute it or stuff it into a temporary
  pair/record just to carry it along.
- **The lambda needs to `break`, `continue`, `return` from the enclosing method, or throw a checked exception** —
  none are possible in a lambda, so you end up faking them.
- **The lambda mutates or reads a local variable** — impossible (must be effectively final), so the code gets bent
  around the limitation.

Rule of thumb: use streams for the part that *is* a transformation, and keep the rest as plain code.

```java
// Overused: one pipeline does read, group, filter, sort, print — and nothing has a name.
Files.lines(dictionary)
  .collect(groupingBy(w -> Stream.of(w.split(""))
        .sorted().collect(joining())))
        .values().stream()
  .filter(g -> g.size() >= minGroupSize)
  .map(g -> g.size() + ": " + g)
  .forEach(System.out::println);

// Happy medium: streams do the grouping; the helper gets a name; the loop stays a loop.
Map<String, List<String>> groups;
try (Stream<String> words = Files.lines(dictionary)) {
    groups = words.collect(groupingBy(Anagrams::alphabetize));   // named, so the key's meaning is visible
}
groups.values().stream()
      .filter(group -> group.size() >= minGroupSize)             // one expression per stage
      .forEach(group -> System.out.println(group.size() + ": " + group));
```

</details>

### How to make a pipeline readable?
<details><summary>Show answer</summary>

Iterative code carries two free labels a pipeline does not have: **explicit types** on declarations and **named
temporary variables**. In a chain of `a -> b -> c` nothing states what `b` is or what the step means — the only
place a name can live is the method being called.

So the helper method is not cosmetic. Extracting a lambda body into a named method puts the missing label back:
`groupingBy(Anagrams::alphabetize)` says what the key is; the inline version says only *how* it is computed and
leaves the reader to infer *what* it is.

**The comment is the honest signal.** If a stage needs a line of prose to explain what it does, the code isn't
saying it. The fix is usually not "go back to a loop" — it's extract the lambda body into a named method. The name
is the comment, and it can't drift out of date.

```java
// Needs a comment: reader must decode the how before seeing the what.
.collect(groupingBy(w -> Stream.of(w.split("")).sorted().collect(joining())))   // sort letters = anagram key

// Named: the what is stated, the how moves out of the main program.
.collect(groupingBy(Anagrams::alphabetize))
```

</details>

### Loop or pipeline — how to choose?
<details><summary>Show answer</summary>

One axis: **what the computation needs to touch.** A code block sits inside the enclosing method and can reach its
context; a function object is a value passed elsewhere and cannot.

| Needs to…                                     | Code block | Lambda                         |
|-----------------------------------------------|------------|--------------------------------|
| read a local variable in scope                | any        | only final / effectively final |
| modify a local variable                       | yes        | no                             |
| return from the enclosing method              | yes        | no                             |
| break / continue an enclosing loop            | yes        | no                             |
| throw a checked exception the method declares | yes        | no                             |

If the computation is best expressed with any of those, it fits a loop, not a pipeline.

The other side of the axis — what a pipeline does with almost no code:

- transform a sequence uniformly
- filter a sequence
- combine a sequence with one operation (sum, concat, min)
- accumulate into a collection, possibly grouped by an attribute
- search for the first element matching a criterion

Trigger: **loops own the enclosing method's state and control flow; streams own the sequence.** Every row above falls
out of that one line.

</details>

### What if two stages need the same value?
<details><summary>Show answer</summary>

`map` destroys its input: after `p -> f(p)` only `f(p)` moves downstream, so a later stage cannot see `p`.

**1. Carry a record — the default.** Map to a small object holding both values. Always works, and reads well: one
line to declare, named fields, `equals`/`hashCode`/`toString` free. `s.score()` at a later stage is as clear as a
local variable.

There is an opinion that carrying a tuple is messy and verbose — that comes from `Map.Entry` and hand-rolled `Pair`
classes: anonymous fields (`getKey`, `getValue`) that say nothing at the call site, and a class to maintain. A
`record` removes that cost entirely. Use one; do not treat it as a compromise.

```java
record Scored(Item item, double score) {}
items.stream()
     .map(i -> new Scored(i, expensiveScore(i)))   // computed once
     .filter(s -> s.score() > threshold)           // named field, not getValue()
     .forEach(s -> log(s.item(), s.score()));
```

**2. Invert the mapping — rare.** Recompute the old value from the new one where you need it. Needs the mapping to be
reversible and cheap to reverse, which usually takes a mathematical identity or an encode/decode pair. Most real
mappings (`word -> length`, `entity -> dto`) are lossy, so this applies narrowly.

```java
// 2^p - 1 in binary is exactly p ones (p=5 -> 31 -> 11111), so the exponent is recoverable from the result.
primes().map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
        .filter(mersenne -> mersenne.isProbablePrime(50))
        .limit(20)
        .forEach(mp -> System.out.println(mp.bitLength() + ": " + mp));   // bitLength() inverts the map
```

</details>

### Stopping a pipeline early?
<details><summary>Show answer</summary>

No direct equivalent of `break` exists. A lambda cannot break an enclosing loop or return from the enclosing method
— it is a function object called by the stream, not code inside your method. There is no statement that says "abandon
the whole pipeline now."

What exists instead: **short-circuiting operations** that stop the pull.

- `limit(n)` — stop after n elements pass this point.
- `takeWhile(pred)` — stop at the first element that fails the test (Java 9+). This is the real `break` analogue.
- `findFirst()` / `findAny()` — terminal, stops at the first hit.
- `anyMatch` / `allMatch` / `noneMatch` — terminal, stops as soon as the answer is decided.

They only work at fixed points in the chain, and only on the element being pulled. If you need to stop based on
accumulated state (`total > budget`), a stream cannot express it — use a loop.

```java
// Loop: break on the first negative.
for (int n : numbers) {
    if (n < 0) break;
    System.out.println(n);
}

// Pipeline: takeWhile stops the pull at the same point.
numbers.stream()
       .takeWhile(n -> n >= 0)
       .forEach(System.out::println);

// No equivalent: the condition depends on state accumulated across elements.
int total = 0;
for (Order o : orders) {
    total += o.amount();
    if (total > budget) break;      // a lambda cannot do this
    process(o);
}
```

</details>

### Skipping one element mid-pipeline?
<details><summary>Show answer</summary>

No `continue` statement either — but this one needs no workaround, because it is what `filter` already is.

`continue` in a loop means: do not run the rest of the body for this element, move to the next. In a pipeline the
"rest of the body" is the downstream stages, and `filter` is the gate that decides whether an element reaches them.
Place the filter where the `continue` sat.

If the test needs a value computed earlier in the body, compute it in a `map` first, then filter on it — or filter on
a derived expression directly.

```java
// Loop: skip blanks, process the rest.
for (String line : lines) {
    if (line.isBlank()) continue;
    process(line.trim());
}

// Pipeline: the filter sits exactly where continue sat.
lines.stream()
     .filter(line -> !line.isBlank())
     .map(String::trim)
     .forEach(this::process);

// Skipping based on a derived value: derive first, then gate.
lines.stream()
     .map(this::parse)
     .filter(record -> record.isValid())    // invalid ones never reach the stages below
     .forEach(this::store);
```

Unlike `break`, nothing is lost here: `filter` expresses `continue` completely.

</details>

### Lambda throws — what happens?
<details><summary>Show answer</summary>

**Unchecked exception:** it propagates out of the terminal operation and up through your method as normal. The
pipeline stops where it stood; already-produced side effects stay. Nothing special about streams here.

**Checked exception:** it does not compile. `Function`, `Predicate`, `Consumer` and friends declare no `throws`
clause, so a lambda body cannot throw a checked exception at all — even if the enclosing method declares it. The
compiler rejects the lambda, not the pipeline.

The naive fix is a `try` block inside every lambda, which destroys readability. The reusable fix is a **wrapper
function** that adapts a throwing lambda into the normal functional interface:

```java
@FunctionalInterface
interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;                 // the shape a normal Function cannot have
}

static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R> f) {
    return t -> {
        try {
            return f.apply(t);
        } catch (Exception e) {
            throw new RuntimeException(e);         // wrap: the checked type is now unchecked
        }
    };
}

// Use: Files::readString throws IOException, so it cannot be passed to map directly.
paths.stream()
     .map(unchecked(Files::readString))            // compiles; IOException surfaces wrapped at runtime
     .forEach(System.out::println);
```

Write one wrapper per interface you need (`unchecked` for `Function`, another for `Consumer`, `Predicate`, `Supplier`
— the shapes differ, so they cannot be one method).

Cost: the original checked type is gone from the signature; callers can no longer catch `IOException` without
unwrapping the cause. That trade is why a loop with a real `try/catch` is often the better answer when the exception
is something you actually intend to handle rather than propagate.

</details>

### What governs exceptions in stream pipelines?
<details><summary>Show answer</summary>

Two independent problems, often confused:

- **Compile time** — a lambda cannot throw a checked exception at all. `Function`, `Predicate`, `Consumer` declare no
  `throws`, so the body won't compile even if the enclosing method declares the type.
- **Runtime** — any exception that escapes a lambda aborts the terminal operation. It does not skip the element; it
  stops the pull, so a `collect` yields nothing and a partial `forEach` leaves side effects behind.

A stream has **no error channel**. If you want element-level failure to be survivable, the failure must become part
of the element type. Everything below is a way to do that.

- [How to make a failure a value?](#how-to-make-a-failure-a-value) — the `Result` type, single stage.
- [Why does Result force a split?](#why-does-result-force-a-split) — multiple failing stages, plain `Result`.
- [Discarding failed elements mid-pipeline?](#discarding-failed-elements-mid-pipeline) — `flatMap`, and why it's weak.
- [Chaining several fallible steps?](#chaining-several-fallible-steps) — `Result` with its own `map`.
- [Passing a throwing lambda to map?](#passing-a-throwing-lambda-to-map) — the compile-time problem.

Trigger: **the pipeline has one channel; a failure must ride in it or it kills the pipeline.**

</details>

### How to make a failure a value?
<details><summary>Show answer</summary>

Wrap the outcome in a type that holds either a value or an error, and a helper that converts a throwing lambda into
one. The lambda then never throws, so the terminal always completes and both groups survive.

```java
record Result<T>(T value, Exception error) {
  static <T> Result<T> ok(T v)           { return new Result<>(v, null); }
  static <T> Result<T> fail(Exception e) { return new Result<>(null, e); }
  boolean isOk() { return error == null; }
}

@FunctionalInterface
interface ThrowingFunction<T, R> { R apply(T t) throws Exception; }

static <T, R> Function<T, Result<R>> attempt(ThrowingFunction<T, R> f) {
  return t -> {
    try { return Result.ok(f.apply(t)); }
    catch (Exception e) { return Result.fail(e); }
  };
}

Map<Boolean, List<Result<String>>> byOutcome = paths.stream()
    .map(attempt(Files::readString))         // never throws — failure is a value now
    .collect(partitioningBy(Result::isOk));  // terminal completes

byOutcome.get(true).forEach(r -> process(r.value()));
byOutcome.get(false).forEach(r -> report(r.error()));  // real feedback, not a log line
```

Strength: nothing is silently lost. Both outcomes are data you can report on, count, retry, or fail the batch over.
For a **single** fallible stage this is clean and needs nothing else.

</details>

### Why does Result force a split?
<details><summary>Show answer</summary>

Because the element type changes and every downstream stage sees it. After one `attempt`, the stream is
`Stream<Result<String>>`, not `Stream<String>`:

```java
paths.stream()
    .map(attempt(Files::readString))  // Stream<Result<String>>
    .map(String::toUpperCase)         // does not compile — the element is Result, not String
```

The stream's `map` has no notion of "skip the failed ones and keep going". Each later stage must unwrap and re-wrap
by hand — noise repeated at every stage. So with a plain `Result`, a second fallible stage means: land the results,
partition, extract the successes, start a new pipeline.

```java
var read = paths.stream().map(attempt(Files::readString)).toList();
read.stream().filter(r -> !r.isOk()).forEach(r -> report("read", r.error()));
List<String> texts = read.stream().filter(Result::isOk).map(Result::value).toList();

var parsed = texts.stream().map(attempt(this::parseJson)).toList();
parsed.stream().filter(r -> !r.isOk()).forEach(r -> report("parse", r.error()));
List<Config> configs = parsed.stream().filter(Result::isOk).map(Result::value).toList();
```

Readable, and every pipeline is typed on the real element. Cost is linear: each fallible stage adds a materialized
list plus three lines. At two stages it is already heavy — that is the signal to change approach.

</details>

### Discarding failed elements mid-pipeline?
<details><summary>Show answer</summary>

`flatMap` can return zero elements, so a failure can simply vanish. The stream stays typed on the real element and
downstream stages need no changes.

```java
paths.stream()
    .flatMap(p -> {
      try { return Stream.of(Files.readString(p)); }
      catch (IOException e) { log(e); return Stream.empty(); }
    })
    .map(String::toUpperCase)  // element is String again — downstream is clean
    .toList();
```

Weakness, and it is serious: **the only trace of a failure is a log line.** No count, no report, no immediate
feedback, nothing the caller can act on. A silently shortened list looks identical to a successful run. It is not
maintainable as a default — reach for it only where a failed element is genuinely uninteresting (best-effort scans,
optional enrichment), never where someone must know what was dropped.

Same shape with `Optional`: `map(...).flatMap(Optional::stream)` — same objection.

</details>

### Chaining several fallible steps?
<details><summary>Show answer</summary>

Give `Result` its own `map`. The unwrap/rewrap happens once, inside that method, instead of at every stage. Failures
short-circuit: a step whose input already failed never runs and passes the error through. The whole chain then lives
inside **one** stream stage, so no split is needed.

```java
record Result<T>(T value, Exception error, String stage) {
  static <T> Result<T> ok(T v) { return new Result<>(v, null, null); }
  boolean isOk() { return error == null; }

  <R> Result<R> map(ThrowingFunction<T, R> f, String stage) {
    if (error != null) return new Result<>(null, error, this.stage);  // already failed: skip, carry through
    try { return new Result<>(f.apply(value), null, null); }
    catch (Exception e) { return new Result<>(null, e, stage); }      // stage name: which step broke
  }
}

Map<Boolean, List<Result<Config>>> byOutcome = paths.stream()
    .map(p -> Result.ok(p)
        .map(Files::readString, "read")   // may fail
        .map(this::parseJson, "parse")    // skipped if read failed
        .map(this::validate, "validate"))
    .collect(partitioningBy(Result::isOk));

byOutcome.get(false).forEach(r -> report(r.stage(), r.error()));
```

Strengths: cost stops being linear — a fourth step is one more line. Nothing is lost. `parseJson` still receives a
`String`, not a wrapper. The stage label turns "IOException somewhere" into something actionable.

Weaknesses: two different `map`s in one expression — `stream.map` walks elements, `result.map` walks the
success/failure axis — which reads confusingly until the shape is familiar. And it is a hand-rolled monad: a design
commitment for the codebase, not a local trick. Half-adopting it is worse than not adopting it.

</details>

### Passing a throwing lambda to map?
<details><summary>Show answer</summary>

It will not compile. The standard functional interfaces declare no `throws`, so a checked exception cannot leave a
lambda body. Three ways out, each with a real cost:

1. **`try/catch` inside every lambda.** Works, no infrastructure. But it destroys the pipeline's readability and the
   block is copy-pasted into every lambda that touches a throwing call. Not viable as a general policy.
2. **A wrapper that rethrows unchecked.** Compiles, reads well. Cost: the checked type is erased from the signature,
   so a caller can no longer catch `IOException` without digging into `getCause()`. A separate wrapper is needed per
   interface shape — `Function`, `Consumer`, `Predicate`, `Supplier` — since their signatures differ.
3. **A wrapper that returns `Result`** (`attempt(...)`). The same wrapper solves the compile problem *and* keeps the
   pipeline alive; the failure becomes data instead of an abort. This is why `Result` covers both the compile-time
   and runtime problems at once.

```java
static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R> f) {
  return t -> {
    try { return f.apply(t); }
    catch (Exception e) { throw new RuntimeException(e); }  // checked type is now hidden from callers
  };
}

paths.stream()
    .map(unchecked(Files::readString))  // compiles; IOException surfaces wrapped at runtime
    .forEach(System.out::println);
```

Note what option 2 really does: it converts a checked exception into an unchecked one and hands the pipeline the same
abort behavior it has for any runtime exception. That is correct when the failure is genuinely fatal to the batch.
When it is not, option 3 is the answer.

</details>

### Collecting failures instead of aborting?
<details><summary>Show answer</summary>

Any exception escaping a lambda aborts the terminal operation — no partial result, the rest of the elements never
run. To keep going, the failure must stop being a throw and become a value.

The type: one channel holds either the value or the error. Two extra fields make the feedback usable — **stage**
(which step broke) and **source** (which input broke). Without them a failure reads "IllegalStateException
somewhere", which nobody can act on.

```java
record Result<T, S>(T value, RuntimeException error, String stage, S source) {

  static <S> Result<S, S> start(S source) { return new Result<>(source, null, null, source); }

  boolean isOk() { return error == null; }

  <R> Result<R, S> map(Function<T, R> f, String stage) {
    if (error != null) return new Result<>(null, error, this.stage, source);  // failed already: skip, carry through
    try { return new Result<>(f.apply(value), null, null, source); }
    catch (RuntimeException e) { return new Result<>(null, e, stage, source); }
  }

  String describe() { return source + " failed at [" + stage + "]: " + error.getMessage(); }
}
```

`S` is pinned at `start` and never rewritten, so a failure at stage 3 still names the original input. Its own `map`
means the unwrap happens once inside the type, not at every stage — so several fallible steps fit in **one** stream
stage and no pipeline split is needed.

Catch choice matters: `RuntimeException` also swallows `NullPointerException` — a bug, not bad data. Catching the
project's own marker type instead keeps programming errors fatal and only data failures collectible.

</details>

### Reporting on a partly failed batch?
<details><summary>Show answer</summary>

Chain the fallible steps inside `Result`, partition once, and build one consolidated report. Every processable item
is processed; every failure is named.

```java
Map<Boolean, List<Result<Config, Path>>> byOutcome = paths.stream()
    .map(p -> Result.start(p)
        .map(this::readFile, "read")
        .map(this::parseJson, "parse")    // skipped if read failed
        .map(this::validate, "validate"))
    .collect(partitioningBy(Result::isOk));

List<Config> configs = byOutcome.get(true).stream().map(Result::value).toList();
List<String> failures = byOutcome.get(false).stream().map(Result::describe).toList();

if (!failures.isEmpty()) {
  throw new IllegalStateException(failures.size() + "/" + paths.size()
      + " items failed:\n" + String.join("\n", failures));
}
```

One exception carrying every failure, instead of one exception about whichever item happened to be first.

Costs to know: two different `map`s in one expression — `stream.map` walks elements, `Result.map` walks the
success/failure axis — which reads oddly until familiar. And it is a hand-rolled monad: a codebase-wide commitment,
not a local trick. Half-adopting it is worse than not adopting it.

</details>