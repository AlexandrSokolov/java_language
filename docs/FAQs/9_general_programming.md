
### How do you keep a local variable's scope small?
<details><summary>Show answer</summary>

Declare it on the line where it is first used, together with its initializer. The scope of a local runs from
its declaration to the end of the enclosing block, so moving the declaration down is what shrinks the scope —
nothing else does.

Declaring at the top of a method gives the variable the whole method as its scope: every later line can read it
or reassign it, and the reader carries the name, the type, and the starting value down the page until something
finally uses them.

```java
// Bad — one declaration block at the top; all three live for the whole method
Customer customer;
Order order;
double total;

customer = repository.findCustomer(id);
// ...many lines...
order = customer.currentOrder();
total = order.sum();

// Good — each variable starts where it is needed
Customer customer = repository.findCustomer(id);
// ...many lines...
Order order = customer.currentOrder();
double total = order.sum();
```

</details>

### When does a local variable earn its name?
<details><summary>Show answer</summary>

Keep the name when it says something the expression does not, or when the value is used more than once:

```java
boolean eligible = age >= 18 && !suspended && hasVerifiedEmail;  // the name states the concept
if (eligible) { ... }
```

Drop it when the name only repeats the call it came from — `findUser` already says the result is a user:

```java
// Adds nothing; the reader now tracks two names down to the last line
var user = findUser(id);
var order = findOrder(orderId);
return handleOrder(user, order);

// The call site shows the whole thing at once
return handleOrder(
  findUser(id),
  findOrder(orderId));
```

This is the strongest form of scope reduction: a value that is never named has no scope to reduce.

One real cost: pass-through bindings are often kept as debugger breakpoints. Inlining removes that, so you step
into the call instead of reading the value in the frame.

</details>

### What does a local variable cost the reader?
<details><summary>Show answer</summary>

The reader must hold the name, its type, and its current value in his head from the declaration until the last
use — and he does not know where that last use is until he reaches it. Five variables in a method means five
slots held open across the whole method.

A method call costs nothing to hold. The reader reads the name, takes the result, and drops it on the same line.

```java
// Two names held open to the last line, only to be passed along
var user = findUser(id);
var order = findOrder(orderId);
return handleOrder(user, order);

// Nothing held: each value is produced and consumed in place
return handleOrder(
  findUser(id),
  findOrder(orderId));
```

Three ways to close the slot instead of holding it open: put the expression at its use site, run the value
through a stream, or move the work into a named method.

Keep a variable when the value is used more than once, or when the order of the calls must be visible to the reader.

</details>

### How should a local variable be initialized?
<details><summary>Show answer</summary>

Give it its value in the declaration itself. If there is not enough information to set a sensible value yet,
move the declaration down to the point where there is — a name with no value is a slot the reader holds open
with nothing in it.


If a variable is initialized to an expression whose evaluation can throw a checked exception, 
the variable must be initialized inside a try block (unless the enclosing method can propagate the exception). 


If the value must be used outside of the try block, then it must be declared before the try block, 
where it cannot yet be "sensibly initialized."

</details>

### Walking a collection — which loop form?
<details><summary>Show answer</summary>

The for-each form, for both arrays and collections. It is the only one of the three that says nothing but
"take each element."

```java
// Bad — the iterator is written three times just to hand you e
for (Iterator<Element> i = c.iterator(); i.hasNext(); ) {
  Element e = i.next();
}

// Bad — the counter is written four times just to reach a[i]
for (int i = 0; i < a.length; i++) {
  ...a[i]...
}

// Good — same shape for an array or a collection
for (Element e : elements) {
  ...
}
```

Each repeat of the iterator or the counter is a chance to use the wrong one, and the compiler does not always
catch it. The two bad forms also look nothing alike, so switching a list for an array means rewriting the loop.
For-each costs nothing at runtime — it compiles to the same code you would have written by hand.

A stream is usually the better answer than any of them: it drops the loop too, leaving only what you do to each element.

</details>

### When can for-each not be used?
<details><summary>Show answer</summary>

