package sample.model.rules;

import org.javatuples.Pair;
import sample.model.Coordinate;
import sample.model.Grid;

import java.io.File;
import java.util.ArrayList;

/**
 * Represents a family of rules or a rulespace
 */
public abstract class RuleFamily extends Rule implements Cloneable {
    /**
     * Name of the Rule Family (displayed in the RuleDialog)
     */
    protected String name;

    /**
     * Rulestring of the Rule Family
     */
    protected String rulestring;

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the rule (eg. B3/S23, R2,C2,S5-9,B7-8,NM)
     * @throws IllegalArgumentException Thrown if an invalid rulestring is passed in
     */
    protected abstract void fromRulestring(String rulestring);

    /**
     * Canonises the inputted rulestring with the currently loaded parameters.
     * This method should be called whenever the parameters of a rule are updated.
     * @param rulestring The rulestring to canonised
     * @return Canonised rulestring
     */
    public abstract String canonise(String rulestring);

    /**
     * Sets the rulestring of the rule family to the inputted value
     * @param rulestring Rulestring of the rule
     */
    public void setRulestring(String rulestring) {
        fromRulestring(rulestring);
        this.rulestring = canonise(rulestring);
    }

    /**
     * Updates the background of the rule based on the loaded parameters.
     * This method should be called whenever the parameters of a rule are updated.
     * For non-strobing rules, the background is {0}.
     */
    public abstract void updateBackground();

    /**
     * The regexes that will match a valid rulestring
     * @return An array of regexes that will match a valid rulestring
     */
    public abstract String[] getRegex();

    /**
     * Returns a plain text description of the rule family to be displayed in the Rule Dialog
     * @return Description of the rule family
     */
    public abstract String getDescription();

    /**
     * Randomise the parameters of the current rule to be between minimum and maximum rules
     * Used in CAViewer's rule search program
     * @param minRule The minimum rule for randomisation
     * @param maxRule The maximum rule for randomisation
     * @throws IllegalArgumentException Thrown if the minimum and maximum rules are invalid
     * @throws UnsupportedOperationException Thrown if the specific rule does not support randomisation
     */
    public void randomise(RuleFamily minRule, RuleFamily maxRule) throws IllegalArgumentException, UnsupportedOperationException {
    }

