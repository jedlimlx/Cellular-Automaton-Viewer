package sample;

import picocli.CommandLine;
import sample.commands.*;

import java.io.IOException;
import java.util.logging.LogManager;

@CommandLine.Command(subcommands = {
        GUICommand.class,
        SimulationCommand.class,
        IdentifyCommand.class,
        RuleRangeCommand.class,
        RuleSearchCommand.class,
        CatalystSearchCommand.class,
        RandomSoupCommand.class,
        SynthesisCommand.class,
        ApgtableCommand.class,
        RuleInfoCommand.class,
        SSSCommand.class,
        DBCommand.class,
        DBEntryCommand.class,
        CanonDBCommand.class,
        CommandLine.HelpCommand.class
})
public class Main implements Runnable {
    public static void main(String[] args) {
        try {
            Benchmark.main(args);
            LogManager.getLogManager().readConfiguration(
                    Main.class.getResourceAsStream("/logging.properties"));
        } catch (IOException ignored) {}

        if (args.length == 0 || args[0].equals("GUI")) {
            GUICommand.main(args);
            return;
        }

        new CommandLine(new Main()).execute(args);
        System.exit(0);
    }

    @Override
    public void run() {
        GUICommand.main(new String[0]);
    }
}
