package sample.model.patterns;

import org.javatuples.Pair;
import sample.model.Grid;
import sample.model.Simulator;
import sample.model.rules.Rule;
import sample.model.rules.RuleFamily;

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

    public void generateMinMaxRule(Grid[] grids) {
        try {
            Pair<RuleFamily, RuleFamily> minMaxRule = ((RuleFamily) getRule()).getMinMaxRule(grids);
            minRule = minMaxRule.getValue0();
            maxRule = minMaxRule.getValue1();
        }
        catch (UnsupportedOperationException exception) {}
    }

    // Returns a brief description of the pattern
    public abstract String toString();

    // Returns a dictionary of attribute: value to be displayed to the user
    public abstract Map<String, String> additionalInfo();
}
