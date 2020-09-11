package sample.controller.dialogs.rule;

import sample.model.rules.OneDimensional;

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
