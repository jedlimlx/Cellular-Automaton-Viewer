package application.model.search.ocgar2

import application.model.SymmetryGenerator
import application.model.patterns.Oscillator
import application.model.patterns.Pattern
import application.model.patterns.Spaceship
import application.model.search.SearchProgram
import application.model.simulation.Simulator
import java.io.File

/**
 * Implements CAViewer's agar search program - OCAgar2
 */
class AgarSearch(parameters: AgarSearchParameters) : SearchProgram(parameters) {
    private var known: HashSet<ArrayList<ArrayList<Int>>> = hashSetOf()
    override fun search(num: Int) {
        var result: Pattern
        var simulator: Simulator
        val searchParameters = searchParameters as AgarSearchParameters

        known = HashSet() // Hash set to store known things
        searchResults = ArrayList() // Initialise search results
        var startTime = System.currentTimeMillis()

        for (i in 0 until num) {
            simulator = Simulator(searchParameters.rule)
            simulator.insertCells(
                SymmetryGenerator.generateC1(
                    50, intArrayOf(1),
                    searchParameters.rule.boundedGrid.width,
                    searchParameters.rule.boundedGrid.height
                ),
                searchParameters.rule.boundedGrid.initialCoordinate
            )

            result = simulator.identify(searchParameters.maxPeriod)
            if (result is Spaceship) {
                if (!known.contains(result.populationSequence)) {
                    add(searchResults, result)
                    add(known, result.populationSequence)
                }
            } else if (result is Oscillator) {
                if (!known.contains(result.populationSequence)) {
                    add(searchResults, result)
                    add(known, result.populationSequence)
                }
            }

            synchronized(this) {
                // To avoid race conditions
                if (numSearched % 5000 == 0 && numSearched != 0) {
                    println(
                        "$numSearched torus searched (" + 5000000 / (System.currentTimeMillis() - startTime) +
                                " rules/s), " + searchResults.size + " objects found!"
                    )
                    startTime = System.currentTimeMillis()
                }
                numSearched++
            }
        }
    }

    override fun writeToFile(file: File): Boolean {
        return false
    }
}