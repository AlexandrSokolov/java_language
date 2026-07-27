
### Describe a code snippet #X
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
try {
  int i = 0;
  while (true)
    range[i++].climb();
} catch (ArrayIndexOutOfBoundsException e) {
}
```

</details>

<details><summary>Show answer</summary>

Loops over `range` by running off the end and catching the resulting `ArrayIndexOutOfBoundsException` to stop.
Exceptions are driving ordinary control flow — the defect. Three reasons it's wrong:

1. **Unreadable** — you can't tell it's a loop by looking. That alone is enough to reject it.
2. **Slower, not faster** — the point was to skip the bounds check, but a `try` block blocks JVM optimizations,
   exceptions aren't tuned for speed (they're meant to be rare), and the standard loop's checks are often
   optimized away anyway. Net result is slower.
3. **Hides real bugs** — if any call inside the loop throws `AIOOBE` from an unrelated array, this `catch`
   swallows it and reads it as a normal loop end. A real bug disappears instead of producing a stack trace.

Correct form — the standard idiom, instantly recognizable:

```java
for (Mountain m : range)
  m.climb();
```

Rule: exceptions are for exceptional conditions only, never for ordinary control flow. Prefer standard idioms
over clever tricks that claim better speed — the speed gain may not survive the next JVM, the maintenance cost
will.

</details>

</details>

### Describe a code snippet #X
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
range.stream()
  .forEach(Mountain::climb);  // climb() does the work — it's the computation
```

</details>

<details><summary>Show answer</summary>

Works, but a stream is the wrong tool. The correct form:

```java
for (Mountain m : range)
  m.climb();
```

1. **`forEach` — mostly for reporting** — its common use is reporting a pipeline's result (print, save each element). 
   It may also do per-element work with a side effect. What it must never do is build a single combined result 
   across elements — that's `collect` or `reduce`'s job, and doing it in `forEach` breaks under `parallel`.
2. **No pipeline to justify the machinery** — no `map`, `filter`, or `collect`, nothing coming out. You pay for
   the `Stream` and `forEach` dispatch and get none of what streams are for. A for-each just walks the array.
3. **Readability — the only real axis — is worse** — `stream().forEach(...)` signals "a transformation is
   happening" and then none is. The loop says exactly what happens with less to parse.

The tell: **is a value coming out?** Transform, filter, count, collect → stream. Just running an effect on each
element → loop. Here nothing comes out, so it's a loop.

</details>

</details>