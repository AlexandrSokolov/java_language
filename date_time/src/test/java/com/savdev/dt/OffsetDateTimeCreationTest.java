package com.savdev.dt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static com.savdev.dt.DateTimeFormatters.DATE_TIME_SIMPLE_FORMAT;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_STRING;
import static com.savdev.dt.DateTimeTestConstants.DAY;
import static com.savdev.dt.DateTimeTestConstants.HOUR;
import static com.savdev.dt.DateTimeTestConstants.MINUTE;
import static com.savdev.dt.DateTimeTestConstants.MONTH;
import static com.savdev.dt.DateTimeTestConstants.SECONDS;
import static com.savdev.dt.DateTimeTestConstants.YEAR;

public class OffsetDateTimeCreationTest {

  @Test
  public void now() {
    OffsetDateTime now = OffsetDateTime.now();
    Assertions.assertNotNull(now);
  }

  @Test
  public void fromDateTimeAttributes() {
    var odt = OffsetDateTime.of(YEAR, MONTH, DAY, HOUR, MINUTE, SECONDS, 0,
      ZoneOffset.of("+02:00"));

    Assertions.assertEquals(YEAR, odt.getYear());
    Assertions.assertEquals(MONTH, odt.getMonth().getValue());
    Assertions.assertEquals(DAY, odt.getDayOfMonth());
    Assertions.assertEquals(HOUR, odt.getHour());
    Assertions.assertEquals(MINUTE, odt.getMinute());
    Assertions.assertEquals(SECONDS, odt.getSecond());
  }

  @Test
  public void fromLocalDateAndLocalTime() {
    var odt = OffsetDateTime.of(
      LocalDate.of(YEAR, MONTH, DAY),
      LocalTime.of(HOUR, MINUTE),
      ZoneOffset.of("+02:00"));

    Assertions.assertEquals(YEAR, odt.getYear());
    Assertions.assertEquals(MONTH, odt.getMonth().getValue());
    Assertions.assertEquals(DAY, odt.getDayOfMonth());
    Assertions.assertEquals(HOUR, odt.getHour());
    Assertions.assertEquals(MINUTE, odt.getMinute());
  }

  @Test
  public void fromUtilDate() throws ParseException {
    var utilDate = new SimpleDateFormat(DATE_TIME_SIMPLE_FORMAT).parse(DATE_TIME_STRING);

    //convert to Instant first
    var odt = OffsetDateTime.ofInstant(
      utilDate.toInstant(),
      ZoneId.systemDefault());

    Assertions.assertEquals(YEAR, odt.getYear());
    Assertions.assertEquals(MONTH, odt.getMonth().getValue());
    Assertions.assertEquals(DAY, odt.getDayOfMonth());
    Assertions.assertEquals(HOUR, odt.getHour());
    Assertions.assertEquals(MINUTE, odt.getMinute());
  }
}
