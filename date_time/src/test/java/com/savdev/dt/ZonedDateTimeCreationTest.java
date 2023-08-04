package com.savdev.dt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.savdev.dt.DateTimeFormatters.DATE_TIME_SIMPLE_FORMAT;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_STRING;
import static com.savdev.dt.DateTimeTestConstants.DAY;
import static com.savdev.dt.DateTimeTestConstants.HOUR;
import static com.savdev.dt.DateTimeTestConstants.MINUTE;
import static com.savdev.dt.DateTimeTestConstants.MONTH;
import static com.savdev.dt.DateTimeTestConstants.SECONDS;
import static com.savdev.dt.DateTimeTestConstants.YEAR;

public class ZonedDateTimeCreationTest {

  @Test
  public void now() {
    ZonedDateTime now = ZonedDateTime.now();
    Assertions.assertNotNull(now);
  }

  @Test
  public void fromDateTimeAttributes() {
    var zdt = ZonedDateTime.of(YEAR, MONTH, DAY, HOUR, MINUTE, SECONDS, 0,
      ZoneOffset.of("+02:00"));

    Assertions.assertEquals(YEAR, zdt.getYear());
    Assertions.assertEquals(MONTH, zdt.getMonth().getValue());
    Assertions.assertEquals(DAY, zdt.getDayOfMonth());
    Assertions.assertEquals(HOUR, zdt.getHour());
    Assertions.assertEquals(MINUTE, zdt.getMinute());
    Assertions.assertEquals(SECONDS, zdt.getSecond());
  }

  @Test
  public void fromLocalDateAndLocalTime() {
    var zdt = ZonedDateTime.of(
      LocalDate.of(YEAR, MONTH, DAY),
      LocalTime.of(HOUR, MINUTE),
      ZoneOffset.of("+02:00"));

    Assertions.assertEquals(YEAR, zdt.getYear());
    Assertions.assertEquals(MONTH, zdt.getMonth().getValue());
    Assertions.assertEquals(DAY, zdt.getDayOfMonth());
    Assertions.assertEquals(HOUR, zdt.getHour());
    Assertions.assertEquals(MINUTE, zdt.getMinute());
  }

  @Test
  public void fromUtilDate() throws ParseException {
    var utilDate = new SimpleDateFormat(DATE_TIME_SIMPLE_FORMAT).parse(DATE_TIME_STRING);

    var zdt = ZonedDateTime.ofInstant(
      utilDate.toInstant(),
      ZoneId.systemDefault());

    Assertions.assertEquals(YEAR, zdt.getYear());
    Assertions.assertEquals(MONTH, zdt.getMonth().getValue());
    Assertions.assertEquals(DAY, zdt.getDayOfMonth());
    Assertions.assertEquals(HOUR, zdt.getHour());
    Assertions.assertEquals(MINUTE, zdt.getMinute());
  }
}
