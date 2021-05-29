package application.model;

/**
 * Represents a coordinate on a 2D plane.
 * Note that the Coordinate class is immutable.
 */
public class Coordinate implements Comparable<Coordinate> {
    /**
     * The x-coordinate of the coordinate
     */
    private final int x;

    /**
     * The y-coordinate of the coordinate
     */
    private final int y;

    /**
     * Initialises a coordinate at (0, 0)
     */
    public Coordinate() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Initialises a coordinate at (x, y)
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x-coordinate
     * @return Returns the x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate
     * @return Returns the y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Adds 2 coordinates. For example, (2, 3) + (1, 4) = (2 + 1, 3 + 4) = (3, 7).
     * @param coordinate The coordinate to add
     * @return Returns the new coordinate
     */
    public Coordinate add(Coordinate coordinate) {
        return new Coordinate(this.x + coordinate.x, this.y + coordinate.y);
    }

    /**
     * Adds 2 coordinates. For example, (2, 3) - (1, 4) = (2 - 1, 3 - 4) = (1, -1).
     * @param coordinate The coordinate to subtract
     * @return Returns the new coordinate
     */
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
        return x + y * 5000;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
