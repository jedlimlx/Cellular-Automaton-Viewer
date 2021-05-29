package application.controller.dialogs;

import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import application.model.rules.Rule;

/**
 * Dialog for the colour picker
 */
public class ColourPickerDialog extends Dialog {
    private final ColorPicker[] colourPickers;
    public ColourPickerDialog(Rule rule, Color[] colours) {
        super.setTitle("Colour Picker");

        GridPane gridPane = new GridPane();
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        colourPickers = new ColorPicker[rule.getNumStates()];
        for (int i = 0; i < rule.getNumStates(); i++) {
            gridPane.add(new Label("State " + i + ":"), 0, i);

            if (colours == null) colourPickers[i] = new ColorPicker(rule.getColour(i));
            else colourPickers[i] = new ColorPicker(colours[i]);

            gridPane.add(colourPickers[i], 1, i);
        }

        Button confirm = new Button("Confirm");
        confirm.setOnAction(event -> {
            // Close the dialog
            super.setResult(Boolean.TRUE);
            super.close();
        });
        gridPane.add(confirm, 0, rule.getNumStates());

        Button cancel = new Button("Cancel");
        cancel.setOnAction(event -> {
            // Close the dialog
            super.setResult(Boolean.FALSE);
            super.close();
        });
        gridPane.add(cancel, 1, rule.getNumStates());

        super.getDialogPane().setContent(gridPane);

        // Allows closing with close button
        Window window = super.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
    }

    public Color[] getColours() {
        Color[] colours = new Color[colourPickers.length];

        for (int i = 0; i < colours.length; i++) {
            colours[i] = colourPickers[i].getValue();
        }

        return colours;
    }
}
