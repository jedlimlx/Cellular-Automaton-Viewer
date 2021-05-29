package application.controller;

import application.model.Coordinate;
import application.model.rules.Rule;
import application.model.simulation.Grid;

import java.util.Stack;

/**
 * Represents an action that is taken by the user
 */
public class Action {
    private final Grid grid;
    private final Rule rule;

    private static MainController controller;
    private static final Stack<Action> undoStack = new Stack<>();
    private static final Stack<Action> redoStack = new Stack<>();

    private Action(Grid grid, Rule rule) {
        this.grid = grid;
        this.rule = rule;
    }

    public static void setController(MainController controller) {
        Action.controller = controller;
    }

    public static void addAction() {
        redoStack.clear();
        undoStack.add(new Action(controller.getSimulator().deepCopy(), controller.getSimulator().getRule()));
    }

    public static void undo() {
        // Ignore if stack is empty
        if (undoStack.size() == 0) return;

        // Pop from undo stack
        Action undoAction = undoStack.pop();

        // Adding to redo stack
        redoStack.add(undoAction);

        // Reverting to previous state
        controller.newPattern();
        controller.getSimulator().setRule(undoAction.rule);
        controller.getSimulator().insertCells(undoAction.grid, new Coordinate());

        controller.renderCells();
    }

    public static void redo() {
        // Ignore if stack is empty
        if (redoStack.size() == 0) return;

        // Pop from redo stack
        Action redoAction = redoStack.pop();
        controller.loadPattern(redoAction.grid.toRLE());
        controller.getSimulator().setRule(redoAction.rule);

        // Adding to undo stack
        undoStack.add(redoAction);

        // Reverting to previous state
        controller.newPattern();
        controller.getSimulator().setRule(redoAction.rule);
        controller.getSimulator().insertCells(redoAction.grid, new Coordinate());

        controller.renderCells();
    }
}
