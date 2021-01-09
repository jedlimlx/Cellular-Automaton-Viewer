package sample.controller.dialogs.search;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Window;
import sample.model.patterns.Pattern;

import java.util.List;
import java.util.Map;

public class PatternsDialog extends Dialog {
    public static PatternTile selected;
    public PatternsDialog(List<Pattern> patternsList, List<Map<String, String>> additionalInfo, ContextMenu menu) {
        super();
        super.setResizable(true);
        super.initModality(Modality.NONE);

        TilePane pane = new TilePane();
        pane.setHgap(5);
        pane.setVgap(5);
        pane.prefWidthProperty().bind(super.widthProperty());

        for (int i = 0; i < patternsList.size(); i++) {
            PatternTile patternTile = new PatternTile(patternsList.get(i),
                    patternsList.get(i).toString(), additionalInfo.get(i));
            patternTile.setOnContextMenuRequested(event -> {
                menu.show(patternTile, event.getScreenX(), event.getScreenY());
                selected = patternTile;
            });
            pane.getChildren().add(patternTile);
        }


        ScrollPane scrollPane = new ScrollPane(pane);
        scrollPane.setStyle("-fx-background-color: transparent;");  // No border
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.prefWidthProperty().bind(super.widthProperty());
        scrollPane.prefHeightProperty().bind(super.heightProperty());

        super.setWidth(300);
        super.setHeight(300);
        super.getDialogPane().setContent(scrollPane);

        // Allows closing with close button
        Window window = super.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
    }
}
