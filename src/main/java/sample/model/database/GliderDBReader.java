package sample.model.database;

import sample.model.patterns.Spaceship;
import sample.model.rules.MinMaxRuleable;
import sample.model.rules.RuleFamily;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Reads the OT / Generations / HROT / whatever glider database. <br>
 * <br>
 * Example Usage: <br> <pre>
 * GliderDBReader reader = new GliderDBReader("new-gliders.db.txt");
 * System.out.println(reader.getEntries(-1, 1, 2, null, null, null));
 * </pre>
 */
public class GliderDBReader extends DatabaseReader {
    private final File file;

    /**
     * Constructs the database from a file
     * @param file The file containing the database
     */
    public GliderDBReader(File file) {
        super(file);
        this.file = file;
    }

    /**
     * Gets entries from the glider db
     * @param period The period to look for
     * @param dx The displacement x to look for
     * @param dy The displacement y to look for
     * @param minRule The min rule
     * @param maxRule The max rule
     * @param sorter How should the entries be sorted?
     * @return Returns the entries in a list
     * @throws FileNotFoundException Thrown when the database file cannot be found
     */
    public List<GliderDBEntry> getEntries(int period, int dx, int dy,
                                          RuleFamily minRule, RuleFamily maxRule,
                                          Comparator<GliderDBEntry> sorter) throws FileNotFoundException {
        Spaceship ship;
        GliderDBEntry entry;
        boolean matchPeriod, matchSlope, matchRule;
        List<GliderDBEntry> entries = new ArrayList<>();

        Scanner scanner = new Scanner(this.file);
        while (scanner.hasNextLine()) {
            entry = new GliderDBEntry(scanner.nextLine());
            ship = entry.getSpaceship();

            matchPeriod = ship.getPeriod() == period || period == -1;
            matchSlope = (Math.abs(ship.getDisplacementX()) == Math.abs(dx) &&
                    Math.abs(ship.getDisplacementY()) == Math.abs(dy)) ||
                    (Math.abs(ship.getDisplacementX()) == Math.abs(dy) &&
                    Math.abs(ship.getDisplacementY()) == Math.abs(dx)) || (dx == -1 && dy == -1);
            matchRule = (minRule == null && maxRule == null) ||
                    ((MinMaxRuleable) ship.getRule()).betweenMinMax(minRule, maxRule);

            // If it matches all conditions, add to the list of entries
            if (matchPeriod && matchSlope && matchRule) entries.add(entry);
        }

        if (sorter != null) entries.sort(sorter);  // Sort the ships
        return entries;
    }
}
