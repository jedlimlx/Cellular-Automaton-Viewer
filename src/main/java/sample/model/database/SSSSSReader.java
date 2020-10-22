package sample.model.database;

import sample.model.patterns.Spaceship;
import sample.model.rules.hrot.HROT;
import sample.model.simulation.Grid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Reads the Smallest Spaceship Supporting Specific Speeds (5S) database.
 * Assumes the database is sorted by period in ascending order and displacement by descending order. <br>
 * Example Usage: <br>
 * <br>
 * <pre>
 * SSSSSReader reader = new SSSSSReader(new File("5s.sss.txt"))
 *
 * // Will probably return NullPointerException until that elusive ship is found
 * System.out.println(Utils.fullRLE(reader.getShipBySpeed("(7, 1)c/8")))
 * </pre>
 */
public class SSSSSReader extends DatabaseReader {
    private final File file;
    private final List<String> lines;

    /**
     * Constructs the reader
     * @param file The file where the database is stored
     * @throws IOException Thrown if there is an IOException while reading the file
     */
    public SSSSSReader(File file) throws IOException {
        super(file);
        this.file = file;
        this.lines = Files.readAllLines(Path.of(file.getPath()), StandardCharsets.US_ASCII);
    }

    /**
     * Gets an spaceship in the database by its speed / velocity
     * Uses binary search for speed.
     * @param period The period of the spaceship
     * @param dx The x displacement of the spaceship
     * @param dy The y displacement of the spaceship
     * @return Returns the spaceship speed
     */
    public Spaceship getShipBySpeed(int period, int dx, int dy) {
        SSSSpaceship ship = new SSSSpaceship(new Spaceship(new HROT(), new Grid(), period, dx, dy));

        // Binary Search
        int start = 0, end = lines.size() - 1, middle, compared;

        while (end >= start) {
            middle = (start + end + 1) / 2;
            compared = new SSSSpaceship(lines.get(middle)).compareTo(ship);
            if (compared == 0) {  // Found ship
                return new SSSSpaceship(lines.get(middle)).getSpaceship();
            } else if (compared > 0) {  // Too high
                end = middle - 1;
            } else {  // Too low
                start = middle + 1;
            }
        }

        return null;
    }
}
