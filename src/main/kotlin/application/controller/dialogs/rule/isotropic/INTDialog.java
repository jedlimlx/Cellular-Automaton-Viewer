package application.controller.dialogs.rule.isotropic;

import application.controller.dialogs.rule.RuleWidget;
import application.model.rules.isotropic.rules.INT;

public class INTDialog extends RuleWidget {
    public INTDialog() {
        super();

        ruleFamily = new INT();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);
    }
}
