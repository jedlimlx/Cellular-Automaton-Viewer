package application.controller.dialogs.search

import application.controller.MainController
import application.model.patterns.Pattern
import application.model.rules.RuleFamily
import application.model.search.SearchProgram
import application.model.search.rulesrc.RuleSearchParameters
import javafx.event.EventHandler
import javafx.scene.control.MenuItem

class RuleSearchResultsDialog(mainController: MainController, searchProgram: SearchProgram):
    SearchResultsDialog(mainController, searchProgram) {

    init {
        super.setTitle("Rule Search Results")

        // Displays the min rule of the pattern in the application
        val showMinRule = MenuItem("Show Min Rule in Application")
        showMinRule.onAction = EventHandler { loadMinRule() }
        menu.items.add(1, showMinRule)

        // Displays the max rule of the pattern in the application
        val showMaxRule = MenuItem("Show Max Rule in Application")
        showMaxRule.onAction = EventHandler { loadMaxRule() }
        menu.items.add(2, showMaxRule)
    }

    override val selectedRLE: String
        get() {
            val pattern = PatternsDialog.selected!!.pattern as Pattern
            return getSelectedRLE(pattern.rule as RuleFamily)
        }

    fun loadMinRule() {
        val pattern = PatternsDialog.selected!!.pattern as Pattern
        mainController.loadPattern(getSelectedRLE(pattern.minRule))
    }

    fun loadMaxRule() {
        val pattern = PatternsDialog.selected!!.pattern as Pattern
        mainController.loadPattern(getSelectedRLE(pattern.maxRule))
    }

    fun getSelectedRLE(rule: RuleFamily): String {
        val targetPattern = (searchProgram.searchParameters as RuleSearchParameters).targetPattern
        targetPattern.updateBounds()

        val width = targetPattern.bounds.value1.subtract(targetPattern.bounds.value0).x + 2
        val height = targetPattern.bounds.value1.subtract(targetPattern.bounds.value0).y + 2

        var rle = "x = $width, y = $height, rule = ${rule.rulestring}\n" // Header
        rle += targetPattern.toRLE(
            targetPattern.bounds.value0,
            targetPattern.bounds.value1
        ) // Body
        return rle
    }

    override fun getAdditionalInfo(pattern: Pattern): Map<String, String> {
        return pattern.additionalInfo()
    }
}