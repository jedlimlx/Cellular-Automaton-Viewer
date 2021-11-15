package application.controller.dialogs.search

import application.model.rules.Rule
import application.model.search.SearchParameters
import application.model.search.csearch.BruteForceSearchParameters
import application.model.search.ocgar2.AgarSearchParameters
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory

class AgarSearchParametersDialog(rule: Rule) : SearchParametersDialog() {
    private val rule: Rule
    private val spinnerMaxPeriod: Spinner<Int>

    override var searchParameters: AgarSearchParameters? = null
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

        // Setting the rule to search
        this.rule = rule
    }

    override fun confirmParameters(): Boolean {
        super.confirmParameters()
        searchParameters = AgarSearchParameters(rule, spinnerMaxPeriod.value)
        return true
    }
}