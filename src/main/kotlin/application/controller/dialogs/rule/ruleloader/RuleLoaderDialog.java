package application.controller.dialogs.rule.ruleloader;

import application.controller.dialogs.rule.RuleWidget;
import application.model.rules.ruleloader.RuleLoader;

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
