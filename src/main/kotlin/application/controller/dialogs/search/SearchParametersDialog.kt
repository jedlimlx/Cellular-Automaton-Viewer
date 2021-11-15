package application.controller.dialogs.search

import application.model.search.SearchParameters
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox

/**
 * A dialog to obtain search parameters from a search dialog
 */
abstract class SearchParametersDialog : Dialog<Any?>() {
    protected var spinnerThreads: Spinner<Int>
    protected var grid: GridPane

    // Search parameters for the search program
    open val searchParameters: SearchParameters? = null

    // Number of threads to use
    var numThreads = 0
        protected set

    init {
        super.setResizable(true)
        val vbox = VBox()
        vbox.spacing = 5.0

        // Grid to store all the stuff
        grid = GridPane()
        grid.hgap = 5.0
        grid.vgap = 5.0
        vbox.children.add(grid)

        // Label for the number of threads
        val labelThreads = Label("Number of Threads:")
        grid.add(labelThreads, 0, 0)

        // The number of threads to run the search program
        val numThreadsFactory: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(1, 15, 5)
        spinnerThreads = Spinner()
        spinnerThreads.isEditable = true
        spinnerThreads.valueFactory = numThreadsFactory
        grid.add(spinnerThreads, 0, 1)

        // Button to confirm parameters
        val confirmButton = Button("Confirm Parameters")
        confirmButton.onAction = EventHandler { event: ActionEvent? ->
            // If successful
            if (confirmParameters()) {
                // Close the dialog
                super.setResult(java.lang.Boolean.TRUE)
                super.close()
            }
        }

        // Place in VBox so child class can have as many widgets as it likes in the grid
        vbox.children.add(confirmButton)

        // Allows closing with close button
        val window = super.getDialogPane().scene.window
        window.onCloseRequest = EventHandler { window.hide() }

        // Setting the VBox as the main content
        super.getDialogPane().content = vbox
    }

    // Updates the search parameters based on the inputted parameters
    open fun confirmParameters(): Boolean {
        numThreads = spinnerThreads.value
        return false
    }
}