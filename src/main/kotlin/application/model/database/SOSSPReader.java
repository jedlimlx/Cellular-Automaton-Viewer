package application.model.database;

import application.model.patterns.Oscillator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads the Smallest Oscillator Supporting Specfic Periods (SOSSP) database.
 * Assumes the database is sorted by period. <br>
 * Example Usage: <br>
 * <br>
 * <pre>
 * SOSSPReader reader = new SOSSPReader(new File("sossp.sss.txt"))
 * System.out.println(Utils.fullRLE(reader.getOscByPeriod(2047)))
 * </pre>
 */
public class SOSSPReader extends DatabaseReader {
    private final File file;
    private final List<String> lines;

    /**
     * Constructs the reader
     * @param file The file where the database is stored
     * @throws IOException Thrown if there is an IOException while reading the file
     */
    public SOSSPReader(File file) throws IOException {
        super(file);
        this.file = file;
        this.lines = Files.readAllLines(Path.of(file.getPath()), StandardCharsets.US_ASCII);
    }

    /**
     * Gets an oscillator in the database by its period.
     * Uses binary search for speed.
     * @param period The period of the oscillator
     * @return Returns the oscillator
     */
    public Oscillator getOscByPeriod(int period) {
        // Binary Search
        int start = 0, end = lines.size() - 1, middle, compared;

        while (end >= start) {
            middle = (start + end + 1) / 2;
            compared = Integer.compare(new SSSOscillator(lines.get(middle)).getOscillator().getPeriod(), period);
            if (compared == 0) {  // Found oscillator
                return new SSSOscillator(lines.get(middle)).getOscillator();
            } else if (compared > 0) {  // Too high
                end = middle - 1;
            } else {  // Too low
                start = middle + 1;
            }
        }

        return null;
    }
}
