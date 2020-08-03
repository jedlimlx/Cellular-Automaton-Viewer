package sample.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import sample.controller.dialogs.RuleDialog;
import sample.controller.dialogs.RuleWidget;
import sample.model.Cell;
import sample.model.*;
import sample.model.rules.HROTGenerations;
import sample.model.rules.RuleFamily;
import sample.model.search.RuleSearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Scanner;
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
    private ImageView recordingImage;

    @FXML
    private ImageView playButtonImage;

    private boolean currentlySelecting = false;
    private Coordinate startSelection;  // The start of the selection
    private Coordinate endSelection;  // The end of the selection
    private Rectangle selectionRectangle;  // Rectangle that represents selection box

    private final RuleDialog dialog = new RuleDialog();  // Dialog to set rule

    private Mode mode;  // Mode (Drawing, Selecting, Panning)
    private Simulator simulator;  // Simulator to simulate rule
    private ArrayList<Button> stateButtons;  // Buttons to switch between states
    private HashMap<Coordinate, Cell> cellList;  // List of cell objects

    private final int CELL_SIZE = 2;  // Cell size
    private int currentState = 1;  // State to draw with
    private boolean simulationRunning = false;  // Is the simulation running?
    private boolean visualisationDone = false;  // Is the visualisation done?
    private boolean recording = false;  // Is the recording on?

    private Giffer giffer;  // For writing to a *.gif

    @FXML
    public void initialize() {
        // Initialise variables
        cellList = new HashMap<>();
        stateButtons = new ArrayList<>();
        mode = Mode.DRAWING;

        // Create simulator object
        simulator = new Simulator(new HROTGenerations("R2,C2,S6-9,B7-8,NM"));
        setCell(0, 0, 0);

        // Create selection rectangle and set properties
        selectionRectangle = new Rectangle();
        selectionRectangle.setOpacity(0.5);
        selectionRectangle.setFill(Color.rgb(150, 230, 255));
        selectionRectangle.toFront();

        // Disable scrollbars and scrolling
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Bind methods
        scrollPane.setOnScroll(this::changeZoomHandler);
        scrollPane.addEventFilter(ScrollEvent.ANY, this::changeZoomHandler);

        scrollPane.setOnKeyPressed(this::keyPressedHandler);
        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressedHandler);

        // Move the center of the ScrollPane
        scrollPane.setHvalue(0.5);
        scrollPane.setVvalue(0.5);

        // Add buttons to the secondary toolbar
        for (int i = 0; i < simulator.getRule().getNumStates(); i++) {
            int index = i;

            Button stateButton = new Button("" + index);
            stateButton.setOnAction((event -> {currentState = index; mode = Mode.DRAWING;}));
            stateButtons.add(stateButton);
            secondaryToolbar.getItems().add(stateButton);
        }

        // Start Simulation Thread
        Thread simulationThread = new Thread(this::runSimulation);
        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    @FXML // Handle the mouse dragged events
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

            // Remove from pane
            drawingPane.getChildren().remove(selectionRectangle);

            selectionRectangle.setX((int)event.getX() / CELL_SIZE * CELL_SIZE);
            selectionRectangle.setY((int)event.getY() / CELL_SIZE * CELL_SIZE);
            startSelection = new Coordinate((int)event.getX() / CELL_SIZE,
                    (int)event.getY() / CELL_SIZE);

            drawingPane.getChildren().add(selectionRectangle);
        }
    }

    @FXML
    public void mouseDragDoneHandler(MouseEvent event) {
        if (mode == Mode.SELECTING && currentlySelecting) {
            currentlySelecting = false;
            endSelection = new Coordinate((int)event.getX() / CELL_SIZE,
                    (int)event.getY() / CELL_SIZE);
        }
    }

    @FXML // Generates random soup in the selection box
    public void generateRandomSoup() {
        // Generate the souop
        Grid soup = SymmetryGenerator.generateSymmetry("C1", 50, new int[]{1},
                endSelection.getX() - startSelection.getX(),
                endSelection.getY() - startSelection.getY());
        
        // Insert the cells in the pane (automatically inserted in the simulator)
        insertCells(soup, startSelection.getX(), startSelection.getY());
    }

    @FXML // Flips selected cells horizontally
    public void flipHorizontalHandler() {
        simulator.reflectCellsX(startSelection, endSelection);  // Reflect cells in the grid
        renderCells(startSelection, endSelection);
    }

    @FXML // Flips selected cells vertically
    public void flipVerticalHandler() {
        simulator.reflectCellsY(startSelection, endSelection);  // Reflect cells in the grid
        renderCells(startSelection, endSelection);
    }

    @FXML // Rotates the selected cells clockwise
    public void rotateCWHandler() {
        simulator.rotateCW(startSelection, endSelection);  // Rotate the cells in the grid
        renderCells(startSelection, endSelection);
    }

    @FXML // Rotates the selected cells counter-clockwise
    public void rotateCCWHandler() {
        simulator.rotateCCW(startSelection, endSelection);  // Rotate the cells in the grid
        renderCells(startSelection, endSelection);
    }

    // Function to set cell at (x, y) to a certain state
    public void setCell(int x, int y, int state) {
        setCell(x, y, state, true);
    }

    public void setCell(int x, int y, int state, boolean updateSimulator) {
        // Bring selection rectangle to the front
        if (mode == Mode.SELECTING) {
            selectionRectangle.toFront();
        }

        // Get the cell object at the specified coordinate
        Cell prevCell = getCellObject(x, y);
        if (prevCell == null) {  // Insert a new cell if one doesn't already exist
            // Create cell
            Rectangle cell = new Rectangle();
            cell.setX(x);
            cell.setY(y);
            cell.setWidth(CELL_SIZE);
            cell.setHeight(CELL_SIZE);
            cell.setFill(simulator.getRule().getColor(state));

            // Add cell to pane and cell list
            drawingPane.getChildren().add(cell);
            addCellObject(x, y, new Cell(x, y, state, cell));
        }
        else if (prevCell.getState() != state) {  // Don't bother if the cell didn't change
            prevCell.getRectangle().setFill(simulator.getRule().getColor(state));
            prevCell.setState(state);
        }

        // Add cell to simulator
        simulator.setCell(x / CELL_SIZE, y / CELL_SIZE, state);
    }

    public void insertCells(Grid cellsToInsert, int x, int y) {
        Coordinate newCell;
        for (Coordinate coord: cellsToInsert) {
            newCell = coord.add(new Coordinate(x, y));
            setCell(newCell.getX() * CELL_SIZE, newCell.getY() * CELL_SIZE,
                    cellsToInsert.getCell(coord));
        }
    }

    // Renders all cells
    public void renderCells() {
        for (Coordinate coordinate: simulator) {
            setCell(coordinate.getX() * CELL_SIZE, coordinate.getY() * CELL_SIZE,
                    simulator.getCell(coordinate),false);
        }
    }

    // Renders cells between the start and end coordinate
    public void renderCells(Coordinate startSelection, Coordinate endSelection) {
        for (int i = startSelection.getX(); i < endSelection.getX(); i++) {
            for (int j = startSelection.getY(); j < endSelection.getY(); j++) {
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
        simulator.step();
        Platform.runLater(() -> {
            try {
                // Only update cells that changed (for speed)
                for (Coordinate cell: simulator.getCellsChanged()) {
                    setCell(cell.getX() * CELL_SIZE, cell.getY() * CELL_SIZE, simulator.getCell(cell));
                }
            }
            catch (ConcurrentModificationException exception) { // Catch an exception that probably won't happen
                System.out.println("ConcurrentModificationException caught in updateCells method!");
                simulationRunning = false;  // Pause the simulation
            }

            // Update the variable to say that the visualisation is done
            visualisationDone = true;

            // Update with new generation
            statusLabel.setText("Generation: " + simulator.getGeneration());
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
                updateCells();

                // Wait for the visualisation to be done
                // To avoid ConcurrentModificationException
                while (!visualisationDone) {
                    wait(1);
                }

                visualisationDone = false;
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

    @FXML // Starts the rule dialog
    public void startRuleDialog() {
        dialog.showAndWait();

        // Check if the user actually hit `Confirm Rule`
        if (dialog.getRule() != null) {
            simulator.setRule(dialog.getRule());
        }
    }

    @FXML // Starts the rule search dialog
    public void startRuleSearchDialog() {
        // TODO (Write a custom dialog)
        RuleFamily minRule, maxRule;

        RuleDialog minRuleDialog = new RuleDialog("Enter Min Rule");
        minRuleDialog.showAndWait();  // Start dialog to get min rule

        // Check if the user actually hit `Confirm Rule`
        if (minRuleDialog.getRule() != null) {
            minRule = minRuleDialog.getRule();
        }
        else {
            return;
        }

        RuleDialog maxRuleDialog = new RuleDialog("Enter Max Rule");
        maxRuleDialog.showAndWait();  // Start dialog to get the max rule

        // Check if the user actually hit `Confirm Rule`
        if (maxRuleDialog.getRule() != null) {
            maxRule = maxRuleDialog.getRule();
        }
        else {
            return;
        }

        try {
            RuleSearch ruleSearch = new RuleSearch(simulator.getCells(startSelection, endSelection),
                    minRule, maxRule);

            // Run in a seperate thread
            Thread thread = new Thread(() -> ruleSearch.search(1000000));
            thread.setDaemon(true);
            thread.start();
        }
        catch (IllegalArgumentException exception) {  // Ensure that the 2 rules have the same rulefamily
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText(exception.getMessage());
            alert.showAndWait();
        }
    }

    @FXML // Generates an APGTable
    public void generateAPGTable() {
        if (simulator.getRule() instanceof RuleFamily) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Apgtable");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                        "Apgtable Files (*.table)", "*.table"));
                File file = fileChooser.showSaveDialog(null);

                if (!((RuleFamily) simulator.getRule()).generateApgtable(file)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error in generating APGTable");
                    alert.setHeaderText("The operation was unsuccessful.");
                    alert.setContentText("The operation was unsuccessful. " +
                            "If you suspect a bug, please report it.");
                    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
                    alert.showAndWait();
                }
                else {
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
        simulator.insertCells(this.simulator.getCells(startSelection, endSelection),
                new Coordinate(0, 0));

        // Results of the identification
        sample.model.patterns.Pattern results = simulator.identify();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Identification results");

        if (results != null)
            alert.setHeaderText(results.toString());
        else
            alert.setHeaderText("Identification Failed :(");

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

            if (!giffer.toGIF(file)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error in generating *.gif");
                alert.setHeaderText("The operation was unsuccessful.");
                alert.setContentText("The operation was unsuccessful. " +
                        "If you suspect a bug, please report it.");
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
                alert.showAndWait();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Operation successful!");
                alert.setHeaderText("The operation was successful.");
                alert.setContentText("The operation was successful. " +
                        "The *.gif has been saved to " + file.getAbsolutePath() + ".");
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);  // Makes it scale to the text
                alert.showAndWait();
            }
        }
        else {
            // Change icon for recording
            recordingImage.setImage(new Image(getClass().getResourceAsStream(
                    "/icon/RecordIcon2.png")));
            giffer = new Giffer();  // Initialise the giffer object
        }

        recording = !recording;
    }

    @FXML // Closes the application
    public void closeApplication() {
        Platform.exit();
    }

    @FXML // Saves the pattern
    public void savePattern() {
        // Get the file to save the pattern in
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save *.rle file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "RLE Files (*.rle)", "*.rle"));
        File file = fileChooser.showSaveDialog(null);

        // Updating the bounds
        simulator.updateBounds();

        // Add header & comments
        String rle = simulator.toRLE(simulator.getBounds().getKey(), simulator.getBounds().getValue());
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

        try {
            // Writing to the file
            FileWriter writer = new FileWriter(file);
            writer.write(rleFinal.toString());
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
        for (Coordinate coordinate: simulator) {
            coordinates.add(coordinate);
        }

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
            for (RuleWidget widget: dialog.getRuleWidgets()) {
                for (String regex: widget.getRuleFamily().getRegex()) {
                    if (rulestring.matches(regex)) {
                        found = true;
                        break;
                    }
                }

                // Completely break out of the loop
                if (found) {
                    simulator.setRule(widget.getRuleFamily());
                    break;
                }
            }

            // Set the rulestring
            ((RuleFamily) simulator.getRule()).setRulestring(rulestring);

            // Generate the additional information from comments
            String[] commentsArray = new String[comments.size()];
            for (int i = 0; i < commentsArray.length; i++) {
                commentsArray[i] = comments.get(i);
            }

            ((RuleFamily) simulator.getRule()).loadComments(commentsArray);

            newPattern();  // Clear all cells
            simulator.fromRLE(rleFinal.toString(), new Coordinate(1024, 1024));  // Insert the new cells
            renderCells();  // Render the new cells
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
        // TODO (Make pasting nicer)
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

        simulator.fromRLE(rleFinal.toString(), startSelection);  // Insert the cells
        renderCells(startSelection, endSelection);  // Render the cells
    }

    @FXML // Copies the currently selected cells to the clipboard
    public void copyCells() {
        if (mode == Mode.SELECTING) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();

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

            content.putString(rleFinal.toString());
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
    }

    // Handles the key pressed event
    public void keyPressedHandler(KeyEvent event) {
        event.consume();  // Only this method applies

        // Enter to toggle simulation
        if (event.getCode().equals(KeyCode.ENTER)) {
            toggleSimulation();
        }
        // Space to step simulation
        else if (event.getCode().equals(KeyCode.SPACE)) {
            updateCells();
        }
        // Delete cells
        else if (event.getCode().equals(KeyCode.DELETE)) {
            deleteCells();
        }
        // Ctrl + C to copy
        else if (event.getCode().equals(KeyCode.C) && event.isControlDown()) { // TODO (Add headers)
            copyCells();
        }
        // Ctrl + V to paste
        else if (event.getCode().equals(KeyCode.V) && event.isControlDown()) {
            pasteRLE();
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
