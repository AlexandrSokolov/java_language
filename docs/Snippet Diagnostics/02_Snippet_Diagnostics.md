### Describe a code snippet 01
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
new Thread(System.out::println).start();

ExecutorService exec = Executors.newCachedThreadPool();
exec.submit(System.out::println);                 // Same argument (`System.out::println`)
```

</details>

<details><summary>Show answer</summary>

```java
new Thread(System.out::println).start();          // compiles

ExecutorService exec = Executors.newCachedThreadPool();
exec.submit(System.out::println);                 // does NOT compile
```

Same argument (`System.out::println`), both targets have a `Runnable` overload — yet the first compiles and the
second doesn't.

`submit` also has a `Callable<T>` overload; `Thread`'s constructor doesn't. You'd think it can't matter, since every
`println` returns `void` and so can't be a `Callable` — but resolution doesn't reason that way. `System.out::println`
is an *inexact* method reference (`println` is itself overloaded), so its meaning isn't known until a target type is
picked. With two functional-interface overloads competing for the same position, resolution can't choose, and it
fails to compile.

The tell: it would compile if `println` weren't overloaded, or if `submit` weren't. It takes *both* overloadings —
the referenced method and the invoked method — to break it.

Fix: disambiguate the reference, e.g. cast it to the intended type, or don't overload across functional interfaces
in the first place.

</details>

</details>
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
interface Report {
  String render();
  String renderAsHtml();
  String renderAsHtmlNoHeader();
  String renderAsHtmlNoFooter();
  String renderAsPlainText();
  void renderToFile(Path p);
}
```

</details>

<details><summary>Show answer</summary>

**The defect: convenience methods that don't pull their weight.** Every one of these is `render()` plus a format or
destination the caller could supply. The variants also head toward a combinatorial explosion (`NoHeader`, `NoFooter`,
`NoHeaderNoFooter`…). Too many methods bloat the type to learn, document, test, and maintain — and on an *interface*
each one also burdens every implementor.

**Fix: one fully-functional method per action; add a shorthand only if it'll be used often.** Collapse the format
variants into one parameterized method, drop what the caller can trivially do themselves:

```java
interface Report {
  String render(Format format);   // HTML / PLAIN_TEXT, header/footer as options on Format
}
// caller writes the file: Files.writeString(p, report.render(HTML));
```

When in doubt, leave it out.

</details>

</details>

### Describe a code snippet 02
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public double sum(ArrayList<Double> values) {
  double total = 0;
  for (double v : values) {
    total += v;
  }
  return total;
}
```

</details>

<details><summary>Show answer</summary>

**The defect: the parameter type is more specific than the method needs.** The body only *iterates* — it never
indexes, never calls an `ArrayList`-specific method. Yet the signature demands an `ArrayList`, so a caller holding a
`LinkedList`, a `Set`, a `List.of(...)`, or any other source must copy into an `ArrayList` first — an unnecessary,
possibly expensive conversion.

**Rule: favor interfaces over classes for parameter types.** Widen to the *least* specific type that supports what
the body does. Since this body only needs sequential access, the honest type is `Iterable<Double>`:

```java
public double sum(Iterable<Double> values) { ... }   // accepts List, Set, Collection, anything iterable
```

If it needed `size()` or `contains()`, `Collection` would be the floor; if it indexed, `List`. Never write `HashMap`
where `Map` works, or `ArrayList` where `Iterable` works — it locks callers to one implementation for no reason.

**Second issue: unboxing.** The source holds `Double`, and `+=` needs a `double`, so each element is opened once.
This is unavoidable here. Changing `double v` to `Double v` does not remove it — it just moves the unbox from the
loop variable to the `+=`. The box was created when the `Collection<Double>` was built; unboxing to compute is the
unavoidable other half, and no parameter type changes that.

**Why not take `DoubleStream` to dodge the boxing?** It does fix boxing for a primitive-holding caller, but at the
cost of flexibility: a stream is a single-use, already-running computation, not data — it can't be reused, can't be
passed twice, and forces every caller to build a pipeline before calling. It also breaks "accept the most general
*source* type," since a stream is a computation in flight, not a source. Small boxing win, real loss in flexibility
and convenience — not an option. The primitive-source case is served by a `double...` overload instead, which keeps
callers passing plain data.

**Handle:** accept *data* in its most general form, not a live pipeline — box cost is fixed by an overload, never by
narrowing the input to a stream.

</details>

</details>

### Describe a code snippet 03
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
void export(Report r, boolean asHtml) {
  if (asHtml) {
    // ...render and write HTML
  } else {
    // ...render and write plain text
  }
}
```

