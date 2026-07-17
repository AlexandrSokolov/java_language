# Code Review Task 5

**Focus area:** ordering contracts — `Comparable`, consistency with `equals`
**How to use:** Review the code below and write your findings. For each issue, name the defect,
say *why* it matters (the failure it causes, and which collections or operations break), and —
where relevant — question the design. Order your findings worst-first. Then send them to me to grade.

```java
import java.util.Objects;

public class Version implements Comparable<Version> {

    private final int major;
    private final int minor;
    private final String label;

    public Version(int major, int minor, String label) {
        this.major = major;
        this.minor = minor;
        this.label = label;
    }

    @Override
    public int compareTo(Version other) {
        if (this.major > other.major) return 1;
        if (this.major < other.major) return -1;
        return this.minor - other.minor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        Version v = (Version) o;
        return major == v.major && minor == v.minor && label.equals(v.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, label);
    }
}
```
