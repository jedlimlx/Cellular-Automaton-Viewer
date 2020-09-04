package sample.model.search;

import sample.model.Coordinate;
import sample.model.Grid;
import sample.model.Simulator;
import sample.model.patterns.Pattern;
import sample.model.rules.Rule;
import sample.model.rules.RuleFamily;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class RuleSearch extends SearchProgram {
    private HashSet<String> known;

    public RuleSearch(SearchParameters parameters) {
        super(parameters);

        if (!(parameters instanceof RuleSearchParameters)) {
            throw new IllegalArgumentException("SearchParameters must be of type RuleSearchParameters");
        }
    }

    public void search(int numRules) {
        // TODO (Implement a more high-tech repetition checker)
        // TODO (Do whatever WildMyron says that searchPatt-matchPatt does with the min / max rule)
        Simulator simulator;
        RuleSearchParameters searchParameters = (RuleSearchParameters) this.searchParameters;

        known = new HashSet<>();  // Hash set to store known things
        searchResults = new ArrayList<>(); // Initialise search results

        for (int i = 0; i < numRules; i++) {
            // Create a new simulator object each time
            simulator = new Simulator((Rule) searchParameters.getMinRule().clone());
            simulator.insertCells(searchParameters.getTargetPattern(), new Coordinate(0, 0));

            // Randomise the rule
            ((RuleFamily) simulator.getRule()).randomise(
                    searchParameters.getMinRule(), searchParameters.getMaxRule());

            // Identify the object
            Pattern result = simulator.identify(searchParameters.getMaxPeriod(), true, 40);
            if (result != null && !result.toString().equals("Still Life") && !known.contains(result.toString())) {
                add(searchResults, result);
                add(known, result.toString());  // To avoid duplicate speeds & whatnot
            }

            synchronized (this) {  // To avoid race conditions
                numSearched++;
            }
        }
    }

    @Override
    public boolean writeToFile(File file) {
        try {
            FileWriter writer = new FileWriter(file);

            // Writing the search parameters
            RuleSearchParameters searchParameters = (RuleSearchParameters) this.searchParameters;
            writer.write("# Running search with " + searchParameters.getTargetPattern().toRLE() + "\n");
            writer.write("# Max Period: " + searchParameters.getMaxPeriod() + "\n");
            writer.write("# Min Rule: " + searchParameters.getMinRule() + "\n");
            writer.write("# Max Rule: " + searchParameters.getMaxRule() + "\n");
            writer.write("Pattern,Rule,Min Rule,Max Rule\n");

            for (Grid grid: searchResults) {   // Writing each pattern into the file
                Pattern pattern = (Pattern) grid;
                writer.write("\"" + pattern + "\",\"" + ((RuleFamily) pattern.getRule()).getRulestring() + "\",\"" +
                        pattern.getMinRule().getRulestring() + "\",\"" +
                        pattern.getMaxRule().getRulestring() + "\"\n");
            }

            // Close the file
            writer.close();
            return true;
        }
        catch (IOException exception) {
            return false;
        }
    }
}