    /**
     * Returns the minimum and maximum rule of the provided evolutionary sequence.
     * This is used in CAViewer's rule search program and identification.
     * The first grid in the list is taken to be at generation 0.
     * @param grids An array of grids representing the evolutionary sequence
     * @return A pair containing the min rule as the first value and the max rule as the second value
     * @throws UnsupportedOperationException Thrown if the specific rule does not support minimum and maximum rules
     */
    public Pair<RuleFamily, RuleFamily> getMinMaxRule(Grid[] grids) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Minimum and maximum rules are not supported for " +
                "this rule family");
    }

    /**
     * Checks if the current rule is between the given minimum and maximum rules
     * A child class the implements this method is compatible with CAViewer's rule search program
     * @param minRule The minimum rule
     * @param maxRule The maximum rule
     * @return True if the current rule is between minimum and maximum rules and false
     * if the current rule is not between the minimum and maximum rules
     * @throws IllegalArgumentException Thrown if the minimum rule and maximum rule are invalid
     * @throws UnsupportedOperationException Thrown if the current rule does not support minimum and maximum rules
     */
    public boolean betweenMinMax(RuleFamily minRule, RuleFamily maxRule) throws IllegalArgumentException,
            UnsupportedOperationException {
        throw new UnsupportedOperationException("Minimum and maximum rules are not supported for " +
                "this rule family");
    }

    /**
     * Checks if the minimum rule and maximum rules provided are valid
     * A child class the implements this method is compatible with CAViewer's rule search program
     * @param minRule The minimum rule to check
     * @param maxRule The maximum rule to check
     * @return True if the minimum and maximum rules are valid and false if the minimum and maximum rules are not valid
     */
    public boolean validMinMax(RuleFamily minRule, RuleFamily maxRule) {
        throw new UnsupportedOperationException("Minimum and maximum rules are not supported for " +
                "this rule family");
    }

    /**
     * Generates an apgtable for apgsearch to use
     * @param file The file to save the apgtable in
     * @return True if the operation was successful, false otherwise
     * @throws UnsupportedOperationException Thrown if apgtable generation for that specific rule is not supported
     */
    public boolean generateApgtable(File file) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Apgtable generation is not supported for this rule family");
    }

    /**
     * Generates comments that will be placed in the RLE.
     * These comments represent additional information that is not stored in the rulestring (e.g. weights)
     * @return An array of comments each starting with "#R" (eg. {"#R 1 2 3 2 1", "#R 2 4 6 4 2"}).
     * If no additional information needs to be added return null or an empty string array.
     */
    public abstract String[] generateComments();

    /**
     * Loads the additional information stored in the comments generated by generateComments
     * An empty array maybe passed in (meaning no comments)
     * @param comments The comments from the RLE (all starting with #R)
     */
    public abstract void loadComments(String[] comments);

    /**
     * Clones the object
     * @return A deepcopy of the object
     */
    @Override
    public abstract Object clone();

    /**
     * Gets the name of the rule
     * @return Name of the rule
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the rulestring of the rule
     * @return Rulestring of the rule
     */
    public String getRulestring() {
        return rulestring;
    }

    @Override
    public String toString() {
        return getRulestring();
    }

    /**
     * Gets the list of neighbours with the input state and output state at the first and last index respectively
     * when provided with an evolutionary sequence. Should be used in conjuction with getMinMaxRule.
     * @param grids An array of grids representing an evolutionary sequence
     * @return Returns the list of neighbours
     */
    protected ArrayList<int[]> getNeighbourList(Grid[] grids) {
        ArrayList<int[]> neighboursList = new ArrayList<>();

        // Running through every generation and check what transitions are required
        for (int i = 0; i < grids.length - 1; i++) {
            grids[i].updateBounds();  // Getting the bounds of the grid
            Pair<Coordinate, Coordinate> bounds = grids[i].getBounds();

            Coordinate coordinate;  // Current coordinate
            Coordinate[] neighbourhood = getNeighbourhood(i);

            // Inverting neighbourhood for triangular rules
            Coordinate[] invertedNeighbourhood = new Coordinate[neighbourhood.length];
            if (tiling == Tiling.Triangular) {
                for (int j = 0; j < neighbourhood.length; j++) {
                    invertedNeighbourhood[j] = new Coordinate(neighbourhood[j].getX(), -neighbourhood[j].getY());
                }
            }

            for (int x = bounds.getValue0().getX() - 5; x < bounds.getValue1().getX() + 5; x++) {
                for (int y = bounds.getValue0().getY() - 5; y < bounds.getValue1().getY() + 5; y++) {
                    coordinate = new Coordinate(x, y);
                    if (dependsOnNeighbours(grids[i].getCell(coordinate)) != -1) continue;

                    // Computes the neighbourhood sum for every cell
                    int[] neighbours = new int[getNeighbourhood(i).length + 2];
                    if (tiling != Tiling.Triangular ||
                            Math.floorMod(coordinate.getX(), 2) == Math.floorMod(coordinate.getY(), 2)) {
                        for (int j = 0; j < neighbourhood.length; j++) {
                            neighbours[j + 1] = grids[i].getCell(coordinate.add(neighbourhood[j]));
                        }
                    }
                    else {
                        for (int j = 0; j < neighbourhood.length; j++) {
                            neighbours[j + 1] = grids[i].getCell(coordinate.add(invertedNeighbourhood[j]));
                        }
                    }

                    neighbours[0] = grids[i].getCell(coordinate);
                    neighbours[neighbourhood.length + 1] = grids[i + 1].getCell(coordinate);

                    neighboursList.add(neighbours);
                }
            }
        }

        return neighboursList;
    }
}
