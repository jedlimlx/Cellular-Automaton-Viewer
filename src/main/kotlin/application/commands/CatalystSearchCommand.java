package application.commands;

import picocli.CommandLine;
import application.model.Coordinate;
import application.model.Utils;
import application.model.rules.hrot.HROT;
import application.model.search.catsrc.CatalystSearch;
import application.model.search.catsrc.CatalystSearchParameters;
import application.model.simulation.Grid;
import application.model.simulation.Simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

@CommandLine.Command(name = "cat", aliases = {"catsrc"},
        description = "Searches randomly generated configurations of still lives for catalysts")
public class CatalystSearchCommand implements Runnable {
    @CommandLine.Option(names = {"-o", "--output"}, description = "Output file to save results", required = true)
    private File outputFile;

    @CommandLine.Option(names = {"-rt", "--repeat_time"}, defaultValue = "50",
            description = "The maximum number of generations to run the pattern (default: 50)")
    private int maxRepeatTime;

    @CommandLine.Option(names = {"-n", "--number"}, defaultValue = "10000",
            description = "Number of catalysts to search for before the program terminates (default: 10000)")
    private int numSearch;

    @CommandLine.Option(names = {"-rot", "--rotate"}, defaultValue = "true",
            description = "Should the catalysts be rotated? (default: true)")
    private boolean rotateCatalysts;

    @CommandLine.Option(names = {"-flip", "--flip"}, defaultValue = "true",
            description = "Should the catalysts be flipped? (default: true)")
    private boolean flipCatalysts;

    @CommandLine.Option(names = {"-n_cat", "--num_cat"}, defaultValue = "3",
            description = "Number of catalysts to place in the search area (default: 3)")
    private int numCatalysts;

    @CommandLine.Option(names = {"-cat", "--catalysts"},
            description = "Input file containing a list of catalysts (one per line) to use", required = true)
    private File catalysts;

    @CommandLine.Option(names = {"-s", "--search"},
            description = "Input file containing an RLE. The target is state 1 and above. " +
                    "The search area is the maximum state found in the RLE.", required = true)
    private File searchArea;

    @CommandLine.Option(names = {"--time"}, defaultValue = "30",
            description = "Number of seconds between file writes (default: 30)")
    private int time;

    @CommandLine.Option(names = {"-t", "--threads"}, defaultValue = "5",
            description = "Number of threads (default: 5)")
    private int threads;

    @CommandLine.Option(names = {"-r", "--rule"}, description = "The rule to search for catalysts in",
            required = true)
    private String rule;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help;

    @Override
    public void run() {
        try {
            // Reading the catalyst input file
            Scanner scanner = new Scanner(catalysts);

            List<Grid> catalysts = new ArrayList<>();
            while (scanner.hasNextLine()) {
                Grid catalyst = new Grid();
                catalyst.fromRLE(scanner.nextLine(), new Coordinate());

                catalysts.add(catalyst);
            }

            scanner.close();

            // Reading target and search area input file
            Simulator simulator = new Simulator(new HROT("B3/S23"));
            Utils.loadPattern(simulator, searchArea);

            // Find max state
            AtomicInteger maxState = new AtomicInteger(1);
            simulator.iterateCells(cell -> maxState.set(Math.max(maxState.get(), simulator.getCell(cell))));

            Grid target = new Grid();
            ArrayList<Coordinate> searchArea = new ArrayList<>();
            simulator.iterateCells(cell -> {
                if (simulator.getCell(cell) == maxState.get()) searchArea.add(cell);
                else if (simulator.getCell(cell) != 0) target.setCell(cell, simulator.getCell(cell));
            });

            CatalystSearchParameters searchParameters = new CatalystSearchParameters(maxRepeatTime,
                    numCatalysts, false, rotateCatalysts,
                    flipCatalysts, catalysts, target, searchArea, Utils.fromRulestring(rule));
            CatalystSearch catalystSearch = new CatalystSearch(searchParameters);
            catalystSearch.searchThreaded(numSearch, threads);

            while (catalystSearch.getNumSearched() < numSearch) {
                if (catalystSearch.getSearchResults() != null) {
                    if (!catalystSearch.writeToFile(outputFile)) {
                        System.err.println("Something went wrong while writing to the output file!");
                    }
                }

                Thread.sleep(time * 1000);
            }

            System.out.println("Search complete, " + catalystSearch.getSearchResults().size() + " catalysts found!");
            System.exit(0);
        }
        catch (FileNotFoundException exception) {
            System.err.println("Input / Output file could not be found!");
            System.exit(-1);
        }
        catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage());
            System.exit(-1);
        }
        catch (InterruptedException ignored) {}
    }
}
