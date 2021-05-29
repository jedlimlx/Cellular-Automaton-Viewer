package application.commands

import java.lang.Runnable
import application.controller.MainController
import javafx.application.Application
import kotlin.Throws
import java.io.IOException
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.SceneAntialiasing
import javafx.scene.image.Image
import javafx.stage.Stage
import picocli.CommandLine

@CommandLine.Command(name = "GUI", aliases = [""], description = ["Starts the CAViewer GUI"])
class GUICommand : Application(), Runnable {
    private var controller: MainController? = null
    @Throws(IOException::class)
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/main.fxml"))
        val scene = Scene(fxmlLoader.load(), 700.0, 650.0, true, SceneAntialiasing.DISABLED)
        scene.stylesheets.add("/style.css")
        controller = fxmlLoader.getController()

        stage.scene = scene
        stage.title = "Cellular Automaton Viewer"
        stage.icons.add(Image(javaClass.getResourceAsStream("/icon/PulsarIcon.png")))
        stage.show()
    }

    override fun stop() {
        controller!!.onApplicationClosed()
    }

    override fun run() {
        launch()
    }
}