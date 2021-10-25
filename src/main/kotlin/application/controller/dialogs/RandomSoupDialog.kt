package application.controller.dialogs

import application.model.SymmetryGenerator
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.WindowEvent
import java.lang.Boolean
import kotlin.Any
import kotlin.Int
import kotlin.Number
import kotlin.String

class RandomSoupDialog(numStates: Int, density: Int, symmetry: String, states: List<Int?>) : Dialog<Any?>() {
    private val densitySlider: Slider
    private val symmetryCombobox: ComboBox<String>
    private val statesCheckBox: ArrayList<CheckBox>

    val density: Int
        get() = densitySlider.value.toInt()

    val symmetry: String
        get() = symmetryCombobox.value

    val states: ArrayList<Int>
        get() {
            val states = ArrayList<Int>()
            for (i in statesCheckBox.indices) {
                if (statesCheckBox[i].isSelected) states.add(i + 1)
            }
            return states
        }

    init {
        super.setTitle("Random Soup Settings")
        super.setResizable(true)

        val vBox = VBox()
        vBox.spacing = 5.0

        val densityLabel = Label("Density:")
        vBox.children.add(densityLabel)

        // Slider for density
        densitySlider = Slider()
        densitySlider.min = 0.0
        densitySlider.max = 100.0
        densitySlider.value = density.toDouble()
        densitySlider.valueProperty()
            .addListener { _: ObservableValue<out Number>, _: Number, newVal: Number ->
                densitySlider.value = newVal.toInt().toDouble()
            } // Round to integer
        vBox.children.add(densitySlider)
        densityLabel.textProperty().bind(
            SimpleStringProperty("Density: ").concat(
                densitySlider.valueProperty()
            )
        )
        vBox.children.add(Label("Symmetry:"))

        // Combobox for symmetries
        symmetryCombobox = ComboBox()
        symmetryCombobox.value = symmetry
        symmetryCombobox.items.addAll(*SymmetryGenerator.symmetries)
        vBox.children.add(symmetryCombobox)

        // Selector for states
        statesCheckBox = ArrayList()
        for (i in 1 until numStates) {
            val checkBox = CheckBox(i.toString() + "")
            if (states.contains(i)) checkBox.isSelected = true
            statesCheckBox.add(checkBox)
            vBox.children.add(checkBox)
        }

        // Okay Button
        val button = Button("Confirm Settings")
        button.onAction = EventHandler {
            // Close the dialog
            super.setResult(Boolean.TRUE)
            super.close()
        }
        vBox.children.add(button)
        super.getDialogPane().content = vBox

        // Allows closing with close button
        val window = super.getDialogPane().scene.window
        window.onCloseRequest = EventHandler { window.hide() }
    }
}