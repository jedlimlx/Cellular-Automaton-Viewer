package sample.controller.dialogs.rule.isotropic;

import sample.controller.dialogs.rule.RuleWidget;
import sample.model.rules.isotropic.rules.INTGenerations;

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
