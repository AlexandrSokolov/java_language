## Java Fundamentals

### Effective Java by Joshua Bloch

### The Well-Grounded Java Developer by Ben Evans

2nd Edition: Published in 2022

### Java in a Nutshell by Ben Evans

Ben Evans took over this legendary O'Reilly series from David Flanagan

8th Edition: Published in 2023 (Covered up to Java 17, deep focus on modern language enhancements).

### "Modern Java in Action"
Authors: Raoul-Gabriel Urma, Mario Fusco, and Alan Mycroft (Manning)

### Brian Goetz whitepapers

official Oracle whitepapers, specifically:
- "Data-Oriented Programming in Java" and 
- "Design Patterns in the Era of Modern Java"

---

## FP in Java, lambdas and streams

### "Functional Programming in Java" (2nd Edition, 2023)
Author: Venkat Subramaniam (Pragmatic Bookshelf)

### "Modern Java in Action"
Authors: Raoul-Gabriel Urma, Mario Fusco, and Alan Mycroft (Manning)

---

## The Idiomatic and Pragmatic Masters

Venkat Subramaniam: A master communicator who bridges the gap between complex language theory and pragmatic engineering. 
His books (like Functional Programming in Java) focus intensely on expressiveness, readability, and design simplicity.

Cay S. Horstmann: Author of the Core Java series. While technically a comprehensive reference, 
Horstmann’s books are distinct because they completely skip the academic hand-waving. 
He targets professional developers who need precise, 
highly technical explanations of why things are designed the way they are.

---

## Java Concurrency

### Java Concurrency in Practice by Brian Goetz

---

## Java Persistence

Since we were just looking at how enums break database mappings, you should know Vlad Mihalcea. 
He is the undisputed world authority on Java persistence (JPA, Hibernate, and database performance). 
His book, High-Performance Java Persistence, 
is a masterclass in how Java actually speaks to relational databases under the hood.

---

## Puzzle-driven, trap-focused

### Java Puzzlers: Traps, Pitfalls, and Corner Cases by Joshua Bloch and Neal Gafter

### Java Challenges: 100+ Proven Tasks that Will Prepare You for Anything" (2021)

### Java Puzzlers NG

The "Next Generation" Conference Puzzlers
"Java Puzzlers NG" (Next Generation)

Where to find it: YouTube (Search for talks by Tagir Valeev, Baruch Sadogursky, or Viktor Gamov)

Why it fits: The original book was born out of Bloch's legendary, standing-room-only talks at JavaOne. 
That tradition never died. Modern Java champions regularly present "Java Puzzlers NG" at conferences like Devoxx and JavaLand. 
These live, highly interactive quiz sessions focus entirely on the bizarre corner cases of modern features—showing you how lambdas, stream parallelization, switch pattern matching, and record serialization can trick even senior developers.

### The Living Archive
"The Java Specialists' Newsletter"

Author: Dr. Heinz Kabutz (Online / Digital)

Why it fits: Heinz Kabutz is essentially the keeper of the modern Java puzzle flame. For over two decades, he has published deep-dive technical newsletters that act as mini-puzzlers. He constantly dissects unexpected behaviors, memory leaks, and performance quirks introduced in newer versions of Java (JDK 11 through JDK 21+).

### 

---

## The Performance and JVM Titans

### Java Performance: In-Depth Advice for Tuning and Programming by Scott Oaks

### Optimizing Cloud Native Java (Ben Evans)
(released in late 2024) is the direct, heavily revised successor to his 2018 book, Optimizing Java.

"Optimizing Cloud Native Java" is the direct 2nd Edition evolution of 
"Optimizing Java: Practical Techniques for Improving JVM Application Performance". 
The authors took the core foundational physics from the original book 
(bytecode, JIT compilation mechanics, and hardware caching) and completely repackaged them alongside 
the modern realities of Java 17/21, containers, and cloud architecture.

The 2nd edition (Optimizing Cloud Native Java) includes everything. You can completely skip the 1st edition.

### Aleksey Shipilëv

Aleksey Shipilëv: A legendary former Oracle and Red Hat JVM performance engineer. 
He created the JMH (Java Microbenchmark Harness) and wrote the deep-dive specifications on the Java Memory Model. 
His technical write-ups (on shipilev.net) are brutally dense, deeply original, and the definitive truth on JVM internals.

### Dr. Heinz Kabutz 

Dr. Heinz Kabutz: Author of The Java Specialists' Newsletter. 
For over two decades, he has published deep-dive refactoring cases, concurrency puzzles, and memory-leak post-mortems. 
He doesn't write generic tutorials; he breaks the JVM on purpose to show you how it works.