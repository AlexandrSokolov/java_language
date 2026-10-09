# Collecting errors in a stream instead of aborting on the first one

**What this is:** a small, self-contained helper you paste into a project. It lets a stream pipeline run
several fallible steps per element, keep going when one element fails, and end with **every failure kept
as data** — with enough context to act on — instead of one exception killing the whole run.

**When you reach for it:** batch work where one bad element must not stop the rest — importing files,
calling a paginated API per item, parsing/validating a list, any "do N things, report what broke" loop.

**When you do not:** a single fallible call (plain `try/catch` is clearer), or where a failure genuinely
*should* abort everything (let it throw).

---

## The problem it solves

Any exception escaping a lambda kills the whole terminal operation. One bad element and the rest never
run, and you get no partial result:

```java
paths.stream()
    .map(p -> Files.readString(p))   // does not even compile: checked IOException in a lambda
    .toList();
```

Two things are wrong at once:

1. **Compile-time:** standard functional interfaces declare no `throws`, so a *checked* exception cannot
   leave a lambda body at all.
2. **Run-time:** even for an *unchecked* exception, the first throw aborts the terminal operation — no
   partial result, the remaining elements never run.

The helper below fixes both: the failure becomes a value that flows to the end of the pipeline, so the
terminal always completes and you get results and failures side by side.

It handles **checked and unchecked the same way** — the throwing interface declares `throws Exception`, so
one `catch (Exception e)` captures both. There is no separate "checked version" and "unchecked version".

---

## The library — copy this block into a project

Drop these two types somewhere shared. No dependencies, plain JDK.

```java
/** A function that is allowed to throw a checked exception. */
@FunctionalInterface
public interface ThrowingFunction<T, R> {
  R apply(T t) throws Exception;
}
```

```java
import java.util.Set;
import java.util.Collections;
import java.util.IdentityHashMap;

/** Stateless helpers for inspecting exceptions and their cause chains. */
public final class ExceptionUtils {

  private ExceptionUtils() {}   // no instances

  private static final Set<Class<? extends Throwable>> BUG_TYPES = Set.of(
      NullPointerException.class,
      IndexOutOfBoundsException.class,
      ClassCastException.class,
      IllegalStateException.class);

  /** True when the throwable is a programming error whose message won't help a non-developer. */
  public static boolean isBug(Throwable t) {
    return BUG_TYPES.stream().anyMatch(type -> type.isInstance(t));
  }

  /** Walk to the innermost cause — the exception that carries the real message. Cycle-safe. */
  public static Throwable rootCause(Throwable t) {
    Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    Throwable cause = t;
    while (cause.getCause() != null && seen.add(cause)) cause = cause.getCause();
    return cause;
  }

  /** Full cause chain, each link's throw site on its own line. Cycle-safe. */
  public static String describeChain(Throwable error) {
    StringBuilder sb = new StringBuilder();
    Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Throwable t = error; t != null && seen.add(t); t = t.getCause()) {
      StackTraceElement[] trace = t.getStackTrace();
      String at = trace.length > 0 ? " @ " + trace[0] : "";
      sb.append("\n  caused by: ").append(t).append(at);
    }
    return sb.toString();
  }
}
```


```java
import java.util.function.Supplier;

/**
 * Carries either a successful value or the exception that stopped it, plus the caller's context.
 * Chain fallible steps with map(): the first failure short-circuits the rest for that element.
 */
public record Result<T>(T value, Exception error, Supplier<String> context) {

  public static <T> Result<T> start(T value, Supplier<String> context) {
    return new Result<>(value, null, context);
  }

  public boolean isOk() { return error == null; }

  public <R> Result<R> map(ThrowingFunction<T, R> f) {
    if (error != null) return new Result<>(null, error, context);
    try {
      return new Result<>(f.apply(value), null, context);
    } catch (Exception e) {
      return new Result<>(null, e, context);
    }
  }

  /** For the queue reader / customer: context + root-cause message, or a support line for bugs. */
  public String message() {
    Throwable root = ExceptionUtils.rootCause(error);
    if (ExceptionUtils.isBug(root)) {
      return context.get() + " -> internal error, please contact support";
    }
    return context.get() + " -> " + root.getMessage();
  }

  /** For the developer: context + throw site of every link in the cause chain. */
  public String describe() {
    return context.get() + ExceptionUtils.describeChain(error);
  }
}
```

That is the whole library. Two files, ~30 lines.

---

## How the pieces work

