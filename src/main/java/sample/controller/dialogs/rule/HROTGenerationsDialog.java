package sample.controller.dialogs.rule;

import sample.controller.NeighbourhoodSelector;
import sample.model.Coordinate;
import sample.model.rules.HROTGenerations;

import java.util.ArrayList;

public class HROTGenerationsDialog extends RuleWidget {
    private final NeighbourhoodSelector neighbourhoodSelector;

    public HROTGenerationsDialog() {
        super();

        neighbourhoodSelector = new NeighbourhoodSelector(2);
        super.add(neighbourhoodSelector, 0, 1);

        this.ruleFamily = new HROTGenerations("R1,C3,S3-5,B2,NM");
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);

        // Getting neighbourhood and weights
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        ArrayList<Integer> weights = new ArrayList<>();
        for (int i = 0; i < 2 * 2 + 1; i++) {
            for (int j = 0; j < 2 * 2 + 1; j++) {
                if (neighbourhoodSelector.getWeights()[i][j] != 0) {
                    neighbourhood.add(new Coordinate(i - 2, j - 2));
                    weights.add(neighbourhoodSelector.getWeights()[i][j]);
                }
            }
        }

        if (!neighbourhood.isEmpty()) {
            boolean weightsNeeded = false;

            // Converting to array
            Coordinate[] neighbourhoodArray = new Coordinate[neighbourhood.size()];
            int[] weightsArray = new int[weights.size()];
            for (int i = 0; i < weights.size(); i++) {
                weightsArray[i] = weights.get(i);
                neighbourhoodArray[i] = neighbourhood.get(i);

                // Check if weights are needed
                if (weights.get(i) != 0 && weights.get(i) != 1)
                    weightsNeeded = true;
            }

            // Cast to HROT Generations because type RuleFamily has no such methods
            ((HROTGenerations) ruleFamily).setNeighbourhood(neighbourhoodArray);

            if (weightsNeeded)  // Check if weights are needed. If they are not needed, set weights to null
                ((HROTGenerations) ruleFamily).setWeights(weightsArray);
            else
                ((HROTGenerations) ruleFamily).setWeights(null);
        }
    }
}
