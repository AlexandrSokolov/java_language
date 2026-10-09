### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public synchronized boolean transferTo(FSOAccount other, int amount) {
  if (balance >= amount) {
    balance = balance - amount;
    other.deposit(amount);   // deposit is also synchronized
    return true;
  }
  return false;
}

// two threads, opposite directions:
//   Thread A: a.transferTo(b, 1)
//   Thread B: b.transferTo(a, 1)
```

</details>

<details><summary>Show answer</summary>

**Deadlock.** `transferTo` is synchronized, so a thread holds the source account's lock for the whole method.
Inside, `other.deposit()` is also synchronized, so it needs the target account's lock too — a second lock taken
while holding the first. With two threads transferring in opposite directions, A holds a and waits for b while B
holds b and waits for a. Neither releases; both stop forever.

**Not guaranteed to appear every run.** Whether it deadlocks depends on timing — both threads must grab their
first lock before either grabs the second. It may run clean many times, then hang. The `sleep`/DB-call delay in
real code just widens the window and makes it reliable.

**Minimal fix — lock ordering.** Acquire both locks in one global order, chosen by a stable key, so the cycle
can't form:

```java
FSOAccount first  = this.id() < other.id() ? this : other;
FSOAccount second = this.id() < other.id() ? other : this;
synchronized (first) {
  synchronized (second) {
    // debit this, credit other
  }
}
```

**Better rewrite — don't hold a lock across the call at all** (open call): do the debit under this account's lock,
release, then call `other.deposit()`. No two locks held at once, nothing to deadlock on — at the cost of a brief
window where the money has left one account before reaching the other.

Handle: a synchronized method that takes a second lock (here via a synchronized call on another object) deadlocks
when two threads do it in opposite order — order the locks, or don't hold one across the call.

</details>

</details>

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```text
"Thread-0" #11 prio=5 os_prio=31 tid=0x00007fb8 nid=0x5b03 waiting for monitor entry
   java.lang.Thread.State: BLOCKED (on object monitor)
      at optjava.atm.FSOAccount.deposit(FSOAccount.java:23)
      - waiting to lock <0x000000071b6b6410> (a optjava.atm.FSOAccount)
      at optjava.atm.FSOAccount.transferTo(FSOAccount.java:39)
      - locked <0x000000071b6b63f8> (a optjava.atm.FSOAccount)
      at optjava.atm.FSOMain.lambda$main$0(FSOMain.java:14)

"Thread-1" #12 prio=5 os_prio=31 tid=0x00007fb9 nid=0x5c04 waiting for monitor entry
   java.lang.Thread.State: BLOCKED (on object monitor)
      at optjava.atm.FSOAccount.deposit(FSOAccount.java:23)
      - waiting to lock <0x000000071b6b63f8> (a optjava.atm.FSOAccount)
      at optjava.atm.FSOAccount.transferTo(FSOAccount.java:39)
      - locked <0x000000071b6b6410> (a optjava.atm.FSOAccount)
      at optjava.atm.FSOMain.lambda$main$1(FSOMain.java:22)
