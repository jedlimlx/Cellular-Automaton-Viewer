package application.controller.dialogs.rule

import application.model.rules.RuleFamily
import javafx.geometry.Pos
import javafx.scene.layout.GridPane

abstract class RuleWidget : GridPane() {
    var ruleFamily: RuleFamily? = null

    // Updates the rule with the necessary information
    abstract fun updateRule(rulestring: String?)
    override fun toString(): String {
        return ruleFamily!!.name
    }

    init {
        // Formatting the grid
        super.setHgap(5.0)
        super.setVgap(5.0)
        super.setAlignment(Pos.CENTER)
    }
}