package sample.controller.dialogs.search;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import sample.controller.MainController;
import sample.model.search.SearchProgram;
import sample.model.simulation.Grid;

import java.io.File;
import java.util.Arrays;

public abstract class SearchResultsDialog extends Dialog {
    protected GridPane grid;  // Store controls here
    protected TableView tableView;  // Display the search results in a table
    protected ContextMenu menu;  // Display on right-click

    protected SearchProgram searchProgram;  // The search program that is running
    protected MainController mainController;

    // Create initial data
    protected final ObservableList<Grid> data = FXCollections.observableArrayList();

    public SearchResultsDialog(MainController mainController, SearchProgram searchProgram) {
        super();
        super.setResizable(true);
        super.initModality(Modality.NONE);

        menu = new ContextMenu();  // The menu that shows on right click

        // Displays the pattern in the application
        MenuItem showPattern = new MenuItem("Show in Application");
        showPattern.setOnAction(event -> loadPattern());
        menu.getItems().add(showPattern);

        // Copies the RLE to the clipboard
        MenuItem copyRLE = new MenuItem("Copy RLE to Clipboard");
        copyRLE.setOnAction(event -> copyToClipboard());
        menu.getItems().add(copyRLE);

        // Grid to arrange controls
        grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);

        // Table to display data
        tableView = new TableView<>();
        tableView.setItems(data);
        tableView.setContextMenu(menu);

        // Rescale with window
        tableView.prefHeightProperty().bind(super.heightProperty());
        tableView.prefWidthProperty().bind(super.widthProperty());

        grid.add(tableView, 0, 0);

        // HBox to contain the buttons
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(5);
        grid.add(buttonsBox, 0, 1);

        // Button to reload data
        Button reloadButton = new Button("Reload Data");
        reloadButton.setOnAction(event -> reloadTableView());
        buttonsBox.getChildren().add(reloadButton);

        // Button to save data to file
        Button saveToFileButton = new Button("Save to File");
        saveToFileButton.setOnAction(event -> saveToFile());
        buttonsBox.getChildren().add(saveToFileButton);

        // Button to terminate search process
        Button terminateSearchButton = new Button("Terminate Search");
        terminateSearchButton.setOnAction(event -> {
            try {
                searchProgram.terminateSearch();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success!");
                alert.setHeaderText("The search program has been terminated!");
                alert.showAndWait();
            }
            catch (Exception exception) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText(exception.getMessage());
                alert.setContentText(Arrays.toString(exception.getStackTrace()));
                alert.showAndWait();
            }
        });
        buttonsBox.getChildren().add(terminateSearchButton);

        // Allows closing with close button
        Window window = super.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        super.getDialogPane().setContent(grid);
        this.searchProgram = searchProgram;  // Setting the search program
        this.mainController = mainController;  // Setting the main controller
    }

    // Mutators
    public void setSearchProgram(SearchProgram searchProgram) {
        this.searchProgram = searchProgram;
    }

    // Saves the search results to a file
    public void saveToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save search results");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Comma Separated Values Files (*.csv)", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        // Writing to file
        if (!searchProgram.writeToFile(file)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error in saving the file");
            alert.setHeaderText("The operation was unsuccessful.");
            alert.setContentText("The operation was unsuccessful. " +
                    "If you suspect a bug, please report it.");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
            alert.showAndWait();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Operation successful!");
            alert.setHeaderText("The operation was successful.");
            alert.setContentText("The operation was successful. " +
                    "The search results have been saved to " + file.getAbsolutePath() + ".");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
            alert.showAndWait();
        }
    }

    // Loads the search result into the main simulator
    public void loadPattern() {
        mainController.loadPattern(getSelectedRLE());
    }

    // Copies the RLE onto the clipboard
    public void copyToClipboard() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();

        content.putString(getSelectedRLE());
        clipboard.setContent(content);
    }

    // Gets the RLE of the selected pattern
    public abstract String getSelectedRLE();

    // Reloads the table with new data from the spreadsheet
    public abstract void reloadTableView();
}
