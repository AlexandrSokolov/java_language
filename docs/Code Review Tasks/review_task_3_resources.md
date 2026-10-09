# Code Review Task 3

**Focus area:** exception handling / resource management / control flow
**How to use:** Review the code below and write your findings. For each issue, name the defect,
say *why* it matters (the failure it causes), and — where relevant — question whether the code
should exist in that form at all. Order your findings worst-first. Then send them to me to grade.

`FileHandle` is a stand-in for any resource that must be closed; assume it works correctly.
Review everything else.

```java
public class ReportGenerator {

    private FileHandle handle;

    public String generate(String path) {
        String result = null;
        try {
            handle = new FileHandle(path);
            result = handle.readAll();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        } finally {
            handle.close();
        }
        return result;
    }

    public int countLines(String path) throws Exception {
        FileHandle h = new FileHandle(path);
        int count = h.readAll().split("\n").length;
        h.close();
        return count;
    }

    static class FileHandle implements AutoCloseable {
        FileHandle(String path) { /* opens a resource */ }
        String readAll() { return ""; }
        public void close() { /* releases the resource */ }
    }
}
```
