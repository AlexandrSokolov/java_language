### How to model a variable restricted to one of a few known alternatives?
<details><summary>Show answer</summary>

* **Primitive constants** (`public static final int`/`String`) — group of named literals; 
  no compiler enforcement that a parameter actually holds one of them.
* **Typesafe enum pattern (pre-Java 5)** — a class with a private constructor exposing a fixed set of
  `public static final` instances; the manual precursor to the language-level construct.
* **Enum type** — first-class language feature; one instance per constant, can hold fields and methods,
  exhaustive in `switch`, serializable, comparable by identity.
* **Sealed interface or class** (Java 17+) — closed hierarchy of permitted subtypes; 
  each alternative can carry its own shape and behavior; exhaustive in pattern `switch`.

</details>

### What should be considered before adopting the int/String constant pattern?
<details><summary>Show answer</summary>

* **No distinct type** — the parameter is still `int` or `String`, so any value of that type is accepted.
  Constants from unrelated groups are interchangeable, typos in string variants surface only at runtime, the
  namespace is flat (prefix conventions stand in for a type name), and the compiler cannot warn when a
  `switch` over the set is non-exhaustive.
* **No attached data or behavior** — each member is a bare literal. There is no place to hang associated
  fields (e.g. a season's average temperature) or methods (e.g. `next()`), no built-in way to iterate or
  count the members, and no readable name in logs or stack traces — only the underlying number or string.
* **Inlined values break consumers** — `static final` primitives are compiled directly into client class
  files. Renumbering on the producer side leaves every consumer running with stale values until it is
  recompiled, which makes the set effectively frozen across deployment boundaries.

</details>

### What is Java's enum type?
<details><summary>Show answer</summary>

A reference type whose entire population is a fixed roster of named instances declared at compile time. 
The language guarantees that no other instances of the type can ever exist.

* **Distinct type with singleton members** — each constant is a unique, final instance of an implicit subclass
  of `java.lang.Enum`. A parameter declared as the enum type accepts only those instances; identity equality
  (`==`) is safe; the namespace is the type itself, no prefix conventions needed.
* **Carries data and behavior** — constants are constructed via a private constructor and can hold instance
  fields, expose methods, and even override methods per constant via constant-specific class bodies.
* **Built-in language and library integration** — `values()`, `valueOf(String)`, `name()`, `ordinal()`,
  works as a `switch` selector with exhaustiveness checks under pattern `switch`, implements `Comparable`
  and `Serializable`, and is safe across class-loader and serialization boundaries.

</details>

### Enum example
<details><summary>Show answer</summary>

```java
public enum Planet {
  MERCURY(3.303e+23, 2.4397e6),
  VENUS(4.869e+24, 6.0518e6),
  EARTH(5.976e+24, 6.37814e6);

  private final double mass;     // kilograms
  private final double radius;   // meters

  Planet(double mass, double radius) {
    this.mass = mass;
    this.radius = radius;
  }

  public double surfaceGravity() {
    return 6.67300E-11 * mass / (radius * radius);  // G * M / r^2
  }
}

// Usage — the parameter type itself enforces the closed set.
double weightOnEarth = mass * Planet.EARTH.surfaceGravity();
for(Planet p :Planet.values()){
  System.out.
  printf("%s: g = %.2f m/s^2%n",p, p.surfaceGravity());
}
```

</details>

### How do enumerated types compare across Java, C#, and C++?
<details><summary>Show answer</summary>

* **Underlying representation** — Java enums are reference-type objects; each constant is a singleton
  instance of an implicit subclass of `java.lang.Enum`. C# enums are value types backed by an integral type
  (default `int`) — each constant is literally a number with a name. C++ enums, in both the unscoped `enum`
  and scoped `enum class` forms, are pure integral constants with no runtime metadata.
* **Type safety and casting rules** — Java forbids casts between integers and enums; the closed set is
  enforced statically. C# allows free `(MyEnum)42` casting, including values that were never declared — the
  result is a perfectly legal `MyEnum` with no name. C++ unscoped enums implicitly convert to and from
  integers; `enum class` requires an explicit cast but still admits out-of-range values via `static_cast`.
* **Attached state and behavior** — Java constants can carry fields, run constructors, define methods, and
  override behavior per constant. C# enums cannot hold instance data or define methods directly; the
  idiomatic workaround is extension methods or a parallel lookup. C++ enums hold nothing; associated data
  requires a separate `constexpr` function or `std::map`.
* **Introspection and iteration** — Java offers `values()`, `valueOf(String)`, `name()`, `ordinal()` in the
  language. C# offers `Enum.GetValues()`, `Enum.GetNames()`, `Enum.Parse()` via reflection on
  `System.Enum`. C++ has no standard facility before the C++26 reflection proposal; libraries such as
  `magic_enum` fill the gap with template tricks.

</details>

### What happens when an enum constant is removed?
<details><summary>Show answer</summary>

* **Source-level references fail to compile** — every direct mention (`Planet.PLUTO`, `case PLUTO:`) becomes
  a compile error. This is the safety net: a clean rebuild of all dependent code will surface every place
  that touches the removed constant.
* **Already-compiled clients fail only when the dead path runs** — class files compiled against the old
  enum are not refreshed. A direct field reference throws `NoSuchFieldError`; `Enum.valueOf(Planet.class,
  "PLUTO")` and most deserialization paths throw `IllegalArgumentException` (or `InvalidObjectException`)
  when the name resolves to nothing. A `switch` compiled against the old set silently falls through to
  `default` or skips the case entirely, depending on how it was written. The failure surfaces lazily — at
  the moment that code path executes, not at startup.
* **Ordinals shift and corrupt silently** — if anyone persisted `ordinal()` to a database, file, or wire
  format, removing a constant slides every later constant down by one. No exception is thrown; old stored
  ordinals now resolve to the wrong constant. This is the canonical reason to persist by `name()` (or an
  explicit, hand-assigned code) rather than by ordinal.

```java
public enum Planet { MERCURY, VENUS, EARTH, MARS /*, PLUTO removed */ }

// Safe: by name, fails loud on missing member.
Planet p = Planet.valueOf("PLUTO");  // throws IllegalArgumentException

// Unsafe: by ordinal, silently mis-maps after a removal.
int stored = 4;                       // was PLUTO when written
Planet rehydrated = Planet.values()[stored];  // ArrayIndexOutOfBoundsException
                                              // or, worse, the wrong constant if a member was added later
```

**Practical rule** — treat constant removal as a breaking change to the public API surface. Coordinate a
recompile of all consumers, migrate any persisted data keyed by `name()`, and never rely on `ordinal()` for
anything that outlives a single JVM run.

</details>

### Where should an enum type be declared?
<details><summary>Show answer</summary>

By the reach of its usage. The declaration site should match the smallest scope from which the enum is
genuinely needed.

- **Top-level class** — when the enum is generally useful and consumed by code that has no natural reason
  to depend on a single host class. Lives in its own compilation unit, carries its own filename, is
  imported wherever needed.
- **Member class of an enclosing top-level class** — when the enum's meaning is bound to one specific host
  class and would be confusing or unused outside it. Nested as a `static` member; clients refer to it as
  `Host.Kind`, which signals the coupling at the call site.

```java
// Generally useful → top-level
public enum Planet { MERCURY, VENUS, EARTH /* ... */ }

// Tightly coupled to one host → nested member
public final class PayrollDay {
    public enum PayType { WEEKDAY, WEEKEND }   // meaningless outside PayrollDay
    private final PayType payType;
    // ...
}
```

**Rule of thumb**
- move the enum out to top-level as soon as a second unrelated class needs to use it. 
- keep it nested only when removing the host class would also remove every reason to use the enum.

</details>

### What does the Enum API provide?
<details><summary>Show answer</summary>

Inherited from `java.lang.Enum` (instance methods):

- `name()` — the exact identifier as declared in source.
- `ordinal()` — zero-based position in the declaration; use with care, not for persistence.
- `toString()` — defaults to `name()`; may be overridden for display.
- `compareTo(E)` — natural ordering by `ordinal()`.
- `equals(Object)` / `hashCode()` — final, identity-based; `==` is equivalent and preferred.
- `getDeclaringClass()` — the enum type itself, unaffected by constant-specific class bodies.

Generated automatically by the compiler for every enum type (static methods):

- `values()` — array of all constants, in declaration order; a fresh copy on each call.
- `valueOf(String)` — looks up a constant by `name()`; throws `IllegalArgumentException` on miss.

On `java.lang.Enum` (static):

- `Enum.valueOf(Class<E>, String)` — reflective lookup when the enum type is only known at runtime.

</details>


### How should a collection holding enum values be chosen?
<details><summary>Show answer</summary>

- `EnumSet<E>` — a `Set` implementation backed by a bit vector. Each constant maps to one bit, so membership
  tests, unions, and intersections are bitwise operations. Roughly the footprint of a single `long` for
  enums of 64 or fewer constants.
- `EnumMap<E, V>` — a `Map` implementation backed by a plain array indexed by `ordinal()`. No hashing, no
  bucket lookup; access is direct array indexing.

**When to choose them**
- Prefer `EnumSet` over `HashSet` and `EnumMap` over `HashMap` whenever the key or element type is an enum.
- Both refuse non-enum members at compile time, which keeps the closed-set guarantee end-to-end.
- Iteration order follows the enum's declaration order, not insertion order.
- Neither is thread-safe; wrap with `Collections.synchronizedSet`/`synchronizedMap` if needed.

</details>

### How to share code among enum constants?
<details><summary>Show answer</summary>

The problem appears when only *some* constants share behavior, while others differ. 
Constant-specific method overriding handles the differences but invites copy-paste; 
a `switch (this)` inside a shared method compiles but offers no compile-time guarantee that new constants are handled. 
The preferred answer is to move the varying behavior into a separate enum type.

**Strategy enum pattern**
- Define a nested enum whose constants represent each distinct behavior.
- The outer enum holds a final field of that strategy type, assigned through its constructor.
- Every outer constant must pick a strategy at declaration time — the compiler enforces it.
- Adding a new constant cannot be forgotten: it has no default and will not compile without a choice.

```java
public enum PayrollDay {
  MONDAY(PayType.WEEKDAY), 
  TUESDAY(PayType.WEEKDAY),
  WEDNESDAY(PayType.WEEKDAY), 
  THURSDAY(PayType.WEEKDAY),
  FRIDAY(PayType.WEEKDAY),
  SATURDAY(PayType.WEEKEND), 
  SUNDAY(PayType.WEEKEND);

  private final PayType payType;

  PayrollDay(PayType payType) {
    this.payType = payType;
  }

  int pay(int minutesWorked, int payRate) {
    return payType.pay(minutesWorked, payRate);
  }

  // Nested strategy enum — one constant per shared behavior.
  private enum PayType {
    WEEKDAY {
      int overtimePay(int mins, int rate) {
        return mins <= MINS_PER_SHIFT ? 0 : (mins - MINS_PER_SHIFT) * rate / 2;
      }
    },
    WEEKEND {
      int overtimePay(int mins, int rate) {
        return mins * rate / 2;
      }
    };

    abstract int overtimePay(int mins, int rate);

    private static final int MINS_PER_SHIFT = 8 * 60;

    int pay(int minsWorked, int payRate) {
      return minsWorked * payRate + overtimePay(minsWorked, payRate);
    }
  }
}
```

**Other options, by decreasing safety**
- **Shared concrete method in the enum** — fine when *all* constants behave identically; 
  just declare a regular method. No sharing problem to solve.
- **Private helper method on the enum** — extract the common logic into a private method; 
  constants that share behavior call it from their overridden form. 
  Simple, but each new constant must remember to call the helper.
- **`switch (this)` inside a method** — works but the compiler will not warn when a new constant is added,
  so a stale `default` branch silently runs. Acceptable only for behavior that lives outside the enum 
  (in a client class) and cannot be added to the enum itself.

</details>

### When are switch statements on enums actually useful?
<details><summary>Show answer</summary>

For **augmenting an enum type with behavior that does not belong inside it**. The switch lives in a client
class and adds a new operation on top of the existing enum, without touching the enum's source.

**Good fits for `switch`**
- **Enums you cannot modify** — third-party, generated, or library enums where adding a method is not an
  option. The switch sits in the client and supplies the missing behavior.
- **Behavior that does not belong on the enum** — operations whose meaning is specific to one caller or one
  subsystem (a UI label, a database column value, a protocol code, an inverse operation that would force
  forward references between constants). Putting it inside the enum would pollute the type with concerns
  that have nothing to do with what the constant *is*.

```java
// Enum we own, but the inverse operation belongs outside it —
// each constant would otherwise need to forward-reference the others.
public enum Operation {PLUS, MINUS, TIMES, DIVIDE}

public final class Operations {
  public static Operation inverse(Operation op) {
    return switch (op) {
      case PLUS -> Operation.MINUS;
      case MINUS -> Operation.PLUS;
      case TIMES -> Operation.DIVIDE;
      case DIVIDE -> Operation.TIMES;
    };
  }
}
```

**Safety note** — with arrow-form `switch` expressions (Java 14+), the compiler requires every constant to
be handled or a `default` branch to exist. Adding a new constant breaks compilation in every exhaustive
switch that ignored it — exactly the failure you want, at the right moment.

</details>

### TODO p.192 Item 35: Use instance fields instead of ordinals