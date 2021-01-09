package sample.controller.dialogs.search;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;
import sample.controller.MainController;
import sample.model.Utils;
import sample.model.patterns.Pattern;
import sample.model.search.SearchProgram;
import sample.model.simulation.Grid;

import java.util.ArrayList;
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
