### When to lazily initialize, and which idiom?
<details><summary>Show answer</summary>

Default to normal initialization. Go lazy only for a measured performance gain, or to break a harmful
initialization circularity.

```java
private final FieldType field = computeFieldValue();   // normal — prefer this
```

Once you must go lazy, pick by case:

- Instance field, for performance → [double-check idiom](#lazy-instance-field-for-performance)
- Static field, for performance → [holder class idiom](#lazy-static-field-for-performance)
- Breaking a circularity → [synchronized accessor](#breaking-an-initialization-circularity)
- Instance field that tolerates repeated init → [single-check idiom](#lazy-field-that-tolerates-repeated-init)

Trigger: normal by default; lazy needs a reason (speed or circularity); then instance vs static, and whether repeat
init is tolerable, picks the idiom.

</details>

### What is lazy initialization, and its cost?
<details><summary>Show answer</summary>

Delaying a field's initialization until its value is first needed; if it's never needed, it's never initialized.
Works for static and instance fields.

The trade-off: it lowers the cost of creating the object or loading the class, but raises the cost of every access
to the field (each access must check whether init has happened). Whether it helps depends on how many instances
actually touch the field, how costly the init is, and how often the field is read afterwards — so it can just as
easily make things slower. Default to normal init; go lazy only when measurement says it pays.

</details>

### Breaking an initialization circularity?
<details><summary>Show answer</summary>

When two fields (or a class and its field) each need the other during initialization, initialize one lazily to
break the cycle. Use a synchronized accessor — the simplest, clearest option:

```java
private FieldType field;
private synchronized FieldType getField() {
  if (field == null)
    field = computeFieldValue();
  return field;
}
```

The `synchronized` makes the check-and-set safe across threads. Same idiom works for a static field — add `static`
to both the field and the accessor. Reach for the faster idioms only when this one's per-access lock is a measured
problem.

</details>

### Lazy static field for performance?
<details><summary>Show answer</summary>

Use the holder class idiom — a nested class whose static field does the work:

```java
private static class FieldHolder {
  static final FieldType field = computeFieldValue();
}
private static FieldType getField() { return FieldHolder.field; }
```

The JVM doesn't initialize `FieldHolder` until it's first used, so `computeFieldValue` runs only on the first call
to `getField`. The accessor isn't synchronized and does nothing but read a field — so after the one-time class init,
access is as cheap as a normal field read. The VM handles the init-time synchronization once, then patches it away.

</details>

### Lazy instance field for performance?
<details><summary>Show answer</summary>

Use the double-check idiom — check without a lock, and only lock if the field looks uninitialized:

```java
private volatile FieldType field;
private FieldType getField() {
  FieldType result = field;          // read the volatile once
  if (result == null) {              // first check, no lock
    synchronized (this) {
      if (field == null)             // second check, under lock
        field = result = computeFieldValue();
    }
  }
  return result;
}
```

- `volatile` is mandatory: after init there's no lock on the read path, so without it another thread could see a
  stale or half-built value.
- The `result` local means the volatile field is read once on the common (already-initialized) path instead of
  twice — a small speed and clarity win.
- The second check inside the lock stops two threads that both passed the first check from initializing twice.

</details>

### Lazy field that tolerates repeated init?
<details><summary>Show answer</summary>

Use the single-check idiom — drop the second check, so init may run more than once:

```java
private volatile FieldType field;
private FieldType getField() {
  FieldType result = field;
  if (result == null)
    field = result = computeFieldValue();   // may run once per racing thread
}
```

Only valid when the field can be initialized repeatedly with no harm — each racing thread may compute its own value.
`volatile` stays so a computed value is visible to others.

Racy single-check variant: if you also don't care that each thread recomputes, and the field is a primitive other
than `long`/`double`, you may drop `volatile` too. Faster field access on some hardware, at the cost of up to one
extra init per thread. Exotic — not for everyday use.

</details>

### Double-check vs holder class — which when?
<details><summary>Show answer</summary>

- Instance field → double-check idiom (per-instance, so a nested static holder can't help).
- Static field → holder class idiom (no `volatile`, no explicit checks — the classloader does the work).

You *can* write double-check for a static field, but there's never a reason to: the holder class is simpler and
just as fast. So double-check is the instance-field tool, holder class the static-field tool.

</details>
