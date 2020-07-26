package sample.controller.dialogs;

import sample.model.Coordinate;
import sample.model.rules.HROT;
import sample.controller.NeighbourhoodSelector;

import java.util.ArrayList;

public class HROTDialog extends RuleWidget {
    private final NeighbourhoodSelector neighbourhoodSelector;

    public HROTDialog() {
        super();

        neighbourhoodSelector = new NeighbourhoodSelector(2);
        super.add(neighbourhoodSelector, 0, 1);

        this.ruleFamily = new HROT("B3/S23");
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
            // Converting to array
            Coordinate[] neighbourhoodArray = new Coordinate[neighbourhood.size()];
            int[] weightsArray = new int[weights.size()];
            for (int i = 0; i < weights.size(); i++) {
                weightsArray[i] = weights.get(i);
                neighbourhoodArray[i] = neighbourhood.get(i);
            }

            // Cast to HROT because type RuleFamily has no such methods
            ((HROT) ruleFamily).setNeighbourhood(neighbourhoodArray);
            ((HROT) ruleFamily).setWeights(weightsArray);
        }
        else {
            // Set weights to null
            ((HROT) ruleFamily).setWeights(null);
        }
    }
}
