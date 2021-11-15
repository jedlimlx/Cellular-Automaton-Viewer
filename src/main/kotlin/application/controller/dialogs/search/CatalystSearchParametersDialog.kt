package application.controller.dialogs.search

import application.model.Coordinate
import application.model.Utils
import application.model.rules.Rule
import application.model.search.SearchParameters
import application.model.search.catsrc.CatalystSearchParameters
import application.model.simulation.Grid
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

class CatalystSearchParametersDialog(private val rule: Rule, private val target: Grid,
                                     private val coordinateList: List<Coordinate>) : SearchParametersDialog() {
    private val catalystTextArea: TextArea

    private val spinnerMaxRepeatTime: Spinner<Int>
    private val spinnerNumCatalyst: Spinner<Int>

    private val rotateCatalystCheckbox: CheckBox
    private val flipCatalystCheckbox: CheckBox

    override var searchParameters: CatalystSearchParameters? = null
        private set

    init {
        // Label for maximum repeat time
        val maxRepeatTimeLabel = Label("Maximum Repeat Time:")
        grid.add(maxRepeatTimeLabel, 0, 2)

        // The maximum repeat time
        val maxRepeatTimeFactory: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(10, 20000, 200)
        spinnerMaxRepeatTime = Spinner()
        spinnerMaxRepeatTime.isEditable = true
        spinnerMaxRepeatTime.setValueFactory(maxRepeatTimeFactory)
        grid.add(spinnerMaxRepeatTime, 0, 3)

        // Label for the number of catalysts to use
        val numCatalystLabel = Label("Number of Catalysts:")
        grid.add(numCatalystLabel, 0, 4)

        // The number of catalysts to use
        val numCatalystFactory: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(1, 20, 3)
        spinnerNumCatalyst = Spinner()
        spinnerNumCatalyst.isEditable = true
        spinnerNumCatalyst.setValueFactory(numCatalystFactory)
        grid.add(spinnerNumCatalyst, 0, 5)

        // Should the catalysts be rotated?
        rotateCatalystCheckbox = CheckBox("Rotate Catalyst?")
        grid.add(rotateCatalystCheckbox, 0, 6)

        // Should the catalysts be flipped?
        flipCatalystCheckbox = CheckBox("Flip Catalyst?")
        grid.add(flipCatalystCheckbox, 0, 7)

        // Label for the list of catalysts to use
        val catalystsToUseLabel = Label("Enter the catalysts to use:")
        grid.add(catalystsToUseLabel, 0, 8)

        // The list of catalysts to use
        catalystTextArea = TextArea()
        catalystTextArea.promptText = "Enter the catalysts to use (one line per headless RLE)."
        grid.add(catalystTextArea, 0, 9)

        // HBox to store the stuf
        val box = HBox()
        box.spacing = 5.0
        grid.add(box, 0, 10)

        // Load the catalysts from catagolue?
        val catagolueUrl = TextField()
        catagolueUrl.promptText = "Enter Catagolue URL"
        box.children.add(catagolueUrl)

        // Button to load
        val loadFromCatagolue = Button("Load from Catagolue")
        loadFromCatagolue.onAction = EventHandler {
            try {
                val stream = URL(catagolueUrl.text.replace("census","textcensus")).openStream()

                val s = Scanner(stream)
                var grid: Grid
                var stillLife: String
                var line: String

                val stillLives = ArrayList<String>()
                while (s.hasNextLine()) {
                    line = s.nextLine()
                    if (line.startsWith("\"xs")) {
                        stillLife = line.split(",".toRegex()).toTypedArray()[0].replace("\"", "")
                        stillLives.add(stillLife)
                    }
                }

                stillLives.sortWith { s1: String, s2: String ->
                    val num1 = Utils.matchRegex("xs([0-9]+)_", s1, 0, 1).toInt()
                    val num2 = Utils.matchRegex("xs([0-9]+)_", s2, 0, 1).toInt()
                    num1.compareTo(num2)
                }

                for ((count, sl) in stillLives.withIndex()) {
                    grid = Grid()
                    grid.fromApgcode(sl, Coordinate())
                    if (catalystTextArea.text.isNotEmpty())
                        catalystTextArea.text = "${catalystTextArea.text}\n${grid.toRLE()}" else
                        catalystTextArea.text = grid.toRLE()
                    if (count == 100) return@EventHandler
                }
            } catch (exception: IOException) {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Error!"
                alert.headerText = "An error occurred when scraping catagolue."
                alert.contentText = "Perhaps you are not connected to the internet or the " +
                        "census URL inputted is invalid?"
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
                alert.showAndWait()

                LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).log(
                    Level.WARNING,
                    exception.message
                )
            }
        }

        box.children.add(loadFromCatagolue)
    }

    override fun confirmParameters(): Boolean {
        super.confirmParameters()

        val catalysts: MutableList<Grid> = ArrayList()
        for (token in catalystTextArea.text.trim().split("\\s*\n\\s*".toRegex()).toTypedArray()) {
            val catalyst = Grid()
            catalyst.fromRLE(token, Coordinate())
            catalysts.add(catalyst)
        }

        searchParameters = CatalystSearchParameters(
            spinnerMaxRepeatTime.value,
            spinnerNumCatalyst.value, false, rotateCatalystCheckbox.isSelected,
            flipCatalystCheckbox.isSelected, catalysts, target, coordinateList, rule
        )
        return true
    }
}