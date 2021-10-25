package application.controller.dialogs.rule.ruleloader

import application.controller.dialogs.rule.RuleWidget
import application.model.rules.ruleloader.RuleLoader

class RuleLoaderDialog : RuleWidget() {
    override fun updateRule(rulestring: String?) {
        ruleFamily!!.rulestring = rulestring
    }

    init {
        ruleFamily = RuleLoader()
    }
}