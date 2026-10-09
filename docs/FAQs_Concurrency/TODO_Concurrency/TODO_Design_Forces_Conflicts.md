## Design Forces Conflicts

The next 2 cards were moved out from [1.2_Design_Forces.md](../1_Fundamentals/1.2_Design_Forces.md) file:

### What's the guiding principle when design forces conflict?
<details><summary>Show answer</summary>

Use the strongest technique the situation allows.
The [practical techniques](#what-are-the-practical-techniques-for-conflicting-design-forces)
are ranked strongest to weakest;
reach for the strongest one that fits, and drop to a weaker one only when nothing stronger is possible.

</details>

### What are the practical techniques for conflicting design forces?
<details><summary>Show answer</summary>

Ranked strongest to weakest — the stronger it is, the less it depends on others behaving:

1. **Restrict external communication.** Cut each subsystem's contact with the outside as much as possible;
   data hiding is the main tool.
2. **Make internal structure deterministic.** Design in static knowledge of the threads and objects in each subsystem,
   even when the subsystems interact concurrently and nondeterministically.
3. **Enforce a policy clients must follow.** Powerful, but relies on client apps cooperating —
   and it's hard to debug when a badly behaved app breaks the rules.
4. **Document the required behavior.** The weakest option — nothing enforces it — but sometimes the only one left
   when the code ships into a very general context.

The order is a spectrum: enforced by design → enforced by cooperation → enforced by hope.

</details>

### Design forces & conflicts — discussion parked for later

Status: **postponed.** We could not settle the "practical techniques for conflicting design forces"
material. Neither of us accepts the source's four-item list as written. That is not confidence that
we are right and the book is wrong — the more likely read is that we are missing a framing that a
different source will supply. Resume when other reading (another concurrency/architecture book) gives
a cleaner version of the same idea.

The four design forces themselves are not in dispute: **safety, liveness, reusability, performance.**
What is unresolved is (a) how they conflict and (b) the list of "techniques" for managing the conflicts.

---

#### Part 1 — The three tensions (this part we did resolve; keep it)

The original card stated each tension as a slogan with no mechanism, which made each slogan read as
wrong. We rewrote each concrete-first. These we agree on:

##### Safety vs liveness
- **Claude's mechanism:** the tension is not "stop bad things" vs "make progress" as abstract goals —
  it is the **tool**. Safety is bought with a *lock*; a lock makes a thread *wait*; waiting is the
  opposite of progress. The same tool that buys safety (block the thread) is the one that risks
  liveness (a thread that blocks and never unblocks = deadlock). Push locking harder → more blocking →
  more deadlock surface.
- **Alexandr:** accepted. The original "safety stops bad things, liveness needs progress, opposite
  goals" was rejected as not literally true — a system can do both most of the time. The lock-as-both-
  tools framing is the honest one.

##### Reusability vs safety
- **Claude's mechanism:** reusable code must let callers plug in their own code (comparator, callback,
  overridden method). That foreign code runs **while your lock is held**, and you did not write it —
  it may call back in (reentrancy), grab another lock (deadlock), or run forever. Foreign code inside
  your protected section can break the invariants the lock was protecting. Book's term: **alien
  method**.
- **Alexandr:** accepted, after rejecting the original "reusable systems expose their internals, and
  exposure breaks safety." Correct objection: reusable code exposes its *intended API*, not its
  internals — "exposure breaks safety" is too crude. The real tension is *running caller code under
  your lock*, not exposure.

##### Safety vs performance
- **Claude's mechanism:** a lock admits one thread at a time; others wait their turn; locked work runs
  single-file instead of in parallel. More/bigger locked sections → more of the program serialized →
  the less extra cores buy you.
- **Alexandr:** accepted (not contested).

**Handle for the set:** every gain is bought from another force; the job is choosing which trade to
make. (Router card built on this — the three tensions each became their own mechanism card.)

---

#### Part 2 — The unresolved part: "techniques for conflicting forces"

The source card ranked four techniques strongest→weakest:

1. Restrict external communication (data hiding / encapsulation).
2. Make internal structure deterministic (design in static knowledge of the threads/objects per
   subsystem, even under concurrent interaction).
3. Enforce a policy clients must follow (powerful but relies on client cooperation; hard to debug).
4. Document the required behavior (weakest; nothing enforces it).

