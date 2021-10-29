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
import java.util.function.ToIntFunction
import kotlin.system.exitProcess

@CommandLine.Command(name = "rr", aliases = ["rule_range"], description = ["Computes rule range an inputted pattern"])
class RuleRangeCommand : Runnable {
    private var simulator: Simulator? = null

    @CommandLine.Option(
        names = ["-i", "--input"], description = ["Input file containing pattern whose " +
                "rule range is to be computed"], required = true
    )
    private var inputFile: File? = null

    @CommandLine.Option(
        names = ["-m", "-g", "--generations"],
        defaultValue = "0",
        description = ["Number of generations to run the pattern (default: 0)"]
    )
    private var generations = 0

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private var help = false

    override fun run() {
        try {
            // Initialise the Simulator
            simulator = Simulator(HROT("B3/S23"))

            // Loading the pattern
            if (inputFile != null) {
                Utils.loadPattern(simulator, inputFile)
            } else {
                println("Input file must be specified!")
                exitProcess(-1)
            }

            val grids = arrayOfNulls<Grid>(generations + 1)
            for (i in 0 until generations + 1) {
                grids[i] = simulator!!.deepCopy()
                simulator!!.step()
            }

            // Checking if min / max rules are supported
            if (simulator!!.rule is MinMaxRuleable) {
                val minMaxRule = (simulator!!.rule as MinMaxRuleable).getMinMaxRule(grids)
                println("Min Rule: " + minMaxRule.value0)
                println("Max Rule: " + minMaxRule.value1)
            } else {
                System.err.println("Minimum, maximum rules are not supported by this rulespace!")
                exitProcess(-1)
            }
        } catch (exception: FileNotFoundException) {
            System.err.println("Input / Output file could not be found!")
            exitProcess(-1)
        }
        exitProcess(0)
    }
}