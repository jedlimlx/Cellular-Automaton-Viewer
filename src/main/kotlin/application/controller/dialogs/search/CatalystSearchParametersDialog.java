package application.controller.dialogs.search;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import application.model.Coordinate;
import application.model.Utils;
import application.model.rules.Rule;
import application.model.search.catsrc.CatalystSearchParameters;
import application.model.simulation.Grid;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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

        // HBox to store the stuf
        HBox box = new HBox();
        box.setSpacing(5);
        grid.add(box, 0, 10);

        // Load the catalysts from catagolue?
        TextField catagolueUrl = new TextField();
        catagolueUrl.setPromptText("Enter Catagolue URL");
        box.getChildren().add(catagolueUrl);

        // Button to load
        Button loadFromCatagolue = new Button("Load from Catagolue");
        loadFromCatagolue.setOnAction(event -> {
            try {
                InputStream stream = new URL(catagolueUrl.getText().replace("census",
                        "textcensus")).openStream();
                Scanner s = new Scanner(stream);

                Grid grid;
                String stillLife, line;

                ArrayList<String> stillLives = new ArrayList<>();
                while (s.hasNextLine()) {
                    line = s.nextLine();
                    if (line.startsWith("\"xs")) {
                        stillLife = line.split(",")[0].replace("\"", "");
                        stillLives.add(stillLife);
                    }
                }

                stillLives.sort((s1, s2) -> {
                    int num1 = Integer.parseInt(Utils.matchRegex("xs([0-9]+)_", s1, 0, 1));
                    int num2 = Integer.parseInt(Utils.matchRegex("xs([0-9]+)_", s2, 0, 1));
                    return Integer.compare(num1, num2);
                });

                int count = 0;
                for (String sl: stillLives) {
                    grid = new Grid();
                    grid.fromApgcode(sl, new Coordinate());

                    if (catalystTextArea.getText().length() > 0)
                        catalystTextArea.setText(catalystTextArea.getText() + "\n" + grid.toRLE());
                    else
                        catalystTextArea.setText(grid.toRLE());

                    if (count++ == 100) return;
                }

            } catch (IOException exception) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("An error occurred when scraping catagolue.");
                alert.setContentText("Perhaps you are not connected to the internet or the " +
                        "census URL inputted is invalid?");
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
                alert.showAndWait();


                LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING,
                        exception.getMessage());
            }
        });
        box.getChildren().add(loadFromCatagolue);
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
