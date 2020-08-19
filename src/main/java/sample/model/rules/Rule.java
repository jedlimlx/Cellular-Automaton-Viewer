package sample.model.rules;

import javafx.scene.paint.Color;
import sample.model.Coordinate;
import sample.model.Grid;

import java.util.HashSet;

/**
 * Represents a single rule
 * @author Lemon41625
 */
public abstract class Rule {
    protected int numStates, alternatingPeriod;

    /**
     * This method returns the neighbourhood of a given cell at a certain generation
     * @param generation The generation of the simulation
     * @return A list of Coordinates that represent the neighbourhood
     */
    public abstract Coordinate[] getNeighbourhood(int generation);

    /**
     * This method represents the transition function of the rule
     * @param neighbours The cell's neighbours in the order of the neighbourhood provided
     * @param cellState The current state of the cell
     * @param generations The current generation of the simulation
     * @return The state of the cell in the next generation
     */
    public abstract int transitionFunc(int[] neighbours, int cellState, int generations);

    // Accessors
    public int getAlternatingPeriod() {
        return alternatingPeriod;
    }

    public int getNumStates() {
        return numStates;
    }

    /**
     * Returns the colour of a cell of the provided state
     * @param state The state of the cell
     * @return The colour of the cell
     */
    public Color getColour(int state) {
        if (state == 0) {
            return Color.rgb(0, 0, 0);
        }

        if (numStates == 2) {  // Get correct colour
            return Color.rgb(255, 255, 255);
        }
        else {
            return Color.rgb(255, 255 * (state - 1) / (numStates - 2), 0);
        }
    }

    /**
     * Steps the grid provided forward one generation
     * @param grid The grid that will be stepped forward one generation
     * @param cellsChanged The cells that changed in the previous generation
     * @param generation The current generation of the simulation
     */
    public void step(Grid grid, HashSet<Coordinate> cellsChanged, int generation) {
        Grid gridCopy = grid.deepCopy();
        HashSet<Coordinate> cellsToCheck = new HashSet<>();
        Coordinate[] neighbourhood = getNeighbourhood(generation);

        // Generate set of cells to run update function on
        // Use a set to avoid duplicates
        if (alternatingPeriod == 1) {
            for (Coordinate cell: cellsChanged) {
                for (Coordinate neighbour: neighbourhood) {
                    cellsToCheck.add(cell.add(neighbour));
                }
                cellsToCheck.add(cell);
            }
        }
        else {
            // TODO (Optimise alternating rules code)
            for (Coordinate cell: grid) {  // Check all alive cell if alternating period >1
                for (Coordinate neighbour: neighbourhood) {
                    cellsToCheck.add(cell.add(neighbour));
                }
                cellsToCheck.add(cell);
            }
        }

        // Clear the cells changed
        cellsChanged.clear();

        int[] neighbours;
        int neighbourhood_size = neighbourhood.length, new_state, prev_state;
        for (Coordinate cell: cellsToCheck) {
            prev_state = gridCopy.getCell(cell);

            // Getting neighbour states
            neighbours = new int[neighbourhood_size];
            for (int i = 0; i < neighbourhood_size; i++) {
                neighbours[i] = gridCopy.getCell(cell.add(neighbourhood[i]));
            }

            // Call the transition function on the new state
            new_state = transitionFunc(neighbours, prev_state, generation);
            if (new_state != prev_state) {
                cellsChanged.add(cell);
                grid.setCell(cell, new_state);
            }
        }
    }
}
