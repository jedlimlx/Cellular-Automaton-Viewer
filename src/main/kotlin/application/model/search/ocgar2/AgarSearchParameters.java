package application.model.search.ocgar2;

import application.model.rules.Rule;
import application.model.search.SearchParameters;

public class AgarSearchParameters extends SearchParameters {
    private Rule rule;
    private int maxPeriod;

    public AgarSearchParameters(Rule rule, int maxPeriod) {
        this.rule = rule;
        this.maxPeriod = maxPeriod;
    }

    public Rule getRule() {
        return rule;
    }

    public int getMaxPeriod() {
        return maxPeriod;
    }
}
