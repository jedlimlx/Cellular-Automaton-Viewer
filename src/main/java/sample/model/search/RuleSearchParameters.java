package sample.model.search;

import sample.model.rules.RuleFamily;
import sample.model.simulation.Grid;

public class RuleSearchParameters extends SearchParameters {
    private final RuleFamily minRule;
    private final RuleFamily maxRule;
    private final int maxPeriod;
    private final Grid targetPattern;

    public RuleSearchParameters(Grid pattern, RuleFamily minRule, RuleFamily maxRule, int maxPeriod) {
        this.minRule = minRule;
        this.maxRule = maxRule;
        this.targetPattern = pattern;
        this.maxPeriod = maxPeriod;
    }

    // Accessors
    public Grid getTargetPattern() {
        return targetPattern;
    }

    public int getMaxPeriod() {
        return maxPeriod;
    }

    public RuleFamily getMinRule() {
        return minRule;
    }

    public RuleFamily getMaxRule() {
        return maxRule;
    }
}
