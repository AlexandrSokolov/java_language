## Guidance for Using the Java Collections Framework


### Domain Models used in Java Collections Framework

- In the Collections Framework Anemic Domain Models anti-pattern appears as nameless collection structures directly 
  containing other collections, sometimes deeply nested, with no provision for custom behavior. 
  The object design failure is fundamentally the same in each case.
- Its core classes (like ArrayList, HashMap, HashSet) are primarily data containers without any domain-specific behavior. 
  They expose getters, setters, and structural operations, but business logic is completely external.

All domain logic (e.g., validation, calculations) is implemented outside, usually in service or utility classes.

### Example of Anemic Domain Model with Java Collections Framework

```java
List<Order> orders = new ArrayList<>();
orders.add(new Order(3, 100.0));
orders.add(new Order(2, 50.0));

// Business logic outside the domain
double totalRevenue = orders.stream()
    .mapToDouble(order -> order.getQuantity() * order.getPrice())
    .sum();

System.out.println("Total Revenue: " + totalRevenue);
```
List is anemic because they only store data. Domain logic (sum calculation) is external.

### Recommendation and solution for Anemic Domain Model with Java Collections Framework

**Avoid Anemic Domain Models**

Instead of using raw collections, wrap them in domain-specific classes:
```java
public class OrderCollection {
  private final List<Order> orders = new ArrayList<>();

  public void addOrder(Order order) {
    if (order.getQuantity() <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    orders.add(order);
  }

  public double calculateTotalRevenue() {
    return orders.stream()
      .mapToDouble(order -> order.getQuantity() * order.getPrice())
      .sum();
  }
}
```
- Business logic is inside the domain class.
- Invariants are enforced.
- The model is expressive and aligns with DDD principles.



### Concrete Benefits of FP-Style Rich Domain Model (Split Design)


In the standard model of object-oriented design, this leads to business logic becoming concentrated in the service layer. 
Fowler notes that:
> “the problem with anemic domain models is that they incur all the costs of a domain model
[in particular the need for persistence mappings], without yielding any of the benefits.” 

The scenario he describes allows for these objects to be named for nouns in the domain space, 
whereas in the Collections Framework this anti-pattern appears as nameless collection structures directly containing other collections, sometimes deeply nested, with no provision for custom behavior. The object design failure is fundamentally the same in each case.


FP-Style Composability (Pipeline)



### OOP-Style Composability



###






### 

What does this mean?

Nameless collection structures

You have raw List, Map, Set types without wrapping them in meaningful domain classes.
Example:
JavaMap<String, List<Order>> customerOrders = new HashMap<>();Show more lines
Here, Map<String, List<Order>> is just a generic container. It doesn’t express the domain concept like “CustomerOrders” or enforce any rules.





Deeply nested collections

When you see types like Map<String, Map<String, List<Order>>> or List<List<Map<String, Order>>>, it’s a code smell.
Why? Because:

It’s hard to understand what the structure represents.
There’s no encapsulation or domain logic.
All business rules are scattered outside these collections.







No provision for custom behavior

These raw collections don’t enforce invariants or provide domain-specific operations.
For example:
JavacustomerOrders.get("CUST123").add(new Order(-1, 100.0)); // Invalid order!Show more lines
Nothing prevents adding an invalid order because the collection is just a container.




✅ Why is this a problem?

Same failure as Anemic Domain Model: Data without behavior.
Hard to maintain: Logic is scattered in utility methods.
Poor readability: Nested collections hide domain meaning.


✅ How to fix it?
Wrap collections in domain-specific classes that:

Give them a name.
Encapsulate invariants.
Provide meaningful operations.

Example:

public class CustomerOrders {
private final Map<String, List<Order>> ordersByCustomer = new HashMap<>();

    public void addOrder(String customerId, Order order) {
        if (order.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        ordersByCustomer.computeIfAbsent(customerId, id -> new ArrayList<>()).add(order);
    }

    public double totalForCustomer(String customerId) {
        return ordersByCustomer.getOrDefault(customerId, List.of()).stream()
            .mapToDouble(o -> o.quantity() * o.price())
            .sum();
    }
}
Now:

The domain concept is clear: CustomerOrders.
Invariants are enforced.
Behavior is close to the data.


Key takeaway from the book
Nested raw collections (Map<String, List<Order>>, List<Map<String, Something>>) are a code smell because they indicate missing domain abstractions. Instead of exposing these structures, create rich domain classes that encapsulate both data and behavior.



Why Nested Collections Are a Code Smell

Collections are generic containers: List, Map, Set don’t represent domain concepts.
When you see deeply nested collections like:
Map<String, List<Order>> customerOrders;
or worse:

Map<String, Map<String, List<Order>>> complexStructure;
it signals:

No domain abstraction: The structure itself is the “model.”
No behavior: All business logic (validation, calculations) is external.
Hard to understand: What does Map<String, Map<String, List<Order>>> mean in the domain? It’s unclear.




✅ Why This Is Anemic Domain Model

Anemic Domain Model = data without behavior.
Nested collections are just raw data structures, not domain objects.
They don’t enforce invariants (e.g., valid order quantity).
They don’t provide meaningful operations (e.g., calculate total revenue).

So, the failure is the same: logic scattered in services or utilities, not encapsulated in domain classes.

Refactored Rich Domain Model

public class CustomerOrders {
private final Map<String, List<Order>> ordersByCustomer = new HashMap<>();

    public void addOrder(String customerId, Order order) {
        if (order.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        ordersByCustomer.computeIfAbsent(customerId, id -> new ArrayList<>()).add(order);
    }

    public double totalForCustomer(String customerId) {
        return ordersByCustomer.getOrDefault(customerId, List.of()).stream()
            .mapToDouble(o -> o.getQuantity() * o.getPrice())
            .sum();
    }
}
Now:

The domain concept is explicit: CustomerOrders.
Invariants enforced.
Behavior is close to the data.


Key Insight from the Book
Nested collections are a code smell because they indicate:

Missing domain abstractions.
Missing encapsulation of behavior.
Increased complexity and maintenance cost.