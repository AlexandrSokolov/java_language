### Describe a code snippet #N
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
int counter = 0;   // local

long n = list.stream()
             .peek(x -> counter++)
             .count();
```

</details>

<details><summary>Show answer</summary>

First: this does not compile. A local captured by a lambda must be effectively final, and `counter++` mutates it.

The tempting fix is a mutable holder — `int[] c = {0}` or `AtomicInteger` — so the lambda mutates the box, not the
variable. Now it compiles and runs. And `counter` may still be `0`.

`count()` asks the source how many elements it has. An `ArrayList` knows — its spliterator reports `SIZED`. If no
stage upstream can change the count (`peek` and `map` cannot; `filter` and `flatMap` can), the JDK returns the size
directly and walks nothing. `peek` never runs. Documented since Java 9.

So the effectively-final error is not a detour — it pushes you toward a workaround that compiles and hides the real
defect: a stage that writes something outside itself depends on a promise the API never made — **once per element**.
Had `peek` only read, skipping it would be invisible.

```java
long n = list.stream().filter(s -> !s.isEmpty()).peek(x -> counter++).count();   // peek runs: filter hides the size
```

Safe uses of `peek`: logging while debugging, where nothing later reads the residue. That is what the javadoc
recommends it for.

</details>

</details>


### Describe a code snippet — parallel result order
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<Integer> out = new ArrayList<>();
words.parallelStream()
     .map(String::length)
     .forEach(out::add);
```

</details>

<details><summary>Show answer</summary>

**Wrong:** the result is built by writing into `out` from the terminal, not by returning values. Under a parallel
stream the threads add in the order they finish, so `out` ends up in execution order, not source order — and it
differs run to run. `ArrayList` is also not thread-safe, so concurrent `add` can lose writes or throw.

**Why:** the terminal only gives back [source order](#what-does-a-stream-guarantee) for values that come home
through the return. A value stashed in an outside collection never entered that ordered flow.

**Fix:** return the value and let the terminal build the result.
```java
List<Integer> out = words.parallelStream()
                         .map(String::length)
                         .collect(toList());   // source order every run, thread-safe
```

</details>

</details>

### Describe a code snippet — key from a counter
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
AtomicInteger idx = new AtomicInteger();
Map<Integer, String> m = words.parallelStream()
                              .collect(toMap(w -> idx.getAndIncrement(), w -> w));
```

</details>

<details><summary>Show answer</summary>

**Wrong:** the key is read from `idx`, which changes on every call during the run. This is a
[stateful lambda](#what-is-a-stateful-lambda) — the value each element gets depends on when its thread reached
`idx`. `collect` returns a valid map, but the pairings differ run to run.

**Why:** `AtomicInteger` is thread-safe, so nothing crashes — but thread-safety is not the issue. The result
depends on execution order, which the stream does not hold steady. The word that lands at key 0 changes each run.

**Fix:** derive the key from the element, not from shared state.
```java
Map<Integer, String> m = words.parallelStream()
                              .collect(toMap(String::length, w -> w));   // key from the element — stable
```
If you truly need a running index, the stream is the wrong tool — use an indexed loop.

</details>

</details>

### Describe a code snippet — a peek that never runs
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
AtomicInteger seen = new AtomicInteger();
long n = list.stream()
             .peek(x -> seen.incrementAndGet())
             .count();
```

</details>

<details><summary>Show answer</summary>

**Wrong:** the code assumes `peek` runs once per element, so `seen` ends up equal to `n`. It does not. `count()`
can take the size from the source directly and [skip the pipeline entirely](#what-does-a-stream-guarantee) — `peek`
never fires and `seen` stays 0. This breaks on a **sequential** stream too; no parallelism needed.

**Why:** the stream may skip a stage when it can prove doing so does not change the result. `count()` only needs the
size, and nothing here changes the count, so every stage is elided.

**Fix:** do not hide work in `peek`. If you need the count of processed elements, count what you return.
```java
long n = list.stream().filter(x -> true).count();   // if a stage can change the count, the pipeline must run
```
`peek` is for logging while debugging, where a skipped run does no harm.

</details>

</details>

### Describe a code snippet — changing the source mid-stream
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
list.stream()
    .forEach(x -> { if (x.equals("a")) list.add("d"); });
```

</details>

<details><summary>Show answer</summary>

**Wrong:** the lambda modifies `list` — the stream's own source — while the stream is running. This is
**interference**: touching the source during the run. It throws `ConcurrentModificationException`, or gives a wrong
result on sources that do not check.

**Why:** the source must not change between the start and end of the terminal operation. This rule holds for
**sequential** streams too — it is not about threads, it is about mutating what you are reading.

**Fix:** do not modify the source inside the pipeline. Collect what you need, then change the collection after.
```java
List<String> toAdd = list.stream().filter(x -> x.equals("a")).map(x -> "d").toList();
list.addAll(toAdd);   // mutate after the stream finishes
```

</details>

</details>

### Describe a code snippet — parallel reduce into a container
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<Integer> result = numbers.parallelStream()
                              .reduce(new ArrayList<>(),
                                      (list, n) -> { list.add(n); return list; },
                                      (l1, l2) -> { l1.addAll(l2); return l1; });
```

</details>

<details><summary>Show answer</summary>

**Wrong:** `reduce` is used with a single mutable container that every thread adds into. The accumulator mutates and
returns the *same* list, so parallel threads share and corrupt one container — lost elements, duplicates, or a
crash. Works on a sequential stream, wrong on a parallel one.

**Why:** `reduce` requires each step to produce a **new** value from its inputs without mutating them (an
associative, side-effect-free combine). A shared mutable accumulator breaks that contract — the identity value
(`new ArrayList<>()`) is also reused across threads, which is illegal.

**Fix:** use `collect` for building a container — it is designed for mutable accumulation with a per-thread
container and a safe merge.
```java
List<Integer> result = numbers.parallelStream().collect(toList());
```

</details>

</details>

### Describe a code snippet — collecting upload results by hand
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

TODO bad card

```java
Map<Boolean, List<Result>> byOutcome = new HashMap<>();
images.parallelStream()
      .forEach(img -> {
        Result r = uploader.send(img);                            // Result holds the outcome + which image
        byOutcome.computeIfAbsent(r.ok(), k -> new ArrayList<>()).add(r);
      });
```

</details>

<details><summary>Show answer</summary>

**Wrong:** the upload result is stashed into `byOutcome` from inside the terminal, and that map is what you read
back — so the result depends on a shared collection filled during the run. Two problems: `HashMap` and its inner
`ArrayList` are not thread-safe, so parallel `add` can lose entries, corrupt the map, or throw; and even made
thread-safe, you have hand-written the accumulation the stream is built to do for you.

**Why:** `uploader.send(img)` is fine — it writes to the network but returns a `Result`, so it does not depend on
changing state. The mistake is not the upload. It is stashing that `Result` in a shared map instead of
[returning it](#what-is-a-stateful-lambda) and letting the terminal collect. A returned value carries its own
answer; a stashed one makes the result depend on order and threads.

**Fix:** return the `Result` from the stage, collect with `partitioningBy`.
```java
Map<Boolean, List<Result>> byOutcome =
    images.parallelStream()
          .map(uploader::send)                     // send, return the Result
          .collect(partitioningBy(Result::ok));    // let collect build it — thread-safe, source order
```
Same uploads, same parallelism — but the result is built from returned values, so it is stable and needs no manual
synchronization.

</details>

</details>