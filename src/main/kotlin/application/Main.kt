package application

import application.commands.*
import picocli.CommandLine
import picocli.CommandLine.HelpCommand
import java.lang.Runnable
import kotlin.jvm.JvmStatic
import java.util.logging.LogManager
import java.io.IOException
import kotlin.system.exitProcess

@CommandLine.Command(subcommands = [
    GUICommand::class,
    SimulationCommand::class,
    IdentifyCommand::class,
    RuleRangeCommand::class,
    RuleSearchCommand::class,
    CatalystSearchCommand::class,
    ShipSearchCommand::class,
    RandomSoupCommand::class,
    SynthesisCommand::class,
    ApgtableCommand::class,
    RuleInfoCommand::class,
    SSSCommand::class,
    DBCommand::class,
    DBEntryCommand::class,
    CanonDBCommand::class,
    HelpCommand::class
])
class Main : Runnable {
    override fun run() {
        GUICommand().run()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                LogManager.getLogManager().readConfiguration(
                    Main::class.java.getResourceAsStream("/logging.properties")
                )
            } catch (ignored: IOException) { }

            if (args.isEmpty() || args[0] == "GUI") {
                GUICommand().run()
                return
            }

            CommandLine(Main()).execute(*args)
            exitProcess(0)
        }
    }
}