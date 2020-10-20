package sample.commands;

import org.javatuples.Pair;
import picocli.CommandLine;
import sample.model.Utils;
import sample.model.rules.MinMaxRuleable;
import sample.model.rules.RuleFamily;
import sample.model.rules.hrot.HROT;
import sample.model.simulation.Grid;
import sample.model.simulation.Simulator;

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
                Utils.loadPattern(simulator, inputFile);
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


            // Checking if min / max rules are supported
            if (simulator.getRule() instanceof MinMaxRuleable) {
                Pair<RuleFamily, RuleFamily> minMaxRule = ((MinMaxRuleable) simulator.getRule()).getMinMaxRule(grids);
                System.out.println("Min Rule: " + minMaxRule.getValue0());
                System.out.println("Max Rule: " + minMaxRule.getValue1());
            }
            else {
                System.err.println("Minimum, maximum rules are not supported by this rulespace!");
                System.exit(-1);
            }
        }
        catch (FileNotFoundException exception) {
            System.err.println("Input / Output file could not be found!");
            System.exit(-1);
        }

        System.exit(0);
    }
}
