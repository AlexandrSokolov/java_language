
### Type inference

Java has historically had a reputation as a verbose language. 
However, in recent versions, the language has evolved to make more and more use of _type inference_. 
This feature of the source code compiler enables the compiler to work out 
some of the type information in programs automatically. 
As a result, it doesn’t need to be told everything explicitly.

The aim of _type inference_ is to reduce boilerplate content, remove duplication, 
and allow for more concise and readable code.

#### Java 5 type inference
This trend started with Java 5, when generic methods were introduced. 
Generic methods permit a very limited form of type inference of generic type arguments, 
so that instead of having to explicitly provide the exact type that is needed, like this:
```java
List<Integer> empty = Collections.<Integer>emptyList();
```
The generic type parameter can be omitted on the right-hand side, like so:
```java
List<Integer> empty = Collections.emptyList();
```

#### Java 7 type inference - _diamond syntax_

```java
Map<Integer, Map<String, String>> usersLists = new HashMap<>();
```
Instead of:
```java
Map<Integer, Map<String, String>> usersLists = new HashMap<Integer, Map<String, String>>();
```

#### Java 8 type inference - 

The type inference algorithm can conclude that the type of `s` is a `String` with lambda expressions:
```java
Function<String, Integer> lengthFn = s -> s.length();
```

### Java 10 - enhanced type inference (var keyword)

Local Variable Type Inference (LVTI), otherwise known as `var`. 
This feature was added in Java 10 and allows the developer to infer the types of variables, 
instead of the types of values, like this:
```java
var names = new ArrayList<String>();
```

The intention of var is to reduce verbosity in Java code and 
to be familiar to programmers coming to Java from other languages.

### Using `var` means Java is dynamically typed language?

It does not introduce dynamic typing, and all Java variables continue to have static types at all times - 
you just don’t need to write them down explicitly in all cases.

### Scope of `var` usage

Type inference in Java is local, and in the case of var, 
the algorithm examines only the declaration of the local variable. 
This means it cannot be used for fields, method arguments, or return types.

### How to declare collection literals

Many other languages support some form of this, and Java itself has always had array literals, as shown here:
```jshell
jshell> int[] numbers = {1, 2, 3};
numbers ==> int[3] { 1, 2, 3 }
```
Java 8 added the ability to have static methods on interfaces. 
Java 11 added Collections factories (JEP 213) to the relevant interfaces:
```java
Set<String> set = Set.of("a", "b", "c");
var list = List.of("x", "y");
```

### Collections factories - lists and sets vs maps

Both `List` and `Set` have factories to create them from amount of elements:
```java
List<E> of(E e1, E e2, E e3); //only as an example
List<E> of(E... elements);
```

For maps, the situation is a little more complicated, because maps have two generic parameters 
(the key type and the value type) and so, although the simple cases can be written like this:
```java
var m1 = Map.of(k1, v1);
var m2 = Map.of(k1, v1, k2, v2);
```
there is no simple way of writing the equivalent of the `varargs` form for map. 
Instead, a different factory method, `ofEntries()`, is used in combination with a static helper method, `Map::entry`, 
to provide an equivalent of a `varargs` form, as shown next:
```java
Map.ofEntries(
  entry(k1, v1),
  entry(k2, v2),
  // ...
  entry(kn, vn));
```

### Adding element into `List.of(2, 3, 5)` list

The factory methods produce instances of _immutable_ types (_unmodifiable_ types - to be more precise).
Attempts to modify instances of these types will result in an exception being thrown.

### Text blocks

The _Text blocks_ is a standard feature in Java 17.

It aims to expand the notion of a string in Java syntax by allowing string literals that extend over multiple lines. 
In turn, that should avoid the need for most of the escape sequences that, historically, 
Java programmers have found to be an excessive hindrance.

A specific goal of Text Blocks is to allow readable strings of code that are not Java 
but that need to be embedded in a Java program (or SQL or JSON or even XML).

