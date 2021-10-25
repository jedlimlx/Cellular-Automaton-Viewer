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

@CommandLine.Command(name = "sim", aliases = ["run"], description = ["Simulate a pattern"])
class SimulationCommand : Runnable {
    private var simulator: Simulator? = null

    @CommandLine.Option(
        names = ["-i", "--input"],
        description = ["Input file containing pattern to be simulated"],
        required = true
    )
    private val inputFile: File? = null

    @CommandLine.Option(names = ["-o", "--out"], description = ["Output file for the pattern."], required = true)
    private val outputFile: File? = null

    @CommandLine.Option(
        names = ["-m", "-g", "--generation"],
        description = ["Number of generations to run the pattern"],
        required = true
    )
    private val generations = 0

    @CommandLine.Option(
        names = ["-s", "--step"],
        defaultValue = "1",
        description = ["Patterns will be printed to the console every step size generations " +
                "(default: 1)"]
    )
    private val stepSize = 0

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true)
    private val help = false

    private var startingCoordinate: Coordinate? = null
    private val rleFinal = StringBuilder()

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

            simulator!!.updateBounds()
            startingCoordinate = simulator!!.bounds.value0
            for (i in 0 until generations + 1) {
                // Printing every stepSize
                if (i % stepSize == 0) getRLE()
                simulator!!.step() // Stepping forward 1 generation
            }

            savePattern(outputFile!!)
        } catch (exception: IllegalArgumentException) {
            System.err.println(exception.message)
            exitProcess(-1)
        } catch (exception: FileNotFoundException) {
            System.err.println("Input / Output file could not be found!")
            exitProcess(-1)
        } catch (exception: IOException) {
            exception.printStackTrace()
            exitProcess(-1)
        }
        exitProcess(0)
    }

    fun getRLE() {
        // Getting bounding box
        simulator!!.updateBounds()
        val bounds = simulator!!.bounds

        val start = bounds.value0
        val end = bounds.value1

        // Add header & comments
        val rle = simulator!!.toRLE(start, end)

        // Adding header
        rleFinal.append(start.x - startingCoordinate!!.x + 1).append(",")
            .append(start.y - startingCoordinate!!.y + 1).append("\n")
        rleFinal.append(end.x - start.x + 1).append(",").append(end.y - start.y + 1).append("\n")
        rleFinal.append(rle).append("\n")
    }

    @Throws(IOException::class)
    fun savePattern(file: File) {
        val fileWriter = FileWriter(file)
        fileWriter.write(rleFinal.toString())
        fileWriter.write("\n@COLOR\n")
        for (state in 0 until simulator!!.rule.numStates) {
            fileWriter.write("$state ")
            fileWriter.write((simulator!!.rule.getColour(state).red * 255).toInt().toString() + " ")
            fileWriter.write((simulator!!.rule.getColour(state).green * 255).toInt().toString() + " ")
            fileWriter.write("${(simulator!!.rule.getColour(state).blue * 255).toInt()}\n")
        }

        fileWriter.close()
    }
}