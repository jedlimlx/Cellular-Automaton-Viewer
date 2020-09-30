package sample.controller.dialogs.rule.isotropic;

import sample.controller.dialogs.rule.RuleWidget;
import sample.model.rules.isotropic.rules.INT;

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
