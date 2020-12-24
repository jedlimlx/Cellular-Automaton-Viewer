package sample.controller.dialogs;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import sample.model.SymmetryGenerator;

import java.util.ArrayList;
import java.util.List;

public class RandomSoupDialog extends Dialog {
    private final Slider densitySlider;
    private final ComboBox<String> symmetryCombobox;
    private final ArrayList<CheckBox> statesCheckBox;

    public RandomSoupDialog(int numStates, int density, String symmetry, List<Integer> states) {
        super();

        super.setTitle("Random Soup Settings");
        super.setResizable(true);

        VBox vBox = new VBox();
        vBox.setSpacing(5);

        Label densityLabel = new Label("Density:");
        vBox.getChildren().add(densityLabel);

        // Slider for density
        densitySlider = new Slider();
        densitySlider.setMin(0);
        densitySlider.setMax(100);
        densitySlider.setValue(density);
        densitySlider.valueProperty().addListener((obs, oldval, newVal) ->
                densitySlider.setValue(newVal.intValue()));  // Round to integer
        vBox.getChildren().add(densitySlider);

        densityLabel.textProperty().bind(new SimpleStringProperty("Density: ").concat(
                densitySlider.valueProperty()));

        vBox.getChildren().add(new Label("Symmetry:"));

        // Combobox for symmetries
        symmetryCombobox = new ComboBox<>();
        symmetryCombobox.setValue(symmetry);
        symmetryCombobox.getItems().addAll(SymmetryGenerator.symmetries);
        vBox.getChildren().add(symmetryCombobox);

        // Selector for states
        statesCheckBox = new ArrayList<>();
        for (int i = 1; i < numStates; i++) {
            CheckBox checkBox = new CheckBox(i + "");
            if (states.contains(i)) checkBox.setSelected(true);

            statesCheckBox.add(checkBox);
            vBox.getChildren().add(checkBox);
        }

        // Okay Button
        Button button = new Button("Confirm Settings");
        button.setOnAction(event -> {
            // Close the dialog
            super.setResult(Boolean.TRUE);
            super.close();
        });
        vBox.getChildren().add(button);

        super.getDialogPane().setContent(vBox);

        // Allows closing with close button
        Window window = super.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
    }

    public int getDensity() {
        return (int) densitySlider.getValue();
    }

    public String getSymmetry() {
        return symmetryCombobox.getValue();
    }

    public ArrayList<Integer> getStates() {
        ArrayList<Integer> states = new ArrayList<>();
        for (int i = 0; i < statesCheckBox.size(); i++) {
            if (statesCheckBox.get(i).isSelected()) states.add(i + 1);
        }

        return states;
    }
}
