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

@CommandLine.Command(name = "id", aliases = ["identify"], description = ["Identifies oscillators / spaceships"])
class IdentifyCommand : Runnable {
    private var simulator: Simulator? = null

    @CommandLine.Option(
        names = ["-i", "--input"],
        description = ["Input file containing pattern to be identified"],
        required = true
    )
    private val inputFile: File? = null

    @CommandLine.Option(
        names = ["-m", "-g", "--max_period"],
        defaultValue = "50",
        description = ["Number of generations to run the pattern"]
    )
    private val maxPeriod = 0

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private val help = false
    override fun run() {
        try {
            // Initialise the Simulator
            simulator = Simulator(HROT("B3/S23"))

            // Loading the pattern
            if (inputFile != null) {
                Utils.loadPattern(simulator, inputFile)
            } else {
                System.err.println("Input file must be specified!")
                exitProcess(-1)
            }

            val pattern = simulator!!.identify(maxPeriod)
            println(pattern)

            for (key in pattern.additionalInfo().keys)
                println("$key: + ${pattern.additionalInfo()[key]}")

        } catch (exception: FileNotFoundException) {
            System.err.println("Input / Output file could not be found!")
            exitProcess(-1)
        }

        exitProcess(0)
    }
}