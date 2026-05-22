### Describe a code snippet #01
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class CaseInsensitiveString {
  private final String s;
  public CaseInsensitiveString(String s) {
    this.s = Objects.requireNonNull(s);
  }
  @Override public boolean equals(Object o) {
    if (o instanceof CaseInsensitiveString)
      return s.equalsIgnoreCase(
        ((CaseInsensitiveString) o).s);
    if (o instanceof String) 
      return s.equalsIgnoreCase((String) o);
    return false;
  }
}
```

</details>

<details><summary>Show answer</summary>

The `equals` implementation violates the symmetry requirement of the equals contract.

A `CaseInsensitiveString` instance can be equal to a `String` instance
(because it explicitly handles o instanceof `String`), but the reverse is not true:
a `String` will never consider itself equal to a `CaseInsensitiveString`,
since `String.equals` only compares to other String objects.


As a result, there exist objects x and y such that `x.equals(y)` returns `true` while `y.equals(x)` returns `false`,
which breaks the symmetry rule of the equals contract.

Remove the ill‑conceived attempt to make equals interoperate with String:
```java
@Override public boolean equals(Object o) {
  return o instanceof CaseInsensitiveString &&
    ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
}
```

</details>

</details>

---

### Describe a code snippet #02
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Point {
  private final int x;
  private final int y;
  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }
  @Override public boolean equals(Object o) {
    if (!(o instanceof Point))
      return false;
    Point p = (Point)o;
    return p.x == x && p.y == y;
  }
}
public class ColorPoint extends Point {
  private final Color color;
  public ColorPoint(int x, int y, Color color) {
    super(x, y);
    this.color = color;
  }
}
```

</details>

<details><summary>Show answer</summary>

Without overitten `equals` in ColourPoint the color information is ignored in equals comparisons.
While this does not violate the equals contract, it is clearly unacceptable.


[It is not possible to extend a **concrete, instantiable class** and **add a new value component**](2.1_methods_common_to_all_objects.md#what-should-you-consider-regarding-the-equality-contract-when-creating-subclasses)
while fully preserving all requirements of the equals contract (in particular, symmetry and transitivity).


In such cases, you should [favor composition over inheritance](2.1_methods_common_to_all_objects.md#how-should-valuesubclass-equality-problems-be-handled).

</details>

</details>

---

### Describe a code snippet #03
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Point {
  private final int x;
  private final int y;
  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }
  @Override public boolean equals(Object o) {
    if (!(o instanceof Point))
      return false;
    Point p = (Point)o;
    return p.x == x && p.y == y;
  }
}
public class ColorPoint extends Point {
  private final Color color;
  public ColorPoint(int x, int y, Color color) {
    super(x, y);
    this.color = color;
  }
  @Override public boolean equals(Object o) {
    if (!(o instanceof ColorPoint))
      return false;
    return super.equals(o) && ((ColorPoint) o).color == color;
  }
}
```

</details>

<details><summary>Show answer</summary>

This solution violates symmetry. You might get different results when comparing a point to a color point and vice versa.
This comparison always returns false because the type of the argument is incorrect.

```java
Point p = new Point(1, 2);
ColorPoint cp = new ColorPoint(1, 2, Color.RED);
```

Then `p.equals(cp)` returns true, while `cp.equals(p)` returns false.


[It is not possible to extend a **concrete, instantiable class** and **add a new value component**](2.1_methods_common_to_all_objects.md#what-should-you-consider-regarding-the-equality-contract-when-creating-subclasses)
while fully preserving all requirements of the equals contract (in particular, symmetry and transitivity).


In such cases, you should [favor composition over inheritance](2.1_methods_common_to_all_objects.md#how-should-valuesubclass-equality-problems-be-handled).

</details>

</details>

---

### Describe a code snippet #04
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public class Point {
  private final int x;
  private final int y;
  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }
  @Override public boolean equals(Object o) {
    if (!(o instanceof Point))
      return false;
    Point p = (Point)o;
    return p.x == x && p.y == y;
  }
}
public class ColorPoint extends Point {
  private final Color color;
  public ColorPoint(int x, int y, Color color) {
    super(x, y);
    this.color = color;
  }
  @Override public boolean equals(Object o) {
    if (!(o instanceof Point))
      return false;
    // If o is a normal Point, do a color-blind comparison
    if (!(o instanceof ColorPoint))
      return o.equals(this);
    // o is a ColorPoint; do a full comparison
    return super.equals(o) && ((ColorPoint) o).color == color;
  }
}
```

</details>

<details><summary>Show answer</summary>

This approach violates transitivity - it does provide symmetry, but at the expense of transitivity:

```java
ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
Point p2 = new Point(1, 2);
ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);
```

Now `p1.equals(p2)` and `p2.equals(p3)` return true, while `p1.equals(p3)` returns false, a clear violation of transitivity.

The first two comparisons are “color-blind,” while the third takes color into account.

[It is not possible to extend a **concrete, instantiable class** and **add a new value component**](2.1_methods_common_to_all_objects.md#what-should-you-consider-regarding-the-equality-contract-when-creating-subclasses)
while fully preserving all requirements of the equals contract (in particular, symmetry and transitivity).


In such cases, you should [favor composition over inheritance](2.1_methods_common_to_all_objects.md#how-should-valuesubclass-equality-problems-be-handled).

</details>

</details>

---

### Describe a code snippet #05
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
@Override public boolean equals(Object o) {
  if (o == null || o.getClass() != getClass())
    return false;
  Point p = (Point) o;
  return p.x == x && p.y == y;
}
```

</details>

<details><summary>Show answer</summary>

This solution violates Liskov substitution principle.

</details>

</details>

---

### Describe a code snippet #06
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
@Override public boolean equals(Object o) {
  if (o == null)
    return false;
  ...
}
```

</details>

<details><summary>Show answer</summary>

Many classes have equals methods that guard against it with an explicit test for null.


This test is unnecessary. To test its argument for equality, 
the equals method must first cast its argument to an appropriate type 
so its accessors can be invoked or its fields accessed:
```java
  @Override public boolean equals(Object o) {
    if (!(o instanceof MyType)) return false;
    MyType mt = (MyType) o;
    ...
  }
```
The instanceof operator is specified to return false if its first operand is null, 
so you don’t need an explicit null check.

</details>

</details>

---

### Implement `equals` for the following class
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public final class PhoneNumber {
  private final short areaCode, prefix, lineNum;

  public PhoneNumber(int areaCode, int prefix, int lineNum) {
    this.areaCode = rangeCheck(areaCode, 999, "area code");
    this.prefix = rangeCheck(prefix, 999, "prefix");
    this.lineNum = rangeCheck(lineNum, 9999, "line num");
  }

  private static short rangeCheck(int val, int max, String arg) {
    if (val < 0 || val > max)
      throw new IllegalArgumentException(arg + ": " + val);
    return (short) val;
  }
}
```

</details>

<details><summary>Show answer</summary>

```java
  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof PhoneNumber))
      return false;
    PhoneNumber pn = (PhoneNumber) o;
    return pn.lineNum == lineNum && pn.prefix == prefix
      && pn.areaCode == areaCode;
  }
