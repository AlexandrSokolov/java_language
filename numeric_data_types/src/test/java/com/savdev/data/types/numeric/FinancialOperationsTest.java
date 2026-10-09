package com.savdev.data.types.numeric;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FinancialOperationsTest {

  final static BigDecimal amount = new BigDecimal("125000.00");

  @Test
  public void currencyFormat_fromLocale() {

    assertEquals(
      "$125,000.00",
      NumberFormat.getCurrencyInstance(Locale.US).format(amount));

    assertEquals(
      "125.000,00 €",
      NumberFormat.getCurrencyInstance(Locale.GERMANY).format(amount));

    assertEquals(
      "125.000,00 €",
      NumberFormat.getCurrencyInstance(new Locale("de", "DE")).format(amount));
  }

  @Test
  public void currencyFormat_asValue() {
    assertEquals(
      "125,000.00",
      new DecimalFormat("###,###,###.00").format(amount));
  }

  /*
    Currency conversion often involves dividing an amount in one currency
      by an exchange rate to obtain its equivalent in another currency.
   */
  @Test
  public void currencyConversion_ExchangeRatePer1Amount() {
    BigDecimal amountInUSD = new BigDecimal("250.75"); // Amount in USD
    BigDecimal exchangeRate = new BigDecimal("1.0915"); // USD to EUR rate for one

    // Convert to EUR with precision of 4 decimal places
    BigDecimal amountInEUR = amountInUSD.divide(exchangeRate, 4, RoundingMode.HALF_UP);
    assertEquals(
      BigDecimal.ZERO.intValue(),
      new BigDecimal("229.7297").compareTo(amountInEUR));
  }

  @Test
  public void currencyConversion_ExchangeRatePer1000Amount() {
    BigDecimal amountInEUR = new BigDecimal("100.00"); // Amount in USD
    BigDecimal exchangeRatePer10000Units = new BigDecimal("840.3361"); // EUR to 10000 NOK rate
    BigDecimal exchangeRateUnits = new BigDecimal("10000");

    // Convert to EUR with precision of 4 decimal places
    BigDecimal amountInNOK = amountInEUR.multiply(exchangeRateUnits)
      .divide(exchangeRatePer10000Units, 4, RoundingMode.HALF_UP);

    assertEquals(
      BigDecimal.ZERO.intValue(),
      new BigDecimal("1190.0000").compareTo(amountInNOK));
  }

  /*
    Calculating monthly loan payments involves dividing the total loan amount by the number of payment periods.
    This calculation requires precise division to avoid incorrect results that may accumulate over time.
   */
  @Test
  public void loanAmortization() {
    BigDecimal loanAmount = new BigDecimal("100000.00"); // Total loan amount
    BigDecimal numberOfPayments = new BigDecimal("360"); // Payments over 30 years

    // Monthly payment before interest
    BigDecimal monthlyPayment = loanAmount.divide(numberOfPayments, 2, RoundingMode.HALF_EVEN);
    assertEquals(
      BigDecimal.ZERO.intValue(),
      new BigDecimal("277.78").compareTo(monthlyPayment));
  }

  /*
    Businesses frequently calculate profit margins by dividing the profit by the revenue.
    The result is often expressed as a percentage, which requires scaling the result appropriately.
   */
  @Test
  public void profitMarginCalculation() {
    BigDecimal revenue = new BigDecimal("12500.00"); // Total revenue
    BigDecimal profit = new BigDecimal("3500.00"); // Profit earned

    // Profit margin as a percentage
    BigDecimal profitMargin = profit.divide(revenue, 4, RoundingMode.HALF_UP)
      .multiply(new BigDecimal("100"));

    assertEquals(28, profitMargin.intValue());
  }

  /*
    In financial systems, dividing the total tax by applicable categories is a common task
      to distribute and analyze tax burdens across multiple entities.
   */
  @Test
  public void taxBreakdown() {
    BigDecimal totalTax = new BigDecimal("7500.00"); // Total tax amount
    BigDecimal numberOfEntities = new BigDecimal("3"); // Number of entities

    // Tax per entity
    BigDecimal taxPerEntity = totalTax.divide(numberOfEntities, 2, RoundingMode.UP);
    assertEquals(
      BigDecimal.ZERO.intValue(),
      new BigDecimal("2500.00").compareTo(taxPerEntity));
  }

  @Test
  public void stockSplits() {
    BigDecimal totalShares = new BigDecimal("1500000"); // Total outstanding shares
    BigDecimal splitRatio = new BigDecimal("3"); // 3-for-1 split

    // New shares per stockholder
    BigDecimal newShares = totalShares.divide(splitRatio, RoundingMode.UNNECESSARY);
    assertEquals(
      BigDecimal.ZERO.intValue(),
      new BigDecimal("500000").compareTo(newShares));
  }

  /*
    When distributing dividends among shareholders,
      dividing the total dividend pool by the number of shares outstanding
        is necessary to determine the dividend per share.
   */
  @Test
  public void dividendAllocation() {
    BigDecimal totalDividend = new BigDecimal("125000.00"); // Total dividend pool
    BigDecimal totalShares = new BigDecimal("5000"); // Total outstanding shares

    // Dividend per share
    BigDecimal dividendPerShare = totalDividend.divide(totalShares, 2, RoundingMode.HALF_DOWN);
    assertEquals(
      BigDecimal.ZERO.intValue(),
      new BigDecimal("25.00").compareTo(dividendPerShare));
  }

}
