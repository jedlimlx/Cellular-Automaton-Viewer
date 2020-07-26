package sample.model.search;

import sample.model.*;

public class RuleSearch {
    private final Grid targetPattern;
    private Simulator simulator;
    private final RuleFamily minRule;
    private final RuleFamily maxRule;
    private final int STABLISATION_TIME, MAX_PERIOD;

    public RuleSearch(Grid pattern, RuleFamily minRule, RuleFamily maxRule) {
        this(pattern, minRule, maxRule, 5, 30);
    }

    public RuleSearch(Grid pattern, RuleFamily minRule, RuleFamily maxRule,
                      int STABLISATION_TIME, int MAX_PERIOD) {
        this.minRule = minRule;
        this.maxRule = maxRule;
        this.targetPattern = pattern.deepCopy();
        this.STABLISATION_TIME = STABLISATION_TIME;
        this.MAX_PERIOD = MAX_PERIOD;
    }

    public void search(int numRules) {
        // TODO (Improve output format)
        for (int i = 0; i < numRules; i++) {
            // Create a new simulator object each time
            simulator = new Simulator((Rule) minRule.clone());
            simulator.insertCells(targetPattern, new Coordinate(0, 0));

            // Randomise the rule
            ((RuleFamily) simulator.getRule()).randomise(minRule, maxRule);

            // Give it time to stabilise
            for (int j = 0; j < STABLISATION_TIME; j++) {
                simulator.step();
            }

            // Identify the object
            String result = simulator.identify(MAX_PERIOD);
            if (!result.equals("Identification Failed :(") && !result.equals("Still Life")) {
                System.out.println(result);
                System.out.println(((RuleFamily) simulator.getRule()).canonise(minRule.getRulestring()));
            }

            // Update user on the number of rules checked
            if (i % 100 == 0) {
                System.out.println(i + " rules checked!");
            }
        }
    }
}