```

</details>

<details><summary>Show answer</summary>

**A deadlock.** Read it by matching the two monitor addresses:

- Thread-0 has **locked `...3f8`** and is **waiting to lock `...410`**.
- Thread-1 has **locked `...410`** and is **waiting to lock `...3f8`**.

Each holds the monitor the other is waiting for — the addresses cross. Both are `BLOCKED (on object monitor)`,
both stuck inside `deposit` (line 23) after entering `transferTo` (line 39). Neither can proceed; neither will
release. That crossed pair is the signature of a two-thread deadlock.

**How to read any deadlock dump:** find threads in `BLOCKED (on object monitor)`, then for each match its
`waiting to lock <addr>` against another thread's `locked <addr>`. If the "waiting" and "locked" addresses form a
loop, it's a deadlock. (The JVM often prints a `Found one Java-level deadlock` summary at the end, but confirming
by the addresses is the skill.)

Handle: in a thread dump, a deadlock is two (or more) threads whose "waiting to lock" and "locked" monitor
addresses cross — each holding what the next one needs.

</details>

</details>

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private final Object A = new Object();
private final Object B = new Object();

void method1() {
  synchronized (A) {
    synchronized (B) {
      // work
    }
  }
}

void method2() {
  synchronized (B) {
    synchronized (A) {
      // work
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

**Deadlock — two locks taken in opposite order.** `method1` takes A then B; `method2` takes B then A. One thread
in `method1` holding A, another in `method2` holding B: the first waits for B, the second waits for A. Each holds
what the other needs; both stop forever.

**Not guaranteed every run.** It needs both threads to grab their first lock before either grabs the second — so
it may run clean many times, then hang.

**Fix — one global lock order.** Both methods take A before B (or B before A), always the same:

```java
void method1() { synchronized (A) { synchronized (B) { /* work */ } } }
void method2() { synchronized (A) { synchronized (B) { /* work */ } } }
```

Handle: two locks in opposite order is the base deadlock — every thread must take shared locks in one fixed
order.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
// "fixed" transfer: orders the two locks by identity hash before locking
void transfer(Account from, Account to, long amount) {
  Account first  = System.identityHashCode(from) < System.identityHashCode(to) ? from : to;
  Account second = (first == from) ? to : from;
  synchronized (first) {
    synchronized (second) {
      from.debit(amount);
      to.credit(amount);
    }
  }
}
```

</details>

<details><summary>Show answer</summary>

**Lock ordering that still deadlocks — the order key is not collision-free.** Ordering by
`System.identityHashCode` gives a stable order *only when the two hashes differ*. Two distinct accounts can share
an identity hash (it is not unique). When they collide, `first`/`second` is decided arbitrarily, two threads pick
opposite orders for that pair, and the deadlock returns.

**Why it looks safe:** it works for millions of transfers, then hangs on the one pair whose hashes happen to
collide — the hardest kind to reproduce.

**Fix — a tie-breaker lock for the equal-hash case.** Take a third, shared lock before the two account locks when
the hashes are equal, so those rare collisions are still totally ordered:

```java
private static final Object TIE = new Object();

int h1 = System.identityHashCode(from), h2 = System.identityHashCode(to);
if (h1 < h2)      { synchronized (from) { synchronized (to) { doTransfer(from, to, amount); } } }
else if (h1 > h2) { synchronized (to)   { synchronized (from) { doTransfer(from, to, amount); } } }
else synchronized (TIE) {            // rare: equal hashes — serialize through one lock
  synchronized (from) { synchronized (to) { doTransfer(from, to, amount); } }
}
```

Handle: "order the locks" prevents deadlock only with a *total, collision-free* order — an ordering key that can
tie needs a tie-breaker, or it is not an order.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
class Taxi {
  private Point location;
  private final Dispatcher dispatcher;

  public synchronized Point getLocation() { return location; }

  public synchronized void setLocation(Point p) {
    location = p;
    dispatcher.notifyAvailable(this);   // alien call, made while holding this Taxi's lock
  }
}

class Dispatcher {
  private final Set<Taxi> available = new HashSet<>();

  public synchronized void notifyAvailable(Taxi t) { available.add(t); }

