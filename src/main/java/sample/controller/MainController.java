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
import sample.controller.dialogs.PopulationGraphDialog;
import sample.controller.dialogs.RandomSoupDialog;
import sample.controller.dialogs.rule.RuleDialog;
import sample.controller.dialogs.search.*;
import sample.model.Cell;
import sample.model.*;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.Rule;
import sample.model.rules.RuleFamily;
import sample.model.rules.hrot.HROT;
import sample.model.rules.ruleloader.ColourDirective;
import sample.model.rules.ruleloader.RuleDirective;
import sample.model.rules.ruleloader.RuleLoader;
import sample.model.rules.ruleloader.RuleNameDirective;
import sample.model.search.catsrc.CatalystSearch;
import sample.model.search.csearch.BruteForceSearch;
import sample.model.search.rulesrc.RuleSearch;
import sample.model.simulation.Grid;
import sample.model.simulation.Simulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
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
    private Button startSimulationButton, simInsideSelectionButton, simOutsideSelectionButton;

    @FXML
    private ToolBar secondaryToolbar;

    @FXML
    private ImageView recordingImage, playButtonImage, playButtonImage1, playButtonImage2;

    @FXML
    private CheckMenuItem gridLinesMenuItem;

    private final int WIDTH = 4096;
    private final int HEIGHT = 4096;
    private final int CELL_SIZE = 1;  // Cell size
    private final String SETTINGS_FILE = "settings.json";

    private SelectionRectangle selectionRectangle;  // Rectangle that represents selection box

    private final RuleDialog dialog = new RuleDialog();  // Dialog to set rule
    private final GifferDialog gifferDialog = new GifferDialog();  // Dialog to create *.gifs

    private Mode mode;  // Mode (Drawing, Selecting, Panning)
    private Simulator simulator;  // Simulator to simulate rule
    private ArrayList<Button> stateButtons;  // Buttons to switch between states
    private HashMap<Coordinate, Cell> cellList;  // List of cell objects
    private Set<Coordinate> deadCellsSet;
    private LRUCache<Coordinate, Cell> deadCellsCache;  // LRU Cache of dead cells
    private Group gridLines;  // The grid lines of the pattern editor

    private Group pasteSelection;  // The group that renders the stuff to be pasted
    private Grid pasteStuff;  // The stuff to paste

    private ArrayList<Integer> populationList;  // The population list

    private int density;  // The density of the random soup
    private String symmetry;  // The symmetry of the random soup
    private ArrayList<Integer> statesToInclude;  // The states to include in the random soup
    private RandomSoupDialog randomSoupDialog;  // Dialog to adjust random soup settings

    private int simulationTime, visualisationTime;  // Time taken for visualisation and simulation

    private int minSimTime = 0;  // Minimum time for one step
    private int stepSize = 1;  // Step size of the simulation
    private int currentState = 1;  // State to draw with

    private boolean recording = false;  // Is the recording on?
    private final boolean simulationRunning = false;  // Is the simulation running?
    private boolean visualisationDone = true;  // Is the visualisation done?
    private boolean showGridLines = false;  // Are the grid lines being shown?

    private SimulationMode simulationMode = SimulationMode.PAUSED;

    private JSONObject settings;  // Store the settings

    private Giffer giffer;  // For writing to a *.gif

    private Logger logger;

    @FXML
    public void initialize() {
        // Initialise logger
        logger = LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.log(Level.INFO, "GUI Initialising...");

        // Initialise variables
        cellList = new HashMap<>();
        deadCellsCache = new LRUCache<>(200);
        stateButtons = new ArrayList<>();
        populationList = new ArrayList<>();
        deadCellsSet = new HashSet<>();
        mode = Mode.DRAWING;

        symmetry = "C1";
        density = 50;
        statesToInclude = new ArrayList<>(Arrays.asList(1));

        deadCellsCache.setDeleteFunc((coordinate, cell) -> {
            if (cell.getState() != 0) return;

            // Destroy the cell object (remove all references to it so it is garbage collected)
            removeCellObject(coordinate.getX(), coordinate.getY());
            drawingPane.getChildren().remove(cell.getRectangle());
        });
        deadCellsCache.setCheckValid(cell -> cell.getState() == 0);

        // Create simulator object
        simulator = new Simulator(new HROT("R2,C2,S6-9,B7-8,NM"));

        // Create selection rectangle and set properties
        selectionRectangle = new SelectionRectangle(CELL_SIZE);
        drawingPane.getChildren().add(selectionRectangle);

        // Linking with Action class to handle undo / redo
        Action.setController(this);

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
            lineX.setStrokeWidth(0.15 * CELL_SIZE);
            lineX.setStartX(i);
            lineX.setEndX(i);
            lineX.setStartY(0);
            lineX.setEndY(HEIGHT);
            lineX.toFront();
            gridLines.getChildren().add(lineX);

            Line lineY = new Line();
            lineY.setStroke(Color.GREY);
            lineY.setStrokeWidth(0.15 * CELL_SIZE);
            lineY.setStartX(0);
            lineY.setEndX(HEIGHT);
            lineY.setStartY(i);
            lineY.setEndY(i);
            lineY.toFront();
            gridLines.getChildren().add(lineY);

            if (i % 10 == 0) {
                lineX.setStrokeWidth(0.2 * CELL_SIZE);
                lineY.setStrokeWidth(0.2 * CELL_SIZE);
            }
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
            logger.log(Level.WARNING, exception.getMessage());
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
            Action.addAction();
            setCell(snapToGrid((int) event.getX()),
                    snapToGrid((int) event.getY()),
                    currentState);
        }
        else if (mode == Mode.SELECTING) {
            selectionRectangle.select(new Coordinate(snapToGrid((int) event.getX()),
                    snapToGrid((int) event.getY())));
        }
    }

    @FXML
    public void mouseDragStartHandler(MouseEvent event) {
        if (mode == Mode.SELECTING) {
            selectionRectangle.unselect();
            selectionRectangle.toFront();

            Coordinate startSelection = new Coordinate(snapToGrid((int) event.getX()),
                    snapToGrid((int) event.getY()));
            selectionRectangle.select(startSelection, startSelection);
        }
        else if (mode == Mode.PASTING) {
            Action.addAction();

            insertCells(pasteStuff, convertToGrid((int) event.getX()), convertToGrid((int)event.getY()));
            pasteSelection.setVisible(false);

            mode = Mode.SELECTING;
        }
    }

    @FXML
    public void mouseDragDoneHandler(MouseEvent event) {
        Coordinate endSelection = null;
        if (mode == Mode.SELECTING && selectionRectangle.isVisible()) {
            endSelection = new Coordinate((int)event.getX(), (int)event.getY());
        }

        if (mode != Mode.PASTING) {
            if (selectionRectangle.getStart() != null && endSelection != null &&
                    (endSelection.subtract(selectionRectangle.getStart()).getX() < 1 ||
                            endSelection.subtract(selectionRectangle.getStart()).getY() < 1)) {
                selectionRectangle.unselect();
            }
        }
    }

    @FXML
    public void mouseMovedHandler(MouseEvent event) {
        if (mode == Mode.PASTING) {
            pasteSelection.setTranslateX(snapToGrid((int)(event.getX())));
            pasteSelection.setTranslateY(snapToGrid((int)(event.getY())));
        }
    }

    @FXML // Generates random soup in the selection box
    public void generateRandomSoup() {
        int[] states = new int[statesToInclude.size()];
        for (int i = 0; i < states.length; i++) {
            states[i] = statesToInclude.get(i);
        }

        // Generate the soup
        Grid soup = SymmetryGenerator.generateSymmetry(symmetry, density, states,
                selectionRectangle.getEnd().getX() - selectionRectangle.getStart().getX() + 1,
                selectionRectangle.getEnd().getY() - selectionRectangle.getStart().getY() + 1);

        // Insert the cells in the pane (automatically inserted in the simulator)
        insertCells(soup, selectionRectangle.getStart().getX(), selectionRectangle.getStart().getY());

        // Move the grid lines and selection to the front
        selectionRectangle.toFront();
        gridLines.toFront();
    }

    @FXML // Flips selected cells horizontally
    public void flipHorizontalHandler() {
        Action.addAction();

        // Reflect cells in the grid
        simulator.reflectCellsX(selectionRectangle.getStart(), selectionRectangle.getEnd());
        renderCells(selectionRectangle.getStart(), selectionRectangle.getEnd());

        // Move the grid lines and selection to the front
        selectionRectangle.toFront();
        gridLines.toFront();
    }

    @FXML // Flips selected cells vertically
    public void flipVerticalHandler() {
        Action.addAction();

        // Reflect cells in the grid
        simulator.reflectCellsY(selectionRectangle.getStart(), selectionRectangle.getEnd());
        renderCells(selectionRectangle.getStart(), selectionRectangle.getEnd());

        // Move the grid lines and selection to the front
        selectionRectangle.toFront();
        gridLines.toFront();
    }

    @FXML // Rotates the selected cells clockwise
    public void rotateCWHandler() {
        Action.addAction();

        // Rotate the cells in the grid
        Coordinate start = selectionRectangle.getStart(), end = selectionRectangle.getEnd();
        simulator.rotateCW(selectionRectangle.getStart(), selectionRectangle.getEnd());
        renderCells(selectionRectangle.getStart(), selectionRectangle.getEnd());

        // Rotate selection rectangle
        if ((end.getX() - start.getX()) % 2 == 1) end = new Coordinate(end.getX() + 1, end.getY());
        if ((end.getY() - start.getY()) % 2 == 1) end = new Coordinate(end.getX(), end.getY() + 1);

        int centerX = (end.getX() - start.getX()) / 2 + start.getX();
        int centerY = (end.getY() - start.getY()) / 2 + start.getY();

        int dxEnd = centerX - end.getX();
        int dyEnd = centerY - end.getY();

        int dxStart = centerX - start.getX();
        int dyStart = centerY - start.getY();

        selectionRectangle.select(convertToScreen(new Coordinate(centerX - dyStart, centerY + dxEnd)),
                convertToScreen(new Coordinate(centerX - dyEnd, centerY + dxStart)));

        renderCells(selectionRectangle.getStart(), selectionRectangle.getEnd());

        // Move the grid lines and selection to the front
        selectionRectangle.toFront();
        gridLines.toFront();
    }

    @FXML // Rotates the selected cells counter-clockwise
    public void rotateCCWHandler() {
        Action.addAction();

        // Rotate the cells in the grid
        Coordinate start = selectionRectangle.getStart(), end = selectionRectangle.getEnd();
        simulator.rotateCCW(selectionRectangle.getStart(), selectionRectangle.getEnd());
        renderCells(selectionRectangle.getStart(), selectionRectangle.getEnd());

        // Rotate selection rectangle
        if ((end.getX() - start.getX()) % 2 == 1) end = new Coordinate(end.getX() + 1, end.getY());
        if ((end.getY() - start.getY()) % 2 == 1) end = new Coordinate(end.getX(), end.getY() + 1);

        int centerX = (end.getX() - start.getX()) / 2 + start.getX();
        int centerY = (end.getY() - start.getY()) / 2 + start.getY();

        int dxEnd = centerX - end.getX();
        int dyEnd = centerY - end.getY();

        int dxStart = centerX - start.getX();
        int dyStart = centerY - start.getY();

        selectionRectangle.select(convertToScreen(new Coordinate(centerX + dyEnd, centerY - dxStart)),
                convertToScreen(new Coordinate(centerX + dyStart, centerY - dxEnd)));

        renderCells(selectionRectangle.getStart(), selectionRectangle.getEnd());

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
        while (deadCellsSet.iterator().hasNext()) {
            Coordinate cell = deadCellsSet.iterator().next();  // No ConcurrentModficationException
            // if (deadCellsCache.get(cell).getState() != 0) return;

            // Destroy the cell object (remove all references to it so it is garbage collected)
            drawingPane.getChildren().remove(cellList.get(cell).getRectangle());
            removeCellObject(cell.getX(), cell.getY());
            deadCellsSet.remove(cell);
        }
    }

    @FXML // Views the population graph
    public void viewPopulationGraph() {
        PopulationGraphDialog dialog = new PopulationGraphDialog(populationList);
        dialog.show();
    }

    @FXML // Changing the random soup settings
    public void changeRandomSoupSettings() {
        randomSoupDialog = new RandomSoupDialog(simulator.getRule().getNumStates(), density, symmetry,
                statesToInclude);
        randomSoupDialog.showAndWait();

        if (randomSoupDialog.getResult() == Boolean.TRUE) {
            density = randomSoupDialog.getDensity();
            symmetry = randomSoupDialog.getSymmetry();
            statesToInclude = randomSoupDialog.getStates();
        }
    }

    @FXML  // Displays information about the current rule
    public void getRuleInformation() {
        StringBuilder contentText = new StringBuilder();
        Map<String, String> information = ((RuleFamily) simulator.getRule()).getRuleInfo();
        for (String key: information.keySet()) {
            contentText.append(key).append(": ").append(information.get(key)).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rule Information");
        alert.setHeaderText("Information");
        alert.setContentText(contentText.toString());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
        alert.showAndWait();
    }

    // Function to set cell at (x, y) to a certain state
    public void setCell(int x, int y, int state) {
        setCell(x, y, state, true);
    }

    public void setCell(int x, int y, int state, boolean updateSimulator) {
        // Get the cell object at the specified coordinate
        Cell prevCell = getCellObject(x, y);
        if (prevCell == null && state != 0) {  // Insert a new cell if one doesn't already exist
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
        else if (prevCell != null && prevCell.getState() != state) {  // Don't bother if the cell didn't change
            if (prevCell.getState() == 0) deadCellsSet.remove(prevCell.getCoordinate());
            // if (prevCell.getState() == 0) deadCellsCache.remove(prevCell.getCoordinate());

            prevCell.getRectangle().setFill(simulator.getRule().getColour(state));
            prevCell.setState(state);

            //if (state == 0) deadCellsCache.put(new Coordinate(x, y), prevCell);
            if (state == 0) deadCellsSet.add(new Coordinate(x, y));
        }

        // Add cell to simulator
        if (updateSimulator)
            simulator.setCell(convertToGrid(x), convertToGrid(y), state);
    }

    public void insertCells(Grid cellsToInsert, int x, int y) {
        cellsToInsert.iterateCells(coord -> {
            Coordinate newCell = coord.add(new Coordinate(x, y));
            setCell(convertToScreen(newCell.getX()), convertToScreen(newCell.getY()),
                    cellsToInsert.getCell(coord));
        });
    }

    // Renders all cells
    public void renderCells() {
        selectionRectangle.toFront();
        gridLines.toFront();
        simulator.iterateCells(coordinate -> setCell(convertToGrid(coordinate.getX()),
                convertToGrid(coordinate.getY()),
                simulator.getCell(coordinate),false));
    }

    // Renders cells between the start and end coordinate
    public void renderCells(Coordinate startSelection, Coordinate endSelection) {
        for (int i = startSelection.getX(); i < endSelection.getX() + 1; i++) {
            for (int j = startSelection.getY(); j < endSelection.getY() + 1; j++) {
                setCell(convertToScreen(i), convertToScreen(j), simulator.getCell(i, j));  // Render the new cells
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
        deadCellsCache.setCapacity(simulator.getPopulation() * 5);

        String simulationString = String.format("Simulation Speed: %.2f step/s",
                1000.0 / (visualisationTime + simulationTime));

        statusLabel.setText("Generation: " + simulator.getGeneration() + ", " +
                simulationString + ", Population: " + simulator.getPopulation());
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
        synchronized (this) {
            visualisationDone = false;

            Set<Coordinate> cellsChanged = new HashSet<>();

            long startTime = System.currentTimeMillis();
            try {
                for (int i = 0; i < stepSize; i++) {
                    if (simulationMode == SimulationMode.IN_SELECTION)
                        simulator.step(coordinate -> coordinate.getX() >= selectionRectangle.getStart().getX() &&
                                coordinate.getY() >= selectionRectangle.getStart().getY() &&
                                coordinate.getX() <= selectionRectangle.getEnd().getX() &&
                                coordinate.getY() <= selectionRectangle.getEnd().getY());
                    else if (simulationMode == SimulationMode.OUTSIDE_SELECTION)
                        simulator.step(coordinate -> coordinate.getX() < selectionRectangle.getStart().getX() ||
                                coordinate.getY() < selectionRectangle.getStart().getY() ||
                                coordinate.getX() > selectionRectangle.getEnd().getX() ||
                                coordinate.getY() > selectionRectangle.getEnd().getY());
                    else simulator.step();

                    if (stepSize > 1) cellsChanged.addAll(simulator.getCellsChanged());
                    else cellsChanged = simulator.getCellsChanged();

                    populationList.add(simulator.getPopulation());
                }
            }
            catch (ConcurrentModificationException exception) {
                logger.log(Level.WARNING, exception.getMessage());
            }

            simulationTime = (int) (System.currentTimeMillis() - startTime);

            Set<Coordinate> finalCellsChanged = cellsChanged;
            Platform.runLater(() -> {
                try {
                    long startTime2 = System.currentTimeMillis();

                    // Only update cells that changed (for speed)
                    for (Coordinate cell: finalCellsChanged) {
                        setCell(convertToScreen(cell.getX()), convertToScreen(cell.getY()), simulator.getCell(cell));
                    }

                    // Move the grid lines and selection to the front
                    if (selectionRectangle.isVisible()) selectionRectangle.toFront();
                    if (gridLines.isVisible()) gridLines.toFront();

                    visualisationTime = (int) (System.currentTimeMillis() - startTime2);
                }
                catch (ConcurrentModificationException exception) { // Catch an exception that will hopefully not happen
                    logger.log(Level.WARNING, exception.getMessage());

                    simulationMode = SimulationMode.PAUSED;

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
                giffer.addGrid(selectionRectangle.getStart(), selectionRectangle.getEnd(),
                        simulator.getCells(selectionRectangle.getStart(), selectionRectangle.getEnd()),
                        simulator.getRule());
            }
        }
    }

    // Runs simulation
    public void runSimulation() {
        int num = 1;

        while (true) {
            if (simulationMode != SimulationMode.PAUSED) {
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
        Action.addAction();

        if (simulationMode == SimulationMode.PAUSED) {
            simulationMode = SimulationMode.RUNNING;
            playButtonImage.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/StopButtonEater.png")));
            playButtonImage1.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/SimInSelection.png")));
            playButtonImage2.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/SimOutSelection.png")));
        } else {
            simulationMode = SimulationMode.PAUSED;
            playButtonImage.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/GliderPlayBtn1.png")));
            playButtonImage1.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/SimInSelection.png")));
            playButtonImage2.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/SimOutSelection.png")));
        }
    }

    @FXML  // Toggles simulation on and off
    public void toggleSimulation1() {
        Action.addAction();

        if (simulationMode == SimulationMode.PAUSED) {
            simulationMode = SimulationMode.IN_SELECTION;
            playButtonImage.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/GliderPlayBtn1.png")));
            playButtonImage1.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/StopInSelection.png")));
            playButtonImage2.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/SimOutSelection.png")));
        } else {
            simulationMode = SimulationMode.PAUSED;
            playButtonImage.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/GliderPlayBtn1.png")));
            playButtonImage1.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/SimInSelection.png")));
            playButtonImage2.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/SimOutSelection.png")));
        }
    }

    @FXML  // Toggles simulation on and off
    public void toggleSimulation2() {
        Action.addAction();

        if (simulationMode == SimulationMode.PAUSED) {
            simulationMode = SimulationMode.OUTSIDE_SELECTION;
            playButtonImage.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/GliderPlayBtn1.png")));
            playButtonImage1.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/SimInSelection.png")));
            playButtonImage2.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/StopOutSelection.png")));
        } else {
            simulationMode = SimulationMode.PAUSED;
            playButtonImage.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/GliderPlayBtn1.png")));
            playButtonImage1.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/SimInSelection.png")));
            playButtonImage2.setImage(new Image(getClass().getResourceAsStream(
                        "/icon/SimOutSelection.png")));
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
            Action.addAction();

            // Set the rule
            simulator.setRule(dialog.getRule());

            // Reload the number of state buttons
            reloadStateButtons();
        }
    }

    @FXML // Starts the rule search dialog
    public void startRuleSearchDialog() {
        // Dialog to get the search parameters
        RuleSearchParametersDialog parametersDialog = new RuleSearchParametersDialog(
                simulator.getCells(selectionRectangle.getStart(), selectionRectangle.getEnd()),
                simulator.getRule());
        parametersDialog.showAndWait();

        if (parametersDialog.getResult() == Boolean.TRUE) {  // If the operation wasn't cancelled
            RuleSearch ruleSearch = new RuleSearch(parametersDialog.getSearchParameters());
            ruleSearch.searchThreaded(Integer.MAX_VALUE, parametersDialog.getNumThreads());

            RuleSearchResultsDialog resultsDialog = new RuleSearchResultsDialog(this, ruleSearch);
            resultsDialog.show();
        }
    }

    @FXML // Starts the catalyst search dialog
    public void startCatalystSearchDialog() {
        // Dialog to get the search parameters
        List<Coordinate> cellCoordinates = new ArrayList<>();
        for (int i = selectionRectangle.getStart().getX(); i < selectionRectangle.getEnd().getX() + 1; i++) {
            for (int j = selectionRectangle.getStart().getY(); j < selectionRectangle.getEnd().getY() + 1; j++) {
                cellCoordinates.add(new Coordinate(i, j));
            }
        }

        CatalystSearchParametersDialog parametersDialog =
                new CatalystSearchParametersDialog(simulator.getRule(), simulator.deepCopy(), cellCoordinates);
        parametersDialog.showAndWait();

        if (parametersDialog.getResult() == Boolean.TRUE) {  // If the operation wasn't cancelled
            CatalystSearch catalystSearch = new CatalystSearch(parametersDialog.getSearchParameters());
            catalystSearch.searchThreaded(Integer.MAX_VALUE, parametersDialog.getNumThreads());

            CatalystSearchResultsDialog resultsDialog = new CatalystSearchResultsDialog(this, catalystSearch);
            resultsDialog.show();
        }
    }

    @FXML // Starts the brute force search dialog
    public void startBruteForceSearchDialog() {
        // Dialog to get the search parameters
        BruteForceSearchParametersDialog parametersDialog =
                new BruteForceSearchParametersDialog(simulator.getRule());
        parametersDialog.showAndWait();

        if (parametersDialog.getResult() == Boolean.TRUE) {  // If the operation wasn't cancelled
            BruteForceSearch bruteForceSearch = new BruteForceSearch(parametersDialog.getSearchParameters());
            bruteForceSearch.searchThreaded(Integer.MAX_VALUE, parametersDialog.getNumThreads());

            BruteForceSearchResultsDialog resultsDialog = new BruteForceSearchResultsDialog(this, bruteForceSearch);
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

                    RuleDirective[] ruleDirectives = ((ApgtableGeneratable) simulator.getRule()).generateApgtable();

                    RuleLoader ruleLoader = new RuleLoader();
                    ruleLoader.addDirective(new RuleNameDirective("@RULE " +
                            file.getName().replace(".rule", "")));

                    StringBuilder colourDirective = new StringBuilder("@COLORS\n");
                    for (int i = 0; i < simulator.getRule().getNumStates(); i++) {
                        colourDirective.append(i).
                                append(" ").append((int) (simulator.getRule().getColour(i).getRed() * 255)).
                                append(" ").append((int) (simulator.getRule().getColour(i).getGreen() * 255)).
                                append(" ").append((int) (simulator.getRule().getColour(i).getBlue() * 255)).
                                append("\n");
                    }
                    ruleLoader.addDirective(new ColourDirective(colourDirective.toString()));

                    for (RuleDirective directive: ruleDirectives) ruleLoader.addRuleDirective(directive);

                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(ruleLoader.export());
                    fileWriter.close();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Operation successful!");
                    alert.setHeaderText("The operation was successful.");
                    alert.setContentText("The operation was successful. " +
                            "The apgtable has been saved to " + file.getAbsolutePath() + ".");
                    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
                    alert.showAndWait();
                }
            } catch (UnsupportedOperationException exception) {
                logger.log(Level.WARNING, exception.getMessage());

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error in generating APGTable");
                alert.setHeaderText("APGTable generation is not supported by this rule / rulespace!");
                alert.setContentText(exception.getMessage());
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
                alert.showAndWait();
            } catch (IOException exception) {
                logger.log(Level.WARNING, exception.getMessage());

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
        simulator.insertCells(this.simulator.getCells(selectionRectangle.getStart(), selectionRectangle.getEnd()),
                new Coordinate());
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
            logger.log(Level.WARNING, exception.getMessage());
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
        RuleFamily rule = Utils.fromRulestring(rulestring);

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

        // Clear the population list
        populationList.clear();

        // Update the status text
        updateStatusText();
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
        String rle = simulator.toRLE(selectionRectangle.getStart(), selectionRectangle.getEnd());
        StringBuilder rleFinal = new StringBuilder();

        // Adding comments
        String[] comments = ((RuleFamily) simulator.getRule()).generateComments();
        if (comments != null) {
            for (String comment: comments) {
                rleFinal.append(comment).append("\n");
            }
        }

        // Adding header
        rleFinal.append("x = ").append(selectionRectangle.getEnd().getX() - selectionRectangle.getStart().getX() + 1).
                append(", y = ").append(selectionRectangle.getEnd().getY() - selectionRectangle.getStart().getY() + 1).
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
            logger.log(Level.WARNING, exception.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Writing to pattern file");
            alert.setHeaderText("An error occuring when writing to the pattern file!");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    @FXML // Creates a new pattern
    public void newPattern() {
        // Add all the cells to a list to avoid ConcurrentModificationException
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        simulator.iterateCells(coordinates::add);

        // Avoiding ConcurrentModificationException
        for (Coordinate coordinate: coordinates) {
            setCell(convertToScreen(coordinate.getX()), convertToScreen(coordinate.getY()), 0);
        }

        // Setting the generation count back to 0
        simulator.setGeneration(0);

        // Clear the population list
        populationList.clear();
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
            logger.log(Level.WARNING, exception.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error reading pattern file");
            alert.setHeaderText("There was an error reading the pattern file!");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    @FXML // Pastes the RLE from the clipboard
    public void pasteRLE() {
        // Get text from clipboard
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
        pasteStuff.updateBounds();

        pasteSelection.getChildren().clear();
        pasteStuff.iterateCells(coordinate -> {
            Rectangle cell = new Rectangle();
            cell.setX(convertToScreen(coordinate.getX()));
            cell.setY(convertToScreen(coordinate.getY()));
            cell.setWidth(CELL_SIZE);
            cell.setHeight(CELL_SIZE);
            cell.setFill(simulator.getRule().getColour(pasteStuff.getCell(coordinate)));

            // Add cell to pane and cell list
            pasteSelection.getChildren().add(cell);
        });

        SelectionRectangle rectangle = new SelectionRectangle(CELL_SIZE);
        rectangle.setFill(Color.rgb(255, 0, 0));
        rectangle.select(convertToScreen(pasteStuff.getBounds().getValue0()), convertToScreen(pasteStuff.getBounds().getValue1()));
        pasteSelection.getChildren().add(rectangle);

        rectangle.toFront();

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
        Action.addAction();

        simulator.clearCells(selectionRectangle.getStart(), selectionRectangle.getEnd());
        renderCells(selectionRectangle.getStart(), selectionRectangle.getEnd());

        // Move the grid lines and selection to the front
        selectionRectangle.toFront();
        gridLines.toFront();
    }

    // Handles the keyboard shortcuts
    public void keyPressedHandler(KeyEvent event) {
        event.consume();  // No one touches this but me

        // Shift + Enter to simulate in selection
        if (event.getCode().equals(KeyCode.ENTER) && event.isShiftDown()) {
            simInsideSelectionButton.fire();
        }
        // Ctrl + Enter to siulation outside selection
        else if (event.getCode().equals(KeyCode.ENTER) && event.isControlDown()) {
            simOutsideSelectionButton.fire();
        }
        // Enter to toggle simulation
        else if (event.getCode().equals(KeyCode.ENTER)) {
            startSimulationButton.fire();
        }
        // Space to step simulation
        else if (event.getCode().equals(KeyCode.SPACE)) {
            if (visualisationDone) {
                Action.addAction();
                updateCells();
            }
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
            Coordinate start = convertToScreen(simulator.getBounds().getValue0());
            Coordinate end = convertToScreen(simulator.getBounds().getValue1());

            selectionRectangle.select(start, end);

            selectionRectangle.setVisible(true);
            mode = Mode.SELECTING;
            
            // Move the grid lines and selection to the front
            selectionRectangle.toFront();
            gridLines.toFront();
        }
        // Ctrl + Z to undo
        else if (event.getCode().equals(KeyCode.Z) && event.isControlDown()) {
            Action.undo();
        }
        // Ctrl + Y to redo
        else if (event.getCode().equals(KeyCode.Y) && event.isControlDown()) {
            Action.redo();
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

    // Converts grid coordinates to screen coordinate
    public int convertToScreen(int x) {
        return x * CELL_SIZE;
    }

    public Coordinate convertToScreen(Coordinate coordinate) {
        return new Coordinate(coordinate.getX() * CELL_SIZE, coordinate.getY() * CELL_SIZE);
    }

    // Converts screen coordinates to grid coordinates
    public int convertToGrid(int x) {
        return x / CELL_SIZE;
    }

    public Coordinate convertToGrid(Coordinate coordinate) {
        return new Coordinate(coordinate.getX() / CELL_SIZE, coordinate.getY() / CELL_SIZE);
    }

    // Snaps the screen X or Y coordinates to the grid
    public int snapToGrid(int x) {
        return x / CELL_SIZE * CELL_SIZE;
    }

    public Coordinate snapToGrid(Coordinate coordinate) {
        return convertToGrid(convertToScreen(coordinate));
    }

    // Gets the simulator
    public Simulator getSimulator() {
        return simulator;
    }
}
