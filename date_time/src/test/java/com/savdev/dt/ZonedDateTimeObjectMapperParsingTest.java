package com.savdev.dt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static com.savdev.dt.DateTimeFormatters.OBJECT_MAPPER;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_STRING;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_TIMEZONE_OFFSET;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_TIMEZONE_OFFSET_JACKSON;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_ZERO_TIMEZONE;

public class ZonedDateTimeObjectMapperParsingTest {

  @Test
  @DisplayName("`24.11.2020 21:45` parsing")
  public void parseFromNoTimezoneString() throws JsonProcessingException {
    InvalidFormatException thrown = Assertions.assertThrows(
      InvalidFormatException.class,
      () -> OBJECT_MAPPER.readValue("\"" + DATE_TIME_STRING + "\"", ZonedDateTime.class));
    Assertions.assertTrue(thrown.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_STRING)));
  }

  @Test
  @DisplayName("`24.11.2020T21:45:54.964Z` - zero timezone parsing")
  public void parseFromZeroTimezoneString() throws JsonProcessingException {
    InvalidFormatException thrown = Assertions.assertThrows(
      InvalidFormatException.class,
      () -> OBJECT_MAPPER.readValue("\"" + DATE_TIME_ZERO_TIMEZONE + "\"", ZonedDateTime.class));
    Assertions.assertTrue(thrown.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_ZERO_TIMEZONE)));
  }

  @Test
  @DisplayName("`24.11.2020T21:45:54.964+0500` with time offset parsing")
  public void parseFromTimezoneString() throws JsonProcessingException {
    InvalidFormatException thrown = Assertions.assertThrows(
      InvalidFormatException.class,
      () -> OBJECT_MAPPER.readValue("\"" + DATE_TIME_TIMEZONE_OFFSET + "\"", ZonedDateTime.class));
    Assertions.assertTrue(thrown.getMessage().contains(
      String.format("Text '%s' could not be parsed", DATE_TIME_TIMEZONE_OFFSET_JACKSON)));
  }

  @Test
  @DisplayName("Standard format parsing")
  public void parseInStandardFormat() throws JsonProcessingException {
    Assertions.assertNotNull(OBJECT_MAPPER.readValue("\"2023-07-07T11:58:42Z\"", ZonedDateTime.class));
    Assertions.assertNotNull(OBJECT_MAPPER.readValue("\"2023-07-07T11:58:42+0500\"", ZonedDateTime.class));
    Assertions.assertNotNull(OBJECT_MAPPER.readValue("\"2023-07-07T11:58:42.954+05:00\"", ZonedDateTime.class));
  }
}
