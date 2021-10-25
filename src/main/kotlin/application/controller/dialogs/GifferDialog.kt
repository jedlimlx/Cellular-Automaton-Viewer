package application.controller.dialogs

import application.model.Giffer
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import javafx.scene.layout.GridPane
import java.lang.Boolean
import kotlin.Any
import kotlin.Int

class GifferDialog : Dialog<Any?>() {
    private val spinnerTiming: Spinner<Int>
    private val spinnerCellSize: Spinner<Int>
    var giffer: Giffer? = null
        private set

    init {
        // Set titles
        super.setTitle("Giffer Dialog")
        super.setResizable(true)
        val grid = GridPane()
        grid.alignment = Pos.CENTER
        grid.hgap = 5.0
        grid.vgap = 5.0

        // Label for the time between frames (ms)
        val labelTiming = Label("Time between frames (ms):")
        grid.add(labelTiming, 0, 0)

        // The timing between frames
        val valueFactoryTiming: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(20, 100, 50)
        spinnerTiming = Spinner()
        spinnerTiming.isEditable = true
        spinnerTiming.valueFactory = valueFactoryTiming
        grid.add(spinnerTiming, 0, 1)

        // Label for the pixel size
        val labelSize = Label("Pixel size:")
        grid.add(labelSize, 0, 2)

        // The cell size of the gif
        val valueFactoryCellSize: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(1, 10, 2)
        spinnerCellSize = Spinner()
        spinnerCellSize.isEditable = true
        spinnerCellSize.valueFactory = valueFactoryCellSize
        grid.add(spinnerCellSize, 0, 3)

        // Button the confirm the rulestring
        val confirmButton = Button("Confirm Settings")
        confirmButton.onAction = EventHandler { confirmSettings() }
        grid.add(confirmButton, 0, 4)

        // Allows closing with close button
        val window = super.getDialogPane().scene.window
        window.onCloseRequest = EventHandler { window.hide() }

        // Setting the grid pane as the main content
        super.getDialogPane().content = grid
    }

    private fun confirmSettings() {
        // Create a giffer based on the settings
        giffer = Giffer(spinnerCellSize.value, spinnerTiming.value)

        // Close the dialog
        super.setResult(Boolean.TRUE)
        super.close()
    }
}