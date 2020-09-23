package sample.model.rules;

import java.io.File;

/**
 * Rule families that support apgtable generation implement this interface
 */
public interface ApgtableGeneratable {
    /**
     * Generates an apgtable for apgsearch to use
     * @param file The file to save the apgtable in
     * @return True if the operation was successful, false otherwise
     */
    boolean generateApgtable(File file);
}
