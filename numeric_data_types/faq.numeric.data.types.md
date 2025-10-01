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