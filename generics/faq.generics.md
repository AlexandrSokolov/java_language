
### The Get and Put Principle

It may be good practice to insert wildcards whenever possible, but how do you decide which wildcard to use? 
Where should you use `extends`, where should you use `super`, and where is it inappropriate to use a wildcard at all?

Fortunately, a simple principle determines which is appropriate:
**Get and Put Principle**: 
- use an `extends` wildcard when you only get values out of a structure,
- use a `super` wildcard when you only put values into a structure, and 
- don’t use a wildcard when you both get and put.

_In Effective Java_, Joshua Bloch (2017, item 31) gives this principle the mnemonic PECS:
(producer-extends, consumer-super). 

Example: `public static <T> void copy(List<? super T> dst, List<? extends T> src)`
The method gets values out of the source `src`, so it is declared with an `extends` wildcard, 
and it puts values into the destination `dst`, so it is declared with a `super` wildcard.

### Bounded or Unbounded?

todo