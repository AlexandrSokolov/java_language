# Code Review Task 1

**Focus area:** value objects / immutability
**How to use:** Review the code below and write your findings. For each issue, name the defect,
say *why* it matters (the failure it causes), and — where relevant — question whether the code
should exist in that form at all. Order your findings worst-first. Then send them to me to grade.

```java
import java.util.List;

public final class Route {

    private final String origin;
    private final String destination;
    private final List<String> waypoints;
    private int[] segmentDistances;

    public Route(String origin, String destination, List<String> waypoints, int[] segmentDistances) {
        this.origin = origin;
        this.destination = destination;
        this.waypoints = waypoints;
        this.segmentDistances = segmentDistances;
    }

    public List<String> getWaypoints() {
        return waypoints;
    }

    public int[] getSegmentDistances() {
        return segmentDistances;
    }

    public int totalDistance() {
        int sum = 0;
        for (int d : segmentDistances) {
            sum += d;
        }
        return sum;
    }

    public String toString() {
        return origin + " -> " + destination + " (" + waypoints.size() + " stops)";
    }
}
```
