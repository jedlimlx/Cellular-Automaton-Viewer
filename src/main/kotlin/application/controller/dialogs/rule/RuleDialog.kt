package application.controller.dialogs.rule

import application.controller.dialogs.rule.hrot.*
import application.controller.dialogs.rule.isotropic.*
import application.controller.dialogs.rule.misc.*
import application.controller.dialogs.rule.ruleloader.RuleLoaderDialog
import application.model.Coordinate
import application.model.rules.RuleFamily
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import java.lang.Boolean
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.Any
import kotlin.Exception
import kotlin.IllegalArgumentException
import kotlin.String
import kotlin.arrayOf

class RuleDialog @JvmOverloads constructor(promptText: String? = "Enter Rulestring") : Dialog<Any?>() {
    private var chosenRuleFamily: RuleFamily? = null

    // Controls
    private val grid: GridPane
    private val confirmRuleButton: Button
    private val rulestringField: TextField
    private val ruleFamilyCombobox: ComboBox<String>

    // Array to store rule widgets
    private val ruleWidgets = arrayOf(
        HROTDialog(), HROTHistoryDialog(),
        HROTSymbiosisDialog(), HROTDeadlyEnemiesDialog(), HROTGenerationsDialog(),
        HROTExtendedGenerationsDialog(), IntegerHROTDialog(), DeficientHROTDialog(),
        HROTRegeneratingGenerationsDialog(), HROTBSFKLDialog(), MultistateCyclicHROTDialog(),
        INTDialog(), INTHistoryDialog(), INTEnergeticDialog(), INTGenerationsDialog(),
        DeficientINTDialog(), EuclideanDialog(), OneDimensionalDialog(), TurmitesDialog(),
        MargolusDialog(), AlternatingDialog(), RuleLoaderDialog()
    )

    // Only return if user hits confirm rule
    var rule: RuleFamily?
        get() = if (super.getResult() === Boolean.TRUE) {
            chosenRuleFamily // Only return if user hits confirm rule
        } else null
        set(rule) {
            var found = false
            for (widget in ruleWidgets) {
                for (regex in widget.ruleFamily!!.regex) {
                    if (rule!!.rulestring.split(":").toTypedArray()[0].matches(Regex(regex))) {
                        found = true
                        break
                    }
                }

                // Completely break out of the loop
                if (found) {
                    rulestringField.text = rule!!.rulestring
                    widget.ruleFamily = rule
                    changeRuleFamily(widget) // Change to the widget whose regex matches

                    // Move the initial coordinate
                    if (rule.boundedGrid != null) {
                        rule.boundedGrid.initialCoordinate = Coordinate(1800, 1800)
                    }

                    break
                }
            }
        }

    init {
        // Set Titles
        super.setTitle("Set Rule")
        super.setResizable(true)

        // Create controls
        grid = GridPane()
        grid.alignment = Pos.CENTER
        grid.hgap = 5.0
        grid.vgap = 5.0

        // HBox to contain combobox and button
        val hBox = HBox()
        hBox.spacing = 5.0
        grid.add(hBox, 0, 0)

        // Combobox to Choose Rule Family
        ruleFamilyCombobox = ComboBox<String>()
        ruleFamilyCombobox.onAction = EventHandler {
            changeRuleFamily(
                ruleWidgets[ruleFamilyCombobox.items.indexOf(ruleFamilyCombobox.value)]
            )
        }
        hBox.children.add(ruleFamilyCombobox)

        // Textbox for rulestring
        rulestringField = TextField()
        rulestringField.promptText = promptText

        // Listen for changes to the text and update the combobox
        rulestringField.textProperty().addListener { _: ObservableValue<out String>, _: String, _: String -> rulestringFieldChanged() }
        rulestringField.onKeyPressed = EventHandler { event: KeyEvent ->   // Enter as a substitute for okay
            if (event.code == KeyCode.ENTER) {
                confirmRule()
            }
        }
        grid.add(rulestringField, 0, 2)

        // Button the confirm the rulestring
        confirmRuleButton = Button("Confirm Rule")
        confirmRuleButton.onAction = EventHandler { confirmRule() }
        grid.add(confirmRuleButton, 0, 3)

        // Get rule family names & configure the button correctly
        ruleFamilyCombobox.setValue(ruleWidgets[0].toString())
        for (widget in ruleWidgets) {
            ruleFamilyCombobox.items.add(widget.toString())
        }

        // Button to display information about the rulespace
        val infoButton = Button("?")
        infoButton.onAction = EventHandler { showRuleFamilyDescription() }
        hBox.children.add(infoButton)

        // Allows closing with close button
        val window = super.getDialogPane().scene.window
        window.onCloseRequest = EventHandler { window.hide() }

        // Add the rule widget to the grid
        grid.add(ruleWidgets[0], 0, 1)
        super.getDialogPane().content = grid
    }

