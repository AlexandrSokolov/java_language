# Algorithm Tasks — Data-Structure Level

## What these cards are

Each card gives a short task with details left out on purpose. Answer before you open it, out loud, in four steps:

- **Ask** — the questions whose answers change the solution: size, sorted or not, duplicates, memory, order needed.
- **Simple solution** — the one that works, with its cost in time and memory.
- **Better solution** — usually a better collection, with its cost.
- **Trade-off** — what you bought and what you paid. Usually memory for time, or order for speed.

The goal is to pick the structure and say the Big-O, not to write clever algorithm code. The code in the answers is
for reference; in the interview, saying the idea and the cost is the main part.

## Which structure for which kind of task

| Kind of task | Structure | Cost |
|---|---|---|
| "Have I seen this before?", find a partner, find duplicates | `HashSet` / `HashMap` | O(1) average per lookup, O(n) total |
| Count things | `HashMap` + `merge` | O(n) |
| Count, and keep the order things first came in | `LinkedHashMap` | O(n) |
| K biggest / K most frequent | `PriorityQueue` of size K | O(n log K) |
| Ranges overlap, neighbors matter | sort first | O(n log n) |
| Nearest key, not exact key (floor, ceiling, range) | `TreeMap` / `TreeSet` | O(log n) per operation |
| Last opened must close first | `ArrayDeque` as a stack | O(1) push / pop |
| Keep arrival order, drop old items from the front | `ArrayDeque` as a queue | O(1) at both ends |
| Longest / shortest connected part of a sequence with a rule | two indexes (sliding window) + `HashMap` | O(n) |
| Cache that drops the least recently used entry | `LinkedHashMap` in access order | O(1) |
| Data does not fit in memory | split by hash, external sort, Bloom filter, database | depends |

## Facts to have ready

- `HashMap`: O(1) on average. When many keys land in one bucket, since Java 8 the bucket turns into a tree, so the
  worst case is O(log n) instead of O(n) (fully true when the keys are `Comparable`).
- `HashMap<Integer, ...>` stores boxed objects. When keys are a small known range (like `a`–`z`), an `int[]` is
  cheaper and faster.
- `ArrayList.get(i)` is O(1); `ArrayList.remove(0)` is O(n) because every element shifts. For a queue use `ArrayDeque`.
- Not `Stack` and not `LinkedList` for a stack or queue: `Stack` is old and locks on every call; `LinkedList` creates a
  node object per element. `ArrayDeque` is a plain array and is faster. It does not accept `null`.
- `List.sort` for objects is O(n log n) and stable: equal elements keep their order.

---

### Algorithm task #01
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You get a list of integers and a target number. Find two numbers in the list that add up to the target.

</details>

<details><summary>Show answer</summary>

**Questions to ask first:**
- Is the list sorted? This changes the best solution.
- Return the positions (indices) or the values? Sorting loses the original positions.
- One pair, or all pairs? Can there be no pair?
- How big is the list? Does it fit in memory? Is memory tight?

**Simple solution: check every pair.** Time O(n²), memory O(1).

```java
int[] findPair(int[] nums, int target) {
  for (int i = 0; i < nums.length; i++) {
    for (int j = i + 1; j < nums.length; j++) {
      if (nums[i] + nums[j] == target) {
        return new int[] {i, j};
      }
    }
  }
  return null; // no pair
}
```

**Better solution: one pass with a `HashMap`.** For each number, the partner it needs is `target - x`. Keep the
numbers already seen in a map and look the partner up instead of scanning. Time O(n), memory O(n).

```java
int[] findPair(int[] nums, int target) {
  Map<Integer, Integer> seen = new HashMap<>(); // value -> index
  for (int i = 0; i < nums.length; i++) {
    Integer j = seen.get(target - nums[i]); // look up BEFORE put: a number must not pair with itself
    if (j != null) {
      return new int[] {j, i};
    }
    seen.put(nums[i], i);
  }
  return null;
}
```

**Alternative: sort, then two pointers.** One pointer starts at the left end and one at the right end. If the sum is
too small, move the left one forward. If it is too big, move the right one back.

```java
int left = 0, right = nums.length - 1;
while (left < right) {
  int sum = nums[left] + nums[right];
  if (sum == target) return new int[] {left, right};
  if (sum < target) left++; // need a bigger sum
  else right--;             // need a smaller sum
}
```

