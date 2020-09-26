package sample.controller.dialogs.search;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import sample.controller.MainController;
import sample.model.patterns.Pattern;
import sample.model.rules.RuleFamily;
import sample.model.search.RuleSearchParameters;
import sample.model.search.SearchProgram;
import sample.model.simulation.Grid;

import java.util.ArrayList;

public class RuleSearchResultsDialog extends SearchResultsDialog {
    private int currentIndex;  // The index of the last loaded result
    public RuleSearchResultsDialog(MainController mainController, SearchProgram searchProgram) {
        super(mainController, searchProgram);
        super.setTitle("Rule Search Results");

        // Adding column for the pattern type (P2 Oscillator, c/3 Spaceship, etc.)
        TableColumn<Grid, String> columnPattern = new TableColumn<>("Pattern");
        columnPattern.prefWidthProperty().bind(tableView.prefWidthProperty().divide(3.1));
        columnPattern.setCellValueFactory(pattern -> new ReadOnlyObjectWrapper(pattern.getValue().toString()));
        tableView.getColumns().add(columnPattern);

        // Adding column for the pattern's min rule
        TableColumn<Grid, String> columnMinRule = new TableColumn<>("Minimum Rule");
        columnMinRule.prefWidthProperty().bind(tableView.prefWidthProperty().divide(3.1));
        columnMinRule.setCellValueFactory(pattern -> new ReadOnlyObjectWrapper(  // Why casting... Why?
                ((Pattern) pattern.getValue()).getMinRule().getRulestring()));
        tableView.getColumns().add(columnMinRule);

        // Adding column for the pattern's max rule
        TableColumn<Grid, String> columnMaxRule = new TableColumn<>("Maximum Rule");
        columnMaxRule.prefWidthProperty().bind(tableView.prefWidthProperty().divide(3.1));
        columnMaxRule.setCellValueFactory(pattern -> new ReadOnlyObjectWrapper(  // Why casting... Why?
                ((Pattern) pattern.getValue()).getMaxRule().getRulestring()));
        tableView.getColumns().add(columnMaxRule);

        // Displays the min rule of the pattern in the application
        MenuItem showMinRule = new MenuItem("Show Min Rule in Application");
        showMinRule.setOnAction(event -> loadMinRule());
        menu.getItems().add(1, showMinRule);

        // Displays the max rule of the pattern in the application
        MenuItem showMaxRule = new MenuItem("Show Max Rule in Application");
        showMaxRule.setOnAction(event -> loadMaxRule());
        menu.getItems().add(2, showMaxRule);
    }

    public void loadMinRule() {
        Pattern pattern = (Pattern) tableView.getSelectionModel().getSelectedItem();  // To get the rule used
        mainController.loadPattern(getSelectedRLE(pattern.getMinRule()));
    }

    public void loadMaxRule() {
        Pattern pattern = (Pattern) tableView.getSelectionModel().getSelectedItem();  // To get the rule used
        mainController.loadPattern(getSelectedRLE(pattern.getMaxRule()));
    }

    @Override
    public String getSelectedRLE() {
        Pattern pattern = (Pattern) tableView.getSelectionModel().getSelectedItem();  // To get the rule used
        return getSelectedRLE((RuleFamily) pattern.getRule());
    }

    public String getSelectedRLE(RuleFamily rule) {
        Grid targetPattern = ((RuleSearchParameters) searchProgram.getSearchParameters()).getTargetPattern();
        targetPattern.updateBounds();

        int width = targetPattern.getBounds().getValue1().subtract(targetPattern.getBounds().getValue0()).getX() + 2;
        int height = targetPattern.getBounds().getValue1().subtract(targetPattern.getBounds().getValue0()).getY() + 2;

        String rle = "x = " + width + ", y = " + height + ", rule = " + rule.getRulestring() + "\n";  // Header

        rle += targetPattern.toRLE(targetPattern.getBounds().getValue0(),
                targetPattern.getBounds().getValue1());  // Body

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
