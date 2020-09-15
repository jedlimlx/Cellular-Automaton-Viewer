package sample.controller.dialogs.rule;

import sample.model.rules.Turmites;

public class TurmitesDialog extends RuleWidget {
    public TurmitesDialog() {
        super();

        ruleFamily = new Turmites();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);
    }
}
