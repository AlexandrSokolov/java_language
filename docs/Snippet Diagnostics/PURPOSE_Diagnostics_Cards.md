# Purpose: Diagnostics Cards

## What this card type is for

A diagnostics card shows a small code snippet with a defect, and the card **gives the answer**. It is a
learning tool for the start of a topic: you read the snippet, name the problem from memory, then reveal the
stored answer and check yourself. You can walk through many traps fast this way and build a catalog of "I have
seen this failure before."

The reader answers from memory first, then reveals. So the answer lives in the card.

## Scope rule (the thing that defines this type)

**Narrow scope.** One defect, or a few points that all sit inside the *same* scope. Not a grab-bag of unrelated
issues across the whole snippet — that is a code review task, a different tool.

The test: can you write **one closing handle line** that covers every point in the answer? If yes, the scope is
tight enough. If the answer needs three unrelated handles, the card is too wide — split it, or move it to a
review task.

A secondary point is allowed only when it lives inside the same scope as the main defect. Example: a card on a
check-then-act race may also note the fix collapses the two calls into one — that is the same scope. A card
that lists "missing shutdown, plus a wasteful per-call pool, plus a swallowed exception" is three scopes — too
wide for this type.

## What the answer contains

- The defect, named plainly, with the concrete failure it causes (what actually goes wrong at runtime).
- The fix, as a short code block.
- Where it matters: whether the outcome is guaranteed or only typical (say so if it is not guaranteed).
- A one-line **handle** at the end — the general principle that compresses what was just shown. Concrete fact
  first, general handle last.

Keep it minimal. A line earns its place only if removing it makes the answer wrong or incomplete on that card's
scope. Cut anything that merely "relates."

## Format

Nested collapsible sections: outer wrapper, then Show code, then Show answer.

````
### Describe a code snippet #NN
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
[snippet]
```

</details>

<details><summary>Show answer</summary>

[answer: defect + concrete failure, fix as code, handle line last]

</details>

</details>

---
````

- Stems are neutral and abstract: `Describe a code snippet #NN`. No hint of the topic in the stem.
- Java code uses 2-space indentation.
- Multiple cards in one file are separated by `---`.
- The answer is synthesized in plain words, never copy-pasted from a source.

## Duplication is wanted

The same defect seen from a different angle, on a different snippet, is a feature, not waste. Repeating a trap
across framings (a lost update on a single field, then on a map counter, then on an ID generator) is how the
skill sticks. Duplicate the important traps on purpose; keep the *snippet* and the *framing* different, not the
conclusion.

## Vocabulary

Plain, common words — the words a plain-spoken engineer says out loud. A rare word may appear only when it *is*
the term being learned, and then it is glossed in plain words immediately. A high-vocabulary word in the stem or
answer is a false-failure trap: the reader fails the word, not the knowledge.
