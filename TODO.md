### the classic Request DTO vs Response DTO problem

public class UserDto {

    //common for both “new” and “existing” users

    @Data
    public static class UserBase {
        private String name;
        private int age;
    }

   // NewUser is for objects not persisted yet.

    @Data
    public static class NewUser extends UserBase {
        // nothing extra for "new"
    }

    // User represents a persisted domain object, which has an ID.
    @Data
    public static class User extends UserBase {
        private int id;
    }
}

### Builder with optional attribute

    public <A> RestClientBuilder<T> ifPresent(
…
}

    public RestClientBuilder<T> withAuth(final ClientRequestFilter authFilter) {
      clientRequestFilters.add(authFilter);
      return this;
    }

    restClient = RestClient.builder(this.serverUrl, getParameterClass())
      .ifPresent(authFilter, RestClient.RestClientBuilder::withAuth)
      .build();

https://github.com/AlexandrSokolov/rest/blob/master/spring/multiple_rest_clients/rest_client/src/main/java/com/savdev/rest/commons/RestClient.java
https://github.com/AlexandrSokolov/rest/blob/master/spring/multiple_rest_clients/rest_client/src/main/java/com/savdev/rest/commons/BaseRestClientService.java


### stream api - grouping (into Map, into statistics report, nested grouping (valid/failed))
### stream api - collecting (to Map, test with `null` values and not unique keys)
### stream api - async handling

### refactore old code from `refactore` module
