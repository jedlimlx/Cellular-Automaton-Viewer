package sample.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import org.json.JSONObject;
import org.json.JSONTokener;
import sample.controller.dialogs.About;
import sample.controller.dialogs.GifferDialog;
import sample.controller.dialogs.rule.RuleDialog;
import sample.controller.dialogs.rule.RuleWidget;
import sample.controller.dialogs.search.RuleSearchParametersDialog;
import sample.controller.dialogs.search.RuleSearchResultsDialog;
import sample.model.Cell;
import sample.model.*;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.Rule;
import sample.model.rules.RuleFamily;
import sample.model.rules.hrot.HROT;
import sample.model.search.RuleSearch;
import sample.model.simulation.Grid;
import sample.model.simulation.Simulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {
    @FXML
    private Pane drawingPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Label statusLabel;

    @FXML
    private Button startSimulationButton;

    @FXML
    private ToolBar secondaryToolbar;

    @FXML
    private ImageView recordingImage, playButtonImage;

    @FXML
    private CheckMenuItem gridLinesMenuItem;

    private final int WIDTH = 4096;
    private final int HEIGHT = 4096;
    private final int CELL_SIZE = 1;  // Cell size
    private final String SETTINGS_FILE = "settings.json";

    private boolean currentlySelecting = false;
    private Coordinate startSelection;  // The start of the selection
    private Coordinate endSelection;  // The end of the selection
    private Rectangle selectionRectangle;  // Rectangle that represents selection box

    private final RuleDialog dialog = new RuleDialog();  // Dialog to set rule
    private final GifferDialog gifferDialog = new GifferDialog();

    private Mode mode;  // Mode (Drawing, Selecting, Panning)
    private Simulator simulator;  // Simulator to simulate rule
    private ArrayList<Button> stateButtons;  // Buttons to switch between states
    private HashMap<Coordinate, Cell> cellList;  // List of cell objects
    private Group gridLines;  // The grid lines of the pattern editor

    private Group pasteSelection;  // The group that renders the stuff to be pasted
    private Grid pasteStuff;  // The stuff to paste

    private ArrayList<Grid> history;  // The history for undo, redo

    private int simulationTime, visualisationTime;  // Time taken for visualisation and simulation

    private int minSimTime = 0;  // Minimum time for one step
    private int stepSize = 1;  // Step size of the simulation
    private int currentState = 1;  // State to draw with

    private boolean recording = false;  // Is the recording on?
    private boolean simulationRunning = false;  // Is the simulation running?
    private boolean visualisationDone = true;  // Is the visualisation done?
    private boolean showGridLines = false;  // Are the grid lines being shown?

    private JSONObject settings;  // Store the settings

    private Giffer giffer;  // For writing to a *.gif

    @FXML
    public void initialize() {
        // Initialise variables
        cellList = new HashMap<>();
        stateButtons = new ArrayList<>();
        history = new ArrayList<>();
        mode = Mode.DRAWING;

        // Create simulator object
        simulator = new Simulator(new HROT("R2,C2,S6-9,B7-8,NM"));

        // Create selection rectangle and set properties
        selectionRectangle = new Rectangle();
        selectionRectangle.setOpacity(0.5);
        selectionRectangle.setFill(Color.rgb(150, 230, 255));
        selectionRectangle.toFront();
        selectionRectangle.setVisible(false);
        drawingPane.getChildren().add(selectionRectangle);

        // Setting zoom
        drawingPane.setScaleX(5);
        drawingPane.setScaleY(5);

        // Disable scrollbars and scrolling
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Bind methods
        scrollPane.setOnScroll(this::changeZoomHandler);
        scrollPane.addEventFilter(ScrollEvent.ANY, this::changeZoomHandler);

        scrollPane.setOnKeyPressed(this::keyPressedHandler);
        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressedHandler);

        // Move the center of the ScrollPane
        scrollPane.setHvalue(0.2);
        scrollPane.setVvalue(0.2);

        // Setting the scroll pane in focus
        drawingPane.requestFocus();

        // Load the correct number of state buttons
        reloadStateButtons();

        // Start Simulation Thread
        Thread simulationThread = new Thread(this::runSimulation);
        simulationThread.setName("Simulation Thread");
        simulationThread.setDaemon(true);
        simulationThread.start();

        // Setting the rule of the rule dialog
        dialog.setRule((RuleFamily) simulator.getRule());

        // Creating grid lines
        gridLines = new Group();
        for (int i = 0; i < WIDTH; i += CELL_SIZE) {
            Line lineX = new Line();
            lineX.setStroke(Color.GREY);
            lineX.setStrokeWidth(0.2 * CELL_SIZE);
            lineX.setStartX(i);
            lineX.setEndX(i);
            lineX.setStartY(0);
            lineX.setEndY(HEIGHT);
            lineX.toFront();
            gridLines.getChildren().add(lineX);

            Line lineY = new Line();
            lineY.setStroke(Color.GREY);
            lineY.setStrokeWidth(0.2 * CELL_SIZE);
            lineY.setStartX(0);
            lineY.setEndX(HEIGHT);
            lineY.setStartY(i);
            lineY.setEndY(i);
            lineY.toFront();
            gridLines.getChildren().add(lineY);
        }

        gridLines.setVisible(false);
        drawingPane.getChildren().add(gridLines);

        // Pane for pasting
        pasteSelection = new Group();
        drawingPane.getChildren().add(pasteSelection);
        pasteSelection.setVisible(true);

        // Loading settings
        try {  // Reading settings from settings file
            JSONTokener tokener = new JSONTokener(new FileInputStream(SETTINGS_FILE));
            settings = new JSONObject(tokener);

            // Grid lines
            if (settings.getBoolean("grid_lines")) {
                showGridLines = true;
                gridLines.setVisible(true);
                gridLinesMenuItem.setSelected(true);
            }

            // Setting the rule
            ObjectMapper m = new ObjectMapper();
            m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            simulator.setRule(m.readValue(settings.get("rule").toString(), Rule.class));

            // Setting the rule of the rule dialog
            dialog.setRule((RuleFamily) simulator.getRule());

            // Reloads the state buttons
            reloadStateButtons();
        }
        catch (IOException exception) {
            settings = new JSONObject();
            exception.printStackTrace();
        }
    }

    @FXML
    public void reloadStateButtons() {
        for (Button button: stateButtons) {
            secondaryToolbar.getItems().remove(button);
        }

        // Clearing stateButtons
        stateButtons.clear();

        // Add buttons to the secondary toolbar
        for (int i = 0; i < simulator.getRule().getNumStates(); i++) {
            int index = i;

            Button stateButton = new Button("" + index);
            stateButton.setOnAction((event -> {currentState = index; mode = Mode.DRAWING;}));
            stateButtons.add(stateButton);
            secondaryToolbar.getItems().add(stateButton);
        }
    }

    @FXML // Handle the mouse events
    public void mouseDraggedHandler(MouseEvent event) {
        if (mode == Mode.DRAWING) {
            setCell((int)event.getX() / CELL_SIZE * CELL_SIZE,
                    (int)event.getY() / CELL_SIZE * CELL_SIZE,
                    currentState);
        }
        else if (mode == Mode.SELECTING) {
            selectionRectangle.setWidth((int)event.getX() / CELL_SIZE * CELL_SIZE -
                    startSelection.getX() * CELL_SIZE);
            selectionRectangle.setHeight((int)event.getY() / CELL_SIZE * CELL_SIZE -
                    startSelection.getY() * CELL_SIZE);
        }
    }

    @FXML
    public void mouseDragStartHandler(MouseEvent event) {
        if (mode == Mode.SELECTING && !currentlySelecting) {
            currentlySelecting = true;

            selectionRectangle.setVisible(false);
            selectionRectangle.toFront();

            selectionRectangle.setX((int)event.getX() / CELL_SIZE * CELL_SIZE);
            selectionRectangle.setY((int)event.getY() / CELL_SIZE * CELL_SIZE);
            startSelection = new Coordinate((int)event.getX() / CELL_SIZE,
                    (int)event.getY() / CELL_SIZE);

            selectionRectangle.setVisible(true);
        }
        else if (mode == Mode.PASTING) {
            insertCells(pasteStuff, (int)(event.getX() / CELL_SIZE), (int)event.getY() / CELL_SIZE);
            pasteSelection.setVisible(false);

            mode = Mode.SELECTING;
        }
    }

    @FXML
    public void mouseDragDoneHandler(MouseEvent event) {
        if (mode == Mode.SELECTING && currentlySelecting) {
            currentlySelecting = false;
            endSelection = new Coordinate((int)event.getX() / CELL_SIZE,
                    (int)event.getY() / CELL_SIZE);
        }

        if (startSelection != null && endSelection != null &&
                (endSelection.subtract(startSelection).getX() < 1 ||
                endSelection.subtract(startSelection).getY() < 1)) {
            selectionRectangle.setVisible(false);
        }
    }

    @FXML
    public void mouseMovedHandler(MouseEvent event) {
        if (mode == Mode.PASTING) {
            pasteSelection.setTranslateX((int)(event.getX() / CELL_SIZE) * CELL_SIZE);
            pasteSelection.setTranslateY((int)(event.getY() / CELL_SIZE) * CELL_SIZE);
        }
    }

    @FXML // Generates random soup in the selection box
    public void generateRandomSoup() {
        // Generate the soup
        Grid soup = SymmetryGenerator.generateSymmetry("C1", 50, new int[]{1},
                endSelection.getX() - startSelection.getX(),
                endSelection.getY() - startSelection.getY());
        
        // Insert the cells in the pane (automatically inserted in the simulator)
        insertCells(soup, startSelection.getX(), startSelection.getY());

        // Move the grid lines and selection to the front
        selectionRectangle.toFront();
        gridLines.toFront();
    }

    @FXML // Flips selected cells horizontally
    public void flipHorizontalHandler() {
        simulator.reflectCellsX(startSelection, endSelection);  // Reflect cells in the grid
        renderCells(startSelection, endSelection);

        // Move the grid lines and selection to the front
        selectionRectangle.toFront();
        gridLines.toFront();
    }

    @FXML // Flips selected cells vertically
    public void flipVerticalHandler() {
        simulator.reflectCellsY(startSelection, endSelection);  // Reflect cells in the grid
        renderCells(startSelection, endSelection);

        // Move the grid lines and selection to the front
        selectionRectangle.toFront();
        gridLines.toFront();
    }

    @FXML // Rotates the selected cells clockwise
    public void rotateCWHandler() {
        simulator.rotateCW(startSelection, endSelection);  // Rotate the cells in the grid
        renderCells(startSelection, endSelection);

        // Move the grid lines and selection to the front
        selectionRectangle.toFront();
        gridLines.toFront();
    }

    @FXML // Rotates the selected cells counter-clockwise
    public void rotateCCWHandler() {
        simulator.rotateCCW(startSelection, endSelection);  // Rotate the cells in the grid
        renderCells(startSelection, endSelection);

        // Move the grid lines and selection to the front
        selectionRectangle.toFront();
        gridLines.toFront();
    }

    @FXML // Sets the generation
    public void setGeneration() {
        // Getting the generation number from the user
        TextInputDialog inputDialog = new TextInputDialog(simulator.getGeneration() + "");
        inputDialog.setTitle("Set Generation");
        inputDialog.setHeaderText("Enter the generation number:");
        inputDialog.showAndWait();

        try {
            simulator.setGeneration(Integer.parseInt(inputDialog.getResult()));
            updateStatusText();
        }
        catch (NumberFormatException exception) {
            // Ensure it's an integer
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText(inputDialog.getResult() + " is not a valid integer!");
            alert.showAndWait();
        }
    }

    @FXML // Sets the step size
    public void setStepSize() {
        // Getting the step size from the user
        TextInputDialog inputDialog = new TextInputDialog(stepSize + "");
        inputDialog.setTitle("Set Step Size:");
        inputDialog.setHeaderText("Enter the step size:");
        inputDialog.showAndWait();

        try {
            stepSize = Integer.parseInt(inputDialog.getResult());
            updateStatusText();
        }
        catch (NumberFormatException exception) {
            // Ensure it's an integer
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText(inputDialog.getResult() + " is not a valid integer!");
            alert.showAndWait();
        }
    }

    @FXML // Sets the maximum simulation speed (gen/s)
    public void setSimSpeed() {
        // Getting the generation number from the user
        TextInputDialog inputDialog = new TextInputDialog(1000 / (minSimTime + 1) + "");
        inputDialog.setTitle("Set Maximum Simulation Speed (gen/s)");
        inputDialog.setHeaderText("Enter the simulation speed (gen/s):");
        inputDialog.showAndWait();

        try {
            minSimTime = 1000 / Integer.parseInt(inputDialog.getResult());
        }
        catch (NumberFormatException exception) {
            // Ensure it's an integer
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText(inputDialog.getResult() + " is not a valid integer!");
            alert.showAndWait();
        }
    }

    @FXML // Clears the cell cache
    public void clearCellsCache() {
        ArrayList<Cell> toBeRemoved = new ArrayList<>();
        for (Coordinate cell: cellList.keySet()) {
            if (cellList.get(cell).getState() == 0) {
                try {
                    toBeRemoved.add(cellList.get(cell));
                }
                catch (NullPointerException ignored) {}
            }
        }

        for (Cell cell: toBeRemoved) {
            // Destroy the cell object (remove all references to it so it is garbage collected)
            removeCellObject(cell.getCoordinate().getX(), cell.getCoordinate().getY());
            drawingPane.getChildren().remove(cell.getRectangle());
        }
    }

    // Function to set cell at (x, y) to a certain state
    public void setCell(int x, int y, int state) {
        setCell(x, y, state, true);
    }

    public void setCell(int x, int y, int state, boolean updateSimulator) {
        // Get the cell object at the specified coordinate
        Cell prevCell = getCellObject(x, y);
        if (prevCell == null) {  // Insert a new cell if one doesn't already exist
            // Create cell
            Rectangle cell = new Rectangle();
            cell.setX(x);
            cell.setY(y);
            cell.setWidth(CELL_SIZE);
            cell.setHeight(CELL_SIZE);
            cell.setFill(simulator.getRule().getColour(state));
            cell.toBack();

            // Add cell to pane and cell list
            drawingPane.getChildren().add(cell);
            addCellObject(x, y, new Cell(x, y, state, cell));
        }
        else if (prevCell.getState() != state) {  // Don't bother if the cell didn't change
            if (state == 0 && cellList.size() > 5000000) {
                // Destroy the cell object (remove all references to it so it is garbage collected)
                removeCellObject(x, y);
                drawingPane.getChildren().remove(prevCell.getRectangle());
            }
            else {
                prevCell.getRectangle().setFill(simulator.getRule().getColour(state));
                prevCell.setState(state);
            }
        }

        // Add cell to simulator
        if (updateSimulator)
            simulator.setCell(x / CELL_SIZE, y / CELL_SIZE, state);
    }

    public void insertCells(Grid cellsToInsert, int x, int y) {
        cellsToInsert.iterateCells(coord -> {
            Coordinate newCell = coord.add(new Coordinate(x, y));
            setCell(newCell.getX() * CELL_SIZE, newCell.getY() * CELL_SIZE,
                    cellsToInsert.getCell(coord));
        });
    }

    // Renders all cells
    public void renderCells() {
        simulator.iterateCells(coordinate -> setCell(coordinate.getX() * CELL_SIZE, coordinate.getY() * CELL_SIZE,
                simulator.getCell(coordinate),false));
    }

    // Renders cells between the start and end coordinate
    public void renderCells(Coordinate startSelection, Coordinate endSelection) {
        for (int i = startSelection.getX(); i < endSelection.getX() + 1; i++) {
            for (int j = startSelection.getY(); j < endSelection.getY() + 1; j++) {
                setCell(i * CELL_SIZE, j * CELL_SIZE, simulator.getCell(i, j));  // Render the new cells
            }
        }
    }

    public void addCellObject(int x, int y, Cell cell) {
        cellList.put(new Coordinate(x, y), cell);
    }

    public void removeCellObject(int x, int y) {
        cellList.remove(new Coordinate(x, y));
    }

    public Cell getCellObject(int x, int y) {
        return cellList.get(new Coordinate(x, y));
    }

    public void updateStatusText() {
        String simulationString = String.format("Simulation Speed: %.2f step/s",
                1000.0 / (visualisationTime + simulationTime));

        statusLabel.setText("Generation: " + simulator.getGeneration() + ", " +
                simulationString);
    }

    @FXML // Zooming in and out of the canvas
    public void changeZoomHandler(ScrollEvent event) {
        final double SCALE_DELTA = 1.2;
        event.consume();
        if (event.getDeltaY() == 0) {
            return;
        }

        double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;

        AnimatedZoomOperator zoomOperator = new AnimatedZoomOperator();
        zoomOperator.zoom(drawingPane, scaleFactor, event.getX(), event.getY());
    }

    @FXML // Updates cells
    public void updateCells() {
        visualisationDone = false;

        Set<Coordinate> cellsChanged = new HashSet<>();

        long startTime = System.currentTimeMillis();
        try {
            for (int i = 0; i < stepSize; i++) {
                simulator.step();
                if (stepSize > 1) cellsChanged.addAll(simulator.getCellsChanged());
                else cellsChanged = simulator.getCellsChanged();
            }
        }
        catch (ConcurrentModificationException ignored) {}

        simulationTime = (int) (System.currentTimeMillis() - startTime);

        Set<Coordinate> finalCellsChanged = cellsChanged;
        Platform.runLater(() -> {
            try {
                long startTime2 = System.currentTimeMillis();

                // Only update cells that changed (for speed)
                for (Coordinate cell: finalCellsChanged) {
                    setCell(cell.getX() * CELL_SIZE, cell.getY() * CELL_SIZE, simulator.getCell(cell));
                }

                // Move the grid lines and selection to the front
                if (selectionRectangle.isVisible()) selectionRectangle.toFront();
                if (gridLines.isVisible()) gridLines.toFront();

                visualisationTime = (int) (System.currentTimeMillis() - startTime2);
            }
            catch (ConcurrentModificationException exception) { // Catch an exception that will hopefully not happen
                simulationRunning = false;  // Pause the simulation

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("ConcurrentModificationException cause in updateCells.");
                alert.setContentText("Please report this as a bug.");
                alert.showAndWait();
            }

            // Ensure that the minimum simulation time is not "surpassed"
            int waitTime = minSimTime - (simulationTime + visualisationTime);
            if (waitTime > 0) {
                wait(waitTime);
                visualisationTime += waitTime;
            }

            // Update the variable to say that the visualisation is done
            visualisationDone = true;

            // Update the status label
            updateStatusText();
        });

        // Add to the gif if the recording is on
        if (recording) {
            giffer.addGrid(startSelection, endSelection,
                    simulator.getCells(startSelection, endSelection), simulator.getRule());
        }
    }

    // Runs simulation
    public void runSimulation() {
        int num = 1;

        while (true) {
            if (simulationRunning) {
                // Wait for the visualisation to be done
                // To avoid ConcurrentModificationException
                // TODO (Using locks would be more elegant)
                while (!visualisationDone) {
                    wait(1);
                }

                updateCells();
            }
            else {
                int finalNum = num;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        playButtonImage.setImage(new Image(getClass().getResourceAsStream(
                                "/icon/GliderPlayBtn" + (finalNum + 1) + ".png")));
                    }
                });

                num++;
                num %= 4;  // Cycle from 1 - 4
                wait(75);
            }

            wait(1);
        }
    }

    @FXML  // Toggles simulation on and off
    public void toggleSimulation() {
        simulationRunning = !simulationRunning;  // Toggle the simulation
        if (!simulationRunning) {  // Changing the icon
            playButtonImage.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/GliderPlayBtn1.png")));
        }
        else {
            playButtonImage.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/StopButtonEater.png")));
        }

        while (true) {
            try {
                history.add(simulator.deepCopy());
                break;
            }
            catch (ConcurrentModificationException ignored) {}
        }
    }

    @FXML  // Change mode to drawing
    public void drawingMode() {
        scrollPane.setPannable(false);
        mode = Mode.DRAWING;
    }

    @FXML  // Change mode to panning
    public void panMode() {
        scrollPane.setPannable(true);
        mode = Mode.PANNING;
    }

    @FXML  // Change mode to selecting
    public void selectionMode() {
        scrollPane.setPannable(false);
        mode = Mode.SELECTING;
    }

    @FXML // Toggle grid lines
    public void toggleGridLines() {
        showGridLines = !showGridLines;
        gridLines.setVisible(showGridLines);
    }

    @FXML // Starts the rule dialog
    public void startRuleDialog() {
        dialog.showAndWait();

        // Check if the user actually hit `Confirm Rule`
        if (dialog.getRule() != null) {
            simulator.setRule(dialog.getRule());

            // Reload the number of state buttons
            reloadStateButtons();
        }
    }

    @FXML // Starts the rule search dialog
    public void startRuleSearchDialog() {
        // Dialog to get the search parameters
        RuleSearchParametersDialog parametersDialog =
                new RuleSearchParametersDialog(simulator.getCells(startSelection, endSelection));
        parametersDialog.showAndWait();

        if (parametersDialog.getResult() == Boolean.TRUE) {  // If the operation wasn;t cancelled
            RuleSearch ruleSearch = new RuleSearch(parametersDialog.getSearchParameters());
            ruleSearch.searchThreaded(Integer.MAX_VALUE, parametersDialog.getNumThreads());

            RuleSearchResultsDialog resultsDialog = new RuleSearchResultsDialog(this, ruleSearch);
            resultsDialog.show();
        }
    }

    @FXML // Provides information about CAViewer
    public void startAboutDialog() {
        About about = new About();
        about.showAndWait();
    }

    @FXML // Generates an APGTable
    public void generateAPGTable() {
        if (simulator.getRule() instanceof RuleFamily) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save APGTable");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                        "Ruletable Files (*.rule)", "*.rule"));
                File file = fileChooser.showSaveDialog(null);

                // If operation is cancelled
                if (file != null) {
                    if (!(simulator.getRule() instanceof ApgtableGeneratable)) {
                        throw new UnsupportedOperationException("This rulespace does not support apgtable generation!");
                    }

                    APGTable apgTable = ((ApgtableGeneratable) simulator.getRule()).generateApgtable(file);

                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write("@RULE " + file.getName().replace(".rule", "") + "\n\n");
                    fileWriter.write("@TABLE\n");
                    fileWriter.write(apgTable.compileAPGTable());
                    fileWriter.close();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Operation successful!");
                    alert.setHeaderText("The operation was successful.");
                    alert.setContentText("The operation was successful. " +
                            "The apgtable has been saved to " + file.getAbsolutePath() + ".");
                    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
                    alert.showAndWait();
                }
            }
            catch (UnsupportedOperationException exception) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error in generating APGTable");
                alert.setHeaderText("APGTable generation is not supported by this rule / rulespace!");
                alert.setContentText(exception.getMessage());
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
                alert.showAndWait();
            }
            catch (IOException exception) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error in generating APGTable");
                alert.setHeaderText("The operation was unsuccessful.");
                alert.setContentText(exception.getMessage());
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
                alert.showAndWait();
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error in generating APGTable");
            alert.setHeaderText("APGTable generation is not supported by this rule / rulespace!");
            alert.setContentText("The rule / rulespace selected does not support APGTable generation." +
                    "If you require this feature, please request it or write it yourself");
            alert.showAndWait();
        }
    }

    @FXML // Identifies selected object
    public void identifySelection() {
        Simulator simulator = new Simulator(this.simulator.getRule());
        simulator.insertCells(this.simulator.getCells(startSelection, endSelection), startSelection);
        simulator.setGeneration(this.simulator.getGeneration());  // Ensure the generation is the same

        // Results of the identification
        sample.model.patterns.Pattern results = simulator.identify();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Identification Results");

        if (results != null) {
            alert.setHeaderText(results.toString());

            // Additional Information
            StringBuilder contentString = new StringBuilder();
            Map<String, String> additionalInfo = results.additionalInfo();
            for (String string: additionalInfo.keySet()) {
                contentString.append(string).append(": ").append(additionalInfo.get(string)).append("\n");
            }

            alert.setContentText(contentString.toString());
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
        }
        else {
            alert.setHeaderText("Identification Failed :(");
        }

        alert.showAndWait();
    }

    @FXML // Toggles the recording
    public void toggleRecording() {
        if (recording) {
            // Change icon for recording
            recordingImage.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/RecordLogo.png")));

            FileChooser fileChooser = new FileChooser();  // Find out where to save the *.gif
            fileChooser.setTitle("Save *.gif");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    "GIF Files (*.gif)", "*.gif"));

            File file = fileChooser.showSaveDialog(null);
            if (file == null) {  // Quit if the operation is cancelled
                recording = !recording;
                return;
            }
            Thread thread = new Thread(() -> {
                if (!giffer.toGIF(file)) {
                    Platform.runLater(() -> {Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error in generating *.gif");
                        alert.setHeaderText("The operation was unsuccessful.");
                        alert.setContentText("The operation was unsuccessful. " +
                                "If you suspect a bug, please report it.");
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
                        alert.showAndWait();
                    });
                }
                else {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Operation successful!");
                        alert.setHeaderText("The operation was successful.");
                        alert.setContentText("The operation was successful. " +
                                "The *.gif has been saved to " + file.getAbsolutePath() + ".");
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
                        alert.showAndWait();
                    });
                }
            });
            thread.setName("Giffer Thread");
            thread.start();
        }
        else {
            // Change icon for recording
            recordingImage.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/RecordIcon2.png")));

            // Get giffer from the dialog
            gifferDialog.showAndWait();

            // Check if the operation was cancelled
            if (gifferDialog.getResult() == Boolean.FALSE) {
                return;
            }

            giffer = gifferDialog.getGiffer();
        }

        recording = !recording;
    }

    @FXML // Closes the application
    public void closeApplication() {
        onApplicationClosed();
        Platform.exit();
    }

    // Saving settings when the application is closed
    public void onApplicationClosed() {
        try {
            ObjectMapper m = new ObjectMapper();

            // Configuring settings
            settings.put("grid_lines", showGridLines);
            settings.put("rule", new JSONObject(m.writeValueAsString(simulator.getRule())));

            // Writing to file
            FileWriter writer = new FileWriter(SETTINGS_FILE);
            writer.write(settings.toString(4));
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Loads the pattern based on an RLE
    public void loadPattern(String RLE) {
        String[] tokens = RLE.split("\n");  // Split by new line
        loadPattern(tokens);
    }

    public void loadPattern(String[] tokens) {
        String rulestring = "";
        Pattern rulestringRegex = Pattern.compile("rule = \\S+");
        ArrayList<String> comments = new ArrayList<>();  // Comments to feed into RuleFamily.loadComments()

        // Parsing code - Removes headers, comments
        StringBuilder rleFinal = new StringBuilder();
        for (String token: tokens) {
            if (token.startsWith("#R")) {  // Check for comment
                comments.add(token);
            }
            else if (token.charAt(0) == 'x') {  // Check for header
                Matcher rulestringMatcher = rulestringRegex.matcher(token);
                if (rulestringMatcher.find()) {
                    rulestring = rulestringMatcher.group().substring(7);
                }
            }
            else if (token.charAt(0) != '#') {  // Not a comment
                rleFinal.append(token);
            }
        }

        // Identify the rule family based on regex
        boolean found = false;
        RuleFamily rule = null;
        for (RuleWidget widget: dialog.getRuleWidgets()) {
            for (String regex: widget.getRuleFamily().getRegex()) {
                if (rulestring.matches(regex)) {
                    found = true;
                    break;
                }
            }

            // Completely break out of the loop
            if (found) {
                widget.getRuleFamily().setRulestring(rulestring);
                rule = widget.getRuleFamily();
                break;
            }
        }

        if (rule != null) {
            // Generate the additional information from comments
            String[] commentsArray = new String[comments.size()];
            for (int i = 0; i < commentsArray.length; i++) {
                commentsArray[i] = comments.get(i);
            }

            rule.loadComments(commentsArray);

            // Set the rulestring
            simulator.setRule(rule);
        }

        newPattern();  // Clear all cells
        simulator.fromRLE(rleFinal.toString(), // Insert the new cells
                new Coordinate(1800 / CELL_SIZE, 1800 / CELL_SIZE));
        renderCells();  // Render the new cells
        
        // Centering the viewport
        // scrollPane.setHvalue(0.2);
        // scrollPane.setVvalue(0.2);
        // drawingPane.setTranslateX(0);
        // drawingPane.setTranslateY(0);

        // Setting the rule of the rule dialog
        dialog.setRule((RuleFamily) simulator.getRule());

        // Reloading the state buttons to state the number of states
        reloadStateButtons();

        // Setting the generation count back to 0
        simulator.setGeneration(0);
    }

    public void loadPattern(ArrayList<String> tokens) {
        String[] tokensArray = new String[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            tokensArray[i] = tokens.get(i);
        }

        loadPattern(tokensArray);
    }

    // Returns the RLE of the pattern
    public String saveRLE() {
        // Add header & comments
        String rle = simulator.toRLE(startSelection, endSelection);
        StringBuilder rleFinal = new StringBuilder();

        // Adding comments
        String[] comments = ((RuleFamily) simulator.getRule()).generateComments();
        if (comments != null) {
            for (String comment: comments) {
                rleFinal.append(comment).append("\n");
            }
        }

        // Adding header
        rleFinal.append("x = ").append(endSelection.getX() - startSelection.getX()).
                append(", y = ").append(endSelection.getY() - startSelection.getY()).
                append(", rule = ").append(((RuleFamily) simulator.getRule()).getRulestring()).append("\n");
        rleFinal.append(rle);

        return rleFinal.toString();
    }

    @FXML // Saves the pattern
    public void savePattern() {
        // Get the file to save the pattern in
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save *.rle file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "RLE Files (*.rle)", "*.rle"));
        File file = fileChooser.showSaveDialog(null);

        try {
            // Writing to the file
            FileWriter writer = new FileWriter(file);
            writer.write(saveRLE());
            writer.close();

            // Tell the user the operation was successful
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Operation successful!");
            alert.setHeaderText("The operation was successful!");
            alert.setContentText("The operation was successful. " +
                    "The pattern has been saved to " + file.getAbsolutePath() + ".");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
            alert.showAndWait();

        }
        catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Writing to pattern file");
            alert.setHeaderText("An error occuring when writing to the pattern file!");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    @FXML // Creates a new pattern
    public void newPattern() {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        simulator.iterateCells(coordinates::add);

        // Avoiding ConcurrentModificationException
        for (Coordinate coordinate: coordinates) {
            setCell(coordinate.getX() * CELL_SIZE, coordinate.getY() * CELL_SIZE, 0);
        }
    }

    @FXML // Loads the pattern from the RLE file
    public void openPattern() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open pattern file");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    "RLE Files (*.rle)", "*.rle"));
            File file = fileChooser.showOpenDialog(null);

            Scanner scanner = new Scanner(file);
            ArrayList<String> tokens = new ArrayList<>();

            while (scanner.hasNextLine()) {  // Getting all text from scanner
                tokens.add(scanner.nextLine());
            }

            scanner.close();  // Close the scanner object

            loadPattern(tokens);  // Loads the pattern
        }
        catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error reading pattern file");
            alert.setHeaderText("There was an error reading the pattern file!");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    @FXML // Pastes the RLE from the clipboard
    public void pasteRLE() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        String RLE = clipboard.getString();

        // Parsing code - Removes headers, comments
        StringBuilder rleFinal = new StringBuilder();
        String[] tokens = RLE.split("\n");

        for (String token: tokens) {
            if (!(token.charAt(0) == '#') && !(token.charAt(0) == 'x')) {  // Check for comment & header
                rleFinal.append(token);
            }
        }

        mode = Mode.PASTING;

        pasteStuff = new Grid();
        pasteStuff.fromRLE(rleFinal.toString(), new Coordinate(0, 0));

        pasteSelection.getChildren().clear();
        pasteStuff.iterateCells(coordinate -> {
            Rectangle cell = new Rectangle();
            cell.setX(coordinate.getX() * CELL_SIZE);
            cell.setY(coordinate.getY() * CELL_SIZE);
            cell.setWidth(CELL_SIZE);
            cell.setHeight(CELL_SIZE);
            cell.setFill(simulator.getRule().getColour(pasteStuff.getCell(coordinate)));

            // Add cell to pane and cell list
            pasteSelection.getChildren().add(cell);
        });

        pasteSelection.setVisible(true);
        pasteSelection.toFront();
        gridLines.toFront();

        //simulator.fromRLE(rleFinal.toString(), startSelection);  // Insert the cells
        //renderCells(startSelection, endSelection);  // Render the cells
    }

    @FXML // Copies the currently selected cells to the clipboard
    public void copyCells() {
        if (mode == Mode.SELECTING) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();

            content.putString(saveRLE());
            clipboard.setContent(content);
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No area selected!");
            alert.setContentText("No area has been selected!");
            alert.showAndWait();
        }
    }

    @FXML // Deletes the cells in the selection
    public void deleteCells() {
        simulator.clearCells(startSelection, endSelection);
        renderCells(startSelection, endSelection);

        // Move the grid lines and selection to the front
        selectionRectangle.toFront();
        gridLines.toFront();
    }

    // Handles the keyboard shortcuts
    public void keyPressedHandler(KeyEvent event) {
        event.consume();  // No one touches this but me

        // Enter to toggle simulation
        if (event.getCode().equals(KeyCode.ENTER)) {
            toggleSimulation();
        }
        // Space to step simulation
        else if (event.getCode().equals(KeyCode.SPACE)) {
            history.add(simulator.deepCopy());
            updateCells();
        }
        // Delete cells
        else if (event.getCode().equals(KeyCode.DELETE)) {
            deleteCells();
        }
        // Ctrl + C to copy
        else if (event.getCode().equals(KeyCode.C) && event.isControlDown()) {
            copyCells();
        }
        // Ctrl + V to paste
        else if (event.getCode().equals(KeyCode.V) && event.isControlDown()) {
            pasteRLE();
        }
        // Ctrl + X to cut
        else if (event.getCode().equals(KeyCode.X) && event.isControlDown()) {
            copyCells();
            deleteCells();
        }
        // Ctrl + A to select all
        else if (event.getCode().equals(KeyCode.A) && event.isControlDown()) {
            simulator.updateBounds();
            startSelection = new Coordinate(simulator.getBounds().getValue0().getX() * CELL_SIZE,
                    simulator.getBounds().getValue0().getY() * CELL_SIZE);
            endSelection = new Coordinate(simulator.getBounds().getValue1().getX() * CELL_SIZE,
                    simulator.getBounds().getValue1().getY() * CELL_SIZE);

            selectionRectangle.setX(simulator.getBounds().getValue0().getX() * CELL_SIZE);
            selectionRectangle.setY(simulator.getBounds().getValue0().getY() * CELL_SIZE);

            selectionRectangle.setWidth(simulator.getBounds().getValue1().getX() * CELL_SIZE -
                    startSelection.getX() * CELL_SIZE + 1);
            selectionRectangle.setHeight(simulator.getBounds().getValue1().getY() * CELL_SIZE -
                    startSelection.getY() * CELL_SIZE + 1);

            selectionRectangle.setVisible(true);
            mode = Mode.SELECTING;
            
            // Move the grid lines and selection to the front
            selectionRectangle.toFront();
            gridLines.toFront();
        }
        // Ctrl + Z to undo
        else if (event.getCode().equals(KeyCode.Z) && event.isControlDown()) {
            newPattern();
            if (history.size() > 1) {
                insertCells(history.get(history.size() - 1), 0, 0);
                history.remove(history.size() - 1);
            }
        }
        // Ctrl + Shift + O to load pattern from clipboard
        else if (event.getCode().equals(KeyCode.O) && event.isShiftDown() && event.isControlDown()) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            String RLE = clipboard.getString();

            loadPattern(RLE);
        }
        // Ctrl + O to open pattern
        else if (event.getCode().equals(KeyCode.O) && event.isControlDown()) {
            openPattern();
        }
        // Ctrl + S to save pattern
        else if (event.getCode().equals(KeyCode.S) && event.isControlDown()) {
            savePattern();
        }
        // Ctrl + N for new pattern
        else if (event.getCode().equals(KeyCode.N) && event.isControlDown()) {
            newPattern();
        }
        // Ctrl + R to start rule dialog
        else if (event.getCode().equals(KeyCode.R) && event.isControlDown()) {
            startRuleDialog();
        }
        // Ctrl + 5 for random soup
        else if (event.getCode().equals(KeyCode.DIGIT5) && event.isControlDown()) {
            generateRandomSoup();
        }
        // X to flip horizontally
        else if (event.getCode().equals(KeyCode.X)) {
            flipHorizontalHandler();
        }
        // Y to flip vertically
        else if (event.getCode().equals(KeyCode.Y)) {
            flipVerticalHandler();
        }
        // > to rotate clockwise
        else if (event.getCode().equals(KeyCode.PERIOD) && event.isShiftDown()) {
            rotateCWHandler();
        }
        // < to rotate counter-clockwise
        else if (event.getCode().equals(KeyCode.COMMA) && event.isShiftDown()) {
            rotateCCWHandler();
        }
    }

    // Wait for the specified number of milliseconds
    public void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
