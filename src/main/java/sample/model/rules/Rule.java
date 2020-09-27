package sample.model.rules;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javafx.scene.paint.Color;
import sample.model.Coordinate;
import sample.model.simulation.Grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single rule
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class Rule {
    /**
     * A list of backgrounds to be used for B0 rules.
     * Example:
     * A 3-state generations B0 rule without Smax would have a background of {0, 1, 2}.
     * A non-B0 rule would have a background of {0}.
     */
    protected int[] background;

    /**
     * Number of states in the rule
     */
    protected int numStates;

    /**
     * Alternating period of the rule
     */
    protected int alternatingPeriod;

    /**
     * The tiling of the rule
     */
    protected Tiling tiling = Tiling.Square;

    /**
     * This method returns the neighbourhood of a given cell at a generation 0
     * @return A list of Coordinates that represent the neighbourhood
     */
    public Coordinate[] getNeighbourhood() {
        return getNeighbourhood(0);
    }

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
     * @param coordinate The coordinate of the cell
     * @return The state of the cell in the next generation
     */
    public abstract int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate);

    /**
     * If the next state of the cell depends on its neighbours, return -1.
     * If not return the next state of the cell.
     * @param state The current state of the cell
     * @param generation The generation of the simulation
     * @param coordinate The coordinate of the cell
     * @return Returns -1 or the next state of the cell
     */
    public int dependsOnNeighbours(int state, int generation, Coordinate coordinate) {
        return -1;
    }

    /**
     * Gets the alternating period of the rule
     * @return Returns the rule's alternating period
     */
    public int getAlternatingPeriod() {
        return alternatingPeriod;
    }

    /**
     * Gets the number of states in the rule
     * @return Returns the number of states in the rule
     */
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
     * Returns the tiling of the rule (Square, Hexagonal or Triangular)
     * @return Returns the rule's tiling
     */
    public Tiling getTiling() {
        return tiling;
    }

    /**
     * Steps the grid provided forward one generation
     * @param grid The grid that will be stepped forward one generation
     * @param cellsChanged An array of sets that contains the cells the changed in the previous generations.
     *                     The first entry will contains the cells that changed in the previous generation
     *                     and the next entry will contain the cells that changed the previous previous generation
     *                     and so on. It should be the same length as the alternating period of the rule
     * @param generation The current generation of the simulation
     * @throws IllegalArgumentException Thrown if the length of cellsChanged is not the same as the alternating period
     */
    public void step(Grid grid, ArrayList<Set<Coordinate>> cellsChanged, int generation)
            throws IllegalArgumentException {
        if (cellsChanged.size() != alternatingPeriod)
            throw new IllegalArgumentException("cellsChanged parameter should have length " + alternatingPeriod + "!");

        Grid gridCopy = grid.deepCopy();
        HashSet<Coordinate> cellsToCheck = new HashSet<>();
        Coordinate[] neighbourhood = getNeighbourhood(generation);

        Coordinate[] invertedNeighbourhood = new Coordinate[neighbourhood.length];
        if (tiling == Tiling.Triangular) {
            // Inverting neighbourhood on the triangular checkboard grid thing
            for (int i = 0; i < neighbourhood.length; i++) {
                invertedNeighbourhood[i] = new Coordinate(neighbourhood[i].getX(), -neighbourhood[i].getY());
            }
        }

        // Generate set of cells to run update function on
        // Use a set to avoid duplicates
        for (Set<Coordinate> cellSet: cellsChanged) {
            for (Coordinate cell: cellSet) {
                if (tiling != Tiling.Triangular || Math.floorMod(cell.getX(), 2) != Math.floorMod(cell.getY(), 2)) {
                    for (Coordinate neighbour: neighbourhood) {
                        cellsToCheck.add(cell.subtract(neighbour));
                    }
                }
                else {
                    for (Coordinate neighbour: invertedNeighbourhood) {
                        cellsToCheck.add(cell.subtract(neighbour));
                    }
                }

                cellsToCheck.add(cell);
            }
        }

        int[] neighbours;
        int newState, prevState;
        for (Coordinate cell: cellsToCheck) {
            prevState = gridCopy.getCell(cell);

            // Getting neighbour states
            if (dependsOnNeighbours(convertState(prevState, generation), generation, cell) == -1) {
                neighbours = new int[neighbourhood.length];
                if (tiling != Tiling.Triangular || Math.floorMod(cell.getX(), 2) == Math.floorMod(cell.getY(), 2)) {
                    for (int i = 0; i < neighbourhood.length; i++) {
                        // Converting based on background
                        neighbours[i] = convertState(gridCopy.getCell(cell.add(neighbourhood[i])), generation);
                    }
                }
                else {
                    for (int i = 0; i < neighbourhood.length; i++) {
                        // Converting based on background
                        neighbours[i] = convertState(gridCopy.getCell(cell.add(invertedNeighbourhood[i])), generation);
                    }
                }

                // Call the transition function on the new state
                // Don't forget to convert back to the current background
                newState = convertState(transitionFunc(neighbours,
                        convertState(prevState, generation), generation, cell), generation + 1);
            }
            else {
                newState = convertState(dependsOnNeighbours(convertState(prevState, generation), generation, cell),
                        generation + 1);
            }

            if (newState != prevState) {
                cellsChanged.get(0).add(cell);
                grid.setCell(cell, newState);
            }
            else {
                for (int i = 0; i < alternatingPeriod; i++) {
                    if (cellsChanged.get(i).contains(cell)) {
                        cellsChanged.get(i).remove(cell);

                        // Move the cell forward into the next entry until it can't be moved forward anymore
                        if (i < alternatingPeriod - 1) cellsChanged.get(i + 1).add(cell);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Convert the cell state based on the background
     * Used to simulate B0 rules
     * @param state The current state of the cell
     * @param generation The generation of the simulation
     * @return Returns the new cell state
     */
    public int convertState(int state, int generation) {
        // 0 <-> background
        if (state == 0)
            return background[Math.floorMod(generation, background.length)];
        else if (state == background[Math.floorMod(generation, background.length)])
            return 0;
        else
            return state;
    }
}
