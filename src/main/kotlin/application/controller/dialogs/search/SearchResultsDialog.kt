package application.controller.dialogs.search

import application.controller.MainController
import application.model.patterns.Pattern
import application.model.search.SearchProgram
import application.model.simulation.Grid
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.WindowEvent
import java.util.*

abstract class SearchResultsDialog(mainController: MainController, searchProgram: SearchProgram) : Dialog<Any?>() {
    protected var grid: GridPane // Store controls here
    protected var menu: ContextMenu // Display on right-click
    protected var searchProgram: SearchProgram // The search program that is running
    protected var mainController: MainController
    protected var index = 0 // The index of the search results that has been loaded
    protected var patterns: MutableMap<String, MutableMap<String, MutableList<Pattern>>> // The organised search results
    protected var patternLists: HBox // The HBox to store the list of patterns

    // Create initial data
    protected val data = FXCollections.observableArrayList<Grid>()

    // Saves the search results to a file
    fun saveToFile() {
        val fileChooser = FileChooser()
        fileChooser.title = "Save search results"
        fileChooser.extensionFilters.add(
            FileChooser.ExtensionFilter(
                "Comma Separated Values Files (*.csv)", "*.csv"
            )
        )

        val file = fileChooser.showSaveDialog(null)

        // Writing to file
        if (!searchProgram.writeToFile(file)) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error in saving the file"
            alert.headerText = "The operation was unsuccessful."
            alert.contentText = "The operation was unsuccessful. " +
                    "If you suspect a bug, please report it."
            alert.dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
            alert.showAndWait()
        } else {
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.title = "Operation successful!"
            alert.headerText = "The operation was successful."
            alert.contentText = "The operation was successful. " +
                    "The search results have been saved to " + file.absolutePath + "."
            alert.dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
            alert.showAndWait()
        }
    }

    // Loads the search result into the main simulator
    fun loadPattern() {
        mainController.loadPattern(selectedRLE!!)
    }

    // Copies the RLE onto the clipboard
    fun copyToClipboard() {
        val clipboard = Clipboard.getSystemClipboard()
        val content = ClipboardContent()
        content.putString(selectedRLE)
        clipboard.setContent(content)
    }

    // Organises the search results
    fun organiseSearchResults() {
        var pattern: Pattern
        if (searchProgram.searchResults != null) {
            for (i in index until searchProgram.searchResults.size) {
                pattern = searchProgram.searchResults[i]
                if (patterns.containsKey(pattern.name)) {
                    if (patterns[pattern.name]!!.containsKey(pattern.toString())) {
                        patterns[pattern.name]!![pattern.toString()]!!.add(pattern)
                    } else {
                        patterns[pattern.name]!![pattern.toString()] = arrayListOf(pattern)
                    }
                } else {
                    patterns[pattern.name] = TreeMap()
                }
            }

            index = searchProgram.searchResults.size
        }
    }

    // Generates the nodes using the organised search results
    fun generateNodes() {
        patternLists.children.clear()
        val headerFont = Font.font("System", FontWeight.BOLD, 24.0)
        for (patternType in patterns.keys) {
            val vbox = VBox()
            vbox.spacing = 5.0

            // Header Text
            val header = Label(patternType)
            header.font = headerFont
            vbox.children.add(header)

            // Adding the hyperlinks
            for (patternName in patterns[patternType]!!.keys) {
                val hyperlink = Hyperlink(patternName)
                hyperlink.onAction = EventHandler {
                    val patList: List<Pattern> = patterns[patternType]!![patternName]!!
                    val n = patList.size

                    val additionalInfo: MutableList<Map<String, String>> = ArrayList(n)
                    for (blocks in patList) additionalInfo.add(getAdditionalInfo(blocks))
                    val dialog = PatternsDialog(
                        patterns[patternType]!![patternName]!!,
                        additionalInfo, menu
                    )
                    dialog.showAndWait()
                }
                vbox.children.add(hyperlink)
            }

            // Adding to HBox
            patternLists.children.add(vbox)
        }
    }

    // Gets the RLE of the selected pattern
    abstract val selectedRLE: String

    // Gets additional information about the provided pattern
    abstract fun getAdditionalInfo(pattern: Pattern): Map<String, String>

    init {
        super.setResizable(true)
        super.initModality(Modality.NONE)
        this.searchProgram = searchProgram
        menu = ContextMenu() // The menu that shows on right click

        // Displays the pattern in the application
        val showPattern = MenuItem("Show in Application")
        showPattern.onAction = EventHandler { loadPattern() }
        menu.items.add(showPattern)

        // Copies the RLE to the clipboard
        val copyRLE = MenuItem("Copy RLE to Clipboard")
        copyRLE.onAction = EventHandler { copyToClipboard() }
        menu.items.add(copyRLE)

        // Grid to arrange controls
        grid = GridPane()
        grid.hgap = 5.0
        grid.vgap = 5.0

        // HBox to contain pattern lists
        patternLists = HBox()
        patternLists.spacing = 20.0
        val scrollPane = ScrollPane(patternLists)
        scrollPane.style = "-fx-background-color: transparent;" // No border
        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        grid.add(scrollPane, 0, 0)

        // Building the list of patterns
        patterns = TreeMap()
        organiseSearchResults()

        // Converting into nodes
        generateNodes()

        // HBox to contain the buttons
        val buttonsBox = HBox()
        buttonsBox.spacing = 10.0
        grid.add(buttonsBox, 0, 1)

        // Button to reload data
        val reloadButton = Button("Reload Data")
        reloadButton.onAction = EventHandler {
            organiseSearchResults()
            generateNodes()
        }
        buttonsBox.children.add(reloadButton)

        // Button to save data to file
        val saveToFileButton = Button("Save to File")
        saveToFileButton.onAction = EventHandler { saveToFile() }
        buttonsBox.children.add(saveToFileButton)

        // Button to terminate search process
        val terminateSearchButton = Button("Terminate Search")
        terminateSearchButton.onAction = EventHandler {
            try {
                searchProgram.terminateSearch()
                val alert = Alert(Alert.AlertType.INFORMATION)
                alert.title = "Success!"
                alert.headerText = "The search program has been terminated!"
                alert.showAndWait()
            } catch (exception: Exception) {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Error!"
                alert.headerText = exception.message
                alert.contentText = Arrays.toString(exception.stackTrace)
                alert.showAndWait()
            }
        }

        buttonsBox.children.add(terminateSearchButton)

        // Allows closing with close button
        val window = super.getDialogPane().scene.window
        window.onCloseRequest = EventHandler { event: WindowEvent? -> window.hide() }
        super.getDialogPane().content = grid

        this.searchProgram = searchProgram // Setting the search program
        this.mainController = mainController // Setting the main controller
    }
}