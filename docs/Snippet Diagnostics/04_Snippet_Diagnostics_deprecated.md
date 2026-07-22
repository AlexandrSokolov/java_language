### Reusing a stream variable
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Stream<Order> orders = orderList.stream();
long count = orders.count();                      // first terminal
List<Order> big = orders.filter(o -> o.total() > 100).toList();   // second terminal — what happens?
```

</details>

<details><summary>Show answer</summary>

The second terminal throws `IllegalStateException: stream has already been operated upon or closed`. A stream is
single-use: one terminal operation consumes it, and the pipeline object cannot be run again.

Fix — build a fresh stream from the source each time, or materialize once and reuse the collection:

```java
List<Order> all = orderList.stream().toList();    // materialize once
long count = all.size();
List<Order> big = all.stream().filter(o -> o.total() > 100).toList();
```

The rule: a `Stream` is not a reusable collection; treat it as a one-shot pipeline. Store the source, not the stream.

</details>

</details>

### parallel on an iterate chain
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Stream.iterate(BigInteger.ONE, n -> n.multiply(BigInteger.TWO))
      .parallel()
      .limit(500_000)
      .forEach(System.out::println);
```

</details>

<details><summary>Show answer</summary>

Two problems.

The source has no known size and no cut point: `iterate` produces element N by applying the function to element N-1,
so no thread can start anywhere but the beginning. `limit` then makes the framework guess how far to go, so it
produces elements past 500,000 and throws them away. Each `BigInteger` is twice the size of the one before, so every
wasted element costs more than all the earlier ones together. The run pins every core and never finishes.

Even with a source that split cleanly, parallel would not pay here. It needs both a large element count and enough
work per element to cover the cost of splitting and merging. The count is there, the work is not — the terminal
operation only prints. Printing is also serialized on the output stream, so the threads queue behind each other.

Fix: drop `parallel()`.

</details>

</details>

### peek for real work — count case
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
long n = files.stream()
    .peek(f -> upload(f))     // side effect meant to run for every file
    .count();                 // just want the number uploaded
```

</details>

<details><summary>Show answer</summary>

`upload` may never run. Since Java 9, `count()` can compute the size without walking the elements when the stream's
size is already known, so the pipeline skips traversal — and `peek` with it. The count is correct; the uploads
silently don't happen.

Fix — put the real work in a terminal that must consume every element, not in `peek`:

```java
files.forEach(this::upload);                       // if the count isn't needed
long n = files.stream().filter(this::uploadReturningTrue).count();   // or make the work observable
```

`peek` is for debugging only. Any element the pipeline can shortcut past is an element `peek` never sees — never rely
on it for effects that must happen.

</details>

</details>

### toMap with colliding keys
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Map<Integer, String> byLength = Stream.of("bat", "cat", "dog")   // all length 3
    .collect(Collectors.toMap(String::length, w -> w));          // runs — result?
```

</details>

<details><summary>Show answer</summary>

Throws `IllegalStateException: Duplicate key 3 (attempted merging values bat and cat)`. The two-argument `toMap`
assumes keys are unique; the moment two elements map to the same key it aborts — no silent overwrite.

Fix — supply a merge function that decides what to do on collision:

```java
// keep first, or combine, or last-wins:
Stream.of("bat", "cat", "dog")
    .collect(Collectors.toMap(String::length, w -> w, (a, b) -> a + "," + b));   // 3 -> "bat,cat,dog"
```

If the key really should be unique, the exception is doing its job — it caught a wrong assumption. If duplicates are
expected, `groupingBy(String::length)` giving a `Map<Integer, List<String>>` is usually the honest shape.

</details>

</details>

### toMap with a null value
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Map<Path, String> texts = paths.stream()
    .collect(Collectors.toMap(p -> p, p -> cache.get(p)));   // cache.get may return null
