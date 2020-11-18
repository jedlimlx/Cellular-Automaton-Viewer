package sample.model.patterns;

import org.javatuples.Pair;
import sample.model.rules.MinMaxRuleable;
import sample.model.rules.Rule;
import sample.model.rules.RuleFamily;
import sample.model.simulation.Grid;
import sample.model.simulation.Simulator;

import java.util.Map;

public abstract class Pattern extends Simulator {
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
}
