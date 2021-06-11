package application.model.rules;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javafx.scene.paint.Color;
import application.model.Coordinate;
import application.model.rules.misc.naive.ReadingOrder;
import application.model.simulation.Grid;
import application.model.simulation.bounds.BoundedGrid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents a single rule
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
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
    protected int alternatingPeriod = 1;

    /**
     * The number of cells that the pattern needs to be translated by in any direction such
     * that it will evolve into an identical next state. For most rules, it is 1.
     * For rules such as Margolus / Block CA, it can be larger than that.
     */
    protected int locationPeriod = 1;

    /**
     * The tiling of the rule
     */
    protected Tiling tiling = Tiling.Square;

    /**
     * Bounded grid used by the rule
     */
    protected BoundedGrid boundedGrid;

    /**
     * Naive reading order used by the rule
     */
    protected ReadingOrder readingOrder;

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
     * Gets the location period of the rule
     * @return Returns the location period of the rule
     */
    public int getLocationPeriod() {
        return locationPeriod;
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
     * Returns the name of the provided state
     * @param state The state of the cell
     * @return The name of the state
     */
    public String getName(int state) {
        return "State " + state;
    }

    /**
     * Returns the tiling of the rule (Square, Hexagonal or Triangular)
     * @return Returns the rule's tiling
     */
    public Tiling getTiling() {
        return tiling;
    }

    /**
     * Gets the bounded grid of the rule
     * @return Returns the bounded grid of the rule
     */
    public BoundedGrid getBoundedGrid() {
        return boundedGrid;
    }

    /**
     * Find I such that <br>
     * ABC <br>
     * DEF -> E' <br>
     * GHI <br>
     * @param neighbours The neighbours of the central cell
     * @param indexOfUnknown The index of the unknown cell
     * @param cellState The current state of the central cell
     * @param nextState The next state of the central cell
     * @param generation The generation that the function is invoked
     * @return Returns a boolean array where each index represents whether the unknown cell could be that state
     */
    public boolean[] getSuccessor(int[] neighbours, int indexOfUnknown, int cellState,
                                           int nextState, int generation) {
        boolean[] possible = new boolean[numStates];
        for (int i = 0; i < numStates; i++) {
            neighbours[indexOfUnknown] = i;
            possible[i] = dependsOnNeighbours(cellState, generation, new Coordinate()) == nextState ||
                    nextState == transitionFunc(neighbours, cellState, generation, new Coordinate());
        }

        return possible;
    }

    /**
     * Steps the grid provided forward one generation
     * @param grid The grid that will be stepped forward one generation
     * @param cellsChanged An array of sets that contains the cells the changed in the previous generations.
     *                     The first entry will contains the cells that changed in the previous generation
     *                     and the next entry will contain the cells that changed the previous previous generation
     *                     and so on. It should be the same length as the alternating period of the rule
     * @param generation The current generation of the simulation
     * @param step A function that returns whether the cell at that coordinate should be stepped forward.
     * @throws IllegalArgumentException Thrown if the length of cellsChanged is not the same as the alternating period
     */
    public void step(Grid grid, ArrayList<Set<Coordinate>> cellsChanged, int generation,
                     Function<Coordinate, Boolean> step)
            throws IllegalArgumentException {
        if (cellsChanged.size() != alternatingPeriod)
            throw new IllegalArgumentException("cellsChanged parameter should have length " + alternatingPeriod + "!");

        int totalSize = 0;
        for (Set<Coordinate> cellSet: cellsChanged) totalSize += cellSet.size();

        HashSet<Coordinate> cellsToCheck = new HashSet<>(totalSize);
        Coordinate[] neighbourhood = getNeighbourhood(generation);

        Coordinate[] invertedNeighbourhood = new Coordinate[neighbourhood.length];
        if (tiling == Tiling.Triangular) {
            // Inverting neighbourhood on the triangular checkboard grid thing
            for (int i = 0; i < neighbourhood.length; i++) {
                invertedNeighbourhood[i] = new Coordinate(neighbourhood[i].getX(), -neighbourhood[i].getY());
            }
        }

        // Generate set of cells to run update function on
        // Use a set to avoid duplicate
        Coordinate neighbour;
        for (Set<Coordinate> cellSet: cellsChanged) {
            for (Coordinate cell: cellSet) {
                if (step != null && !step.apply(cell)) continue;  // Don't evaluate this cell

                if (tiling != Tiling.Triangular || Math.floorMod(cell.getX(), 2) != Math.floorMod(cell.getY(), 2)) {
                    for (Coordinate neighbour2: neighbourhood) {
                        neighbour = cell.subtract(neighbour2);

                        // Apply the bounded grid
                        if (boundedGrid != null && boundedGrid.atEdge(neighbour))
                            neighbour = boundedGrid.map(neighbour);

                        if (step != null && !step.apply(neighbour)) continue;

                        cellsToCheck.add(neighbour);
                    }
                }
                else {
                    for (Coordinate neighbour2: invertedNeighbourhood) {
                        neighbour = cell.subtract(neighbour2);

                        // Apply the bounded grid
                        if (boundedGrid != null && boundedGrid.atEdge(neighbour))
                            neighbour = boundedGrid.map(neighbour);

                        if (step != null && !step.apply(neighbour)) continue;

                        cellsToCheck.add(neighbour);
                    }
                }

                // Apply the bounded grid
                if (boundedGrid != null && boundedGrid.atEdge(cell)) cell = boundedGrid.map(cell);

                cellsToCheck.add(cell);
            }
        }

        int[] neighbours;
        int newState, prevState;
        if (readingOrder == null) {
            Grid gridCopy = grid.deepCopy();
            neighbours = new int[neighbourhood.length];
            for (Coordinate cell: cellsToCheck) {
                prevState = gridCopy.getCell(cell);

                // Getting neighbour states
                if (dependsOnNeighbours(convertState(prevState, generation), generation, cell) == -1) {
                    if (tiling != Tiling.Triangular || Math.floorMod(cell.getX(), 2) == Math.floorMod(cell.getY(), 2)) {
                        for (int i = 0; i < neighbourhood.length; i++) {
                            neighbour = cell.add(neighbourhood[i]);

                            // Apply the bounded grid
                            if (boundedGrid != null && boundedGrid.atEdge(neighbour))
                                neighbour = boundedGrid.map(neighbour);

                            // Converting based on background
                            neighbours[i] = convertState(gridCopy.getCell(neighbour), generation);
                        }
                    }
                    else {
                        for (int i = 0; i < neighbourhood.length; i++) {
                            neighbour = cell.add(invertedNeighbourhood[i]);

                            // Apply the bounded grid
                            if (boundedGrid != null && boundedGrid.atEdge(neighbour))
                                neighbour = boundedGrid.map(neighbour);

                            // Converting based on background
                            neighbours[i] = convertState(gridCopy.getCell(neighbour), generation);
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
        } else {
            Coordinate cell;

            // Priority queue to ensure the cells are evaluated in the right order
            PriorityQueue<Coordinate> cellsQueue = new PriorityQueue<>(readingOrder.getCoordinateComparator());
            cellsQueue.addAll(cellsToCheck);

            for (int i = 0; i < alternatingPeriod; i++) {
                cellsChanged.get(i).clear();
            }

            while (!cellsQueue.isEmpty()) {
                cell = cellsQueue.poll();

                prevState = grid.getCell(cell);

                // Getting neighbour states
                if (dependsOnNeighbours(convertState(prevState, generation), generation, cell) == -1) {
                    neighbours = new int[neighbourhood.length];
                    if (tiling != Tiling.Triangular || Math.floorMod(cell.getX(), 2) == Math.floorMod(cell.getY(), 2)) {
                        for (int i = 0; i < neighbourhood.length; i++) {
                            neighbour = cell.add(neighbourhood[i]);

                            // Apply the bounded grid
                            if (boundedGrid != null && boundedGrid.atEdge(neighbour))
                                neighbour = boundedGrid.map(neighbour);

                            // Converting based on background
                            neighbours[i] = grid.getCell(neighbour);
                        }
                    }
                    else {
                        for (int i = 0; i < neighbourhood.length; i++) {
                            neighbour = cell.add(invertedNeighbourhood[i]);

                            // Apply the bounded grid
                            if (boundedGrid != null && boundedGrid.atEdge(neighbour))
                                neighbour = boundedGrid.map(neighbour);

                            // Converting based on background
                            neighbours[i] = grid.getCell(neighbour);
                        }
                    }

                    // Call the transition function on the new state
                    newState = transitionFunc(neighbours, prevState, generation, cell);
                }
                else {
                    newState = dependsOnNeighbours(prevState, generation, cell);
                }

                if (newState != prevState) {
                    if (tiling != Tiling.Triangular || Math.floorMod(cell.getX(), 2) !=
                            Math.floorMod(cell.getY(), 2)) {
                        for (Coordinate neighbour2: neighbourhood) {
                            neighbour = cell.subtract(neighbour2);

                            // Apply the bounded grid
                            if (boundedGrid != null && boundedGrid.atEdge(neighbour))
                                neighbour = boundedGrid.map(neighbour);

                            if (step != null && !step.apply(neighbour)) continue;

                            if (readingOrder.getCoordinateComparator().compare(cell, neighbour) < 0 &&
                                    !cellsQueue.contains(neighbour)) {
                                cellsQueue.add(neighbour);
                            }
                        }
                    }
                    else {
                        for (Coordinate neighbour2: invertedNeighbourhood) {
                            neighbour = cell.subtract(neighbour2);

                            // Apply the bounded grid
                            if (boundedGrid != null && boundedGrid.atEdge(neighbour))
                                neighbour = boundedGrid.map(neighbour);

                            if (step != null && !step.apply(neighbour)) continue;

                            if (readingOrder.getCoordinateComparator().compare(cell, neighbour) < 0 &&
                                    !cellsQueue.contains(neighbour)) {
                                cellsQueue.add(neighbour);
                            }
                        }
                    }

                    cellsChanged.get(0).add(cell);
                    grid.setCell(cell, newState);
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

    /**
     * Sets the background of the rule
     * @param background The background of the rule
     */
    public void setBackground(int[] background) {
        this.alternatingPeriod = background.length;
        this.background = background;
    }

    /**
     * Sets the bounded grid of the rule
     * @param boundedGrid The bounded grid of the rule
     */
    public void setBoundedGrid(BoundedGrid boundedGrid) {
        this.boundedGrid = boundedGrid;
    }

    /**
     * Sets the naive reading order of the rule
     * @param order The naive reading order the rule should use
     */
    public void setReadingOrder(ReadingOrder order) {
        this.readingOrder = order;
    }
}
