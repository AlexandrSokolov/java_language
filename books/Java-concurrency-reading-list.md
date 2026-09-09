# Concurrency — reading list

## Classic concurrency (pre-Java 21)

### Effective Java (3rd ed.) by Joshua Bloch — primary source
Concurrency chapter, items 78–84 (from `synchronized` access through thread-safety
documentation and lazy initialization). Start here for class-level, practical rules.

### The Well-Grounded Java Developer (2nd ed.) by Benjamin Evans et al. — second source
- Chapter 5 — concurrency fundamentals, the Java Memory Model, the design forces.
- Chapter 6 — the JDK concurrency libraries.
- Chapter 16 — advanced concurrent programming.

### Java Concurrency in Practice by Brian Goetz et al.
Addison-Wesley Professional, 2006. Single edition — never revised, so it predates
Java 7+ (no fork/join, `CompletableFuture`, or streams). Still the definitive depth
layer on the Java Memory Model and the reasoning behind safe publication and
visibility. Read it for the formal model, not for the modern API surface.

### The Art of Multiprocessor Programming (2nd ed.) by Herlihy, Shavit, Luchangco, Spear
Morgan Kaufmann, 2020. The standard text on the theory of concurrent algorithms and
data structures — locks, non-blocking structures, linearizability. The natural depth
layer past JCiP for the formal side, and unlike JCiP it is current.

### Modern Java in Action by Urma, Fusco, Mycroft
Manning, 2018. Book-length treatment of fork/join, parallel streams, and
`CompletableFuture` — exactly the post-2006 gap JCiP leaves. Skip if WGJD ch. 6/16
already cover enough of this for you.

## Project Loom era (virtual threads, structured concurrency, scoped values)

Everything above predates Project Loom, finalized in Java 21 (2023). Virtual threads
change the model: imperative blocking code that reads linearly and scales like the old
reactive stacks, so for I/O-bound work the callback machinery of Reactor/RxJava mostly
falls away. Caveat: the area is still moving — structured concurrency was reworked again
in Java 25 and scoped values are still stabilizing — so check which JDK any book targets
and supplement with current material (e.g. Inside.java).

### Modern Concurrency in Java by A N M Bazlur Rahman
O'Reilly, 2025. Full title: *A Deep Dive into Virtual Threads, Structured Concurrency,
and Scoped Values*. The most recent of the Loom books and the one most squarely aimed
at the new model. Author is a Java Champion and InfoQ Java editor. Start here for a
current, book-length treatment of Loom.

### Virtual Threads, Structured Concurrency, and Scoped Values by Ron Veen, David Vlijmincx
Apress, 2024. A compact, focused guide to exactly the three new Loom APIs — nothing
more. Good if you want a quick, targeted read rather than a full concurrency text.
Contrasts virtual threads against the traditional threading model of the last 25 years
and closes on scheduling strategies.

### Java Concurrency and Parallelism by Jay Wang
Packt, 2024. Covers the modern APIs with a cloud-native / microservices slant —
concurrency applied to distributed and cloud workloads. Weaker as a fundamentals text;
read it for the applied cloud angle, not the core model.

### Java Concurrency Patterns: Mastering Multithreading and Asynchronous Techniques by Peter Jones
2024. Patterns-oriented take on multithreading and async techniques. Limited public
information and few reviews, so treat as unverified until you can sample it.

### Mastering Java Concurrency: Threads, Synchronization, and Parallel Processing by Peter Jones
2024. Broad survey of threads, synchronization, and parallel processing. As above:
little public track record — verify quality before relying on it.

## Concurrent Algorithms

### Introduction to Algorithms by Cormen, Leiserson, Rivest, Stein
MIT Press, 3rd ed., 2009. The title undersells it — a serious, rigorous reference.
Recommended for both single-threaded and concurrent algorithms.

### The Algorithm Design Manual (3rd ed.) by Steven Skiena
Springer, 2020. A practical companion to Cormen — strong for both single-threaded
and concurrent algorithms.