```java
String query = """
  SELECT "ORDER_ID", "QUANTITY", "CURRENCY_PAIR" FROM "ORDERS"
  WHERE "CLIENT_ID" = ?
  ORDER BY "DATE_TIME", "STATUS" LIMIT 100;
  """;
```
- the Text Block is started and terminated with the sequence """, which was not legal Java prior to version 15. 
- the Text Block can be indented with whitespace at the start of each line—and the whitespace will be ignored
If we print out the query variable, then we get exactly the string we constructed, as shown here:
```text
SELECT "ORDER_ID", "QUANTITY", "CURRENCY_PAIR" FROM "ORDERS"
WHERE "CLIENT_ID" = ?
ORDER BY "DATE_TIME", "STATUS" LIMIT 100;
```

### Limitation of Java Text blocks

Unlike various other programming languages, Java Text Blocks do not currently support _interpolation_, 
although this feature is under active consideration for inclusion in a future version.

String interpolation in JavaScript, uses backticks \` and `${expression}` for interpolation: 
```javascript
const name = "Alex";
const age = 30;
const message = `My name is ${name} and I am ${age} years old.`;
console.log(message);
```

String interpolation in Kotlin, use `$variable` or `${expression}` inside strings:
```kotlin
val name = "Alex"
val age = 30
val message = "My name is $name and I am $age years old."
println(message)
```
String interpolation in Scala. Prefix the string with `s` and use `$variable` or `${expression}`:
```scala
val name = "Alex"
val age = 30
val message = s"My name is $name and I am $age years old."
println(message)
```

### Original `switch` statement, related issues

[Switch demo project](java_statements/src/main/java/com/savdev/statements/SwitchDemo.java)

Classical switch:
```java
switch(month) {
  case 1:
    System.out.println("January");
    break;
  case 2:
    System.out.println("February");
    break;
  // ... and so on
}
```
Issues:
- Java’s switch statement inherited the property that if a `case` doesn’t end with `break`, 
    execution will continue after the next case. 
    This rule allows the grouping of cases that need identical handling, like this:
    ```java
    switch(month) {
      case 12:
      case 1:
      case 2:
        System.out.println("Winter, brrrr");
        break;
      case 3:
      case 4:
      case 5:
        System.out.println("Spring has sprung!");
        break;
      // ... and so on
    }
    ```
    Convenience for this situation, though, brought with it a dark and buggy side. 
    Omitting a single `break` is an easy mistake for programmers, both new and old, and often introduced errors.
- clunky when trying to capture a value for later use.
  if we wanted to grab that message for use elsewhere, instead of printing it,
  we’d have to set up a variable outside the switch, set it correctly in each branch, 
  and potentially ensure after the switch that we actually set the value:
    ```java
    String message = null;
    switch(month) {
      case 12:
      case 1:
      case 2:
        message = "Winter, brrrr";
        break;
      case 3:
      case 4:
      case 5:
        message = "Spring has sprung!";
        break;
      // ... and so on
    }
    ```
    We now must ensure every case properly sets the message variable or risk a bug report in our future.

### Switch expressions

Switch Expressions, introduced in Java 14 (JEP 361), provide alternative to original switch statement.

_Switch Expressions_ vs the existing _Switch Statement_ - takes on more meaning. 
In programming languages, a _statement_ is a piece of code executed for its side effect. 
An _expression_ refers instead to code executed to produce a value. 
`switch` prior to Java 14 was only a side-effecting statement, 
but now it can produce values when used as an expression.

```java
String message = switch (month) {
  case 1, 2, 12 -> "Winter, brrrr";
  case 3, 4, 5 -> "Spring has sprung!";
  case 6, 7, 8 -> "Summer is here!";
  case 9, 10, 11 -> "Fall has descended";
  default -> {
    throw new IllegalArgumentException("Oops, that's not a month");
  }
}
```
- The `->` indicates we’re in a `switch` expression, so those cases don’t need an explicit `yield`. 
- Our default case shows how a block enclosed in {} can be used where we don’t have a single value. 
- If you’re using the value of a switch expression (as we are by assigning it to `message`), 
  multiline cases must either yield or throw.

Improvements:
1. multiple cases are directly supported by the comma-delimited list after the case
2. you cannot forget to break those case
3. you cannot forget to assign to a variable inside the cases, the expression returns a value now

If we remove the `default` line from the previous example, we get a compile error:
```text
error: the switch expression does not cover all possible input values
String message = switch(month) {
```
Unlike `switch` statements, Switch Expressions must handle every possible case for your input type, 
or your code won’t even compile.
For instance, an int, we must include a `default` clause as it is not feasible to list 
all approximately four billion possible values.

### Switch expression with Java enums

If all the possible enum constants are present in a switch expression,
the match is total, and it is not necessary to include a default case - 
the compiler can use the exhaustiveness of the enum constants.

### Switch expression for multiple types

Right name is Pattern matching?
switch on enums and strings with guards (refined).
Pattern matching enhances traditional `switch`:
```java

enum Status { OK, WARN, ERROR }

static String label(Object input) {
    return switch (input) {
        case Status s when s == Status.OK -> "All good";
        case Status s -> "Status: " + s;
        case String str when str.isBlank() -> "Empty string";
        case String str -> "String(" + str.length() + ")";
        case Integer n when n >= 0 -> "Non-negative int";
        case Integer n -> "Negative int";
        default -> "Unknown";
    };
}

```

### Data Holder Classes, what problem existed?

- For a simple data holder (like Person with name and age), you had to write:
  - Fields
  - Constructor
  - getters
  - equals(), 
  - hashCode()
  - toString()
  Even with IDE generation, this was repetitive and error-prone.
- Semantic Ambiguity
  A plain class doesn’t clearly express its intent:
  Is it mutable? Is it just a DTO? Does it have behavior?
- Immutability Requires Extra Work
  Making fields `final` and ensuring no setters adds more boilerplate.
- Equality and Hashing Bugs
  Manual implementation of `equals()` and `hashCode()` often leads to mistakes.

### Java Records

Java Records solve these problems with data holder classes:
- Concise Declaration. A record automatically generates:
  - private final fields
  - Canonical constructor
  - equals(), hashCode(), toString()
  - Accessor methods (not called getters, just fieldName())
- Clear Intent - Records are transparent carriers for immutable data. 
  The keyword record signals: This is a data class, not a behavior-heavy object.
- Immutability by Design
- Pattern Matching Integration

```java
public record Person(String name, int age) {}
```

Java Records are ideal for Functional/DTO Use Cases:
- DTOs
- Value objects
- Data returned from APIs
- Modeling algebraic data types (with sealed classes)

It should also reduce or remove the need for libraries like `Lombok`.

### Using Java Record with Builder Pattern

TODO - copy the example into the demo apps

- Use the canonical constructor (or a static factory) (when you only need a couple of optional values):
  - The record has few components (say ≤ 4–5) and all are required.
  - You don’t need partial construction, stepwise setting, or readability benefits.
  - You’re already doing validation in the record’s compact constructor.
  - You don’t need defaults or many optional parameters.
- Consider a builder if one or more apply:
  - Many components (high arity) where the canonical constructor is unreadable.
  - Lots of optional fields and sensible defaults.
  - Fluent construction improves clarity at call sites.
  - Cross-field validation that benefits from building up state before validating.
  - You want a “toBuilder()” style for slight modifications of an existing instance (records are immutable). 

```java
public record User(
    String username,
    String email,
    boolean newsletter,
    int quotaMb,
    String role
) {
    // Compact constructor for validation
    public User {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username required");
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("invalid email");
        if (quotaMb < 0) throw new IllegalArgumentException("quota must be >= 0");
    }

    // Optional: a fluent builder for readability/defaults
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String username;
        private String email;
        private boolean newsletter = false; // default
        private int quotaMb = 1024;         // default
        private String role = "USER";       // default

        public Builder username(String v) { this.username = v; return this; }
        public Builder email(String v) { this.email = v; return this; }
        public Builder newsletter(boolean v) { this.newsletter = v; return this; }
        public Builder quotaMb(int v) { this.quotaMb = v; return this; }
        public Builder role(String v) { this.role = v; return this; }

        public User build() {
            return new User(username, email, newsletter, quotaMb, role); // validation happens in compact constructor
        }
    }

    // Quality-of-life: a toBuilder() copy-with pattern
    public Builder toBuilder() {
        return new Builder()
            .username(username)
            .email(email)
            .newsletter(newsletter)
            .quotaMb(quotaMb)
            .role(role);
    }
}
```
Usage:
```java
var admin = User.builder()
    .username("alex")
    .email("alex@example.com")
    .role("ADMIN")
    .newsletter(true)
    .build();

var downgraded = admin.toBuilder()
    .role("USER")
    .quotaMb(512)
    .build();
```

### You want to copy a java record with a changed single field

Use "_Withers_" (copy methods).
Add small, focused methods that return a new record with one field changed:
```java
public record Account(String id, String owner, long balance) {
  public Account withBalance(long newBalance) { return new Account(id, owner, newBalance); }
}
```

### Sealed classes

```java
public abstract sealed class Pet {
  private final String name;

  protected Pet(String name) {
    this.name = name;
  }

  public String name() {
    return name;
  }

  public static final class Cat extends Pet {
    public Cat(String name) {
      super(name);
    }

    void meow() {
      System.out.println(name() + " meows");
    }
  }

  public static final class Dog extends Pet {
    public Dog(String name) {
      super(name);
    }

    void bark() {
      System.out.println(name() + " barks");
    }
  }
}
```
The class Pet is declared as `sealed`.
`sealed` means that the class can be extended only inside the current compilation unit.
Therefore, the subclasses have to be nested within the current class. 
We also declare Pet to be `abstract` because we don’t want any general `Pet` instances, 
only `Pet.Cat` and `Pet.Dog` objects.

### Sealed interfaces

```java
public sealed interface FXOrder permits MarketOrder, LimitOrder {
  int units();
  CurrencyPair pair();
  Side side();
  LocalDateTime sentAt();
}
public record MarketOrder(
  int units,
  CurrencyPair pair,
  Side side,
  LocalDateTime sentAt,
  boolean allOrNothing) implements FXOrder {
  // constructors and factories elided
}
public record LimitOrder(
  int units,
  CurrencyPair pair,
  Side side,
  LocalDateTime sentAt,
  double price,
  int ttl) implements FXOrder {
  // constructors and factories elided
}
```
- First, `FXOrder` is now a `sealed` interface. 
- Second, we can see the use of a second new keyword, `permits`, 
  which allows the developer to list the permissible implementations of this sealed interface - 
  and our implementations are Records.

### Using the implementing classes for sealed interfaces vs sealed classes

- The `sealed` class can be extended only inside the current compilation unit.
- When you use permits with `sealed` interfaces, 
  the implementing classes **do not have to live within the same file and can be separate compilation units**.

### Using instanceof

For example, consider an object that has been obtained reflectively about which little or nothing is known.
In these circumstances, the appropriate thing to do is to use `instanceof` to check that
the type is as expected and then perform a downcast.
The `instanceof` test provides a guard condition that ensures that the cast
will not cause a `ClassCastException` at runtime. The resulting code looks like this example:
```java
Object o = // ...
if (o instanceof String) {
  String s = (String)o;
  System.out.println(s.length());
} else {
  System.out.println("Not a String");
}
```
To check and avoid the cast:
```java
if (o instanceof String s) {
  //s is in scope on this branch
  System.out.println(s.length());
} else {
  System.out.println("Not a String");
}
```

### Pattern Matching for `instanceof`

```java
var msg = switch (o) {
  case String s -> "String of length:"+ s.length();
  case Integer i -> "Integer:"+ i;
  case null, default -> "Not a String or Integer";
};

System.out.println(msg);
```

### Switch expression with Sealed Types

```java

sealed interface Expr permits Lit, Add, Neg {}
record Lit(int value) implements Expr {}
record Add(Expr left, Expr right) implements Expr {}
record Neg(Expr inner) implements Expr {}

static int eval(Expr e) {
    return switch (e) {
        case Lit(int v) -> v;
        case Add(Expr l, Expr r) -> eval(l) + eval(r);
        case Neg(Expr inner) -> -eval(inner);
    }; // no default needed: sealed hierarchy is exhaustive
}
```
example with shapes:
```java
public sealed interface Shape permits Circle, Rectangle, Square {}

static double area(Shape s) {
  return switch (s) {
    case Circle c -> Math.PI * c.radius() * c.radius();
    case Rectangle r -> r.width() * r.height();
    case Square sq -> sq.side() * sq.side();
  };
}
```