package sample.model;

import java.util.Objects;

public class Coordinate implements Comparable<Coordinate> {
    private final int x, y;  // Make it immutable
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Accessors
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Coordinate add(Coordinate coordinate) {
        return new Coordinate(this.x + coordinate.x, this.y + coordinate.y);
    }

    public Coordinate subtract(Coordinate coordinate) {
        return new Coordinate(this.x - coordinate.x, this.y - coordinate.y);
    }

    @Override
    public int compareTo(Coordinate o) {
        if (this.x == o.x) {
            return Integer.compare(this.y, o.y);
        }
        else {
            return Integer.compare(this.x, o.x);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return x == that.x &&
                y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
