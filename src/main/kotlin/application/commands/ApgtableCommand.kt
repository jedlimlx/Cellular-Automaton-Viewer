package application.commands

import application.model.Utils
import application.model.rules.ApgtableGeneratable
import application.model.rules.ruleloader.ColourDirective
import application.model.rules.ruleloader.RuleLoader
import application.model.rules.ruleloader.RuleNameDirective
import picocli.CommandLine
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.system.exitProcess

@CommandLine.Command(
    name = "apgtable",
    aliases = ["ruletable"],
    description = ["Generates an apgtable / ruletable for apgsearch"]
)
class ApgtableCommand : Runnable {
    @CommandLine.Option(names = ["-r", "--rulestring"], description = ["Rulestring of the rule"], required = true)
    private var ruleString: String? = null

    @CommandLine.Option(names = ["-o", "--output"], description = ["Output file"], required = true)
    private var outputFile: File? = null

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private var help = false

    override fun run() {
        try {
            val family = Utils.fromRulestring(ruleString)
            if (family !is ApgtableGeneratable) throw UnsupportedOperationException("This rulespace does not support apgtable generation!")

            val ruleDirectives = (family as ApgtableGeneratable).generateApgtable()
            val ruleLoader = RuleLoader()
            ruleLoader.addDirective(
                RuleNameDirective(
                    "@RULE " +
                            outputFile!!.name.replace(".rule", "")
                )
            )

            val colourDirective = StringBuilder("@COLORS\n")
            for (i in 0 until family.numStates) {
                colourDirective.append(i).append(" ").append((family.getColour(i).red * 255).toInt()).append(" ")
                    .append((family.getColour(i).green * 255).toInt()).append(" ")
                    .append((family.getColour(i).blue * 255).toInt()).append("\n")
            }
            ruleLoader.addDirective(ColourDirective(colourDirective.toString()))

            for (directive in ruleDirectives) ruleLoader.addRuleDirective(directive)
            val fileWriter = FileWriter(outputFile)
            fileWriter.write(ruleLoader.export())
            fileWriter.close()
        } catch (exception: UnsupportedOperationException) {
            System.err.println(exception.message)
            exitProcess(-1)
        } catch (exception: IOException) {
            System.err.println(exception.message)
            exitProcess(-1)
        }
    }
}