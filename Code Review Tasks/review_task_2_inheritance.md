# Code Review Task 2

**Focus area:** inheritance / substitutability / equals across a hierarchy
**How to use:** Review the code below and write your findings. For each issue, name the defect,
say *why* it matters (the failure it causes), and — where relevant — question whether the design
should exist in that form at all. Order your findings worst-first. Then send them to me to grade.

```java
public class Rectangle {

    protected int width;
    protected int height;

    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int area() {
        return width * height;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Rectangle)) return false;
        Rectangle r = (Rectangle) o;
        return width == r.width && height == r.height;
    }

    @Override
    public int hashCode() {
        return width * 31 + height;
    }
}

class Square extends Rectangle {

    public Square(int side) {
        super(side, side);
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
        this.height = width;
    }

    @Override
    public void setHeight(int height) {
        this.width = height;
        this.height = height;
    }
}
```
