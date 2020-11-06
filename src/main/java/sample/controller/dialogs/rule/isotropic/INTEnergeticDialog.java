package sample.controller.dialogs.rule.isotropic;

import sample.controller.dialogs.rule.RuleWidget;
import sample.model.rules.isotropic.rules.energetic.INTEnergetic;
import sample.model.rules.isotropic.rules.history.INTHistory;

public class INTEnergeticDialog extends RuleWidget {
    public INTEnergeticDialog() {
        super();

        ruleFamily = new INTEnergetic();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);
    }
}
