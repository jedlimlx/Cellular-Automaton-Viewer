package sample.controller.dialogs.rule.isotropic;

import sample.controller.dialogs.rule.RuleWidget;
import sample.model.rules.isotropic.rules.DeficientINT;
import sample.model.rules.isotropic.rules.INT;

public class DeficientINTDialog extends RuleWidget {
    public DeficientINTDialog() {
        super();

        ruleFamily = new DeficientINT();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);
    }
}