- **Already sorted:** time O(n), memory O(1). This is the best case, better than the map.
- **Not sorted:** the sort costs O(n log n). Sorting in place keeps memory at O(1), but the original positions are
  lost. If positions are needed, you sort a copy that keeps the indices, and memory goes back to O(n).

**Trade-off:** the map buys time with memory: O(n) time for O(n) extra memory. The pair check and the sort keep
memory low and pay with time.

**What decides:** list already sorted → two pointers. Unsorted and positions needed → `HashMap`. Unsorted, memory
tight, input may be changed → sort in place, then two pointers.

</details>

</details>

---

### Algorithm task #02
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You get a string. Find the first character that appears in it only once.

</details>

<details><summary>Show answer</summary>

**Questions to ask first:**
- Which characters: only `a`–`z`, or any text? With full Unicode a `char` is not always a whole character (an emoji
  takes two), so you would walk `s.codePoints()` instead.
- Upper and lower case — same character or different?
- What to return when every character repeats?
- Can the input be read twice, or is it a stream?

**Simple solution: for each character, count it by scanning the whole string.** Time O(n²), memory O(1).

**Better solution: count first, then find.** Pass 1 counts every character in a `HashMap`. Pass 2 walks the string
again and returns the first character with count 1. Time O(n), memory O(k), where k = number of different characters.

```java
Character firstUnique(String s) {
  Map<Character, Integer> counts = new HashMap<>();
  for (char c : s.toCharArray()) {
    counts.merge(c, 1, Integer::sum);
  }
  for (char c : s.toCharArray()) { // second pass over the string keeps the original order
    if (counts.get(c) == 1) {
      return c;
    }
  }
  return null; // every character repeats
}
```

**Alternative (one pass over the input): `LinkedHashMap`.** It keeps the order keys were first added, so pass 2 walks
the map (k entries) instead of the string (n characters). Use it when the input is a stream you cannot read twice,
or when the text is long and has few different characters.

**Improvement for a known small alphabet:** only `a`–`z` → `int[26]` instead of a map. No boxing, no hashing, memory
is a fixed 26 slots. Costs: works only for that alphabet.

**Trade-off:** O(k) extra memory buys O(n) time instead of O(n²).

**What decides:** input can be read twice → `HashMap` + second pass. Stream → `LinkedHashMap`. Small known alphabet →
`int[]`.

</details>

</details>

---

### Algorithm task #03
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You get a large list of words. Return the K words that appear most often.

</details>

<details><summary>Show answer</summary>

**Questions to ask first:**
- How big is K compared to the number of different words?
- Ties — any order, or alphabetical?
- Does everything fit in memory, or do the words come as a stream?
- Exact answer, or is an approximate top-K fine?

**Step 1 in every solution: count.** `HashMap<String, Integer>` with `merge`. Time O(n), memory O(m), where
m = number of different words.

**Simple solution: sort all counts, take the first K.** Time O(m log m).

```java
List<String> top = counts.entrySet().stream()
    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
    .limit(k)
    .map(Map.Entry::getKey)
    .toList();
```

**Better solution: a min-heap of size K.** Keep only the current top K in a `PriorityQueue`. The weakest of them sits
on top; when the heap grows past K, drop it. Time O(m log K), extra memory O(K).

```java
List<String> topK(Map<String, Integer> counts, int k) {
  // min-heap: the least frequent of the current top K is on top and goes first
  PriorityQueue<Map.Entry<String, Integer>> heap = new PriorityQueue<>(Map.Entry.comparingByValue());
  for (Map.Entry<String, Integer> e : counts.entrySet()) {
    heap.offer(e);
    if (heap.size() > k) {
      heap.poll(); // drop the least frequent
    }
  }
  List<String> result = new ArrayList<>();
  while (!heap.isEmpty()) {
    result.add(heap.poll().getKey());
  }
  Collections.reverse(result); // most frequent first
  return result;
}
```

**Trade-off:** the heap wins when K is much smaller than m (log K instead of log m, and K memory instead of a full
sorted copy). When K is close to m, the plain sort is just as fast and simpler to read.

**When the counts do not fit in memory:** split the words by hash into files, so the same word always goes to the
same file; count each file and keep its top K; then merge those small top-K lists. If an approximate answer is fine,
there are compact counting structures for this (Count-Min Sketch) — name only.

