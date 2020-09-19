package sample.controller.dialogs.rule.misc;

import sample.controller.dialogs.rule.RuleWidget;
import sample.model.rules.misc.OneDimensional;

public class OneDimensionalDialog extends RuleWidget {
    public OneDimensionalDialog() {
        super();

        ruleFamily = new OneDimensional();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);
    }
}