</details>

<details><summary>Show answer</summary>

**Two defects.** At the call site `export(r, true)` is opaque — `true` says nothing about what it selects. And the
body is a two-way branch on the flag, which is a sign the choice shouldn't be a parameter at all.

**Two fixes, for two situations:**

- **The flag switches between two behaviours** (this case). Don't pass the choice in — **split into two methods**,
  `exportHtml(r)` and `exportText(r)`. The branch disappears and each name states its intent.
- **The flag selects a mode that could grow.** Use a **two-element enum**, readable and extensible:

```java
enum Format { HTML, PLAIN_TEXT }                     // PDF can join later
void export(Report r, Format format);                // vs. export(r, true)
```

Exception: a bare `boolean` is fine when the method name already makes it obvious — `setVisible(true)`.

</details>

</details>

### Describe a code snippet 04
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public static String classify(Set<?> s)        { return "Set"; }
public static String classify(List<?> lst)      { return "List"; }
public static String classify(Collection<?> c)  { return "Unknown Collection"; }

public static void main(String[] args) {
  Collection<?>[] collections = {
    new HashSet<String>(),
    new ArrayList<BigInteger>(),
    new HashMap<String, String>().values()
  };
  for (Collection<?> c : collections)
    System.out.println(classify(c));   // prints what, three times?
}
```

</details>

<details><summary>Show answer</summary>

Prints `Unknown Collection` three times — not `Set`, `List`, `Unknown Collection`.

The loop variable's declared type is `Collection<?>`. Overload selection uses that static type and happens at compile
time, so the compiler picks `classify(Collection<?>)` once, for every iteration. The actual objects (`HashSet`,
`ArrayList`, the map's `values` view) differ at runtime, but runtime type does not steer overloading — so the other
two overloads are never reached.

Fix: if behavior must depend on the actual type, don't overload — use one method that tests the type inside, e.g.
`instanceof` branches, so the decision moves to runtime:
```java
public static String classify(Collection<?> c) {
  return c instanceof Set  ? "Set"
       : c instanceof List ? "List"
       :                     "Unknown Collection";
}
```

</details>

</details>

### Describe a code snippet 05
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
class Wine                             { String name() { return "wine"; } }
class SparklingWine extends Wine       { @Override String name() { return "sparkling wine"; } }
class Champagne     extends SparklingWine { @Override String name() { return "champagne"; } }

public class Overriding {
  public static void main(String[] args) {
    List<Wine> wineList = List.of(new Wine(), new SparklingWine(), new Champagne());
    for (Wine wine : wineList)
      System.out.println(wine.name());   // prints what?
  }
}
```

</details>

<details><summary>Show answer</summary>

Prints `wine`, `sparkling wine`, `champagne` — one per element.

`name()` is overridden, so which version runs is decided at runtime from each object's actual type. The loop
variable is declared `Wine`, but that static type is ignored here — the most specific override for the real object
always wins.

This is the exact mirror of the overloading trap: same loop shape, opposite outcome. Overriding follows the object
(runtime, dynamic type); overloading follows the declaration (compile time, static type). Put side by side, one
static-typed loop prints three different lines here and three identical lines there — that contrast *is* the two
dispatch rules.

</details>

</details>

### Describe a code snippet 06
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public void print(int... nums)     { System.out.println("varargs"); }
public void print(int a, int b)    { System.out.println("two-arg");  }

// call site
print(1, 2);   // which one?
```

</details>

<details><summary>Show answer</summary>

Prints `two-arg`, not `varargs`. When a fixed-arity overload matches, the compiler prefers it and skips the varargs
one — varargs is treated as the last resort. So the two-arg version silently wins for `print(1, 2)`, while
`print(1)` and `print(1, 2, 3)` fall to varargs. Same method name, three call shapes, two different targets — the
kind of "which one runs?" ambiguity overloading is supposed to avoid.

General rule: **don't overload a varargs method at all.** 
If you must, ensure no confusing call site exists — but distinct names are the clean fix here too.

</details>

</details>

### Describe a code snippet 07
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public int min(int... nums) {
  if (nums.length == 0)
    throw new IllegalArgumentException("min needs at least one argument");
  int min = nums[0];
  for (int n : nums)
    if (n < min) min = n;
  return min;
}
```

