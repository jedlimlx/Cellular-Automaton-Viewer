package sample.controller.dialogs.rule.misc;

import sample.controller.dialogs.rule.RuleWidget;
import sample.model.rules.misc.AlternatingRule;

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
