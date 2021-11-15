package application.controller

import application.controller.dialogs.*
import application.controller.dialogs.rule.RuleDialog
import application.controller.dialogs.search.*
import application.model.*
import application.model.Cell
import application.model.rules.ApgtableGeneratable
import application.model.rules.Rule
import application.model.rules.RuleFamily
import application.model.rules.hrot.HROT
import application.model.rules.ruleloader.ColourDirective
import application.model.rules.ruleloader.RuleLoader
import application.model.rules.ruleloader.RuleNameDirective
import application.model.search.catsrc.CatalystSearch
import application.model.search.csearch.BruteForceSearch
import application.model.search.ocgar2.AgarSearch
import application.model.search.rulesrc.RuleSearch
import application.model.simulation.Grid
import application.model.simulation.Simulator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Group
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.ConcurrentModificationException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class MainController {
    @FXML private var drawingPane: Pane = Pane()
    @FXML private var scrollPane: ScrollPane = ScrollPane()
    @FXML private var statusLabel: Label = Label()
    @FXML private var startSimulationButton: Button = Button()
    @FXML private var simInsideSelectionButton: Button = Button()
    @FXML private var simOutsideSelectionButton: Button = Button()
    @FXML private var secondaryToolbar: ToolBar = ToolBar()
    @FXML private var recordingImage: ImageView = ImageView()
    @FXML private var playButtonImage: ImageView = ImageView()
    @FXML private var playButtonImage1: ImageView = ImageView()
    @FXML private var playButtonImage2: ImageView = ImageView()
    @FXML private var gridLinesMenuItem: CheckMenuItem = CheckMenuItem()

    private var selectionRectangle: SelectionRectangle? = null // Rectangle that represents selection box
    private val dialog = RuleDialog() // Dialog to set rule
    private val gifferDialog = GifferDialog() // Dialog to create *.gifs
    private var mode: Mode = Mode.DRAWING // Mode (Drawing, Selecting, Panning)

    // Gets the simulator
    var simulator = Simulator(HROT("R2,C2,S6-9,B7-8,NM")) // Simulator to simulate rule
        private set
    private var stateButtons: MutableList<Button> = ArrayList() // Buttons to switch between states
    private var cellList: MutableMap<Coordinate, Cell> = HashMap() // List of cell objects
    private var deadCellsSet: MutableSet<Coordinate> = HashSet()
    private var deadCellsCache: LRUCache<Coordinate, Cell> = LRUCache(200) // LRU Cache of dead cells

    private var gridLines = Group() // The grid lines of the pattern editor
    private var boundedGridLines: Group? = null // The lines to mark the bounded grid
    private var pasteSelection = Group() // The group that renders the stuff to be pasted
    private var pasteStuff: Grid? = null // The stuff to paste

    private var populationList: MutableList<Int> = ArrayList() // The population list

    private var density = 50 // The density of the random soup
    private var symmetry = "C1" // The symmetry of the random soup
    private var statesToInclude = listOf(1) // The states to include in the random soup
    private var randomSoupDialog: RandomSoupDialog? = null // Dialog to adjust random soup settings

    private var simulationTime = 0
    private var visualisationTime = 0 // Time taken for visualisation and simulation
    private var minSimTime = 0 // Minimum time for one step
    private var stepSize = 1 // Step size of the simulation
    private var currentState = 1 // State to draw with
    private var colours: MutableMap<Rule, Array<Color>> = HashMap() // Colours to use for each state

    private var recording = false // Is the recording on?
    private var visualisationDone = true // Is the visualisation done?
    private var showGridLines = false // Are the grid lines being shown?
    private var simulationMode = SimulationMode.PAUSED

    private lateinit var settings: JSONObject // Store the settings

    private lateinit var giffer: Giffer // For writing to a *.gif
    private lateinit var logger: Logger

    @FXML
    fun initialize() {
        // Initialise logger
        logger = LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME)
        logger.log(Level.INFO, "GUI Initialising...")

        // Initialise variables
        deadCellsCache.setDeleteFunc { coordinate: Coordinate, cell: Cell ->
            if (cell.state != 0) return@setDeleteFunc

            // Destroy the cell object (remove all references to it so it is garbage collected)
            removeCellObject(coordinate.x, coordinate.y)
            drawingPane.children.remove(cell.rectangle)
        }
        deadCellsCache.setCheckValid { cell: Cell -> cell.state == 0 }

        // Create selection rectangle and set properties
        selectionRectangle = SelectionRectangle(CELL_SIZE)
        drawingPane.children.add(selectionRectangle)

        // Linking with Action class to handle undo / redo
        Action.setController(this)

        // Setting zoom
        drawingPane.scaleX = 5.0
        drawingPane.scaleY = 5.0

        // Disable scrollbars and scrolling
        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

        // Bind methods
        scrollPane.onScroll = EventHandler { event: ScrollEvent -> changeZoomHandler(event) }
        scrollPane.addEventFilter(ScrollEvent.ANY) { event: ScrollEvent -> changeZoomHandler(event) }
        scrollPane.onKeyPressed = EventHandler { event: KeyEvent -> keyPressedHandler(event) }
        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED) { event: KeyEvent -> keyPressedHandler(event) }

        // Move the center of the ScrollPane
        scrollPane.hvalue = 0.2
        scrollPane.vvalue = 0.2

        // Setting the scroll pane in focus
        drawingPane.requestFocus()

        // Load the correct number of state buttons
        reloadStateButtons()

        // Start Simulation Thread
        val simulationThread = Thread { runSimulation() }
        simulationThread.name = "Simulation Thread"
        simulationThread.isDaemon = true
        simulationThread.start()

        // Setting the rule of the rule dialog
        dialog.rule = simulator.rule as RuleFamily

        // Creating grid lines
        var i = 0
        while (i < WIDTH) {
            val lineX = Line().apply {
                stroke = Color.GREY
                strokeWidth = 0.15 * CELL_SIZE
                startX = i.toDouble()
                endX = i.toDouble()
                startY = 0.0
                endY = HEIGHT.toDouble()
                toFront()
            }

            gridLines.children.add(lineX)

            val lineY = Line().apply {
                stroke = Color.GREY
                strokeWidth = 0.15 * CELL_SIZE
                startX = 0.0
                endX = HEIGHT.toDouble()
                startY = i.toDouble()
                endY = i.toDouble()
                toFront()
            }

            gridLines.children.add(lineY)

            if (i % 10 == 0) {
                lineX.strokeWidth = 0.2 * CELL_SIZE
                lineY.strokeWidth = 0.2 * CELL_SIZE
            }

            i += CELL_SIZE
        }

        gridLines.isVisible = false
        drawingPane.children.add(gridLines)

        // Pane for pasting
        drawingPane.children.add(pasteSelection)
        pasteSelection.isVisible = true

        // Loading settings
        try {  // Reading settings from settings file
            settings = JSONObject(JSONTokener(FileInputStream(SETTINGS_FILE)))

            // Grid lines
            if (settings.getBoolean("grid_lines")) {
                showGridLines = true
                gridLines.isVisible = true
                gridLinesMenuItem.isSelected = true
            }

            // Setting the rule
            val m = ObjectMapper()
            m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            m.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)

            simulator.rule = m.readValue(settings["rule"].toString(), Rule::class.java)
            drawBoundedGrid()

            // Setting the colours
            val typeFactory = m.typeFactory
            val ruleListType = typeFactory.constructCollectionType(
                ArrayList::class.java, Rule::class.java
            )

            val colourListType = typeFactory.constructCollectionType(
                ArrayList::class.java, Array<String>::class.java
            )

            val rules = m.readValue<List<Rule>>(
                settings["rules"].toString(), ruleListType
            )

            val colours = m.readValue<List<Array<String>>>(
                settings["colours"].toString(), colourListType
            )

            colours.forEachIndexed { index, it ->
                this.colours[rules[index]] = it.map {
                    val tokens = it.split(" ").toTypedArray()
                    Color.rgb(
                        (tokens[0].toDouble() * 255).toInt(),
                        (tokens[1].toDouble() * 255).toInt(),
                        (tokens[2].toDouble() * 255).toInt()
                    )
                }.toTypedArray()
            }

            // Setting the rule of the rule dialog
            dialog.rule = simulator.rule as RuleFamily

            // Reloads the state buttons
            reloadStateButtons()
        } catch (exception: IOException) {
            settings = JSONObject()
            logger.log(Level.WARNING, exception.message)
        }
    }

    @FXML
    fun reloadStateButtons() {
        for (button in stateButtons)
            secondaryToolbar.items.remove(button)

        // Clearing stateButtons
        stateButtons.clear()

        // Add buttons to the secondary toolbar
        for (i in 0 until simulator.rule.numStates) {
            val tooltip = Tooltip(simulator.rule.getName(i))
            secondaryToolbar.items.add(Button("" + i).apply {
                this.tooltip = tooltip
                onAction = EventHandler {
                    currentState = i
                    mode = Mode.DRAWING
                }

                stateButtons.add(this)
            })
        }
    }

    @FXML // Handle the mouse events
    fun mouseDraggedHandler(event: MouseEvent) {
        if (mode == Mode.DRAWING) {
            Action.addAction()
            setCell(
                snapToGrid(event.x.toInt()),
                snapToGrid(event.y.toInt()),
                currentState
            )
        } else if (mode == Mode.SELECTING) {
            selectionRectangle!!.select(
                Coordinate(
                    snapToGrid(event.x.toInt()),
                    snapToGrid(event.y.toInt())
                )
            )
        }
    }

    @FXML
    fun mouseDragStartHandler(event: MouseEvent) {
        if (mode == Mode.SELECTING) {
            selectionRectangle!!.unselect()
            selectionRectangle!!.toFront()
            val startSelection = Coordinate(
                snapToGrid(event.x.toInt()),
                snapToGrid(event.y.toInt())
            )
            selectionRectangle!!.select(startSelection, startSelection)
        } else if (mode == Mode.PASTING) {
            Action.addAction()
            insertCells(pasteStuff, convertToGrid(event.x.toInt()), convertToGrid(event.y.toInt()))
            pasteSelection.isVisible = false
            mode = Mode.SELECTING
        }
    }

    @FXML
    fun mouseDragDoneHandler(event: MouseEvent) {
        var endSelection: Coordinate? = null
        if (mode == Mode.SELECTING && selectionRectangle!!.isVisible) {
            endSelection = Coordinate(event.x.toInt(), event.y.toInt())
        }
        if (mode != Mode.PASTING) {
            if (selectionRectangle!!.start != null && endSelection != null &&
                (endSelection.subtract(selectionRectangle!!.start).x < 1 ||
                        endSelection.subtract(selectionRectangle!!.start).y < 1)
            ) {
                selectionRectangle!!.unselect()
            }
        }
    }

    @FXML
    fun mouseMovedHandler(event: MouseEvent) {
        if (mode == Mode.PASTING) {
            pasteSelection.translateX = snapToGrid(event.x.toInt()).toDouble()
            pasteSelection.translateY = snapToGrid(event.y.toInt()).toDouble()
        }
    }

    @FXML // Generates random soup in the selection box
    fun generateRandomSoup() {
        if (selectionRectangle!!.isSelecting) {
            val states = statesToInclude.toIntArray()

            // Generate the soup
            val soup = SymmetryGenerator.generateSymmetry(
                symmetry, density, states,
                selectionRectangle!!.end.x - selectionRectangle!!.start.x + 1,
                selectionRectangle!!.end.y - selectionRectangle!!.start.y + 1
            )

            // Insert the cells in the pane (automatically inserted in the simulator)
            insertCells(soup, selectionRectangle!!.start.x, selectionRectangle!!.start.y)

            // Move the grid lines and selection to the front
            selectionRectangle!!.toFront()
            gridLines.toFront()
        } else {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "No area selected!"
            alert.contentText = "No area has been selected!"
            alert.showAndWait()
        }
    }

    @FXML // Flips selected cells horizontally
    fun flipHorizontalHandler() {
        Action.addAction()

        // Reflect cells in the grid
        simulator.reflectCellsX(selectionRectangle!!.start, selectionRectangle!!.end)
        renderCells(selectionRectangle!!.start, selectionRectangle!!.end)

        // Move the grid lines and selection to the front
        selectionRectangle!!.toFront()
        gridLines.toFront()
    }

    @FXML // Flips selected cells vertically
    fun flipVerticalHandler() {
        Action.addAction()

        // Reflect cells in the grid
        simulator.reflectCellsY(selectionRectangle!!.start, selectionRectangle!!.end)
        renderCells(selectionRectangle!!.start, selectionRectangle!!.end)

        // Move the grid lines and selection to the front
        selectionRectangle!!.toFront()
        gridLines.toFront()
    }

    @FXML // Rotates the selected cells clockwise
    fun rotateCWHandler() {
        Action.addAction()

        // Rotate the cells in the grid
        val start = selectionRectangle!!.start
        var end = selectionRectangle!!.end
        simulator.rotateCW(selectionRectangle!!.start, selectionRectangle!!.end)
        renderCells(selectionRectangle!!.start, selectionRectangle!!.end)

        // Rotate selection rectangle
        if ((end.x - start.x) % 2 == 1) end = Coordinate(end.x + 1, end.y)
        if ((end.y - start.y) % 2 == 1) end = Coordinate(end.x, end.y + 1)

        val centerX = (end.x - start.x) / 2 + start.x
        val centerY = (end.y - start.y) / 2 + start.y

        val dxEnd = centerX - end.x
        val dyEnd = centerY - end.y

        val dxStart = centerX - start.x
        val dyStart = centerY - start.y

        selectionRectangle!!.select(
            convertToScreen(Coordinate(centerX - dyStart, centerY + dxEnd)),
            convertToScreen(Coordinate(centerX - dyEnd, centerY + dxStart))
        )
        renderCells(selectionRectangle!!.start, selectionRectangle!!.end)

        // Move the grid lines and selection to the front
        selectionRectangle!!.toFront()
        gridLines.toFront()
    }

    @FXML // Rotates the selected cells counter-clockwise
    fun rotateCCWHandler() {
        Action.addAction()

        // Rotate the cells in the grid
        val start = selectionRectangle!!.start
        var end = selectionRectangle!!.end

        simulator.rotateCCW(selectionRectangle!!.start, selectionRectangle!!.end)
        renderCells(selectionRectangle!!.start, selectionRectangle!!.end)

        // Rotate selection rectangle
        if ((end.x - start.x) % 2 == 1) end = Coordinate(end.x + 1, end.y)
        if ((end.y - start.y) % 2 == 1) end = Coordinate(end.x, end.y + 1)

        val centerX = (end.x - start.x) / 2 + start.x
        val centerY = (end.y - start.y) / 2 + start.y

        val dxEnd = centerX - end.x
        val dyEnd = centerY - end.y

        val dxStart = centerX - start.x
        val dyStart = centerY - start.y

        selectionRectangle!!.select(
            convertToScreen(Coordinate(centerX + dyEnd, centerY - dxStart)),
            convertToScreen(Coordinate(centerX + dyStart, centerY - dxEnd))
        )
        renderCells(selectionRectangle!!.start, selectionRectangle!!.end)

        // Move the grid lines and selection to the front
        selectionRectangle!!.toFront()
        gridLines.toFront()
    }

    @FXML // Sets the generation
    fun setGeneration() {
        // Getting the generation number from the user
        val inputDialog = TextInputDialog(simulator.generation.toString() + "").apply {
            title = "Set Generation"
            headerText = "Enter the generation number:"
        }
        inputDialog.showAndWait()

        if (inputDialog.result != null) {
            try {
                simulator.generation = inputDialog.result.toInt()
                updateStatusText()
            } catch (exception: NumberFormatException) {
                // Ensure it's an integer
                Alert(Alert.AlertType.ERROR).apply {
                    title = "Error!"
                    headerText = inputDialog.result.toString() + " is not a valid integer!"
                }.showAndWait()
            }
        }
    }

    @FXML // Sets the step size
    fun setStepSize() {
        // Getting the step size from the user
        val inputDialog = TextInputDialog(stepSize.toString() + "")
        inputDialog.title = "Set Step Size:"
        inputDialog.headerText = "Enter the step size:"
        inputDialog.showAndWait()

        if (inputDialog.result != null) {
            try {
                stepSize = inputDialog.result.toInt()
                updateStatusText()
            } catch (exception: NumberFormatException) {
                // Ensure it's an integer
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Error!"
                alert.headerText = inputDialog.result.toString() + " is not a valid integer!"
                alert.showAndWait()
            }
        }
    }

    @FXML // Sets the maximum simulation speed (gen/s)
    fun setSimSpeed() {
        // Getting the generation number from the user
        val inputDialog = TextInputDialog((1000 / (minSimTime + 1)).toString() + "").apply {
            title = "Set Maximum Simulation Speed (gen/s)"
            headerText = "Enter the simulation speed (gen/s):"
        }
        inputDialog.showAndWait()

        // Check if user clicked "Cancel"
        if (inputDialog.result != null) {
            try {
                minSimTime = 1000 / inputDialog.result.toInt()
            } catch (exception: NumberFormatException) {
                // Ensure it's an integer
                Alert(Alert.AlertType.ERROR).apply {
                    title = "Error!"
                    headerText = inputDialog.result.toString() + " is not a valid integer!"
                }.showAndWait()
            }
        }
    }

    @FXML // Clears the cell cache
    fun clearCellsCache() {
        while (deadCellsSet.iterator().hasNext()) {
            val cell = deadCellsSet.iterator().next() // No ConcurrentModficationException
            // if (deadCellsCache.get(cell).getState() != 0) return;

            // Destroy the cell object (remove all references to it so it is garbage collected)
            drawingPane.children.remove(cellList[cell]!!.rectangle)
            removeCellObject(cell.x, cell.y)
            deadCellsSet.remove(cell)
        }
    }

    @FXML // Opens a dialog to adjust the colours
    fun adjustColours() {
        val dialog = ColourPickerDialog(simulator.rule, colours[simulator.rule])
        dialog.showAndWait()

        if (dialog.result === java.lang.Boolean.TRUE) {
            colours[simulator.rule] = dialog.colours
            scrollPane.style = "-fx-background: rgb(${(dialog.colours[0].red * 255).toInt()}," +
                    "${(dialog.colours[0].green * 255).toInt()}," +
                    "${(dialog.colours[0].blue * 255).toInt()})"
            drawingPane.style = "-fx-background: rgb(${(dialog.colours[0].red * 255).toInt()}," +
                    "${(dialog.colours[0].green * 255).toInt()}," +
                    "${(dialog.colours[0].blue * 255).toInt()})"
            renderCells()
        }
    }

    @FXML // Views the population graph
    fun viewPopulationGraph() {
        PopulationGraphDialog(populationList as ArrayList).show()
    }

    @FXML // Changing the random soup settings
    fun changeRandomSoupSettings() {
        randomSoupDialog = RandomSoupDialog(
            simulator.rule.numStates, density, symmetry,
            statesToInclude
        )
        randomSoupDialog!!.showAndWait()

        if (randomSoupDialog!!.result === java.lang.Boolean.TRUE) {
            density = randomSoupDialog!!.density
            symmetry = randomSoupDialog!!.symmetry
            statesToInclude = randomSoupDialog!!.states
        }
    }

    // Displays information about the current rule
    @get:FXML
    val ruleInformation: Unit
        get() {
            val contentText = StringBuilder()
            val information = (simulator.rule as RuleFamily).ruleInfo
            for (key in information.keys) {
                contentText.append(key).append(": ").append(information[key]).append("\n")
            }
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.title = "Rule Information"
            alert.headerText = "Information"
            alert.contentText = contentText.toString()
            alert.dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
            alert.showAndWait()
        }

    @FXML // Sets the rule directory
    fun setRuleDirectory() {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = "Set Rule Directory"

        val file = directoryChooser.showDialog(null)
        RuleLoader.RULE_DIRECTORY = file.path
        settings.put("rule_directory", file.path)

        Alert(Alert.AlertType.INFORMATION).apply {
            title = "Operation Successful"
            headerText = "Operation Successful."
            contentText = "Please restart the application for the changes to take effect."
        }.showAndWait()
    }

    @FXML // Increases the step size
    fun increaseStepSize() {
        stepSize++
    }

    @FXML // Decreases the step size
    fun decreaseStepSize() {
        if (stepSize > 1) stepSize--
    }

    // Function to set cell at (x, y) to a certain state
    fun setCell(x: Int, y: Int, state: Int, updateSimulator: Boolean = true, updateColours: Boolean = false) {
        if (simulator.rule.boundedGrid != null &&  // Skip cells outside the bounded grid
            simulator.rule.boundedGrid.atEdge(Coordinate(x, y))
        ) return

        // Get the cell object at the specified coordinate
        val prevCell = getCellObject(x, y)
        if (prevCell == null && state != 0) {  // Insert a new cell if one doesn't already exist
            // Create cell
            val cell = Rectangle().apply {
                this.x = x.toDouble()
                this.y = y.toDouble()
                width = CELL_SIZE.toDouble()
                height = CELL_SIZE.toDouble()
            }

            if (colours[simulator.rule] == null) cell.fill = simulator.rule.getColour(state) else cell.fill =
                colours[simulator.rule]!![state]
            cell.toBack()

            // Add cell to pane and cell list
            drawingPane.children.add(cell)
            addCellObject(x, y, Cell(x, y, state, cell))
        } else if (prevCell != null && (prevCell.state != state || updateColours)) {
            if (prevCell.state == 0 && state != 0) deadCellsSet.remove(prevCell.coordinate)
            if (colours[simulator.rule] == null) prevCell.rectangle.fill =
                simulator.rule.getColour(state) else prevCell.rectangle.fill = colours[simulator.rule]!![state]
            prevCell.state = state
            if (state == 0) deadCellsSet.add(Coordinate(x, y))
        }

        // Add cell to simulator
        if (updateSimulator) simulator.setCell(convertToGrid(x), convertToGrid(y), state)
    }

    fun insertCells(cellsToInsert: Grid?, x: Int, y: Int) {
        cellsToInsert!!.iterateCells { coord: Coordinate ->
            val newCell = coord.add(Coordinate(x, y))
            setCell(
                convertToScreen(newCell.x), convertToScreen(newCell.y),
                cellsToInsert.getCell(coord)
            )
        }
    }

    // Renders all cells
    fun renderCells() {
        selectionRectangle!!.toFront()
        gridLines.toFront()
        for (coordinate in cellList.keys) {
            setCell(
                convertToGrid(coordinate.x),
                convertToGrid(coordinate.y),
                simulator.getCell(coordinate), updateSimulator = false, updateColours = true
            )
        }

        simulator.iterateCells { coordinate: Coordinate ->
            setCell(
                convertToGrid(coordinate.x),
                convertToGrid(coordinate.y),
                simulator.getCell(coordinate), updateSimulator = false, updateColours = true
            )
        }
    }

    // Renders cells between the start and end coordinate
    fun renderCells(startSelection: Coordinate, endSelection: Coordinate) {
        for (i in startSelection.x until endSelection.x + 1) {
            for (j in startSelection.y until endSelection.y + 1) {
                setCell(convertToScreen(i), convertToScreen(j), simulator.getCell(i, j)) // Render the new cells
            }
        }
    }

    fun addCellObject(x: Int, y: Int, cell: Cell) {
        cellList[Coordinate(x, y)] = cell
    }

    fun removeCellObject(x: Int, y: Int) {
        cellList.remove(Coordinate(x, y))
    }

    fun getCellObject(x: Int, y: Int): Cell? {
        return cellList[Coordinate(x, y)]
    }

    fun updateStatusText() {
        val simulationString = String.format(
            "Simulation Speed: %.2f step/s",
            1000.0 / (visualisationTime + simulationTime)
        )

        statusLabel.text = "Generation: ${simulator.generation}, " +
                "$simulationString, Population: ${simulator.population}"
    }

    // Draws bounded grid for the current rule if necessary
    fun drawBoundedGrid() {
        drawingPane.children.remove(boundedGridLines)
        if (simulator.rule.boundedGrid == null) return

        // Creating grid lines
        boundedGridLines = Group()
        val horizontalLine1 = Line().apply {
            stroke = Color.GREY
            strokeWidth = CELL_SIZE.toDouble()

            if (simulator.rule.boundedGrid.height != 0 &&
                simulator.rule.boundedGrid.width != 0
            ) {
                startX = 1800 / CELL_SIZE - CELL_SIZE / 2.0
                endX = 1800 / CELL_SIZE + simulator.rule.boundedGrid.width + CELL_SIZE / 2.0
            } else if (simulator.rule.boundedGrid.width == 0) {
                startX = 0.0
                endX = WIDTH.toDouble()
            }

            startY = 1800 / CELL_SIZE - CELL_SIZE / 2.0
            endY = 1800 / CELL_SIZE - CELL_SIZE / 2.0
            toFront()
        }

        boundedGridLines!!.children.add(horizontalLine1)

        val horizontalLine2 = Line().apply {
            stroke = Color.GREY
            strokeWidth = CELL_SIZE.toDouble()

            if (simulator.rule.boundedGrid.height != 0 &&
                simulator.rule.boundedGrid.width != 0
            ) {
                startX = 1800 / CELL_SIZE - CELL_SIZE / 2.0
                endX = 1800 / CELL_SIZE + simulator.rule.boundedGrid.width + CELL_SIZE / 2.0
            } else if (simulator.rule.boundedGrid.width == 0) {
                startX = 0.0
                endX = WIDTH.toDouble()
            }

            startY = 1800 / CELL_SIZE + simulator.rule.boundedGrid.height + CELL_SIZE / 2.0
            endY = 1800 / CELL_SIZE + simulator.rule.boundedGrid.height + CELL_SIZE / 2.0
            toFront()
        }

        boundedGridLines!!.children.add(horizontalLine2)

        val verticalLine1 = Line().apply {
            stroke = Color.GREY
            strokeWidth = CELL_SIZE.toDouble()
            startX = 1800 / CELL_SIZE - CELL_SIZE / 2.0
            endX = 1800 / CELL_SIZE - CELL_SIZE / 2.0

            if (simulator.rule.boundedGrid.height != 0 &&
                simulator.rule.boundedGrid.width != 0
            ) {
                startY = 1800 / CELL_SIZE - CELL_SIZE / 2.0
                endY = 1800 / CELL_SIZE + simulator.rule.boundedGrid.height + CELL_SIZE / 2.0
            } else if (simulator.rule.boundedGrid.height == 0) {
                startY = 0.0
                endY = HEIGHT.toDouble()
            }

            toFront()
        }

        boundedGridLines!!.children.add(verticalLine1)

        val verticalLine2 = Line().apply {
            stroke = Color.GREY
            strokeWidth = CELL_SIZE.toDouble()
            startX = 1800 / CELL_SIZE + simulator.rule.boundedGrid.width + CELL_SIZE / 2.0
            endX = 1800 / CELL_SIZE + simulator.rule.boundedGrid.width + CELL_SIZE / 2.0

            if (simulator.rule.boundedGrid.height != 0 &&
                simulator.rule.boundedGrid.width != 0
            ) {
                startY = 1800 / CELL_SIZE - CELL_SIZE / 2.0
                endY = 1800 / CELL_SIZE + simulator.rule.boundedGrid.height + CELL_SIZE / 2.0
            } else if (simulator.rule.boundedGrid.height == 0) {
                startY = 0.0
                endY = HEIGHT.toDouble()
            }

            toFront()
        }

        boundedGridLines!!.children.add(verticalLine2)
        drawingPane.children.add(boundedGridLines)
    }

    @FXML // Zooming in and out of the canvas
    fun changeZoomHandler(event: ScrollEvent) {
        val SCALE_DELTA = 1.2
        event.consume()
        if (event.deltaY == 0.0) {
            return
        }
        val scaleFactor = if (event.deltaY > 0) SCALE_DELTA else 1 / SCALE_DELTA
        val zoomOperator = AnimatedZoomOperator()
        zoomOperator.zoom(drawingPane, scaleFactor, event.x, event.y)
    }

    @FXML // Updates cells
    fun updateCells() {
        synchronized(this) {
            visualisationDone = false
            var cellsChanged: MutableSet<Coordinate> = HashSet()
            val startTime = System.currentTimeMillis()
            try {
                for (i in 0 until stepSize) {
                    if (simulationMode == SimulationMode.IN_SELECTION)
                        simulator.step {
                                coordinate: Coordinate -> coordinate.x >= selectionRectangle!!.start.x &&
                                coordinate.y >= selectionRectangle!!.start.y &&
                                coordinate.x <= selectionRectangle!!.end.x && coordinate.y <= selectionRectangle!!.end.y
                        }
                    else if (simulationMode == SimulationMode.OUTSIDE_SELECTION)
                        simulator.step {
                                coordinate: Coordinate -> coordinate.x < selectionRectangle!!.start.x ||
                                coordinate.y < selectionRectangle!!.start.y ||
                                coordinate.x > selectionRectangle!!.end.x ||
                                coordinate.y > selectionRectangle!!.end.y
                        }
                    else simulator.step()

                    if (stepSize > 1) cellsChanged.addAll(simulator.cellsChanged) else cellsChanged =
                        simulator.cellsChanged
                    populationList.add(simulator.population)
                }
            } catch (exception: ConcurrentModificationException) {
                logger.log(Level.WARNING, exception.message)
            }

            simulationTime = (System.currentTimeMillis() - startTime).toInt()
            val finalCellsChanged: Set<Coordinate> = cellsChanged
            Platform.runLater {
                try {
                    val startTime2 = System.currentTimeMillis()

                    // Only update cells that changed (for speed)
                    for (cell in finalCellsChanged) {
                        setCell(convertToScreen(cell.x), convertToScreen(cell.y), simulator.getCell(cell))
                    }

                    // Move the grid lines and selection to the front
                    if (selectionRectangle!!.isVisible) selectionRectangle!!.toFront()
                    if (gridLines.isVisible) gridLines.toFront()
                    visualisationTime = (System.currentTimeMillis() - startTime2).toInt()
                } catch (exception: ConcurrentModificationException) { // Catch an exception that will hopefully not happen
                    logger.log(Level.WARNING, exception.message)
                    exception.printStackTrace()
                    simulationMode = SimulationMode.PAUSED

                    Alert(Alert.AlertType.ERROR).apply {
                        title = "Error!"
                        headerText = "ConcurrentModificationException cause in updateCells."
                        contentText = "Please report this as a bug."
                    }.showAndWait()
                }

                // Ensure that the minimum simulation time is not "surpassed"
                val waitTime = minSimTime - (simulationTime + visualisationTime)
                if (waitTime > 0) {
                    wait(waitTime)
                    visualisationTime += waitTime
                }

                // Update the variable to say that the visualisation is done
                visualisationDone = true

                // Update the status label
                updateStatusText()
            }

            // Add to the gif if the recording is on
            if (recording) {
                giffer.addGrid(
                    selectionRectangle!!.start, selectionRectangle!!.end,
                    simulator.getCells(selectionRectangle!!.start, selectionRectangle!!.end),
                    simulator.rule
                )
            }
        }
    }

    // Runs simulation
    fun runSimulation() {
        var num = 1
        while (true) {
            if (simulationMode != SimulationMode.PAUSED) {
                // Wait for the visualisation to be done
                // To avoid ConcurrentModificationException
                // TODO (Using locks would be more elegant)
                while (!visualisationDone) wait(1)

                updateCells()
            } else {
                val finalNum = num
                Platform.runLater(object : Runnable {
                    override fun run() {
                        playButtonImage.image = Image(
                            javaClass.getResourceAsStream(
                                "/icon/GliderPlayBtn" + (finalNum + 1) + ".png"
                            )
                        )
                    }
                })
                num++
                num %= 4 // Cycle from 1 - 4
                wait(75)
            }
            Platform.runLater {
                try {
                    updateStatusText()
                } catch (ignored: ConcurrentModificationException) { }
            }
        }
    }

    @FXML // Toggles simulation on and off
    fun toggleSimulation() {
        Action.addAction()
        if (simulationMode == SimulationMode.PAUSED) {
            simulationMode = SimulationMode.RUNNING
            playButtonImage.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/StopButtonEater.png"
                )
            )
            playButtonImage1.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/SimInSelection.png"
                )
            )
            playButtonImage2.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/SimOutSelection.png"
                )
            )
        } else {
            simulationMode = SimulationMode.PAUSED
            playButtonImage.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/GliderPlayBtn1.png"
                )
            )
            playButtonImage1.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/SimInSelection.png"
                )
            )
            playButtonImage2.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/SimOutSelection.png"
                )
            )
        }
    }

    @FXML // Toggles simulation on and off
    fun toggleSimulation1() {
        Action.addAction()
        if (simulationMode == SimulationMode.PAUSED) {
            simulationMode = SimulationMode.IN_SELECTION
            playButtonImage.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/GliderPlayBtn1.png"
                )
            )
            playButtonImage1.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/StopInSelection.png"
                )
            )
            playButtonImage2.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/SimOutSelection.png"
                )
            )
        } else {
            simulationMode = SimulationMode.PAUSED
            playButtonImage.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/GliderPlayBtn1.png"
                )
            )
            playButtonImage1.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/SimInSelection.png"
                )
            )
            playButtonImage2.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/SimOutSelection.png"
                )
            )
        }
    }

    @FXML // Toggles simulation on and off
    fun toggleSimulation2() {
        Action.addAction()
        if (simulationMode == SimulationMode.PAUSED) {
            simulationMode = SimulationMode.OUTSIDE_SELECTION
            playButtonImage.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/GliderPlayBtn1.png"
                )
            )
            playButtonImage1.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/SimInSelection.png"
                )
            )
            playButtonImage2.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/StopOutSelection.png"
                )
            )
        } else {
            simulationMode = SimulationMode.PAUSED
            playButtonImage.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/GliderPlayBtn1.png"
                )
            )
            playButtonImage1.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/SimInSelection.png"
                )
            )
            playButtonImage2.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/SimOutSelection.png"
                )
            )
        }
    }

    @FXML // Change mode to drawing
    fun drawingMode() {
        scrollPane.isPannable = false
        mode = Mode.DRAWING
    }

    @FXML // Change mode to panning
    fun panMode() {
        scrollPane.isPannable = true
        mode = Mode.PANNING
    }

    @FXML // Change mode to selecting
    fun selectionMode() {
        scrollPane.isPannable = false
        mode = Mode.SELECTING
    }

    @FXML // Toggle grid lines
    fun toggleGridLines() {
        showGridLines = !showGridLines
        gridLines.isVisible = showGridLines
    }

    @FXML // Starts the rule dialog
    fun startRuleDialog() {
        dialog.showAndWait()

        // Check if the user actually hit `Confirm Rule`
        if (dialog.rule != null) {
            Action.addAction()

            // Set the rule
            simulator.rule = dialog.rule

            // Reload the number of state buttons
            reloadStateButtons()

            // Re-render all the cells
            if (colours[dialog.rule!!] != null) {
                scrollPane.style = "-fx-background: rgb(${colours[dialog.rule!!]!![0].red * 255}," +
                        "${colours[dialog.rule!!]!![0].green * 255}," +
                        "${colours[dialog.rule!!]!![0].blue * 255})"
                drawingPane.style = "-fx-background: rgb(${colours[dialog.rule!!]!![0].red * 255}," +
                        "${colours[dialog.rule!!]!![0].green * 255}," +
                        "${colours[dialog.rule!!]!![0].blue * 255})"
            } else {
                scrollPane.style = "-fx-background: rgb(${dialog.rule!!.getColour(0).red * 255}," +
                        "${dialog.rule!!.getColour(0).green * 255}," +
                        "${dialog.rule!!.getColour(0).blue * 255})"
                drawingPane.style = "-fx-background: rgb(${dialog.rule!!.getColour(0).red * 255},${
                        dialog.rule!!.getColour(0).green * 255},${
                        dialog.rule!!.getColour(0).blue * 255})"
            }
            renderCells()

            // Drawing the bounded grid
            drawBoundedGrid()
        }
    }

    @FXML // Starts the rule search dialog
    fun startRuleSearchDialog() {
        // Dialog to get the search parameters
        val parametersDialog = RuleSearchParametersDialog(
            simulator.getCells(selectionRectangle!!.start, selectionRectangle!!.end),
            simulator.rule
        )
        parametersDialog.showAndWait()

        if (parametersDialog.result === java.lang.Boolean.TRUE) {  // If the operation wasn't cancelled
            val ruleSearch = RuleSearch(parametersDialog.searchParameters)
            ruleSearch.searchThreaded(Int.MAX_VALUE, parametersDialog.numThreads)

            val resultsDialog = RuleSearchResultsDialog(this, ruleSearch)
            resultsDialog.show()
        }
    }

    @FXML // Starts the catalyst search dialog
    fun startCatalystSearchDialog() {
        // Dialog to get the search parameters
        val cellCoordinates: MutableList<Coordinate> = ArrayList()
        for (i in selectionRectangle!!.start.x until selectionRectangle!!.end.x + 1) {
            for (j in selectionRectangle!!.start.y until selectionRectangle!!.end.y + 1) {
                cellCoordinates.add(Coordinate(i, j))
            }
        }

        val parametersDialog = CatalystSearchParametersDialog(simulator.rule, simulator.deepCopy(), cellCoordinates)
        parametersDialog.showAndWait()

        if (parametersDialog.result === java.lang.Boolean.TRUE) {  // If the operation wasn't cancelled
            val catalystSearch = CatalystSearch(parametersDialog.searchParameters)
            catalystSearch.searchThreaded(Int.MAX_VALUE, parametersDialog.numThreads)

            val resultsDialog = CatalystSearchResultsDialog(this, catalystSearch)
            resultsDialog.show()
        }
    }

    @FXML // Starts the brute force search dialog
    fun startBruteForceSearchDialog() {
        // Dialog to get the search parameters
        val parametersDialog = BruteForceSearchParametersDialog(simulator.rule)
        parametersDialog.showAndWait()

        if (parametersDialog.result === java.lang.Boolean.TRUE) {  // If the operation wasn't cancelled
            val bruteForceSearch = BruteForceSearch(parametersDialog.searchParameters)
            bruteForceSearch.searchThreaded(Int.MAX_VALUE, parametersDialog.numThreads)

            val resultsDialog = BruteForceSearchResultsDialog(this, bruteForceSearch)
            resultsDialog.show()
        }
    }

    @FXML // Starts the agar search dialog
    fun startAgarSearchDialog() {
        // Dialog to get the search parameters
        val parametersDialog = AgarSearchParametersDialog(simulator.rule)
        parametersDialog.showAndWait()

        if (parametersDialog.result === java.lang.Boolean.TRUE) {  // If the operation wasn't cancelled
            val agarSearch = AgarSearch(parametersDialog.searchParameters)
            agarSearch.searchThreaded(Int.MAX_VALUE, parametersDialog.numThreads)

            val resultsDialog = AgarSearchResultsDialog(this, agarSearch)
            resultsDialog.show()
        }
    }

    @FXML // Provides information about CAViewer
    fun startAboutDialog() {
        val about = About()
        about.showAndWait()
    }

    @FXML // Generates an APGTable
    fun generateAPGTable() {
        if (simulator.rule is RuleFamily) {
            try {
                val fileChooser = FileChooser()
                fileChooser.title = "Save APGTable"
                fileChooser.extensionFilters.add(
                    FileChooser.ExtensionFilter(
                        "Ruletable Files (*.rule)", "*.rule"
                    )
                )
                val file = fileChooser.showSaveDialog(null)

                // If operation is cancelled
                if (file != null) {
                    if (simulator.rule !is ApgtableGeneratable)
                        throw UnsupportedOperationException("This rulespace does not support apgtable generation!")

                    val ruleDirectives = (simulator.rule as ApgtableGeneratable).generateApgtable()
                    val ruleLoader = RuleLoader()
                    ruleLoader.addDirective(
                        RuleNameDirective(
                            "@RULE ${file.name.replace(".rule", "")}"
                        )
                    )

                    val colourDirective = StringBuilder("@COLORS\n")
                    for (i in 0 until simulator.rule.numStates) {
                        colourDirective.append(i).append(" ").append((simulator.rule.getColour(i).red * 255).toInt())
                            .append(" ").append(
                            (simulator.rule.getColour(i).green * 255).toInt()
                        ).append(" ").append((simulator.rule.getColour(i).blue * 255).toInt()).append("\n")
                    }
                    ruleLoader.addDirective(ColourDirective(colourDirective.toString()))

                    ruleDirectives.forEach { ruleLoader.addRuleDirective(it) }

                    val fileWriter = FileWriter(file)
                    fileWriter.write(ruleLoader.export())
                    fileWriter.close()

                    Alert(Alert.AlertType.INFORMATION).apply {
                        title = "Operation successful!"
                        headerText = "The operation was successful."
                        contentText = "The operation was successful. " +
                                "The apgtable has been saved to ${file.absolutePath}."
                        dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
                    }.showAndWait()
                }
            } catch (exception: UnsupportedOperationException) {
                logger.log(Level.WARNING, exception.message)

                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Error in generating APGTable"
                alert.headerText = "APGTable generation is not supported by this rule / rulespace!"
                alert.contentText = exception.message
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
                alert.showAndWait()
            } catch (exception: IOException) {
                logger.log(Level.WARNING, exception.message)

                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Error in generating APGTable"
                alert.headerText = "The operation was unsuccessful."
                alert.contentText = exception.message
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
                alert.showAndWait()
            }
        } else {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error in generating APGTable"
            alert.headerText = "APGTable generation is not supported by this rule / rulespace!"
            alert.contentText = "The rule / rulespace selected does not support APGTable generation." +
                    "If you require this feature, please request it or write it yourself"
            alert.showAndWait()
        }
    }

    @FXML // Identifies selected object
    fun identifySelection() {
        val simulator = Simulator(this.simulator.rule)
        simulator.insertCells(
            this.simulator.getCells(selectionRectangle!!.start, selectionRectangle!!.end),
            Coordinate() // Ensure location is the same
        )
        simulator.generation = this.simulator.generation // Ensure the generation is the same

        // Results of the identification
        val results = simulator.identify()
        Alert(Alert.AlertType.INFORMATION).apply {
            title = "Identification Results"

            if (results != null) {
                headerText = results.toString()

                // Additional Information
                val contentString = StringBuilder()
                val additionalInfo = results.additionalInfo()
                additionalInfo.keys.forEach {
                    contentString.append(it).append(": ").append(additionalInfo[it]).append("\n")
                }

                contentText = contentString.toString()
                dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
            } else {
                headerText = "Identification Failed :("
            }
        }.showAndWait()
    }

    @FXML // Toggles the recording
    fun toggleRecording() {
        if (recording) {
            // Change icon for recording
            recordingImage.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/RecordLogo.png"
                )
            )

            val fileChooser = FileChooser() // Find out where to save the *.gif
            fileChooser.title = "Save *.gif"
            fileChooser.extensionFilters.add(
                FileChooser.ExtensionFilter(
                    "GIF Files (*.gif)", "*.gif"
                )
            )

            val file = fileChooser.showSaveDialog(null)
            if (file == null) {  // Quit if the operation is cancelled
                recording = !recording
                return
            }

            val thread = Thread {
                if (!giffer.toGIF(file)) {
                    Platform.runLater {
                        Alert(Alert.AlertType.ERROR).apply {
                            title = "Error in generating *.gif"
                            headerText = "The operation was unsuccessful."
                            contentText = "The operation was unsuccessful. " +
                                    "If you suspect a bug, please report it."
                            dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
                        }.showAndWait()
                    }
                } else {
                    Platform.runLater {
                        Alert(Alert.AlertType.INFORMATION).apply {
                            title = "Operation successful!"
                            headerText = "The operation was successful."
                            contentText = "The operation was successful. " +
                                    "The *.gif has been saved to ${file.absolutePath}."
                            dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
                        }.showAndWait()
                    }
                }
            }
            thread.name = "Giffer Thread"
            thread.start()
        } else {
            // Change icon for recording
            recordingImage.image = Image(
                javaClass.getResourceAsStream(
                    "/icon/RecordIcon2.png"
                )
            )

            // Get giffer from the dialog
            gifferDialog.showAndWait()

            // Check if the operation was cancelled
            if (gifferDialog.result === java.lang.Boolean.FALSE) return
            giffer = gifferDialog.giffer!!
        }

        recording = !recording
    }

    @FXML // Closes the application
    fun closeApplication() {
        onApplicationClosed()
        Platform.exit()
    }

    // Saving settings when the application is closed
    fun onApplicationClosed() {
        try {
            val m = ObjectMapper()

            // Configuring settings
            settings.put("grid_lines", showGridLines)
            settings.put("rule", JSONObject(m.writeValueAsString(simulator.rule)))

            // Writing colours
            val rules = ArrayList<Rule>()
            val colours = ArrayList<Array<String>>()
            for (rule in this.colours.keys) {
                rules.add(rule)

                this.colours[rule]?.map {
                    "${it.red} ${it.green} ${it.blue}"
                }?.toTypedArray()
            }

            settings.put("rules", JSONArray(m.writeValueAsString(rules)))
            settings.put("colours", JSONArray(m.writeValueAsString(colours)))

            // Writing to file
            val writer = FileWriter(SETTINGS_FILE)
            writer.write(settings.toString(4))
            writer.close()
        } catch (exception: IOException) {
            logger.log(Level.WARNING, exception.message)
        }
    }

    // Loads the pattern based on an RLE
    fun loadPattern(RLE: String) {
        loadPattern(RLE.split("\n").toTypedArray()) // Split by new line
    }

    fun loadPattern(tokens: Array<String>) {
        var rulestring = ""
        val rulestringRegex = Regex("rule = ([ \\S]+)")
        val comments = ArrayList<String>() // Comments to feed into RuleFamily.loadComments()

        // Parsing code - Removes headers, comments
        val rleFinal = StringBuilder()
        tokens.forEach { token ->
            when {
                // Check for comment
                token.startsWith("#R") -> comments.add(token)

                // Check for header
                token[0] == 'x' -> rulestring = rulestringRegex.find(token)!!.groupValues[0].substring(7)

                // Not a comment
                token[0] != '#' -> rleFinal.append(token)
            }
        }

        // Identify the rule family based on regex
        val rule = Utils.fromRulestring(rulestring)
        if (rule != null) {
            // Generate the additional information from comments
            rule.loadComments(comments.toTypedArray())

            // Set the rulestring
            simulator.rule = rule
        }

        newPattern() // Clear all cells
        simulator.fromRLE(
            rleFinal.toString(),  // Insert the new cells
            Coordinate(1800 / CELL_SIZE, 1800 / CELL_SIZE)
        )

        // Re-render all the cells
        if (colours[simulator.rule] != null) {
            scrollPane.style = "-fx-background: rgb(${colours[simulator.rule]!![0].red * 255}," +
                    "${colours[simulator.rule]!![0].green * 255}," +
                    "${colours[simulator.rule]!![0].blue * 255})"
            drawingPane.style = "-fx-background: rgb(" + colours[simulator.rule]!![0].red * 255 + "," +
                    colours[simulator.rule]!![0].green * 255 + "," +
                    colours[simulator.rule]!![0].blue * 255 + ")"
        } else {
            scrollPane.style = "-fx-background: rgb(" + simulator.rule.getColour(0).red * 255 + "," +
                    simulator.rule.getColour(0).green * 255 + "," +
                    simulator.rule.getColour(0).blue * 255 + ")"
            drawingPane.style = "-fx-background: rgb(" + simulator.rule.getColour(0).red * 255 + "," +
                    simulator.rule.getColour(0).green * 255 + "," +
                    simulator.rule.getColour(0).blue * 255 + ")"
        }
        renderCells()

        // Centering the viewport
        // scrollPane.setHvalue(0.2);
        // scrollPane.setVvalue(0.2);
        // drawingPane.setTranslateX(0);
        // drawingPane.setTranslateY(0);

        // Setting the rule of the rule dialog
        dialog.rule = simulator.rule as RuleFamily

        // Reloading the state buttons to state the number of states
        reloadStateButtons()

        // Drawing the bounded grid
        drawBoundedGrid()

        // Setting the generation count back to 0
        simulator.generation = 0

        // Clear the population list
        populationList.clear()

        // Update the status text
        updateStatusText()
    }

    fun loadPattern(tokens: ArrayList<String>) {
        loadPattern(tokens.toTypedArray())
    }

    // Returns the RLE of the pattern
    fun saveRLE(): String {
        // Add header & comments
        val rle = simulator.toRLE(selectionRectangle!!.start, selectionRectangle!!.end)
        val rleFinal = StringBuilder()

        // Adding comments
        (simulator.rule as RuleFamily).generateComments()?.forEach { comment ->
            rleFinal.append(comment).append("\n")
        }

        // Adding header
        rleFinal.append("x = ").append(selectionRectangle!!.end.x - selectionRectangle!!.start.x + 1).append(", y = ")
            .append(
                selectionRectangle!!.end.y - selectionRectangle!!.start.y + 1
            ).append(", rule = ").append((simulator.rule as RuleFamily).rulestring).append("\n")
        rleFinal.append(rle)
        return rleFinal.toString()
    }

    @FXML // Saves the pattern
    fun savePattern() {
        // Get the file to save the pattern in
        val fileChooser = FileChooser()
        fileChooser.title = "Save *.rle file"
        fileChooser.extensionFilters.add(
            FileChooser.ExtensionFilter(
                "RLE Files (*.rle)", "*.rle"
            )
        )

        val file = fileChooser.showSaveDialog(null)
        try {
            // Writing to the file
            val writer = FileWriter(file)
            writer.write(saveRLE())
            writer.close()

            // Tell the user the operation was successful
            Alert(Alert.AlertType.INFORMATION).apply {
                title = "Operation successful!"
                headerText = "The operation was successful!"
                contentText = "The operation was successful. " +
                        "The pattern has been saved to " + file.absolutePath + "."
                dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
            }.showAndWait()
        } catch (exception: IOException) {
            logger.log(Level.WARNING, exception.message)

            Alert(Alert.AlertType.ERROR).apply {
                title = "Writing to pattern file"
                headerText = "An error occuring when writing to the pattern file!"
                contentText = exception.message
            }.showAndWait()
        }
    }

    @FXML // Creates a new pattern
    fun newPattern() {
        // Add all the cells to a list to avoid ConcurrentModificationException
        val coordinates = ArrayList<Coordinate>()
        simulator.iterateCells { e: Coordinate -> coordinates.add(e) }

        // Avoiding ConcurrentModificationException
        for (coordinate in coordinates) {
            setCell(convertToScreen(coordinate.x), convertToScreen(coordinate.y), 0)
        }

        // Setting the generation count back to 0
        simulator.generation = 0

        // Clear the population list
        populationList.clear()
    }

    @FXML // Loads the pattern from the RLE file
    fun openPattern() {
        try {
            val fileChooser = FileChooser()
            fileChooser.title = "Open pattern file"
            fileChooser.extensionFilters.add(
                FileChooser.ExtensionFilter(
                    "RLE Files (*.rle)", "*.rle"
                )
            )

            val file = fileChooser.showOpenDialog(null)

            val scanner = Scanner(file)
            val tokens = ArrayList<String>()
            while (scanner.hasNextLine()) {  // Getting all text from scanner
                tokens.add(scanner.nextLine())
            }
            scanner.close() // Close the scanner object

            loadPattern(tokens) // Loads the pattern
        } catch (exception: IOException) {
            logger.log(Level.WARNING, exception.message)
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error reading pattern file"
            alert.headerText = "There was an error reading the pattern file!"
            alert.contentText = exception.message
            alert.showAndWait()
        }
    }

    @FXML // Pastes the RLE from the clipboard
    fun pasteRLE() {
        // Get text from clipboard
        val clipboard = Clipboard.getSystemClipboard()
        val RLE = clipboard.string

        // Parsing code - Removes headers, comments
        val rleFinal = StringBuilder()
        val tokens = RLE.split("\n").toTypedArray()
        for (token in tokens) {
            if (token[0] != '#' && token[0] != 'x') {  // Check for comment & header
                rleFinal.append(token)
            }
        }

        mode = Mode.PASTING
        pasteStuff = Grid()
        pasteStuff!!.fromRLE(rleFinal.toString(), Coordinate(0, 0))
        pasteStuff!!.updateBounds()
        pasteSelection.children.clear()
        pasteStuff!!.iterateCells { coordinate: Coordinate ->
            val cell = Rectangle()
            cell.x = convertToScreen(coordinate.x).toDouble()
            cell.y = convertToScreen(coordinate.y).toDouble()
            cell.width = CELL_SIZE.toDouble()
            cell.height = CELL_SIZE.toDouble()
            cell.fill = simulator.rule.getColour(pasteStuff!!.getCell(coordinate))

            // Add cell to pane and cell list
            pasteSelection.children.add(cell)
        }

        val rectangle = SelectionRectangle(CELL_SIZE)
        rectangle.fill = Color.rgb(255, 0, 0)
        rectangle.select(convertToScreen(pasteStuff!!.bounds.value0), convertToScreen(pasteStuff!!.bounds.value1))
        pasteSelection.children.add(rectangle)
        rectangle.toFront()
        pasteSelection.isVisible = true
        pasteSelection.toFront()
        gridLines.toFront()

        //simulator.fromRLE(rleFinal.toString(), startSelection);  // Insert the cells
        //renderCells(startSelection, endSelection);  // Render the cells
    }

    @FXML // Copies the currently selected cells to the clipboard
    fun copyCells() {
        if (selectionRectangle!!.isSelecting) {
            val clipboard = Clipboard.getSystemClipboard()
            val content = ClipboardContent()
            content.putString(saveRLE())
            clipboard.setContent(content)
        } else {
            Alert(Alert.AlertType.ERROR).apply {
                title = "No area selected!"
                contentText = "No area has been selected!"
            }.showAndWait()
        }
    }

    @FXML // Deletes the cells in the selection
    fun deleteCells() {
        if (selectionRectangle!!.isSelecting) {
            Action.addAction()
            simulator.clearCells(selectionRectangle!!.start, selectionRectangle!!.end)
            renderCells(selectionRectangle!!.start, selectionRectangle!!.end)

            // Move the grid lines and selection to the front
            selectionRectangle!!.toFront()
            gridLines.toFront()
        } else {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "No area selected!"
            alert.contentText = "No area has been selected!"
            alert.showAndWait()
        }
    }

    // Handles the keyboard shortcuts
    fun keyPressedHandler(event: KeyEvent) {
        event.consume() // No one touches this but me

        // Shift + Enter to simulate in selection
        when {
            event.code == KeyCode.ENTER && event.isShiftDown -> simInsideSelectionButton.fire()
            event.code == KeyCode.ENTER && event.isControlDown -> simOutsideSelectionButton.fire()
            event.code == KeyCode.ENTER -> startSimulationButton.fire()
            event.code == KeyCode.SPACE -> {
                if (visualisationDone) {
                    Action.addAction()
                    updateCells()
                }
            }
            event.code == KeyCode.DELETE -> deleteCells()
            event.code == KeyCode.C && event.isControlDown -> copyCells()
            event.code == KeyCode.V && event.isControlDown -> pasteRLE()
            event.code == KeyCode.X && event.isControlDown -> {
                copyCells()
                deleteCells()
            }
            event.code == KeyCode.A && event.isControlDown -> {
                simulator.updateBounds()
                val start = convertToScreen(simulator.bounds.value0)
                val end = convertToScreen(simulator.bounds.value1)
                selectionRectangle!!.select(start, end)
                selectionRectangle!!.isVisible = true
                mode = Mode.SELECTING

                // Move the grid lines and selection to the front
                selectionRectangle!!.toFront()
                gridLines.toFront()
            }
            event.code == KeyCode.Z && event.isControlDown -> Action.undo()
            event.code == KeyCode.Y && event.isControlDown -> Action.redo()
            event.code == KeyCode.O && event.isShiftDown && event.isControlDown -> {
                val clipboard = Clipboard.getSystemClipboard()
                loadPattern(clipboard.string)
            }
            event.code == KeyCode.O && event.isControlDown -> openPattern()
            event.code == KeyCode.S && event.isControlDown -> savePattern()
            event.code == KeyCode.N && event.isControlDown -> newPattern()
            event.code == KeyCode.R && event.isControlDown -> startRuleDialog()
            event.code == KeyCode.DIGIT5 && event.isControlDown -> generateRandomSoup()
            event.code == KeyCode.X -> flipHorizontalHandler()
            event.code == KeyCode.Y -> flipVerticalHandler()
            event.code == KeyCode.PERIOD && event.isShiftDown -> rotateCWHandler()
            event.code == KeyCode.COMMA && event.isShiftDown -> rotateCCWHandler()
        }
    }

    // Wait for the specified number of milliseconds
    fun wait(milliseconds: Int) {
        try {
            Thread.sleep(milliseconds.toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    // Converts grid coordinates to screen coordinate
    fun convertToScreen(x: Int): Int {
        return x * CELL_SIZE
    }

    fun convertToScreen(coordinate: Coordinate): Coordinate {
        return Coordinate(coordinate.x * CELL_SIZE, coordinate.y * CELL_SIZE)
    }

    // Converts screen coordinates to grid coordinates
    fun convertToGrid(x: Int): Int {
        return x / CELL_SIZE
    }

    fun convertToGrid(coordinate: Coordinate): Coordinate {
        return Coordinate(coordinate.x / CELL_SIZE, coordinate.y / CELL_SIZE)
    }

    // Snaps the screen X or Y coordinates to the grid
    fun snapToGrid(x: Int): Int {
        return x / CELL_SIZE * CELL_SIZE
    }

    fun snapToGrid(coordinate: Coordinate): Coordinate {
        return convertToGrid(convertToScreen(coordinate))
    }

    companion object {
        const val WIDTH = 4096
        const val HEIGHT = 4096
        const val CELL_SIZE = 1 // Cell size
        const val SETTINGS_FILE = "settings.json"
    }
}