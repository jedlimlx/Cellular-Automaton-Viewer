package application.controller.dialogs.search;

import application.controller.MainController;
import application.model.Utils;
import application.model.patterns.Pattern;
import application.model.search.SearchProgram;

import java.util.Map;

public class AgarSearchResultsDialog extends SearchResultsDialog {
    public AgarSearchResultsDialog(MainController mainController, SearchProgram searchProgram) {
        super(mainController, searchProgram);
        super.setTitle("Agar Search Results");
    }

    @Override
    public String getSelectedRLE() {
        Pattern pattern = (Pattern) PatternsDialog.selected.getPattern();
        return Utils.fullRLE(pattern);
    }

    @Override
    public Map<String, String> getAdditionalInfo(Pattern pattern) {
        return pattern.additionalInfo();
    }
}
