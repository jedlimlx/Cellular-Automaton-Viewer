package application.commands

import java.lang.Runnable
import application.model.simulation.Simulator
import application.model.Coordinate
import java.lang.StringBuilder
import application.model.rules.hrot.HROT
import java.lang.IllegalArgumentException
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.Throws
import java.io.FileWriter
import application.model.rules.MinMaxRuleable
import application.model.rules.RuleFamily
import java.util.concurrent.atomic.AtomicInteger
import application.model.search.catsrc.CatalystSearchParameters
import application.model.search.catsrc.CatalystSearch
import java.lang.InterruptedException
import application.model.rules.ApgtableGeneratable
import java.lang.UnsupportedOperationException
import application.model.rules.ruleloader.RuleDirective
import application.model.rules.ruleloader.RuleLoader
import application.model.rules.ruleloader.RuleNameDirective
import application.model.rules.ruleloader.ColourDirective
import application.model.patterns.Spaceship
import application.model.database.GliderDBEntry
import application.model.patterns.Oscillator
import java.lang.ClassCastException
import application.model.database.GliderDBReader
import application.model.search.rulesrc.RuleSearchParameters
import application.model.search.rulesrc.RuleSearch
import application.model.database.SSSSSReader
import java.lang.IllegalStateException
import application.model.database.SOSSPReader
import application.model.SymmetryGenerator
import application.model.Utils
import application.model.simulation.Grid
import picocli.CommandLine
import java.io.File
import java.util.*
import java.util.function.ToIntFunction
import kotlin.system.exitProcess

@CommandLine.Command(
    name = "cat",
    aliases = ["catsrc"],
    description = ["Searches randomly generated configurations of still lives for catalysts"]
)
class CatalystSearchCommand : Runnable {
    @CommandLine.Option(names = ["-o", "--output"], description = ["Output file to save results"], required = true)
    private var outputFile: File? = null

    @CommandLine.Option(
        names = ["-rt", "--repeat_time"],
        defaultValue = "50",
        description = ["The maximum number of generations to run the pattern (default: 50)"]
    )
    private var maxRepeatTime = 0

    @CommandLine.Option(
        names = ["-n", "--number"],
        defaultValue = "10000",
        description = ["Number of catalysts to search for before the program terminates (default: 10000)"]
    )
    private var numSearch = 0

    @CommandLine.Option(
        names = ["-rot", "--rotate"],
        defaultValue = "true",
        description = ["Should the catalysts be rotated? (default: true)"]
    )
    private var rotateCatalysts = false

    @CommandLine.Option(
        names = ["-flip", "--flip"],
        defaultValue = "true",
        description = ["Should the catalysts be flipped? (default: true)"]
    )
    private var flipCatalysts = false

    @CommandLine.Option(
        names = ["-n_cat", "--num_cat"],
        defaultValue = "3",
        description = ["Number of catalysts to place in the search area (default: 3)"]
    )
    private var numCatalysts = 0

    @CommandLine.Option(
        names = ["-cat", "--catalysts"],
        description = ["Input file containing a list of catalysts (one per line) to use"],
        required = true
    )
    private var catalysts: File? = null

    @CommandLine.Option(
        names = ["-s", "--search"], description = ["Input file containing an RLE. The target is state 1 and above. " +
                "The search area is the maximum state found in the RLE."], required = true
    )
    private var searchArea: File? = null

    @CommandLine.Option(
        names = ["--time"],
        defaultValue = "30",
        description = ["Number of seconds between file writes (default: 30)"]
    )
    private var time = 0

    @CommandLine.Option(
        names = ["-t", "--threads"],
        defaultValue = "5",
        description = ["Number of threads (default: 5)"]
    )
    private var threads = 0

    @CommandLine.Option(
        names = ["-r", "--rule"],
        description = ["The rule to search for catalysts in"],
        required = true
    )
    private var rule: String? = null

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private var help = false

    override fun run() {
        try {
            // Reading the catalyst input file
            val scanner = Scanner(catalysts!!)
            val catalysts: MutableList<Grid> = ArrayList()
            while (scanner.hasNextLine()) {
                val catalyst = Grid()
                catalyst.fromRLE(scanner.nextLine(), Coordinate())
                catalysts.add(catalyst)
            }
            scanner.close()

            // Reading target and search area input file
            val simulator = Simulator(HROT("B3/S23"))
            Utils.loadPattern(simulator, searchArea)

            // Find max state
            val maxState = AtomicInteger(1)
            simulator.iterateCells { cell: Coordinate? ->
                maxState.set(
                    maxState.get().coerceAtLeast(simulator.getCell(cell))
                )
            }

            val target = Grid()
            val searchArea = ArrayList<Coordinate>()
            simulator.iterateCells { cell: Coordinate ->
                if (simulator.getCell(cell) == maxState.get()) searchArea.add(
                    cell
                ) else if (simulator.getCell(cell) != 0) target.setCell(cell, simulator.getCell(cell))
            }

            val searchParameters = CatalystSearchParameters(
                maxRepeatTime,
                numCatalysts, false, rotateCatalysts,
                flipCatalysts, catalysts, target, searchArea, Utils.fromRulestring(rule)
            )

            val catalystSearch = CatalystSearch(searchParameters)
            catalystSearch.searchThreaded(numSearch, threads)

            while (catalystSearch.numSearched < numSearch) {
                if (!catalystSearch.writeToFile(outputFile!!)) {
                    System.err.println("Something went wrong while writing to the output file!")
                }

                Thread.sleep((time * 1000).toLong())
            }

            println("Search complete, " + catalystSearch.searchResults.size + " catalysts found!")
            exitProcess(0)
        } catch (exception: FileNotFoundException) {
            System.err.println("Input / Output file could not be found!")
            exitProcess(-1)
        } catch (exception: IllegalArgumentException) {
            System.err.println(exception.message)
            exitProcess(-1)
        } catch (ignored: InterruptedException) { }
    }
}