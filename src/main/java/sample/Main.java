package sample;

import picocli.CommandLine;
import sample.commands.*;

@CommandLine.Command(subcommands = {
        GUICommand.class,
        SimulationCommand.class,
        IdentifyCommand.class,
        RuleRangeCommand.class,
        RuleSearchCommand.class,
        RandomSoupCommand.class,
        SynthesisCommand.class,
        ApgtableCommand.class,
        CommandLine.HelpCommand.class
})
public class Main implements Runnable {
    public static void main(String[] args) {
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
