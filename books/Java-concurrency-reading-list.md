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



TODO:
#### Book: Introduction to Algorithms (often called CLRS after the authors Cormen, Leiserson, Rivest, and Stein)

Where to look: Chapter 27: "Multithreaded Algorithms" (in the 3rd and 4th editions).

Why it's essential: Charles Leiserson, one of the authors of this book, 
is also the co-inventor of the Cilk language and the work-stealing scheduler. 
This chapter abstracts away threads entirely and teaches you how to model multithreaded computations as 
Directed Acyclic Graphs (DAGs). It covers the fundamental metrics of Work (total execution time on one processor) 
and Span (the critical path length). It then mathematically proves why randomized work-stealing is optimal 
for scheduling these graphs.

#### Book: The Art of Multiprocessor Programming by Maurice Herlihy, Nir Shavit, Victor Luchangco, and Michael Spear.

For the Data Structures and Low-Level Design
Where to look: Chapter 16: "Work-Stealing and Work-Dealing".

Why it's essential: This is arguably the definitive book on concurrent data structures. 
While CLRS proves the algorithm is mathematically optimal, this book teaches you how to actually build it in memory 
without killing performance with locks. It explains the Chase-Lev Deque, which is the lock-free, 
concurrent double-ended queue that powers almost every modern work-stealing runtime (including Java's ForkJoinPool). 
It explains the tricky memory-ordering and hardware synchronization required to let the "owner" thread operate on one 
end of the queue while "thieves" safely steal from the other.

#### Book: Structured Parallel Programming: Patterns for Efficient Computation by Michael McCool, Arch D. Robison, and James Reinders.

For the Architectural Design Patterns
Where to look: The chapters on the "Map", "Reduce", and "Fork-Join" patterns.

Why it's essential: Written by the architects of Intel Threading Building Blocks (TBB) and Cilk Plus. 
It doesn't focus on Java; instead, it focuses on how to look at a computational problem, divide it correctly, 
and map it to a runtime that uses work-stealing. 
It bridges the gap between raw data structures and high-level application design.

#### The Foundational Papers (Highly Recommended)
If you are willing to read academic papers, these two are highly readable and are considered the bibles of this topic:

"Scheduling Multithreaded Computations by Work Stealing" (1999) by Robert D. Blumofe and Charles E. Leiserson. 
This is the original paper that formalized how work-stealing works and proved its efficiency over "work-sharing" models.

"A Java Fork/Join Framework" (2000) by Doug Lea. 
Even though you aren't looking for Java APIs, Doug Lea (the author of Java's concurrency libraries) 
wrote this paper to explain the design decisions he made when translating the Cilk work-stealing algorithms 
into a managed language like Java. It is a masterclass in concurrent systems design.

Blumofe & Leiserson, "Scheduling Multithreaded Computations by Work Stealing" — 
the foundational proof: the T₁/P + O(T∞) time bound and the space bound. This is the theory.

Arora, Blumofe, Plaxton (ABP), "Thread Scheduling for Multiprogrammed Multiprocessors" — the first lock-free work-stealing deque.

Chase & Lev, "Dynamic Circular Work-Stealing Deque" — 
the growable/resizable variant that modern runtimes (including Java's) actually resemble. 
This is the "variations" you're after.

Frigo, Leiserson, Randall, "The Implementation of the Cilk-5 Multithreaded Language" — 
the work-first principle and the THE protocol. Cilk (MIT) is the origin of divide-and-conquer + work-stealing, 
and Java's Fork/Join is directly modeled on it — so this transfers to Java almost line-for-line.