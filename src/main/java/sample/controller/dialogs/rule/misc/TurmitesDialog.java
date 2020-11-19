package sample.controller.dialogs.rule.misc;

import sample.controller.dialogs.rule.RuleWidget;
import sample.model.rules.misc.turmites.Turmites;

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
