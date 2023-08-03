##

* [Classes that represent datetime]()
* [Issues of the old original Java date-time API](#issues-of-the-old-original-java-date-time-api)
* [New Java date-time API, motivation](#how-new-java-date-time-api-solves-issues-of-the-old-api)

### Datetime classes

1. If you want to handle offsets and time zones, you must choose between `ZonedDateTime` and `OffsetDateTime`

Both work pretty much similar. 

ObjctMapper can do that, need to be tested:
2023-07-07T11:58:42Z

Disadvantage: `24.11.2020T21:45:54.964Z` date format with zero zone, cannot be parsed.



2. `java.time.LocalDateTime`.

Never use this type if you construct it from string values, that might contain offset or timezone name!

This type does not represent `local` datetime, but UTC datetime with no information about offset/timezone!

For instance your locale time has `+0200` offset.

You get a string value: `2020-11-24T21:45:54.964+0500`

You construct instance of `java.time.LocalDateTime` as:
```java
var ldt = LocalDateTime.parse(
  "2020-11-24T21:45:54.964+0500",
  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
```

You might expect the `ldt` will have:
```21 (hour) - 5(offset in input) + 2(correction to local offset) = 18 (hours of your local time)```

No, it does not work this way.

The instance of `java.time.LocalDateTime` will represent only date-time information without offset: 
``java
ldt.toString();
``
Prints:
```text
2020-11-24T21:45:54.964
```

2. 

### Issues of the old original Java date-time API

* the existing formatters aren’t thread-safe. 
This puts the burden on developers to use them in a thread-safe manner and 
to think about concurrency problems.
* `java.util.Date` represents an instant on the timeline — a wrapper around the number 
of milli-seconds since the UNIX epoch. This type represents different use cases for Date and Time very poor.
* It was an issue to work with different calendaring systems. 
The needs of users in some areas of the world, such as Japan or Thailand, don’t necessarily follow `ISO-8601`.

### How new Java date-time API solves issues of the old API

* Immutable-value classes. Classes are immutable and thread-safe
* Domain-driven design. The new API represent different use cases for `Date` and `Time` closely.
* Separation of chronologies. The new API allows people to work with different calendaring systems.