    private fun confirmRule() {
        // Get currently selected rule widget
        val index = ruleFamilyCombobox.items.indexOf(ruleFamilyCombobox.value)
        val ruleWidget = ruleWidgets[index]

        // Update the rule in the rule widget
        try {
            ruleWidget.updateRule(rulestringField.text)
        } catch (exception: IllegalArgumentException) {
            LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).log(
                Level.WARNING, exception.message
            )

            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error!"
            alert.headerText = "This rulestring is invalid!"
            alert.contentText = """
                ${exception.message}
                If you suspect this is a bug, please report it!
                """.trimIndent()
            alert.showAndWait()
            return
        } catch (exception: Exception) {
            LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).log(
                Level.WARNING, exception.message
            )
            exception.printStackTrace()

            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error!"
            alert.headerText = "An error occured!"
            alert.contentText = """
                ${exception.message}
                If you suspect this is a bug, please report it!
                """.trimIndent()
            alert.showAndWait()
            return
        }

        chosenRuleFamily = ruleWidget.ruleFamily
        if (chosenRuleFamily!!.boundedGrid != null) {
            chosenRuleFamily!!.boundedGrid.initialCoordinate = Coordinate(1800, 1800)
        }

        // Canonise the rulestring in the text field
        rulestringField.text = chosenRuleFamily!!.rulestring

        // Close the dialog
        super.setResult(Boolean.TRUE)
        super.close()
    }

    private fun rulestringFieldChanged() {
        var found = false
        for (widget in ruleWidgets) {
            for (regex in widget.ruleFamily!!.regex) {
                if (rulestringField.text.split(":").toTypedArray()[0].matches(Regex(regex))) {
                    found = true
                    break
                }
            }

            // Completely break out of the loop
            if (found) {
                changeRuleFamily(widget) // Change to the widget who regex matches
                break
            }
        }
    }

    private fun changeRuleFamily(widget: RuleWidget) {
        // Get currently selected rule widget
        val index = ruleFamilyCombobox.items.indexOf(ruleFamilyCombobox.value)
        val ruleWidget = ruleWidgets[index]

        // Remove the current widget
        grid.children.remove(ruleWidget)

        // Add the new one
        grid.add(widget, 0, 1)

        // Set the value of the combobox
        ruleFamilyCombobox.value = widget.ruleFamily!!.name
    }

    private fun showRuleFamilyDescription() {
        // Get currently selected rule widget
        val index = ruleFamilyCombobox.items.indexOf(ruleFamilyCombobox.value)
        val ruleWidget = ruleWidgets[index]

        // Display description
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = ruleWidget.ruleFamily!!.name + " Description"
        alert.headerText = "Description of " + ruleWidget.ruleFamily!!.name
        alert.contentText = ruleWidget.ruleFamily!!.description
        alert.dialogPane.minHeight = Region.USE_PREF_SIZE // Makes it scale to the text
        alert.showAndWait()
    }
}