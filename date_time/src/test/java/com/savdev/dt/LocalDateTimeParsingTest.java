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

public class LocalDateTimeParsingTest {
  @Test
  @DisplayName("`24.11.2020 21:45` parsing")
  public void parseFromNoTimezoneString() {
    // cannot parse without formatter
    DateTimeParseException thrown = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> LocalDateTime.parse(DATE_TIME_STRING));

    Assertions.assertTrue(thrown.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_STRING)));

    var ldt = LocalDateTime.parse(
      DATE_TIME_STRING,
      DateTimeFormatter.ofPattern(DATE_TIME_SIMPLE_FORMAT));

    Assertions.assertEquals(
      expected(),
      ldt);
  }

  @Test
  @DisplayName("`24.11.2020T21:45:54.964Z` - zero timezone parsing")
  public void parseFromZeroTimezoneString() {
    // cannot parse without formatter
    DateTimeParseException thrown = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> LocalDateTime.parse(DATE_TIME_ZERO_TIMEZONE));

    Assertions.assertTrue(thrown.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_ZERO_TIMEZONE)));

    var ldt = LocalDateTime.parse(
      DATE_TIME_ZERO_TIMEZONE,
      DateTimeFormatter.ofPattern(DATE_TIME_ZERO_ZONE_FORMAT));

    Assertions.assertEquals(YEAR, ldt.getYear());
    Assertions.assertEquals(MONTH, ldt.getMonth().getValue());
    Assertions.assertEquals(DAY, ldt.getDayOfMonth());
    Assertions.assertEquals(HOUR, ldt.getHour());
    Assertions.assertEquals(MINUTE, ldt.getMinute());
    Assertions.assertEquals(SECONDS, ldt.getSecond());
    Assertions.assertEquals(MILLISECONDS, ldt.get(ChronoField.MILLI_OF_SECOND));
  }

  @Test
  @DisplayName("`24.11.2020T21:45:54.964+0500` with time offset parsing")
  public void parseFromTimezoneString() {
    // cannot parse without formatter
    DateTimeParseException thrown = Assertions.assertThrows(
      DateTimeParseException.class,
      () -> LocalDateTime.parse(DATE_TIME_TIMEZONE_OFFSET));

    Assertions.assertTrue(thrown.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_TIMEZONE_OFFSET)));

    //Not recommended:
    var ldtOffsetLost = LocalDateTime.parse(
      DATE_TIME_TIMEZONE_OFFSET,
      DateTimeFormatter.ofPattern(DATE_TIME_ZONE_FORMAT));

    //Zone offset gets entirely cut off, it gets ignored!
    Assertions.assertEquals(HOUR, ldtOffsetLost.getHour());

    //A better approach, convert to ZonedDateTime and then get LocalDateTime with a corrected time defined Zone
    var zdt = ZonedDateTime.parse(
        DATE_TIME_TIMEZONE_OFFSET,
        DateTimeFormatter.ofPattern(DATE_TIME_ZONE_FORMAT).withZone(BERLIN_ZONE_ID));
    //and then to LocalDateTime
    var ldt = zdt.toLocalDateTime();

    Assertions.assertNotEquals(HOUR, ldt.getHour());
    //instead of 21H for `+0500` we get 17H for Berlin (`+0100`)
    Assertions.assertEquals(
      HOUR - OFFSET + TimeUnit.HOURS.convert(zdt.getOffset().getTotalSeconds(), TimeUnit.SECONDS),
      ldt.getHour());
  }

  private LocalDateTime expected() {
    return LocalDateTime.parse(DATE_TIME_STRING, DateTimeFormatter.ofPattern(DATE_TIME_SIMPLE_FORMAT));
  }
}
