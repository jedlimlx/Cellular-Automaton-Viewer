package application.controller.dialogs.rule.isotropic

import application.controller.dialogs.rule.RuleWidget
import application.model.rules.isotropic.rules.history.INTHistory

class INTHistoryDialog : RuleWidget() {
    override fun updateRule(rulestring: String?) {
        ruleFamily!!.rulestring = rulestring
    }

    init {
        ruleFamily = INTHistory()
    }
}