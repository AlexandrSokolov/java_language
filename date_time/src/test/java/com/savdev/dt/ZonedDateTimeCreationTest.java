package com.savdev.dt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.concurrent.TimeUnit;

import static com.savdev.dt.DateTimeFormatters.DATE_TIME_SIMPLE_FORMAT;
import static com.savdev.dt.DateTimeFormatters.DATE_TIME_ZERO_ZONE_FORMAT;
import static com.savdev.dt.DateTimeFormatters.DATE_TIME_ZONE_FORMAT;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_STRING;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_TIMEZONE_OFFSET;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_ZERO_TIMEZONE;
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
  public void createFromNoTimezoneStringThrows() {
    DateTimeParseException thrown = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> ZonedDateTime.parse(
          DATE_TIME_STRING,
          DateTimeFormatter.ofPattern(DATE_TIME_SIMPLE_FORMAT)),
      "Datetime value with no offset must throw an exception, but it didn't"
    );

    Assertions.assertTrue(thrown.getMessage().contains("Text '24.11.2020 21:45' could not be parsed"));
    Assertions.assertTrue(thrown.getMessage().contains("Unable to obtain ZonedDateTime"));
  }

  @Test
  public void createFromZeroTimezoneStringThrows() {
    DateTimeParseException thrown = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> ZonedDateTime.parse(
        DATE_TIME_ZERO_TIMEZONE,
        DateTimeFormatter.ofPattern(DATE_TIME_ZERO_ZONE_FORMAT)),
      "Datetime value with no offset must throw an exception, but it didn't"
    );

    Assertions.assertTrue(thrown.getMessage().contains("Text '24.11.2020T21:45:54.964Z' could not be parsed"));
    Assertions.assertTrue(thrown.getMessage().contains("Unable to obtain ZonedDateTime"));
  }

  @Test
  public void createFromTimezoneString() {
    var zdt = ZonedDateTime.parse(
      DATE_TIME_TIMEZONE_OFFSET,
      DateTimeFormatter.ofPattern(DATE_TIME_ZONE_FORMAT));

    Assertions.assertEquals(HOUR, zdt.getHour());

    Instant instant = zdt.toInstant(); //2020-11-24T21:45:54.964-05:00 -> 2020-11-25T02:45:54.964Z"
    ZoneId systemZone = ZoneId.systemDefault(); //ZoneId.of( "Europe/Berlin" );
    ZoneOffset currentOffsetForMyZone = systemZone.getRules().getOffset(instant);
    long systemHoursOffset = TimeUnit.HOURS.convert(currentOffsetForMyZone.getTotalSeconds(), TimeUnit.SECONDS);

    /* `+0500` offset */
    long zdtHoursOffset = TimeUnit.HOURS.convert(zdt.getOffset().get(ChronoField.OFFSET_SECONDS), TimeUnit.SECONDS);

    Assertions.assertEquals(
      (HOUR - zdtHoursOffset + systemHoursOffset) % 24,
      zdt.withZoneSameInstant(ZoneId.systemDefault()).getHour());
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