</details>

<details><summary>Show answer</summary>

The method needs at least one argument, and the length check enforces that — but at **runtime**. A call to `min()`
compiles cleanly and only fails when it runs. The check moves the failure earlier than the `nums[0]` crash, yet it's
still one call too late: the caller ships broken code that the compiler could have rejected.

The signature is lying — it says "zero or more" while the body means "one or more." A runtime check can't repair a
signature that promises the wrong thing.

Fix: split the first argument out as a required parameter, so "at least one" is stated in the type and enforced at
compile time:

```java
public int min(int first, int... rest) {
  int min = first;
  for (int n : rest)
    if (n < min) min = n;
  return min;
}
```

Now `min()` won't compile, the length check disappears, and the loop needs no special-casing for the first element.
General rule: when a varargs method needs a minimum count, encode it with leading required parameters — never a
runtime length check.

</details>

</details>

### Describe a code snippet 08
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Set<Integer> set = new TreeSet<>();
List<Integer> list = new ArrayList<>();
for (int i = -3; i < 3; i++) {
  set.add(i);
  list.add(i);
}
for (int i = 0; i < 3; i++) {
  set.remove(i);
  list.remove(i);
}
System.out.println(set + " " + list);
```

</details>

<details><summary>Show answer</summary>

Prints `[-3, -2, -1] [-2, 0, 2]` — the set and list diverge, though the removal calls look identical. 

**Two traps stack here.**

**Trap 1 — overload selection.**
- `set.remove(i)` has only one candidate, `remove(E)` = `remove(Integer)`, 
   so `i` autoboxes and the *value* is removed — the non-negative values go, as expected. 
- `list.remove(i)` has two overloads: `remove(int index)` and `remove(Object)`. 
  A bare `int` matches the fixed `int` overload with no boxing, so `list` removes *by position*, not by value.

**Trap 2 — the index shifts after each removal.** `remove(index)` deletes the element *currently* at that position,
and everything after slides left. So the three calls don't hit the first three elements — they skip:
```
start: [-3, -2, -1, 0, 1, 2]
remove(0) -> deletes -3 -> [-2, -1, 0, 1, 2]
remove(1) -> deletes -1 -> [-2, 0, 1, 2] (index 1 is now -1, not -2)
remove(2) -> deletes 1 -> [-2, 0, 2] (index 2 is now 1)
```
Every second element goes (`-3, -1, 1`), leaving `[-2, 0, 2]`. Trap 1 chooses the wrong method; trap 2 explains why
the leftovers look scrambled.

Fix — force the value overload by making the argument a reference type:

```java
list.remove((Integer) i);           // or Integer.valueOf(i)
```

Root cause: before generics, `List` had `remove(Object)` and `remove(int)` — radically different, safe. 
Generifying `Object` to `E` plus autoboxing collapsed that gap, so `int` and `Integer` now compete. 
The language change retro-damaged the interface — the concrete reason overloading needs extra caution once boxing exists.

</details>

</details>

### Describe a code snippet 09
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
new Thread(System.out::println).start();

ExecutorService exec = Executors.newCachedThreadPool();
exec.submit(System.out::println);                 // same argument as above
```

</details>

<details><summary>Show answer</summary>

```java
new Thread(System.out::println).start();          // compiles

ExecutorService exec = Executors.newCachedThreadPool();
exec.submit(System.out::println);                 // does NOT compile
```

Same argument (`System.out::println`), both targets accept a `Runnable` — yet the first compiles and the second does not. 
To see why, three sets of signatures matter.

**The called method — `Thread` has one relevant constructor, `submit` has two:**

```java
Thread(Runnable target)                           // one option, no fork

Future<?>     submit(Runnable task)               // submit has TWO functional-interface overloads
<T> Future<T> submit(Callable<T> task)
```

**The referenced method — `println` is heavily overloaded:**

```java
void println()                                    // System.out::println names a FAMILY, not one method
void println(int x)
void println(String x)
void println(Object x)
// ...and more
```

