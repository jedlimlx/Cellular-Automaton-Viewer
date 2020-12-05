package sample.controller;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import sample.model.Coordinate;

/**
 * The selection rectangle used by CAViewer to select multiple cells
 */
public class SelectionRectangle extends Rectangle {
    private final int CELL_SIZE;
    private Coordinate start, end;

    /**
     * Constructs a selection rectangle
     * @param CELL_SIZE The cell size of the grid where the selection rectangle is placed
     */
    public SelectionRectangle(int CELL_SIZE) {
        this.CELL_SIZE = CELL_SIZE;

        start = new Coordinate();
        end = new Coordinate();

        super.setOpacity(0.3);
        super.setFill(Color.rgb(75, 175, 0));
        super.toFront();
        super.setVisible(false);
    }

    /**
     * Selects the specified area
     * @param start The start coordinate of the selection area
     * @param end The end coordinate of the selection area
     */
    public void select(Coordinate start, Coordinate end) {
        this.start = start;
        this.end = end;

        super.setX(start.getX());
        super.setY(start.getY());
        super.setWidth(end.getX() - start.getX() + CELL_SIZE);
        super.setHeight(end.getY() - start.getY() + CELL_SIZE);

        super.setVisible(true);
    }

    /**
     * Selects the specified area
     * @param end The end coordinate of the selection area
     */
    public void select(Coordinate end) {
        select(start, end);
    }

    /**
     * Unselects the specified area
     */
    public void unselect() {
        super.setVisible(false);
    }

    /**
     * Is the selection rectangle active
     * @return Returns whether the selection rectangle is active
     */
    public boolean isSelecting() {
        return super.isVisible();
    }

    /**
     * Gets the start coordinate of the selection
     * @return Returns the start coordinate of the selection
     */
    public Coordinate getStart() {
        return new Coordinate(start.getX() / CELL_SIZE, start.getY() / CELL_SIZE);
    }

    /**
     * Gets the end coordinate of the selection
     * @return Returns the end coordinate of the selection
     */
    public Coordinate getEnd() {
        return new Coordinate(end.getX() / CELL_SIZE, end.getY() / CELL_SIZE);
    }
}
