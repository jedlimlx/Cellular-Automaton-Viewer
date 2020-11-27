package sample.model.rules.misc;

import sample.model.Coordinate;
import sample.model.Utils;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.RuleFamily;
import sample.model.rules.ruleloader.RuleDirective;
import sample.model.simulation.Grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

/**
 * Implements the alternating rules rulespace
 */
public class AlternatingRule extends RuleFamily implements ApgtableGeneratable {
    private ArrayList<RuleFamily> rules;

    /**
     * Constructs an alternating rule
     */
    public AlternatingRule() {
        name = "Alternating Rules";
    }

    /**
     * Constructs an alternating rule based on the provided rulestring
     * @param rulestring The rulestring of the alternating rule
     */
    public AlternatingRule(String rulestring) {
        name = "Alternating Rules";

        setRulestring(rulestring);
    }

    @Override
    protected void fromRulestring(String rulestring) {
        numStates = -1;
        rules = new ArrayList<>();
        for (String rule: rulestring.split("\\|")) {
            RuleFamily ruleFamily = Utils.fromRulestring(rule);
            rules.add(ruleFamily);

            if (numStates != -1) {
                if (numStates != ruleFamily.getNumStates())
                    throw new IllegalArgumentException("Alternating rules must have the same number of states!");
            } else {
                numStates = ruleFamily.getNumStates();
            }
        }

        alternatingPeriod = rules.size();
        updateBackground();

        for (RuleFamily ruleFamily: rules) {
            ruleFamily.setBackground(background);
            ruleFamily.setBoundedGrid(boundedGrid);
            ruleFamily.setReadingOrder(readingOrder);
        }
    }

    @Override
    public String canonise(String rulestring) {
        StringBuilder canon = new StringBuilder();
        for (RuleFamily ruleFamily: rules) canon.append(ruleFamily.getRulestring().split(":")[0]).append("|");

        return canon.substring(0, canon.length() - 1);
    }

    @Override
    public String[] getRegex() {
        return new String[]{".+\\|.+(\\|.*)*"};
    }

    @Override
    public String getDescription() {
        return "This implements the alternating rules rulespace.\n" +
                "B0 rules are supported.\n" +
                "The format is as follows:\n" +
                "<1st rule>|<2nd rule>|<3rd rule>...\n\n" +
                "Examples:\n" +
                "B13/S012345678|B/S15 (alternlife)";
    }

    @Override
    public Object clone() {
        if (rulestring == null)
            return new AlternatingRule();
        return new AlternatingRule(rulestring);
    }

    @Override
    public Coordinate[] getNeighbourhood(int generation) {
        return rules.get(generation % rules.size()).getNeighbourhood();
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        return rules.get(generations % rules.size()).transitionFunc(neighbours, cellState, generations, coordinate);
    }

    @Override
    public void step(Grid grid, ArrayList<Set<Coordinate>> cellsChanged, int generation,
                     Function<Coordinate, Boolean> step)
            throws IllegalArgumentException {
        rules.get(generation % rules.size()).step(grid, cellsChanged, generation, step);
    }

    @Override
    public RuleDirective[] generateApgtable() {
        ArrayList<RuleDirective> ruleDirectives = new ArrayList<>();
        for (RuleFamily ruleFamily: rules) {
            if (!(ruleFamily instanceof ApgtableGeneratable)) {
                throw new UnsupportedOperationException("Apgtable generation not supported by this rule!");
            }

            ruleDirectives.addAll(Arrays.asList(((ApgtableGeneratable) ruleFamily).generateApgtable()));
        }

        return ruleDirectives.toArray(new RuleDirective[0]);
    }
}
