package application.controller.dialogs.rule.hrot

import application.controller.NeighbourhoodSelector
import application.controller.dialogs.rule.RuleWidget
import application.controller.dialogs.rule.SharedWidgets
import application.model.rules.hrot.IntegerHROT

class IntegerHROTDialog : RuleWidget() {
    private val neighbourhoodSelector: NeighbourhoodSelector = SharedWidgets.neighbourhoodSelector

    init {
        super.add(neighbourhoodSelector, 0, 1)
        ruleFamily = IntegerHROT()
    }

    override fun updateRule(rulestring: String?) {
        ruleFamily!!.rulestring = rulestring
        val neighbourhoodAndWeights = neighbourhoodSelector.neighbourhoodAndWeights
            ?: return
        if (neighbourhoodAndWeights.value0 != null) {
            (ruleFamily as IntegerHROT).neighbourhood = neighbourhoodAndWeights.value0
            if (neighbourhoodAndWeights.value1 != null) {
                (ruleFamily as IntegerHROT).weights = neighbourhoodAndWeights.value1
            }
        }
    }
}