```

</details>

<details><summary>Show answer</summary>

If any `cache.get(p)` returns `null`, this throws `NullPointerException` — even though a plain `HashMap.put(key,
null)` is legal. `Collectors.toMap` is backed by `merge`, which forbids null values, so the collector is stricter
than the map it builds.

Fix — filter the nulls out first, or replace them with a sentinel before collecting:

```java
paths.stream()
    .filter(p -> cache.get(p) != null)
    .collect(Collectors.toMap(p -> p, cache::get));
```

The trap is that it looks like map insertion but isn't — the collector's null rule is the one that applies, not the
map's.

</details>

</details>

### Stream.of with an array
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
int[] values = {10, 20, 30};
int total = Stream.of(values)          // build a stream over the numbers?
    .mapToInt(Integer::intValue)
    .sum();                            // compiles?
```

</details>

<details><summary>Show answer</summary>

It does not compile the way intended, and the root cause is `Stream.of(values)`: given an `int[]`, it produces a
`Stream<int[]>` with **one** element — the whole array — not a stream of three ints. `Stream.of` only spreads a
`T...`, and `int[]` isn't `Integer[]`, so the array is treated as a single object.

Fix — use `Arrays.stream`, which has an `int[]` overload returning an `IntStream`:

```java
int total = Arrays.stream(values).sum();          // IntStream over 10, 20, 30
```

The rule: for a primitive array use `Arrays.stream`, not `Stream.of`. `Stream.of` is safe only for object arrays or a
loose list of arguments.

</details>

</details>

### map where flatMap was needed
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<Order> orders = ...;                          // each Order has List<Item> items()
List<Item> allItems = orders.stream()
    .map(Order::items)                             // want every item across all orders
    .collect(Collectors.toList());                 // what type is this really?
```

</details>

<details><summary>Show answer</summary>

`allItems` is not `List<Item>` — it's `List<List<Item>>`. `map(Order::items)` turns each order into its *list* of
items, so the stream is `Stream<List<Item>>`. Flattening never happened; the nesting is still there.

Fix — `flatMap` opens each inner list into the stream instead of keeping it as one element:

```java
List<Item> allItems = orders.stream()
    .flatMap(o -> o.items().stream())              // each order contributes its items individually
    .collect(Collectors.toList());
```

Rule of thumb: when the per-element function returns a collection or stream and you want the contents merged, that's
`flatMap`, not `map`. `map` gives one output per input; `flatMap` gives many.

</details>

</details>

### sorted without a comparator
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
record Point(int x, int y) {}                      // not Comparable
List<Point> sorted = points.stream()
    .sorted()                                      // compiles — safe?
    .collect(Collectors.toList());
```

</details>

<details><summary>Show answer</summary>

It compiles but throws `ClassCastException` at runtime the moment two elements are compared. The no-argument
`sorted()` casts elements to `Comparable`; `Point` doesn't implement it, so the cast fails during execution, not at
compile time.

Fix — pass an explicit comparator so the ordering is defined:

```java
points.stream()
    .sorted(Comparator.comparingInt(Point::x).thenComparingInt(Point::y))
    .collect(Collectors.toList());
```

The trap is the silence at compile time: `sorted()` has no way to demand `Comparable` in its signature, so the check
slips to runtime. Any time the element isn't obviously `Comparable`, pass a comparator.

</details>

</details>

### findFirst on an empty result
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
User admin = users.stream()
    .filter(User::isAdmin)
    .findFirst()
    .get();                                        // what if no admin exists?
```

</details>

<details><summary>Show answer</summary>

If no user is an admin, `findFirst()` returns an empty `Optional` and `.get()` throws `NoSuchElementException`.
Calling `get()` without checking presence is the classic Optional misuse — it reintroduces exactly the failure
Optional was meant to make visible.

Fix — decide what "not found" means and encode it, rather than blindly unwrapping:

```java
User admin = users.stream().filter(User::isAdmin).findFirst()
    .orElseThrow(() -> new IllegalStateException("no admin configured for tenant " + tenantId));
