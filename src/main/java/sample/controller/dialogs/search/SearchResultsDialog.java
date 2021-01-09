package sample.controller.dialogs.search;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import sample.controller.MainController;
import sample.model.patterns.Pattern;
import sample.model.search.SearchProgram;
import sample.model.simulation.Grid;

import java.io.File;
import java.util.*;

public abstract class SearchResultsDialog extends Dialog {
    protected GridPane grid;  // Store controls here
    protected ContextMenu menu;  // Display on right-click

    protected SearchProgram searchProgram;  // The search program that is running
    protected MainController mainController;

    protected int index;  // The index of the search results that has been loaded
    protected Map<String, Map<String, List<Pattern>>> patterns;  // The organised search results

    protected HBox patternLists;  // The HBox to store the list of patterns

    // Create initial data
    protected final ObservableList<Grid> data = FXCollections.observableArrayList();

    public SearchResultsDialog(MainController mainController, SearchProgram searchProgram) {
        super();
        super.setResizable(true);
        super.initModality(Modality.NONE);

        this.searchProgram = searchProgram;

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

        // HBox to contain pattern lists
        patternLists = new HBox();
        patternLists.setSpacing(20);

        ScrollPane scrollPane = new ScrollPane(patternLists);
        scrollPane.setStyle("-fx-background-color: transparent;");  // No border
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        grid.add(scrollPane, 0, 0);

        // Building the list of patterns
        patterns = new TreeMap<>();
        organiseSearchResults();

        // Converting into nodes
        generateNodes();

        // HBox to contain the buttons
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(10);
        grid.add(buttonsBox, 0, 1);

        // Button to reload data
        Button reloadButton = new Button("Reload Data");
        reloadButton.setOnAction(event -> {
            organiseSearchResults();
            generateNodes();
        });
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

    // Organises the search results
    public void organiseSearchResults() {
        Pattern pattern;
        for (int i = index; i < searchProgram.getSearchResults().size(); i++) {
            pattern = searchProgram.getSearchResults().get(i);
            if (patterns.containsKey(pattern.getName())) {
                if (patterns.get(pattern.getName()).containsKey(pattern.toString())) {
                    patterns.get(pattern.getName()).get(pattern.toString()).add(pattern);
                } else {
                    patterns.get(pattern.getName()).put(pattern.toString(),
                            new ArrayList<>(Collections.singletonList(pattern)));
                }
            } else {
                patterns.put(pattern.getName(), new TreeMap<>());
            }
        }

        index = searchProgram.getSearchResults().size();
    }

    // Generates the nodes using the organised search results
    public void generateNodes() {
        patternLists.getChildren().clear();

        Font headerFont = Font.font("System", FontWeight.BOLD, 24);
        for (String patternType: patterns.keySet()) {
            VBox vbox = new VBox();
            vbox.setSpacing(5);

            // Header Text
            Label header = new Label(patternType);
            header.setFont(headerFont);
            vbox.getChildren().add(header);

            // Adding the hyperlinks
            for (String patternName: patterns.get(patternType).keySet()) {
                Hyperlink hyperlink = new Hyperlink(patternName);
                hyperlink.setOnAction(event -> {
                    List<Pattern> patList = patterns.get(patternType).get(patternName);

                    int n = patList.size();
                    List<Map<String, String>> additionalInfo = new ArrayList<>(n);
                    for (Pattern blocks : patList) additionalInfo.add(getAdditionalInfo(blocks));

                    PatternsDialog dialog = new PatternsDialog(patterns.get(patternType).get(patternName),
                            additionalInfo, menu);
                    dialog.showAndWait();
                });
                vbox.getChildren().add(hyperlink);
            }

            // Adding to HBox
            patternLists.getChildren().add(vbox);
        }
    }

    // Gets the RLE of the selected pattern
    public abstract String getSelectedRLE();

    // Gets additional informaiton about the provided pattern
    public abstract Map<String, String> getAdditionalInfo(Pattern pattern);
}
