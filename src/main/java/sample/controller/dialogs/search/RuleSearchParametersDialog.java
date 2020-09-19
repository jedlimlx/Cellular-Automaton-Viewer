package sample.controller.dialogs.search;

import javafx.scene.control.*;
import sample.controller.dialogs.rule.RuleDialog;
import sample.model.Grid;
import sample.model.rules.MinMaxRuleable;
import sample.model.search.RuleSearchParameters;

public class RuleSearchParametersDialog extends SearchParametersDialog {
    private final Grid targetPattern;
    private final Spinner<Integer> spinnerMaxPeriod;

    private final RuleDialog minRuleDialog;
    private final RuleDialog maxRuleDialog;

    public RuleSearchParametersDialog(Grid targetPattern) {
        super();

        // Initialise the rule dialogs
        minRuleDialog = new RuleDialog("Enter Min Rule");
        maxRuleDialog = new RuleDialog("Enter Max Rule");

        // Label for the maximum period
        Label labelMaxPeriod = new Label("Max Period:");
        grid.add(labelMaxPeriod, 0, 2);

        // The maximum period for period detection
        SpinnerValueFactory<Integer> maxPeriodFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 20000, 70);
        spinnerMaxPeriod = new Spinner<>();
        spinnerMaxPeriod.setEditable(true);
        spinnerMaxPeriod.setValueFactory(maxPeriodFactory);
        grid.add(spinnerMaxPeriod, 0, 3);

        // Button to select min rule
        Button buttonMinRule = new Button("Set Minimum Rule");
        buttonMinRule.setOnAction(event -> minRuleDialog.showAndWait());
        grid.add(buttonMinRule, 0, 4);

        // Button to select max rule
        Button buttonMaxRule = new Button("Set Maximum Rule");
        buttonMaxRule.setOnAction(event -> maxRuleDialog.showAndWait());
        grid.add(buttonMaxRule, 0, 5);

        // Setting the target pattern
        this.targetPattern = targetPattern;
    }

    @Override
    public boolean confirmParameters() {
        super.confirmParameters();

        try {
            // Checking if the rulespace supports min / max rules
            if (!(minRuleDialog.getRule() instanceof MinMaxRuleable)) {
                throw new IllegalArgumentException();
            }

            // Checking if the min and max rules are valid
            if (!((MinMaxRuleable) minRuleDialog.getRule()).
                    validMinMax(minRuleDialog.getRule(), maxRuleDialog.getRule())) {
                throw new IllegalArgumentException();
            }

            searchParameters = new RuleSearchParameters(targetPattern,
                    minRuleDialog.getRule(), maxRuleDialog.getRule(), spinnerMaxPeriod.getValue());
            return true;
        }
        catch (NullPointerException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("The min / max rule is not specified!");
            alert.setContentText("The min / max rule is not specified!");
            alert.showAndWait();
        }
        catch (UnsupportedOperationException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Minimum and maximum rules are not supported by this rule family.");
            alert.setContentText("The minimum and maximum rules are not supported by this rule family. " +
                    "As a result, you cannot run rule search on rules in this rule family. " +
                    "If you need this feature, please request for it.");
            alert.showAndWait();
        }
        catch (IllegalArgumentException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("The min / max rule is invalid!");
            alert.setContentText("The min / max rule is invalid!");
            alert.showAndWait();
        }

        return false;
    }
}
