package sample.model.database;

import sample.model.patterns.Pattern;

/**
 * An entry in a database
 */
public abstract class DatabaseEntry {
    /**
     * Constructs the entry with the given entry
     * @param entry The entry
     */
    public DatabaseEntry(String entry) {

    }

    /**
     * Constructs the entry with the given pattern
     * @param pattern The pattern
     */
    public DatabaseEntry(Pattern pattern) {

    }

    public abstract String toString();
}
