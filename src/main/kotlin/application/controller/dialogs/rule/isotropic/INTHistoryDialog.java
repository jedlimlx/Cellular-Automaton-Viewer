package application.controller.dialogs.rule.isotropic;

import application.controller.dialogs.rule.RuleWidget;
import application.model.rules.isotropic.rules.history.INTHistory;

public class INTHistoryDialog extends RuleWidget {
    public INTHistoryDialog() {
        super();

        ruleFamily = new INTHistory();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);
    }
}
