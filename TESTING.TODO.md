These are excellent, rigorous questions. You are absolutely thinking like a senior code reviewer right now. Let’s break down the technical reasoning behind both of these choices.

1. AssertJ (assertThat) vs. Standard JUnit (assertEquals)
   It might seem like adding AssertJ just makes testing more complicated by introducing another library, but it is actually the industry standard in modern Spring Boot applications (which is why Spring Boot automatically includes it in the spring-boot-starter-test dependency).

Here is exactly why developers prefer AssertJ over standard JUnit assertions:

A. The "Expected vs. Actual" Confusion
In standard JUnit, the signature is assertEquals(expected, actual).
Developers constantly mix up the order. If you write assertEquals(actual, expected) and the test fails, JUnit will print a very confusing error message saying: "Expected [ActualValue] but was [ExpectedValue]".

AssertJ forces a left-to-right, English-like reading order:
assertThat(actual).isEqualTo(expected);
You can never mix them up.

B. IDE Auto-Completion (The Biggest Benefit)
If you use standard JUnit, you have to memorize the assertion methods (assertTrue, assertNull, assertIterableEquals).

With AssertJ, you just type assertThat(myResult). and press Ctrl+Space. Because the IDE knows the exact Type of myResult, it will instantly suggest all the valid assertions for that specific object.

If it is a List, it suggests .hasSize(), .contains(), .isEmpty().

If it is a String, it suggests .startsWith(), .containsIgnoringCase().

C. Rich Error Messages
If comparing two lists in standard JUnit fails, it just says they are not equal. AssertJ will actually analyze the collections and tell you exactly what is missing:
"Expecting ArrayList to contain [Job A, Job B] but could not find [Job B]."