Resolution runs in two independent steps, and each overloading sits on a different one:

- **Step A — pick the target functional interface** (looks at the *called* method). 
  - `Thread`: forced to `Runnable`, no choice. 
  - `submit`: fork between `Runnable` and `Callable<T>`.
- **Step B — pick which `println`** (looks at the *referenced* method). 
  Only decidable once the target's shape is known (how many args, what types).

The rule that ties it together: 
**an inexact method reference — one whose own method is overloaded, 
so Step B isn't settled — is *not a pertinent argument* for overload resolution.** 
The compiler ignores it while doing Step A.

Walk both sites with that rule:

- **`Thread`** — one constructor, so Step A has no fork. The target is fixed as `Runnable`; 
  the compiler then knows the shape (no-arg, `void`) and picks `println()` at Step B. 
  The inexactness never mattered — nothing to disambiguate. **Compiles.**
- **`submit`** — Step A must choose `Runnable` vs `Callable`. 
  The only argument that could break the tie is `System.out::println`, but it's inexact, so the rules discard it. 
  With no usable argument, *both* overloads stay applicable → **ambiguous → compile error.** 
  (The real message: *"reference to submit is ambiguous, both `submit(Callable<T>)` and `submit(Runnable)` match."*)

**Either overloading alone is harmless — it takes both to break it:**

- Drop `submit`'s second overload → target forced to `Runnable` → resolves like the `Thread` case. Compiles.
- Make `println` non-overloaded → the reference is now *exact*, becomes a pertinent argument, carries a known
  `void`/no-arg signature, matches `Runnable` not `Callable`. Compiles.

**Correcting the common wrong model:** 
"overloading across functional interfaces just doesn't work" is false — 
`submit(Runnable)` / `submit(Callable)` coexist and are called successfully all the time. 
The `Thread` constructor isn't special; it simply has no second overload, which is the same as the "drop the overload" fix. 
Overloading across functional interfaces is not *broken*; 
it's a **landmine** that a specific argument shape — an inexact method reference — steps on. 
That's exactly why the guidance is to not overload across functional interfaces at all: 
not because it never works, but because you can't see, at the declaration site, 
which caller will hand you an argument the compiler can't pin down.


Fix at the call site: make the reference exact by giving it a target type, e.g.
`exec.submit((Runnable) System.out::println);` — 
or, better, follow the design rule and don't put two functional-interface overloads on the same method name.

</details>

</details>

### Describe a code snippet 10
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
new Thread(System.out::println).start();

ExecutorService exec = Executors.newCachedThreadPool();
exec.submit(System.out::println);                 // Same argument (`System.out::println`)
```

</details>

<details><summary>Show answer</summary>

```java
new Thread(System.out::println).start();          // compiles

ExecutorService exec = Executors.newCachedThreadPool();
exec.submit(System.out::println);                 // does NOT compile
```

Same argument (`System.out::println`), both targets have a `Runnable` overload — yet the first compiles and the
second doesn't.

`submit` also has a `Callable<T>` overload; `Thread`'s constructor doesn't. You'd think it can't matter, since every
`println` returns `void` and so can't be a `Callable` — but resolution doesn't reason that way. `System.out::println`
is an *inexact* method reference (`println` is itself overloaded), so its meaning isn't known until a target type is
picked. With two functional-interface overloads competing for the same position, resolution can't choose, and it
fails to compile.

The tell: it would compile if `println` weren't overloaded, or if `submit` weren't. It takes *both* overloadings —
the referenced method and the invoked method — to break it.

Fix: disambiguate the reference, e.g. cast it to the intended type, or don't overload across functional interfaces
in the first place.

</details>

</details>

### Describe a code snippet 11
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public List<Cheese> getCheeses() {
  return cheesesInStock.isEmpty()
          ? null
          : new ArrayList<>(cheesesInStock);
}
```

</details>

<details><summary>Show answer</summary>

Returns `null` for the empty case — don't. 
Every caller must now write `if (cheeses != null && …)`, and one missed guard is a latent NPE. 
There is never a reason to return `null` in place of an empty list or array.

Fix — just return the list, empty or not:

```java
public List<Cheese> getCheeses() {
  return new ArrayList<>(cheesesInStock);   // mutable copy, empty or not — one uniform contract
}
```

