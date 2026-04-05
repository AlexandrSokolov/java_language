###
<details><summary>Show answer</summary>

</details>

## Range limits and overflow / underflow

### Range limits and overflow / underflow topics
<details><summary>Show answer</summary>

- Every numeric primitive has fixed bounds (byte, short, int, long, float, double)
- Overflow happens silently for integer types
- Underflow and overflow behave differently for floating-point numbers
- No built-in overflow checks for primitives

Typical problems:

int silently wrapping around
Assuming long is “big enough”
Forgetting that float/double can produce Infinity or NaN

</details>

### What are the numeric ranges of Java primitive integer types?
<details><summary>Show answer</summary>

</details>


### What issues can happen dur to the range limits of numeric data types?
<details><summary>Show answer</summary>

- overflow
- underflow - What is integer underflow?

</details>

### What happens when an int overflows in Java? 
<details><summary>Show answer</summary>

Is an exception thrown?

</details>

### How does Java handle overflow for long?
<details><summary>Show answer</summary>

</details>

### How does floating-point overflow differ from integer overflow?
<details><summary>Show answer</summary>

</details>


### What values can double produce on overflow?
<details><summary>Show answer</summary>

</details>

### How to prevent integer overflow in Java?
<details><summary>Show answer</summary>

How can you detect or prevent integer overflow in Java?

</details>

### When would Math.addExact() be useful?
<details><summary>Show answer</summary>

</details>

## Floating-point precision and rounding

### Precision and rounding errors (especially floating-point)
<details><summary>Show answer</summary>

float and double cannot represent most decimal values exactly
Comparing floating-point numbers with == is unsafe
Cumulative rounding errors in calculations
Different JVMs / CPUs can produce slightly different results in edge cases

Typical problems:

Monetary calculations with double
Unexpected comparison failures
“Why is 0.1 + 0.2 not 0.3?”

</details>

### Why can’t double precisely represent decimal numbers like 0.1?
<details><summary>Show answer</summary>

</details>

### Why is 0.1 + 0.2 != 0.3 in Java?
<details><summary>Show answer</summary>

</details>

### Is float ever a good choice over double?
<details><summary>Show answer</summary>

</details>

### Why should you not compare double values using ==?
<details><summary>Show answer</summary>

</details>

### How do you correctly compare floating-point numbers?
<details><summary>Show answer</summary>

</details>

### What is rounding error, and how does it accumulate?
<details><summary>Show answer</summary>

</details>

### What is machine epsilon?
<details><summary>Show answer</summary>

</details>

## Choosing the correct numeric type

### Choosing the wrong data type
<details><summary>Show answer</summary>

Using floating-point numbers where exact values are required
Using int where long is required (IDs, timestamps)
Overusing wrapper types (Integer, Long) without need
Using BigDecimal without understanding its cost

Typical problems:

Performance degradation
Incorrect business results
Hard-to-read and over-engineered code

</details>

### How do you decide between int, long, double, and BigDecimal?
<details><summary>Show answer</summary>

</details>

### Why is double a bad choice for money?
<details><summary>Show answer</summary>

</details>

### When should BigDecimal be used?
<details><summary>Show answer</summary>

</details>

### What are the downsides of BigDecimal?
<details><summary>Show answer</summary>

</details>

### Can long safely represent timestamps?
<details><summary>Show answer</summary>

</details>

### What numeric type would you choose for an ID and why?
<details><summary>Show answer</summary>

</details>

## Integer division pitfalls

### Integer division pitfalls topics
<details><summary>Show answer</summary>

Integer division truncates toward zero
Loss of fractional part without warning
Implicit type promotion can hide bugs

Typical problems:

5 / 2 == 2
Wrong averages
Mixing int, long, and double accidentally

</details>

### What is the result of 5 / 2 in Java and why?
<details><summary>Show answer</summary>

</details>

### How do you get a precise result when dividing integers?
<details><summary>Show answer</summary>

</details>

### Why does integer division truncate instead of round?
<details><summary>Show answer</summary>

</details>

### How can integer division lead to subtle bugs?
<details><summary>Show answer</summary>

</details>

### What happens if one operand is cast to double?
<details><summary>Show answer</summary>

</details>

## Autoboxing and unboxing

### Autoboxing and unboxing dangers topics

Silent conversion between primitives and wrappers
NullPointerException during unboxing
Performance overhead in tight loops

Typical problems:

Integer compared with ==
Null values in numeric wrappers
Hidden allocations

### What is autoboxing and unboxing in Java?
<details><summary>Show answer</summary>

</details>

### When does autoboxing happen implicitly?
<details><summary>Show answer</summary>

</details>

### What are the performance implications of autoboxing?
<details><summary>Show answer</summary>

</details>

### How can unboxing lead to NullPointerException?
<details><summary>Show answer</summary>

</details>

### Why can autoboxing be dangerous in loops?
<details><summary>Show answer</summary>

</details>

## Equality and comparison semantics

### Equality and comparison semantics topics

== vs .equals() for wrapper types
Floating-point comparison rules
Special values: NaN, Infinity, -0.0

Typical problems:

Cached Integer values behaving “magically”
NaN not equal to itself
Incorrect sorting or comparisons

### What’s the difference between == and .equals() for numeric wrappers?
<details><summary>Show answer</summary>

</details>

### Why does Integer.valueOf(100) == Integer.valueOf(100) return true?
<details><summary>Show answer</summary>

</details>

### What is Integer caching in Java?
<details><summary>Show answer</summary>

</details>

### Why is NaN != NaN?
<details><summary>Show answer</summary>

</details>

### How does Java compare -0.0 and 0.0?
<details><summary>Show answer</summary>

</details>

