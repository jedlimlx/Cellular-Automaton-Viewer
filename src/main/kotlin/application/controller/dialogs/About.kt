package application.controller.dialogs

import javafx.scene.web.WebView
import javafx.scene.web.WebEngine
import javafx.scene.layout.GridPane
import application.model.Giffer
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import javafx.scene.layout.VBox
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart.Series
import javafx.scene.chart.XYChart
import javafx.stage.FileChooser
import java.io.FileWriter
import java.io.IOException
import java.util.logging.LogManager
import javafx.beans.value.ObservableValue
import javafx.beans.property.SimpleStringProperty
import application.model.SymmetryGenerator
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.stage.WindowEvent

class About : Dialog<Any?>() {
    init {
        super.setTitle("About")
        super.setResizable(true)

        val view = WebView()
        val engine = view.engine
        engine.load("https://jedlimlx.github.io/Cellular-Automaton-Viewer/")

        super.getDialogPane().content = view

        // Allows closing with close button
        val window = super.getDialogPane().scene.window
        window.onCloseRequest = EventHandler { window.hide() }
    }
}