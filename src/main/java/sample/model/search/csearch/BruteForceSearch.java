package sample.model.search.csearch;

import sample.model.Coordinate;
import sample.model.SymmetryGenerator;
import sample.model.patterns.Pattern;
import sample.model.search.SearchProgram;
import sample.model.simulation.Grid;
import sample.model.simulation.Simulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * CAViewer's brute force search program - csearch.
 *
 * TODO (Object Separation)
 */
public class BruteForceSearch extends SearchProgram {
    private HashSet<Pattern> known;
    private HashSet<Integer> tried;

    /**
     * Constructs the brute force search program with the provided parameters
     * @param parameters The parameters of the search program
     */
    public BruteForceSearch(BruteForceSearchParameters parameters) {
        super(parameters);
    }

    @Override
    public void search(int num) {
        searchThreaded(num, 1);
    }

    @Override
    public void searchThreaded(int num, int numThreads) {
        // Multi-threading
        executor = Executors.newFixedThreadPool(numThreads);

        // Split into search space into shards for distributed search
        for (int t = 0; t < numThreads; t++) {
            int finalT = t;
            executor.submit(() -> {
                Simulator simulator;
                BruteForceSearchParameters searchParameters = (BruteForceSearchParameters) this.searchParameters;

                known = new HashSet<>();  // Hash set to store known things
                tried = new HashSet<>();  // Hash set to prevent re-attempting rotations and reflections of a soup
                searchResults = new ArrayList<>(); // Initialise search results

                long total, startTime = System.currentTimeMillis();
                if (searchParameters.isRandom())
                    total = num;  // Random Soups
                else  // Brute Force
                    total = (long) Math.pow(2, searchParameters.getxBound() * searchParameters.getyBound());

                // There are n ^ (x * y) possible soups to check
                for (long i = finalT * (total / numThreads); i < (finalT + 1) * (total / numThreads); i++) {
                    // Create a new simulator object each time
                    simulator = new Simulator(searchParameters.getRule());

                    // Converting to base n to get the soup
                    if (searchParameters.isRandom()) {
                        simulator.insertCells(SymmetryGenerator.generateSymmetry("C1", 50, new int[]{1},
                                searchParameters.getxBound(), searchParameters.getyBound()),
                                new Coordinate());
                    } else {
                        int x = 0, y = 0;
                        String charString;
                        String soup = BigInteger.valueOf(i).toString(2);
                        for (int j = 0; j < soup.length(); j++) {
                            charString = soup.charAt(j) + "";
                            if (charString.matches("\\d")) {
                                simulator.setCell(x, y, Integer.parseInt(charString));
                            } else {
                                simulator.setCell(x, y, soup.charAt(j) - 65 + 10);
                            }

                            if (x == searchParameters.getxBound()) {
                                y++;
                                x = 0;
                            } else {
                                x++;
                            }
                        }

                        // Quit if soup has already been tried
                        if (tried.contains(simulator.hashCode())) continue;

                        // Adding rotations to tried
                        simulator.updateBounds();
                        tried.add(simulator.hashCode());

                        simulator.rotateCW(simulator.getBounds().getValue0(), simulator.getBounds().getValue1());
                        tried.add(simulator.hashCode());

                        simulator.rotateCW(simulator.getBounds().getValue0(), simulator.getBounds().getValue1());
                        tried.add(simulator.hashCode());

                        simulator.rotateCW(simulator.getBounds().getValue0(), simulator.getBounds().getValue1());
                        tried.add(simulator.hashCode());
                    }

                    // Identify the object
                    Pattern result = simulator.identify(searchParameters.getMaxPeriod());

                    if (result != null && !result.toString().equals("Still Life") &&
                            !known.contains(result)) {
                        add(searchResults, result);
                        add(known, result);  // To avoid duplicate speeds & whatnot
                    }

                    if (numSearched % 5000 == 0) {
                        System.out.println(numSearched + " soups searched (" +
                                5000000 / (System.currentTimeMillis() - startTime) +
                                " soup/s), " + searchResults.size() + " objects found!");
                        startTime = System.currentTimeMillis();
                    }

                    synchronized (this) {  // To avoid race conditions
                        numSearched++;
                    }
                }

                System.out.println("Completed shard " + finalT + " of the search space!");
            });
        }
    }

    @Override
    public boolean writeToFile(File file) {
        try {
            FileWriter writer = new FileWriter(file);

            // Writing the search parameters
            BruteForceSearchParameters searchParameters = (BruteForceSearchParameters) this.searchParameters;
            writer.write("# Rule: " + searchParameters.getRule() + "\n");
            writer.write("# Max Period: " + searchParameters.getMaxPeriod() + "\n");
            writer.write("Pattern,RLE\n");

            for (Grid grid: searchResults) {   // Writing each pattern into the file
                Pattern pattern = (Pattern) grid;
                writer.write("\"" + pattern + "\"," + pattern.toRLE() + "\n");
            }

            // Close the file
            writer.close();
            return true;
        }
        catch (IOException exception) {
            LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).
                    log(Level.WARNING, exception.getMessage());
            return false;
        }
    }
}
