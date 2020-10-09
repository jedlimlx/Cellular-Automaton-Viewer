package sample.model.rules.isotropic.transitions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import sample.model.Coordinate;

import java.util.ArrayList;
import java.util.HashSet;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
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
     * Sets the transition string of the INT transitions
     * @param string THe transition string
     */
    public void setTransitionString(String string) {
        parseTransitions(string);
        transitionString = canoniseTransitions();
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
     * @param transition The INT transition to be added
     */
    protected void addTransition(ArrayList<Integer> transition) {
        transitionTable.addAll(getSymmetries(transition));
    }

    /**
     * Applies the symmetries to the required transitions
     * @param transition The transition on which the symmetries will be applied
     * @return Returns the applied symmetries
     */
    protected abstract ArrayList<ArrayList<Integer>> getSymmetries(ArrayList<Integer> transition);

    /**
     * Checks if the provided neighbours of the cell satisfy the INT transitions
     * @param neighbours Neighbours of the cell
     * @return Returns true if the condition is satisfied, false otherwise
     */
    public boolean checkTransition(int[] neighbours) {
        ArrayList<Integer> neighboursList = new ArrayList<>();
        for (int neighbour: neighbours) {
            neighboursList.add(neighbour);
        }

        return transitionTable.contains(neighboursList);
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
     * Gets the transition table of the INT transition
     * @return Returns the transition table
     */
    public HashSet<ArrayList<Integer>> getTransitionTable() {
        return transitionTable;
    }

    /**
     * Gets the transition string of the INT transitions
     * @return Returns the transition string of the INT transitions
     */
    public String getTransitionString() {
        return transitionString;
    }

    /**
     * Makes a deep copy of the INT transitions
     * @return Returns the deep copy
     */
    public abstract Object clone();
}
