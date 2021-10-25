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
import java.util.function.ToIntFunction
import kotlin.system.exitProcess

@CommandLine.Command(
    name = "info",
    aliases = ["information"],
    description = ["Get more information about a particular rule"]
)
class RuleInfoCommand : Runnable {
    @CommandLine.Option(names = ["-r", "--rulestring"], description = ["Rulestring of the rule"], required = true)
    private val ruleString: String? = null

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private val help = false

    override fun run() {
        val info = Utils.fromRulestring(ruleString).ruleInfo
        for (key in info.keys) {
            println(key + ": " + info[key])
        }

        exitProcess(0)
    }
}