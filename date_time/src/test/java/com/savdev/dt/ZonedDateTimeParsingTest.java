package com.savdev.dt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.concurrent.TimeUnit;

import static com.savdev.dt.DateTimeFormatters.BERLIN_ZONE_ID;
import static com.savdev.dt.DateTimeFormatters.DATE_TIME_SIMPLE_FORMAT;
import static com.savdev.dt.DateTimeFormatters.DATE_TIME_ZERO_ZONE_FORMAT;
import static com.savdev.dt.DateTimeFormatters.DATE_TIME_ZONE_FORMAT;
import static com.savdev.dt.DateTimeFormatters.OBJECT_MAPPER;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_STRING;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_TIMEZONE_OFFSET;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_ZERO_TIMEZONE;
import static com.savdev.dt.DateTimeTestConstants.DAY;
import static com.savdev.dt.DateTimeTestConstants.HOUR;
import static com.savdev.dt.DateTimeTestConstants.MILLISECONDS;
import static com.savdev.dt.DateTimeTestConstants.MINUTE;
import static com.savdev.dt.DateTimeTestConstants.MONTH;
import static com.savdev.dt.DateTimeTestConstants.OFFSET;
import static com.savdev.dt.DateTimeTestConstants.SECONDS;
import static com.savdev.dt.DateTimeTestConstants.YEAR;

public class ZonedDateTimeParsingTest {
  @Test
  @DisplayName("`24.11.2020 21:45` parsing")
  public void parseFromNoTimezoneString() {
    // cannot parse neither without formatter
    DateTimeParseException thrownWithoutFormatter = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> ZonedDateTime.parse(DATE_TIME_STRING));

    Assertions.assertTrue(thrownWithoutFormatter.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_STRING)));

    // no with formatter
    DateTimeParseException thrownWithFormatter = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> ZonedDateTime.parse(
        DATE_TIME_STRING,
        DateTimeFormatter.ofPattern(DATE_TIME_SIMPLE_FORMAT)));

    Assertions.assertTrue(thrownWithFormatter.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_STRING)));
    Assertions.assertTrue(thrownWithFormatter.getMessage().contains("Unable to obtain ZonedDateTime"));

    //covert to LocalDateTime, then to ZonedDateTime
    var zdt = LocalDateTime.parse(
        DATE_TIME_STRING,
        DateTimeFormatter.ofPattern(DATE_TIME_SIMPLE_FORMAT))
      .atZone(BERLIN_ZONE_ID);

    Assertions.assertEquals(YEAR, zdt.getYear());
    Assertions.assertEquals(MONTH, zdt.getMonth().getValue());
    Assertions.assertEquals(DAY, zdt.getDayOfMonth());
    Assertions.assertEquals(HOUR, zdt.getHour());
    Assertions.assertEquals(MINUTE, zdt.getMinute());
  }

  @Test
  @DisplayName("`24.11.2020T21:45:54.964Z` - zero timezone parsing")
  public void parseFromZeroTimezoneString() {
    // cannot parse neither without formatter
    DateTimeParseException thrown = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> ZonedDateTime.parse(DATE_TIME_ZERO_TIMEZONE));

    Assertions.assertTrue(thrown.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_ZERO_TIMEZONE)));

    // no with formatter (without zone id)
    DateTimeParseException thrownWithFormatter = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> ZonedDateTime.parse(
        DATE_TIME_ZERO_TIMEZONE,
        DateTimeFormatter.ofPattern(DATE_TIME_ZERO_ZONE_FORMAT))
    );

    Assertions.assertTrue(thrownWithFormatter.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_ZERO_TIMEZONE)));
    Assertions.assertTrue(thrownWithFormatter.getMessage().contains("Unable to obtain ZonedDateTime"));

    // Parse as ZonedDateTime and covert to ZonedDateTime
    var zdt = ZonedDateTime.parse(
      DATE_TIME_ZERO_TIMEZONE,
      DateTimeFormatter.ofPattern(DATE_TIME_ZERO_ZONE_FORMAT)
        .withZone(BERLIN_ZONE_ID));

    Assertions.assertEquals(YEAR, zdt.getYear());
    Assertions.assertEquals(MONTH, zdt.getMonth().getValue());
    Assertions.assertEquals(DAY, zdt.getDayOfMonth());
    Assertions.assertEquals(HOUR, zdt.getHour());
    Assertions.assertEquals(MINUTE, zdt.getMinute());
    Assertions.assertEquals(SECONDS, zdt.getSecond());
    Assertions.assertEquals(MILLISECONDS, zdt.get(ChronoField.MILLI_OF_SECOND));
  }

  @Test
  @DisplayName("`24.11.2020T21:45:54.964+0500` with time offset parsing")
  public void parseFromTimezoneString() {
    // cannot parse without formatter
    DateTimeParseException thrown = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> ZonedDateTime.parse(DATE_TIME_TIMEZONE_OFFSET));

    Assertions.assertTrue(thrown.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_TIMEZONE_OFFSET)));

    var zdt = ZonedDateTime.parse(
      DATE_TIME_TIMEZONE_OFFSET,
      DateTimeFormatter.ofPattern(DATE_TIME_ZONE_FORMAT)
        .withZone(BERLIN_ZONE_ID));

    Assertions.assertEquals(YEAR, zdt.getYear());
    Assertions.assertEquals(MONTH, zdt.getMonth().getValue());
    Assertions.assertEquals(DAY, zdt.getDayOfMonth());
    //instead of 21H for `+0500` we get 17H for Berlin (`+0100`)
    Assertions.assertEquals(
      HOUR - OFFSET + TimeUnit.HOURS.convert(zdt.getOffset().getTotalSeconds(), TimeUnit.SECONDS),
      zdt.getHour());
    Assertions.assertEquals(MINUTE, zdt.getMinute());
    Assertions.assertEquals(SECONDS, zdt.getSecond());
    Assertions.assertEquals(MILLISECONDS, zdt.get(ChronoField.MILLI_OF_SECOND));
    Assertions.assertNotEquals(OFFSET,
      TimeUnit.HOURS.convert(zdt.getOffset().getTotalSeconds(), TimeUnit.SECONDS));
  }
}
