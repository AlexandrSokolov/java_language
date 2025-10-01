package com.savdev.statements;

import java.time.Month;

public class SwitchDemo {

  /**
   * Java `switch` is an alternative to multiple `if/else` blocks
   *
   * @param animal
   * @return
   */
  public String classicSwitch(String animal) {
    String result;
    switch (animal) {
      case "DOG":
        result = "domestic animal";
        break; //exit switch
      case "CAT":
        result = "domestic animal";
        break; //exit switch
      case "TIGER":
        result = "wild animal";
        break; //exit switch
      default:
        result = "unknown animal";
        break; //exit switch, the last block it is not required, but makes the code less error-prone
    }
    return result;
  }

  /**
   * In the example bellow the same result we get and the same code block is used for "DOG" and "CAT"
   *  Just do not "break" the switch case.
   *
   * @param animal
   * @return
   */
  public String sameCode4MultipleCases(String animal) {
    String result;
    switch (animal) {
      case "DOG":
      case "CAT":
        result = "domestic animal";
        break;
      case "TIGER":
        result = "wild animal";
        break;
      default:
        result = "unknown animal";
        break;
    }
    return result;
  }


  /**
   * new switch syntax:
   *  - multiple cases can be combined with better readability
   *  - you pass an expression that returns value
   * @param month
   * @return
   */
  public int jdk14SwitchStatements(Month month) {
    return switch(month) {
      case JANUARY, JUNE, JULY -> 3;
      case FEBRUARY, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER -> 1;
      case MARCH, MAY, APRIL, AUGUST -> 2;
      default -> 0;
    };
  }

  /**
   * If you return a constant, you could use `yield` instead of `break`, then it looks like the old switch
   * @param day
   * @return
   */
  public String jdk14SwitchWithYields(String day) {
    return  switch (day) {
      case "Monday":
        yield  "Weekday";
      case "Tuesday":
        yield "Weekday";
      case "Wednesday":
        yield "Weekday";
      case "Thursday":
        yield "Weekday";
      case "Friday":
        yield "Weekday";
      case "Saturday":
        yield "Weekend";
      case "Sunday":
        yield "Weekend";
      default:
        yield "Unknown";
    };
  }

  static class Employee {
    String dept;

    public String getDept() {
      return dept;
    }
  }
  /**
   * You can match patterns in a case label.
   *
   * For example:
   * `case Integer i- > "It is an integer";`
   * The passed object is checked for the type “Integer” and then assigned to the variable “i” if it is an integer.
   *
   * You could write patterns with `when`:
   *  `case Employee employee when employee.getDept().equals("IT") -> "IT Employee";`
   *
   *  And use `null` in case blocks:
   *  `case null -> "It is a null object";`
   *
   * @param obj
   * @return
   */
  public String jdk17Switch_PatternMatching_CheckingDataType_GuardedPatterns(Object obj) {
    return switch (obj) {
      case Integer i -> "It is an integer";
      case String s -> "It is a string";
      case Employee employee when employee.getDept().equals("IT") -> "IT Employee";
      case null -> "It is a null object";
      default -> "It is none of the known data types";
    };
  }
}
