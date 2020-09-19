package sample.commands;

import picocli.CommandLine;
import sample.model.Simulator;
import sample.model.rules.hrot.HROT;

import java.io.File;
import java.io.FileNotFoundException;

@CommandLine.Command(name = "id", aliases = {"identify"}, description = "Identifies oscillators / spaceships")
public class
IdentifyCommand implements Runnable {
    private Simulator simulator;

    @CommandLine.Option(names = {"-i", "--input"},
            description = "Input file containing pattern to be identified", required = true)
    private File inputFile;

    @CommandLine.Option(names = {"-m", "-g", "--max_period"}, defaultValue = "50",
            description = "Number of generations to run the pattern")
    private int maxPeriod;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help;

    @Override
    public void run() {
        try {
            // Initialise the Simulator
            simulator = new Simulator(new HROT("B3/S23"));

            // Loading the pattern
            if (inputFile != null) {
                CommandUtils.loadPattern(simulator, inputFile);
            }
            else {
                System.err.println("Input file must be specified!");
                System.exit(-1);
            }
            
            sample.model.patterns.Pattern pattern = simulator.identify(maxPeriod);
            System.out.println(pattern);

            for (String key: pattern.additionalInfo().keySet()) {
                System.out.println(key + ": " + pattern.additionalInfo().get(key));
            }
        }
        catch (FileNotFoundException exception) {
            System.err.println("Input / Output file could not be found!");
            System.exit(-1);
        }

        System.exit(0);
    }
}
