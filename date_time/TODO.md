###

The problem is that there is a difference between what a ZoneId is and a ZoneOffset is. 
To create a OffsetDateTime, you need an zone offset. 
But there is no one-to-one mapping between a ZoneId and a ZoneOffset because it actually 
depends on the current daylight saving time. 
For the same ZoneId like "Europe/Berlin", there is one offset for summer and a different offset for winter.

So use ZonedDateTime and then convert it to OffsetDateTime

### todo with other formats:

//    TODO how to parse then?
//
//      var o1 = OffsetDateTime.parse("2023-07-07T11:58:42Z");
//    var s1 = o1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
//    var o2 = OffsetDateTime.parse("2023-07-07T11:58:42Z", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));


### testing dates on

csCommonsReviewManager/src/main/java/com/bm/cs/commons/rm/dto/PdfTronAnnotationExtendedDto.java

###

ZoneId.of(ZoneOffset.of("+02:00").getId());

###