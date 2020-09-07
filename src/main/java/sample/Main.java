package sample;

import javafx.application.Platform;
import picocli.CommandLine;
import sample.commands.*;

@CommandLine.Command(subcommands = {
        GUICommand.class,
        SimulationCommand.class,
        IdentifyCommand.class,
        RuleRangeCommand.class,
        RuleSearchCommand.class,
        CommandLine.HelpCommand.class
})
public class Main implements Runnable {
    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("GUI")) {
            GUICommand.main(args);
            return;
        }
        
        Platform.startup(() -> {});  // Start JavaFX before doing anything else
        Platform.runLater(() -> new CommandLine(new Main()).execute(args));
    }

    @Override
    public void run() {
        GUICommand.main(new String[0]);
    }
}
