package sample.model.database;

import java.io.File;

/**
 * Reads a database and stores the information
 */
public abstract class DatabaseReader {
    /**
     * Constructs the database from a file
     * @param database The file containing the database
     */
    public DatabaseReader(File database) {
    }
}
