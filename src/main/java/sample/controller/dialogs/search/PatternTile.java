package sample.controller.dialogs.search;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import sample.model.simulation.Grid;

import java.util.Map;

public class PatternTile extends VBox {
    private Grid pattern;

    public PatternTile(Grid pattern, String title, Map<String, String> additionalInfo) {
        pattern.updateBounds();

        Label titleLabel = new Label(title);
        titleLabel.setFont(new Font(20));
        super.getChildren().add(titleLabel);

        Canvas canvas = new Canvas(pattern.getBounds().getValue1().getX() - pattern.getBounds().getValue0().getX(),
                pattern.getBounds().getValue1().getY() - pattern.getBounds().getValue0().getY());
        GraphicsContext gc = canvas.getGraphicsContext2D();

        pattern.iterateCells(cell -> gc.rect(cell.getX() - pattern.getBounds().getValue0().getX(),
                cell.getY() - pattern.getBounds().getValue0().getY(), 1, 1));

        super.getChildren().add(canvas);

        for (Map.Entry<String, String> entry: additionalInfo.entrySet()) {
            super.getChildren().add(new Label(entry.getKey() + ": " + entry.getValue()));
        }

        this.pattern = pattern;
    }

    public Grid getPattern() {
        return pattern;
    }
}
