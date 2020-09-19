package sample.controller.dialogs.rule.hrot;

import org.javatuples.Pair;
import sample.controller.NeighbourhoodSelector;
import sample.controller.dialogs.rule.RuleWidget;
import sample.controller.dialogs.rule.SharedWidgets;
import sample.model.Coordinate;
import sample.model.rules.hrot.DeficientHROT;

public class DeficientHROTDialog extends RuleWidget {
    private final NeighbourhoodSelector neighbourhoodSelector;

    public DeficientHROTDialog() {
        super();

        neighbourhoodSelector = SharedWidgets.getNeighbourhoodSelector();
        super.add(neighbourhoodSelector, 0, 1);

        this.ruleFamily = new DeficientHROT();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);

        Pair<Coordinate[], int[]> neighbourhoodAndWeights = neighbourhoodSelector.getNeighbourhoodAndWeights();
        if (neighbourhoodAndWeights == null)
            return;

        if (neighbourhoodAndWeights.getValue0() != null) {
            ((DeficientHROT) ruleFamily).setNeighbourhood(neighbourhoodAndWeights.getValue0());
            if (neighbourhoodAndWeights.getValue1() != null) {
                ((DeficientHROT) ruleFamily).setWeights(neighbourhoodAndWeights.getValue1());
            }
        }
    }
}
