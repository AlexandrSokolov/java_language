package com.savdev.dt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.concurrent.TimeUnit;

import static com.savdev.dt.DateTimeFormatters.BERLIN_ZONE_ID;
import static com.savdev.dt.DateTimeFormatters.DATE_TIME_SIMPLE_FORMAT;
import static com.savdev.dt.DateTimeFormatters.DATE_TIME_ZERO_ZONE_FORMAT;
import static com.savdev.dt.DateTimeFormatters.DATE_TIME_ZONE_FORMAT;
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

public class OffsetDateTimeParsingTest {
  @Test
  @DisplayName("`24.11.2020 21:45` parsing")
  public void parseFromNoTimezoneString() {
    // cannot parse neither without formatter
    DateTimeParseException thrownWithoutFormatter = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> OffsetDateTime.parse(DATE_TIME_STRING));

    Assertions.assertTrue(thrownWithoutFormatter.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_STRING)));

    // cannot parse no with formatter
    DateTimeParseException thrownWithFormatter = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> OffsetDateTime.parse(
        DATE_TIME_STRING,
        DateTimeFormatter.ofPattern(DATE_TIME_SIMPLE_FORMAT)));

    Assertions.assertTrue(thrownWithFormatter.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_STRING)));
    Assertions.assertTrue(thrownWithFormatter.getMessage().contains("Unable to obtain OffsetDateTime"));

    //covert to LocalDateTime, then to ZonedDateTime and only then to OffsetDateTime
    var odt = LocalDateTime.parse(
        DATE_TIME_STRING,
        DateTimeFormatter.ofPattern(DATE_TIME_SIMPLE_FORMAT))
      .atZone(BERLIN_ZONE_ID)
      .toOffsetDateTime();

    Assertions.assertEquals(YEAR, odt.getYear());
    Assertions.assertEquals(MONTH, odt.getMonth().getValue());
    Assertions.assertEquals(DAY, odt.getDayOfMonth());
    Assertions.assertEquals(HOUR, odt.getHour());
    Assertions.assertEquals(MINUTE, odt.getMinute());
  }

  @Test
  @DisplayName("`24.11.2020T21:45:54.964Z` - zero timezone parsing")
  public void parseFromZeroTimezoneString() {
    // cannot parse neither without formatter
    DateTimeParseException thrown = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> OffsetDateTime.parse(DATE_TIME_ZERO_TIMEZONE));

    Assertions.assertTrue(thrown.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_ZERO_TIMEZONE)));

    // no with formatter
    DateTimeParseException thrownWithFormatter = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> OffsetDateTime.parse(
        DATE_TIME_ZERO_TIMEZONE,
        DateTimeFormatter.ofPattern(DATE_TIME_ZERO_ZONE_FORMAT))
    );

    Assertions.assertTrue(thrownWithFormatter.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_ZERO_TIMEZONE)));
    Assertions.assertTrue(thrownWithFormatter.getMessage().contains("Unable to obtain OffsetDateTime"));

    // Parse as ZonedDateTime and covert to OffsetDateTime
    var odt = ZonedDateTime.parse(
      DATE_TIME_ZERO_TIMEZONE,
      DateTimeFormatter.ofPattern(DATE_TIME_ZERO_ZONE_FORMAT)
        .withZone(BERLIN_ZONE_ID)) //zone id is required cause of `ZonedDateTime` using
      .toOffsetDateTime();

    Assertions.assertEquals(YEAR, odt.getYear());
    Assertions.assertEquals(MONTH, odt.getMonth().getValue());
    Assertions.assertEquals(DAY, odt.getDayOfMonth());
    Assertions.assertEquals(HOUR, odt.getHour());
    Assertions.assertEquals(MINUTE, odt.getMinute());
    Assertions.assertEquals(SECONDS, odt.getSecond());
    Assertions.assertEquals(MILLISECONDS, odt.get(ChronoField.MILLI_OF_SECOND));
  }

  @Test
  @DisplayName("`24.11.2020T21:45:54.964+0500` with time offset parsing")
  public void parseFromTimezoneString() {
    // cannot parse without formatter
    DateTimeParseException thrown = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> OffsetDateTime.parse(DATE_TIME_TIMEZONE_OFFSET));

    Assertions.assertTrue(thrown.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_TIMEZONE_OFFSET)));

    var odt = OffsetDateTime.parse(
      DATE_TIME_TIMEZONE_OFFSET,
      DateTimeFormatter.ofPattern(DATE_TIME_ZONE_FORMAT));

    Assertions.assertEquals(YEAR, odt.getYear());
    Assertions.assertEquals(MONTH, odt.getMonth().getValue());
    Assertions.assertEquals(DAY, odt.getDayOfMonth());
    Assertions.assertEquals(HOUR, odt.getHour());
    Assertions.assertEquals(MINUTE, odt.getMinute());
    Assertions.assertEquals(SECONDS, odt.getSecond());
    Assertions.assertEquals(MILLISECONDS, odt.get(ChronoField.MILLI_OF_SECOND));
    Assertions.assertEquals(OFFSET,
      TimeUnit.HOURS.convert(odt.getOffset().getTotalSeconds(), TimeUnit.SECONDS));
  }
}
