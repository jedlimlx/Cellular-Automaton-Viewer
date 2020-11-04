package sample.model;

import javafx.scene.shape.Rectangle;

public class Cell {
    private Coordinate coordinate;
    private final Rectangle rectangle;
    private int state;

    public Cell (Coordinate coordinate, int state, Rectangle rectangle) {
        this.coordinate = coordinate;
        this.state = state;
        this.rectangle = rectangle;

        // Gives that sharp, crisp look that pixelated things should have
        rectangle.setSmooth(false);
    }

    public Cell (int x, int y, int state, Rectangle rectangle) {
        this(new Coordinate(x, y), state, rectangle);
    }

    // Accessors
    public int getState() {
        return state;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    // Mutators
    public void setState(int state) {
        this.state = state;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
}
