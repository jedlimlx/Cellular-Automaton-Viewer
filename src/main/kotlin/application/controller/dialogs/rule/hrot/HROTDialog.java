package application.controller.dialogs.rule.hrot;

import org.javatuples.Pair;
import application.controller.NeighbourhoodSelector;
import application.controller.dialogs.rule.RuleWidget;
import application.controller.dialogs.rule.SharedWidgets;
import application.model.Coordinate;
import application.model.rules.hrot.HROT;

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