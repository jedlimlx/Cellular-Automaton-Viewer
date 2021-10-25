package application.controller.dialogs.rule.misc

import application.controller.dialogs.rule.RuleWidget
import application.model.rules.misc.AlternatingRule

class AlternatingDialog : RuleWidget() {
    override fun updateRule(rulestring: String?) {
        ruleFamily!!.rulestring = rulestring
    }

    init {
        ruleFamily = AlternatingRule()
    }
}