package sample.controller.dialogs.search;

import javafx.scene.control.*;
import sample.model.Coordinate;
import sample.model.rules.Rule;
import sample.model.search.catsrc.CatalystSearchParameters;
import sample.model.simulation.Grid;

import java.util.ArrayList;
import java.util.List;

public class CatalystSearchParametersDialog extends SearchParametersDialog {
    private final Rule rule;
    private final Grid target;
    private final List<Coordinate> coordinateList;
    private final TextArea catalystTextArea;
    private final Spinner<Integer> spinnerMaxRepeatTime;
    private final Spinner<Integer> spinnerNumCatalyst;
    private final CheckBox rotateCatalystCheckbox, flipCatalystCheckbox;

    public CatalystSearchParametersDialog(Rule rule, Grid target, List<Coordinate> coordinateList) {
        this.rule = rule;
        this.target = target;
        this.coordinateList = coordinateList;

        // Label for maximum repeat time
        Label maxRepeatTimeLabel = new Label("Maximum Repeat Time:");
        grid.add(maxRepeatTimeLabel, 0, 2);

        // The maximum repeat time
        SpinnerValueFactory<Integer> maxRepeatTimeFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 20000, 200);
        spinnerMaxRepeatTime = new Spinner<>();
        spinnerMaxRepeatTime.setEditable(true);
        spinnerMaxRepeatTime.setValueFactory(maxRepeatTimeFactory);
        grid.add(spinnerMaxRepeatTime, 0, 3);

        // Label for the number of catalysts to use
        Label numCatalystLabel = new Label("Number of Catalysts:");
        grid.add(numCatalystLabel, 0, 4);

        // The number of catalysts to use
        SpinnerValueFactory<Integer> numCatalystFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 3);
        spinnerNumCatalyst = new Spinner<>();
        spinnerNumCatalyst.setEditable(true);
        spinnerNumCatalyst.setValueFactory(numCatalystFactory);
        grid.add(spinnerNumCatalyst, 0, 5);

        // Should the catalysts be rotated?
        rotateCatalystCheckbox = new CheckBox("Rotate Catalyst?");
        grid.add(rotateCatalystCheckbox, 0, 6);

        // Should the catalysts be flipped?
        flipCatalystCheckbox = new CheckBox("Flip Catalyst?");
        grid.add(flipCatalystCheckbox, 0, 7);

        // Label for the list of catalysts to use
        Label catalystsToUseLabel = new Label("Enter the catalysts to use:");
        grid.add(catalystsToUseLabel, 0, 8);

        // The list of catalysts to use
        catalystTextArea = new TextArea();
        catalystTextArea.setPromptText("Enter the catalysts to use (one line per headless RLE).");
        grid.add(catalystTextArea, 0, 9);
    }

    @Override
    public boolean confirmParameters() {
        super.confirmParameters();

        List<Grid> catalysts = new ArrayList<>();
        for (String token: catalystTextArea.getText().strip().split("\\s*\n\\s*")) {
            Grid catalyst = new Grid();
            catalyst.fromRLE(token, new Coordinate());

            catalysts.add(catalyst);
        }

        searchParameters = new CatalystSearchParameters(spinnerMaxRepeatTime.getValue(),
                spinnerNumCatalyst.getValue(), false, rotateCatalystCheckbox.isSelected(),
                flipCatalystCheckbox.isSelected(), catalysts, target, coordinateList, rule);
        return true;
    }

    @Override
    public CatalystSearchParameters getSearchParameters() {
        return (CatalystSearchParameters) super.getSearchParameters();
    }
}
