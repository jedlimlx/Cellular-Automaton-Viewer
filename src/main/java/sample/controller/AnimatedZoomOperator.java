package sample.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimatedZoomOperator {
    // Taken from https://stackoverflow.com/questions/29506156/javafx-8-zooming-relative-to-mouse-pointer
    private final Timeline timeline;

    public AnimatedZoomOperator() {
        this.timeline = new Timeline(60);
    }

    public void zoom(Node node, double factor, double x, double y) {
        // Determine scale
        double oldScale = node.getScaleX();
        double scale = oldScale * factor;
        double f = (scale / oldScale) - 1;

        // Determine offset that we will have to move the node
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        double dx = (x - (bounds.getWidth() / 2 + bounds.getMinX()));
        double dy = (y - (bounds.getHeight() / 2 + bounds.getMinY()));

        // Timeline that scales and moves the node
        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(),
                        node.getTranslateX() - f * dx)),
                new KeyFrame(Duration.millis(200), new KeyValue(node.translateYProperty(),
                        node.getTranslateY() - f * dy)),
                new KeyFrame(Duration.millis(200), new KeyValue(node.scaleXProperty(), scale)),
                new KeyFrame(Duration.millis(200), new KeyValue(node.scaleYProperty(), scale))
        );
        timeline.play();
    }
}