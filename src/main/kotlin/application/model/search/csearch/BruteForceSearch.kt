package application.model.search.csearch

import application.model.search.SearchProgram
import java.util.HashSet
import java.util.concurrent.Executors
import application.model.simulation.Simulator
import application.model.SymmetryGenerator
import application.model.Coordinate
import application.model.patterns.Pattern
import java.math.BigInteger
import java.io.FileWriter
import java.io.IOException
import java.util.logging.LogManager
import java.io.File
import java.util.ArrayList
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.pow

/**
 * CAViewer's brute force search program - csearch.
 *
 * TODO (Object Separation)
 */
class BruteForceSearch(parameters: BruteForceSearchParameters): SearchProgram(parameters) {
    private var known: HashSet<Pattern> = hashSetOf()
    private var tried: HashSet<Int> = hashSetOf()
    override fun search(num: Int) {
        searchThreaded(num, 1)
    }

    override fun searchThreaded(num: Int, numThreads: Int) {
        // Multi-threading
        executor = Executors.newFixedThreadPool(numThreads)

        // Split into search space into shards for distributed search
        for (t in 0 until numThreads) {
            executor.submit {
                var simulator: Simulator
                val searchParameters = searchParameters as BruteForceSearchParameters

                known = HashSet() // Hash set to store known things
                tried = HashSet() // Hash set to prevent re-attempting rotations and reflections of a soup
                searchResults = ArrayList() // Initialise search results

                var startTime = System.currentTimeMillis()
                val total = if (searchParameters.isRandom) num.toLong()
                else 2.0.pow((searchParameters.xBound * searchParameters.yBound).toDouble()).toLong()

                // There are n ^ (x * y) possible soups to check
                for (i in t * (total / numThreads) until (t + 1) * (total / numThreads)) {
                    // Check if the search should stop
                    if (stop) break

                    // Create a new simulator object each time
                    simulator = Simulator(searchParameters.rule)

                    // Converting to base n to get the soup
                    if (searchParameters.isRandom) {
                        simulator.insertCells(
                            SymmetryGenerator.generateSymmetry(
                                searchParameters.symmetry,
                                searchParameters.density, searchParameters.statesToInclude,
                                searchParameters.xBound, searchParameters.yBound
                            ), Coordinate()
                        )
                    } else {
                        var x = 0
                        var y = 0
                        var charString: String
                        val soup = BigInteger.valueOf(i).toString(2)
                        for (j in soup.indices) {
                            charString = soup[j].toString() + ""
                            if (charString.matches(Regex("\\d"))) simulator.setCell(x, y, charString.toInt())
                            else simulator.setCell(x, y, soup[j].code - 65 + 10)

                            if (x == searchParameters.xBound) {
                                y++
                                x = 0
                            } else {
                                x++
                            }
                        }

                        // Quit if soup has already been tried
                        if (tried.contains(simulator.hashCode())) continue

                        // Adding rotations to tried
                        simulator.updateBounds()
                        tried.add(simulator.hashCode())

                        simulator.rotateCW(simulator.bounds.value0, simulator.bounds.value1)
                        tried.add(simulator.hashCode())

                        simulator.rotateCW(simulator.bounds.value0, simulator.bounds.value1)
                        tried.add(simulator.hashCode())

                        simulator.rotateCW(simulator.bounds.value0, simulator.bounds.value1)
                        tried.add(simulator.hashCode())
                    }

                    // Identify the object
                    val result = simulator.identify(searchParameters.maxPeriod)
                    if (result != null && result.toString() != "Still Life" && !known.contains(result)) {
                        add(searchResults, result)
                        add(known, result) // To avoid duplicate speeds & whatnot
                    }

                    if (numSearched % 5000 == 0) {
                        println(
                            "$numSearched soups searched (" + 5000000 / (System.currentTimeMillis() - startTime) +
                                    " soup/s), " + searchResults.size + " objects found!"
                        )
                        startTime = System.currentTimeMillis()
                    }

                    synchronized(this) {  // To avoid race conditions
                        numSearched++
                    }
                }

                println("Completed shard $t of the search space!")
            }
        }
    }

    override fun writeToFile(file: File): Boolean {
        return try {
            val writer = FileWriter(file)

            // Writing the search parameters
            val searchParameters = searchParameters as BruteForceSearchParameters
            writer.write("# Rule: ${searchParameters.rule}\n")
            writer.write("# Max Period: ${searchParameters.maxPeriod}\n")
            writer.write("Pattern,RLE\n")
            for (grid in searchResults) {   // Writing each pattern into the file
                writer.write("\"$grid\",${grid.toRLE()}\n")
            }

            // Close the file
            writer.close()
            true
        } catch (exception: IOException) {
            LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, exception.message)
            false
        }
    }
}