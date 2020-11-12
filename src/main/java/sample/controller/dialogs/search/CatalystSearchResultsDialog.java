package sample.controller.dialogs.search;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;
import sample.controller.MainController;
import sample.model.patterns.Pattern;
import sample.model.rules.RuleFamily;
import sample.model.search.SearchProgram;
import sample.model.simulation.Grid;

import java.util.ArrayList;

public class CatalystSearchResultsDialog extends SearchResultsDialog {
    private int currentIndex;

    public CatalystSearchResultsDialog(MainController mainController, SearchProgram searchProgram) {
        super(mainController, searchProgram);
        super.setTitle("Catalyst Search Results");

        // Adding column for the repeat time
        TableColumn<Grid, String> columnPattern = new TableColumn<>("Repeat Time");
        columnPattern.prefWidthProperty().bind(tableView.prefWidthProperty());
        columnPattern.setCellValueFactory(pattern -> new ReadOnlyObjectWrapper(pattern.getValue()));
        tableView.getColumns().add(columnPattern);
    }

    @Override
    public String getSelectedRLE() {
        Pattern pattern = (Pattern) tableView.getSelectionModel().getSelectedItem();  // To get the rule used
        pattern.updateBounds();

        int width = pattern.getBounds().getValue1().subtract(pattern.getBounds().getValue0()).getX() + 2;
        int height = pattern.getBounds().getValue1().subtract(pattern.getBounds().getValue0()).getY() + 2;

        String rle = "x = " + width + ", y = " + height + ", rule = " +
                ((RuleFamily) pattern.getRule()).getRulestring() + "\n";  // Header

        rle += pattern.toRLE();  // Body
        return rle;
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
