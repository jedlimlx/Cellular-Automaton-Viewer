package application.controller

import application.model.Coordinate
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

/**
 * The selection rectangle used by CAViewer to select multiple cells
 */
class SelectionRectangle(private val CELL_SIZE: Int) : Rectangle() {
    var start: Coordinate
        get() = Coordinate(field.x / CELL_SIZE, field.y / CELL_SIZE)
        private set
    var end: Coordinate
        get() = Coordinate(field.x / CELL_SIZE, field.y / CELL_SIZE)
        private set

    /**
     * Constructs a selection rectangle
     * @param CELL_SIZE The cell size of the grid where the selection rectangle is placed
     */
    init {
        start = Coordinate()
        end = Coordinate()
        super.setOpacity(0.3)
        super.setFill(Color.rgb(75, 175, 0))
        super.toFront()
        super.setVisible(false)
    }

    /**
     * Selects the specified area
     * @param start The start coordinate of the selection area
     * @param end The end coordinate of the selection area
     */
    fun select(start: Coordinate, end: Coordinate) {
        this.start = start
        this.end = end
        super.setX(start.x.toDouble())
        super.setY(start.y.toDouble())
        super.setWidth((end.x - start.x + CELL_SIZE).toDouble())
        super.setHeight((end.y - start.y + CELL_SIZE).toDouble())
        super.setVisible(true)
    }

    /**
     * Selects the specified area
     * @param end The end coordinate of the selection area
     */
    fun select(end: Coordinate) {
        select(start, end)
    }

    /**
     * Unselects the specified area
     */
    fun unselect() {
        super.setVisible(false)
    }

    /**
     * Is the selection rectangle active
     * @return Returns whether the selection rectangle is active
     */
    val isSelecting: Boolean
        get() = super.isVisible()

}