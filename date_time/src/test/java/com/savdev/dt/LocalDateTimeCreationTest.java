package com.savdev.dt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.savdev.dt.DateTimeFormatters.DATE_TIME_SIMPLE_FORMAT;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_STRING;
import static com.savdev.dt.DateTimeTestConstants.DAY;
import static com.savdev.dt.DateTimeTestConstants.HOUR;
import static com.savdev.dt.DateTimeTestConstants.MINUTE;
import static com.savdev.dt.DateTimeTestConstants.MONTH;
import static com.savdev.dt.DateTimeTestConstants.YEAR;

public class LocalDateTimeCreationTest {

  @Test
  public void now() {
    LocalDateTime now = LocalDateTime.now();
    Assertions.assertNotNull(now);
  }

  @Test
  public void fromDateTimeAttributes() {
    var ldt = LocalDateTime.of(YEAR, MONTH, DAY, HOUR, MINUTE);
    Assertions.assertEquals(
      expected(),
      ldt);
  }

  @Test
  public void fromLocalDateAndLocalTime() {
    var ldt = LocalDateTime.of(
      LocalDate.of(YEAR, MONTH, DAY),
      LocalTime.of(HOUR, MINUTE));
    Assertions.assertEquals(
      expected(),
      ldt);
  }

  @Test
  public void fromUtilDate() throws ParseException {
    var utilDate = new SimpleDateFormat(DATE_TIME_SIMPLE_FORMAT).parse(DATE_TIME_STRING);
    var ldt = LocalDateTime.ofInstant(
      utilDate.toInstant(),
      ZoneId.systemDefault());
    Assertions.assertEquals(
      expected(),
      ldt);
  }

  private LocalDateTime expected() {
    return LocalDateTime.parse(DATE_TIME_STRING, DateTimeFormatter.ofPattern(DATE_TIME_SIMPLE_FORMAT));
  }
}
