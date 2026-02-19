## Creating and Destroying Objects

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
- [Unlike constructors, they can help to avoid creating unnecessary duplicate objects](#unnecessary-objects-constructors-vs-static-factories)
- [Can be used by instance-controlled classes](#instance-controlled-classes)
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

### Instance-controlled classes
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

Such flexible static factory methods form the basis of service provider frameworks.
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

</details>

### Service provider frameworks examples
<details><summary>Show answer</summary>

- [Java Logging](#java-logging-and-its-component-roles-in-a-service-provider-framework)
- [the Java Database Connectivity API (JDBC)](#java-database-connectivity-api-jdbc-and-its-component-roles-in-a-service-provider-framework)

</details>

### Java Logging and its component roles in a service provider framework
<details><summary>Show answer</summary>

- Service interface - Logging API – what clients program against
  - org.slf4j.Logger
  - org.apache.logging.log4j.Logger
  - java.util.logging.Logger
- Service providers - Logging bindings / providers
  - logback-classic for SLF4J
  - Log4j2 core for Log4j2 API
- Provider registration / discovery mechanism - Logging provider loader – use different discovery approaches
  - SLF4J - Uses classpath scanning to find a single binding (`StaticLoggerBinder`). 
    This is exactly parallel to `DriverManager` discovering JDBC drivers.
  - Log4j2 - Uses Plugin discovery (annotation processing + plugin cache).
- Service access API – how clients obtain an implementation
  - access methods (LoggerFactory.getLogger(), LogManager.getLogger())
- (Optional) Service provider interface (SPI) – how providers create instances.

</details>

### Java Database Connectivity API (JDBC) and its component roles in a service provider framework
<details><summary>Show answer</summary>

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
- [the Builder pattern](#the-builder-pattern) - Best and the only recommended option

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

```scala
def connect(host: String = "localhost", port: Int = 3306, secure: Boolean = false): Unit =
  println(s"Connecting to $host:$port, secure = $secure")

// Using only the parameters you want (named)
connect(port = 5432)
connect(secure = true, host = "db.example.com")
```

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

### TODO Item 5: Prefer dependency injection to hardwiring resources

### Unnecessary objects creation, examples how not to do, solutions
<details><summary>Show answer</summary>

It is often appropriate to reuse a single object instead of creating
a new functionally equivalent object each time it is needed.
Reuse can be both faster and more stylish. An object can always be reused if it is immutable.

Examples how not to do:
- [String instances creation, what must you care about?](#string-instances-creation-what-must-you-care-about)
- [Autoboxing](#autoboxing)
- [Boolean creation from a string, what must you remember about?](#boolean-creation-from-a-string-what-must-you-remember-about)
- [Using regular expression](#using-regular-expression)

Possible workarounds:
- You can often avoid creating unnecessary objects by [static factory methods](#static-factory-methods)
  The constructor must create a new object each time it’s called,
  while the factory method is never required to do so and won’t in practice.
- [Object caching and reusing](#using-regular-expression)
- [maintaining your own object pool - only for extremely heavyweight objects](#avoiding-object-creation-by-maintaining-your-own-object-pool)

</details>

### Unnecessary objects, constructors vs static factories
<details><summary>Show answer</summary>

Unlike constructors, static factory methods are not required to create a new object each time they’re invoked.
To avoid creating unnecessary duplicate objects they can:
- use preconstructed instances, or
- cache instances as they’re constructed

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

### String instances creation, what must you care about?
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

### Boolean creation from a string, what must you remember about?
<details><summary>Show answer</summary>

- `Boolean(String)` constructor must create a new object each time it’s called.
- the factory method `Boolean.valueOf(String)` is preferable, it reuses existing immutable object

Note: `Boolean(String)`  constructor was deprecated in Java 9.

</details>

### Using regular expression
<details><summary>Show answer</summary>

```java
static boolean isRomanNumeral(String s) {
  return s.matches("^(?=.)M*(C[MD]|D?C{0,3})" + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
}
```
- It’s not suitable for repeated use in performance-critical situations. 
- The problem is that it internally creates a `Pattern` instance for the regular expression and uses it only once, 
  after which it becomes eligible for garbage collection.
- Creating a `Pattern` instance is expensive 
  because it requires compiling the regular expression into a finite state machine.

Solution to improve the performance:
- explicitly compile the regular expression into a Pattern instance (which is immutable) as part of class initialization, 
- cache it
- and reuse the same instance for every invocation

```java
public class RomanNumerals {
  private static final Pattern ROMAN = Pattern.compile(
          "^(?=.)M*(C[MD]|D?C{0,3})"
                  + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");

  static boolean isRomanNumeral(String s) {
    return ROMAN.matcher(s).matches();
  }
}
```

</details>

### What issue might exist with caching from the previous example? How can it be fixed?
<details><summary>Show answer</summary>

```java
public class RomanNumerals {
  private static final Pattern ROMAN = Pattern.compile(
          "^(?=.)M*(C[MD]|D?C{0,3})"
                  + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");

  static boolean isRomanNumeral(String s) {
    return ROMAN.matcher(s).matches();
  }
}
```
- It would be possible to eliminate the initialization by lazily initializing the field 
  the first time the `isRomanNumeral` method is invoked, but this is not recommended.
- As is often the case with lazy initialization, it would complicate the implementation 
  with no measurable performance improvement

</details>

### Avoiding object creation by maintaining your own object pool
<details><summary>Show answer</summary>

- Avoiding object creation by maintaining your own object pool is a bad idea 
  **unless** the objects in the pool are **extremely heavyweight**. 
- The classic example of an object that does justify an object pool is a database connection.

**Maintaining your own object pools clutters your code, increases memory footprint, and harms performance**.

</details>

### In which situations you cannot reuse an existing object and must create a new one?
<details><summary>Show answer</summary>

With defensive copying you should create a new one. Don’t reuse an existing object in such cases.

- The penalty for reusing an object when defensive copying is called for is far greater 
  than the penalty for needlessly creating a duplicate object. 
- Failing to make defensive copies where required can lead to insidious bugs and security holes; 
- creating objects unnecessarily merely affects style and performance.

</details>

### Obsolete references, idea
<details><summary>Show answer</summary>

Objects popped from the stack are never dereferenced:
```java
public Object pop() {
  if (size == 0)
    throw new EmptyStackException();
  return elements[--size];
}
```
- After decrementing size, the element is no longer logically part of the stack.
- But the array slot still contains a reference to that object.
  This means:
    - The object remains reachable from the Stack instance. 
    - As far as the JVM GC is concerned, this object is still in use. 
    - It cannot be garbage collected even though the program no longer needs it. 
    - Over time, especially in long-lived stacks, this produces a memory leak.

Why this is a real memory leak?
This leak does not show up as a sudden OutOfMemoryError.

Instead:
- Memory usage grows slowly and unnecessarily.
- The GC runs more often and works harder.
- Latency increases.
- If the application is long-running (server, background service), memory pressure builds up.

This is a classic **accumulating logical leak**, not a spike.

Solution:
You should always null-out references to "dead" elements:

</details>

### Obsolete references how to avoid and examples
<details><summary>Show answer</summary>

If your class **manually manages object storage** (e.g., with an array, buffer, or custom pooling) and 
an element becomes logically unused, **explicitly clear the reference**.

This applies to:
* caches
* object pools
* stacks/queues/ring buffers/custom collections
* any growable array-like structure
* listeners and other callbacks

How to recognize when you need this rule
Ask yourself:
- Do I store objects in an array or similar manual container?
  If yes → potential for obsolete references.
- Does the container shrink logically without shrinking physically?
  (example: pop, removeAt, dequeue)
  If yes → clear the slot.
- Does my container reuse the same internal array over time?
  If yes → clear stale entries.

</details>


### Java collections and obsolete references
<details><summary>Show answer</summary>

Java collections (`ArrayList`, `HashMap`, etc.) already follow this rule internally - 
to eliminate obsolete object references.

</details>

### Describe a code snippet #X
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

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

</details>

<details><summary>Show answer</summary>

- The implementation does not null out popped values, meaning objects may remain reachable and cause memory leaks.
- A safer pop would be:
  ```java
  Object result = elements[--size];
  elements[size] = null; // eliminate obsolete reference
  return result;
  ```
  Note: **the best solution isn’t to fix your custom Stack at all**.
  The best solution is to replace it with `Deque` (usually `ArrayDeque`).
- parameterize class: `public class Stack<E> { ... }`
- For production code: prefer `Deque<E>` (like `ArrayDeque<E>`) instead of writing your own stack.

</details>

</details>

### For the previous example with stack, describe the workflow in details
<details><summary>Show answer</summary>

- If stack once had 1000 elements, and now has 100, then 900 obsolete references remain.
- But if I start pushing again, those obsolete references will be overwritten.
- Therefore, the leak cannot grow unbounded

**This implementation does NOT leak memory unboundedly.**

It leaks **up to the high-water mark** — the highest number of elements the stack has ever held.

So if:
- the stack once grew to 1000 items
- later shrank to 10
- then it permanently holds references for 990 elements that are _logically deleted but still strongly reachable_

This is a **bounded leak**, but a leak nonetheless.
Such leak is **not infinite**, but **persistent**.

**Why it is still considered a memory leak in Java?**

A memory leak in Java is not only:
> memory that keeps growing forever

It is also:
> memory that should be collectible, but is not collectible because your code keeps references to unused objects.

Even if it is **bounded**, it satisfies the definition.

This is called a "logical leak" (or "loitering objects")

</details>

### When does logical (bounded) leak become a real problem?
<details><summary>Show answer</summary>

Such leaks:
- leaks are bounded
- leaks don’t grow indefinitely
- leaks don’t crash immediately

But they still:
- bloat heap usage
- prevent GC from reclaiming memory
- degrade performance
- cause long-term memory accumulation

Imagine:
- The stack belongs to a singleton.
- Or a thread pool.
- Or a server-side session.
- Or a long-running service.

If the stack peaks once at huge size — say 1M elements — and then is used with only 10 or 20 elements
your app will permanently retain memory for 1M objects.

Even though you never need them again.
This is a classic real-world production failure pattern.

</details>

### Nulling out object references, what it means in general?
<details><summary>Show answer</summary>

**Nulling out object references should be the exception rather than the norm.**

The best way to eliminate an obsolete reference are:
- to let the variable that contained the reference fall out of scope. 
  This occurs naturally if you define each variable in the narrowest possible scope
- to use standard library collections instead of implementing your own data structures that manually manage memory

</details>

### How to avoid obsolete references in caches?
<details><summary>Show answer</summary>

**A cache that grows but never shrinks will eventually leak memory.
To prevent this, you need automatic or explicit eviction.**

- Use `WeakHashMap` for caches where keys determine lifetime

    Idea: If the only reference to a key is inside the cache, the key should be collectable — 
    and the cache entry should disappear automatically.
    
    - Entries disappear automatically once the key becomes weakly reachable.
    - Good for “data associated with an object,” like metadata or mirrors.
    - **Not good for large or long-lived caches** (WeakHashMap is not a general-purpose cache).

- Use `java.util.concurrent` caches (e.g., `CacheBuilder`, custom maps) that auto-expire or are bounded

    Modern Java uses:
    - LRU, LFU, size-based eviction
    - time-based eviction (expireAfterWrite / expireAfterAccess)
    - reference-based eviction (using weak or soft references)
  
    For example, `Guava Cache` (or `Caffeine`):
    - automatically evicts old entries (time-based eviction)
    - prevents memory blow-up
    - never requires manual nulling
  
    **This is the preferred production solution.**

- Run periodic cleanup for hand-made caches.
  - run a periodic cleanup that removes entries older than N minutes
  - or removes entries that haven’t been used recently

- Manually remove entries when they become stale.

  Remove entries you no longer need immediately

</details>

### Memory leaks caused by listeners and other callbacks, example
<details><summary>Show answer</summary>

If you implement an API where clients register callbacks but don’t deregister them explicitly,
they will accumulate unless you take some action.

1. Object A registers itself as a listener for something
2. Object B stores a reference to A (e.g., in a list of listeners)
3. Object A is no longer needed, but
4. B still keeps a reference to A
5. GC cannot collect A because B is still alive

This happens in:
* Swing, JavaFX
* Observer pattern
* Event buses
* Reactive frameworks
* Custom callback registries
* Threading frameworks
* Even scheduled timers!


Possible solutions:
- unregister listeners when you no longer need them
- (preferable) store only weak references to them, for instance, by storing them only as keys in a `WeakHashMap`.

</details>

### Describe a code snippet #X
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class BigObject implements EventListener {
  private byte[] largeData = new byte[10_000_000]; // 10 MB block

  public BigObject(EventSource source) {
    source.addListener(this); // registers as a listener
  }

  @Override
  public void onEvent(Event e) {
    // handle event...
  }
}
public class EventSource {
  private final List<EventListener> listeners = new ArrayList<>();

  public void addListener(EventListener l) {
    listeners.add(l);
  }

  public void fireEvent(Event e) {
    for (var l : listeners) {
      l.onEvent(e);
    }
  }
}
EventSource source = new EventSource();

BigObject obj = new BigObject(source);
obj = null;
```

</details>

<details><summary>Show answer</summary>

- You think it's free to be garbage collected, but it is not. 
- Even though you did: `obj = null;` the object is **not** garbage-collected.
- `EventSource` still has a strong reference to the listener: `listeners -> BigObject`

As long as `EventSource` lives, so does every listener it ever collected.
If many such objects register and disappear logically but remain referenced, memory keeps accumulating.

Correct solutions: 

1. unregister listeners when you no longer need them:
    ```java
    public class BigObject implements EventListener {
      private EventSource source;
    
      public BigObject(EventSource source) {
        this.source = source;
        source.addListener(this);
      }
    
      public void close() {
        source.removeListener(this);
      }
    
      @Override
      public void onEvent(Event e) {
      }
    }
    //usage;
    BigObject obj = new BigObject(source);
    ...
    obj.close(); // prevents the leak
    obj = null;
    ```
2. Better solution for long-lived systems: Weak listeners
    ```java
    private final List<WeakReference<EventListener>> listeners = new ArrayList<>();
    ```
   
</details>

</details>

### TODO Item 8: Avoid finalizers and cleaners

### TODO Item 9: Prefer try-with-resources to try-finally

