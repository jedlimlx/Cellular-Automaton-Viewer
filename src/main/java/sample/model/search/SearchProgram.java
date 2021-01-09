package sample.model.search;

import sample.model.patterns.Pattern;
import sample.model.simulation.Grid;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * All search programs built into CAViewer should inherit from this
 */
public abstract class SearchProgram {
    protected int numSearched;  // Number of whatever searched
    protected ExecutorService executor;  // Stores the threads running the search
    protected SearchParameters searchParameters;  // The parameters for the search
    protected ArrayList<Pattern> searchResults;  // Store search results here

    protected boolean stop = false;  // Tell the search program to stop

    /**
     * Constructs a search program with the provided parameters
     * @param parameters The parameters of the search program
     */
    public SearchProgram(SearchParameters parameters) {
        searchParameters = parameters;
    }

    /**
     * Adds an object to a list (thread-safe)
     * @param list The list to add the object to
     * @param object The object to be added
     * @param <T> The type of the object
     */
    public <T> void add(List<T> list, T object) {
        // To avoid race conditions and ConcurrentModificationException
        synchronized (this) {
            list.add(object);
        }
    }

    /**
     * Adds an object to a set (thread-safe)
     * @param set The set to add the object to
     * @param object The object to be added
     * @param <T> The type of the object
     */
    public <T> void add(Set<T> set, T object) {
        // To avoid race conditions and ConcurrentModificationException
        synchronized (this) {
            set.add(object);
        }
    }

    /**
     * Search for something num times
     * @param num The number of times to search for something
     */
    public abstract void search(int num);

    /**
     * Performs the search but with <i>multi-threading</i>
     * @param num The number of times to search for something
     * @param numThreads The number of threads to use in the search
     */
    public void searchThreaded(int num, int numThreads) {
        executor = Executors.newFixedThreadPool(numThreads);

        // Split the load equally among the threads
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> search(num / numThreads));
        }
    }

    /**
     * Terminates the search process
     */
    public void terminateSearch() {
        executor.shutdownNow();
        stop = true;
    }

    /**
     * Gets the search results
     * @return Returns the search results
     */
    public ArrayList<Pattern> getSearchResults() {
        return searchResults;
    }

    /**
     * Gets the search parameters
     * @return Returns the search parameters
     */
    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

    /**
     * Gets the number of objects searched
     * @return Returns the number of objects searched
     */
    public int getNumSearched() {
        return numSearched;
    }

    /**
     * Writes the search results to a file
     * @param file The file to write the search results to
     * @return Returns true if successful, false if unsuccessful
     */
    public abstract boolean writeToFile(File file);
}
