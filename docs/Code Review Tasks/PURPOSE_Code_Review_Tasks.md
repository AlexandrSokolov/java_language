# Purpose: Code Review Tasks

## What this card type is for

A code review task is a larger piece of code with **several real defects, usually across more than one area**
(immutability, encapsulation, exception choice, API design, and so on). The card **does not give the answer.**
The reader writes a full review, sends it in, and the AI **grades that review** against a hidden key.

This is a senior-level tool. Its job is to measure the reader's **broad and deep** Java knowledge: not just
"can you spot the bug" but "do you see the trade-offs, can you offer alternatives, do you know why the code
should or should not exist in that form."

A task may be scoped to one area on purpose (e.g. all-generics, all-immutability). Even then it must carry
**multiple issues** — that is what separates a review task from a diagnostics card.

## How it is used (the loop)

1. The card presents the code and the instructions. No answer is shown.
2. The reader reviews it: for each finding, name the defect, say *why* it matters (the failure it causes), and
   where relevant question whether the code should exist in that form at all. Findings are ordered worst-first.
3. The reader sends the review to the AI.
4. The AI grades it against a hidden key: what was caught, what was missed, what was misjudged, and where the
   reasoning or the trade-off analysis could go deeper. The AI returns a report and, if useful, how to improve.

## What the card author (AI) must prepare

- **The review snippet.** One coherent piece of code — a class, a small hierarchy, a pair of classes — carrying
  several planted defects across the intended areas. Real code, not a list of disconnected lines.
- **A hidden grading key**, not shown in the card. The key lists every planted defect worst-first, the failure
  each causes, the fix or redesign, the trade-offs and alternatives a strong answer would raise, and the
  distinction between a must-fix and a judgment call. The key is the standard the reader is graded against.

The key is where the depth lives. It is allowed — expected — to hold trade-offs, multiple valid designs, and
"it depends" branches. That richness is what a senior review is scored on.

## Grading stance

- Credit what was caught, plainly.
- Name what was missed, worst-first, with the failure it causes.
- Flag anything misjudged (a real issue called a non-issue, or the reverse).
- Push on depth: where a finding was correct but shallow, show the trade-off or alternative that a stronger
  answer would have reached.
- Do not inflate. The purpose is an honest read of broad knowledge, so a missed area is stated, not softened.

## Format

The task card holds a focus line, usage instructions, and the code. No answer block.

````
# Code Review Task N

**Focus area:** [area(s), or "mixed"]
**How to use:** Review the code below and write your findings. For each issue, name the defect, say *why* it
matters (the failure it causes), and — where relevant — question whether the code should exist in that form at
all. Order your findings worst-first. Then send them to me to grade.

```java
[the code under review]
```
````

The grading key is kept in a separate place the reader does not see until after grading, or held by the AI.

- Java code uses 2-space indentation.
- The code should read like something a real developer wrote — plausible, not a contrived defect list.
