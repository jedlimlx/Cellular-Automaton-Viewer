package application.model.search

import application.model.patterns.Pattern
import application.model.search.SearchParameters
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.lang.Runnable
import java.util.ArrayList

/**
 * All search programs built into CAViewer should inherit from this
 */
abstract class SearchProgram(searchParameters: SearchParameters) {
    // The search paramemeters
    var searchParameters: SearchParameters = searchParameters
        protected set

    // Number of whatever searched
    var numSearched = 0
        protected set

    // Stores the threads running the search
    protected lateinit var executor: ExecutorService

    /**
     * Gets the search results
     * @return Returns the search results
     */
    var searchResults: ArrayList<Pattern> = arrayListOf() // Store search results here
        protected set
    protected var stop = false // Tell the search program to stop

    /**
     * Adds an object to a list (thread-safe)
     * @param list The list to add the object to
     * @param object The object to be added
     * @param <T> The type of the object
    </T> */
    fun <T> add(list: MutableList<T>, `object`: T) {
        // To avoid race conditions and ConcurrentModificationException
        synchronized(this) { list.add(`object`) }
    }

    /**
     * Adds an object to a set (thread-safe)
     * @param set The set to add the object to
     * @param object The object to be added
     * @param <T> The type of the object
    </T> */
    fun <T> add(set: MutableSet<T>, `object`: T) {
        // To avoid race conditions and ConcurrentModificationException
        synchronized(this) { set.add(`object`) }
    }

    /**
     * Search for something num times
     * @param num The number of times to search for something
     */
    abstract fun search(num: Int)

    /**
     * Performs the search but with *multi-threading*
     * @param num The number of times to search for something
     * @param numThreads The number of threads to use in the search
     */
    open fun searchThreaded(num: Int, numThreads: Int) {
        executor = Executors.newFixedThreadPool(numThreads)

        // Split the load equally among the threads
        for (i in 0 until numThreads)
            executor.submit { search(num / numThreads) }
    }

    /**
     * Terminates the search process
     */
    fun terminateSearch() {
        executor.shutdownNow()
        stop = true
    }

    /**
     * Writes the search results to a file
     * @param file The file to write the search results to
     * @return Returns true if successful, false if unsuccessful
     */
    abstract fun writeToFile(file: File): Boolean
}