For-each hands you the element and nothing else — no iterator, no position. Three jobs need one of those:

**Removing while walking** — for-each holds an iterator you cannot reach, and removing behind its back breaks it:

```java
for (Element e : c) {
  if (e.isStale()) {
    c.remove(e);      // the iterator notices the size changed -> ConcurrentModificationException
  }
}

for (Iterator<Element> i = c.iterator(); i.hasNext(); ) {
  if (i.next().isStale()) {
    i.remove();       // removing through the iterator keeps it valid
  }
}

c.removeIf(Element::isStale);   // one call, no loop
```

The exception is best-effort: the check is not guaranteed to fire, so code like this can also just corrupt the
walk silently.

**Replacing values in place** — for-each gives you a copy of the reference, so assigning to it changes nothing:

```java
for (String name : names) {
  name = name.trim();   // rebinds the local only; the list still holds the old value
}

for (int i = 0; i < names.size(); i++) {
  names.set(i, names.get(i).trim());   // writing back needs the position
}

names.replaceAll(String::trim);   // one call, no loop
```

**Walking two collections in lockstep** — one element from each per step, both advanced together:

```java
for (String name : names) {
  register(name, ???);   // no way to reach the matching element of scores
}

for (int i = 0; i < names.size(); i++) {
  register(names.get(i), scores.get(i));   // position i in both lists
}
```

</details>


### Which type for money calculations?
<details><summary>Show answer</summary>

Not `float` or `double`. They cannot hold 0.1 exactly, so results drift: `1.03 - 0.42` prints `0.6100000000000001`. 
Rounding on print does not help — the error is already in every intermediate value. 
Same for `Float` and `Double`: the wrappers hold the same binary format.


`int` or `long` in cents is exact, and fast. It works only while you stay on add, subtract, and compare, and
only while the number fits: nine digits for `int`, eighteen for `long`. You track the decimal point yourself.


`BigDecimal` is the safe default. It holds the scale for you, and division takes a rounding mode as an argument,
so percentages, interest, and a legally fixed rounding rule are covered.


Why `BigDecimal` over `long` even when both are exact: the `long` version depends on nobody ever dividing, and
that condition is written nowhere. The first person who needs to split a total reaches for the type that
divides, and `double` is right there. `BigDecimal` closes that path. What it costs is allocation on every
operation, which shows up at millions of operations and nowhere else.


