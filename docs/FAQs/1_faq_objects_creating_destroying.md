Creating and Destroying Objects

### Java Object creations, ways
<details><summary>Show answer</summary>

- [via constructor](#class-constructor)
- [via static factory method](#static-factory-methods)
- [via the Builder pattern](#the-builder-pattern)

</details>

### Class Constructor
<details><summary>Show answer</summary>

The traditional way for a class to allow a client to obtain an instance is to provide a public constructor.

</details>

### Static factory methods
<details><summary>Show answer</summary>

A class can provide a public static factory method, 
which is simply a static method that returns an instance of the class:
```java
public static Boolean valueOf(boolean b) {
  return b ? Boolean.TRUE : Boolean.FALSE;
}
```

</details>

### Providing a static factory method, advantages and disadvantages
<details><summary>Show answer</summary>

Advantages:
- [Unlike constructors, static factory methods have names](#named-static-factory-methods-compare-with-a-constructor)
- [Unlike constructors, they can help to avoid creating unnecessary duplicate objects](#how-to-avoid-creating-unnecessary-duplicate-objects)
- [flexibility in choosing the class of the returned object](#choosing-the-class-of-the-returned-object-when-it-gets-created)
- [the class of the returned object can vary from call to call as a function of the input parameters](#how-to-vary-the-class-of-the-created-object-from-call-to-call-as-a-function-of-the-input-parameters-motivation)
- [the class of the returned object need not exist when the class containing the method is written](#service-provider-frameworks)

Disadvantages:
- classes without public or protected constructors cannot be subclassed.
  Arguably this can be a blessing in disguise because it encourages programmers to use 
  composition instead of inheritance, and is required for immutable types
- they are hard for programmers to find

</details>

### Named static factory methods, compare with a constructor
<details><summary>Show answer</summary>

Unlike constructors, static factory methods have names. 
- Carefully chosen names to highlight their differences and more clear as a result.
- Also, a class can have only a single constructor with a given signature.
  Programmers have been known to get around this restriction by providing two constructors whose parameter lists
  differ only in the order of their parameter types.
  Carefully chosen names to highlight their differences solves this issue.

</details>

### How to avoid creating unnecessary duplicate objects?
<details><summary>Show answer</summary>

Unlike constructors, static factory methods are not required to create a new object each time they’re invoked.
This allows:
- to use preconstructed instances, or 
- to cache instances as they’re constructed
to avoid creating unnecessary duplicate objects.

</details>

### Why avoid creating unnecessary duplicate objects could be useful?
<details><summary>Show answer</summary>

The ability of static factory methods to return the same object from repeated invocations 
allows classes to maintain strict control over what instances exist at any time. 
Classes that do this are said to be _instance-controlled_.
There are several reasons to write _instance-controlled_ classes. Instance control allows a class to:
- guarantee that it is a singleton or
- guarantee that it is noninstantiable
- it allows an immutable value class to make the guarantee that no two equal instances exist:
  `a.equals(b)` if and only if `a == b`. 

This is the basis of the Flyweight pattern. Enum types provide this guarantee.

</details>

### Choosing the class of the returned object when it gets created
<details><summary>Show answer</summary>

Unlike constructors, static factory methods can return an object of any subtype of their return type. 
This gives you great flexibility in choosing the class of the returned object.

One application of this flexibility is that an API can return objects without making their classes public. 
Hiding implementation classes in this fashion leads to a very compact API. 
This technique lends itself to _interface-based frameworks_, 
where interfaces provide natural return types for static factory methods.

</details>

### Convenience used to define static factory methods on interfaces
<details><summary>Show answer</summary>

- Prior to Java 8, interfaces couldn’t have static methods. 
  By convention, static factory methods for an interface named `Type` were put in a _noninstantiable companion class_ 
  named `Types`.
- As of Java 8, the restriction that interfaces cannot contain static methods was eliminated, 
  so there is typically little reason to provide a _noninstantiable companion class_ for an interface. 
  Many public static members that would have been at home in such a class should instead be put in the interface itself.

</details>

### How to vary the class of the created object from call to call as a function of the input parameters? Motivation
<details><summary>Show answer</summary>

Unlike constructors, static factory methods can make it possible that the class of the returned object 
can vary from call to call as a function of the input parameters. 
Any subtype of the declared return type is permissible. 
The class of the returned object can also vary from release to release.

The `EnumSet` class has no public constructors, only static factories.
In the OpenJDK implementation, they return an instance of one of two subclasses, 
depending on the size of the underlying enum type: 
- if it has sixty-four or fewer elements, as most enum types do, the static factories return a `RegularEnumSet` instance, 
  which is backed by a single long; 
- if the enum type has sixty-five or more elements, the factories return a `JumboEnumSet` instance, 
  backed by a long array.

The existence of these two implementation classes is invisible to clients. 
If `RegularEnumSet` ceased to offer performance advantages for small enum types, 
it could be eliminated from a future release with no ill effects. 
Similarly, a future release could add a third or fourth implementation of `EnumSet` 
if it proved beneficial for performance. 
Clients neither know nor care about the class of the object they get back from the factory; 
they care only that it is some subclass of EnumSet.

</details>

### Service provider frameworks
<details><summary>Show answer</summary>

Unlike constructors, with static factory methods the class of the returned object 
need not exist when the class containing the method is written.

Such flexible static factory methods form the basis of service provider frameworks, 
like the Java Database Connectivity API (JDBC). 
A service provider framework is a system in which providers implement a service, 
and the system makes the implementations available to clients, 
decoupling the clients from the implementations.

There are three essential components in a service provider framework: 
- _a service interface_, which represents an implementation; 
- _a provider registration API_, which providers use to register implementations; and 
- _a service access API_, which clients use to obtain instances of the service. 
  The service access API may allow clients to specify criteria for choosing an implementation. 
  In the absence of such criteria, the API returns an instance of a default implementation, 
  or allows the client to cycle through all available implementations.
  **The service access API is the flexible static factory that forms the basis of the service provider framework**.
- [optional] a service provider interface, 
  which describes a factory object that produce instances of the service interface. 
  In the absence of a service provider interface, implementations must be instantiated reflectively. 


In the case of JDBC:
- `Connection` plays the part of the service interface, 
- `DriverManager.registerDriver` is the provider registration API, 
- `DriverManager.getConnection` is the service access API, and 
- `Driver` is the service provider interface.

</details>

### Static factory method vs the Factory Method pattern
<details><summary>Show answer</summary>

A static factory method is not the same as the Factory Method pattern from Design Patterns.

Static Factory Method
- it’s just a technique using a static helper for creating instances, not part of a design pattern
- a more flexible version of constructor
  - can return the cached instance, constructor always creates new instance
  - provides meaningful names for different set of parameters, constructor always has the same name
  - can return different types

Factory Method Pattern (GoF design pattern)
- it is an object-oriented creational design pattern
- its goal is to let subclasses choose which concrete class to instantiate

</details>

### Common names for static factory methods
<details><summary>Show answer</summary>

- `from` - A type-conversion method that takes a single parameter and returns a corresponding instance of this type, 
  for example: `Date d = Date.from(instant);`
- `of` - An aggregation method that takes multiple parameters and returns 
  an instance of this type that incorporates them, for example: `Set<Rank> faceCards = EnumSet.of(JACK, QUEEN, KING);`
- `valueOf` - A more verbose alternative to `from` and `of`, for example: 
  `BigInteger prime = BigInteger.valueOf(Integer.MAX_VALUE);`
- `instance` or `getInstance` - Returns an instance that is described by its parameters (if any) 
  but cannot be said to have the same value, for example: `StackWalker luke = StackWalker.getInstance(options);`
- `create` or `newInstance` - Like `instance` or `getInstance`, except that the method guarantees that 
  each call returns a new instance, for example: `Object newArray = Array.newInstance(classObject, arrayLen);`
- get*Type* - Like `getInstance`, but used if the factory method is in a different class. 
  `Type` is the type of object returned by the factory method, for example: `FileStore fs = Files.getFileStore(path);`
- new*Type* - Like `newInstance`, but used if the factory method is in a different class. 
  `Type` is the type of object returned by the factory method, for example: 
  `BufferedReader br = Files.newBufferedReader(path);`
- _type_ - A concise alternative to `getType` and `newType`, for example: 
  `List<Complaint> litany = Collections.list(legacyLitany);`

</details>

### Objects creation with large numbers of optional parameters, options
<details><summary>Show answer</summary>

Static factories and constructors share a limitation: 
they do not scale well to large numbers of optional parameters.

Bad options:
- [_Telescoping constructor_ pattern](#telescoping-constructor-pattern)
- [The JavaBeans pattern](#the-javabeans-pattern)
Best and the only recommended option:
- [the Builder pattern](#the-builder-pattern)

</details>

### Telescoping constructor pattern
<details><summary>Show answer</summary>

The telescoping constructor pattern, 
in which you provide a constructor with only the required parameters, 
another with a single optional parameter, a third with two optional parameters, and so on, 
culminating in a constructor with all the optional parameters.

```java
// Telescoping constructor pattern - does not scale well!
public class NutritionFacts {
  private final int servingSize; // (mL) required
  private final int servings; // (per container) required
  private final int calories; // (per serving) optional
  private final int fat; // (g/serving) optional
  private final int sodium; // (mg/serving) optional
  private final int carbohydrate; // (g/serving) optional
  public NutritionFacts(int servingSize, int servings) {
    this(servingSize, servings, 0);
  }
  public NutritionFacts(int servingSize, int servings,
                        int calories) {
    this(servingSize, servings, calories, 0);
  }
  public NutritionFacts(int servingSize, int servings,
                        int calories, int fat) {
    this(servingSize, servings, calories, fat, 0);
  }
  public NutritionFacts(int servingSize, int servings,
                        int calories, int fat, int sodium) {
    this(servingSize, servings, calories, fat, sodium, 0);
  }
  public NutritionFacts(int servingSize, int servings,
                        int calories, int fat, int sodium, int carbohydrate) {
    this.servingSize = servingSize;
    this.servings = servings;
    this.calories = calories;
    this.fat = fat;
    this.sodium = sodium;
    this.carbohydrate = carbohydrate;
  }
}
```
The telescoping constructor pattern works, 
but it is hard to write client code when there are many parameters, and harder still to read it.

</details>

### The JavaBeans pattern
<details><summary>Show answer</summary>

The JavaBeans pattern, in which you call a parameterless constructor to create the object and then 
call setter methods to set each required parameter and each optional parameter of interest:
```java
// JavaBeans Pattern - allows inconsistency, mandates mutability
public class NutritionFacts {
  // Parameters initialized to default values (if any)
  private int servingSize = -1; // Required; no default value
  private int servings = -1; // Required; no default value
  private int calories = 0;
  private int fat = 0;
  private int sodium = 0;
  private int carbohydrate = 0;

  public NutritionFacts() {
  }

  // Setters
  public void setServingSize(int val) {
    servingSize = val;
  }

  public void setServings(int val) {
    servings = val;
  }

  public void setCalories(int val) {
    calories = val;
  }

  public void setFat(int val) {
    fat = val;
  }

  public void setSodium(int val) {
    sodium = val;
  }

  public void setCarbohydrate(int val) {
    carbohydrate = val;
  }
}
```

The JavaBeans pattern has serious disadvantages of its own:
- Because construction is split across multiple calls, a JavaBean may be in an inconsistent state 
  partway through its construction. The class does not have the option of enforcing consistency merely by 
  checking the validity of the constructor parameters. 
  Attempting to use an object when it’s in an inconsistent state may cause failures that are far removed 
  from the code containing the bug and hence difficult to debug.
- A related disadvantage is that the JavaBeans pattern precludes the possibility of making a class immutable 
  and requires added effort on the part of the programmer to ensure thread safety.

</details>

### The Builder pattern
<details><summary>Show answer</summary>

Instead of making the desired object directly, the client calls:
1. a constructor (or static factory) with all of the required parameters and gets a builder object
2. then the client calls setter-like methods on the builder object to set each optional parameter of interest
3. finally, the client calls a parameterless build method to generate the object, which is typically immutable

```java
// Builder Pattern
public class NutritionFacts {
  private final int servingSize;
  private final int servings;
  private final int calories;
  private final int fat;
  private final int sodium;
  private final int carbohydrate;

  public static class Builder {
    // Required parameters
    private final int servingSize;
    private final int servings;
    // Optional parameters - initialized to default values
    private int calories = 0;
    private int fat = 0;
    private int sodium = 0;
    private int carbohydrate = 0;

    public Builder(int servingSize, int servings) {
      this.servingSize = servingSize;
      this.servings = servings;
    }

    public Builder calories(int val) {
      calories = val;
      return this;
    }

    public Builder fat(int val) {
      fat = val;
      return this;
    }

    public Builder sodium(int val) {
      sodium = val;
      return this;
    }

    public Builder carbohydrate(int val) {
      carbohydrate = val;
      return this;
    }

    public NutritionFacts build() {
      return new NutritionFacts(this);
    }
  }

  private NutritionFacts(Builder builder) {
    servingSize = builder.servingSize;
    servings = builder.servings;
    calories = builder.calories;
    fat = builder.fat;
    sodium = builder.sodium;
    carbohydrate = builder.carbohydrate;
  }
}
```
Usage:
```java
NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8)
  .calories(100)
  .sodium(35)
  .carbohydrate(27)
  .build();
```

</details>

### Scala and Python alternatives to the Build pattern in Java
<details><summary>Show answer</summary>

The Builder pattern simulates named optional parameters as found in Python and Scala.

</details>

### Builder pattern for class hierarchies
<details><summary>Show answer</summary>

Recursive generics (self types) for inheritance.
It uses a recursive type bound like B extends Builder<B>.


```java
// Base class (immutable)
public abstract class Animal {
  private final String name;
  private final int age;

  protected Animal(Builder<?> builder) {
    this.name = builder.name;
    this.age = builder.age;
  }

  public String getName() {
    return name;
  }

  public int getAge() {
    return age;
  }

  // Base builder with self-type generic
  public static abstract class Builder<B extends Builder<B>> {
    private String name;
    private int age;

    public B name(String name) {
      this.name = name;
      return self();
    }

    public B age(int age) {
      this.age = age;
      return self();
    }

    // Subclasses implement this to return "this" as the correct B type
    protected abstract B self();

    // Subclasses provide the actual build() that constructs the subtype
    public abstract Animal build();
  }
}

// Subclass
public class Dog extends Animal {
  private final String breed;
  private final boolean trained;

  private Dog(Builder builder) {
    super(builder);
    this.breed = builder.breed;
    this.trained = builder.trained;
  }

  public String getBreed() {
    return breed;
  }

  public boolean isTrained() {
    return trained;
  }

  public static class Builder extends Animal.Builder<Builder> {
    private String breed;
    private boolean trained;

    public Builder breed(String breed) {
      this.breed = breed;
      return self();
    }

    public Builder trained(boolean trained) {
      this.trained = trained;
      return self();
    }

    @Override
    protected Builder self() {
      return this;
    }

    @Override
    public Dog build() {
      // Optional: validation
      if (breed == null || breed.isBlank()) {
        throw new IllegalStateException("breed must be set");
      }
      return new Dog(this);
    }
  }
}

// Another subclass
public class Cat extends Animal {
  private final boolean indoorOnly;

  private Cat(Builder builder) {
    super(builder);
    this.indoorOnly = builder.indoorOnly;
  }

  public boolean isIndoorOnly() {
    return indoorOnly;
  }

  public static class Builder extends Animal.Builder<Builder> {
    private boolean indoorOnly;

    public Builder indoorOnly(boolean indoorOnly) {
      this.indoorOnly = indoorOnly;
      return self();
    }

    @Override
    protected Builder self() {
      return this;
    }

    @Override
    public Cat build() {
      return new Cat(this);
    }
  }
}
```
`Animal.Builder` is a generic type with a recursive type parameter. 
This, along with the abstract `self` method, allows method chaining to work properly in subclasses, 
without the need for casts. 
This workaround for the fact that Java lacks a self type is known as the simulated self-type idiom.

Note that the `build` method in each subclass’s builder is declared to return the correct subclass: 
- the `build` method of `Dog.Builder` returns `Dog`
- while the one in `Cat.Builder` returns `Cat`. 


This technique, wherein a subclass method is declared to return 
a subtype of the return type declared in the super-class, is known as covariant return typing. 
It allows clients to use these builders without the need for casting.

Cons
- Boilerplate (`self()` in each subclass).
- Harder when you need polymorphic build decisions

</details>

### Lombok Builder
<details><summary>Show answer</summary>

```java
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class Animal {
  private final String name;
  private final int age;
}

@Getter
@SuperBuilder
public class Dog extends Animal {
  private final String breed;
  private final boolean trained;
}

@Getter
@SuperBuilder
public class Cat extends Animal {
  private final boolean indoorOnly;
}
```
Usage:
```java
Dog d = Dog.builder()
  .name("Rex")
  .age(5)
  .breed("Shepherd")
  .trained(true)
  .build();
```

</details>

### A singleton, idea
<details><summary>Show answer</summary>

A singleton is simply a class that is instantiated exactly once.
Singletons typically represent:
- either a stateless object such as a function
- a system component that is intrinsically unique

</details>

### Issues with singletons
<details><summary>Show answer</summary>

Making a class a singleton can make it difficult to test its clients 
because it’s impossible to substitute a mock implementation for a singleton 
unless it implements an interface that serves as its type.

</details>

### Singleton implementations
<details><summary>Show answer</summary>

- [Eager Initialization singleton](#eager-initialization-singleton)
- [Lazy Initialization singleton](#lazy-initialization-singleton-recommended-approach)
- [Enum singleton](#enum-singleton)
- [Spring (or DI) Singleton](#alternative-to-java-singleton-in-spring-context)

</details>

### Eager Initialization singleton
<details><summary>Show answer</summary>

Simplest, thread-safe, the member is a final field:

```java
public final class EagerSingleton {
  private static final EagerSingleton INSTANCE = new EagerSingleton();

  private EagerSingleton() { /* init */ }

  public static EagerSingleton getInstance() {
    return INSTANCE;
  }
```
The private constructor is called only once, to initialize the public static final field `EagerSingleton.INSTANCE`. 
The lack of a public or protected constructor guarantees a single universe: 
exactly one `EagerSingleton` instance will exist once the `EagerSingleton` class is initialized - 
no more, no less. 
Nothing that a client does can change this, with one caveat: 
a privileged client can invoke the private constructor reflectively 
with the aid of the `AccessibleObject.setAccessible` method. 
If you need to defend against this attack, modify the constructor to make it throw an exception 
if it’s asked to create a second instance.

</details>

### Lazy Initialization singleton, recommended approach
<details><summary>Show answer</summary>

Using Initialization-on-Demand Holder (IoDH)

```java
public final class HolderSingleton {
  private HolderSingleton() { /* init */ }

  private static class Holder {
    static final HolderSingleton INSTANCE = new HolderSingleton();
  }

  public static HolderSingleton getInstance() {
    return Holder.INSTANCE;
  }
}
```
Lazy, thread-safe, no synchronization overhead.
**This is recommended general-purpose lazy singleton.**

</details>

### Lazy Initialization singleton with synchronized accessor:
<details><summary>Show answer</summary>

```java
public final class LazySingleton {
  private static LazySingleton instance;

  private LazySingleton() { /* init */ }

  public static synchronized LazySingleton getInstance() {
    if (instance == null) {
      instance = new LazySingleton();
    }
    return instance;
  }
}
```
`synchronized` on every call can be a bottleneck in hot paths

</details>

### Lazy Initialization singleton using Double-Checked Locking (DCL) with volatile:
<details><summary>Show answer</summary>

Solution without `synchronized` accessor using:
```java
public final class DclSingleton {
  private static volatile DclSingleton instance;

  private DclSingleton() { /* init */ }

  public static DclSingleton getInstance() {
    DclSingleton local = instance; // local var improves performance
    if (local == null) {
      synchronized (DclSingleton.class) {
        local = instance;
        if (local == null) {
          local = new DclSingleton();
          instance = local;
        }
      }
    }
    return local;
  }
}
```
Lazy, with minimal synchronization after init.
Cons:
- More complex to read and get right.
- Still some synchronization on first initialization.

</details>

### Enum singleton
<details><summary>Show answer</summary>

This option The most robust against serialization & reflection single-instance pattern in Java.
```java
public enum EnumSingleton {
    INSTANCE;

    // methods
    public void doWork() { /* ... */ }
}
```
Pros:
- Single instance guaranteed by JVM.
- Automatically safe against serialization and reflection attacks.
- Concise.

Cons:
- Cannot extend classes (enums can implement interfaces).
- Harder to parameterize construction (though you can add fields and constructors for constants).

</details>

### Alternative to Java singleton in Spring context
<details><summary>Show answer</summary>

- Java singleton - a design pattern ensuring only one instance of a class exists in the JVM.
- Spring Bean (Default Scope) - Not a language-level singleton, but container-managed: 
  one instance per application context.

When to use which?
- Use Spring beans in Spring-based apps—better integration, DI, and lifecycle management.
- Use Java Singleton only in non-Spring environments or for very low-level utilities.

</details>

### Creation of a class that only groups static methods and static fields, idea, design tips, cons
<details><summary>Show answer</summary>

Such classes usually called - _utility_ classes. They are not designed to be instantiated: 
an instance would be nonsensical.

Such classes have acquired a bad reputation because some people abuse them to avoid thinking in terms of objects, 
but they do have valid uses. 
- They can be used to group related methods on primitive values or arrays, 
  in the manner of `java.lang.Math` or `java.util.Arrays`. 
- They can also be used to group static methods, including factories, for objects that implement some interface, 
  in the manner of `java.util.Collections`. 
- Lastly, such classes can be used to group methods on a final class, since you can’t put them in a subclass.


Cons:
- Static utilities are inherently harder to mock.
- 

Design tips:
- Keep utility methods small and focused; avoid turning them into _"god classes"_.
- Organize by domain (e.g., `MathUtils`, `StringUtils`, `IOUtils`) rather than dumping everything in one place.

</details>

### Utility class implementations
<details><summary>Show answer</summary>

- [Final utility class with a private constructor](#final-utility-class-with-a-private-constructor)
- [Utility interface with static methods](#utility-interfaces-with-static-methods)

</details>

### Final utility class with a private constructor
<details><summary>Show answer</summary>

This is classic, recommended option.

```java
public final class MathUtils {
  private MathUtils() {
    // Prevent instantiation via reflection (optional)
    throw new AssertionError("No MathUtils instances for you!");
  }
  public static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
  public static boolean isEven(int n) {
    return (n & 1) == 0;
  }
```

</details>

### Utility interfaces with static methods
<details><summary>Show answer</summary>

```java
public interface Hashing {
  static int murmur332(byte[] data) {
    // implementation here
    return 0;
  }
}
```
Pros
- No instantiation; clear namespacing.
- Useful when grouping helper functions conceptually tied to an API.

Cons
- Static methods on interfaces are not polymorphic and not inherited by implementors.
- Less common for general-purpose utilities than classes.

Use when
- You want namespacing for helper methods related to an interface contract.

</details>

### Utility class vs utility interface
<details><summary>Show answer</summary>

| Aspect                          | Final Utility Class + Private Constructor                        | Interface with Static Methods                                          |
|---------------------------------|------------------------------------------------------------------|------------------------------------------------------------------------|
| **Instantiation**               | Impossible (private constructor)                                 | Impossible (interfaces cannot be instantiated)                         |
| **Encapsulation**               | ✅ Can have private static fields, caches, helpers                | ❌ All fields are `public static final`; no private state               |
| **Static Initialization Block** | ✅ Supported                                                      | ❌ Not supported                                                        |
| **Semantic Intent**             | Clear: a namespace of utilities                                  | Ambiguous: interfaces imply a contract                                 |
| **Inheritance**                 | ❌ Cannot be subclassed (final)                                   | N/A (interfaces cannot be extended for implementation)                 |
| **Future Evolution**            | ✅ Can add nested classes, factories, even instance methods later | ❌ Adding non-static methods changes semantics (implies implementation) |
| **Tooling & Conventions**       | Widely recognized pattern                                        | Less common for general-purpose utilities                              |
| **Private Helpers**             | ✅ Possible                                                       | ❌ Not possible                                                         |
| **Static Import Ergonomics**    | ✅ Same (`import static ...`)                                     | ✅ Same                                                                 |
| **Use Case Fit**                | General-purpose utility libraries                                | Companion methods tied to an interface contract                        |

</details>

### Unnecessary objects issue, solution
<details><summary>Show answer</summary>

It is often appropriate to reuse a single object instead of creating 
a new functionally equivalent object each time it is needed. 
Reuse can be both faster and more stylish. An object can always be reused if it is immutable.

A good example how not to do:
[String instances creation](#string-instances-creation)
[Autoboxing](#autoboxing)

You can often avoid creating unnecessary objects by [static factory methods](#static-factory-methods)
The constructor must create a new object each time it’s called, 
while the factory method is never required to do so and won’t in practice.

</details>

### Autoboxing
<details><summary>Show answer</summary>

```java
// Hideously slow! Can you spot the object creation?
private static long sum() {
  Long sum = 0L;
  for (long i = 0; i <= Integer.MAX_VALUE; i++)
    sum += i;
  return sum;
}
```
Autoboxing - creates unnecessary objects, which allows the programmer to mix primitive and boxed primitive types,
boxing and unboxing automatically as needed.
Autoboxing blurs but does not erase the distinction between primitive and boxed primitive types.
As a result, prefer primitives to boxed primitives, and watch out for unintentional autoboxing.

</details>

### Memory leak, what is it?
<details><summary>Show answer</summary>

A memory leak is a situation where a program unintentionally holds onto memory that it no longer needs, 
preventing that memory from being reclaimed by the garbage collector.

Having memory leaks can silently manifest itself as reduced performance due to:
- increased garbage collector activity or 
- increased memory footprint
- In extreme cases, such memory leaks can cause disk paging and even program failure with an `OutOfMemoryError`, 
  but such failures are relatively rare.

</details>

### What happens in a memory leak?
<details><summary>Show answer</summary>

- Your application allocates memory for objects or data structures.
- Later, those objects are no longer needed, but references to them still exist 
  (e.g., in a static field, collection, cache, or listener).
- Because the garbage collector sees these references, it cannot free the memory.
- Over time, this unused memory accumulates, leading to:
  - Increased heap usage.
  - Slower performance.
  - Eventually, `OutOfMemoryError` or system instability.

</details>

### Why a garbage collector cannot fix the issue with memory leaks?
<details><summary>Show answer</summary>

A garbage collector (GC) only frees memory for objects that are truly unreachable, 
but a memory leak occurs when objects are still reachable (via references) 
even though they are logically no longer needed.

</details>

### Common causes in Java
<details><summary>Show answer</summary>

Static collections (e.g., Map, List) that keep growing and never cleared.
Listeners or callbacks registered but never removed.
ThreadLocal variables not cleaned up in thread pools.
Caches without eviction policies.
Improper use of singletons holding large object graphs.

</details>

### How to detect memory leaks?
<details><summary>Show answer</summary>

- Use tools like VisualVM, JProfiler, or Eclipse MAT to analyze heap dumps.
- Look for objects that should have been garbage collected but remain referenced.

</details>

### How to fix memory leaks?
<details><summary>Show answer</summary>

- Removing unnecessary references.
- Implementing proper cleanup (e.g., removeListener()).
- Using weak references (WeakHashMap) for caches.
- Applying proper lifecycle management.

</details>

### Memory leak
<details><summary>Show answer</summary>

```java
public class Stack {
  private Object[] elements;
  private int size = 0;
  private static final int DEFAULT_INITIAL_CAPACITY = 16;

  public Stack() {
    elements = new Object[DEFAULT_INITIAL_CAPACITY];
  }

  public void push(Object e) {
    ensureCapacity();
    elements[size++] = e;
  }

  public Object pop() {
    if (size == 0)
      throw new EmptyStackException();
    return elements[--size];
  }

  /**
   * Ensure space for at least one more element, roughly
   * doubling the capacity each time the array needs to grow.
   */
  private void ensureCapacity() {
    if (elements.length == size)
      elements = Arrays.copyOf(elements, 2 * size + 1);
  }
}
```
If a stack grows and then shrinks, the objects that were popped off the stack will not be garbage collected, 
even if the program using the stack has no more references to them. 
This is because the stack maintains obsolete references to these objects. 
An obsolete reference is simply a reference that will never be dereferenced again. 
In this case, any references outside of the _"active portion"_ of the element array are obsolete. 
The active portion consists of the elements whose index is less than size.

null out references once they
become obsolete. In the case of our Stack class, the reference to an item
becomes obsolete as soon as it’s popped off the stack:

```java
public Object pop() {
  if (size == 0)
    throw new EmptyStackException();
  Object result = elements[--size];
  elements[size] = null; // Eliminate obsolete reference
  return result;
}
```
An added benefit of nulling out obsolete references is that if they are
subsequently dereferenced by mistake, the program will immediately fail with a
NullPointerException, rather than quietly doing the wrong thing. It is
always beneficial to detect programming errors as quickly as possible.

When programmers are first stung by this problem, they may overcompensate
by nulling out every object reference as soon as the program is finished using it.
This is neither necessary nor desirable; it clutters up the program unnecessarily.
Nulling out object references should be the exception rather than the norm.
The best way to eliminate an obsolete reference is to let the variable that
contained the reference fall out of scope. This occurs naturally if you define each
variable in the narrowest possible scope

So when should you null out a reference? What aspect of the Stack class
makes it susceptible to memory leaks? Simply put, it manages its own memory.
The storage pool consists of the elements of the elements array (the object
reference cells, not the objects themselves). The elements in the active portion of
the array (as defined earlier) are allocated, and those in the remainder of the
array are free. The garbage collector has no way of knowing this; to the garbage
collector, all of the object references in the elements array are equally valid.
Only the programmer knows that the inactive portion of the array is unimportant.
The programmer effectively communicates this fact to the garbage collector by
manually nulling out array elements as soon as they become part of the inactive
portion.
Generally speaking, whenever a class manages its own memory, the
programmer should be alert for memory leaks. Whenever an element is freed,
any object references contained in the element should be nulled out.

</details>

### Describe the following code
<details><summary>Show answer</summary>

#

</details>

### String instances creation
<details><summary>Show answer</summary>

```java
String s = new String("bikini");
```

The statement creates a new String instance each time it is executed, and none of those object creations is necessary. 
The argument to the String constructor ("bikini") is itself a String instance, 
functionally identical to all of the objects created by the constructor. 
If this usage occurs in a loop or in a frequently invoked method, 
millions of String instances can be created needlessly.
The improved version is simply the following:
```java
String s = "bikini";
```
This version uses a single String instance, rather than creating a new one each time it is executed.

</details>