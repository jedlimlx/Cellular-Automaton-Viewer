package sample.controller.dialogs.rule.misc;

import sample.controller.dialogs.rule.RuleWidget;
import sample.model.rules.misc.AlternatingRule;
import sample.model.rules.misc.Euclidean;

public class EuclideanDialog extends RuleWidget {
    public EuclideanDialog() {
        super();

        ruleFamily = new Euclidean();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);
    }
}