```

</details>

</details>

---

### Describe a code snippet #07
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public boolean equals(MyClass o) {
...
}
```

</details>

<details><summary>Show answer</summary>

The `equals` method must always have the following signature:

```java
@Override
public boolean equals(Object o)
```

A common mistake is to write an `equals` method like this:

```java
public boolean equals(MyClass o) {
    ...
}
```

This method does **not override** `Object.equals(Object)` — it **overloads** it instead.
As a result, it is never invoked by Java framework code (such as collections), which
relies strictly on the `equals(Object)` contract. This often leads to subtle and confusing
bugs.

Providing such a “strongly typed” `equals` method is unacceptable, even in addition to
a correctly declared one. It can cause `@Override` annotations in subclasses to generate
false positives, creating a misleading sense of correctness.

Consistent use of the **`@Override` annotation** prevents this mistake. If the method
signature is incorrect, the compiler will report an error immediately, making the
problem obvious and preventing incorrect behavior.

```java
// Still broken, but won’t compile
@Override public boolean equals(MyClass o) {
...
}
```

</details>

</details>

---

### Describe a code snippet #08
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
@Override public int hashCode() { return 42; }
```

</details>

<details><summary>Show answer</summary>

If `hashCode()` returns `42` for all elements in a set, then every element ends up in the same bucket
of the underlying hash table. The set will still work correctly because duplicates are ultimately detected using
`equals()`, but all operations degrade to linear time.
Adding, searching, and removing elements becomes `O(N)` instead of `O(1)` because the set must scan
through the entire list of elements stored in that single bucket.

</details>

</details>

---

### Describe a code snippet #09
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
static Comparator<Object> hashCodeOrder = new Comparator<>() {
  public int compare(Object o1, Object o2) {
    return o1.hashCode() - o2.hashCode();
  }
};
```

</details>

<details><summary>Show answer</summary>

Using `hashCode()` for ordering violates the comparator contract and results in arbitrary, unstable, and error‑prone 
behavior that can break sorted collections in subtle ways.

</details>

</details>

---

### Describe a code snippet #10
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
public static void main(String[] args) {
  List<String> strings = new ArrayList<>();
  add(strings, Integer.valueOf(42));
  String s = strings.get(0);
}

