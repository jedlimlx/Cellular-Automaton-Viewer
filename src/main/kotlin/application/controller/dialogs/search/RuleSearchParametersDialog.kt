package application.controller.dialogs.search

import application.controller.dialogs.rule.RuleDialog
import application.model.Coordinate
import application.model.rules.MinMaxRuleable
import application.model.rules.Rule
import application.model.search.SearchParameters
import application.model.search.ocgar2.AgarSearchParameters
import application.model.search.rulesrc.RuleSearchParameters
import application.model.simulation.Grid
import application.model.simulation.Simulator
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import org.controlsfx.control.SegmentedButton

class RuleSearchParametersDialog(targetPattern: Grid, rule: Rule) : SearchParametersDialog() {
    private val targetPattern: Grid

    private val spinnerMaxPeriod: Spinner<Int>
    private val spinnerMinPop: Spinner<Int>
    private val spinnerMaxPop: Spinner<Int>
    private val spinnerMaxX: Spinner<Int>
    private val spinnerMaxY: Spinner<Int>
    private val spinnerMatchGenerations: Spinner<Int>

    private val rule: Rule
    private val minRuleDialog: RuleDialog = RuleDialog("Enter Min Rule")
    private val maxRuleDialog: RuleDialog = RuleDialog("Enter Max Rule")

    private var manualControl = false

    override var searchParameters: RuleSearchParameters? = null
        private set

    init {
        // Label for the maximum period
        grid.add(Label("Max Period:"), 0, 2)

        // The maximum period for period detection
        val maxPeriodFactory: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(10, 20000, 70)
        spinnerMaxPeriod = Spinner()
        spinnerMaxPeriod.isEditable = true
        spinnerMaxPeriod.valueFactory = maxPeriodFactory
        grid.add(spinnerMaxPeriod, 0, 3)

        // The minimum and maximum population for period detection
        val populationFactory: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(0, 20000, 0)
        grid.add(Label("Min Population:"), 0, 4)
        spinnerMinPop = Spinner()
        spinnerMinPop.isEditable = true
        spinnerMinPop.valueFactory = populationFactory
        grid.add(spinnerMinPop, 0, 5)

        val populationFactory2: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(0, 20000, 100)
        grid.add(Label("Max Population:"), 0, 6)
        spinnerMaxPop = Spinner()
        spinnerMaxPop.isEditable = true
        spinnerMaxPop.valueFactory = populationFactory2
        grid.add(spinnerMaxPop, 0, 7)

        // Maximum bounding box for period detection
        val boundingBoxFactory: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(0, 20000, 40)
        grid.add(Label("Max Width:"), 0, 8)
        spinnerMaxX = Spinner()
        spinnerMaxX.isEditable = true
        spinnerMaxX.valueFactory = boundingBoxFactory
        grid.add(spinnerMaxX, 0, 9)
        grid.add(Label("Max Height:"), 0, 10)

        val boundingBoxFactory2: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(0, 20000, 40)
        spinnerMaxY = Spinner()
        spinnerMaxY.isEditable = true
        spinnerMaxY.valueFactory = boundingBoxFactory2
        grid.add(spinnerMaxY, 0, 11)
        grid.add(Separator(), 0, 12)

        // Either manual control or match x generations
        val manualControlButton = ToggleButton("Manual Control")
        val matchGenerationsButton = ToggleButton("Match X Generations")
        val segmentedButton = SegmentedButton()
        segmentedButton.buttons.addAll(manualControlButton, matchGenerationsButton)
        grid.add(segmentedButton, 0, 13)

        // Button to select min rule
        val buttonMinRule = Button("Set Minimum Rule")
        buttonMinRule.onAction = EventHandler { event: ActionEvent? -> minRuleDialog.showAndWait() }
        grid.add(buttonMinRule, 0, 14)

        // Button to select max rule
        val buttonMaxRule = Button("Set Maximum Rule")
        buttonMaxRule.onAction = EventHandler { event: ActionEvent? -> maxRuleDialog.showAndWait() }
        grid.add(buttonMaxRule, 0, 15)

        // Match X generations
        val matchGenerationsLabel = Label("Match X Generations:")
        grid.add(matchGenerationsLabel, 0, 16)

        val matchGenerationsFactory: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(0, 200, 10)
        spinnerMatchGenerations = Spinner()
        spinnerMatchGenerations.isEditable = true
        spinnerMatchGenerations.valueFactory = matchGenerationsFactory
        grid.add(spinnerMatchGenerations, 0, 17)

        matchGenerationsButton.onAction = EventHandler {
            buttonMinRule.isDisable = true
            buttonMaxRule.isDisable = true
            matchGenerationsLabel.isDisable = false
            spinnerMatchGenerations.isDisable = false
            manualControl = false
        }

        manualControlButton.onAction = EventHandler {
            buttonMinRule.isDisable = false
            buttonMaxRule.isDisable = false
            matchGenerationsLabel.isDisable = true
            spinnerMatchGenerations.isDisable = true
            manualControl = true
        }

        // Setting the target pattern
        this.rule = rule
        this.targetPattern = targetPattern
    }

    override fun confirmParameters(): Boolean {
        super.confirmParameters()

        try {
            if (manualControl) {
                // Checking if the rulespace supports min / max rules
                require(minRuleDialog.rule is MinMaxRuleable)

                // Checking if the min and max rules are valid
                require((minRuleDialog.rule as MinMaxRuleable?)!!.validMinMax(minRuleDialog.rule, maxRuleDialog.rule))
                searchParameters = RuleSearchParameters(
                    targetPattern,
                    minRuleDialog.rule!!, maxRuleDialog.rule!!, spinnerMaxPeriod.value,
                    spinnerMinPop.value, spinnerMaxPop.value, spinnerMaxX.value,
                    spinnerMaxY.value
                )
            } else {
                // Checking if the rulespace supports min / max rules
                require(rule is MinMaxRuleable)

                // Evolve target pattern for x generations
                val simulator = Simulator(rule)
                simulator.insertCells(targetPattern, Coordinate())

                val grids = arrayOfNulls<Grid>(spinnerMatchGenerations.value)
                for (i in 0 until spinnerMatchGenerations.value) {
                    grids[i] = simulator.deepCopy()
                    grids[i]!!.background = rule.convertState(0, simulator.generation)
                    simulator.step()
                }

                val minMaxRule = (rule as MinMaxRuleable).getMinMaxRule(grids)
                searchParameters = RuleSearchParameters(
                    targetPattern,
                    minMaxRule.value0, minMaxRule.value1, spinnerMaxPeriod.value,
                    spinnerMinPop.value, spinnerMaxPop.value, spinnerMaxX.value,
                    spinnerMaxY.value
                )
            }

            return true
        } catch (exception: NullPointerException) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error!"
            alert.headerText = "The min / max rule is not specified!"
            alert.contentText = "The min / max rule is not specified!"
            alert.showAndWait()
        } catch (exception: UnsupportedOperationException) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error!"
            alert.headerText = "Minimum and maximum rules are not supported by this rule family."
            alert.contentText = "The minimum and maximum rules are not supported by this rule family. " +
                    "As a result, you cannot run rule search on rules in this rule family. " +
                    "If you need this feature, please request for it."
            alert.showAndWait()
        } catch (exception: IllegalArgumentException) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error!"
            alert.headerText = "The min / max rule is invalid!"
            alert.contentText = "The min / max rule is invalid!"
            alert.showAndWait()
        }

        return false
    }
}