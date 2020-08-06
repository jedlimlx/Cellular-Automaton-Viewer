package sample.controller.dialogs;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import sample.model.Giffer;

public class GifferDialog extends Dialog {
    private Spinner<Integer> spinnerTiming;
    private Spinner<Integer> spinnerCellSize;

    private Giffer giffer;

    public GifferDialog() {
        super();

        // Set titles
        super.setTitle("Giffer Dialog");
        super.setResizable(true);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);

        // Label for the time between frames (ms)
        Label labelTiming = new Label("Time between frames (ms):");
        grid.add(labelTiming, 0, 0);

        // The timing between frames
        SpinnerValueFactory<Integer> valueFactoryTiming =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(20, 100, 50);
        spinnerTiming = new Spinner<>();
        spinnerTiming.setEditable(true);
        spinnerTiming.setValueFactory(valueFactoryTiming);
        grid.add(spinnerTiming, 0, 1);

        // Label for the pixel size
        Label labelSize = new Label("Pixel size:");
        grid.add(labelSize, 0, 2);

        // The cell size of the gif
        SpinnerValueFactory<Integer> valueFactoryCellSize =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2);
        spinnerCellSize = new Spinner<>();
        spinnerCellSize.setEditable(true);
        spinnerCellSize.setValueFactory(valueFactoryCellSize);
        grid.add(spinnerCellSize, 0, 3);

        // Button the confirm the rulestring
        Button confirmButton = new Button("Confirm Settings");
        confirmButton.setOnAction(event -> confirmSettings());
        grid.add(confirmButton, 0, 4);

        // Setting the grid pane as the main content
        super.getDialogPane().setContent(grid);
    }

    public void confirmSettings() {
        // Create a giffer based on the settings
        giffer = new Giffer(spinnerCellSize.getValue(), spinnerTiming.getValue());

        // Close the dialog
        super.setResult(Boolean.TRUE);
        super.close();
    }

    public Giffer getGiffer() {
        return giffer;
    }
}
