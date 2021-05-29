package application.controller.dialogs.search;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import application.model.search.SearchParameters;

/**
 * A dialog to obtain search parameters from a search dialog
 */
public abstract class SearchParametersDialog extends Dialog {
    protected Spinner<Integer> spinnerThreads;
    protected GridPane grid;

    // Search parameters for the search program
    protected SearchParameters searchParameters;

    // Number of threads to use
    protected int numThreads;

    /**
     * Constructs the dialog
     */
    public SearchParametersDialog() {
        super();
        super.setResizable(true);

        VBox vbox = new VBox();
        vbox.setSpacing(5);

        // Grid to store all the stuff
        grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        vbox.getChildren().add(grid);

        // Label for the number of threads
        Label labelThreads = new Label("Number of Threads:");
        grid.add(labelThreads, 0, 0);

        // The number of threads to run the search program
        SpinnerValueFactory<Integer> numThreadsFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 15, 5);
        spinnerThreads = new Spinner<>();
        spinnerThreads.setEditable(true);
        spinnerThreads.setValueFactory(numThreadsFactory);
        grid.add(spinnerThreads, 0, 1);

        // Button to confirm parameters
        Button confirmButton = new Button("Confirm Parameters");
        confirmButton.setOnAction(event -> {
            // If successful
            if (confirmParameters()) {
                // Close the dialog
                super.setResult(Boolean.TRUE);
                super.close();
            }
        });

        // Place in VBox so child class can have as many widgets as it likes in the grid
        vbox.getChildren().add(confirmButton);

        // Allows closing with close button
        Window window = super.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        // Setting the VBox as the main content
        super.getDialogPane().setContent(vbox);
    }

    /**
     * Updates the search parameters based on the inputted parameters
     * @return Returns true if successful, false otherwise
     */
    public boolean confirmParameters() {
        numThreads = spinnerThreads.getValue();
        return false;
    }

    /**
     * Gets the search parameters
     * @return Returns the search parameters
     */
    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

    /**
     * Gets the number of threads used
     * @return Returns the number of threads used
     */
    public int getNumThreads() {
        return numThreads;
    }
}
