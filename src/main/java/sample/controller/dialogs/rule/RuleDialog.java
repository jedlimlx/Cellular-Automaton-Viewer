package sample.controller.dialogs.rule;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import sample.controller.dialogs.rule.hrot.*;
import sample.controller.dialogs.rule.isotropic.*;
import sample.controller.dialogs.rule.misc.AlternatingDialog;
import sample.controller.dialogs.rule.misc.OneDimensionalDialog;
import sample.controller.dialogs.rule.misc.TurmitesDialog;
import sample.controller.dialogs.rule.ruleloader.RuleLoaderDialog;
import sample.model.Coordinate;
import sample.model.rules.RuleFamily;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class RuleDialog extends Dialog {
    private RuleFamily chosenRuleFamily;

    // Controls
    private final GridPane grid;
    private final Button confirmRuleButton;
    private final TextField rulestringField;
    private final ComboBox ruleFamilyCombobox;

    // Array to store rule widgets
    private final RuleWidget[] ruleWidgets = new RuleWidget[]{new HROTDialog(), new HROTHistoryDialog(),
            new HROTSymbiosisDialog(), new HROTDeadlyEnemiesDialog(), new HROTGenerationsDialog(),
            new HROTExtendedGenerationsDialog(), new IntegerHROTDialog(), new DeficientHROTDialog(),
            new HROTRegeneratingGenerationsDialog(), new MultistateCyclicHROTDialog(), new INTDialog(),
            new INTHistoryDialog(), new INTEnergeticDialog(), new INTGenerationsDialog(), new DeficientINTDialog(),
            new OneDimensionalDialog(), new TurmitesDialog(), new AlternatingDialog(), new RuleLoaderDialog()};

    public RuleDialog(String promptText) {
        super();

        // Set Titles
        super.setTitle("Set Rule");
        super.setResizable(true);

        // Create controls
        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);

        // HBox to contain combobox and button
        HBox hBox = new HBox();
        hBox.setSpacing(5);
        grid.add(hBox, 0, 0);

        // Combobox to Choose Rule Family
        ruleFamilyCombobox = new ComboBox();
        ruleFamilyCombobox.setOnAction((event) -> changeRuleFamily(
                ruleWidgets[ruleFamilyCombobox.getItems().indexOf(ruleFamilyCombobox.getValue())]));
        hBox.getChildren().add(ruleFamilyCombobox);

        // Textbox for rulestring
        rulestringField = new TextField();
        rulestringField.setPromptText(promptText);
        rulestringField.textProperty().addListener((observable, oldValue, newValue) ->
                rulestringFieldChanged());  // Listen for changes to the text and update the combobox

        rulestringField.setOnKeyPressed(event -> {  // Enter as a substitute for okay
            if (event.getCode().equals(KeyCode.ENTER)) {
                confirmRule();
            }
        });

        grid.add(rulestringField, 0, 2);

        // Button the confirm the rulestring
        confirmRuleButton = new Button("Confirm Rule");
        confirmRuleButton.setOnAction((event) -> confirmRule());
        grid.add(confirmRuleButton, 0, 3);

        // Get rule family names & configure the button correctly
        ruleFamilyCombobox.setValue(ruleWidgets[0].toString());
        for (RuleWidget widget : ruleWidgets) {
            ruleFamilyCombobox.getItems().add(widget.toString());
        }

        // Button to display information about the rulespace
        Button infoButton = new Button("?");
        infoButton.setOnAction((event) -> showRuleFamilyDescription());
        hBox.getChildren().add(infoButton);

        // Allows closing with close button
        Window window = super.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        // Add the rule widget to the grid
        grid.add(ruleWidgets[0], 0, 1);
        super.getDialogPane().setContent(grid);
    }

    public RuleDialog() {
        this("Enter Rulestring");
    }

    public void confirmRule() {
        // Get currently selected rule widget
        int index = ruleFamilyCombobox.getItems().indexOf(ruleFamilyCombobox.getValue());
        RuleWidget ruleWidget = ruleWidgets[index];

        // Update the rule in the rule widget
        try {
            ruleWidget.updateRule(rulestringField.getText());
        } catch (IllegalArgumentException exception) {
            LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).log(
                    Level.WARNING, exception.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("This rulestring is invalid!");
            alert.setContentText(exception.getMessage() + "\nIf you suspect this is a bug, please report it!");
            alert.showAndWait();
            return;
        } catch (Exception exception) {
            LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).log(
                    Level.WARNING, exception.getMessage());
            exception.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("An error occured!");
            alert.setContentText(exception.getMessage() + "\nIf you suspect this is a bug, please report it!");
            alert.showAndWait();
            return;
        }

        chosenRuleFamily = ruleWidget.getRuleFamily();
        if (chosenRuleFamily.getBoundedGrid() != null) {
            chosenRuleFamily.getBoundedGrid().setInitialCoordinate(new Coordinate(1800, 1800));
        }

        // Canonise the rulestring in the text field
        rulestringField.setText(chosenRuleFamily.getRulestring());

        // Close the dialog
        super.setResult(Boolean.TRUE);
        super.close();
    }

    public void rulestringFieldChanged() {
        boolean found = false;
        for (RuleWidget widget: ruleWidgets) {
            for (String regex: widget.getRuleFamily().getRegex()) {
                if (rulestringField.getText().split(":")[0].matches(regex)) {
                    found = true;
                    break;
                }
            }

            // Completely break out of the loop
            if (found) {
                changeRuleFamily(widget);  // Change to the widget who regex matches
                break;
            }
        }
    }

    public void changeRuleFamily(RuleWidget widget) {
        // Get currently selected rule widget
        int index = ruleFamilyCombobox.getItems().indexOf(ruleFamilyCombobox.getValue());
        RuleWidget ruleWidget = ruleWidgets[index];

        // Remove the current widget
        grid.getChildren().remove(ruleWidget);

        // Add the new one
        grid.add(widget, 0, 1);

        // Set the value of the combobox
        ruleFamilyCombobox.setValue(widget.getRuleFamily().getName());
    }

    public void showRuleFamilyDescription() {
        // Get currently selected rule widget
        int index = ruleFamilyCombobox.getItems().indexOf(ruleFamilyCombobox.getValue());
        RuleWidget ruleWidget = ruleWidgets[index];

        // Display description
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(ruleWidget.getRuleFamily().getName() + " Description");
        alert.setHeaderText("Description of " + ruleWidget.getRuleFamily().getName());
        alert.setContentText(ruleWidget.getRuleFamily().getDescription());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
        alert.showAndWait();
    }

    public RuleFamily getRule() {
        if (super.getResult() == Boolean.TRUE) {
            return chosenRuleFamily;  // Only return if user hits confirm rule
        }
        else {
            return null;
        }
    }

    public RuleWidget[] getRuleWidgets() {
        return ruleWidgets;
    }

    public void setRule(RuleFamily rule) {
        boolean found = false;
        for (RuleWidget widget: ruleWidgets) {
            for (String regex: widget.getRuleFamily().getRegex()) {
                if (rule.getRulestring().split(":")[0].matches(regex)) {
                    found = true;
                    break;
                }
            }

            // Completely break out of the loop
            if (found) {
                rulestringField.setText(rule.getRulestring());
                widget.setRuleFamily(rule);
                changeRuleFamily(widget);  // Change to the widget whose regex matches

                // Move the initial coordinate
                if (rule.getBoundedGrid() != null) {
                    rule.getBoundedGrid().setInitialCoordinate(new Coordinate(1800, 1800));
                }

                break;
            }
        }
    }
}
