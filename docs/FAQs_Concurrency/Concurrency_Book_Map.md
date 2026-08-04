# Concurrency — The Book Map

Companion to `Concurrency_Spine.md`. The spine is the topic's own order. This file is the **reading
order**: each book top-to-bottom as you actually go through it, with every chapter pointing to the
spine theme (1–9) it feeds. You read a book in its own order; the arrow tells you which folder the
cards land in.

Books in your order: **Effective Java → The Well-Grounded Java Developer → Java Concurrency in Practice**.

Depth each book tends to hit (from the spine's ladder): EJ = **Rule**, WGJD = **Mechanism**, JCP = **Model**.

Spine themes: 1 memory model · 2 safe sharing · 3 composing · 4 building blocks · 5 task execution ·
6 liveness & performance · 7 advanced tools · 8 other models · 9 trade-offs.

---

## Book 1 — Effective Java (Chapter 11: Concurrency)

Read as: the **rules** pass. Fills the shallow end of several themes at once.

| Item | Title                                           | → Spine theme                             |
|------|-------------------------------------------------|-------------------------------------------|
| 78   | Synchronize access to shared mutable data       | 1 memory model · 2 safe sharing           |
| 79   | Avoid excessive synchronization                 | 2 safe sharing · 6 liveness & performance |
| 80   | Prefer executors, tasks, and streams to threads | 5 task execution                          |
| 81   | Prefer concurrency utilities to `wait`/`notify` | 4 building blocks                         |
| 82   | Document thread safety                          | 3 composing thread-safe objects           |
| 83   | Use lazy initialization judiciously             | 2 safe sharing                            |
| 84   | Don't depend on the thread scheduler            | 06.1_Liveness_Problems.md                 |

Note: EJ items scatter across themes — that scatter is why item order can't be the folder structure.

---

## Book 2 — The Well-Grounded Java Developer

Read as: the **mechanism** pass. Opens the JVM so the rules become consequences you can see.

### Chapter 5 — Java concurrency fundamentals

| Section | → Spine theme |
|---------|---------------|
| 5.1 Concurrency theory primer (Thread, hardware, Amdahl's law, threading model) | 1 memory model (threading model) · 9 trade-offs (Amdahl) |
| 5.2 Design concepts — Safety and concurrent type safety | 2 safe sharing |
| 5.2 Design concepts — Liveness | 6 liveness & performance |
| 5.2 Design concepts — Performance | 6 liveness & performance |
| 5.2 Design concepts — Reusability | 4 building blocks (soft; could be 3) |
| 5.2 Design concepts — How/why the forces conflict | 9 trade-offs |
| 5.2 Design concepts — Sources of overhead | 9 trade-offs · 6 liveness & performance |
| 5.3 Block-structured concurrency (synchronization, locks, deadlocks, `volatile`, thread states, immutability) | 2 safe sharing · 6 liveness (deadlocks) |
| 5.4 The Java Memory Model | 1 memory model |
| 5.5 Understanding concurrency through bytecode (lost update, sync in bytecode, unsync reads, volatile access) | 1 memory model |

### Chapter 6 — JDK concurrency libraries

| Section | → Spine theme |
|---------|---------------|
| 6.1 Building blocks for modern concurrent applications | 4 building blocks |
| 6.2 Atomic classes | 4 building blocks (· 7 advanced for CAS depth) |
| 6.3 Lock classes (Condition objects) | 4 building blocks (· 7 advanced) |
| 6.4 `CountDownLatch` | 4 building blocks |
| 6.5 `ConcurrentHashMap` | 4 building blocks |
| 6.6 `CopyOnWriteArrayList` | 4 building blocks |
| 6.7 Blocking queues (`BlockingQueue` APIs, `WorkUnit`) | 4 building blocks (· 5 task execution as producer/consumer) |
| 6.8 Futures (`CompletableFuture`) | 5 task execution |
| 6.9 Tasks and execution (modeling tasks, executors, single-thread/fixed/cached pools, `ScheduledThreadPoolExecutor`) | 5 task execution |

### Chapter 16 — Advanced concurrent programming

| Section | → Spine theme |
|---------|---------------|
| 16.1 The Fork/Join framework (work-stealing) | 5 task execution · 6 liveness (scalability) |
| 16.2 Concurrency and functional programming (`CompletableFuture`, parallel streams) | 5 task execution |
| 16.3 Under the hood with Kotlin coroutines | 8 other models |
| 16.4 Concurrent Clojure (STM, futures/pcalls, agents) | 8 other models |

---

## Book 3 — Java Concurrency in Practice

Read as: the **model** pass. The book whose order *is* the spine — read straight through, it deepens
each theme to Model depth. (Older APIs; trust it for the reasoning frame, top up code from Book 2.)

### Part I — Fundamentals

| Chapter | → Spine theme |
|---------|---------------|
| 2 Thread safety (atomicity, locking, guarding state) | 1 memory model · 2 safe sharing |
| 3 Sharing objects (visibility, publication/escape, confinement, immutability, safe publication) | 1 memory model · 2 safe sharing |
| 4 Composing objects (thread-safe class design, confinement, delegation, documenting policy) | 3 composing thread-safe objects |
| 5 Building blocks (sync/concurrent collections, blocking queues, synchronizers) | 4 building blocks |

### Part II — Structuring concurrent applications

| Chapter | → Spine theme |
|---------|---------------|
| 6 Task execution | 5 task execution |
| 7 Cancellation and shutdown | 5 task execution |
| 8 Applying thread pools (sizing, saturation policy) | 5 task execution · 6 liveness (sizing) |
| 9 GUI applications | (skip / optional — single-thread confinement example) |

### Part III — Liveness, performance, and testing

| Chapter | → Spine theme |
|---------|---------------|
| 10 Avoiding liveness hazards (deadlock, starvation, livelock) | 6 liveness & performance |
| 11 Performance and scalability (Amdahl, context switches, contention) | 6 liveness & performance · 9 trade-offs (Amdahl) |
| 12 Testing concurrent programs | 6 liveness & performance |

### Part IV — Advanced topics

| Chapter | → Spine theme |
|---------|---------------|
| 13 Explicit locks (`ReentrantLock`, read-write) | 7 advanced tools (· 2 safe sharing) |
| 14 Building custom synchronizers (AQS, condition queues) | 7 advanced tools |
| 15 Atomic variables and non-blocking synchronization (CAS) | 7 advanced tools |
| 16 The Java Memory Model (happens-before) | 1 memory model |
| Appendix A — Annotations for concurrency (`@GuardedBy`, etc.) | 3 composing thread-safe objects |

---

## How the two files work together

- **Learning a book:** read it top-to-bottom; for each chapter, the arrow says which spine folder the
  cards go in. You never reorder the book.
- **Understanding the topic:** read the spine; it tells you the right order and how deep each theme goes.
- **Same theme, three books:** a theme's folder fills across all three passes — Rule from Book 1,
  Mechanism from Book 2, Model from Book 3. One folder, three visits, no reshuffling.
