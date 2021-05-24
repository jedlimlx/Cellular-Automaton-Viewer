package sample.controller.dialogs.rule.misc;

import sample.controller.dialogs.rule.RuleWidget;
import sample.model.rules.misc.Euclidean;
import sample.model.rules.misc.Margolus;

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
