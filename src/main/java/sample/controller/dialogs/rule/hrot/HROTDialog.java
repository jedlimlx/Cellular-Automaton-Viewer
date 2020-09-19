package sample.controller.dialogs.rule.hrot;

import org.javatuples.Pair;
import sample.controller.NeighbourhoodSelector;
import sample.controller.dialogs.rule.RuleWidget;
import sample.controller.dialogs.rule.SharedWidgets;
import sample.model.Coordinate;
import sample.model.rules.hrot.HROT;

public class HROTDialog extends RuleWidget {
    private final NeighbourhoodSelector neighbourhoodSelector;

    public HROTDialog() {
        super();

        neighbourhoodSelector = SharedWidgets.getNeighbourhoodSelector();
        super.add(neighbourhoodSelector, 0, 1);

        this.ruleFamily = new HROT("B3/S23");
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);

        Pair<Coordinate[], int[]> neighbourhoodAndWeights = neighbourhoodSelector.getNeighbourhoodAndWeights();
        if (neighbourhoodAndWeights == null)
            return;

        if (neighbourhoodAndWeights.getValue0() != null) {
            ((HROT) ruleFamily).setNeighbourhood(neighbourhoodAndWeights.getValue0());
            if (neighbourhoodAndWeights.getValue1() != null) {
                ((HROT) ruleFamily).setWeights(neighbourhoodAndWeights.getValue1());
            }
        }
    }
}
