package sample.controller.dialogs.search;

import javafx.scene.control.*;
import sample.model.rules.Rule;
import sample.model.search.csearch.BruteForceSearchParameters;
import sample.model.search.ocgar2.AgarSearchParameters;

public class AgarSearchParametersDialog extends SearchParametersDialog {
    private final Rule rule;
    private final Spinner<Integer> spinnerMaxPeriod;

    public AgarSearchParametersDialog(Rule rule) {
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

        // Setting the rule to search
        this.rule = rule;
    }

    @Override
    public boolean confirmParameters() {
        super.confirmParameters();

        searchParameters = new AgarSearchParameters(rule, spinnerMaxPeriod.getValue());
        return true;
    }

    @Override
    public AgarSearchParameters getSearchParameters() {
        return (AgarSearchParameters) super.getSearchParameters();
    }
}
