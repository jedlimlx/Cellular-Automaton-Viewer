package sample.model.search;

import sample.model.Grid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SearchProgram {
    protected int numSearched;  // Number of whatever searched
    protected ExecutorService executor;  // Stores the threads running the search
    protected SearchParameters searchParameters;  // The parameters for the search
    protected ArrayList<Grid> searchResults;  // Store search results here

    public SearchProgram(SearchParameters parameters) {
        searchParameters = parameters;
    }

    // Use these methods to avoid ConcurrentModificationException when multithreading
    public void add(List list, Object object) {
        // To avoid race conditions and ConcurrentModificationException
        synchronized (this) {
            list.add(object);
        }
    }

    public void add(Set set, Object object) {
        // To avoid race conditions and ConcurrentModificationException
        synchronized (this) {
            set.add(object);
        }
    }

    // Search for something num times
    public abstract void search(int num);

    public void searchThreaded(int num, int numThreads) {
        executor = Executors.newFixedThreadPool(numThreads);

        // Split the load equally among the threads
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> search(num / numThreads));
        }
    }

    // Terminates the search process
    public void terminateSearch() {
        executor.shutdownNow();
    }

    // Accessors
    public ArrayList<Grid> getSearchResults() {
        return searchResults;
    }

    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

    public int getNumSearched() {
        return numSearched;
    }

    // Writes the search results to a file
    // Returns true if successful, false if unsuccessful
    public abstract boolean writeToFile(File file);
}
