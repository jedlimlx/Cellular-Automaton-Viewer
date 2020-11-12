package sample.model.search.rulesrc;

import sample.model.rules.RuleFamily;
import sample.model.search.SearchParameters;
import sample.model.simulation.Grid;

public class RuleSearchParameters extends SearchParameters {
    private final RuleFamily minRule;
    private final RuleFamily maxRule;

    private final int maxPeriod;
    private final int maxPop, minPop;
    private final int maxX, maxY;
    private final Grid targetPattern;

    public RuleSearchParameters(Grid pattern, RuleFamily minRule, RuleFamily maxRule, int maxPeriod,
                                int minPop, int maxPop, int maxX, int maxY) {
        this.minRule = minRule;
        this.maxRule = maxRule;
        this.targetPattern = pattern;
        this.maxPeriod = maxPeriod;
        this.minPop = minPop;
        this.maxPop = maxPop;
        this.maxX = maxX;
        this.maxY = maxY;
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

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMinPop() {
        return minPop;
    }

    public int getMaxPop() {
        return maxPop;
    }
}