  public synchronized Image getImage() {
    Image img = new Image();
    for (Taxi t : available)
      img.draw(t.getLocation());        // alien call, made while holding the Dispatcher's lock
    return img;
  }
}
```

</details>

<details><summary>Show answer</summary>

**Deadlock hidden behind method calls — no nested `synchronized` in sight.** Each class calls the other while
holding its own lock:

- `setLocation` holds the **Taxi** lock, then calls `notifyAvailable`, which needs the **Dispatcher** lock.
- `getImage` holds the **Dispatcher** lock, then calls `getLocation`, which needs the **Taxi** lock.

One thread in `setLocation` (has Taxi, wants Dispatcher) and another in `getImage` (has Dispatcher, wants Taxi) is
the same opposite-order cycle — but the second lock is acquired *inside a call to another object*, so it is easy
to miss. This is the trap: the deadlock is in the call graph, not on the page.

**Fix — open calls: don't call another object while holding your lock.** Do the locked work, release, then call
out:

```java
public void setLocation(Point p) {
  synchronized (this) { location = p; }   // lock released here
  dispatcher.notifyAvailable(this);       // called with no lock held
}
```

Handle: a method that holds its lock and calls another synchronized object is a hidden second-lock acquire —
release before the call (open call), or the cycle is invisible until it hangs.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
ExecutorService pool = Executors.newFixedThreadPool(1);   // one worker

Future<String> outer = pool.submit(() -> {
  // this task needs the result of another task, submitted to the SAME pool
  Future<String> inner = pool.submit(() -> "done");
  return inner.get();                                      // waits for inner to run
});
outer.get();
```

</details>

<details><summary>Show answer</summary>

**Thread-starvation deadlock — no locks at all.** The pool has one worker. `outer` occupies it and then blocks on
`inner.get()`. But `inner` cannot run until a worker is free, and the only worker is held by `outer` waiting for
`inner`. The pool is stuck with no lock involved.

**Where it bites in real code:** any task in a bounded pool that waits on another task submitted to the *same*
pool — the deadlock appears only when the pool happens to be full, so it passes tests and hangs in production
under load.

**Fix — don't block a pooled task on another task in the same pool.** Compose without blocking a worker, or use a
separate pool for the dependent work:

```java
CompletableFuture
  .supplyAsync(() -> "done", pool)     // no worker blocks waiting for another
  .thenApply(s -> process(s));
```

Handle: a pooled task that waits on another task in the same bounded pool can starve every worker — never block a
worker on work that needs a worker.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private final Object lock = new Object();
private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();

void process() {
  synchronized (lock) {
    Task t = queue.take();   // blocks until an item arrives — while holding lock
    handle(t);
  }
}

void submit(Task t) {
  synchronized (lock) {      // producer needs the same lock to add work
    queue.put(t);
  }
}
```

</details>

<details><summary>Show answer</summary>

**Deadlock — a blocking call made while holding a lock.** `process` takes `lock`, then calls `queue.take()`,
which blocks until an item exists. But the only way an item arrives is `submit`, which needs the same `lock` —
held by the waiting `process`. The consumer waits for an item that the producer can never deliver because it
can't get the lock.

**This is not a two-lock cycle** — it is one lock held across a blocking wait, which is the same family:
something that can block forever is holding the lock nothing else can get.

**Fix — never hold a lock across a blocking call.** `BlockingQueue` is already thread-safe; it needs no external
lock at all:

```java
void process() {
  Task t = queue.take();   // no lock held; the queue handles its own safety
  handle(t);
}
void submit(Task t) { queue.put(t); }
```

Handle: holding a lock across a blocking call (`take`, `get`, I/O) freezes everyone who needs that lock — do the
blocking wait with no lock held.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
private final Semaphore mutex = new Semaphore(1);   // used as a lock

void outer() throws InterruptedException {
  mutex.acquire();
  try {
    inner();          // inner also acquires the same mutex
  } finally {
    mutex.release();
  }
}

void inner() throws InterruptedException {
  mutex.acquire();    // second acquire, same thread — never returns
  try { /* work */ }
  finally { mutex.release(); }
}
```

</details>

<details><summary>Show answer</summary>

**Self-deadlock — a non-reentrant lock re-acquired by the same thread.** A `Semaphore(1)` has one permit and no
concept of an owner. `outer` takes the permit, then calls `inner`, which tries to take it again. The permit is
gone, so the thread blocks — waiting for a permit only it could release, and it is stuck before it can. One
thread, no cycle, permanently frozen.

