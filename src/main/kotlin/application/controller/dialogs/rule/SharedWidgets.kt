package application.controller.dialogs.rule

import application.controller.NeighbourhoodSelector
import javafx.beans.value.ObservableValue

object SharedWidgets {
    val neighbourhoodSelector = NeighbourhoodSelector(2)
        get() {
            val newNeighbourhoodSelector = NeighbourhoodSelector(2)

            // Bidirectional Binding
            field.spinnerRange.valueProperty()
                .addListener { _: ObservableValue<out Int>, _: Int, newValue: Int ->
                    newNeighbourhoodSelector.spinnerRange.valueFactory.setValue(newValue)
                }
            newNeighbourhoodSelector.spinnerRange.valueProperty()
                .addListener { _: ObservableValue<out Int>, _: Int, newValue: Int ->
                    field.spinnerRange.valueFactory.setValue(newValue)
                }

            return newNeighbourhoodSelector
        }
}