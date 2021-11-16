package application.model.search.ocgar2;

import application.model.SymmetryGenerator;
import application.model.patterns.Oscillator;
import application.model.patterns.Pattern;
import application.model.patterns.Spaceship;
import application.model.search.SearchProgram;
import application.model.simulation.Simulator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Implements CAViewer's agar search program - OCAgar2
 */
public class AgarSearch extends SearchProgram {
    private HashSet<ArrayList<ArrayList<Integer>>> known;

    /**
     * Constructs the agar search program with the provided parameters
     * @param parameters The parameters of the agar search program
     */
    public AgarSearch(AgarSearchParameters parameters) {
        super(parameters);
    }

    @Override
    public void search(int num) {
        Pattern result;
        Simulator simulator;
        AgarSearchParameters searchParameters = (AgarSearchParameters) this.searchParameters;

        known = new HashSet<>();  // Hash set to store known things
        searchResults = new ArrayList<>(); // Initialise search results

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < num; i++) {
            simulator = new Simulator(searchParameters.getRule());
            simulator.insertCells(SymmetryGenerator.generateC1(50, new int[]{1},
                    searchParameters.getRule().getBoundedGrid().getWidth(),
                    searchParameters.getRule().getBoundedGrid().getHeight()),
                    searchParameters.getRule().getBoundedGrid().getInitialCoordinate());
            result = simulator.identify(searchParameters.getMaxPeriod());

            if (result instanceof Spaceship) {
                if (!known.contains(((Spaceship) result).getPopulationSequence())) {
                    add(searchResults, result);
                    add(known, ((Spaceship) result).getPopulationSequence());
                }
            } else if (result instanceof Oscillator) {
                if (!known.contains(((Oscillator) result).getPopulationSequence())) {
                    add(searchResults, result);
                    add(known, ((Oscillator) result).getPopulationSequence());
                }
            }

            synchronized (this) {  // To avoid race conditions
                if (numSearched % 5000 == 0 && numSearched != 0) {
                    System.out.println(numSearched + " torus searched (" +
                            5000000 / (System.currentTimeMillis() - startTime) +
                            " rules/s), " + searchResults.size() + " objects found!");
                    startTime = System.currentTimeMillis();
                }

                numSearched++;
            }
        }
    }

    @Override
    public boolean writeToFile(File file) {
        return false;
    }
}