**What decides:** K small → heap. K close to m, or code clarity matters more → sort.

</details>

</details>

---

### Algorithm task #04
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You get a list of time intervals, each with a start and an end. Merge the intervals that overlap.

</details>

<details><summary>Show answer</summary>

**Questions to ask first:**
- Is the list already sorted by start?
- Do touching intervals overlap? Is `[1, 3]` and `[3, 5]` one interval or two?
- May I change the input, or must I return a new list?
- One batch, or do intervals keep arriving?

**Simple solution: compare every interval with every other, merge, repeat until nothing changes.** Time O(n²) or
worse.

**Better solution: sort by start, then one pass.** After sorting, an interval can only overlap the last merged one.
If the next one starts before the last one ends, extend the end; otherwise start a new interval. Time O(n log n)
for the sort plus O(n) for the pass; memory O(n) for the result.

```java
List<int[]> merge(List<int[]> intervals) {
  List<int[]> sorted = new ArrayList<>(intervals);
  sorted.sort(Comparator.comparingInt(a -> a[0])); // by start
  List<int[]> result = new ArrayList<>();
  for (int[] next : sorted) {
    int[] last = result.isEmpty() ? null : result.get(result.size() - 1);
    if (last != null && next[0] <= last[1]) { // overlaps (touching counts here)
      last[1] = Math.max(last[1], next[1]);   // max: next may sit fully inside last
    } else {
      result.add(new int[] {next[0], next[1]}); // copy, so the input is not changed
    }
  }
  return result;
}
```

**Trade-off:** the O(n log n) sort buys a single simple pass. If the input is already sorted, the whole task is O(n).

