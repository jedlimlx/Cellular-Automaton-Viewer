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

@CommandLine.Command(name = "5s", aliases = ["sssss"], description = ["Queries the 5S / SOSSP database"])
class SSSCommand : Runnable {
    @CommandLine.Option(names = ["-v", "--velocity"], description = ["Velocity of the ship"])
    private var velocity: String = ""

    @CommandLine.Option(names = ["-p", "--period"], description = ["Period of the oscillator"], defaultValue = "0")
    private var period = 0

    @CommandLine.Option(names = ["-db", "--database"], description = ["The database file"], required = true)
    private var databaseFile: File? = null

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private var help = false

    override fun run() {
        try {
            if (period == 0 && velocity == "") {
                System.err.println("Either period or velocity must be specified")
            } else if (period == 0) {
                val reader = SSSSSReader(databaseFile)
                val period: Int
                val dx: Int
                var dy = 0
                if (velocity.endsWith("o")) {
                    val regex = "(\\d+)?c/(\\d+)o"
                    dx = try {
                        Utils.matchRegex("(\\d+)c/\\d+o", velocity, 0, 1).toInt()
                    } catch (exception: IllegalStateException) {
                        1
                    }
                    period = Utils.matchRegex(regex, velocity, 0, 2).toInt()
                } else if (velocity.endsWith("d")) {
                    val regex = "(\\d+)?c/(\\d+)d"
                    dx = try {
                        Utils.matchRegex("(\\d+)c/\\d+d", velocity, 0, 1).toInt()
                    } catch (exception: IllegalStateException) {
                        1
                    }
                    dy = dx
                    period = Utils.matchRegex(regex, velocity, 0, 2).toInt()
                } else {
                    val regex = "\\((\\d+),\\s*(\\d+)\\)c/(\\d+)"
                    dx = Utils.matchRegex(regex, velocity, 0, 1).toInt()
                    dy = Utils.matchRegex(regex, velocity, 0, 2).toInt()
                    period = Utils.matchRegex(regex, velocity, 0, 3).toInt()
                }

                val result = reader.getShipBySpeed(period, dx, dy)
                if (result == null) {
                    System.err.println("No such spaceship found in database!")
                    exitProcess(-1)
                } else {
                    println("#C $result")
                    println("#C Population: " + result.population)
                    println(Utils.fullRLE(result))
                }
            } else {
                val reader = SOSSPReader(databaseFile)
                val result = reader.getOscByPeriod(period)
                if (result == null) {
                    System.err.println("No such oscillator found in database!")
                    exitProcess(-1)
                } else {
                    println("#C $result")
                    println("#C Population: " + result.population)
                    println(Utils.fullRLE(result))
                }
            }
        } catch (exception: IOException) {
            System.err.println(exception.message)
            exitProcess(-1)
        } catch (exception: IllegalStateException) {
            System.err.println("Invalid velocity inputted!")
            exitProcess(-1)
        }

        exitProcess(0)
    }
}