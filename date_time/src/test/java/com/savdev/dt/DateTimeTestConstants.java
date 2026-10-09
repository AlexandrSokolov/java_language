package com.savdev.dt;

public interface DateTimeTestConstants {

  int DAY = 24;
  int MONTH = 11;
  int YEAR = 2020;
  Integer MILLISECONDS = 964;
  Integer SECONDS = 54;
  Integer MINUTE = 45;
  Integer HOUR = 21;

  Long OFFSET = 5L;

  // 24.11.2020 21:45
  String DATE_TIME_STRING = String.format("%d.%d.%d %d:%d",
    DAY,  MONTH, YEAR, HOUR, MINUTE);

  // 24.11.2020T21:45:54.964Z
  String DATE_TIME_ZERO_TIMEZONE = String.format("%d.%d.%dT%d:%d:%d.%dZ",
    DAY,  MONTH, YEAR, HOUR, MINUTE, SECONDS, MILLISECONDS);

  // 24.11.2020T21:45:54.964+0500
  String DATE_TIME_TIMEZONE_OFFSET = String.format("%d.%d.%dT%d:%d:%d.%d+0%d00",
    DAY,  MONTH, YEAR, HOUR, MINUTE, SECONDS, MILLISECONDS, OFFSET);

  String DATE_TIME_TIMEZONE_OFFSET_JACKSON = String.format("%d.%d.%dT%d:%d:%d.%d+0%d:00",
    DAY,  MONTH, YEAR, HOUR, MINUTE, SECONDS, MILLISECONDS, OFFSET);
}