**What decides:** one batch → sort + one pass. Intervals arrive one by one and each must be checked at once → keep
them in a `TreeMap` by start and check only the neighbors, O(log n) each (see task #05).

</details>

</details>

---

### Algorithm task #05
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You store the price changes of a product, each with the time it changed. Given any moment in time, return the price
that was valid at that moment.

</details>

<details><summary>Show answer</summary>

**Questions to ask first:**
- How many price changes, how many queries?
- Are new changes added over time, or is the data loaded once?
- What if the moment is before the first change?
- Is it used from many threads?

**Simple solution: a list of changes, scan it for the latest change not after T.** O(n) per query. A sorted list
with binary search gives O(log n) per query, but each insert costs O(n) because elements shift.

**Better solution: `TreeMap<Instant, BigDecimal>` and `floorEntry`.** `floorEntry(t)` returns the entry with the
greatest key that is not greater than `t`. Query and insert are both O(log n).

```java
TreeMap<Instant, BigDecimal> prices = new TreeMap<>();
prices.put(Instant.parse("2026-01-01T00:00:00Z"), new BigDecimal("10.00"));
prices.put(Instant.parse("2026-03-01T00:00:00Z"), new BigDecimal("12.50"));

Map.Entry<Instant, BigDecimal> e = prices.floorEntry(t); // latest change at or before t
BigDecimal price = (e == null) ? null : e.getValue();    // null: t is before the first change
```

**Why not `HashMap`:** it finds only the exact key. "The nearest key before T" needs keys kept in order.

**Trade-off against `HashMap`:** O(log n) instead of O(1), and it buys order: `floorKey`, `ceilingKey`, `headMap`,
`subMap` for nearest-key and range queries.

**Alternatives:**
- **Data loaded once, never changed:** sorted array + `Arrays.binarySearch`. Same O(log n), less memory, faster in
  practice (a plain array). Costs: an insert is O(n).
- **Many threads:** `ConcurrentSkipListMap`. Same `NavigableMap` methods, O(log n), safe without one big lock.

**What decides:** data changes → `TreeMap`. Loaded once → sorted array. Many threads → `ConcurrentSkipListMap`.

</details>

</details>

---

### Algorithm task #06
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You get a string with brackets `( ) [ ] { }`. Check that every bracket is closed by the right kind, in the right
order.

</details>

<details><summary>Show answer</summary>

**Questions to ask first:**
- Are there other characters in the string? Ignore them?
- All three kinds, or only one kind?
- Is an empty string valid?

**Simple solution: remove `()`, `[]`, `{}` pairs again and again until nothing changes.** Time O(n²).

**Better solution: a stack.** The last bracket opened must be the first one closed — that is exactly a stack. On an
opening bracket, push the closing bracket you expect. On a closing bracket, pop and compare. At the end the stack
must be empty. Time O(n), memory O(n) in the worst case.

```java
boolean isBalanced(String s) {
  Deque<Character> stack = new ArrayDeque<>();
  for (char c : s.toCharArray()) {
    switch (c) {
      case '(' -> stack.push(')'); // push the closer we expect
      case '[' -> stack.push(']');
      case '{' -> stack.push('}');
      case ')', ']', '}' -> {
        if (stack.isEmpty() || stack.pop() != c) {
          return false; // nothing open, or the wrong kind
        }
      }
      default -> { } // ignore other characters
    }
  }
  return stack.isEmpty(); // something left = opened but never closed
}
```

**Why `ArrayDeque`, not `Stack`:** `Stack` is an old class built on `Vector` and locks on every call. `ArrayDeque` is
a plain array and faster.

**Improvement for one kind of bracket:** a single `int` counter: +1 on open, -1 on close, never below zero, zero at
the end. Memory O(1).

**Trade-off:** O(n) memory for the stack buys O(n) time.

**What decides:** one kind → counter. Several kinds → stack.

</details>

</details>

---

### Algorithm task #07
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You get a string. Find the length of the longest connected part of it in which no character repeats.

</details>

<details><summary>Show answer</summary>

**Questions to ask first:**
- Which characters: ASCII only, or any text?
- Only the length, or the part itself?
- Empty string — return 0?

**Simple solution: check every part.** For each start, extend to the right with a `HashSet` until a repeat. Time
O(n²) (O(n³) if each part is checked from scratch).

**Better solution: a sliding window.** Two indexes mark a window with no repeats. Move the right one forward each
step. If the new character is already inside the window, jump the left index just past its last position. A
`HashMap` keeps the last position of each character. Time O(n), memory O(k), k = number of different characters.

```java
int longestUnique(String s) {
  Map<Character, Integer> lastSeen = new HashMap<>();
  int best = 0;
  int left = 0; // window is s[left..right]
  for (int right = 0; right < s.length(); right++) {
    char c = s.charAt(right);
    Integer prev = lastSeen.get(c);
    if (prev != null && prev >= left) { // repeat inside the window
      left = prev + 1;                  // move the window start past it
    }
    lastSeen.put(c, right);
    best = Math.max(best, right - left + 1);
  }
  return best;
}
```

**Why it is O(n):** both indexes only move forward, never back, so each character is handled a fixed number of times.

**Improvement for ASCII:** `int[128]` instead of the map. No boxing, fixed memory.

**Trade-off:** memory for the last positions buys one pass instead of checking every part.

**What decides:** "longest / shortest connected part with a rule" → sliding window. The map is there only to answer
"is this character inside the window, and where?" fast.

</details>

</details>

---

### Algorithm task #08
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

Allow each user at most N requests per minute. Reject the rest.

</details>

<details><summary>Show answer</summary>

**Questions to ask first:**
- Must it be an exact rolling minute (the last 60 seconds), or is "per clock minute" fine?
- How many users? How big is N?
- One server or many?
- Many threads?

**Simple solution: fixed window.** `HashMap<user, count>`, reset every minute. O(1) per request, O(users) memory.
Defect: at the edge of two minutes a user can send N at the end of one and N at the start of the next — 2N in a few
seconds.

**Better solution (exact): keep request times.** Per user an `ArrayDeque<Long>` of request times. On each request,
drop times older than the window from the front; if fewer than N remain, record the request and allow it. O(1)
amortized per request (each time is added once and removed once), memory O(N) per user.

```java
class RateLimiter {
  private final int limit;
  private final long windowMillis;
  private final Map<String, Deque<Long>> log = new HashMap<>();

  RateLimiter(int limit, long windowMillis) {
    this.limit = limit;
    this.windowMillis = windowMillis;
  }

  synchronized boolean allow(String user, long now) {
    Deque<Long> times = log.computeIfAbsent(user, u -> new ArrayDeque<>());
    while (!times.isEmpty() && times.peekFirst() <= now - windowMillis) {
      times.pollFirst(); // drop requests older than the window
    }
    if (times.size() < limit) {
      times.addLast(now);
      return true;
    }
    return false;
  }
}
```

**Problems a senior names:**
- The map never shrinks: users who stopped sending still hold memory. Needs a cleanup of empty or old entries.
- `synchronized` is one lock for all users. Under load: `ConcurrentHashMap` plus a lock per user.

**Alternative: token bucket.** Per user only two numbers: tokens left and last refill time. Tokens refill at a fixed
rate; each request takes one. O(1) memory per user and allows short controlled bursts. This is what most production
limiters use.

**Many servers:** the counters must live in a shared store (for example Redis), or each server allows N on its own.

**Trade-off:** fixed window — cheapest, but bursts at the edge. Request log — exact, but O(N) memory per user. Token
bucket — cheap and smooth, but not an exact count of the last 60 seconds.

**What decides:** exact count and small N → request log. Many users or big N → token bucket. Several servers →
shared store.

</details>

</details>

---

### Algorithm task #09
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

Build a cache with a fixed size. When it is full, it drops the entry that was used longest ago.

</details>

<details><summary>Show answer</summary>

**Questions to ask first:**
- What is the size limit?
- Does reading an entry count as using it? (Usually yes.)
- Many threads?
- Do entries also expire by time?

**Simple solution: `HashMap` plus a list of keys in use order.** On every use, move the key to the end of the list.
Finding the key in the list is O(n), so every `get` is O(n).

**Better solution: `LinkedHashMap` in access order.** It is a hash table with a linked list running through its
entries, so moving or removing an entry is O(1). With access order, every `get` moves the entry to the end; the
eldest entry is the least recently used. `get` and `put` are O(1).

```java
class LruCache<K, V> extends LinkedHashMap<K, V> {
  private final int capacity;

  LruCache(int capacity) {
    super(16, 0.75f, true); // true = access order: get() moves the entry to the end
    this.capacity = capacity;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > capacity; // checked after each put; eldest = least recently used
  }
}
```

**If asked to build it without `LinkedHashMap`:** `HashMap<K, Node>` plus your own doubly linked list of nodes. The
map finds the node in O(1); the list moves it to the end in O(1). Same idea, written by hand.

**Design note:** extending `LinkedHashMap` exposes every `Map` method to callers. Wrapping a private
`LinkedHashMap` inside your own class (composition) gives a small, clean API.

**Threads:** not thread-safe, and even `get` changes the order, so parallel reads alone can break it.
`Collections.synchronizedMap` — simple, one lock, slow under load. In production — a ready cache library (Caffeine).

**Trade-off:** extra memory for the links in every entry buys O(1) order updates.

**What decides:** one thread → `LinkedHashMap`. Many threads → a ready cache library.

</details>

</details>

---

### Algorithm task #10
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

You have a few billion records (for example customer emails) in files. Remove the duplicates.

</details>

<details><summary>Show answer</summary>

**Questions to ask first:**
- Total size compared to memory?
- Must the result be exact, or is a tiny loss fine?
- Must the original order stay?
- One-off job or a continuous stream? One machine or a cluster?

**Simple solution (if it fit in memory): a `HashSet`.** O(n) time, O(n) memory. Say first why it does not work here:
each string plus each set entry costs tens of bytes, so billions of records need tens to hundreds of GB.

**Options:**
- **Split by hash.** Read once and write each record to file number `hash(record) % P`. The same record always lands
  in the same file, so each file can be cleaned on its own with a `HashSet` in memory. O(n) time, two passes over
  the disk, extra disk space.
- **External sort.** Sort chunks that fit in memory, then merge them. Duplicates end up next to each other and are
  dropped during the merge. O(n log n), exact; the original order is lost.
- **Bloom filter.** A bit array plus several hash functions. It answers "maybe seen" or "surely not seen". Very small
  memory, one pass. Costs: some unique records are wrongly marked "seen" and dropped.
- **Let the database do it.** Load into a table with a unique index, or `SELECT DISTINCT`. In business systems this
  is often the real answer.

**Trade-off:** split by hash — exact, but needs disk and two passes. External sort — exact, slower. Bloom filter —
tiny memory and fast, but not exact.

**What decides:** exact result needed → split by hash (or the database). Small loss is fine and memory is tight →
Bloom filter.

</details>

</details>
