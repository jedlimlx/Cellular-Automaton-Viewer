package application.controller.dialogs.rule.hrot;

import org.javatuples.Pair;
import application.controller.NeighbourhoodSelector;
import application.controller.dialogs.rule.RuleWidget;
import application.controller.dialogs.rule.SharedWidgets;
import application.model.Coordinate;
import application.model.rules.hrot.HROTExtendedGenerations;

public class HROTExtendedGenerationsDialog extends RuleWidget {
    private final NeighbourhoodSelector neighbourhoodSelector;

    public HROTExtendedGenerationsDialog() {
        super();

        neighbourhoodSelector = SharedWidgets.getNeighbourhoodSelector();
        super.add(neighbourhoodSelector, 0, 1);

        this.ruleFamily = new HROTExtendedGenerations();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);

        Pair<Coordinate[], int[]> neighbourhoodAndWeights = neighbourhoodSelector.getNeighbourhoodAndWeights();
        if (neighbourhoodAndWeights == null)
            return;

        if (neighbourhoodAndWeights.getValue0() != null) {
            ((HROTExtendedGenerations) ruleFamily).setNeighbourhood(neighbourhoodAndWeights.getValue0());
            if (neighbourhoodAndWeights.getValue1() != null) {
                ((HROTExtendedGenerations) ruleFamily).setWeights(neighbourhoodAndWeights.getValue1());
            }
        }
    }
}
