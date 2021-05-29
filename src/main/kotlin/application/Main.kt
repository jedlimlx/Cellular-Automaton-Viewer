package application

import application.commands.GUICommand
import application.commands.SimulationCommand
import application.commands.IdentifyCommand
import application.commands.RuleRangeCommand
import application.commands.RuleSearchCommand
import application.commands.CatalystSearchCommand
import application.commands.RandomSoupCommand
import application.commands.SynthesisCommand
import application.commands.ApgtableCommand
import application.commands.RuleInfoCommand
import application.commands.SSSCommand
import application.commands.DBCommand
import application.commands.DBEntryCommand
import application.commands.CanonDBCommand
import picocli.CommandLine
import picocli.CommandLine.HelpCommand
import java.lang.Runnable
import kotlin.jvm.JvmStatic
import java.util.logging.LogManager
import java.io.IOException
import kotlin.system.exitProcess

@CommandLine.Command(subcommands = [GUICommand::class, SimulationCommand::class, IdentifyCommand::class, RuleRangeCommand::class, RuleSearchCommand::class, CatalystSearchCommand::class, RandomSoupCommand::class, SynthesisCommand::class, ApgtableCommand::class, RuleInfoCommand::class, SSSCommand::class, DBCommand::class, DBEntryCommand::class, CanonDBCommand::class, HelpCommand::class])
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
            } catch (ignored: IOException) {
            }

            if (args.isEmpty() || args[0] == "GUI") {
                GUICommand().run()
                return
            }

            CommandLine(Main()).execute(*args)
            exitProcess(0)
        }
    }
}