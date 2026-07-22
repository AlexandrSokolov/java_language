
### Describe a code snippet #X
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Comparator<Integer> naturalOrder =
    (i, j) -> (i < j) ? -1 : (i == j ? 0 : 1);

System.out.println(naturalOrder.compare(1000, 1000));
```

</details>

<details><summary>Show answer</summary>

Prints `1` — the comparator reports the first value as greater than the second.

**The defect.** `i < j` unboxes both sides and works. `i == j` has two boxed operands, so it compares objects,
not values. Two different `Integer` objects holding 1000 are not the same object, so the branch is skipped and
the method falls through to `1`.

**Why it hides.** `Integer.valueOf` reuses cached objects for small values (default −128..127), so
`compare(1, 1)` returns 0 and the card looks fine. It fails only outside the cache — and the cache bound is
raisable with `-XX:AutoBoxCacheMax`, so the same code can behave differently on two JVMs. Sorting also hides it:
a sort that never asks about two equal values never runs the broken branch.

**Not a defect, but does not belong.** Writing this comparator at all — `Comparator.naturalOrder()` is the
answer for a type's own ordering, and `Comparator.comparingInt(...)` for a key.

**Minimal fix** — unbox first, then every comparison is on primitives:

```java
Comparator<Integer> naturalOrder = (iBoxed, jBoxed) -> {
  int i = iBoxed, j = jBoxed;
  return i < j ? -1 : (i == j ? 0 : 1);
};
```

**What to keep** — do not hand-write it:

```java
Comparator<Integer> naturalOrder = Comparator.naturalOrder();
Comparator<Integer> byValue = Integer::compare;   // if a Comparator<Integer> is what you need
```

The minimal fix leaves a hand-rolled three-branch comparator that the next reader must verify again; the library
call cannot be got wrong.

</details>

</details>