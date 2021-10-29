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
import java.io.File
import java.util.function.ToIntFunction
import kotlin.system.exitProcess

@CommandLine.Command(name = "canon", aliases = ["canonise"], description = ["Canonises a GliderDB database"])
class CanonDBCommand : Runnable {
    @CommandLine.Option(names = ["-db", "--database"], description = ["The database file"], required = true)
    private var databaseFile: File? = null

    @CommandLine.Option(names = ["-out", "--output"], description = ["The output file"], required = true)
    private var outputFile: File? = null

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private var help = false

    override fun run() {
        try {
            val reader = GliderDBReader(databaseFile)
            reader.canoniseDB(outputFile)
        } catch (exception: IOException) {
            System.err.println(exception.message)
            exitProcess(-1)
        }

        exitProcess(0)
    }
}