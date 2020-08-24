package sample.model.rules;

import org.javatuples.Pair;
import sample.model.Grid;

import java.io.File;

/**
 * Represents a family of rules or a rulespace
 * @author Lemon41625
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
    public abstract void fromRulestring(String rulestring);

    /**
     * Canonises the inputted rulestring with the currently loaded parameters.
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
     * Returns the minimum and maximum rule of the provided evolutionary sequence
     * A child class the implements this method is compatible with CAViewer's rule search program
     * If you're lazy, just return the min and max rule as the same rule
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
}