// or .orElse(defaultAdmin) / .map(...).orElse(...) if absence is normal
```

`orElseThrow` with a message beats a bare `get()`: same failure when it's truly unexpected, but the error says what
was missing and where. Reach for `get()` almost never.

</details>

</details>

### map then filter — ordering cost
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<Result> out = ids.stream()
    .map(this::expensiveLookup)                    // network call per id
    .filter(r -> r.score() > threshold)
    .toList();                                     // correctness is fine — what's wasteful?
```

</details>

<details><summary>Show answer</summary>

Correct, but it runs `expensiveLookup` for **every** id, then discards the ones that don't pass. If the cheap test
can be expressed on the id itself, filtering *before* the costly map avoids the wasted calls.

Fix — move any predicate that doesn't need the mapped value ahead of the expensive stage:

```java
ids.stream()
    .filter(this::worthLookingUp)                  // cheap gate first
    .map(this::expensiveLookup)                    // only survivors pay the cost
    .filter(r -> r.score() > threshold)            // this one genuinely needs the result
    .toList();
```

The order of `filter`/`map` never changes the answer here, but it changes how many expensive operations run. Put the
cheapest, most-eliminating stage first.

</details>

</details>

### Boxing in a numeric pipeline
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
long total = orders.stream()
    .map(Order::amountCents)                       // returns int
    .reduce(0, Integer::sum);                      // what's the hidden cost?
```

</details>

<details><summary>Show answer</summary>

Every element is boxed into an `Integer` to flow through the object stream, then unboxed to add — one allocation per
order, plus the accumulator boxing. On a large stream this is real overhead for pure arithmetic.

Fix — drop to a primitive stream with `mapToInt`/`mapToLong`, which carries raw values and gives a direct `sum()`:

```java
long total = orders.stream()
    .mapToLong(Order::amountCents)                 // primitive stream, no boxing
    .sum();
```

Rule: when a pipeline ends in numeric aggregation, use `IntStream`/`LongStream`/`DoubleStream`. The boxed
`Stream<Integer>` is for when you genuinely need object elements, not for counting money.

</details>

</details>

### groupingBy holding whole elements
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Map<Dept, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::dept));
int headcount = byDept.get(SALES).size();          // only ever need the counts
```

</details>

<details><summary>Show answer</summary>

Works, but it builds a full `List<Employee>` per department just to call `size()` on it — every employee object is
retained in memory when only a number was wanted. The single-argument `groupingBy` defaults its downstream to
`toList`.

Fix — give `groupingBy` a downstream collector that computes the number directly:

```java
Map<Dept, Long> headcount = employees.stream()
    .collect(Collectors.groupingBy(Employee::dept, Collectors.counting()));
```

The lesson: `groupingBy`'s second argument reshapes each bucket. `counting()`, `summingInt(...)`, `mapping(...)`,
`averagingDouble(...)` let you collect the summary you actually need instead of the raw list.

</details>

</details>

### Parallel flag on an iterator source
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Iterable<Order> orders = repository.streamAll();
StreamSupport.stream(orders.spliterator(), true)
             .forEach(this::handle);
```

</details>

<details><summary>Show answer</summary>

Two problems.

The source has no known size and no cut point: a spliterator built from a plain `Iterable` only knows `hasNext` /
`next`, so it cannot hand a range to another thread. All it can do is pull elements one at a time and copy them into
small arrays for other threads to consume — the pulling stays single-threaded and now carries extra copying on top.

Second, `handle` may now run on ForkJoinPool threads while the database cursor behind `streamAll` lives on the
calling thread. If the connection closes when the caller returns, the worker threads are still reading from it.

Fix: drop the parallel flag — pass `false`.

</details>

</details>

### Parallel flag on an iterator source
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Iterable<Order> orders = repository.streamAll();
StreamSupport.stream(orders.spliterator(), true)
             .forEach(this::handle);
```

</details>

<details><summary>Show answer</summary>

