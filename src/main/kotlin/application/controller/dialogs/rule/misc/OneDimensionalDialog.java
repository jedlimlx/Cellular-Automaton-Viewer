package application.controller.dialogs.rule.misc;

import application.controller.dialogs.rule.RuleWidget;
import application.model.rules.misc.OneDimensional;

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
