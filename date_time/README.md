##

* [Classes that represent datetime](#datetime-classes)
* [Issues of the old original Java date-time API](#issues-of-the-old-original-java-date-time-api)
* [New Java date-time API, motivation](#how-new-java-date-time-api-solves-issues-of-the-old-api)

### Datetime classes

These classes are:
* `OffsetDateTime`
* `ZonedDateTime`
* `LocalDateTime`

##### `OffsetDateTime` vs `ZonedDateTime`

When external system exchange datetime information they must explicitly specify one of the following:
* time offset (existing offset or zero-zone)
* time zone

Keep in mind that a time zone can have different time offset values.
For the same ZoneId like `Europe/Berlin`, there is one offset for summer and a different offset for winter.

You can easily convert `OffsetDateTime` to `ZonedDateTime` and back, 
so both classes can be used for communication between systems.

But if you want to display in one system datetime information for a human, provided from a system with another timezone,
then `ZonedDateTime` is a preferable type. Based on it, I would say `ZonedDateTime` is preferable.

`ZonedDateTime` class usage:
* [`ZonedDateTime` creation](src/test/java/com/savdev/dt/ZonedDateTimeCreationTest.java)
* [`ZonedDateTime` parsing](src/test/java/com/savdev/dt/ZonedDateTimeParsingTest.java)
* [`ZonedDateTime` parsing by jackson (via `ObjectMapper`)](src/test/java/com/savdev/dt/ZonedDateTimeObjectMapperParsingTest.java)

`OffsetDateTime` class usage:
* [`OffsetDateTime` creation](src/test/java/com/savdev/dt/OffsetDateTimeCreationTest.java)
* [`OffsetDateTime` parsing](src/test/java/com/savdev/dt/OffsetDateTimeParsingTest.java)


##### `LocalDateTime` (don't use it)

It is **not local** datetime, it just class that represents only date and time.
It ignores any information about time zone or offset.
Using this class is very error-prone option and should be avoided.
Only for certain performance issues, it might be useful.

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