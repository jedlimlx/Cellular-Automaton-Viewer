package application.controller

import application.model.Coordinate
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import javafx.scene.layout.GridPane
import org.javatuples.Pair

/**
 * A widget that selects the neighbourhood and weights for a rule. <br></br>
 * <br></br>
 * Example Usage: <br></br>
 * <pre>
 * NeighbourhoodSelector selector = new NeighbourhoodSelector(2);
 *
 * Button button = new Button("Load Neighbourhood");
 * button.setOnAction(() -> {
 * System.out.println(selector.getNeighbourhoodAndWeights());
 * });
</pre> *
 */
class NeighbourhoodSelector(range: Int) : GridPane() {
    private var range: Int
    private val resetButton: Button

    /**
     * Gets the spinner that stores the range
     * @return Returns the spinner that stores the range
     */
    val spinnerRange: Spinner<Int>

    /**
     * Gets the raw weights as an 2D integer array
     * @return Returns the raw weights as an 2D integer array
     */
    var rawWeights: Array<IntArray>
        private set

    var onWeightsChanged: Runnable? = null
    private var weightButtons: Array<Array<Button?>>

    /**
     * Reloads the buttons in the neighbourhood selector with a new range
     * @param range The new range for the neighbourhood selector
     */
    private fun reloadButtons(range: Int) {
        this.range = range
        val prevWeightButtons = weightButtons
        rawWeights = Array(2 * range + 1) { IntArray(2 * range + 1) }
        weightButtons = Array(2 * range + 1) { arrayOfNulls(2 * range + 1) }

        // Remove old buttons
        for (btnLst in prevWeightButtons) {
            for (button in btnLst) {
                super.getChildren().remove(button)
            }
        }

        // Create buttons
        for (i in 0 until 2 * range + 1) {
            for (j in 0 until 2 * range + 1) {
                rawWeights[i][j] = 0
                weightButtons[i][j] = Button("0")
                weightButtons[i][j]!!.onAction = EventHandler { weightButtonClickedHandler(i, j) }

                super.add(weightButtons[i][j], i, j + 1)
            }
        }

        super.getChildren().remove(resetButton)
        super.add(resetButton, 0, 2 * range + 2, 2 * range + 1, 1)

        super.getChildren().remove(spinnerRange)
        super.add(spinnerRange, 0, 0, 2 * range + 1, 1)
    }

    /**
     * Handles the clicking of the buttons
     * @param i The index of the row that the button is in
     * @param j The index of the column that the button is in
     */
    private fun weightButtonClickedHandler(i: Int, j: Int) {
        rawWeights[i][j] = (rawWeights[i][j] + 11) % 21 - 10 // Loop from -10 to 10
        weightButtons[i][j]!!.text = rawWeights[i][j].toString() + "" // Set text to weight
        onWeightsChanged!!.run()
    }

    /**
     * Resets the weights in the neighbourhood selector
     */
    private fun resetWeights() {
        for (i in 0 until 2 * range + 1) {
            for (j in 0 until 2 * range + 1) {
                rawWeights[i][j] = 0
                weightButtons[i][j]!!.text = "0"
            }
        }
        if (onWeightsChanged != null) onWeightsChanged!!.run()
    }

    /**
     * Sets the weights of the neighbourhood selector
     * @param weights The weights of the neighbourhood selector
     */
    fun setWeights(weights: Array<IntArray>) {
        rawWeights = weights
        for (i in 0 until 2 * range + 1) {
            for (j in 0 until 2 * range + 1) {
                rawWeights[i][j] = weights[i][j]
                weightButtons[i][j]!!.text = weights[i][j].toString() + ""
            }
        }
        onWeightsChanged!!.run()
    }

    /**
     * Gets the selected neighbourhood and weights.
     * @return Returns a pair. The 1st entry is the neighbourhood and the 2nd are the weights.
     */
    val neighbourhoodAndWeights: Pair<Array<Coordinate?>, IntArray?>?
        get() {
            // Getting neighbourhood and weights
            val neighbourhood = ArrayList<Coordinate>()
            val weights = ArrayList<Int>()
            for (i in 0 until 2 * 2 + 1) {
                for (j in 0 until 2 * 2 + 1) {
                    if (rawWeights[i][j] != 0) {
                        neighbourhood.add(Coordinate(i - 2, j - 2))
                        weights.add(rawWeights[i][j])
                    }
                }
            }

            if (neighbourhood.isNotEmpty()) {
                var weightsNeeded = false

                // Converting to array
                val neighbourhoodArray = arrayOfNulls<Coordinate>(neighbourhood.size)
                val weightsArray = IntArray(weights.size)
                for (i in weights.indices) {
                    weightsArray[i] = weights[i]
                    neighbourhoodArray[i] = neighbourhood[i]

                    // Check if weights are needed
                    if (weights[i] != 0 && weights[i] != 1) weightsNeeded = true
                }

                // Checking if weights are needed (if there are weight that are not 1 & 0)
                return if (weightsNeeded) Pair(neighbourhoodArray, weightsArray) else Pair(neighbourhoodArray, null)
            }
            return null
        }

    /**
     * Creates a new NeighbourhoodSelector with an initial range of 2
     * @param range The initial range
     */
    init {
        // Formatting the grid
        super.setHgap(5.0)
        super.setVgap(5.0)

        // Array to store the weights and buttons
        rawWeights = Array(2 * range + 1) { IntArray(2 * range + 1) }
        weightButtons = Array(2 * range + 1) { arrayOfNulls(2 * range + 1) }

        // Spinner to change the range
        spinnerRange = Spinner()
        spinnerRange.promptText = "Enter Range"
        spinnerRange.isEditable = true
        spinnerRange.valueFactory = IntegerSpinnerValueFactory(0, 10)
        spinnerRange.valueProperty()
            .addListener { _: ObservableValue<out Int>?, _: Int?, newValue: Int -> reloadButtons(newValue) }
        super.add(spinnerRange, 0, 0, 2 * range + 1, 1)

        // Button to reset the weights
        resetButton = Button("Reset")
        resetButton.onAction = EventHandler { resetWeights() }
        resetButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE) // Allow button to grow

        super.add(resetButton, 0, 2 * range + 2, 2 * range + 1, 2 * range + 1)
        this.range = range
        reloadButtons(range)
    }
}