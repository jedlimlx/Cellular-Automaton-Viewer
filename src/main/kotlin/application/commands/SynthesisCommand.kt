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
    name = "synth", aliases = ["synthesis"], description = ["Generates random configurations " +
            "of spaceships to be piped into apgsearch"]
)
class SynthesisCommand : Runnable {
    @CommandLine.Option(
        names = ["-n", "--num"], description = ["The number of ships to generate " +
                "(default: 25)"], defaultValue = "20"
    )
    private val num = 0

    @CommandLine.Option(
        names = ["-x", "--width"],
        description = ["The width of the area where ships are generated (default: 50)"],
        defaultValue = "50"
    )
    private val x = 0

    @CommandLine.Option(
        names = ["-y", "--height"],
        description = ["The height of the area where ships are generated (default: 50)"],
        defaultValue = "50"
    )
    private val y = 0

    @CommandLine.Option(
        names = ["-s", "--ships"],
        description = ["The ships to include in the random configuration"],
        required = true
    )
    private val ships: Array<File> = arrayOf()

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private val help = false

    override fun run() {
        try {
            val spaceships = arrayOfNulls<Simulator>(ships.size)
            for (i in spaceships.indices) {
                spaceships[i] = Simulator(HROT("B3/S23"))
                Utils.loadPattern(spaceships[i], ships[i])
            }

            while (true) {
                println("x = 0, y = 0, rule = B3/S23")
                println(SymmetryGenerator.generateSynth(spaceships, num, x, y).toRLE())
            }
        } catch (exception: FileNotFoundException) {
            System.err.println("The file could not be found!")
            exitProcess(-1)
        }

        exitProcess(0)
    }
}