**`start(value, context)`** enters the chain. `context` is a `Supplier<String>` the caller builds from its
*own* facts — which object, which run, whose request. Only the caller has this; the failing step never
does. It is a `Supplier` so the string is built only when a failure actually reads it, never on the success
path.

**`map(f)`** runs one step. If the element already failed, `map` skips the step and passes the error along
unchanged — so the first failure wins and no later step runs on broken input. If the element is still ok,
`map` runs the step and catches anything it throws, checked or unchecked.

**`describe()`** produces the failure line: the caller's context plus the exception's own message. The
exception says *what* broke; the context says *what the caller was doing*. Neither alone is enough when you
read the failure later out of context.

**No stage/step field is needed.** The exception's own type already tells you which step broke — an
`IOException` from a read step, an `IllegalArgumentException` from a parse step. A separate step label would
just repeat what the type already carries. (If two of your steps can throw the *same* type, add a step
label then — otherwise skip it.)

---

## Usage — the driver

Chain the fallible steps inside one `stream.map`, partition once, report the failures.

```java
record Job(int tenantId, String path) {}

List<Job> jobs = List.of(
    new Job(4471, "/cfg/ok.json"),
    new Job(9002, "/cfg/bad.json"),
    new Job(5310, "/cfg/parse.json"));

Map<Boolean, List<Result<Integer>>> byOutcome = jobs.stream()
    .map(job -> Result.start(job.path(), () -> "tenant " + job.tenantId() + " nightly run")
        .map(this::readFile)     // step 1 — may throw checked IOException
        .map(this::parseJson))   // step 2 — skipped if step 1 failed
    .collect(Collectors.partitioningBy(Result::isOk));

List<Integer> ok = byOutcome.get(true).stream().map(Result::value).toList();
List<String> failed4customers = byOutcome.get(false).stream().map(Result::message).toList();
List<String> failed4dev = byOutcome.get(false).stream().map(Result::describe).toList();

ok.forEach(v -> System.out.println("OK: " + v));
failed4customers.forEach(f -> System.out.println("FAIL: " + f));
failed4dev.forEach(f -> System.out.println("FAIL: " + f));
```

The two steps used above:

```java
String readFile(String path) throws Exception {           // checked exception
  if (path.contains("bad")) throw new IOException("cannot read " + path);
  return "content-of-" + path;
}

Integer parseJson(String s) throws Exception {             // could be checked or unchecked
  if (s.contains("parse")) throw new IllegalArgumentException("bad json in " + s);
  return s.length();
}
```

Output:

```
OK: 23
FAIL: tenant 9002 nightly run -> java.io.IOException: cannot read /cfg/bad.json
FAIL: tenant 5310 nightly run -> java.lang.IllegalArgumentException: bad json in content-of-/cfg/parse.json
```

Both failures carry the caller's context, and the two exception *types* differ — read-failure vs
parse-failure is clear with no step label. The success ran clean through both steps.

---

## Fail the batch on any failure (optional)

If a partly-failed batch should still raise one exception at the end — carrying *every* failure, not just
whichever was first — partition and throw after:

```java
List<String> failed = byOutcome.get(false).stream().map(Result::describe).toList();
if (!failed.isEmpty()) {
  throw new IllegalStateException(
      failed.size() + "/" + jobs.size() + " items failed:\n" + String.join("\n", failed));
}
```

One exception describing the whole batch, instead of one about whichever element happened to be first.

---

## Extending it

- **More step shapes:** `map` covers value→value. A step that consumes without returning, or a throwing
  predicate for filtering, needs its own method — a throwing predicate is the tricky one, because `false`
  ("does not belong") is a third outcome the two-slot record cannot hold; filter *outside* the `Result`
  chain in that case.
- **Plain `String` context** instead of `Supplier<String>` is fine below a few thousand elements — one
  character less to build per call site, at the cost of building the string even on success.
- **Keep NPEs fatal:** `catch (Exception e)` also catches `NullPointerException`, which is usually a bug,
  not bad data. If you want programming errors to stay fatal and only data failures collected, catch a
  narrower project-specific type instead of `Exception`.

---

## jshell quick test

To try it in jshell: put each type on its own paste, put `@FunctionalInterface` on the *same line* as the
interface, write the pipeline as one physical line (leading `.map` on a new line makes jshell think the
statement already ended), and use lambdas (`x -> readFile(x)`) instead of method references — top-level
jshell methods cannot be referenced with `::`.
</file_text>