The `true` asks for parallel, but the spliterator built from a plain `Iterable` only knows `hasNext` / `next`. It
reports no size and offers no index, so it cannot hand a range to another thread. All it can do is pull elements one
at a time and copy them into small arrays for other threads to consume — the pulling itself stays single-threaded and
now carries extra copying.

The database cursor behind `streamAll` also stays single-threaded, and `handle` may now run on ForkJoinPool threads
while the cursor lives on the calling thread — a lifetime problem if the connection closes when the caller returns.

Fix: keep it sequential, or read into a list first and parallelize that:

```java
List<Order> orders = repository.findAll();
orders.parallelStream()
      .forEach(this::handle);
```

</details>

</details>

### Stateful lambda in a pipeline
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<String> input = List.of("a", "b", "c", "d", "e", "f", "g", "h");

AtomicInteger position = new AtomicInteger();
List<String> numbered = input.parallelStream()
        .map(s -> position.incrementAndGet() + ":" + s)
        .toList();
```

</details>

<details><summary>Show answer</summary>

The lambda keeps state outside itself: the number it produces for an element depends on how many elements were
mapped before it. The `Stream` spec requires `map`'s function to be stateless — the result for an element must depend
on that element alone.


`AtomicInteger` stops the counter from losing increments, so every number 1..8 appears exactly once and no exception
is thrown. What it cannot fix is *which* element gets which number: the workers run in an unspecified order, so `"a"`
may come out as `5:a`. The list order is still correct — `toList` on an ordered stream keeps encounter order — so the
result looks plausible and is wrong.


Dropping `parallel` makes the output right — one thread increments in encounter order — but the lambda is still
illegal, and the next reader who parallelizes it gets the bug back. Fix the lambda: derive the number from the
element's position, so the result holds either way.


```java
List<String> numbered = IntStream.range(0, input.size())
        .parallel()
        .mapToObj(i -> (i + 1) + ":" + input.get(i))
        .toList();
```

Separately, `parallel` did not belong here anyway — eight elements and one concatenation each. 
Parallel pays only when the per-element work is heavy enough to cover splitting and merging.

</details>

</details>

### reduce with a non-associative op
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<Integer> prices = List.of(100, 20, 5, 3);

int remaining = prices.parallelStream()
        .reduce(200, (a, b) -> a - b);
```

</details>

<details><summary>Show answer</summary>

Subtraction is not associative: `(a - b) - c` is not `a - (b - c)`. `reduce` splits the elements into chunks and
merges the partial results in whatever order the tree unwinds, so regrouping the operands changes the answer.

There is a second defect in the same line. With the two-argument `reduce`, the identity doubles as the starting value
for **every chunk**, and the spec requires `f(identity, x) == x`. Here 200 is not an identity for subtraction, so
each chunk starts from 200 and subtracts its own elements. Sequential gives `200-100-20-5-3 = 72`; two chunks give
`(200-100-20) - (200-5-3) = -112`. The result therefore depends on how many chunks the framework makes, 
which changes with the machine and the data size — so it can pass on a laptop and fail in production.


Sequential hides both: one chunk means no regrouping and only one application of the identity, so the answer is
always 72 and nothing flags the bug.

Fix 1 — drop `parallel`. One chunk, so neither defect can fire:

```java
int remaining = prices.stream()
        .reduce(200, (a, b) -> a - b);
```

Fix 2 — make the lambda legal, so the result holds whether or not anyone parallelizes it later. `+` is associative
and 0 satisfies `0 + x == x`; the starting value is applied once, outside the stream:

```java
int remaining = 200 - prices.stream()
                            .reduce(0, Integer::sum);
```

Fix 1 leaves an illegal lambda in the code and only works because nothing splits it — the next reader who adds
`parallel` gets the bug back. Fix 2 is a rewrite of the calculation, and it is the one to keep.

Separately, `parallel` did not belong here anyway — four numbers and one subtraction each. Parallel pays only when
the per-element work is heavy enough to cover splitting and merging.

</details>

</details>

