package application.controller.dialogs.search

import application.controller.dialogs.RandomSoupDialog
import application.model.rules.Rule
import application.model.search.SearchParameters
import application.model.search.csearch.BruteForceSearchParameters
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory

class BruteForceSearchParametersDialog(rule: Rule) : SearchParametersDialog() {
    private val rule: Rule
    private val randomSoupDialog: RandomSoupDialog

    private val bruteForce: CheckBox
    private val spinnerMaxPeriod: Spinner<Int>

    private val spinnerBoundX: Spinner<Int>
    private val spinnerBoundY: Spinner<Int>

    override var searchParameters: BruteForceSearchParameters? = null
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

        // Bounding box for enumeration
        val boundingBoxFactory3: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(0, 20000, 5)
        grid.add(Label("Soup Width:"), 0, 4)
        spinnerBoundX = Spinner()
        spinnerBoundX.isEditable = true
        spinnerBoundX.valueFactory = boundingBoxFactory3
        grid.add(spinnerBoundX, 0, 5)
        grid.add(Label("Soup Height:"), 0, 6)

        val boundingBoxFactory4: SpinnerValueFactory<Int> = IntegerSpinnerValueFactory(0, 20000, 5)
        spinnerBoundY = Spinner()
        spinnerBoundY.isEditable = true
        spinnerBoundY.valueFactory = boundingBoxFactory4
        grid.add(spinnerBoundY, 0, 7)

        // Brute-force enumeration or random soup search
        bruteForce = CheckBox("Brute Force?")
        grid.add(bruteForce, 0, 8)

        // Random Soup Parameters
        randomSoupDialog = RandomSoupDialog(rule.numStates, 50, "C1", listOf(1))

        val randomSoupParametersButton = Button("Set Random Soup Parameters")
        randomSoupParametersButton.onAction = EventHandler { event: ActionEvent? -> randomSoupDialog.showAndWait() }
        grid.add(randomSoupParametersButton, 0, 9)

        // Setting the rule to search
        this.rule = rule
    }

    override fun confirmParameters(): Boolean {
        super.confirmParameters()
        try {
            searchParameters = BruteForceSearchParameters(
                rule, spinnerMaxPeriod.value,
                spinnerBoundX.value, spinnerBoundY.value, !bruteForce.isSelected,
                randomSoupDialog.symmetry, randomSoupDialog.states, randomSoupDialog.density
            )

            return true
        } catch (exception: IllegalArgumentException) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error!"
            alert.headerText = exception.message
            alert.contentText = exception.message
            alert.showAndWait()
        }

        return false
    }
}