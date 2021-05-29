package application.model.patterns;

import org.javatuples.Pair;
import application.model.rules.MinMaxRuleable;
import application.model.rules.Rule;
import application.model.rules.RuleFamily;
import application.model.simulation.Grid;
import application.model.simulation.Simulator;

import java.util.Comparator;
import java.util.Map;

public abstract class Pattern extends Simulator {
    protected String name;
    protected RuleFamily minRule, maxRule;
    
    public Pattern(Rule rule) {
        super(rule);
    }

    // Accessors
    public RuleFamily getMinRule() {
        return minRule;
    }

    public RuleFamily getMaxRule() {
        return maxRule;
    }

    public String getName() {
        return name;
    }

    // Mutators
    public void setMinRule(RuleFamily minRule) {
        this.minRule = minRule;
    }

    public void setMaxRule(RuleFamily maxRule) {
        this.maxRule = maxRule;
    }

    public void setPhases(Grid[] grids) {
        // Checking if min / max rules are supported
        if (getRule() instanceof MinMaxRuleable) {
            try {
                Pair<RuleFamily, RuleFamily> minMaxRule = ((MinMaxRuleable) getRule()).getMinMaxRule(grids);
                minRule = minMaxRule.getValue0();
                maxRule = minMaxRule.getValue1();
            } catch (UnsupportedOperationException ignored) {}
        }
    }

    // Returns a brief description of the pattern
    public abstract String toString();

    // Returns a dictionary of attribute: value to be displayed to the user
    public abstract Map<String, String> additionalInfo();

    // Sorts the patterns by their string representations
    public Comparator<String> compareString() {
        return String::compareTo;
    }
}
