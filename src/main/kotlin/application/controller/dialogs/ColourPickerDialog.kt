package application.controller.dialogs

import application.model.rules.Rule
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ColorPicker
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import java.lang.Boolean
import kotlin.Any
import kotlin.Array

/**
 * Dialog for the colour picker
 */
class ColourPickerDialog(rule: Rule, colours: Array<Color>?) : Dialog<Any?>() {
    private val colourPickers: Array<ColorPicker>

    val colours: Array<Color>
        get() = colourPickers.map { it.value }.toTypedArray()

    init {
        super.setTitle("Colour Picker")
        val gridPane = GridPane()
        gridPane.vgap = 5.0
        gridPane.hgap = 5.0

        colourPickers = Array(rule.numStates) { i ->
            gridPane.add(Label("State $i:"), 0, i)

            if (colours == null) ColorPicker(rule.getColour(i))
            else ColorPicker(colours[i])
        }
        colourPickers.forEachIndexed { i, colourPicker -> gridPane.add(colourPicker, 1, i) }

        val confirm = Button("Confirm")
        confirm.onAction = EventHandler {
            // Close the dialog
            super.setResult(Boolean.TRUE)
            super.close()
        }
        gridPane.add(confirm, 0, rule.numStates)

        val cancel = Button("Cancel")
        cancel.onAction = EventHandler {
            // Close the dialog
            super.setResult(Boolean.FALSE)
            super.close()
        }
        gridPane.add(cancel, 1, rule.numStates)
        super.getDialogPane().content = gridPane

        // Allows closing with close button
        val window = super.getDialogPane().scene.window
        window.onCloseRequest = EventHandler { window.hide() }
    }
}