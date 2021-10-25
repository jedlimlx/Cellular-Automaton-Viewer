package application.controller.dialogs.rule.hrot

import application.controller.NeighbourhoodSelector
import application.controller.dialogs.rule.RuleWidget
import application.controller.dialogs.rule.SharedWidgets
import application.model.rules.hrot.MultistateCyclicHROT

class MultistateCyclicHROTDialog : RuleWidget() {
    private val neighbourhoodSelector: NeighbourhoodSelector = SharedWidgets.neighbourhoodSelector

    init {
        super.add(neighbourhoodSelector, 0, 1)
        ruleFamily = MultistateCyclicHROT()
    }

    override fun updateRule(rulestring: String?) {
        ruleFamily!!.rulestring = rulestring
        val neighbourhoodAndWeights = neighbourhoodSelector.neighbourhoodAndWeights
            ?: return
        if (neighbourhoodAndWeights.value0 != null) {
            (ruleFamily as MultistateCyclicHROT).neighbourhood = neighbourhoodAndWeights.value0
            if (neighbourhoodAndWeights.value1 != null) {
                (ruleFamily as MultistateCyclicHROT).weights = neighbourhoodAndWeights.value1
            }
        }
    }
}