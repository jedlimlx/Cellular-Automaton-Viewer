package application.controller.dialogs.rule.misc;

import application.controller.dialogs.rule.RuleWidget;
import application.model.rules.misc.AlternatingRule;

public class AlternatingDialog extends RuleWidget {
    public AlternatingDialog() {
        super();

        ruleFamily = new AlternatingRule();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);
    }
}
