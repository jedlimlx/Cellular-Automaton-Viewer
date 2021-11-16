package application.model.search.rulesrc

import application.model.Coordinate
import application.model.Utils
import application.model.database.GliderDBEntry
import application.model.patterns.Oscillator
import application.model.patterns.Pattern
import application.model.patterns.Spaceship
import application.model.rules.MinMaxRuleable
import application.model.rules.Rule
import application.model.rules.RuleFamily
import application.model.search.SearchProgram
import application.model.simulation.Grid
import application.model.simulation.Simulator
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.math.abs

/**
 * Implements CAViewer's rule search program - rulesrc
 */
class RuleSearch(parameters: RuleSearchParameters) : SearchProgram(parameters) {
    private var known: HashSet<Pattern> = hashSetOf()

    /**
     * Searches numRules for a spaceship / oscillator that matches the target pattern
     * @param numRules The number of rules to search
     * @throws IllegalArgumentException Thrown if the search parameters are invalid
     */
    @Throws(IllegalArgumentException::class)
    override fun search(numRules: Int) {
        // TODO (Do whatever WildMyron says that searchPatt-matchPatt does with the min / max rule)
        var simulator: Simulator
        val searchParameters = searchParameters as RuleSearchParameters

        // Checking for valid minimum, maximum rules
        require(searchParameters.minRule is MinMaxRuleable) { "This rule family does not support min / max rules!" }

        known = HashSet() // Hash set to store known things
        searchResults = ArrayList() // Initialise search results
        var startTime = System.currentTimeMillis()
        for (i in 0 until numRules) {
            // Check if the search should stop
            if (stop) break

            // Create a new simulator object each time
            simulator = Simulator(searchParameters.minRule.clone() as Rule)
            simulator.insertCells(searchParameters.targetPattern, Coordinate(0, 0))

            // Randomise the rule
            if (simulator.rule is MinMaxRuleable) {
                (simulator.rule as MinMaxRuleable).randomise(searchParameters.minRule, searchParameters.maxRule)
            }

            // Identify the object
            val result = simulator.identify(searchParameters.maxPeriod) { grid: Grid ->
                grid.updateBounds()
                grid.population < searchParameters.maxPop &&
                        grid.population > searchParameters.minPop &&
                        grid.bounds.value1.x - grid.bounds.value0.x < searchParameters.maxX &&
                        grid.bounds.value1.y - grid.bounds.value0.y < searchParameters.maxY
            }

            if (result != null && result.toString() != "Still Life" && !known.contains(result)) {
                add(searchResults, result)
                add(known, result) // To avoid duplicate speeds & whatnot

                if (result is Spaceship) {
                    // Report oblique spaceships
                    if (abs(result.displacementX) != abs(result.displacementY) &&
                        result.displacementX != 0 && result.displacementY != 0) {
                        println()
                        println("Found oblique $result ship!")
                        println(Utils.fullRLE(result))
                        println()
                    } else if (result.period > 100) {
                        println()
                        println("Found high period $result ship!")
                        println(Utils.fullRLE(result))
                        println()
                    }
                }
            }

            synchronized(this) {
                // To avoid race conditions
                if (numSearched % 5000 == 0 && numSearched != 0) {
                    println(
                        "$numSearched rules searched (" + 5000000 / (System.currentTimeMillis() - startTime) +
                                " rules/s), " + searchResults.size + " objects found!"
                    )
                    startTime = System.currentTimeMillis()
                }

                numSearched++
            }
        }
    }

    override fun writeToFile(file: File): Boolean {
        return try {
            val writer = FileWriter(file)
            val writer2 = FileWriter(file.parent + "/ships.db.txt")
            val writer3 = FileWriter(file.parent + "/osc.db.txt")

            // Writing the search parameters
            val searchParameters = searchParameters as RuleSearchParameters
            writer.write("# Running search with ${searchParameters.targetPattern.toRLE()}\n")
            writer.write("# Max Period: ${searchParameters.maxPeriod}\n")
            writer.write("# Min Rule: ${searchParameters.minRule}")
            writer.write("# Max Rule: ${searchParameters.maxRule}")
            writer.write("Pattern,Rule,Min Rule,Max Rule\n")
            for (grid in searchResults) {   // Writing each pattern into the file
                val pattern = grid as Pattern
                writer.write("\"$pattern\",\"${(pattern.rule as RuleFamily).rulestring}\"," +
                        "\"${pattern.minRule.rulestring}\",\"${pattern.maxRule.rulestring}\"\n")
                if (pattern is Spaceship) {  // Writing to the *.db.txt files
                    writer2.write("${GliderDBEntry(pattern, "", "")}\n")
                } else if (pattern is Oscillator) {
                    writer3.write("${GliderDBEntry(pattern, "", "")}\n")
                }
            }

            // Close the file
            writer.close()
            writer2.close()
            writer3.close()
            true
        } catch (exception: IOException) {
            LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, exception.message)
            false
        }
    }
}