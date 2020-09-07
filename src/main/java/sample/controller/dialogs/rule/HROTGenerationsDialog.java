package sample.controller.dialogs.rule;

import org.javatuples.Pair;
import sample.controller.NeighbourhoodSelector;
import sample.model.Coordinate;
import sample.model.rules.HROTGenerations;

public class HROTGenerationsDialog extends RuleWidget {
    private final NeighbourhoodSelector neighbourhoodSelector;

    public HROTGenerationsDialog() {
        super();

        neighbourhoodSelector = SharedWidgets.getNeighbourhoodSelector();
        super.add(neighbourhoodSelector, 0, 1);

        this.ruleFamily = new HROTGenerations("R1,C3,S3-5,B2,NM");
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);

        Pair<Coordinate[], int[]> neighbourhoodAndWeights = neighbourhoodSelector.getNeighbourhoodAndWeights();
        if (neighbourhoodAndWeights == null)
            return;

        if (neighbourhoodAndWeights.getValue0() != null) {
            ((HROTGenerations) ruleFamily).setNeighbourhood(neighbourhoodAndWeights.getValue0());
            if (neighbourhoodAndWeights.getValue1() != null) {
                ((HROTGenerations) ruleFamily).setWeights(neighbourhoodAndWeights.getValue1());
            }
        }
    }
}
