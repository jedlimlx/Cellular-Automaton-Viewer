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
import picocli.CommandLine
import java.util.function.ToIntFunction
import kotlin.system.exitProcess

@CommandLine.Command(name = "rand", aliases = ["random"], description = ["Generates random soups to pipe into apgluxe"])
class RandomSoupCommand : Runnable {
    @CommandLine.Option(
        names = ["-n", "--num"], description = ["The number of soups to generate " +
                "(default: -1, generate soups forever)"], defaultValue = "-1"
    )
    private val num = 0

    @CommandLine.Option(
        names = ["-d", "--density"],
        description = ["The density of the random soup (default: 50)"],
        defaultValue = "50"
    )
    private val density = 0

    @CommandLine.Option(
        names = ["-x", "--width"],
        description = ["The width of the random soup (default: 16)"],
        defaultValue = "16"
    )
    private val x = 0

    @CommandLine.Option(
        names = ["-y", "--height"],
        description = ["The height of the random soup (default: 16)"],
        defaultValue = "16"
    )
    private val y = 0

    @CommandLine.Option(
        names = ["-s", "--symmetry"], description = ["The symmetry of the random soup. " +
                "[C1, D2-, D4+] (default: C1)"], defaultValue = "C1"
    )
    private val symmetry: String? = null

    @CommandLine.Option(
        names = ["-S", "--states"],
        description = ["The states to include in the random soup (default: 1)"],
        defaultValue = "1"
    )
    private val states: IntArray = intArrayOf(0)

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private val help = false
    override fun run() {
        if (num == -1) {
            while (true) {
                println("x = $x, y = $y, rule = B3/S23")
                println(SymmetryGenerator.generateSymmetry(symmetry, density, states, x, y).toRLE())
            }
        } else {
            for (i in 0 until num) {
                println("x = $x, y = $y, rule = B3/S23")
                println(SymmetryGenerator.generateSymmetry(symmetry, density, states, x, y).toRLE())
            }
        }

        exitProcess(0)
    }
}