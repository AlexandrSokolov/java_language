### Describe a code snippet #01
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<String> strings = new ArrayList<>(List.of("alpha", "bravo", "charlie"));
for (String s : strings) {
  if (! s.contains("r")) {
  strings.remove(s);      
  }
    }
```

</details>

<details><summary>Show answer</summary>

These are structural changes during iteration. You get `ConcurrentModificationException`:
```java
strings.remove(s);      // throws ConcurrentModificationException
```
Solution with an explicit iterator:
```java
List<String> strings = new ArrayList<>(List.of("alpha", "bravo", "charlie"));
for (Iterator<String> itr = strings.iterator() ; itr.hasNext() ; ) {
  String s = itr.next();
  if (! s.contains("r")) {
    itr.remove();
  }
}
```
If memory constraints don’t prevent you from making a new copy of the list,
streams offer a neater solution to this problem:
```java
List<String> strings = new ArrayList<>(List.of("alpha", "bravo", "charlie"));
List<String> modifiedStrings = strings.stream()
  .filter(s -> s.contains("r"))
  .toList();
assert modifiedStrings.equals(List.of("bravo", "charlie"));
```

</details>

</details>

---

### Describe a code snippet #02
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
class Person {
  private final String name;
  public Person(String name) {
    this.name = name;
  }
  @Override
  public boolean equals(Object o) {
    return o instanceof Person p && name.equals(p.name);
  }
}
Set<Person> people = new HashSet<>();
people.add(new Person("Alice"));
  if (people.contains(new Person("Alice"))) {
  //do something  
  }
```

</details>

<details><summary>Show answer</summary>

This class should override `hashCode` so that it depends only on the same field that equals depends on (i.e., name).
But it does not; as a result, hashed collections will not work correctly.

Solution - implement `hashCode()`:

```java
class Person {
  private final String name;
  public Person(String name) {
    this.name = name;
  }
  @Override
  public boolean equals(Object o) {
    return o instanceof Person p && name.equals(p.name);
  }
  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
```

</details>

</details>

---

### Describe a code snippet #03
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
```java
class Person {
  private String name;
  public Person(String name) {
    this.name = name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public int hashCode() {
    return name.hashCode();
  }
  public boolean equals(Object o) {
    return o instanceof Person p && name.equals(p.name);
  }
}
Set<Person> people = new HashSet<>();
Person alice = new Person("Alice");
people.add(alice);
alice.setName("Bob");
//what the following checks return:
people.contains(new Person("Alice"));
people.contains(new Person("Bob"));
```

</details>

<details><summary>Show answer</summary>

An object of type `Person` can be reliably retrieved, but only if the value of name is unchanged.
If that field is modified, the collection can no longer match the object with either the old or the new value.

To prevent problems of this kind, make sure the fields a `Set`, `Map`, or internally ordered `Queue` 
relies on for ordering or lookup are immutable.

</details>

</details>

---


### Describe a code snippet #04
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
Collection<String> cs = ...
String[] sa = cs.toArray(new String[0]);
```

</details>

<details><summary>Show answer</summary>

The new array will be created during `toArray` method execution,
although if the array supplied as the argument to the second overload of `toArray` is long enough,
it is used to receive the elements of the collection, overwriting its existing elements.

</details>

</details>

---


### Describe a code snippet #05
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List.of(1, 2, 3).toArray(new String[0]);
```

</details>

<details><summary>Show answer</summary>

The code compiles successfully but throws `ArrayStoreException` at run time.

</details>

</details>

---


### Describe a code snippet #06
<details><summary><strong>Show details</strong></summary>

<details><summary>Show code</summary>

```java
List<Integer> l = List.of(0, 1, 2);
int[] a = l.toArray(new int[0]);  
```

</details>

<details><summary>Show answer</summary>

You get compile-time error.
This is illegal because the parameter `T` in the method call must - as for any type parameter - be a reference type.

Solutions:
1. resort to copying the array explicitly:
    ```java
    jshell> List<Integer> integers = List.of(0, 1, 2);
    integers ==> [0, 1, 2]
    jshell> int[] ints = new int[integers.size()];
    ints ==> int[3] { 0, 0, 0 }
    jshell> for (int i=0; i<integers.size(); i++) { ints[i] = integers.get(i); }
    jshell> ints
    ints ==> int[3] { 0, 1, 2 }
    ```
2. using the Stream API:
    ```java
    jshell> int[] ints = integers.stream()
       ...>     .mapToInt(Integer::intValue)
       ...>     .toArray();
    ints ==> int[3] { 0, 1, 2 
    ```

</details>

</details>

---