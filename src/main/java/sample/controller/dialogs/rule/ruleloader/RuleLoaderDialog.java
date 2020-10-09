package sample.controller.dialogs.rule.ruleloader;

import sample.controller.dialogs.rule.RuleWidget;
import sample.model.rules.misc.OneDimensional;
import sample.model.rules.ruleloader.RuleLoader;

public class RuleLoaderDialog extends RuleWidget {
    public RuleLoaderDialog() {
        super();

        ruleFamily = new RuleLoader();
    }

    @Override
    public void updateRule(String rulestring) {
        ruleFamily.setRulestring(rulestring);
    }
}