**The tempting "optimization" that quietly breaks the contract.** 
To avoid allocating an empty list, you might reach for the shared empty singleton:

```java
return cheesesInStock.isEmpty()
    ? Collections.emptyList()               // IMMUTABLE
    : new ArrayList<>(cheesesInStock);      // MUTABLE
```

This is a defect, not an optimization. 
The method now returns a **mutable** list almost always, but an **immutable** one on the days stock is empty. 
A caller who does `getCheeses().add(x)` works all year, 
then throws `UnsupportedOperationException` the first time the shop runs out — 
a data-dependent bug that hides far longer than the NPE you removed. 
`Collections.emptyList()` is empty *and* immutable; it is not a drop-in for "empty ArrayList."
Never mix mutable and immutable returns on one method.


**If you want the allocation saved, make BOTH branches immutable — decide the contract, don't stumble into it:**

```java
return List.copyOf(cheesesInStock);         // immutable in both cases; empty input returns the shared empty singleton
```

`List.copyOf` (Java 10+) returns an unmodifiable list, 
and for an empty input it hands back the same cached empty instance — 
so you get the no-allocation win *and* a uniform contract, in one call. 
This is the modern replacement for the dated `Collections.emptyList()` fallback.


**When returning immutable makes sense:** when the returned collection is meant to be **read-only** — 
a defensive snapshot the caller inspects but must not edit. 
A getter handing out a copy of internal state is exactly this case:
no caller should be mutating a defensive copy anyway, so immutable is the honest type. 
Return **mutable** only when the caller is genuinely expected to own and modify the returned list.


**Copy vs. view — a related trap, different contract:**

```java
Collections.unmodifiableList(myList)        // read-only VIEW — no copy, but your later changes to myList SHOW THROUGH
List.copyOf(myList)                         // read-only SNAPSHOT — independent; later changes do NOT show through
```

A view is *read-only but live*, not "a cheaper copy." 
Use a view when the caller should see current data cheaply without editing it; 
use a copy when the caller needs a stable snapshot decoupled from your internal state. 
Using a view where you meant snapshot leaks your later mutations; 
using a copy where you meant live wastes allocation and hides updates.

</details>

</details>

### Describe a code snippet 12
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Optional<ProcessHandle> parent = ph.parent();
System.out.println("Parent PID: " +
  (parent.isPresent() ? String.valueOf(parent.get().pid()) : "N/A"));
```

</details>

<details><summary>Show answer</summary>

Works, but `isPresent()` + `get()` is the verbose, manual form. 
Replace it with `map` + `orElse`, which transforms the value if present and supplies the fallback in one expression:

```java
System.out.println("Parent PID: " +
  ph.parent()
    .map(ProcessHandle::pid)      // ProcessHandle -> long
    .map(String::valueOf)         // long -> String
    .orElse("N/A"));
```

`isPresent` is a safety valve — legal, but most of its uses have a shorter, 
clearer replacement (`map`, `filter`, `flatMap`, `ifPresent`, or `or`). 
Reach for those first; drop to `isPresent` only when none fit.

</details>

</details>

### Describe a code snippet 13
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Stream<Optional<String>> stream = ...;
List<String> present = stream
    .filter(Optional::isPresent)
    .map(Optional::get)
    .collect(Collectors.toList());
```

</details>

<details><summary>Show answer</summary>

Works, but it's the Java 8 two-step: `filter` drops the empty ones, `map` unwraps the rest. 
Since Java 9, `Optional` has a `stream()` method that returns a stream of one element if present, zero if empty — 
so `flatMap` does both steps at once:

```java
List<String> present = stream
    .flatMap(Optional::stream)
    .collect(Collectors.toList());
```

Each `Optional` becomes a 0-or-1-element stream, and `flatMap` concatenates them, so the empties contribute nothing
and the present values flow through unwrapped.

Trade-off worth knowing: `filter(Optional::isPresent).map(Optional::get)` spells out the intent literally — 
"keep the present ones, then unwrap." `flatMap(Optional::stream)` is shorter, 
but you must know that `Optional::stream` emits zero or one element to see why the empties vanish. 
Prefer the `flatMap` form as the standard idiom (Java 9+); 
it's the one reviewers expect, but the mechanism isn't self-evident the way the filter/map pair is.

</details>

</details>