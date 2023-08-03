package com.savdev.dt;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static com.savdev.dt.DateTimeFormatters.OBJECT_MAPPER;
import static com.savdev.dt.DateTimeTestConstants.DATE_TIME_TIMEZONE_OFFSET;

public class ObjectMapperParsingTest {

  @Test
  public void t() throws JsonProcessingException {
    var omLdt2 = OBJECT_MAPPER.readValue("\"2023-07-07T11:58:42Z\"", ZonedDateTime.class);
    var omLdt3 = OBJECT_MAPPER.readValue("\"2023-07-07T11:58:42+0500\"", ZonedDateTime.class);
    var omLdt4 = OBJECT_MAPPER.readValue("\"2023-07-07T11:58:42.954+0500\"", ZonedDateTime.class);
    var omLdt = OBJECT_MAPPER.readValue("\"" + DATE_TIME_TIMEZONE_OFFSET + "\"", ZonedDateTime.class);
    System.out.println();
  }
}
