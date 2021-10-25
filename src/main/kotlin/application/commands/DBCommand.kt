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
import java.util.Comparator
import java.util.function.ToIntFunction
import kotlin.system.exitProcess

@CommandLine.Command(name = "db", aliases = ["database"], description = ["Queries the GliderDB database"])
class DBCommand : Runnable {
    @CommandLine.Option(names = ["-db", "--database"], description = ["The database file"], required = true)
    private val file: File? = null

    @CommandLine.Option(names = ["-p", "--period"], description = ["The period of the ship"], defaultValue = "-1")
    private val period = 0

    @CommandLine.Option(names = ["-dx"], description = ["The x displacement of the ship"], defaultValue = "-1")
    private val dx = 0

    @CommandLine.Option(names = ["-dy"], description = ["The y displacement of the ship"], defaultValue = "-1")
    private val dy = 0

    @CommandLine.Option(names = ["-min", "--min_rule"], description = ["The minimum rule"], defaultValue = "")
    private val minRule: String? = null

    @CommandLine.Option(names = ["-max", "--max_rule"], description = ["The maximum rule"], defaultValue = "")
    private val maxRule: String? = null

    @CommandLine.Option(
        names = ["-sort", "--sort"],
        description = ["Sort by [period, slope, population]"],
        defaultValue = ""
    )
    private val sort: String? = null

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private val help = false

    override fun run() {
        var minRule: RuleFamily?
        var maxRule: RuleFamily?

        try {
            minRule = Utils.fromRulestring(this.minRule)
            maxRule = Utils.fromRulestring(this.maxRule)
        } catch (exception: IllegalArgumentException) {
            minRule = null
            maxRule = null
        }

        val comparator: Comparator<GliderDBEntry>? = when (sort) {
            "period" -> Comparator.comparingInt { o: GliderDBEntry -> o.spaceship.period }
            "slope" -> Comparator { o1: GliderDBEntry, o2: GliderDBEntry ->
                if (o1.spaceship.displacementX == o2.spaceship.displacementX)
                    o1.spaceship.displacementY.compareTo(o2.spaceship.displacementY)
                else o1.spaceship.displacementX.compareTo(o2.spaceship.displacementX)
            }
            "population" -> Comparator.comparingInt { o: GliderDBEntry -> o.spaceship.population }
            else -> null
        }

        try {
            val reader = GliderDBReader(file)
            val entries = reader.getEntries(period, dx, dy, minRule, maxRule, comparator)
            for (entry in entries) {
                println("#C " + entry.spaceship)

                if (entry.name != "") println("#C Name: " + entry.name)
                if (entry.discoverer != "") println("#C Discovered by: " + entry.discoverer)

                println("#C Min Rule: " + entry.spaceship.minRule)
                println("#C Max Rule: " + entry.spaceship.maxRule)
                println("#C Population: " + entry.spaceship.population)
                println(Utils.fullRLE(entry.spaceship))
                println()
            }
        } catch (exception: FileNotFoundException) {
            System.err.println("The file specified cannot be found!")
            exitProcess(-1)
        }

        exitProcess(0)
    }
}