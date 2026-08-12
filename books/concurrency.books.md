## Concurrency — reading list

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

### Introduction to Algorithms by Cormen, Leiserson, Rivest, Stein
MIT Press, 3rd ed., 2009. The title undersells it — a serious, rigorous reference.
Recommended for both single-threaded and concurrent algorithms.

### The Algorithm Design Manual (3rd ed.) by Steven Skiena
Springer, 2020. A practical companion to Cormen — strong for both single-threaded
and concurrent algorithms.