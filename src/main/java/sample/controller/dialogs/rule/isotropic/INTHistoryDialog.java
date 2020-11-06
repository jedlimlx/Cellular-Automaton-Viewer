package sample.controller.dialogs.rule.isotropic;

import sample.controller.dialogs.rule.RuleWidget;
import sample.model.rules.isotropic.rules.INT;
import sample.model.rules.isotropic.rules.history.INTHistory;

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
