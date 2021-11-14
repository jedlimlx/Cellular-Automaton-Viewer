package application.controller

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.SceneAntialiasing
import javafx.scene.control.ComboBox
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.testfx.api.*
import org.testfx.api.FxAssert.verifyThat
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.matcher.control.LabeledMatchers.*

class MainControllerTest: ApplicationTest() {
    // TODO (Add more GUI Unit Tests)
    // TODO (Headless Testing)

    private var controller: MainController? = null

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

    @Test
    fun testSelectionRectangle() {
        try {
            sleep(2000)
            clickOn("#selectionButton")
            moveTo("#scrollPane")

            // Drawing selection rectangle
            moveBy(-100.0, -100.0)
            press(MouseButton.PRIMARY)
            moveBy(200.0, 200.0)
            release(MouseButton.PRIMARY)

            // Simulate random soup
            clickOn("#randomSoupButton")
            clickOn("#startSimulationButton")
            sleep(5000)
        } catch (exception: Exception) {
            println("Test cannot run in headless mode. Skipping...")
        }
    }

    @Test
    fun testSetRule() {
        try {
            sleep(2000)

            val tests = mapOf(
                "B3/S23" to "HROT",
                "/2/3" to "HROT Generations",
                "B4a5/S" to "INT",
                "W110" to "One Dimensional"
            )
            tests.forEach {
                // Open the Rule Dialog
                clickOn(hasText("File"))
                clickOn(hasText("New Rule"))
                clickOn(".text-field")

                // Clear the text field
                press(KeyCode.CONTROL, KeyCode.A)
                release(KeyCode.CONTROL, KeyCode.A)
                press(KeyCode.BACK_SPACE)
                release(KeyCode.BACK_SPACE)

                // Test the automatic regex
                write(it.key)
                verifyThat(".combo-box") { node: ComboBox<String> -> node.value == it.value }

                clickOn(hasText("Confirm Rule"))
            }
        } catch (exception: Exception) {
            println("Test cannot run in headless mode. Skipping...")
        }
    }
}