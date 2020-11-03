package sample.model.search;

import sample.model.Coordinate;
import sample.model.database.GliderDBEntry;
import sample.model.patterns.Oscillator;
import sample.model.patterns.Pattern;
import sample.model.patterns.PowerLawPattern;
import sample.model.patterns.Spaceship;
import sample.model.rules.MinMaxRuleable;
import sample.model.rules.Rule;
import sample.model.rules.RuleFamily;
import sample.model.simulation.Grid;
import sample.model.simulation.Simulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class RuleSearch extends SearchProgram {
    private HashSet<Pattern> known;

    public RuleSearch(RuleSearchParameters parameters) {
        super(parameters);
    }

    public void search(int numRules) throws IllegalArgumentException {
        // TODO (Do whatever WildMyron says that searchPatt-matchPatt does with the min / max rule)
        Simulator simulator;
        RuleSearchParameters searchParameters = (RuleSearchParameters) this.searchParameters;

        // Checking for valid minimum, maximum rules
        if (!(searchParameters.getMinRule() instanceof MinMaxRuleable)) {
            throw new IllegalArgumentException("This rule family does not support min / max rules!");
        }

        known = new HashSet<>();  // Hash set to store known things
        searchResults = new ArrayList<>(); // Initialise search results

        for (int i = 0; i < numRules; i++) {
            // Create a new simulator object each time
            simulator = new Simulator((Rule) searchParameters.getMinRule().clone());
            simulator.insertCells(searchParameters.getTargetPattern(), new Coordinate(0, 0));

            // Randomise the rule
            if (simulator.getRule() instanceof MinMaxRuleable) {
                ((MinMaxRuleable) simulator.getRule()).randomise(
                        searchParameters.getMinRule(), searchParameters.getMaxRule());
            }

            // Identify the object
            Pattern result = simulator.identify(searchParameters.getMaxPeriod(), grid -> {
                grid.updateBounds();
                return grid.getPopulation() < searchParameters.getMaxPop() &&
                        grid.getPopulation() > searchParameters.getMinPop() &&
                        (grid.getBounds().getValue1().getX() - grid.getBounds().getValue0().getX()) <
                                searchParameters.getMaxX() &&
                        (grid.getBounds().getValue1().getY() - grid.getBounds().getValue0().getY()) <
                                searchParameters.getMaxY();
            });

            if (result != null && !result.toString().equals("Still Life") &&
                    !known.contains(result)) {
                add(searchResults, result);
                add(known, result);  // To avoid duplicate speeds & whatnot
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
            FileWriter writer2 = new FileWriter(new File(file.getParent() + "/ships.db.txt"));
            FileWriter writer3 = new FileWriter(new File(file.getParent() + "/osc.db.txt"));

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
                if (pattern instanceof Spaceship) {  // Writing to the *.db.txt files
                    writer2.write(new GliderDBEntry((Spaceship) pattern, "", "").toString() + "\n");
                } else if (pattern instanceof Oscillator) {
                    writer3.write(new GliderDBEntry((Oscillator) pattern, "", "").toString() + "\n");
                }
            }

            // Close the file
            writer.close();
            writer2.close();
            writer3.close();
            return true;
        }
        catch (IOException exception) {
            LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).
                    log(Level.WARNING, exception.getMessage());
            return false;
        }
    }
}
