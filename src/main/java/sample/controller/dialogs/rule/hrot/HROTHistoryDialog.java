package sample.controller.dialogs.rule.hrot;

import org.javatuples.Pair;
import sample.controller.NeighbourhoodSelector;
import sample.controller.dialogs.rule.RuleWidget;
import sample.controller.dialogs.rule.SharedWidgets;
import sample.model.Coordinate;
import sample.model.rules.hrot.history.HROTHistory;

public class HROTHistoryDialog extends RuleWidget {
    private final NeighbourhoodSelector neighbourhoodSelector;

    public HROTHistoryDialog() {
        super();

        neighbourhoodSelector = SharedWidgets.getNeighbourhoodSelector();
        super.add(neighbourhoodSelector, 0, 1);

        this.ruleFamily = new HROTHistory();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);

        Pair<Coordinate[], int[]> neighbourhoodAndWeights = neighbourhoodSelector.getNeighbourhoodAndWeights();
        if (neighbourhoodAndWeights == null)
            return;

        if (neighbourhoodAndWeights.getValue0() != null) {
            ((HROTHistory) ruleFamily).setNeighbourhood(neighbourhoodAndWeights.getValue0());
            if (neighbourhoodAndWeights.getValue1() != null) {
                ((HROTHistory) ruleFamily).setWeights(neighbourhoodAndWeights.getValue1());
            }
        }
    }
}
