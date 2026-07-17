# Code Review Task 4

**Focus area:** generics / wildcards / type safety
**How to use:** Review the code below and write your findings. For each issue, name the defect,
say *why* it matters (the failure it causes), and — where relevant — question whether the API
should be shaped differently. Order your findings worst-first. Then send them to me to grade.

```java
import java.util.ArrayList;
import java.util.List;

public class Box<T> {

    private List<T> contents = new ArrayList<>();

    public void add(T item) {
        contents.add(item);
    }

    public T get(int i) {
        return contents.get(i);
    }

    public void addAll(List<T> items) {
        for (T item : items) {
            contents.add(item);
        }
    }

    public void copyFrom(Box other) {
        for (Object o : other.contents) {
            contents.add((T) o);
        }
    }

    public static double sumSizes(List<Box> boxes) {
        double total = 0;
        for (Box b : boxes) {
            total += b.contents.size();
        }
        return total;
    }
}
```
