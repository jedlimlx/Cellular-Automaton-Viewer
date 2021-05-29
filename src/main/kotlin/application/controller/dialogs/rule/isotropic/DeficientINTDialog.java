package application.controller.dialogs.rule.isotropic;

import application.controller.dialogs.rule.RuleWidget;
import application.model.rules.isotropic.rules.DeficientINT;

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
