package sample.model.rules.isotropic.transitions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import sample.model.Coordinate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;

/**
 * All INT transitions should inherit from this class
 */
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
    @JsonIgnore
    protected HashSet<ArrayList<Integer>> transitionTable;

    /**
     * A transition table that is sorted in lexicographical order (useful for deficient rules).
     * Contains all the transitions in string format (e.g. 1c, 1e, 2a, 4c, 4z, 6k...)
     */
    @JsonIgnore
    protected TreeSet<String> sortedTransitionTable;

    /**
     * Constructs INT transitions from the provided string
     * @param string The string representation of the INT transitions
     */
    public INTTransitions(String string) {
        transitionTable = new HashSet<>();
        sortedTransitionTable = new TreeSet<>();
    }

    /**
     * Sets the transition string of the INT transitions
     * @param string The transition string
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
    public abstract String canoniseTransitions();

    /**
     * Adds the provided transition to the transitions table
     * @param transition The INT transition to be added
     */
    public void addTransition(ArrayList<Integer> transition) {
        addTransition(getTransitionsFromNeighbours(transition));
    }

    /**
     * Remove the provided transition to the transitions table
     * @param transition The INT transition to be removed
     */
    public void removeTransition(ArrayList<Integer> transition) {
        removeTransition(getTransitionsFromNeighbours(transition));
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
    @JsonIgnore
    public abstract String getRegex();

    /**
     * Gets the INT transition from the neighbours
     * @param neighbours The neighbours of the cell
     * @return Returns the INT transition
     */
    public abstract String getTransitionsFromNeighbours(ArrayList<Integer> neighbours);

    /**
     * Adds an INT transition
     * @param transition The INT transition to add
     */
    public abstract void addTransition(String transition);

    /**
     * Removes an INT transition
     * @param transition The INT transition to remove
     */
    public abstract void removeTransition(String transition);

    /**
     * Checks if this INT transition is a subset of another
     * @param transition The INT transition to check
     * @return Returns true if the this INT transition is a subset of another, false otherwise
     */
    public boolean checkSubset(INTTransitions transition) {
        for (String string: sortedTransitionTable) {
            if (!transition.getSortedTransitionTable().contains(string)) {
                return false;
            }
        }

        return true;
    }

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
    @JsonIgnore
    public TreeSet<String> getSortedTransitionTable() {
        return sortedTransitionTable;
    }

    /**
     * Gets the transition table of the INT transition
     * @return Returns the transition table
     */
    @JsonIgnore
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

    /**
     * Generates a random INT transition between a min and max INT transition
     * @param minTransitions The minimum INT transition
     * @param maxTransitions The maximum INT transition
     * @return Returns the randomised INT transition
     */
    public static INTTransitions randomise(INTTransitions minTransitions, INTTransitions maxTransitions) {
        Random random = new Random();
        int transitionProbability = random.nextInt(500) + 250;

        INTTransitions newTransition = (INTTransitions) minTransitions.clone();
        for (String transition: maxTransitions.getSortedTransitionTable()) {
            if (!minTransitions.getSortedTransitionTable().contains(transition) &&
                    random.nextInt(1000) > transitionProbability) {
                newTransition.addTransition(transition);
            }
        }

        return newTransition;
    }

    /**
     * Generates an INT transition with the maximum number of transitions possible
     * @return Returns the INT transition with the maximum number of transitions possible
     */
    @JsonIgnore
    public INTTransitions getMaxTransition() {
        INTTransitions maxTransitions = (INTTransitions) this.clone();
        for (int i = 0; i < Math.pow(2, neighbourhood.length); i++) {
            ArrayList<Integer> transition = new ArrayList<>();
            String binaryString = Integer.toBinaryString(i);
            binaryString = "0".repeat(neighbourhood.length - binaryString.length()) + binaryString;

            for (int j = 0; j < binaryString.length(); j++)
                transition.add(Integer.parseInt(binaryString.charAt(j) + ""));

            maxTransitions.addTransition(transition);
        }

        return maxTransitions;
    }

    /**
     * Generates an INT transition with the minimum number of transitions possible
     * @return Returns the INT transition with the minimum number of transitions possible
     */
    @JsonIgnore
    public INTTransitions getMinTransition() {
        INTTransitions minTransitions = (INTTransitions) this.clone();
        minTransitions.getSortedTransitionTable().clear();
        minTransitions.getTransitionTable().clear();
        return minTransitions;
    }
}