See: [What can go wrong with BigDecimal?](#what-can-go-wrong-with-bigdecimal)

</details>

### What can go wrong with BigDecimal?
<details><summary>Show answer</summary>

**Building one from a `double`.** `new BigDecimal(0.009)` gives `0.008999999999999999319...` — the `double` was
already wrong before the constructor saw it, and the constructor copies it faithfully. Use
`new BigDecimal("0.009")`, or `BigDecimal.valueOf(0.009)`, which goes through `Double.toString` and gives the
short decimal you meant.

**`equals` compares the scale too.** `new BigDecimal("2.50").equals(new BigDecimal("2.5"))` is `false`. That is
what `HashMap`, `HashSet`, `distinct()`, and `contains` use, so the same amount lands in two buckets. Use
`compareTo` for comparing, and `stripTrailingZeros` before using one as a key.

**`divide` without a rounding mode throws.** `ONE.divide(new BigDecimal("3"))` fails at runtime with
`ArithmeticException` — the result does not terminate and the class refuses to round on its own. Pass a scale
and a `RoundingMode`.

</details>

### Primitive vs boxed — what differs?
<details><summary>Show answer</summary>

**Identity.** A primitive has only its value. A boxed type is an object, so it also has an identity — which
object it is. Two boxed objects can hold the same value and still be two objects.

**One extra value.** A boxed type can be `null`. A primitive has only real values.

**Cost.** A boxed value is an object on the heap: more memory, and every operation on it goes through an
allocation and a method call instead of a machine instruction.

</details>

### What does the primitive/boxed difference break?
<details><summary>Show answer</summary>

**Identity → `==` compares objects.** With both sides boxed, `==` asks "same object", not "same value". Small
values come from a cache and look right; larger ones do not:

```java
Integer a = 1000, b = 1000;
a == b        // false
```

**`null` → unboxing throws.** Any operation needing a primitive unboxes the boxed side — comparison, arithmetic,
a condition, an array index, assignment. A `null` there throws, often far from where the `null` came from:

```java
Integer i = null;   // an unassigned field is null too
if (i == 42) { }    // i.intValue() -> NPE
```

Note the pair: with two boxed operands `==` does not unbox; with one primitive operand it does. Same operator,
opposite behaviour, decided by the other side.

**Cost → a new object per operation.** Boxed types are immutable, so `+=` cannot change the object — it unboxes,
adds, and builds a new one. Harmless once, one object per iteration in a loop:

```java
Long sum = 0L;
sum += i;           // sum = Long.valueOf(sum.longValue() + i);
```

</details>

### Where does the boxing trap hide?
<details><summary>Show answer</summary>

**A field you never assigned.** A boxed field starts at `null`, like any other object field — nothing warns you,
and the throw happens wherever it is first used:

```java
static Integer i;
if (i == 42) { }    // NPE, and nothing above this line looks wrong
```

**One expression where the operator switches behaviour.** `<` unboxes both sides; `==` on two boxed operands
compares objects. Two lines apart, same variables, and the first one works:

```java
Comparator<Integer> c = (i, j) -> (i < j) ? -1 : (i == j ? 0 : 1);   // == compares objects
```

Sorting passes: the `==` branch only runs on equal values, which is the case where it is wrong.

**One wrong letter in a declaration.** Compiles clean, no warning, and the loop builds one object per iteration:

```java
Long sum = 0L;      // long was meant
```

</details>

### When is String a poor substitute for a real type?
<details><summary>Show answer</summary>

Strings model text. Data that arrives as a string (file, network, keyboard input) is not always text — keeping it as
a string is right only when the data really is textual.

- Numeric data → `int`, `float`, `BigInteger`.
- A yes/no answer → `boolean` or an `enum`.
- Any value with a fitting type, primitive or object → use that type; if none exists, write one.

Handle: if a proper value type fits, `String` is a lazy substitute for it.

</details>

### `String` parameters — what do they fail to enforce?
<details><summary>Show answer</summary>

A `String` parameter accepts any string, so the signature says nothing about what's actually valid. `void
transfer(String from, String to, String amount)` compiles even if you swap the arguments or pass a plain word for
the amount — the type carries no meaning and the compiler can't reject a wrong value.

A dedicated type fixes both: `transfer(AccountId from, AccountId to, Money amount)` states what each slot is, and the
compiler rejects anything else — you can't pass an `AccountId` where `Money` is expected.

Scala can name the intent with a type alias (`type AccountId = String`), which helps reading and maintenance — the
call site now says what the value is. But an alias is still the same underlying type, so it does not *enforce*
anything: a raw `String`, or an `OrderId`, still passes. For real enforcement Scala uses opaque types or value
classes; Java has no alias, so its wrapper type gives readability and enforcement together.

Handle: `String` documents nothing and blocks nothing — a named type is where readability and compiler checking live.

</details>

### What to consider when joining strings?
<details><summary>Show answer</summary>

`String` is immutable, so `a + b` can't modify either operand — it allocates a new `String` holding the result. 
Join a fixed few and that one allocation is fine; it reads best:

```java
String name = first + " " + last;
```

The cost only shows up when you repeat `+` in a loop:

```java
String result = "";
for (String line : lines)
  result += line;             // new String every iteration
```

Concatenating `n` strings with `+` in a loop is O(n²): 
each `+` copies the whole result so far into a new object, so the growing prefix is recopied on every step.

The fix is `StringBuilder`: a mutable buffer you `append` to, producing the final `String` once at the end:

```java
StringBuilder sb = new StringBuilder();
for (String line : lines)
  sb.append(line);
String result = sb.toString();
```

This is the standard pattern for an immutable type — pair it with a mutable companion (here `String` /
`StringBuilder`) so bulk building happens in one place instead of allocating at every step.

Handle: `+` for a fixed few, `StringBuilder` for a loop — immutability makes repeated `+` quietly quadratic.

</details>

### Building a log message — what to weigh?
<details><summary>Show answer</summary>

`logger.debug("cost = " + expensive())` builds the message even when debug is off — the `+` runs before `debug` is
called, so a disabled level still pays for the string. The old fix guards it:

```java
if (logger.isDebugEnabled())
  logger.debug("cost = " + expensive());
```

This works but is verbose. Deferring with a lambda is cleaner — the message is built only if the level is on. Both
frameworks now support it, but differently.

log4j2 overloaded the existing method, so you pass a lambda to the same `debug` you already use:

```java
logger.debug(() -> "cost = " + expensive());
```

SLF4J (since 2.0) does *not* add a `Supplier` overload to `debug(...)` — it puts lambda support on a separate fluent
API instead:

```java
logger.atDebug().log(() -> "cost = " + expensive());
```

SLF4J avoided the overload on purpose: adding a functional overload to `debug` on an interface that countless
implementations already depend on risks call ambiguity and binary-compatibility breakage. The result is worse to
use — you can't just hand a lambda to `debug`; you go through `atDebug().log(...)`.

Handle: don't build a message the level will discard — guard it, or defer it with a lambda (`debug(() -> ...)` in
log4j2, `atDebug().log(() -> ...)` in SLF4J).

</details>

### What should a variable's declared type be?
<details><summary>Show answer</summary>

Declare variables, parameters, returns, and fields as the interface type, not the implementing class — use the class
only to construct. `List<String> names = new ArrayList<>();`, not `ArrayList<String> names`.

Naming the implementation instead couples your code to it, and you lose what decoupling buys:

- **Free to swap** — change the implementation at the `new` call; declarations stay untouched.
- **Independent evolution** — code depends on the contract, not on one class's internals.
- **Safe to change the implementation** — no caller silently relied on class-specific behavior.
- **Reuse across contexts** — callers coupled to the contract accept any implementation.

Exception: no suitable interface exists (e.g. `String`, value classes), or the needed guarantee has no interface for
it — then the class type is the honest declaration.

Handle: program to the contract; name the class only where you create the object or need what only that class promises.

</details>

### What does reflection cost you?
<details><summary>Show answer</summary>

Reflection gives programmatic access to a class known only at runtime — construct instances, invoke methods, read
fields from a `Class` object. The price:

- **No compile-time checks** — a wrong or missing method fails at runtime, not at compile. 
  Exception checking is gone too.
- **Clumsy, verbose code** — reflective access is tedious to write and hard to read.
- **Slower calls** — a reflective invocation runs far slower than a direct one.

Handle: you trade every compile-time guarantee and normal call syntax for the ability to reach a class you didn't
know at compile time.

</details>

### When is reflection the right tool?
<details><summary>Show answer</summary>

Rarely. It's for the narrow case where a program must work with classes not known at compile time.

- **System-level tools** — code analysis tools, dependency-injection frameworks. Even these have been moving off it.
- **Running against multiple versions of another library** — compile against the oldest baseline, reach newer
  classes or methods reflectively, and fall back if they're absent at runtime.

Rule of thumb: if you doubt whether you need reflection, you don't.

Handle: reflection is a system-programming tool for classes-unknown-at-compile-time — not something ordinary
application code should reach for.

</details>

### How do you use reflection when forced to?
<details><summary>Show answer</summary>

Split it into two phases and keep reflection out of the second:

1. Use reflection **only to create** the object — turn the runtime class name into an instance.
2. Assign it to an **interface or superclass known at compile time**, then call everything normally through that.

Reflection touches one line; the rest of the program is ordinary typed code.

```java
// class name arrives at runtime, but we know it's a Set
Class<? extends Set<String>> cl =
  (Class<? extends Set<String>>) Class.forName(args[0]);
Set<String> s = cl.getDeclaredConstructor().newInstance();  // reflection ends here

s.addAll(rest);   // normal calls through the interface
```

The costs — no compile checks, verbose code, slow calls — hit only the instantiation line. Once you hold `s` as a
`Set`, it's indistinguishable from any other `Set`.

Handle: reflect to build, interface to use — never reflect to call methods you could call through a known type.

</details>

### What does JNI cost you?
<details><summary>Show answer</summary>

JNI lets Java call native methods written in C or C++. The price:

- **Memory unsafe** — native code isn't bound by Java's safety, so you're exposed to memory corruption again. One
  native bug can corrupt the whole process.
- **Less portable** — native code is more platform-dependent than Java.
- **Harder to debug** — problems cross the language boundary.
- **Can hurt performance** — the GC can't track native memory, and crossing into and out of native code costs.
- **Glue code** — tedious to write, hard to read.

Recommendation: use native methods judiciously. Keep native code minimal, test it hard, since a single native bug
sinks the whole application.

Handle: JNI trades Java's memory safety, portability, and debuggability for reaching native code — pay it only when
nothing else reaches.

</details>

### When is JNI still legitimate?
<details><summary>Show answer</summary>

Two motivations survive:

- **Platform-specific facilities** — reaching OS features Java didn't expose. Rarely needed now: the platform matured
  and covers most of them (e.g. the process API for OS processes).
- **Native libraries with no Java equivalent** — when the capability only exists as native code.

Performance is rarely a reason anymore — JVMs got fast enough to match native for most work. The exception is a
genuinely top-tier native library (e.g. GMP for heavy multiprecision arithmetic) that Java can't match.

Handle: reach for JNI to get *access* you can't get in Java — a platform feature or a native-only library — not for
routine speed.

</details>

### How should you approach optimization?
<details><summary>Show answer</summary>

Write good programs, not fast ones — a clean architecture can be optimized later. 
So don't optimize by guessing or too early.


The exception is the boundaries clean architecture can't hide — **APIs, wire protocols, persistent data formats**.
They're public contracts other code and systems depend on, so once shipped they can't change without breaking every
consumer — design their performance right the first time.


Once the system is built and clear, if it's not fast enough:

1. **Measure** — intuition about where time goes is unreliable.
2. **Profile** to find the actual hotspot.
3. **Fix the algorithm first** — no low-level tuning saves a quadratic algorithm.
4. **Re-measure** after every change; repeat until satisfied.

Handle: design the hard-to-change boundaries with performance in mind, otherwise don't optimize until you've measured
— then algorithm before micro-tuning.

</details>

### How should Java identifiers be named?
<details><summary>Show answer</summary>

**Typographical** (near-absolute, rarely break):

- **Packages** — lowercase, dot-separated, reversed domain: `com.google.common.collect`.
- **Classes / interfaces / enums / annotations** — `PascalCase`: `LinkedHashMap`. Acronyms capitalize first letter
  only: `HttpUrl`, not `HTTPURL`.
- **Methods / fields** — `camelCase`: `ensureCapacity`.
- **Constants** (`static final`, immutable value) — `UPPER_SNAKE`: `MIN_VALUE`. The only place underscores belong.
- **Type parameters** — one letter: `T` type, `E` element, `K`/`V` map, `X` exception, `R` return.

**Grammatical** (looser):

- **Instantiable class** — singular noun: `ChessPiece`. **Utility class** — plural: `Collectors`.
- **Interface** — noun, or `-able`/`-ible`: `Runnable`.
- **Action method** — verb: `drawImage`. **`boolean` method** — `is`/`has` prefix: `isEmpty`.
- **Accessor** — noun or `get`-prefix: `size()`, `getTime()`.
- **Conversions** — `toType` returns a new independent object (`toString`, `toArray`); `asType` returns a view backed
  by the original (`asList`); `typeValue` returns a primitive of the same value (`intValue`).
- **Static factories** — `from`, `of`, `valueOf`, `getInstance`, `newInstance`.

Handle: typographical rules are near-absolute; grammatical ones follow common sense.

</details>