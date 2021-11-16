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
import picocli.CommandLine
import java.io.File
import java.util.function.ToIntFunction
import kotlin.system.exitProcess

@CommandLine.Command(name = "rs", aliases = ["rulesrc"], description = ["Searches rules for oscillators / spaceships"])
class RuleSearchCommand : Runnable {
    @CommandLine.Option(names = ["-i", "--input"], description = ["Input file containing the seed"], required = true)
    private var inputFile: File? = null

    @CommandLine.Option(names = ["-o", "--output"], description = ["Output file to save results"], required = true)
    private var outputFile: File? = null

    @CommandLine.Option(
        names = ["-m", "-g", "--max_period"],
        defaultValue = "50",
        description = ["The maximum number of generations to run the pattern (default: 50)"]
    )
    private var maxPeriod = 0

    @CommandLine.Option(
        names = ["-min_pop"],
        defaultValue = "0",
        description = ["The minimum population of the pattern (default: 0)"]
    )
    private var minPop = 0

    @CommandLine.Option(
        names = ["-pop", "-max_pop"],
        defaultValue = "100",
        description = ["The maximum population of the pattern (default: 100)"]
    )
    private var maxPop = 0

    @CommandLine.Option(
        names = ["-x", "-width"],
        defaultValue = "40",
        description = ["The maximum width of the pattern's bounding box (default: 40)"]
    )
    private var maxBoundX = 0

    @CommandLine.Option(
        names = ["-y", "-max_y"],
        defaultValue = "40",
        description = ["The maximum height the pattern's bounding box (default: 40)"]
    )
    private var maxBoundY = 0

    @CommandLine.Option(
        names = ["-n", "--number"],
        defaultValue = "10000",
        description = ["Number of rules to search before the program terminates (default: 10000)"]
    )
    private var numSearch = 0

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
        names = ["-min", "--min_rule"],
        description = ["Minimum rule of the search space"],
        required = true
    )
    private var minRule: String? = null

    @CommandLine.Option(
        names = ["-max", "--max_rule"],
        description = ["Maximum rule of the search space"],
        required = true
    )
    private var maxRule: String? = null

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private var help = false

    override fun run() {
        try {
            val simulator = Simulator(HROT("B3/S23"))
            if (inputFile != null) {
                Utils.loadPattern(simulator, inputFile)
            } else {
                System.err.println("Input file must be specified!")
                exitProcess(-1)
            }

            if (outputFile == null) {
                System.err.println("Output file must be specified!")
                exitProcess(-1)
            }

            val parameters = RuleSearchParameters(
                simulator.deepCopy(),
                Utils.fromRulestring(minRule), Utils.fromRulestring(maxRule), maxPeriod, minPop, maxPop,
                maxBoundX, maxBoundY
            )

            val ruleSearch = RuleSearch(parameters)
            ruleSearch.searchThreaded(numSearch, threads)

            while (ruleSearch.numSearched < numSearch) {
                if (ruleSearch.searchResults != null) {
                    if (!ruleSearch.writeToFile(outputFile!!)) {
                        System.err.println("Something went wrong while writing to the output file!")
                    }
                }
                Thread.sleep((time * 1000).toLong())
            }

            println("Search Complete.")
        } catch (exception: FileNotFoundException) {
            System.err.println("Input / Output file could not be found!")
            exitProcess(-1)
        } catch (ignored: InterruptedException) { }
        exitProcess(0)
    }
}