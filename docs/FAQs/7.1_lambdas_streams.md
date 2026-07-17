## Lambdas and Streams - Lambdas

### What is a function object?
<details><summary>Show answer</summary>

A type with exactly one abstract method serves as a **function type**. 
An instance of such a type is a **function object** — it represents a function or an action.

</details>

### What governs lambdas and function objects?
<details><summary>Show answer</summary>

Java has no function type. A one-method interface stands in for one, so its instances become the function objects
everything else is built on.

Four things follow, in order:

1. **How you write one** — [anonymous class, lambda, method reference](#how-is-a-function-object-created)
2. **Which type it targets** — [the standard set](#what-are-the-six-basic-functional-interfaces), so you
   [rarely declare your own](#when-should-you-write-your-own-functional-interface)
3. **What the compiler needs** — [generics feed the inference](#how-do-generics-affect-type-inference-in-lambdas)
4. **What the object actually is** — [not what the syntax suggests](#two-lambdas-with-identical-bodies--are-they-equal)

**Trigger:** *no function type → an interface stands in → written three ways → mostly pre-supplied → an object, not a function.*

</details>

### How is a function object created?
<details><summary>Show answer</summary>

- [as an anonymous class](#what-is-an-anonymous-class)
- [as a lambda](#what-is-a-lambda)
- [as a method reference](#what-is-a-method-reference)

</details>

### What is an anonymous class?
<details><summary>Show answer</summary>

A class declared and instantiated in one expression, with no name. It may implement an interface or extend a class,
and the body is written inline at the point of use.

```java
Comparator<String> byLength = new Comparator<String>() {
  public int compare(String a, String b) {   // the single abstract method, implemented inline
    return Integer.compare(a.length(), b.length());
  }
};
```

</details>

### What is a lambda?
<details><summary>Show answer</summary>

An expression that supplies the body of the one abstract method, with no class declaration around it. 
The compiler infers both the target type and the parameter types from the context.

```java
Comparator<String> byLength = (a, b) -> Integer.compare(a.length(), b.length());   // types inferred, no class written
```

</details>

### Lambda or anonymous class — which fits?
<details><summary>Show answer</summary>

Decide by **capability**, not preference — each handles a different shape of inline one-off:

- **Target type** — lambda works *only* for a functional interface (one abstract method); an anonymous class can
  implement an interface with **many** methods, or **extend a class**.
- **State** — a lambda holds no instance fields (only captures effectively-final variables); an anonymous class can
  declare **its own fields**.
- **Meaning of `this`** — in a lambda `this` is the **enclosing** instance; in an anonymous class `this` is the
  **anonymous instance** itself. 
  If you need access to the function object from within its body, then you must use an anonymous class.

So:

- **Lambda fits** when the one-off is a single-method interface, stateless, and you want enclosing `this` — less
  boilerplate, clearer at the call site, ideal in streams and callbacks.
- **Anonymous class is required** when the one-off needs **multiple methods**, **its own state**, **to extend a
  class**, or **its own `this`** — none of which a lambda can express.

The lambda is the narrower, lighter tool for the common case; the anonymous class is the general tool for everything a
lambda can't reach. Neither replaces the other — capability decides.

</details>

### What governs how much a lambda should contain?
<details><summary>Show answer</summary>

A lambda has no name and no documentation, so it carries no explanation of itself — the code must speak alone.
That holds only while the computation is short and self-evident. Past a few lines, or when the logic is not obvious
at a glance, move it into a named method and pass a reference instead.

</details>

### How do generics affect type inference in lambdas?
<details><summary>Show answer</summary>

Lambda type inference is fed by generics — the compiler derives the parameter types from the target type's type
arguments. Raw types or non-generic APIs leave it nothing to infer from, so every parameter type must be written
out by hand, and the lambda's brevity is lost.

```java
List<String> words = ...;
words.sort((a, b) -> Integer.compare(a.length(), b.length()));   // a, b inferred as String

List raw = ...;                                                   // raw type — nothing to infer
raw.sort((Object a, Object b) ->
    Integer.compare(((String) a).length(), ((String) b).length()));   // types and casts by hand
```

</details>

### Enum constructors and lambdas — what to consider?
<details><summary>Show answer</summary>

Enum constructor arguments are evaluated in a static context.
Thus, lambdas in enum constructors can’t access instance members of the enum.

</details>

### What is a method reference?
<details><summary>Show answer</summary>

A shorthand for a lambda that does nothing but call an existing method. `::` names the method; the compiler builds
the function object from it, so the parameters are never written out.

```java
map.merge(key, 1, (count, incr) -> count + incr);   // lambda
map.merge(key, 1, Integer::sum);                    // method reference — same behavior, no params
```

</details>

### Method reference vs lambda?
<details><summary>Show answer</summary>

Not a capability difference — a lambda can express anything a method reference can (one obscure JLS corner aside).
The difference is in what the code reads like and where the logic lives:

- **Method reference** — shorter and clearer when the lambda is only a call to something that already exists.
- **Lambda** — the fallback when no suitable method exists, or the body is genuinely inline logic.

The escape hatch: when a lambda grows too long or unclear, extract its body into a named method and pass a
reference to it. The method gets a name and documentation — the two things a lambda can never have.

**But "usually" is not "always."** A **static** method reference must spell out the class that holds the method;
a lambda calling it from inside that same class doesn't. So with a long class name the reference is the longer form:

```java
// inside class GoshThisClassNameIsHumongous, calling its own static action()
service.execute(GoshThisClassNameIsHumongous::action);   // reference — class name unavoidable
service.execute(() -> action());                          // lambda — shorter and no less clear
```

This is specific to statics. For an instance method the receiver form `this::action` stays short regardless of the
class name, and the trade-off mostly disappears.

The rule is not "prefer method references." It is: pick whichever is shorter and clearer at the call site.

</details>

### How is an identity function written?
<details><summary>Show answer</summary>

Two forms:

```java
Function.identity()   // the JDK's static factory
x -> x                // the inline lambda
```

Both produce the same function object. 
The lambda is usually shorter and clearer — the factory's name says less than the code it replaces, 
so prefer `x -> x` inline.

</details>

### `Function.identity()` vs `Function::identity`?
<details><summary>Show answer</summary>

Different things, one layer apart:

- **`Function.identity()`** — a **call**. Runs the factory, hands back the function object `x -> x`.
  Type: `Function<T, T>`.
- **`Function::identity`** — a **reference to the factory**. Doesn't run it; wraps it as a function object that
  *produces* an identity function when invoked. Type: `Supplier<Function<T, T>>`.

```java
Function<String, String> f = Function.identity();            // f is x -> x
f.apply("a");                                                // "a"

Supplier<Function<String, String>> s = Function::identity;   // one level up
s.get().apply("a");                                          // "a" — must call get() first

stream.map(Function.identity());   // ok — map wants a Function
stream.map(Function::identity);    // compile error — that's a Supplier
```

The usual "`::` passes, `()` calls" instinct misleads here. 
It holds when the method *is* the behavior you want to pass (`Integer::sum` — `sum` does the adding). 
`identity()` isn't the behavior; it's a factory that returns the behavior. So call it and pass the result.

</details>

### What are the six basic functional interfaces?
<details><summary>Show answer</summary>

Everything in `java.util.function` derives from these six. Recall them by the two questions that define any
function shape: **what goes in** and **what comes out**.

| Interface           | Method            | Shape       | Example               |
|:--------------------|:------------------|:------------|:----------------------|
| `Supplier<T>`       | `T get()`         | nothing → T | `Instant::now`        |
| `Consumer<T>`       | `void accept(T)`  | T → nothing | `System.out::println` |
| `Predicate<T>`      | `boolean test(T)` | T → boolean | `Collection::isEmpty` |
| `Function<T,R>`     | `R apply(T)`      | T → R       | `Arrays::asList`      |
| `UnaryOperator<T>`  | `T apply(T)`      | T → T       | `String::toLowerCase` |
| `BinaryOperator<T>` | `T apply(T,T)`    | (T,T) → T   | `BigInteger::add`     |

`Runnable` completes the grid — nothing → nothing. It lives in `java.lang`, not this package, but it is a proper
functional interface and a valid lambda target.

</details>

### Operators vs Function — what's the relationship?
<details><summary>Show answer</summary>

They are — `UnaryOperator<T> extends Function<T,T>` and `BinaryOperator<T> extends BiFunction<T,T,T>`. 
They add no abstract method; they only pin the type parameters together.

The point is expressiveness in an API signature. 
- `UnaryOperator<String>` states that the result is the same type as the input; 
- `Function<String,String>` says the same thing but the reader must compare the two arguments to see it.
- `List.replaceAll` takes a `UnaryOperator` because same-type-out is a real constraint of the operation, not an accident.

Consequence: any `UnaryOperator<T>` can be passed where a `Function<T,T>` is expected. Not the reverse.

</details>

### Supplier vs Consumer — which way does data flow?
<details><summary>Show answer</summary>

Opposites, and the names are from the **caller's** point of view, which is where the confusion starts.

- `Supplier<T>` — **supplies to you**. Takes nothing, returns a T. Used for deferred or lazy production:
  `Objects.requireNonNullElseGet(x, Foo::new)` only builds the `Foo` if needed.
- `Consumer<T>` — **consumes what you give it**. Takes a T, returns nothing. Used for side effects only, since
  there is no result: `list.forEach(System.out::println)`.

A `Consumer` that returns nothing is useless unless it mutates something or performs I/O. That is the tell that
side effects are its whole purpose.

</details>

### Which functional interfaces take two arguments?
<details><summary>Show answer</summary>

The `Bi` prefix means arity two. Seven interfaces total:

| Interface                 | Method                      | Shape           |
|:--------------------------|:----------------------------|:----------------|
| `BiFunction<T,U,R>`       | `R apply(T,U)`              | (T,U) → R       |
| `BiConsumer<T,U>`         | `void accept(T,U)`          | (T,U) → nothing |
| `BiPredicate<T,U>`        | `boolean test(T,U)`         | (T,U) → boolean |
| `BinaryOperator<T>`       | `T apply(T,T)`              | (T,T) → T       |
| `ToIntBiFunction<T,U>`    | `int applyAsInt(T,U)`       | (T,U) → int     |
| `ToLongBiFunction<T,U>`   | `long applyAsLong(T,U)`     | (T,U) → long    |
| `ToDoubleBiFunction<T,U>` | `double applyAsDouble(T,U)` | (T,U) → double  |

Note the two deliberate gaps. There is no `BiSupplier` — a supplier takes no arguments, so arity has nothing to vary. 
There is no `BiUnaryOperator` — "unary" already fixes the arity at one; 
the two-argument same-type form is `BinaryOperator`.

`BiFunction<K,V,V>` is what `Map.merge` and `Map.compute` take; `BiConsumer<K,V>` is what `Map.forEach` takes.

</details>

### How are the interfaces in java.util.function generated?
<details><summary>Show answer</summary>

From the six basics, by two rules — plus a short list of exceptions worth knowing.

**Rule 1 — arity.** Prefix `Bi` for two arguments: `BiFunction`, `BiConsumer`, `BiPredicate`.

**Rule 2 — primitive specialization.** Every variant exists to avoid boxing. 
Three primitives get support: `int`, `long`, `double`.

- *Primitive in* → prefix the primitive: `IntPredicate`, `IntConsumer`, `IntFunction<R>`, `IntUnaryOperator`.
- *Primitive out* → prefix `To`: `ToIntFunction<T>`, `ToIntBiFunction<T,U>`.
- *Both, different types* → name both: `IntToLongFunction`, `DoubleToIntFunction`.
- *Method names change too* — `apply` becomes `applyAsInt`/`applyAsLong`/`applyAsDouble`, because erasure would
  otherwise collide the overloads.

**The exceptions — where the rules stop working:**

- **Suppliers break the `To` rule.** It's `IntSupplier`, not `ToIntSupplier` — even though the primitive is on the
  output side. A supplier has no input, so there's no ambiguity to resolve and the prefix form was kept short.
- **`BooleanSupplier` is the only boolean specialization.** No `BooleanFunction`, no `BooleanConsumer`. Anything
  returning boolean from an argument is already `Predicate`, so the family would be redundant.
- **`ObjIntConsumer<T>`, `ObjLongConsumer<T>`, `ObjDoubleConsumer<T>`** — the mixed object-plus-primitive form.
  Used by `Collector`'s accumulator, and by nothing else in the package.

**The point:** don't memorize the 37. Recall that they exist only to avoid boxing, and the name falls out. When
one is missing, that's the tell — nothing else needed it.

</details>

### When should you write your own functional interface?
<details><summary>Show answer</summary>

Default is to reuse a standard one — a shared type means less to learn, and it comes with the composition methods
(`andThen`, `compose`, `negate`, `or`) already built. Write your own only:
- when a standard interface genuinely doesn't fit, or 
- when at least one of these applies strongly enough to earn its keep:
  - **The name carries real meaning.** `Comparator<T>` is structurally `ToIntBiFunction<T,T>`, but *comparator* names
    a concept everyone knows, and the name appears constantly in APIs.
  - **The contract is stronger than the shape.** A comparator must be transitive and antisymmetric; the type has a
    documented contract that `ToIntBiFunction` has no place to state.
  - **It needs its own default methods.** `Comparator.reversed()`, `thenComparing()` — a general-purpose interface
    can't host these.

Two of the three describing your interface is the signal — one alone usually isn't.

**Always annotate it `@FunctionalInterface`.** It documents the intent and makes the compiler reject a second
abstract method, so nobody breaks lambda usage by accident later.

</details>


### Which interface fits a nothing-in, nothing-out action?
<details><summary>Show answer</summary>

`Runnable` — `@FunctionalInterface`, `void run()`. A valid lambda target anywhere:

```java
Runnable onSave = () -> log.info("saved");   // no thread in sight
onSave.run();
```

Its association with `Thread` and `ExecutorService` comes from where it's most often *used*, not from anything in
the type. It lives in `java.lang`, not `java.util.function`, so scanning the functional package for this shape
makes the grid look like it has a hole. It doesn't — which is why people reinvent it as `Executable` or `Action`.

</details>

### Boxed vs primitive functional interfaces?
<details><summary>Show answer</summary>

**Do not use the boxed interfaces with primitives.**
Because `Function<Integer,Integer>` boxes on every call, and the cost is not the allocation alone.

```java
IntUnaryOperator fast = x -> x * 2;              // int in, int out — no objects
Function<Integer,Integer> slow = x -> x * 2;      // unbox, multiply, box the result
```

Three separate costs stack up: allocation of the wrapper, the unbox/box round trip per call, and — the one people
miss — the loss of the flat memory layout. A boxed stream holds pointers to scattered `Integer` objects, so every
element access is a potential cache miss, where an `IntStream` walks contiguous ints.

The point is that this is invisible in a benchmark of one call and severe over millions.

</details>

### Can an interface with several methods still be functional?
<details><summary>Show answer</summary>

Yes. The requirement is exactly one **abstract** method — not one method.

Free to add without breaking it:
- **default methods** — `Function` has `andThen` and `compose`; `Predicate` has `negate`, `and`, `or`.
- **static methods** — `Function.identity()`, `Predicate.not()`.
- **`private` methods** (Java 9+) — shared helpers for the defaults.
- **public methods of `Object`** — `Comparator` redeclares `equals(Object)` as abstract and is still functional;
  every implementer inherits one from `Object`, so it doesn't count.

That last case is the one that catches people. `Comparator` looks like it has two abstract methods; the `Object`
rule means it has one.

</details>

### Two lambdas with identical bodies — are they equal?
<details><summary>Show answer</summary>

No. Nothing about lambda identity is specified.

```java
Runnable a = () -> {};
Runnable b = () -> {};
a.equals(b);      // false — distinct objects
a == b;           // false
a.getClass();     // some synthetic name, unspecified, e.g. Foo$$Lambda$14/0x...
```

Lambdas inherit `Object.equals`, so it's identity comparison. The class name is generated at runtime by
`invokedynamic` and is not a stable API — never parse it, switch on it, or persist it.

Two consequences that actually bite:
- **A lambda cannot be removed from a listener list unless you kept the reference.** `removeListener(x -> f(x))`
  passes a *new* object that equals nothing already registered.
- **A stateless lambda may or may not be a singleton.** The JVM is permitted to reuse one instance and often does,
  but it isn't guaranteed — so identity-based logic is broken in both directions.

</details>

### Can a lambda modify a local variable it uses?
<details><summary>Show answer</summary>

No — captured locals must be **final or effectively final**, meaning never reassigned after initialization.

```java
int count = 0;
list.forEach(x -> count++);        // compile error

int[] count = {0};
list.forEach(x -> count[0]++);      // compiles — the array reference is unchanged

List<String> seen = new ArrayList<>();
list.forEach(x -> seen.add(x));     // compiles — the reference is final, the object is not
```

**Why:** a local lives on the stack and dies with its method. A lambda can outlive that frame, so the value is
**copied** into it at capture. If reassignment were allowed, the two copies would silently diverge — the compiler
forbids it rather than mislead you.

**The distinction people miss:** the restriction is on the **variable**, not the object. Mutating what a captured
reference points at compiles fine, which is exactly why the `int[]` and `ArrayList` workarounds exist.

**But compiling is not the same as being correct.** Those workarounds defeat a rule that exists for a reason: run
the same code on a parallel stream and both are data races. The right move is a `reduce` or a `Collector`, not a
smuggled mutable box.

**Contrast with anonymous classes:** the rule is identical — the same capture semantics apply, and always have
(pre-Java-8 they required explicit `final`). Where they *do* differ is `this`: inside an anonymous class `this` is
the anonymous instance; inside a lambda `this` is the enclosing instance, since a lambda introduces no new scope.

</details>