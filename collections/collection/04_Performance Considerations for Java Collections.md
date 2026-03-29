### Why is collection performance difficult to predict?
<details>
<summary>Show answer</summary>

Unfortunately, this is very difficult both to predict in theory and to assess in practice.

[**Many factors contribute to it**.](#what-factors-affect-the-performance-of-java-collections)


</details>

---

### What factors affect the performance of Java collections?
<details>
<summary>Show answer</summary>

- How often any of the collection’s operations are executed
- Which operations are executed most frequently
- The time cost of each of the operations that are executed
- How many orphaned objects each produces, and what overhead is incurred in collecting them
- The locality properties (discussed in the following subsection) of the collection
- How much parallelism is involved, both at instruction and thread level

</details>

---

### Why is premature optimization discouraged when working with collections?
<details>
<summary>Show answer</summary>

Understanding how these factors shape real‑world performance is part of performance tuning, 
best captured by Donald Knuth’s well‑known 1974 rule:
"_Premature optimization is the root of all evil._"

The explanation of this remark is twofold.
1. First, for many programs, performance is just not a critical issue.
   If a program is rarely executed or already uses few resources, optimization is a waste of effort,
   and indeed may well be harmful.
2. Second, even for performance-critical programs, accurate measurement is essential, but intuition is unreliable.

</details>

---

### Why is accurate measurement essential for evaluating collection performance?
<details>
<summary>Show answer</summary>

Assessing which part is critical normally requires **accurate measurement**;

In the same paper, Donald Knuth added about failed intuition:

“_It is often a mistake to make a priori judgments about what parts of a program are really critical,
since the universal experience of programmers who have been using measurement tools
has been that their intuitive guesses fail._”

This carries an important implication about comparing the performance of different collections.

</details>

---

### Can you give an example of how different collection designs lead to different performance trade‑offs?
<details>
<summary>Show answer</summary>

For example, `CopyOnWriteArrayList` provides highly efficient concurrent read operations
at the cost of very expensive writes.

So to use it in a system that requires highly performant concurrent access to a List,
you have to have confidence, gained by measurement if necessary,
that read operations greatly outnumber writes.

</details>

---

### What is a common way to measure how algorithm performance changes with increasing data size?
<details>
<summary>Show answer</summary>

A common way to measure how algorithm performance changes with increasing data size is to use **Big‑O notation**, 
which describes how the running time grows relative to the size of the input. 
It focuses on the overall growth trend rather than exact timings, 
making it easy to compare different operations or algorithms.


Big‑O captures the growth trend instead of focusing on exact timings. 

</details>

---

### What is the time complexity of common collection operations?
<details>
<summary>Show answer</summary>


Big-O notation:

|  **Time**  | **Common Name** | **Effect on the execution count <br/> if N is doubled** |                 	**Example algorithms**                  |
|:----------:|:---------------:|:-------------------------------------------------------:|:--------------------------------------------------------:|
|    O(1)    |    Constant     |                        Unchanged                        |               Insertion into a hash table                |
|  O(log N)  |   Logarithmic   |             Increased by a constant amount              |                  Insertion into a tree                   |
|    O(N)    |     Linear      |                         Doubled                         |                      Linear search                       |
| O(N log N) |                 |        Doubled plus an amount proportional to N         |                        Merge sort                        |
|   O(N^2)   |    Quadratic    |                   Increased fourfold                    | Insertion sort worst case <br/> (input in reverse order) |


</details>

---