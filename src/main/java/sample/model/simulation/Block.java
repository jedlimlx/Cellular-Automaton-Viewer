package sample.model.simulation;

import sample.model.Coordinate;
import sample.model.rules.Rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents a block of cells
 */
public class Block implements Cloneable {
    /**
     * The array storing the cells
     */
    private final int[][] cells;

    /**
     * The starting coordinate of the block (top-left corner)
     */
    private final Coordinate coordinate;

    /**
     * The state of the block (Active, Stable, Oscillating)
     */
    private BlockState state;

    /**
     * The number of alive cells in the thing
     */
    private int population;

    /**
     * Constructs a block with a specific width and height
     * @param startingCoordinate The coordinate of the block (top-left)
     * @param width Width of the block
     * @param height Height of the block
     */
    public Block(Coordinate startingCoordinate, int width, int height) {
        cells = new int[height][width];
        this.coordinate = startingCoordinate;
    }

    /**
     * Sets the value of a cell in the block
     * @param x The x-coordinate of the cell
     * @param y The y-coordinate of the cell
     * @param state The state of the cell
     */
    public void setCell(int x, int y, int state) {
        int prevState = cells[y - coordinate.getY()][x - coordinate.getX()];
        if (prevState == 0 && state > 0) population++;
        else if (prevState > 0 && state == 0) population--;

        cells[y - coordinate.getY()][x - coordinate.getX()] = state;
    }

    /**
     * Gets the state of the cell at coordinate (x, y)
     * @param x The x-coordinate of the cell to get
     * @param y The y-coordinate of the cell to get
     * @return Returns the state of the cell at (x, y)
     */
    public int getCell(int x, int y) {
        return cells[y - coordinate.getY()][x - coordinate.getX()];
    }

    /**
     * Gets the starting coordinate of the block (top-left)
     * @return Returns the starting coordinate of the block
     */
    public Coordinate getStartCoordinate() {
        return coordinate;
    }

    /**
     * Checks if the coordinate is within the current block
     * @param coordinate The coordinate to check
     * @return Returns true if the coordinate is within the block, false otherwise
     */
    public boolean inBlock(Coordinate coordinate) {
        Coordinate thing = coordinate.subtract(getStartCoordinate());
        return thing.getX() > 0 && thing.getX() < cells.length && thing.getY() > 0 &&
                thing.getY() < cells.length;
    }

    /**
     * Gets the population of alive cells (> state 0) in the block
     * @return Returns the population of the block
     */
    public int getPopulation() {
        return population;
    }

    /**
     * Gets the state of the block
     * @return Returns the state of the block
     */
    public BlockState getState() {
        return state;
    }

    /**
     * Sets the state of the block
     * @param state The state of the block
     */
    public void setState(BlockState state) {
        this.state = state;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(cells);
    }

    @Override
    public Object clone() {
        Block block = new Block(coordinate, cells[0].length, cells.length);
        for (int i = 0; i < cells.length; i++) {
            System.arraycopy(cells[i], 0, block.cells[i], 0, cells[0].length);
        }

        block.population = population;
        return block;
    }
}
