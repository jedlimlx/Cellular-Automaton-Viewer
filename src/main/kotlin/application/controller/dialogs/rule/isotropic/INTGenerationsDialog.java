package application.controller.dialogs.rule.isotropic;

import application.controller.dialogs.rule.RuleWidget;
import application.model.rules.isotropic.rules.INTGenerations;

public class INTGenerationsDialog extends RuleWidget {
    public INTGenerationsDialog() {
        super();

        ruleFamily = new INTGenerations();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);
    }
}
