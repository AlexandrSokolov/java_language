### Describe this interface — method count
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

### Analyze this signature — parameter type
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

</details>

</details>

### Describe this method — boolean parameter
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

### Describe a code snippet #N
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
`instanceof` branches, so the decision moves to runtime.

</details>

</details>

### Describe a code snippet #N
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

### Describe a code snippet #N
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

The deeper trap: a no-arg call. Adding `print()` resolves to the varargs overload with an **empty** array — easy to
invoke by accident and hard to spot. Guard it with a leading required parameter, e.g. `print(int first, int... rest)`,
so an empty call won't compile.

General rule: **don't overload a varargs method at all.** If you must, ensure no confusing call site exists — but
distinct names are the clean fix here too.

</details>

</details>

### Describe a code snippet #N
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
  list.remove(i);   // same call — same result?
}
System.out.println(set + " " + list);
```

</details>

<details><summary>Show answer</summary>

Prints `[-3, -2, -1] [-2, 0, 2]` — the set and list diverge, though the removal calls look identical.

`set.remove(i)` has only one candidate, `remove(E)` = `remove(Integer)`, so `i` autoboxes and the *value* is removed
— the non-negative values go, as expected.

`list.remove(i)` has two overloads: `remove(int index)` and `remove(Object)`. A bare `int` matches the fixed
`int` overload with no boxing, so it removes *by position*, not by value. Removing index 0, then 1, then 2 from
`[-3,-2,-1,0,1,2]` leaves `[-2, 0, 2]`.

Fix — force the value overload by making the argument a reference type:

```java
list.remove((Integer) i);           // or Integer.valueOf(i)
```

Root cause: before generics, `List` had `remove(Object)` and `remove(int)` — radically different, safe. Generifying
`Object` to `E` plus autoboxing collapsed that gap, so `int` and `Integer` now compete. The language change
retro-damaged the interface — the concrete reason overloading needs extra caution once boxing exists.

</details>

</details>

### Describe a code snippet #N
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
new Thread(System.out::println).start();          // compiles

ExecutorService exec = Executors.newCachedThreadPool();
exec.submit(System.out::println);                 // does NOT compile — why?
```

</details>

<details><summary>Show answer</summary>

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

### Describe a code snippet #N
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public List<Cheese> getCheeses() {
  return cheesesInStock.isEmpty() ? null           // what's wrong?
                                   : new ArrayList<>(cheesesInStock);
}
```

</details>

<details><summary>Show answer</summary>

Returns `null` for the empty case — don't. Every caller must now write `if (cheeses != null && …)`, and one missed
guard is a latent NPE.

Fix — just return the list, empty or not:

```java
public List<Cheese> getCheeses() {
  return new ArrayList<>(cheesesInStock);
}
```

If (and only if) profiling shows the empty-list allocation hurts, return a shared immutable empty instead:
`cheesesInStock.isEmpty() ? Collections.emptyList() : new ArrayList<>(cheesesInStock)`.

</details>

</details>

### isPresent or map — which?
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Optional<ProcessHandle> parent = ph.parent();
System.out.println("Parent PID: " +
  (parent.isPresent() ? String.valueOf(parent.get().pid()) : "N/A"));   // improve this
```

</details>

<details><summary>Show answer</summary>

Works, but `isPresent()` + `get()` is the verbose, manual form. Replace it with `map` + `orElse`, which transforms
the value if present and supplies the fallback in one expression:

```java
System.out.println("Parent PID: " +
  ph.parent().map(h -> String.valueOf(h.pid())).orElse("N/A"));
```

`isPresent` is a safety valve — legal, but most of its uses have a shorter, clearer replacement (`map`, `filter`,
`flatMap`, `ifPresent`, or `or`). Reach for those first; drop to `isPresent` only when none fit.

One more idiom: to collect the present values out of a `Stream<Optional<T>>`, use `flatMap(Optional::stream)`
(Java 9+) instead of `filter(Optional::isPresent).map(Optional::get)`.

</details>

</details>