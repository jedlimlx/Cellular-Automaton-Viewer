package sample.commands;

import picocli.CommandLine;
import sample.model.Utils;
import sample.model.rules.hrot.HROT;
import sample.model.search.rulesrc.RuleSearch;
import sample.model.search.rulesrc.RuleSearchParameters;
import sample.model.simulation.Simulator;

import java.io.File;
import java.io.FileNotFoundException;

@CommandLine.Command(name = "rs", aliases = {"rulesrc"}, description = "Searches rules for oscillators / spaceships")
public class RuleSearchCommand implements Runnable {
    @CommandLine.Option(names = {"-i", "--input"}, description = "Input file containing the seed", required = true)
    private File inputFile;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Output file to save results", required = true)
    private File outputFile;

    @CommandLine.Option(names = {"-m", "-g", "--max_period"}, defaultValue = "50",
            description = "The maximum number of generations to run the pattern (default: 50)")
    private int maxPeriod;

    @CommandLine.Option(names = {"-min_pop"}, defaultValue = "0",
            description = "The minimum population of the pattern (default: 0)")
    private int minPop;

    @CommandLine.Option(names = {"-pop", "-max_pop"}, defaultValue = "100",
            description = "The maximum population of the pattern (default: 100)")
    private int maxPop;

    @CommandLine.Option(names = {"-x", "-width"}, defaultValue = "40",
            description = "The maximum width of the pattern's bounding box (default: 40)")
    private int maxBoundX;

    @CommandLine.Option(names = {"-y", "-max_y"}, defaultValue = "40",
            description = "The maximum height the pattern's bounding box (default: 40)")
    private int maxBoundY;

    @CommandLine.Option(names = {"-n", "--number"}, defaultValue = "10000", 
            description = "Number of rules to search before the program terminates (default: 10000)")
    private int numSearch;

    @CommandLine.Option(names = {"--time"}, defaultValue = "30",
            description = "Number of seconds between file writes (default: 30)")
    private int time;

    @CommandLine.Option(names = {"-t", "--threads"}, defaultValue = "5", 
            description = "Number of threads (default: 5)")
    private int threads;
    
    @CommandLine.Option(names = {"-min", "--min_rule"}, description = "Minimum rule of the search space",
            required = true)
    private String minRule;
    
    @CommandLine.Option(names = {"-max", "--max_rule"}, description = "Maximum rule of the search space",
            required = true)
    private String maxRule;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help;

    @Override
    public void run() {
        try {
            Simulator simulator = new Simulator(new HROT("B3/S23"));

            if (inputFile != null) {
                Utils.loadPattern(simulator, inputFile);
            }
            else {
                System.err.println("Input file must be specified!");
                System.exit(-1);
            }

            if (outputFile == null) {
                System.err.println("Output file must be specified!");
                System.exit(-1);
            }

            RuleSearchParameters parameters = new RuleSearchParameters(simulator.deepCopy(),
                    Utils.fromRulestring(minRule), Utils.fromRulestring(maxRule), maxPeriod, minPop, maxPop,
                    maxBoundX, maxBoundY);

            RuleSearch ruleSearch = new RuleSearch(parameters);
            ruleSearch.searchThreaded(numSearch, threads);

            while (ruleSearch.getNumSearched() < numSearch) {
                if (ruleSearch.getSearchResults() != null) {
                    if (!ruleSearch.writeToFile(outputFile)) {
                        System.err.println("Something went wrong while writing to the output file!");
                    }
                }

                Thread.sleep(time * 1000);
            }

            System.out.println("Search Complete.");
        }
        catch (FileNotFoundException exception) {
            System.err.println("Input / Output file could not be found!");
            System.exit(-1);
        }
        catch (InterruptedException ignored) {}

        System.exit(0);
    }
}
