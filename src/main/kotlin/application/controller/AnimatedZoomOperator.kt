package application.controller

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.scene.Node
import javafx.util.Duration

class AnimatedZoomOperator {
    // Taken from https://stackoverflow.com/questions/29506156/javafx-8-zooming-relative-to-mouse-pointer
    private val timeline: Timeline = Timeline(60.0)
    fun zoom(node: Node, factor: Double, x: Double, y: Double) {
        // Determine scale
        val oldScale = node.scaleX
        val scale = oldScale * factor
        val f = scale / oldScale - 1

        // Determine offset that we will have to move the node
        val bounds = node.localToScene(node.boundsInLocal)
        val dx = x - (bounds.width / 2 + bounds.minX)
        val dy = y - (bounds.height / 2 + bounds.minY)

        // Timeline that scales and moves the node
        timeline.keyFrames.clear()
        timeline.keyFrames.addAll(
            KeyFrame(
                Duration.millis(200.0), KeyValue(
                    node.translateXProperty(),
                    node.translateX - f * dx
                )
            ),
            KeyFrame(
                Duration.millis(200.0), KeyValue(
                    node.translateYProperty(),
                    node.translateY - f * dy
                )
            ),
            KeyFrame(Duration.millis(200.0), KeyValue(node.scaleXProperty(), scale)),
            KeyFrame(Duration.millis(200.0), KeyValue(node.scaleYProperty(), scale))
        )
        timeline.play()
    }
}