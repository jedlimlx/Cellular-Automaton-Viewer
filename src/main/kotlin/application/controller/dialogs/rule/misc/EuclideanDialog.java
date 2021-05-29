package application.controller.dialogs.rule.misc;

import application.controller.dialogs.rule.RuleWidget;
import application.model.rules.misc.Euclidean;

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
