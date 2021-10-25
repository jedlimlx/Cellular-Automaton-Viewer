package application.controller.dialogs.rule.isotropic

import application.controller.dialogs.rule.RuleWidget
import application.model.rules.isotropic.rules.DeficientINT

class DeficientINTDialog : RuleWidget() {
    override fun updateRule(rulestring: String?) {
        ruleFamily!!.rulestring = rulestring
    }

    init {
        ruleFamily = DeficientINT()
    }
}