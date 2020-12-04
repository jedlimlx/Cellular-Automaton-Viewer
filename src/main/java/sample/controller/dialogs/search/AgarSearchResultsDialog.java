package sample.controller.dialogs.search;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;
import sample.controller.MainController;
import sample.model.Utils;
import sample.model.patterns.Pattern;
import sample.model.search.SearchProgram;
import sample.model.simulation.Grid;

import java.util.ArrayList;

public class AgarSearchResultsDialog extends SearchResultsDialog {
    private int currentIndex;

    public AgarSearchResultsDialog(MainController mainController, SearchProgram searchProgram) {
        super(mainController, searchProgram);
        super.setTitle("Agar Search Results");

        // Adding column for the repeat time
        TableColumn<Grid, String> columnPattern = new TableColumn<>("Pattern");
        columnPattern.prefWidthProperty().bind(tableView.prefWidthProperty());
        columnPattern.setCellValueFactory(pattern -> new ReadOnlyObjectWrapper(pattern.getValue()));
        tableView.getColumns().add(columnPattern);
    }

    @Override
    public String getSelectedRLE() {
        Pattern pattern = (Pattern) tableView.getSelectionModel().getSelectedItem();  // To get the rule used
        return Utils.fullRLE(pattern);
    }

    @Override
    public void reloadTableView() {
        ArrayList<Grid> searchResults = searchProgram.getSearchResults();
        for (int i = currentIndex; i < searchResults.size(); i++) {  // Adding in the new objects
            data.add(searchResults.get(i));
        }

        currentIndex = searchResults.size();
        tableView.setItems(data);  // Updating the items on the table
    }
}
