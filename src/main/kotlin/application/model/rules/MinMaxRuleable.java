package application.model.rules;

import org.javatuples.Pair;
import application.model.simulation.Grid;

/**
 * Rule families that support minimum, maximum rules implement this interface
 */
public interface MinMaxRuleable {
    /**
     * Randomise the parameters of the current rule to be between minimum and maximum rules
     * Used in CAViewer's rule search program
     * @param minRule The minimum rule for randomisation
     * @param maxRule The maximum rule for randomisation
     * @throws IllegalArgumentException Thrown if the minimum and maximum rules are invalid
     * @throws UnsupportedOperationException Thrown if the specific rule does not support randomisation
     */
    void randomise(RuleFamily minRule, RuleFamily maxRule);

    /**
     * Returns the minimum and maximum rule of the provided evolutionary sequence.
     * This is used in CAViewer's rule search program and identification.
     * The first grid in the list is taken to be at generation 0.
     * @param grids An array of grids representing the evolutionary sequence
     * @return A pair containing the min rule as the first value and the max rule as the second value
     * @throws UnsupportedOperationException Thrown if the specific rule does not support minimum and maximum rules
     */
    Pair<RuleFamily, RuleFamily> getMinMaxRule(Grid[] grids);

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
    boolean betweenMinMax(RuleFamily minRule, RuleFamily maxRule);

    /**
     * Checks if the minimum rule and maximum rules provided are valid
     * A child class the implements this method is compatible with CAViewer's rule search program
     * @param minRule The minimum rule to check
     * @param maxRule The maximum rule to check
     * @return True if the minimum and maximum rules are valid and false if the minimum and maximum rules are not valid
     */
    boolean validMinMax(RuleFamily minRule, RuleFamily maxRule);
}
