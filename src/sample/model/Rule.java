package sample.model;

import javafx.scene.paint.Color;

import java.util.HashSet;

public abstract class Rule {
    protected int numStates, alternatingPeriod;

    // Child class must implement
    public abstract Coordinate[] getNeighbourhood(int generation);
    public abstract int transitionFunc(int[] neighbours, int cellState, int generations);

    // Accessors
    public int getAlternatingPeriod() {
        return alternatingPeriod;
    }
    public int getNumStates() {
        return numStates;
    }

    // Generate colour palette
    public Color getColor(int state) {
        if (numStates == 2) {  // Get correct colour
            if (state == 0) {
                return Color.rgb(0, 0, 0);
            }
            return Color.rgb(255, 255, 255);
        }
        else {
            return Color.rgb(255, 255 * (state - 1) / (numStates - 2), 0);
        }
    }

    // Method to step forward one generation
    public void step(Grid grid, HashSet<Coordinate> cellsChanged, int generation) {
        Grid gridCopy = grid.deepCopy();
        HashSet<Coordinate> cellsToCheck = new HashSet<>();
        Coordinate[] neighbourhood = getNeighbourhood(generation);

        // Generate set of cells to run update function on
        // Use a set to avoid duplicates
        for (Coordinate cell: cellsChanged) {
            for (Coordinate neighbour: neighbourhood) {
                cellsToCheck.add(cell.add(neighbour));
            }
            cellsToCheck.add(cell);
        }

        // Clear the cells changed
        // TODO (Fix the mess that is B0 rules)
        if (alternatingPeriod > 1) {
            if (generation % (alternatingPeriod + 1) == 0) {
                cellsChanged.clear();
            }
        }
        else {
            cellsChanged.clear();
        }

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
