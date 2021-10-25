package application.controller

import application.model.Coordinate
import application.model.rules.Rule
import application.model.simulation.Grid
import java.util.*

/**
 * Represents an action that is taken by the user
 */
class Action private constructor(private val grid: Grid, private val rule: Rule) {
    companion object {
        private lateinit var controller: MainController
        private val undoStack = Stack<Action>()
        private val redoStack = Stack<Action>()

        fun setController(controller: MainController) {
            Companion.controller = controller
        }

        fun addAction() {
            redoStack.clear()
            undoStack.add(Action(controller.simulator.deepCopy(), controller.simulator.rule))
        }

        fun undo() {
            // Ignore if stack is empty
            if (undoStack.size == 0) return

            // Pop from undo stack
            val undoAction = undoStack.pop()

            // Adding to redo stack
            redoStack.add(undoAction)

            // Reverting to previous state
            controller.newPattern()
            controller.simulator.rule = undoAction.rule
            controller.simulator.insertCells(undoAction.grid, Coordinate())
            controller.renderCells()
        }

        fun redo() {
            // Ignore if stack is empty
            if (redoStack.size == 0) return

            // Pop from redo stack
            val redoAction = redoStack.pop()
            controller.loadPattern(redoAction.grid.toRLE())
            controller.simulator.rule = redoAction.rule

            // Adding to undo stack
            undoStack.add(redoAction)

            // Reverting to previous state
            controller.newPattern()
            controller.simulator.rule = redoAction.rule
            controller.simulator.insertCells(redoAction.grid, Coordinate())
            controller.renderCells()
        }
    }
}