### Collectors.toList vs Stream.toList
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<String> a = names.stream().collect(Collectors.toList());
a.add("extra");                                    // fine

List<String> b = names.stream().toList();
b.add("extra");                                    // what happens here?
```

</details>

<details><summary>Show answer</summary>

The second `add` throws `UnsupportedOperationException`. The two look interchangeable but differ in one contract:
`Stream.toList()` (Java 16+) returns an **unmodifiable** list, while `Collectors.toList()` returns a mutable one (in
practice an `ArrayList`, though its spec doesn't even promise mutability).

So neither is a safe default without knowing which you got:

```java
List<String> mutable   = new ArrayList<>(names.stream().toList());   // if you must modify
List<String> immutable = names.stream().toList();                    // if you want it frozen
```

The trap bites when refactoring one call into the other and a later `add`/`sort`/`remove` suddenly breaks. Pick by
whether the result must be modifiable, not by which is shorter to type.

</details>

</details>

### Optional as a field
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
class Config {
  private Optional<String> region;                 // "clean null-safety"?
  Config(String region) { this.region = Optional.ofNullable(region); }
}
```

</details>

<details><summary>Show answer</summary>

`Optional` is the wrong tool for a field. It isn't `Serializable`, it adds an allocation per instance, and it still
allows `region = null`, so it doesn't even remove the null it was meant to. It was designed as a **return type** for
methods that might have nothing to return, not as a container for state.

Fix — store a plain nullable field (or split the type), and hand out `Optional` only at the read boundary:

```java
class Config {
  private final String region;                     // may be null
  Config(String region) { this.region = region; }
  Optional<String> region() { return Optional.ofNullable(region); }   // Optional at the API edge
}
```

The rule: `Optional` in method returns, not in fields, parameters, or collections. As a field it costs more than the
null check it replaces.

</details>

</details>

### Infinite stream without a limit
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<Integer> squares = Stream.iterate(1, n -> n + 1)
    .map(n -> n * n)
    .filter(n -> n % 2 == 0)
    .collect(Collectors.toList());                 // what does this do at runtime?
```

</details>

<details><summary>Show answer</summary>

It never returns — it runs until it exhausts memory or hangs. `Stream.iterate(1, n -> n + 1)` is an infinite source,
and `filter` is not a short-circuiting operation: it happily keeps pulling forever, and `collect` keeps accumulating.
Nothing tells the pipeline to stop.

Fix — bound an infinite source with a short-circuiting stage before the terminal:

```java
List<Integer> squares = Stream.iterate(1, n -> n + 1)
    .map(n -> n * n)
    .filter(n -> n % 2 == 0)
    .limit(10)                                     // now the pull stops
    .collect(Collectors.toList());
// or the 3-arg iterate with a predicate: Stream.iterate(1, n -> n < 100, n -> n + 1)
```

The trap: `filter` looks like it narrows the stream, but it only narrows what passes — it never ends it. Only
`limit`, `takeWhile`, `findFirst`, or a short-circuiting match can terminate an infinite source.

</details>

</details>

### Modifying source during collect
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<Task> tasks = new ArrayList<>(loaded);
tasks.stream()
    .filter(Task::isStale)
    .forEach(tasks::remove);                        // prune stale tasks — safe?
```

</details>

<details><summary>Show answer</summary>

It throws `ConcurrentModificationException`. The `forEach` is still reading `tasks` through the stream while
`tasks::remove` structurally changes that same list — modifying the source of a live stream is illegal, and fail-fast
collections detect it and throw.

Fix — separate the read from the write: collect what to remove, then remove it (or use `removeIf`, built for exactly
this):

```java
tasks.removeIf(Task::isStale);                      // one call, no stream, no interference

// or, if a stream is needed to decide:
List<Task> stale = tasks.stream().filter(Task::isStale).toList();
tasks.removeAll(stale);
```

The rule: never write to the collection a stream is reading while the terminal runs. Decide with the stream, mutate
afterward.

</details>

</details>
