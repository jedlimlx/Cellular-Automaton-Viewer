package application.model.search.catsrc

import application.model.Coordinate
import application.model.patterns.Catalyst
import application.model.search.SearchProgram
import application.model.simulation.Grid
import application.model.simulation.Simulator
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

internal class PlacedCatalyst(
    val catalyst: Grid, val hash: Int, val startCoordinate: Coordinate?, val startCoordinate2: Coordinate?,
    val coordinateList: List<Coordinate>
) {
    private var interacted = false
    private var regenerated = false
    fun hasInteracted(): Boolean {
        return interacted
    }

    fun hasRegenerated(): Boolean {
        return regenerated
    }

    fun setInteracted(interacted: Boolean) {
        this.interacted = interacted
    }

    fun setRegenerated(regenerated: Boolean) {
        this.regenerated = regenerated
    }
}

class CatalystSearch(parameters: CatalystSearchParameters) : SearchProgram(parameters) {
    private var known: HashSet<Catalyst> = hashSetOf()
    private val random = Random()

    override fun search(num: Int) {
        var simulator: Simulator
        val searchParameters = searchParameters as CatalystSearchParameters
        if (searchParameters.bruteForce) {
            TODO("Brute force")
        } else {
            known = HashSet()
            searchResults = ArrayList() // Initialise search results

            var startTime = System.currentTimeMillis()
            var initialGeneration: Int
            var repeatTime = -1
            var hash: Int
            var numRegen: Int
            var numInteracted: Int
            var usedCatalysts: MutableList<PlacedCatalyst>
            var placedCatalysts: List<PlacedCatalyst>?
            for (i in 0 until num) {
                // Check if the search should stop
                if (stop) break
                simulator = Simulator(searchParameters.rule)
                initialGeneration = -1
                usedCatalysts = ArrayList()

                placedCatalysts = randomAddCatalyst(simulator, searchParameters)
                if (placedCatalysts == null) continue  // The catalysts overlap and are not still lives

                // Inserting the target
                simulator.insertCells(searchParameters.target, Coordinate())
                for (j in 0 until searchParameters.maxRepeatTime) {
                    simulator.step()
                    numRegen = 0
                    numInteracted = 0
                    for (catalyst in placedCatalysts) {
                        hash = simulator.hashCode(catalyst.coordinateList, catalyst.startCoordinate)
                        if (hash != catalyst.hash && !catalyst.hasInteracted()) {
                            catalyst.setInteracted(true)
                            if (initialGeneration == -1) initialGeneration = simulator.generation
                        } else if (hash == catalyst.hash && catalyst.hasInteracted() && !catalyst.hasRegenerated()) {
                            usedCatalysts.add(catalyst)
                            catalyst.setRegenerated(true)
                            repeatTime = simulator.generation - initialGeneration
                        }

                        // To consider a catalyst valid,
                        // 1. At least one of the sub-catalysts must have been interacted with
                        // 2. All interacted catalysts must have been regenerated
                        if (catalyst.hasInteracted()) numInteracted++
                        if (catalyst.hasInteracted() && catalyst.hasRegenerated()) numRegen++
                    }

                    // Every single catalyst regenerated
                    if (numRegen == numInteracted && numInteracted >= 1) {
                        val original = Grid()
                        original.insertCells(searchParameters.target, Coordinate())
                        for (catalyst in usedCatalysts) original.insertCells(
                            catalyst.catalyst,
                            catalyst.startCoordinate2
                        )

                        val catalyst = Catalyst(simulator.rule, original, repeatTime)
                        if (!known.contains(catalyst)) {
                            add(searchResults, catalyst)
                            add(known, catalyst)
                        }
                        break
                    }
                }

                synchronized(this) {
                    // To avoid race conditions
                    if (numSearched % 5000 == 0 && numSearched != 0) {
                        println(
                            "$numSearched potential catalysts searched (" + 5000000 / (System.currentTimeMillis() - startTime) +
                                    " potential catalysts/s), " + searchResults.size + " catalysts found!"
                        )
                        startTime = System.currentTimeMillis()
                    }
                    numSearched++
                }
            }
        }
    }

    override fun writeToFile(file: File): Boolean {
        return try {
            val fileWriter = FileWriter(file)
            fileWriter.write("# Running search in ${(searchParameters as CatalystSearchParameters).rule}\n")
            fileWriter.write("Catalyst,RLE\n")
            for (i in searchResults.indices)
                fileWriter.write("${searchResults[i]},${searchResults[i].toRLE()}\n")
            fileWriter.close()
            true
        } catch (exception: IOException) {
            LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, exception.message)
            false
        }
    }

    private fun randomAddCatalyst(grid: Simulator, searchParameters: CatalystSearchParameters): List<PlacedCatalyst>? {
        var index: Int
        var catalyst: Grid
        var coordinate: Coordinate?
        val placedCatalysts = ArrayList<PlacedCatalyst>()
        for (i in 0 until searchParameters.numCatalysts) {
            index = random.nextInt(searchParameters.coordinateList.size)
            coordinate = searchParameters.coordinateList[index]

            index = random.nextInt(searchParameters.catalysts.size)
            catalyst = searchParameters.catalysts[index].deepCopy()

            if (searchParameters.rotateCatalyst) {
                catalyst.updateBounds()
                for (j in 0 until random.nextInt(4)) catalyst.rotateCW(catalyst.bounds.value0, catalyst.bounds.value1)
            }

            if (searchParameters.flipCatalyst) {
                catalyst.updateBounds()

                when (random.nextInt(4)) {
                    0 -> catalyst.reflectCellsX(catalyst.bounds.value0, catalyst.bounds.value1)
                    1 -> catalyst.reflectCellsY(catalyst.bounds.value0, catalyst.bounds.value1)
                    2 -> {
                        catalyst.reflectCellsX(catalyst.bounds.value0, catalyst.bounds.value1)
                        catalyst.reflectCellsY(catalyst.bounds.value0, catalyst.bounds.value1)
                    }
                }
            }

            grid.insertCells(catalyst, coordinate)

            val originalCoordinate = coordinate
            val bfsResult = catalyst.bfs(1, searchParameters.rule.neighbourhood)
            var coordinate3 = Coordinate()
            for (coordinate2 in bfsResult) {
                if (coordinate3.x > coordinate2.x) coordinate3 = Coordinate(coordinate2.x, coordinate3.y)
                if (coordinate3.y > coordinate2.y) coordinate3 = Coordinate(coordinate3.x, coordinate2.y)
            }

            val hash = catalyst.hashCode(bfsResult, coordinate3)
            for (j in bfsResult.indices) bfsResult[j] = bfsResult[j].add(coordinate)
            for (coordinate2 in bfsResult) {
                if (coordinate!!.x > coordinate2.x) coordinate = Coordinate(coordinate2.x, coordinate.y)
                if (coordinate.y > coordinate2.y) coordinate = Coordinate(coordinate.x, coordinate2.y)
            }
            placedCatalysts.add(PlacedCatalyst(catalyst, hash, coordinate, originalCoordinate, bfsResult))
        }

        // Ensuring the catalysts are stable
        val hash = grid.hashCode()
        grid.step()
        return if (hash != grid.hashCode()) null else placedCatalysts
    }
}