Framed as a spectrum: enforced by design → enforced by cooperation → enforced by hope.

We could not accept this list. Objections below.

##### On #1 — encapsulation
- **Claude's mechanism:** private field reachable only through synchronized methods means *every*
  access goes through the lock; no back door skips it. A public field is a path around the lock, so
  safety becomes impossible regardless of synchronization. So encapsulation is the *precondition* that
  makes locking able to work.
- **Alexandr's objection (this is the strong one):** that shows encapsulation *achieves safety* — it
  is a technique for one force, **not** a technique for resolving a *conflict between* forces. The
  card is titled "techniques for conflicting forces," but #1 is not about a conflict at all. So #1 is
  mis-filed here; it belongs in the safety/FSO material ("how do you achieve safety").
- **Where we landed:** Alexandr is right. #1 does not belong on a "conflicts" card.

##### On #2 — deterministic internal structure
- **Claude's mechanism (attempted):** know at design time which threads exist and which objects they
  touch (fixed pool, fixed responsibilities) so safety/liveness become *analyzable* — you can only
  prove no deadlock/race if the structure is fixed and knowable.
- **Alexandr's objection:** for reusable code you **do not control** the execution structure — the
  caller picks the pool type, its configuration, the hardware limits, the concurrency. So "make the
  structure deterministic" is only available to someone who owns the *whole application* (a closed
  system), not to a library/component author. For the case that matters (reusable code) the technique
  is close to vacuous.
- **Where we landed:** Alexandr is right that it does not generalize. At most it survives demoted to
  "only when you own the entire app (closed system)." As a *general* technique it fails.

##### On #3 — enforce a policy the client must follow (the part with real content)
- **Alexandr's insight (he derived this, it is the keeper):** #3 maps onto the thread-safety spectrum
  we had already written:
    - **Unconditionally thread-safe** — mutable, enough internal synchronization that callers need none
      (`AtomicLong`, `ConcurrentHashMap`).
    - **Conditionally thread-safe** — safe except some sequences need external locking by the caller
      (`Collections.synchronized…` wrappers, whose iterators the client must lock).
- The sharp point: **there is no true "enforce" — only "document" — unless you take the control flow
  away from the caller.** Two levels:
    - *Expose the lock, tell the caller to use it* (the `synchronized…`-iterator pattern). You **cannot
      enforce** correct use; you can only document "hold this lock while iterating." A caller who
      forgets is unsafe and you cannot stop them. Lives on hope. This is conditionally thread-safe.
    - *Take the control flow* — template-method / consumer approach: acquire the lock internally and run
      the caller's code inside it (`withLock(Runnable)`, a consumer run while the lock is held,
      `ConcurrentHashMap.compute(key, BiFunction)` running the lambda under the bin lock). **Now** it is
      enforced, because the caller never touches the lock — you do, around their code.
- **Where we landed:** this is the one genuinely valuable, concrete thing in the whole card. The
  honest spectrum for "a rule the caller must obey" is:
  **take the control flow (enforced) → expose the lock + document it (not enforced, hope) → document a
  rule with no lock exposed (weakest hope).**

##### On #4 — document
- Agreed by both: weakest, but the **clearest**. The only option left when the code ships into a
  context so general you cannot hide anything or pin any structure down.

---

#### Why we are parking it

The source's four-item list mixes at least three different ideas under one "techniques for conflicts"
heading:
- #1 = a technique to **achieve safety** (not a conflict technique at all).
- #2 = a technique that **only works in a closed system you fully control** (fails for reusable code).
- #3/#4 = the real axis: **how to make a caller follow a rule**, where "enforce" collapses to
  "document" unless you seize the control flow.

We both found arguments against the list as written, and we both agree we are probably missing a
cleaner framing rather than having outsmarted the source. Too much to resolve cleanly now. Resume when
another book frames "managing the trade-offs between the four forces" — compare its structure against
the three points above and see whether it dissolves the confusion.

#### The one card worth writing when we return (do not lose this)
> **How do you make a caller follow a locking rule?** — two levels: (1) take the control flow (run the
> caller's code under your lock via a lambda/consumer — `ConcurrentHashMap.compute` is the anchor) =
> actually enforced; (2) expose the lock and document it (`synchronized…` iterator) = hope, not
> enforcement. Cross-link to the conditionally-thread-safe card. This is the same axis at the API-
> contract level.

