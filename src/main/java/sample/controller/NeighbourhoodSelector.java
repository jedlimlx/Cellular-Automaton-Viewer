package sample.controller;

import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import org.javatuples.Pair;
import sample.model.Coordinate;

import java.util.ArrayList;

/**
 * A widget that selects the neighbourhood and weights for a rule. <br>
 * <br>
 * Example Usage: <br>
 * <pre>
 * NeighbourhoodSelector selector = new NeighbourhoodSelector(2);
 *
 * Button button = new Button("Load Neighbourhood");
 * button.setOnAction(() -> {
 *     System.out.println(selector.getNeighbourhoodAndWeights());
 * });
 * </pre>
 */
public class NeighbourhoodSelector extends GridPane {
    private int range;

    private Button resetButton;
    private Spinner<Integer> spinnerRange;

    private int[][] weights;
    private Button[][] weightButtons;

    private Runnable onWeightsChanged;

    private boolean recursed = false;

    /**
     * Creates a new NeighbourhoodSelector with an initial range of 2
     * @param range The initial range
     */
    public NeighbourhoodSelector(int range) {
        super();

        // Formatting the grid
        super.setHgap(5);
        super.setVgap(5);

        // Array to store the weights and buttons
        weights = new int[2 * range + 1][2 * range + 1];
        weightButtons = new Button[2 * range + 1][2 * range + 1];

        // Spinner to change the range
        spinnerRange = new Spinner<>();
        spinnerRange.setPromptText("Enter Range");
        spinnerRange.setEditable(true);
        spinnerRange.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10));
        spinnerRange.valueProperty().addListener((obs, oldValue, newValue) -> reloadButtons(newValue));
        super.add(spinnerRange, 0, 0, 2 * range + 1, 1);

        // Button to reset the weights
        resetButton = new Button("Reset");
        resetButton.setOnAction((event) -> resetWeights());
        resetButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);  // Allow button to grow
        super.add(resetButton, 0, 2 * range + 2,  2 * range + 1, 2 * range + 1);

        this.range = range;
        reloadButtons(range);
    }

    /**
     * Reloads the buttons in the neighbourhood selector with a new range
     * @param range The new range for the neighbourhood selector
     */
    public void reloadButtons(int range) {
        this.range = range;

        Button[][] prevWeightButtons = weightButtons;
        weights = new int[2 * range + 1][2 * range + 1];
        weightButtons = new Button[2 * range + 1][2 * range + 1];

        // Remove old buttons
        for (Button[] btnLst: prevWeightButtons) {
            for (Button button: btnLst) {
                super.getChildren().remove(button);
            }
        }

        // Create buttons
        for (int i = 0; i < 2 * range + 1; i++) {
            for (int j = 0; j < 2 * range + 1; j++) {
                int finalI = i, finalJ = j;  // Apparently necessary

                weights[i][j] = 0;
                weightButtons[i][j] = new Button("0");
                weightButtons[i][j].setOnAction((event) -> weightButtonClickedHandler(finalI, finalJ));
                super.add(weightButtons[i][j], i, j + 1);
            }
        }

        super.getChildren().remove(resetButton);
        super.add(resetButton, 0, 2 * range + 2,  2 * range + 1, 1);

        super.getChildren().remove(spinnerRange);
        super.add(spinnerRange, 0, 0, 2 * range + 1, 1);
    }

    /**
     * Handles the clicking of the buttons
     * @param i The index of the row that the button is in
     * @param j The index of the column that the button is in
     */
    private void weightButtonClickedHandler(int i, int j) {
        weights[i][j] = (weights[i][j] + 11) % 21 - 10;  // Loop from -10 to 10
        weightButtons[i][j].setText(weights[i][j] + "");  // Set text to weight

        onWeightsChanged.run();
    }

    /**
     * Resets the weights in the neighbourhood selector
     */
    public void resetWeights() {
        for (int i = 0; i < 2 * range + 1; i++) {
            for (int j = 0; j < 2 * range + 1; j++) {
                weights[i][j] = 0;
                weightButtons[i][j].setText("0");
            }
        }

        onWeightsChanged.run();
    }

    /**
     * Sets the weights of the neighbourhood selector
     * @param weights The weights of the neighbourhood selector
     */
    public void setWeights(int[][] weights) {
        this.weights = weights;

        for (int i = 0; i < 2 * range + 1; i++) {
            for (int j = 0; j < 2 * range + 1; j++) {
                this.weights[i][j] = weights[i][j];
                weightButtons[i][j].setText(weights[i][j] + "");
            }
        }

        onWeightsChanged.run();
    }

    /**
     * Gets the raw weights as an 2D integer array
     * @return Returns the raw weights as an 2D integer array
     */
    public int[][] getRawWeights() {
        return weights;
    }

    /**
     * Gets the selected neighbourhood and weights.
     * @return Returns a pair. The 1st entry is the neighbourhood and the 2nd are the weights.
     */
    public Pair<Coordinate[], int[]> getNeighbourhoodAndWeights() {
        // Getting neighbourhood and weights
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        ArrayList<Integer> weights = new ArrayList<>();
        for (int i = 0; i < 2 * 2 + 1; i++) {
            for (int j = 0; j < 2 * 2 + 1; j++) {
                if (this.weights[i][j] != 0) {
                    neighbourhood.add(new Coordinate(i - 2, j - 2));
                    weights.add(this.weights[i][j]);
                }
            }
        }

        if (!neighbourhood.isEmpty()) {
            boolean weightsNeeded = false;

            // Converting to array
            Coordinate[] neighbourhoodArray = new Coordinate[neighbourhood.size()];
            int[] weightsArray = new int[weights.size()];
            for (int i = 0; i < weights.size(); i++) {
                weightsArray[i] = weights.get(i);
                neighbourhoodArray[i] = neighbourhood.get(i);

                // Check if weights are needed
                if (weights.get(i) != 0 && weights.get(i) != 1)
                    weightsNeeded = true;
            }

            // Checking if weights are needed (if there are weight that are not 1 & 0)
            if (weightsNeeded)
                return new Pair<>(neighbourhoodArray, weightsArray);
            else
                return new Pair<>(neighbourhoodArray, null);
        }

        return null;
    }

    /**
     * Gets the spinner that stores the range
     * @return Returns the spinner that stores the range
     */
    public Spinner<Integer> getSpinnerRange() {
        return spinnerRange;
    }

    /**
     * The method to call when the weights change
     * @param onWeightsChanged The method to call when weights change
     */
    public void setOnWeightsChanged(Runnable onWeightsChanged) {
        this.onWeightsChanged = onWeightsChanged;
    }
}
