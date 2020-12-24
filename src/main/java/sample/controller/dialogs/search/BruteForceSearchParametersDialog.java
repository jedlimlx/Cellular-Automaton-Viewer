package sample.controller.dialogs.search;

import javafx.scene.control.*;
import sample.controller.dialogs.RandomSoupDialog;
import sample.model.rules.Rule;
import sample.model.search.csearch.BruteForceSearchParameters;

import java.util.Arrays;
import java.util.Collections;

public class BruteForceSearchParametersDialog extends SearchParametersDialog {
    private final RandomSoupDialog randomSoupDialog;

    private final Rule rule;
    private final CheckBox bruteForce;
    private final Spinner<Integer> spinnerMaxPeriod, spinnerBoundX, spinnerBoundY;

    public BruteForceSearchParametersDialog(Rule rule) {
        super();

        // Label for the maximum period
        grid.add(new Label("Max Period:"), 0, 2);

        // The maximum period for period detection
        SpinnerValueFactory<Integer> maxPeriodFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 20000, 70);
        spinnerMaxPeriod = new Spinner<>();
        spinnerMaxPeriod.setEditable(true);
        spinnerMaxPeriod.setValueFactory(maxPeriodFactory);
        grid.add(spinnerMaxPeriod, 0, 3);

        // Bounding box for enumeration
        SpinnerValueFactory<Integer> boundingBoxFactory3 =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20000, 5);

        grid.add(new Label("Soup Width:"), 0, 4);

        spinnerBoundX = new Spinner<>();
        spinnerBoundX.setEditable(true);
        spinnerBoundX.setValueFactory(boundingBoxFactory3);
        grid.add(spinnerBoundX, 0, 5);

        grid.add(new Label("Soup Height:"), 0, 6);

        SpinnerValueFactory<Integer> boundingBoxFactory4 =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20000, 5);

        spinnerBoundY = new Spinner<>();
        spinnerBoundY.setEditable(true);
        spinnerBoundY.setValueFactory(boundingBoxFactory4);
        grid.add(spinnerBoundY, 0, 7);

        // Brute-force enumeration or random soup search
        bruteForce = new CheckBox("Brute Force?");
        grid.add(bruteForce, 0, 8);

        // Random Soup Parameters
        randomSoupDialog = new RandomSoupDialog(rule.getNumStates(), 50, "C1",
                Collections.singletonList(1));
        Button randomSoupParametersButton = new Button("Set Random Soup Parameters");
        randomSoupParametersButton.setOnAction(event -> randomSoupDialog.showAndWait());

        grid.add(randomSoupParametersButton, 0, 9);

        // Setting the rule to search
        this.rule = rule;
    }

    @Override
    public boolean confirmParameters() {
        super.confirmParameters();

        try {
            searchParameters = new BruteForceSearchParameters(rule, spinnerMaxPeriod.getValue(),
                    spinnerBoundX.getValue(), spinnerBoundY.getValue(), !bruteForce.isSelected(),
                    randomSoupDialog.getSymmetry(), randomSoupDialog.getStates(), randomSoupDialog.getDensity());
            return true;
        }
        catch (IllegalArgumentException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText(exception.getMessage());
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }

        return false;
    }

    @Override
    public BruteForceSearchParameters getSearchParameters() {
        return (BruteForceSearchParameters) super.getSearchParameters();
    }
}
