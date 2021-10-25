package application.controller.dialogs

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.chart.XYChart.Series
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Dialog
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import javafx.stage.FileChooser
import javafx.stage.WindowEvent
import java.io.FileWriter
import java.io.IOException
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

class PopulationGraphDialog(population: ArrayList<Int>) : Dialog<Any?>() {
    init {
        super.setTitle("Population Graph")
        super.setResizable(true)

        // VBox to hold the graph
        val vbox = VBox()
        vbox.isFillWidth = true
        vbox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)

        // Constructing the graph
        val xAxis = NumberAxis()
        xAxis.label = "Generation"

        val yAxis = NumberAxis()
        yAxis.label = "Population"

        val populationGraph = LineChart(xAxis, yAxis)
        populationGraph.title = "Population Graph"

        val populationSeries = Series<Number, Number>()
        populationSeries.name = "Population"

        for (i in population.indices) {
            val data = XYChart.Data<Number, Number>(i, population[i])
            val rect = Rectangle(0.0, 0.0)
            rect.isVisible = false
            data.node = rect
            populationSeries.data.add(data)
        }

        // Fill window
        populationGraph.prefWidthProperty().bind(super.widthProperty().subtract(50))
        populationGraph.prefHeightProperty().bind(super.heightProperty().subtract(50))
        populationGraph.data.add(populationSeries)
        vbox.children.add(populationGraph)

        // Button to save data
        val saveData = Button("Save Data")
        saveData.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.title = "Save Population Data"
            fileChooser.extensionFilters.add(
                FileChooser.ExtensionFilter(
                    "Comma-Separated Values Files (*.csv)", "*.csv"
                )
            )

            val file = fileChooser.showSaveDialog(null)
            try {
                val writer = FileWriter(file)
                writer.write("Generation,Population\n")
                for (i in population.indices) writer.write("$i,${population[i]}\n")
                writer.close()
            } catch (exception: IOException) {
                LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, exception.message)

                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Error in saving data"
                alert.headerText = "The operation was unsuccessful."
                alert.contentText = exception.message
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
                alert.showAndWait()
            }
        }
        vbox.children.add(saveData)

        // Fill window
        vbox.prefWidthProperty().bind(super.widthProperty())
        vbox.prefHeightProperty().bind(super.heightProperty())
        super.getDialogPane().content = vbox

        // Allows closing with close button
        val window = super.getDialogPane().scene.window
        window.onCloseRequest = EventHandler { window.hide() }
    }
}