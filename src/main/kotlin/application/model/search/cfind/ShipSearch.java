package application.model.search.cfind;

import application.model.Coordinate;
import application.model.search.SearchProgram;

import java.io.File;

/**
 * Implements CAViewer's gfind-like ship search program - cfind for multi-state and higher range rules.
 */
public class ShipSearch extends SearchProgram {
    /**
     * Constructs the ship search program with the provided parameters
     * @param parameters The parameters of the search program
     */
    public ShipSearch(ShipSearchParameters parameters) {
        super(parameters);
    }

    @Override
    public void search(int num) {
        searchThreaded(num, 1);
    }

    @Override
    public void searchThreaded(int num, int numThreads) {
        ShipSearchParameters searchParameters = (ShipSearchParameters) getSearchParameters();

        // Getting the neighbourhood range
        int range = 0;
        for (Coordinate coordinate: searchParameters.getRule().getNeighbourhood()) {
            range = Math.max(range, Math.max(coordinate.getX(), coordinate.getY()));
        }

        // Maximum BFS queue size
        int maxQueueSize = (int) Math.pow(2, 21);
    }

    @Override
    public boolean writeToFile(File file) {
        return false;
    }
}
