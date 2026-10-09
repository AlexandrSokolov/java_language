package com.savdev.dt;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.ZoneId;

public interface DateTimeFormatters {

  String DATE_TIME_SIMPLE_FORMAT = "dd.MM.yyyy HH:mm";
  String DATE_TIME_ZERO_ZONE_FORMAT = "dd.MM.yyyy'T'HH:mm:ss.SSS'Z'"; // 24.11.2020T21:45:54.964Z
  String DATE_TIME_ZONE_FORMAT = "dd.MM.yyyy'T'HH:mm:ss.SSSZ"; // 24.11.2020T21:45:54.964+0500

  ZoneId BERLIN_ZONE_ID = ZoneId.of("Europe/Berlin");

  ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(new Jdk8Module())
    .registerModule(new JavaTimeModule());
}
