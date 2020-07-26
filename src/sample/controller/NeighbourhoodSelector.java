package sample.controller;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
public class NeighbourhoodSelector extends GridPane {
    private final int[][] weights;
    private final int RANGE;
    private final Button[][] weightButtons;

    public NeighbourhoodSelector(int range) {
        super();

        // Formatting the grid
        super.setHgap(5);
        super.setVgap(5);

        // Array to store the weights and buttons
        weights = new int[2 * range + 1][2 * range + 1];
        weightButtons = new Button[2 * range + 1][2 * range + 1];

        // Create buttons
        RANGE = range;
        for (int i = 0; i < 2 * range + 1; i++) {
            for (int j = 0; j < 2 * range + 1; j++) {
                int finalI = i, finalJ = j;  // Apparently necessary

                weights[i][j] = 0;
                weightButtons[i][j] = new Button("0");
                weightButtons[i][j].setOnAction((event) -> weightButtonClickedHandler(finalI, finalJ));
                super.add(weightButtons[i][j], i, j);
            }
        }

        // Button to reset the weights
        Button resetButton = new Button("Reset");
        resetButton.setOnAction((event) -> resetWeights());
        resetButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);  // Allow button to grow
        super.add(resetButton, 0, 2 * range + 1,  2 * range + 1, 2 * range + 1);
    }

    public void weightButtonClickedHandler(int i, int j) {
        weights[i][j] = (weights[i][j] + 11) % 21 - 10;  // Loop from -10 to 10
        weightButtons[i][j].setText(weights[i][j] + "");  // Set text to weight
    }

    public void resetWeights() {
        for (int i = 0; i < 2 * RANGE + 1; i++) {
            for (int j = 0; j < 2 * RANGE + 1; j++) {
                weights[i][j] = 0;
                weightButtons[i][j].setText("0");
            }
        }
    }

    public int[][] getWeights() {
        return weights;
    }
}