### How should floating-point values be compared safely?
<details><summary>Show answer</summary>

</details>

## Type promotion and casting rules

### Type promotion and implicit casting rules

Automatic promotion in expressions
Unexpected widening to int or long
Narrowing casts causing data loss

Typical problems:

Math behaving differently than expected
Loss of precision after casting
Bugs hidden in complex expressions

### How does Java perform numeric type promotion in expressions?
<details><summary>Show answer</summary>

</details>

### Why does byte + byte result in an int?
<details><summary>Show answer</summary>

</details>

### What are widening vs narrowing conversions?
<details><summary>Show answer</summary>

</details>

### What happens when you cast a long to int?
<details><summary>Show answer</summary>

</details>

### Can type casting lead to silent data loss?
<details><summary>Show answer</summary>

</details>

### How can implicit promotion introduce bugs?
<details><summary>Show answer</summary>

</details>

## Performance considerations

### Performance considerations topics

Primitive vs wrapper performance
Cost of BigInteger / BigDecimal
Memory footprint of numeric types
Impact on garbage collection

Typical problems:

Unnecessary boxing
Large-scale numeric processing being slow
Overusing precision when it’s not needed

### What is the performance difference between primitives and wrapper types?
<details><summary>Show answer</summary>

</details>

### Why are wrapper classes more expensive than primitives?
<details><summary>Show answer</summary>

</details>

### When does BigDecimal become a performance problem?
<details><summary>Show answer</summary>

</details>

### How do numeric choices affect garbage collection?
<details><summary>Show answer</summary>

</details>

### Why does using Integer in collections impact performance?
<details><summary>Show answer</summary>

</details>

## Serialization, persistence, and interoperability

### Serialization, persistence, and interoperability topics

Numeric limits across systems (DB, JSON, APIs)
Precision loss during serialization
Mismatch between Java and external formats

Typical problems:

long values broken in JavaScript
Decimal precision lost in JSON
Database column mismatches

### What happens to large long values when sent to JavaScript?
<details><summary>Show answer</summary>

</details>

### Why is numeric precision often lost in JSON?
<details><summary>Show answer</summary>

</details>

### How can database numeric types cause data loss in Java?
<details><summary>Show answer</summary>

</details>

### What issues arise when serializing BigDecimal?
<details><summary>Show answer</summary>

</details>

### How do you safely transfer numeric data between systems?
<details><summary>Show answer</summary>

</details>

## Code readability and intent

### Readability and intent clarity

Magic numbers
Missing units (seconds vs milliseconds)
Implicit assumptions about scale or precision

Typical problems:

Maintenance nightmares
Bugs caused by misunderstood units
Code that “works” but is misleading

### Why are magic numbers dangerous?
<details><summary>Show answer</summary>

</details>

### Why should numeric units be explicit?
<details><summary>Show answer</summary>

</details>

### What problems arise when using raw numbers for time or distance?
<details><summary>Show answer</summary>

</details>

### How do enums or value objects help with numeric clarity?
<details><summary>Show answer</summary>

</details>

### How do you make numeric-heavy code self-documenting?
<details><summary>Show answer</summary>

</details>

## Testing numeric edge cases

### Testing edge cases

Boundary values are often untested
Extreme values expose numeric bugs
Missing tests for overflow or precision loss

Typical problems:

Production-only bugs
Failures under high load or large data
Incorrect assumptions verified too late

### Why are boundary values important in numeric tests?
<details><summary>Show answer</summary>

</details>

### What edge cases should be tested for numeric code?
<details><summary>Show answer</summary>

</details>

### How do you test overflow behavior?
<details><summary>Show answer</summary>

</details>

### Why do numeric bugs often appear only in production?
<details><summary>Show answer</summary>

</details>

### How would you test floating-point accuracy?
<details><summary>Show answer</summary>

</details>

## Bonus senior-level / discussion questions

### How would you design a money API in Java?
<details><summary>Show answer</summary>

</details>

### How would you protect a system from numeric overflow bugs?
<details><summary>Show answer</summary>

</details>

### How do modern JVMs handle numeric optimizations?
<details><summary>Show answer</summary>

</details>

### When is correctness more important than performance in numeric code?
<details><summary>Show answer</summary>

</details>


###
- [Precision vs Scale](#precision-vs-scale)
- [`BigDecimal` creation](#bigdecimal-creation-issue)
- [`BigDecimal` comparison](#bigdecimal-comparison-issue)
- [`BigDecimal`, how to divide](#bigdecimal-how-to-divide)


### Precision vs Scale

- `Precision` is the overall limit of, well, precision, in a number. 
    E.g. `1234` and `12.34` have the same precision (4 decimal digits).
- `Scale` is the limit of the digits after the decimal point. 
    `3.34` and `234.25` have the same scale (2 digits after the decimal point).

### `BigDecimal` creation issue

Never create it from double, use creation from string instead:
```java
assertNotEquals(new BigDecimal("0.3"), new BigDecimal(0.3));
```
- [See `BigDecimalTest.bigDecimal_positiveCreation()](src/test/java/com/savdev/data/types/numeric/BigDecimalTest.java)
- [See `BigDecimalTest.bigDecimal_checkNegativeOrPositive()](src/test/java/com/savdev/data/types/numeric/BigDecimalTest.java)

### `BigDecimal` comparison issue

Don't use `equals`:
```java
assertFalse(BigDecimal.ZERO.equals(new BigDecimal("0.0000")));
```
Use `compareTo` instead.


[See `BigDecimalTest.bigDecimal_comparison()](src/test/java/com/savdev/data/types/numeric/BigDecimalTest.java)

### `BigDecimal`, how to divide

Always use either:
- `RoundingMode` or
- both `scale` and `RoundingMode` 