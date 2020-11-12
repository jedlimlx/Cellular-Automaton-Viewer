package sample.model.search.csearch;

import sample.model.rules.Rule;
import sample.model.search.SearchParameters;

public class BruteForceSearchParameters extends SearchParameters {
    private final Rule rule;

    private final boolean random;
    private final int maxPeriod, xBound, yBound;

    public BruteForceSearchParameters(Rule rule, int maxPeriod, int xBound, int yBound, boolean random) {
        this.rule = rule;
        this.maxPeriod = maxPeriod;
        this.xBound = xBound;
        this.yBound = yBound;
        this.random = random;
    }

    public Rule getRule() {
        return rule;
    }

    public int getMaxPeriod() {
        return maxPeriod;
    }

    public int getxBound() {
        return xBound;
    }

    public int getyBound() {
        return yBound;
    }

    public boolean isRandom() {
        return random;
    }
}
