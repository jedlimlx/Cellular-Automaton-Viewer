package sample.model.rules.isotropic;

import sample.model.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public abstract class INTTransitions {
    /**
     * The transition string of the INT transition
     */
    protected String transitionString;

    /**
     * The neighbourhood of the INT transitions
     */
    protected Coordinate[] neighbourhood;

    /**
     * The transition table with the symmetries applied
     */
    protected HashSet<ArrayList<Integer>> transitionTable;

    /**
     * A transition table that is sorted in lexicographical order (useful for deficient rules).
     * Contains all the transitions in string format (e.g. 1c, 1e, 2a, 4c, 4z, 6k...)
     */
    protected ArrayList<String> sortedTransitionTable;

    /**
     * Constructs INT transitions from the provided string
     * @param string The string representation of the INT transitions
     */
    public INTTransitions(String string) {
        transitionTable = new HashSet<>();
        sortedTransitionTable = new ArrayList<>();
    }

    /**
     * Parses transitions from the provided string
     * @param string The string representation of the INT transitions
     */
    protected abstract void parseTransitions(String string);

    /**
     * Canonises the transitions based on the currently loaded parameters
     * @return Returns the canonised transitions in the form of a string
     */
    protected abstract String canoniseTransitions();

    /**
     * Adds the provided transition to the transitions table and the sorted.
     * The various symmetries of the INT transition should be applied here.
     * @param transition The INT transition to be added
     */
    protected abstract void addTransition(ArrayList<Integer> transition);

    /**
     * Checks if the provided neighbours of the cell satisfy the INT transitions
     * @param neighbours Neighbours of the cell
     * @return Returns true if the condition is satisfied, false otherwise
     */
    public boolean checkTransition(int[] neighbours) {
        return transitionTable.contains(Arrays.asList(neighbours));
    }

    /**
     * Gets the regex for the INT transitions
     * @return Returns the regex for the INT transitions
     */
    public abstract String getRegex();

    /**
     * Gets the neighbourhood of the INT transitions
     * @return Returns the neighbourhood of the INT transitions
     */
    public Coordinate[] getNeighbourhood() {
        return neighbourhood;
    }

    /**
     * Gets the sorted transitions
     * @return Returns the sorted transitions
     */
    public ArrayList<String> getSortedTransitionTable() {
        return sortedTransitionTable;
    }

    /**
     * Makes a deep copy of the INT transitions
     * @return Returns the deep copy
     */
    protected abstract Object clone();
}