**The contrast worth knowing:** `synchronized` and `ReentrantLock` are **reentrant** — the same thread can
re-acquire a lock it already holds, so this exact nesting is fine with them. A `Semaphore` (or any non-reentrant
lock) is not. `ReentrantLock` has the mirror trap: two `lock()` calls need two `unlock()` calls, or the lock is
never fully released.

**Fix — use a reentrant lock, or don't re-lock in the nested call:**

```java
private final ReentrantLock lock = new ReentrantLock();   // same thread may re-enter
// or: inner() assumes the caller holds the lock and does not acquire it again
```

Handle: re-acquiring a non-reentrant lock on the same thread self-deadlocks — `synchronized`/`ReentrantLock` are
reentrant, a `Semaphore` used as a mutex is not.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
class A {
  static final B b = B.create();   // A's init needs B
  static A create() { return new A(); }
}

class B {
  static final A a = A.create();   // B's init needs A
  static B create() { return new B(); }
}

// two threads at once:
//   Thread 1: new A()   -> triggers A's static init
//   Thread 2: new B()   -> triggers B's static init
```

</details>

<details><summary>Show answer</summary>

**Class-initialization deadlock.** The JVM holds a per-class lock while running a class's static initializer
(JLS 12.4.2). Thread 1 holds A's init lock and, to finish, needs B initialized — so it waits for B's lock.
Thread 2 holds B's init lock and needs A — it waits for A's lock. Two init locks, opposite order, the same cycle
— but the locks are the JVM's, not yours, so nothing in the code looks synchronized.

**Only under concurrency.** Single-threaded, this does *not* deadlock — the thread already owns both init locks
and instead sees a **partially initialized** class (one static field still `null`), a different bug. The deadlock
needs two threads triggering the two initializers at the same moment.

**Fix — break the cyclic dependency between the static initializers.** Don't have two classes' static state
depend on each other; initialize lazily or move the shared state out:

```java
// e.g. resolve B lazily instead of in A's static initializer
static B b() { return Holder.INSTANCE; }   // no A<->B init cycle at class-load time
```

Handle: two classes whose static initializers each need the other deadlock on the JVM's init locks under two
threads — keep class initialization acyclic.

</details>

</details>

---

### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
// runs on the common ForkJoinPool
CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> {
  // blocks a common-pool thread waiting on more common-pool work
  return CompletableFuture
      .supplyAsync(() -> fetch())     // also on the common pool
      .join();                        // blocks THIS common-pool thread
});
f.join();
```

</details>

<details><summary>Show answer</summary>

**Common-pool starvation — blocking a pool thread inside a task on the same pool.** `supplyAsync` with no
executor runs on `ForkJoinPool.commonPool()`, whose size defaults to CPU cores − 1. The outer task holds one
common-pool thread and calls `.join()`, blocking it while it waits for an inner task that *also* needs a
common-pool thread. Do this on enough cores at once and every common-pool thread is blocked waiting for work that
can't be scheduled — the same starvation deadlock as a full fixed pool, hidden inside `CompletableFuture`.

**Why it's sneaky:** the common pool is shared process-wide (parallel streams use it too), so one component
blocking it can freeze unrelated code — and small machines (few cores → tiny common pool) hit it first.

**Fix — give blocking async work its own executor, and don't block inside common-pool stages:**

```java
Executor io = Executors.newFixedThreadPool(N);
CompletableFuture.supplyAsync(() -> fetch(), io)     // dedicated pool, not the common one
    .thenApply(s -> process(s));                     // compose instead of .join() inside a stage
```

Handle: blocking a `CompletableFuture` stage that runs on the shared common pool can starve it process-wide —
give blocking work a dedicated executor and compose rather than `join()` inside a stage.

</details>

</details>

---
