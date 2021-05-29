package application.controller.dialogs.rule.misc;

import application.controller.dialogs.rule.RuleWidget;
import application.model.rules.misc.turmites.Turmites;

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
