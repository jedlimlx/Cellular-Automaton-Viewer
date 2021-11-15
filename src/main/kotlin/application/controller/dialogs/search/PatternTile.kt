package application.controller.dialogs.search

import application.controller.dialogs.search.PatternTile
import javafx.stage.Modality
import javafx.scene.layout.TilePane
import javafx.scene.input.ContextMenuEvent
import application.controller.dialogs.search.PatternsDialog
import javafx.scene.layout.VBox
import javafx.scene.canvas.GraphicsContext
import application.model.Coordinate
import application.model.simulation.Grid
import javafx.scene.canvas.Canvas
import javafx.scene.control.Label
import javafx.scene.text.Font

class PatternTile(pattern: Grid, title: String?, additionalInfo: Map<String, String>) : VBox() {
    val pattern: Grid

    init {
        pattern.updateBounds()

        val titleLabel = Label(title)
        titleLabel.font = Font(20.0)
        super.getChildren().add(titleLabel)

        val canvas = Canvas(
            (pattern.bounds.value1.x - pattern.bounds.value0.x).toDouble(),
            (pattern.bounds.value1.y - pattern.bounds.value0.y).toDouble()
        )
        val gc = canvas.graphicsContext2D

        pattern.iterateCells { cell: Coordinate ->
            gc.rect(
                (cell.x - pattern.bounds.value0.x).toDouble(),
                (cell.y - pattern.bounds.value0.y).toDouble(), 1.0, 1.0
            )
        }

        super.getChildren().add(canvas)

        for ((key, value) in additionalInfo) {
            super.getChildren().add(Label("$key: $value"))
        }
        this.pattern = pattern
    }
}