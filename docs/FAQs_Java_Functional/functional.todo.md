
### How does functional programming reframe Strategy and Decorator?
<details><summary>Show answer</summary>

Strategy and Decorator are, at heart, just *passing a function* — once functions are first-class you don't need a
class to vary behavior:
- **Strategy → a function parameter** — pass the algorithm directly (`x -> x * x`) instead of a `Strategy` interface
  plus implementing classes.
- **Decorator → a wrapper function** — `withLogging(f)` returning an enhanced function is function composition, not a
  wrapper class.

So FP reframes a chunk of the GoF catalog: where OOP reaches for a class to vary behavior along an axis, FP often just
passes a function — same goal, far less ceremony. The patterns shrink to functions.

</details>

### Functional decoration vs the OOP Decorator?
<details><summary>Show answer</summary>

Both add behavior in layers without subclassing, but they decorate **different kinds of things**.

**The real marker: is the decorated thing a *value/transform*, or a *stateful, behavioral object*?**

- **Functional wins when you're transforming a value.** If each layer takes the thing and returns an enhanced version
  (`T -> T`), decoration is just function composition — chain the transforms with `andThen`. No interface, no abstract
  decorator, no wrapper classes. Adding a layer is one new function.
- **OOP Decorator wins when you're wrapping a behavioral object** — one with identity, state, or a multi-method
  contract you must preserve. The wrapper holds the real object and intercepts calls to it; the object graph stays
  intact.

**Where functional decoration actively loses, even though it looks simpler:**

- **Identity must be preserved.** A `T -> T` transform returns a *new* value each layer; callers holding a reference
  to the original, or relying on `==`/`instanceof`, are broken. The OOP Decorator wraps the same instance.
- **The layer carries rich state.** A decorator adding a buffer, a cache, or a connection across calls needs somewhere
  to keep that state. A pure transform is stateless; forcing state into closures gets ugly fast.
- **The contract is genuinely multi-method and you need all of it.** Collapsing a many-method type into "rebuild the
  whole thing each layer" stops being cheaper than a wrapper once the type is non-trivial — you reconstruct everything
  on every call.
- **Each layer intercepts behavior, not data.** Logging, access control, or caching *around a service* is wrapping a
  behavioral contract — the OOP Decorator's home ground. Functional fits when each layer *transforms a value*.

**The practical heuristic** (true most of the time, for the right reason): if you genuinely have a single-method
functional interface to decorate, functional almost always wins — because that situation only arises when the thing is
already a value/transform. The moment the thing is a stateful or multi-method object, reach for the OOP Decorator
despite its extra ceremony. [→ functional decoration code example](3.8_classes_and_interfaces_design.md#what-does-functional-decoration-look-like-in-code)

</details>


### What does functional decoration look like in code?
<details><summary>Show answer</summary>

Model the decorated thing as an immutable value and each decoration as a `T -> T` transform, then **compose** the
transforms. No interface, no abstract decorator, no wrapper classes:

```java
// The decorated thing is a value, not a behavioral object
record Coffee(double cost, String description) { }

// Each decoration is a Coffee -> Coffee transform
UnaryOperator<Coffee> milk  = c -> new Coffee(c.cost() + 0.5, c.description() + ", milk");
UnaryOperator<Coffee> sugar = c -> new Coffee(c.cost() + 0.2, c.description() + ", sugar");

// Stacking decorations becomes andThen composition
Coffee result = milk.andThen(sugar).apply(new Coffee(2.0, "Simple coffee"));
// result -> Coffee[cost=2.7, description="Simple coffee, milk, sugar"]
```

Each `andThen` is one more layer; adding a feature is a **new function, not a new class**. Compare the OOP Decorator
for the same example — interface, abstract `CoffeeDecorator`, and a concrete class per feature — all of which
collapse here into two lambdas, *because* `Coffee` is pure data. The instant `Coffee` needed identity or state, this
would break and the wrapper-class version would be the right one.
[→ functional decoration vs the OOP Decorator](#functional-decoration-vs-the-oop-decorator)

</details>
