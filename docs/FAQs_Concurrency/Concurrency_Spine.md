# Concurrency — The Spine (learning order)

The order to learn concurrency so each theme rests only on the ones before it. This is the JCP flow
(a book built to teach the topic bottom-up), topped up with modern themes the older order predates.
No book names here — this is the map of the topic itself. Book-to-theme mapping lives in the
companion file `Concurrency_Book_Map.md`.

## How to read a theme

Each theme is a **folder** (expect several card files inside). Under it:

- **Subtopics** — the things that live in this folder.
- **Depth ladder** — the same theme learned at three distances. Fill in this order:
  - **Rule** — what to do; a practice you can follow before you know why.
  - **Mechanism** — how the machine makes the rule true (JVM, bytecode, what `volatile` does to a read).
  - **Model** — the formal system underneath (happens-before as an ordering, safe publication as a theorem).

You act on a theme at Rule depth immediately; you don't wait for Model depth to start.

## The four parts (designer's altitude)

Themes are grouped into **parts** (JCP's term). A part is an altitude — it tells you *which kind of
question* you're answering when you sit down to a theme:

- **Part I — Fundamentals:** one object — how do I make *this* safe?
- **Part II — Structuring:** the application — how do I organize *work* across threads?
- **Part III — Liveness, performance, testing:** the running system — does it stay alive and fast *under load*?
- **Part IV — Advanced:** the machine floor — what's *underneath* the tools when they aren't enough?
- **Closing recap:** step back — weigh what it all cost. Read last, sits outside the four parts.

Parts II and III hold one theme each — the 9 themes are coarser than JCP's 16 chapters, so a part can
be single-tenant. The altitude still holds.

---

## Part I — Fundamentals
*Altitude: one object. How do I make this thing safe?*

### 1. The memory model (the root)

Why shared data needs synchronizing at all. Everything below is a consequence of this folder.

- **Subtopics:** visibility, instruction reordering, atomicity of reads/writes, happens-before, `volatile` as visibility.
- **Depth ladder:**
  - Rule — synchronize all access to shared mutable data.
  - Mechanism — the threading model and the bytecode: lost updates, unsynchronized reads, volatile access.
  - Model — happens-before as a formal ordering; the Java Memory Model.

### 2. Safe sharing of one object

Given the memory model: how to share a single object safely.

- **Subtopics:** thread confinement, immutability, safe publication, escape, `volatile` vs locks, lazy init.
- **Depth ladder:**
  - Rule — confine, or make immutable, or publish safely; avoid excessive synchronization.
  - Mechanism — synchronization and locks, the `volatile` keyword, how immutability removes the problem.
  - Model — publication and escape, safe-publication guarantees, confinement as a discipline.

### 3. Composing thread-safe objects

Building bigger safe objects out of safe parts, and recording what's safe so others use it right.

- **Subtopics:** designing a thread-safe class, instance confinement, delegating thread safety,
  adding functions to existing safe classes, documenting the policy (`@GuardedBy`).
- **Depth ladder:**
  - Rule — document thread safety; state the level a class guarantees.
  - Mechanism — (light) immutability and confinement as building tools.
  - Model — state ownership, delegation, designing/documenting a synchronization policy.

### 4. Building blocks (ready-made safe objects)

Stop hand-rolling synchronization; use the objects the platform already made safe.

- **Subtopics:** synchronized vs concurrent collections, blocking queues, synchronizers (latch,
  semaphore, barrier), atomic classes, copy-on-write.
- **Depth ladder:**
  - Rule — prefer concurrency utilities to `wait`/`notify`.
  - Mechanism — how each block works: atomic classes, lock classes, `CountDownLatch`,
    `ConcurrentHashMap`, `CopyOnWriteArrayList`.
  - Model — synchronizers and concurrent collections as the standard vocabulary.

## Part II — Structuring concurrent applications
*Altitude: the application. How do I organize work across threads?*

### 5. Task execution

Stop thinking in threads; think in tasks handed to a runner.

- **Subtopics:** Executor framework, thread pools and sizing, `Future`/`FutureTask`,
  `CompletableFuture`, fork/join and work-stealing, parallel streams, cancellation and shutdown.
- **Depth ladder:**
  - Rule — prefer executors, tasks, and streams to raw threads.
  - Mechanism — fork/join, work-stealing, `CompletableFuture`, parallel streams.
  - Model — task execution, cancellation/shutdown, thread-pool sizing and saturation policy.

## Part III — Liveness, performance, testing
*Altitude: the running system. Does it stay alive and fast under load?*

### 6. Liveness & performance

What breaks when you compose everything above.

- **Subtopics:** deadlock, livelock, starvation, lock contention, context-switch cost, scalability,
  not depending on the scheduler, testing concurrent code.
- **Depth ladder:**
  - Rule — avoid excessive synchronization; don't depend on the thread scheduler.
  - Mechanism — deadlocks, sources of overhead, work-stealing as a scalability answer.
  - Model — liveness hazards, performance/scalability analysis, testing concurrent programs.

## Part IV — Advanced topics
*Altitude: the machine floor. What's underneath the tools when they aren't enough?*

### 7. Advanced tools

The lower-level machinery you reach for once the standard blocks aren't enough.

- **Subtopics:** explicit locks (`ReentrantLock`, read-write), building custom synchronizers (AQS,
  condition queues), atomic variables and non-blocking algorithms (CAS).
- **Depth ladder:**
  - Rule — reach for these only when a ready-made block won't do.
  - Mechanism — how explicit locks and atomics behave.
  - Model — custom synchronizers, non-blocking synchronization, CAS.

### 8. Other concurrency models (contrast)

Not Java-core; kept to see the alternatives that sharpen the Java model by comparison.

- **Subtopics:** Kotlin coroutines, Clojure STM and agents.
- **Depth ladder:** single pass — contrast cards only, no three-depth build.

## Closing recap (stands outside the parts)
*Altitude: step back. Weigh what every part cost against what it won.*

### 9. The trade-offs (what you win, what you pay)

Read this last — a trade-off only means something once both sides are real. You can weigh "safety
costs performance" only after you've written a lock and measured what it cost. First it's a slogan;
last it's the summary that ties the themes together.

You add threads to win **throughput** and **responsiveness**. You pay in three currencies:

- **Safety** — more ways for shared data to go wrong.
- **Liveness** — more ways to get stuck (deadlock, starvation).
- **Overhead** — locks, context switches, and coordination eat part of the win.

- **Subtopics:** the gains (throughput, responsiveness); the three costs; why they pull against the
  gain; how far parallel speed-up can go (Amdahl's law).
- **Depth ladder:**
  - Rule — favour correctness first; trade for speed only when measured.
  - Mechanism — where overhead comes from: contention, context switches, coordination.
  - Model — the forces as competing goals; Amdahl's law as the hard limit on the payoff.

---

## Modern top-ups the classic order predates

Fold these into the themes above as you meet them; they are not a separate folder:

- `CompletableFuture`, parallel streams, fork/join → theme 5.
- Virtual threads / structured concurrency (if you add them later) → theme 5, with a note in theme 6
  on how they change the liveness picture.