private static void add(List list, Object o) {
  list.add(o);
}
```

</details>

<details><summary>Show answer</summary>

This program compiles, but because it uses the raw type `List`, you get a warning:

> Test.java:10: warning: [unchecked] unchecked call to add(E) as a
> member of the raw type List
> list.add(o);

If you run the program, you get a `ClassCastException` when the program tries to cast 
the result of the invocation `strings.get(0)`, which is an `Integer`, to a `String`. 
This is a compiler-generated cast, so it’s normally guaranteed to succeed, 
but in this case we ignored a compiler warning and paid the price.


If you replace the raw type `List` with the parameterized type `List<Object>`:

```java
private static void add(List<Object> list, Object o) {
  list.add(o);
}
```

and try to recompile the program, you’ll find that it no longer compiles but emits the error message:

> Test.java:5: error: incompatible types:
> List<String> cannot be converted to List<Object&gt;</br>
> add(strings, Integer.valueOf(42));

</details>

</details>

---

### Describe a code snippet #11
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
static int numElementsInCommon(Set s1, Set s2) {
  int result = 0;
  for (Object o1 : s1)
    if (s2.contains(o1))
      result++;
  return result;
}
```

</details>

<details><summary>Show answer</summary>


- This method works, but it uses raw types, which are dangerous and produces a warning.
  The safe alternative is to use unbounded wildcard types. If you want to use a generic type 
  but don't know or care about the actual type parameter, you can use a question mark (?) as a placeholder.
  ```java
  // Uses unbounded wildcard type - typesafe and flexible
  static int numElementsInCommon(Set<?> s1, Set<?> s2) { ... }
  ```
- The logic itself is correct but the method could simply be replaced by:
  ```java
  s1.stream().filter(s2::contains).count();
  ```
- Or even more directly using the Java Collections API:
  ```java
  Set<?> intersection = new HashSet<>(s1);
  intersection.retainAll(s2);
  return intersection.size();
  ```

</details>

</details>

---

### Describe a code snippet 12
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<String> strings = new ArrayList<>();
strings.add("Java");

List list = strings; 
list.add(10);

for (String s : strings) {
  System.out.println(s); // What happens here?
}
```

</details>

<details><summary>Show answer</summary>

- `List list = strings;` - assigning parameterized to raw
- `list.add(10);`        
  - the compiler produces an unchecked warning here, but does not prevent it.
  - no `ClassCastException` here
  - The raw list allows adding an Integer because it ignores type parameters. This is known as **Heap Pollution**.
- `System.out.println(s);` - throw a `ClassCastException` at runtime during the enhanced for-loop.

</details>

</details>

---

### Describe a code snippet 13
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<String>[] stringLists = new List<String>[1]; // (1)
List<Integer> intList = List.of(42);              // (2)
Object[] objects = stringLists;                   // (3)
objects[0] = intList;                             // (4)
String s = stringLists[0].get(0);                 // (5)
```

</details>

<details><summary>Show answer</summary>

**Line (1)** — `List<String>[] stringLists = new List<String>[1];`
- Hypothetically allocates a generic array; assume it compiles for the sake of the argument.
- In real Java this is a compile-time error — the whole snippet exists to justify *why* it must be.
- The language forbids it explicitly — JLS §15.10.1 lists *generic array creation* among the compile-time errors,
  producing the diagnostic **"generic array creation"**.

**Line (2)** — `List<Integer> intList = List.of(42);`
- Creates an immutable `List<Integer>` holding the single boxed value `42`.
- Sets up a payload whose element type is incompatible with `List<String>`.

**Line (3)** — `Object[] objects = stringLists;`
- Widens the reference from `List<String>[]` to `Object[]`.
- Legal because arrays are **covariant**: `T[]` is a subtype of `Object[]` for any reference `T`.

**Line (4)** — `objects[0] = intList;`
- Stores a `List<Integer>` into a slot declared to hold `List<String>`.
- No `ArrayStoreException` is thrown — at runtime erasure has collapsed both sides to the raw type `List`,
  so the array's component check (`List[]` expects `List`) sees a match.
- The type system has now been silently corrupted: heap pollution.

**Line (5)** — `String s = stringLists[0].get(0);`
- Reads the sole element from the sole list; the compiler inserts an implicit cast to `String`.
- The actual object is an `Integer`, so the cast fails with `ClassCastException` at runtime.
- The error surfaces far from its true cause (line 1), which is exactly the failure mode generics aim to prevent.

**Conclusion** — for the implicit cast in (5) to remain trustworthy, the chain must be broken at its weakest link.
That link is (1): generic array creation is therefore a compile-time error.

</details>

</details>

---

### Describe a code snippet #X
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
example();
```

</details>

<details><summary>Show answer</summary>

Your explanation goes here.

</details>

</details>

---

### Describe a code snippet #X
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
example();
```

</details>

<details><summary>Show answer</summary>

Your explanation goes here.

</details>

</details>

---