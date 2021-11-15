package application.controller.dialogs.search

import application.controller.MainController
import application.model.Utils
import application.model.patterns.Pattern
import application.model.search.SearchProgram

class CatalystSearchResultsDialog(mainController: MainController, searchProgram: SearchProgram) :
    SearchResultsDialog(mainController, searchProgram) {
    override val selectedRLE: String
        get() {
            val pattern = PatternsDialog.selected!!.pattern as Pattern
            return Utils.fullRLE(pattern)
        }

    override fun getAdditionalInfo(pattern: Pattern): Map<String, String> {
        return pattern.additionalInfo()
    }

    init {
        super.setTitle("Catalyst Search Results")
    }
}