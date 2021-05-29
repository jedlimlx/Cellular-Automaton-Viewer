package application.controller.dialogs.rule.misc;

import application.controller.dialogs.rule.RuleWidget;
import application.model.rules.misc.Margolus;

public class MargolusDialog extends RuleWidget {
    public MargolusDialog() {
        super();

        ruleFamily = new Margolus();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);
    }
}
