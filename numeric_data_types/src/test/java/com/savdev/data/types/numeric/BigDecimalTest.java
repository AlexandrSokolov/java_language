package com.savdev.data.types.numeric;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import static org.junit.jupiter.api.Assertions.*;

public class BigDecimalTest {

  public static final int TEST_INT = 125;
  public static final String TEST_DECIMAL_PART = "0.2345678912";
  public static final double TEST_DOUBLE = 125.2345678912;
  public static final String TEST_DECIMAL = "125.2345678912";

  @Test
  public void bigDecimal_positiveCreation() {
    new BigDecimal(TEST_DECIMAL); //from decimal String
    new BigDecimal(TEST_INT); //from int

    new BigDecimal(TEST_DOUBLE); //from double
    //be careful to create BigDecimal from double or float
    //see https://stackoverflow.com/questions/3693014/bigdecimal-from-double-incorrect-value
    assertNotEquals(new BigDecimal("0.3"), new BigDecimal(0.3));

    //constants:
    Assertions.assertNotNull(BigDecimal.ZERO);
    Assertions.assertNotNull(BigDecimal.ONE);
    Assertions.assertNotNull(BigDecimal.TWO);
    Assertions.assertNotNull(BigDecimal.TEN);
  }

  @Test
  public void bigDecimal_negativeCreation() {
    assertEquals(
      new BigDecimal("-1"),
      new BigDecimal(TEST_INT).negate());
  }

  @Test
  public void bigDecimal_comparison() {
    //avoid comparing using `equals`
    //it considers two BigDecimal objects equal only if they are equal in both value and scale.
    assertTrue(BigDecimal.ZERO.equals(new BigDecimal("0")));
    assertFalse(BigDecimal.ZERO.equals(new BigDecimal("0.0000")));

    //use compareTo instead
    //compareTo returns:
    //-1 if THIS smaller than THAT
    // 0 THIS and THAT are equal
    // 1 if THIS is bigger than THAT
    assertEquals(0, BigDecimal.ZERO.compareTo(new BigDecimal("0")));
    assertEquals(0, BigDecimal.ZERO.compareTo(new BigDecimal("0.0000")));

    assertEquals(-1, BigDecimal.ZERO.compareTo(BigDecimal.TEN));
    assertEquals(1, BigDecimal.ZERO.compareTo(BigDecimal.TEN.negate()));
  }

  @Test
  public void bigDecimal_checkNegativeOrPositive() {
    //use signum(), it returns:
    //-1 for negative
    // 0 for zero
    // 1 for positive
    assertEquals(-1, BigDecimal.TEN.negate().signum());
    assertEquals(0, BigDecimal.ZERO.signum());
    assertEquals(1, BigDecimal.TEN.signum());
  }

  @Test
  public void bigDecimal_divide() {
    var dividend = BigDecimal.TEN;
    var divisor = new BigDecimal(3);
    //throws ArithmeticException if:
    // - this.scale() is insufficient to represent the result of the division exactly
    // - divisor == 0
    // - roundingMode == ROUND_UNNECESSARY
    Exception exception = Assertions.assertThrows(
      ArithmeticException.class,
      () -> dividend.divide(divisor));
    assertEquals("Non-terminating decimal expansion; no exact representable decimal result.",
      exception.getMessage());

    //you must specify either rounding mode:
    //10 : 3 = 3 (with RoundingMode.HALF_UP - rounds towards the nearest neighbor, with ties rounded up.)
    assertEquals(
      new BigDecimal(3),
      dividend.divide(divisor, RoundingMode.HALF_UP));

    //10 : 3 = 4 (with RoundingMode.UP)
    assertEquals(
      new BigDecimal(4),
      dividend.divide(divisor, RoundingMode.UP));

    //10 : 3 = 3 (with RoundingMode.HALF_DOWN - rounds towards the nearest neighbor, with ties rounded down.)
    assertEquals(
      new BigDecimal(3),
      dividend.divide(divisor, RoundingMode.HALF_DOWN));

    //specify both scale (number of decimal places) and rounding mode:
    assertEquals(
      new BigDecimal("3.3333"),
      dividend.divide(divisor, 4, RoundingMode.HALF_UP));
  }

  @Test
  public void bigDecimal_multiply() {
    BigDecimal bigDecimal = new BigDecimal(TEST_DECIMAL).multiply(
      new BigDecimal(TEST_INT),
      new MathContext(
        /*Precision: The number of digits to be used for an operation; this accuracy is rounded to results.*/
        5,
        /*the rounding algorithm*/ RoundingMode.HALF_UP));
  }

  @Test
  public void bigDecimal_extractIntPart() {
    BigDecimal.TEN.intValue();
    BigDecimal.TEN.intValueExact();
    BigDecimal.TEN.toBigInteger();
    BigDecimal.TEN.toBigIntegerExact();
  }

  @Test
  public void bigDecimal_extractDecimalPart() {
    BigDecimal bd = new BigDecimal( TEST_DECIMAL);
    BigDecimal fractionalPart = bd.remainder( BigDecimal.ONE ); // 0.2345678912
    assertEquals(
      new BigDecimal(TEST_DECIMAL_PART),
      fractionalPart);
  }

  @Test
  public void bigDecimal_formatting() {
    //show original `125.2345678912` with 4 digits after as `125.2346`
    assertEquals(
      "125.2346",
      new BigDecimal(TEST_DECIMAL).setScale(4, RoundingMode.HALF_UP).toString()
    );

    assertEquals(
      "125.00",
      new DecimalFormat("###,###,###.00").format(
        new BigDecimal(TEST_INT))
    );

    assertEquals(
      "125.23",
      new DecimalFormat("###,###,###.00").format(
        new BigDecimal(TEST_DECIMAL))
    );
  }
}
