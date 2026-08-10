# Purpose: Design Challenge Cards

## What this card type is for

A design challenge is an open question that asks the reader to **design something** — model a hierarchy, place a
property, choose a pattern, shape an API. The answer is not a single fact. It is a **way of thinking**: ask the
clarifying questions, propose more than one design, name the trade-offs of each, and pick a default with the
reason.

This is the highest-level tool, the one closest to a real senior/architect interview. At that level the
interviewer is not checking for one right answer — the back-and-forth itself reveals how deeply the person
understands the trade-offs. The reader knows in advance that their first answer is usually not complete; that is
the point of the level, and working through the gap is where the learning happens.

So this card type is collaborative. The AI is a **peer working the problem with the reader**, not a grader
handing back a score. It helps the reader reach the parts of the design they did not get to on their own.

## How it is used

1. The card poses the design question.
2. The reader gives their current thinking — a draft design, some questions, a partial trade-off analysis.
3. The AI works *with* them: surfaces the clarifying questions the reader should have asked, shows the designs
   they missed, lays out the trade-offs (what each design buys and costs), and names the discriminator that
   decides between them. It fills the gap between the reader's draft and a complete senior answer.

The reader has said their own answer is usually not enough here, and wants help going through it. Treat the card
as a discussion to work, not a test to mark.

## What a complete answer looks like (the shape to model)

The strong design answers follow a recognizable arc:

- **Draft answer** — the typical first instinct, stated plainly, even when it is not yet the right design.
- **Why the draft is not final** — the specific defects: which contract it breaks, which explosion it invites,
  which invariant it loses.
- **Correct design** — the design that fixes those defects, with the code.
- **Alternative design(s)** — a real second option, each with **buys** and **costs** stated, not hidden.
- **The discriminator** — the single test that decides which design to use (e.g. "one behavior chosen from
  alternatives → strategy; many behaviors combining at once → decorator"; "applies-to-all is not the test, free
  combination is").

The trade-offs and alternatives are the substance. A design card that gives one answer with no alternative and
no discriminator has missed the level.

## Format

Collapsible sections. A challenge may hold a question block and an answer block; a worked example may show the
draft, the critique, and the correct design in sequence.

````
### [Short design question]
<details><summary>Show answer</summary>

**Draft answer — [what it answers, and that it is not yet right].**
[the draft, with code]

**Why this draft is not the final design:**
- [defect 1: the contract/invariant/explosion it hits]
- [defect 2]

**Correct design:** [the fix, with code]

**Alternative design ([axis]):** [second option]
- **Buys:** [what it gains]
- **Costs:** [what it gives up]

[The discriminator — the one test that decides.]

</details>
````

For a question the reader should attempt cold first, wrap the prompt in its own block:

````
### [Design challenge title]
<details><summary><strong>Show details</strong></summary>

<details><summary>Show question</summary>

[the open design question]

</details>

<details><summary>Show answer</summary>

[worked answer in the arc above]

</details>

</details>
````

- Java code uses 2-space indentation.
- Cross-links between related design cards are welcome — the reasoning on one card often powers another, and a
  link keeps the map traversable instead of repeating the argument.
- Plain words in the prose; richer vocabulary is fine in discussion, but the design *questions* stay plain.
