package application.model.search.csearch;

import application.model.rules.Rule;
import application.model.search.SearchParameters;

import java.util.List;

public class BruteForceSearchParameters extends SearchParameters {
    private final Rule rule;

    private final boolean random;
    private final int maxPeriod, xBound, yBound;

    private final String symmetry;
    private final List<Integer> statesToInclude;
    private final int density;

    public BruteForceSearchParameters(Rule rule, int maxPeriod, int xBound, int yBound, boolean random,
                                      String symmetry, List<Integer> statesToInclude, int density) {
        this.rule = rule;
        this.maxPeriod = maxPeriod;
        this.xBound = xBound;
        this.yBound = yBound;
        this.random = random;
        this.symmetry = symmetry;
        this.statesToInclude = statesToInclude;
        this.density = density;

        if (!random && !symmetry.equals("C1"))
            throw new IllegalArgumentException("Brute force search only supports C1 symmetry!");
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

    public String getSymmetry() {
        return symmetry;
    }

    public int[] getStatesToInclude() {
        int[] states = new int[statesToInclude.size()];
        for (int i = 0; i < statesToInclude.size(); i++)
            states[i] = statesToInclude.get(i);

        return states;
    }

    public int getDensity() {
        return density;
    }
}
