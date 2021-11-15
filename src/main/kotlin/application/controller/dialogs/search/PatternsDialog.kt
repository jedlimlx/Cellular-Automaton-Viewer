package application.controller.dialogs.search

import application.model.patterns.Pattern
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.Dialog
import javafx.scene.control.ScrollPane
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.TilePane
import javafx.stage.Modality
import javafx.stage.WindowEvent

class PatternsDialog(patternsList: List<Pattern>, additionalInfo: List<Map<String, String>>, menu: ContextMenu) :
    Dialog<Any?>() {
    companion object {
        @JvmField
        var selected: PatternTile? = null
    }

    init {
        super.setResizable(true)
        super.initModality(Modality.NONE)

        val pane = TilePane()
        pane.hgap = 5.0
        pane.vgap = 5.0
        pane.prefWidthProperty().bind(super.widthProperty())

        for (i in patternsList.indices) {
            val patternTile = PatternTile(patternsList[i], patternsList[i].toString(), additionalInfo[i])
            patternTile.onContextMenuRequested = EventHandler { event: ContextMenuEvent ->
                menu.show(patternTile, event.screenX, event.screenY)
                selected = patternTile
            }
            pane.children.add(patternTile)
        }

        val scrollPane = ScrollPane(pane)
        scrollPane.style = "-fx-background-color: transparent;" // No border
        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        scrollPane.prefWidthProperty().bind(super.widthProperty())
        scrollPane.prefHeightProperty().bind(super.heightProperty())

        super.setWidth(300.0)
        super.setHeight(300.0)
        super.getDialogPane().content = scrollPane

        // Allows closing with close button
        val window = super.getDialogPane().scene.window
        window.onCloseRequest = EventHandler { event: WindowEvent? -> window.hide() }
    }
}