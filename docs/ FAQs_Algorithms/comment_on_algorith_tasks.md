# Algorithm tasks in a senior Java interview

## If they give you a task

Say this in about 30 seconds, then start working on the task.

> "That's a fair question. It shows how someone works through a problem. I'll be open with you: puzzle-style
> algorithm tasks haven't been part of my daily work for a long time. My work has been business systems: REST
> services, databases, messaging, integrations, notifications. The performance problems I solved there were a
> different kind: N+1 queries, the wrong collection where it's called millions of times, missing indexes, too many
> single network calls instead of batches. So I think about cost and complexity every day, just at the system level.
> Let me work through this one out loud. I'll start with a simple solution that works, then see how to make it
> faster."

## If they ask "How strong are you in algorithms?"

> "Honestly, it's not my strongest area. I haven't trained puzzle tasks for years. Where I'm strong is the data
> structures and costs that show up in real systems: which collection to use, what a lookup costs, when to let the
> database do the work. If the team needs more than that, I'll close the gap."

## Cheap insurance

The statement lands much better if you can then do three things: state the simple solution that works, say its cost
in Big-O, and suggest one improvement. This does not need the whole puzzle universe. The Collections study already
covers most of it; it only needs to be connected to performance. An evening or two of practice is enough.

Many beginner-level tasks come down to one move. A nested `for` loop compares every item with every other item, which
is O(n²). Replace it with a single pass that stores items in a `HashMap` or `HashSet` and looks them up, which is O(n).
When a lookup does not help, the next move is usually to sort first, which is O(n log n).

If you get stuck, this sentence is a safe default:

> "The simple way is a nested loop, which is O(n²). To make it faster, I'd load the data into a `HashMap` first.
> Each lookup then costs constant time on average instead of a scan, so the whole task drops to O(n)."