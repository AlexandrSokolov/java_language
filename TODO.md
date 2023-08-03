```java
Optional.ofNullable(gc.getModifiedAt())
          .orElse(gc.getSentAt())
          .toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime()
```


### todo refactor module - extract and move