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

@CommandLine.Command(
    name = "entry",
    aliases = ["db_entry"],
    description = ["Generates an entry for the GliderDB database"]
)
class DBEntryCommand : Runnable {
    private var simulator: Simulator? = null

    @CommandLine.Option(
        names = ["-i", "--input"],
        description = ["Input file containing pattern to be simulated"],
        required = true
    )
    private var inputFile: File? = null

    override fun run() {
        try {
            // Initialise the Simulator
            simulator = Simulator(HROT("B3/S23"))
            Utils.loadPattern(simulator, inputFile)

            when (simulator!!.identify()) {
                is Spaceship -> println(GliderDBEntry(simulator!!.identify() as Spaceship, "", ""))
                is Oscillator -> println(GliderDBEntry(simulator!!.identify() as Oscillator, "", ""))
                else -> {
                    System.err.println("The object is not a spaceship / oscillator!")
                    exitProcess(-1)
                }
            }
        } catch (exception: FileNotFoundException) {
            System.err.println("Input file not found!")
            exitProcess(-1)
        } catch (exception: ClassCastException) {
            System.err.println("The object is not a spaceship / oscillator!")
            exitProcess(-1)
        }

        exitProcess(0)
    }
}