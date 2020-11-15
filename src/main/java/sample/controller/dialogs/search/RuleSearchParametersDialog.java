package sample.controller.dialogs.search;

import javafx.scene.control.*;
import org.controlsfx.control.SegmentedButton;
import org.javatuples.Pair;
import sample.controller.dialogs.rule.RuleDialog;
import sample.model.Coordinate;
import sample.model.rules.MinMaxRuleable;
import sample.model.rules.Rule;
import sample.model.rules.RuleFamily;
import sample.model.search.rulesrc.RuleSearchParameters;
import sample.model.simulation.Grid;
import sample.model.simulation.Simulator;

public class RuleSearchParametersDialog extends SearchParametersDialog {
    private final Grid targetPattern;
    private final Spinner<Integer> spinnerMaxPeriod, spinnerMinPop, spinnerMaxPop, spinnerMaxX, spinnerMaxY,
            spinnerMatchGenerations;

    private final Rule rule;
    private final RuleDialog minRuleDialog;
    private final RuleDialog maxRuleDialog;

    private boolean manualControl;

    public RuleSearchParametersDialog(Grid targetPattern, Rule rule) {
        super();

        // Initialise the rule dialogs
        minRuleDialog = new RuleDialog("Enter Min Rule");
        maxRuleDialog = new RuleDialog("Enter Max Rule");

        // Label for the maximum period
        grid.add(new Label("Max Period:"), 0, 2);

        // The maximum period for period detection
        SpinnerValueFactory<Integer> maxPeriodFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 20000, 70);
        spinnerMaxPeriod = new Spinner<>();
        spinnerMaxPeriod.setEditable(true);
        spinnerMaxPeriod.setValueFactory(maxPeriodFactory);
        grid.add(spinnerMaxPeriod, 0, 3);

        // The minimum and maximum population for period detection
        SpinnerValueFactory<Integer> populationFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20000, 0);

        grid.add(new Label("Min Population:"), 0, 4);

        spinnerMinPop = new Spinner<>();
        spinnerMinPop.setEditable(true);
        spinnerMinPop.setValueFactory(populationFactory);
        grid.add(spinnerMinPop, 0, 5);

        SpinnerValueFactory<Integer> populationFactory2 =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20000, 100);

        grid.add(new Label("Max Population:"), 0, 6);

        spinnerMaxPop = new Spinner<>();
        spinnerMaxPop.setEditable(true);
        spinnerMaxPop.setValueFactory(populationFactory2);
        grid.add(spinnerMaxPop, 0, 7);

        // Maximum bounding box for period detection
        SpinnerValueFactory<Integer> boundingBoxFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20000, 40);

        grid.add(new Label("Max Width:"), 0, 8);

        spinnerMaxX = new Spinner<>();
        spinnerMaxX.setEditable(true);
        spinnerMaxX.setValueFactory(boundingBoxFactory);
        grid.add(spinnerMaxX, 0, 9);

        grid.add(new Label("Max Height:"), 0, 10);

        SpinnerValueFactory<Integer> boundingBoxFactory2 =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20000, 40);

        spinnerMaxY = new Spinner<>();
        spinnerMaxY.setEditable(true);
        spinnerMaxY.setValueFactory(boundingBoxFactory2);
        grid.add(spinnerMaxY, 0, 11);

        grid.add(new Separator(), 0, 12);

        // Either manual control or match x generations
        ToggleButton manualControlButton = new ToggleButton("Manual Control");
        ToggleButton matchGenerationsButton = new ToggleButton("Match X Generations");

        SegmentedButton segmentedButton = new SegmentedButton();
        segmentedButton.getButtons().addAll(manualControlButton, matchGenerationsButton);
        grid.add(segmentedButton, 0, 13);

        // Button to select min rule
        Button buttonMinRule = new Button("Set Minimum Rule");
        buttonMinRule.setOnAction(event -> minRuleDialog.showAndWait());
        grid.add(buttonMinRule, 0, 14);

        // Button to select max rule
        Button buttonMaxRule = new Button("Set Maximum Rule");
        buttonMaxRule.setOnAction(event -> maxRuleDialog.showAndWait());
        grid.add(buttonMaxRule, 0, 15);

        // Match X generations
        Label matchGenerationsLabel = new Label("Match X Generations:");
        grid.add(matchGenerationsLabel, 0, 16);

        SpinnerValueFactory<Integer> matchGenerationsFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 200, 10);

        spinnerMatchGenerations = new Spinner<>();
        spinnerMatchGenerations.setEditable(true);
        spinnerMatchGenerations.setValueFactory(matchGenerationsFactory);
        grid.add(spinnerMatchGenerations, 0, 17);

        matchGenerationsButton.setOnAction(event -> {
            buttonMinRule.setDisable(true);
            buttonMaxRule.setDisable(true);
            matchGenerationsLabel.setDisable(false);
            spinnerMatchGenerations.setDisable(false);
            manualControl = false;
        });
        manualControlButton.setOnAction(event -> {
            buttonMinRule.setDisable(false);
            buttonMaxRule.setDisable(false);
            matchGenerationsLabel.setDisable(true);
            spinnerMatchGenerations.setDisable(true);
            manualControl = true;
        });

        // Setting the target pattern
        this.rule = rule;
        this.targetPattern = targetPattern;
    }

    @Override
    public boolean confirmParameters() {
        super.confirmParameters();

        try {
            if (manualControl) {
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
                        minRuleDialog.getRule(), maxRuleDialog.getRule(), spinnerMaxPeriod.getValue(),
                        spinnerMinPop.getValue(), spinnerMaxPop.getValue(), spinnerMaxX.getValue(),
                        spinnerMaxY.getValue());
            } else {
                // Checking if the rulespace supports min / max rules
                if (!(rule instanceof MinMaxRuleable)) {
                    throw new IllegalArgumentException();
                }

                // Evolve target pattern for x generations
                Simulator simulator = new Simulator(rule);
                simulator.insertCells(targetPattern, new Coordinate());

                Grid[] grids = new Grid[spinnerMatchGenerations.getValue()];
                for (int i = 0; i < spinnerMatchGenerations.getValue(); i++) {
                    grids[i] = simulator.deepCopy();
                    grids[i].setBackground(rule.convertState(0, simulator.getGeneration()));
                    simulator.step();
                }

                Pair<RuleFamily, RuleFamily> minMaxRule = ((MinMaxRuleable) rule).getMinMaxRule(grids);
                searchParameters = new RuleSearchParameters(targetPattern,
                        minMaxRule.getValue0(), minMaxRule.getValue1(), spinnerMaxPeriod.getValue(),
                        spinnerMinPop.getValue(), spinnerMaxPop.getValue(), spinnerMaxX.getValue(),
                        spinnerMaxY.getValue());
            }

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

    @Override
    public RuleSearchParameters getSearchParameters() {
        return (RuleSearchParameters) super.getSearchParameters();
    }
}
