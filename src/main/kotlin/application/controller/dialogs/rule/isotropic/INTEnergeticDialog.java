package application.controller.dialogs.rule.isotropic;

import application.controller.dialogs.rule.RuleWidget;
import application.model.rules.isotropic.rules.energetic.INTEnergetic;

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
