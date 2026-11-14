# Java Language State

- **Strengths** — backward compatibility, the JVM and its ecosystem, a clear language for learning concepts.
- **New features in use** — data modelling with records, sealed types and pattern matching; collections and streams;
  virtual threads; runtime improvements.
- **Missing or weak** — null safety, generics with erasure, value types, ergonomic gaps, no `async`/`await` by design;
  FP limits → [FAQs_Java_Functional/1.2](../FAQs_Java_Functional/1_Functional_Basics/1.2_Limitations_of_Java_as_a_FP_language.md).
- **Coming** — Valhalla, Leyden, structured concurrency; each one linked from the gap it fixes.

## Strengths

### What are Java's lasting strengths?
<details><summary>Show answer</summary>

- [Backward compatibility](#why-is-backward-compatibility-one-of-javas-lasting-strengths) — old code keeps running on
  new JVMs.
- [The JVM and its ecosystem](#why-are-the-jvm-and-its-ecosystem-a-bigger-strength-than-the-language-itself) — the
  runtime and the libraries matter more than the syntax.
- [A clear language for learning](#why-is-java-still-a-good-language-for-learning-programming-concepts) — concepts are
  explicit and visible in the code.

</details>

### Why is backward compatibility one of Java's lasting strengths?
<details><summary>Show answer</summary>

A JAR compiled many years ago usually runs on a current JVM without changes. Companies can upgrade the JDK and get
newer garbage collectors, security fixes and performance without rewriting code, which is why so much long-lived
business software runs on Java.

It has a price. Old design decisions stay: generics are erased at run time so that pre-generics code keeps working
([generics card](#what-cant-you-do-with-java-generics-and-why)), and old APIs such as `Date` or `Vector` are never
removed. Compatibility is also not absolute: strong encapsulation of JDK internals (JDK 16–17) broke libraries that used
internal classes, and features like the Security Manager were removed.

**Handle:** compatibility makes upgrades cheap, and it is also the reason old design mistakes stay.

</details>

### Why are the JVM and its ecosystem a bigger strength than the language itself?
<details><summary>Show answer</summary>

The language is only half of what you choose. The JVM brings:

- a JIT compiler that optimizes the running code based on how it is actually used;
- a choice of garbage collectors, from throughput-oriented G1 to ZGC with sub-millisecond pauses;
- built-in observability: Java Flight Recorder, JMX, thread and heap dumps.

On top of it sits a huge ecosystem: Spring, Hibernate, Kafka clients, testing tools, Maven Central. The platform also
runs other languages — Kotlin, Scala, Groovy, Clojure — so a team can use a more modern language and keep the same
runtime and libraries.

**Handle:** you choose Java mostly for the platform; the syntax is the smaller part.

</details>

### Why is Java still a good language for learning programming concepts?
<details><summary>Show answer</summary>

Java makes concepts explicit. Types are declared, interfaces are separate from implementations, and you must choose an
implementation yourself: there is no "just a list", you pick `ArrayList` or `LinkedList` and so have to think about how
each one works. Costs are visible too, such as boxing an `int` into an `Integer`. Concurrency has explicit tools and a
documented memory model, which few languages offer.

The usual counterpoint: Python is easier as a very first language, because there is less ceremony before the first
program runs. Java reduced that ceremony in JDK 25 with instance `main` methods and compact source files:

```java
void main() {
  System.out.println("Hello");
}
```

**Handle:** Java shows *how* things work, which makes it a good school for concepts, if not the shortest path to
"Hello, world".

</details>

## New features in use

### Which recent Java features do you use in everyday code?
<details><summary>Show answer</summary>

- [Data modelling](#how-do-records-sealed-types-and-pattern-matching-change-the-way-you-model-a-domain) — records,
  sealed types, pattern matching in `switch`.
- [Collections and streams](#what-changed-in-collections-and-streams-in-recent-java-versions) — `toList()`, sequenced
  collections, gatherers.
- [Concurrency](#what-changed-in-java-concurrency-with-virtual-threads) — virtual threads, scoped values.
- [Runtime improvements](#what-does-each-java-release-bring-at-the-runtime-level-without-code-changes) — faster
  without code changes.

</details>

### How do records, sealed types and pattern matching change the way you model a domain?
<details><summary>Show answer</summary>

Together they let you model data as a closed set of variants that the compiler checks.

- A record is a final, shallowly immutable data carrier: the constructor, accessors, `equals`, `hashCode` and
  `toString` are generated. It replaces most hand-written or Lombok-generated data classes.
- A sealed interface lists exactly which types may implement it.
- Pattern matching in a `switch` expression takes a value apart by its type and components.

```java
sealed interface Payment permits Card, Transfer {}
record Card(String number, BigDecimal amount) implements Payment {}
record Transfer(String iban, BigDecimal amount) implements Payment {}

BigDecimal fee(Payment payment) {
  return switch (payment) {
    case Card c -> c.amount().multiply(CARD_RATE);
    case Transfer t -> FLAT_FEE;
  };
}
```

There is no `default` branch, because the compiler knows all subtypes. If someone adds a third payment type, every such
`switch` stops compiling until it handles the new type, so no case is forgotten. This combination is what other
languages call algebraic data types.

Lombok is still common where records do not fit, for example JPA entities, which need a no-argument constructor and
mutable fields.

Versions: records 16, sealed types 17, pattern matching in `switch` and record patterns 21.

**Handle:** sealed types + records + `switch` = a closed set of variants that the compiler checks for completeness.

</details>

### What changed in collections and streams in recent Java versions?
<details><summary>Show answer</summary>

- `Stream.toList()` (16) returns an unmodifiable list. It replaces `collect(Collectors.toList())`, which makes no promise
  about the list type or whether it can be changed.
- Sequenced collections (21) give every ordered collection one API for both ends: `getFirst()`, `getLast()`,
  `addFirst()`, `removeLast()`, `reversed()`; maps get `firstEntry()`, `pollLastEntry()`, `putFirst()`. Before, getting
  the last element of a `LinkedHashSet` had no direct method, and a list needed `list.get(list.size() - 1)`.
- Stream gatherers (24) let you write your own intermediate operations with `stream.gather(…)`, the way collectors let
  you write terminal ones. Built-in ones cover common gaps:

```java
List<List<Integer>> batches = Stream.of(1, 2, 3, 4, 5, 6, 7)
                                    .gather(Gatherers.windowFixed(3))
                                    .toList(); // [[1, 2, 3], [4, 5, 6], [7]]
```

Other built-in gatherers: `windowSliding`, `fold`, `scan`, `mapConcurrent`.

**Handle:** an unmodifiable `toList()`, one API for both ends of ordered collections, and custom intermediate stream
steps.

</details>

### What changed in Java concurrency with virtual threads?
<details><summary>Show answer</summary>

Virtual threads (21) are threads managed by the JVM, not by the operating system, so they cost almost nothing to create
and you can run millions of them. A virtual thread runs on top of a normal platform thread (the carrier). When it
blocks on I/O, the JVM unmounts it and the carrier runs another virtual thread.

As a result, plain blocking code — one thread per request, a simple `jdbc` call — scales like reactive code, without
callbacks or `Mono`/`Flux`. In Spring Boot it is one setting: `spring.threads.virtual.enabled=true`.

Points to know:

- since JDK 24, `synchronized` no longer pins a virtual thread to its carrier; before that, blocking inside
  `synchronized` blocked the carrier too;
- do not pool virtual threads; limit access to scarce resources with a `Semaphore` or the connection pool instead;
- they help with waiting (I/O), not with CPU-heavy work.

Scoped values (final in 25) replace `ThreadLocal` for passing context such as a user or a request ID. A value is bound
for one call scope and cannot be changed, so millions of threads do not each carry a mutable copy.

Structured concurrency is the next step, still in preview: [structured concurrency](#what-problem-does-structured-concurrency-solve).

**Handle:** virtual threads make blocking cheap, so simple blocking code scales.

</details>

### What does each Java release bring at the runtime level, without code changes?
<details><summary>Show answer</summary>

Every release improves the runtime, so upgrading the JDK is itself a performance step. Recent examples:

- garbage collectors: generational ZGC (21, the only ZGC mode since 24), G1 improvements;
- smaller objects: compact object headers (25, switched on with `-XX:+UseCompactObjectHeaders`) shrink every object
  header;
- faster startup and warm-up: the AOT cache from [Project Leyden](#what-problem-does-project-leyden-solve-and-how)
  (24–25).

**Handle:** a JDK upgrade is free performance — newer GCs, smaller objects, faster startup.

</details>

## Missing or weak

### Where is Java weak compared to other modern languages?
<details><summary>Show answer</summary>

- [Null safety](#how-does-java-handle-null-compared-to-languages-with-null-safety) — not checked by the compiler.
- [Generics](#what-cant-you-do-with-java-generics-and-why) — erased at run time, no primitives.
- [Value types](#what-does-the-lack-of-value-types-cost-in-java) — every object has identity and a header.
- [Ergonomic gaps](#which-everyday-ergonomic-gaps-does-java-still-have) — no named parameters, checked exceptions in
  lambdas, no string interpolation.
- [No `async`/`await`](#why-does-java-have-no-asyncawait-and-is-that-a-gap) — a design choice, not a gap.
- [Functional programming limits](#what-limits-java-as-a-functional-programming-language).

</details>

### How does Java handle null compared to languages with null safety?
<details><summary>Show answer</summary>

In Java any reference can be `null`, and the compiler does not check it. Protection is by convention:

- `Optional` as a return type for "may be absent";
- nullness annotations checked by tools — JSpecify is becoming the common standard, and Spring Framework 7 uses it;
- discipline and tests.

Kotlin, C#, Swift and TypeScript put nullness into the type system: `String` and `String?` are different types, and
the compiler rejects using a possibly-null value without a check.

Java plans null-restricted types (such as `String!`) as part of [Valhalla](#what-will-project-valhalla-change).

**Handle:** in Java null safety is a convention; in Kotlin or C# it is a type.

</details>

### What can't you do with Java generics, and why?
<details><summary>Show answer</summary>

Generics are erased: the type arguments exist only at compile time, and at run time a `List<String>` is just a `List`.
Java 5 chose this so that generic code works with pre-generics code and libraries.

What you cannot do because of it:

- `new T()` or `new T[10]`;
- `instanceof List<String>`;
- two overloads that differ only in type arguments (`void f(List<String>)` and `void f(List<Integer>)`);
- primitive type arguments: `List<int>` does not exist, so `List<Integer>` boxes every value into an object.

Workarounds: passing a `Class<T>` token, primitive streams (`IntStream`), or primitive collection libraries. C# keeps
type arguments at run time (reified generics) and supports primitives in them; Kotlin has `reified` type parameters
for inline functions.

Generics over primitives are a later step of [Valhalla](#what-will-project-valhalla-change).

**Handle:** erasure bought compatibility and costs run-time type information and primitive generics.

</details>

### What does the lack of value types cost in Java?
<details><summary>Show answer</summary>

Every Java object has identity: its own heap allocation, a header, and a reference that points to it. For small data
like a `Point(x, y)` or a money amount, that is mostly overhead:

- memory: the header can be as large as the data;
- memory layout: a `Point[]` is an array of references to objects scattered over the heap, not the points themselves,
  so reading it causes cache misses;
- GC pressure: millions of small objects to allocate and collect;
- boxing: `Integer` instead of `int` everywhere generics are involved.

C# has `struct`, Rust and C++ have values by default: the data sits directly in the array or field. The JIT can
sometimes avoid an allocation (escape analysis), but not for arrays or object fields.

The fix is [Valhalla](#what-will-project-valhalla-change).

**Handle:** in Java small data is still a full object — identity you do not need costs memory and speed.

</details>

### Which everyday ergonomic gaps does Java still have?
<details><summary>Show answer</summary>

- No named or default parameters. Kotlin, C# and Python have them. Java falls back on builders, overloads, or records
  with long component lists that are hard to read. Derived record creation ("withers") has been proposed *(unverified:
  current status)*.
- Checked exceptions do not work with lambdas: `Function` cannot throw a checked exception, so stream code ends up
  wrapping it in a `RuntimeException` or putting `try`/`catch` inside the lambda. No other mainstream language adopted
  checked exceptions.
- No string interpolation. Kotlin, C# and JavaScript have it. Java's string templates were previewed in JDK 21–22 and
  withdrawn in 23 for a redesign; until then you use `formatted()` or `String.format`.
- Also missing: extension methods and properties (Kotlin, C#).

**Handle:** day-to-day friction is in parameters, exceptions in lambdas, and string formatting.

</details>

### Why does Java have no async/await, and is that a gap?
<details><summary>Show answer</summary>

It is a deliberate choice. In C#, JavaScript, Kotlin (`suspend`) and Rust, `async` functions are "colored": an async
function can only be awaited from another async function. The color spreads up the call chain, and libraries end up
with two versions of their APIs.

Java solved the same problem — not wasting threads while waiting for I/O — in the runtime instead:
[virtual threads](#what-changed-in-java-concurrency-with-virtual-threads) make blocking cheap. Normal blocking code
and the existing blocking APIs scale without a new keyword or a second API.

The trade-off: code does not show where it may suspend, and CPU-heavy work gains nothing. Reactive libraries remain
useful for streaming data with backpressure.

**Handle:** other languages changed the language to avoid blocking; Java made blocking cheap.

</details>

### What limits Java as a functional programming language?
<details><summary>Show answer</summary>

Java has lambdas and streams, but functions are not a type of their own: a lambda is an implementation of a functional
interface. Lambdas can capture only effectively final variables, checked exceptions do not fit them, there is no tail
call optimization, and data is mutable by default, with no persistent collections in the JDK.

Details:
[FAQs_Java_Functional/1.2](../FAQs_Java_Functional/1_Functional_Basics/1.2_Limitations_of_Java_as_a_FP_language.md).

**Handle:** Java supports a functional style, not functional programming as a first-class model.

</details>

## Coming

### Which upcoming Java projects fix its current gaps?
<details><summary>Show answer</summary>

- [Valhalla](#what-will-project-valhalla-change) — value classes, null-restricted types, later primitive generics.
- [Leyden](#what-problem-does-project-leyden-solve-and-how) — startup and warm-up time.
- [Structured concurrency](#what-problem-does-structured-concurrency-solve) — running related concurrent tasks as one
  unit.

</details>

### What will Project Valhalla change?
<details><summary>Show answer</summary>

Valhalla adds value classes: classes whose objects have no identity. They are compared by their state (`==` compares
the fields), you cannot lock on them, and the JVM is free to store them flat — directly inside arrays and fields, with
no header and no pointer.

```java
value record Point(int x, int y) {}

Point[] points = new Point[1_000_000]; // can be stored as x, y, x, y, … instead of a million references
```

It is planned in steps:

- value classes (JEP 401) — Gemini says it is targeting preview in JDK 28 *(unverified)*;
- null-restricted types (`Point!`), which allow full flattening and bring null checking into the type system;
- later, generics over primitives and value classes.

Existing value-based classes such as `Integer` and `LocalDate` are planned to become value classes. That is why the JDK
already warns about `synchronized` on them.

It fixes: [value types](#what-does-the-lack-of-value-types-cost-in-java),
[null safety](#how-does-java-handle-null-compared-to-languages-with-null-safety), and
[primitive generics](#what-cant-you-do-with-java-generics-and-why).

**Handle:** Valhalla lets objects that only hold data be stored like primitives.

</details>

### What problem does Project Leyden solve, and how?
<details><summary>Show answer</summary>

Leyden attacks slow startup and warm-up. On every start, the JVM loads and links classes, runs code in the interpreter,
collects profiles and only then JIT-compiles the hot code. That matters for short-lived processes, serverless functions
and fast scaling.

Leyden moves this work into a training run. You run the application once, the JVM records what it did into an AOT
cache, and production runs start from that cache:

```text
java -XX:AOTCacheOutput=app.aot -jar app.jar   # training run
java -XX:AOTCache=app.aot -jar app.jar         # production run
```

*(unverified: flag names)*

Delivered in steps: ahead-of-time class loading and linking (24), method profiles in the cache (25), and more since —
Gemini mentions JEP 516 in JDK 26 *(unverified)*.

Compared with GraalVM Native Image: native image starts even faster, but it assumes no new code appears at run time, so
reflection and dynamic features need configuration, and peak performance can be lower. Leyden keeps the full JVM and
its JIT and makes the start faster.

**Handle:** Leyden does the JVM's startup work once in a training run and reuses it on every start.

</details>

### What problem does structured concurrency solve?
<details><summary>Show answer</summary>

With an `ExecutorService` or `CompletableFuture`, subtasks are not tied to the task that started them. If one of three
parallel calls fails, the other two keep running, and nobody waits for them or cancels them: orphan threads. Failures
and cancellation are handled by hand, and a thread dump does not show which tasks belong together.

Structured concurrency treats related subtasks as one unit with a clear scope. The scope cannot end before all its
subtasks have finished. When one fails, the others are cancelled, and the error reaches the parent.

```java
try (var scope = StructuredTaskScope.open()) {
  Subtask<User> user = scope.fork(() -> findUser(id));
  Subtask<List<Order>> orders = scope.fork(() -> findOrders(id));
  scope.join(); // fails if any subtask failed; the others are cancelled
  return new Profile(user.get(), orders.get());
}
```

It is still in preview (`--enable-preview`); Gemini says it is the sixth preview in JDK 26, JEP 525 *(unverified)*. The
API changed between previews *(unverified: the example uses the shape from the JDK 25 redesign)*.

It fixes the cost of coordinating tasks with `CompletableFuture`, and builds on
[virtual threads](#what-changed-in-java-concurrency-with-virtual-threads).

**Handle:** concurrent subtasks start, fail and finish together inside one scope — like a method call.

</details>
