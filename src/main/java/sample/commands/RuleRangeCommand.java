package sample.commands;

import org.javatuples.Pair;
import picocli.CommandLine;
import sample.model.Grid;
import sample.model.Simulator;
import sample.model.rules.HROT;
import sample.model.rules.RuleFamily;

import java.io.File;
import java.io.FileNotFoundException;

@CommandLine.Command(name = "rr", aliases = {"rule_range"}, description = "Computes rule range an inputted pattern")
public class RuleRangeCommand implements Runnable {
    private Simulator simulator;

    @CommandLine.Option(names = {"-i", "--input"}, description = "Input file containing pattern whose " +
            "rule range is to be computed", required = true)
    private File inputFile;

    @CommandLine.Option(names = {"-m", "-g", "--generations"}, defaultValue = "0",
            description = "Number of generations to run the pattern (default: 0)")
    private int generations;

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
                System.out.println("Input file must be specified!");
                System.exit(-1);
            }
            
            Grid[] grids = new Grid[generations + 1];
            for (int i = 0; i < generations + 1; i++) {
                grids[i] = simulator.deepCopy();
                simulator.step();
            }

            Pair<RuleFamily, RuleFamily> minMaxRule = ((RuleFamily) simulator.getRule()).getMinMaxRule(grids);
            System.out.println("Min Rule: " + minMaxRule.getValue0());
            System.out.println("Max Rule: " + minMaxRule.getValue1());
        }
        catch (FileNotFoundException exception) {
            System.out.println("Input / Output file could not be found!");
            System.exit(-1);
        }

        System.exit(0);
    }
}
