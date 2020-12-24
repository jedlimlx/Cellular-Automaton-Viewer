package sample.model.database;

import org.javatuples.Quintet;
import sample.controller.SimulationMode;
import sample.model.Coordinate;
import sample.model.patterns.Oscillator;
import sample.model.patterns.Pattern;
import sample.model.patterns.Spaceship;
import sample.model.rules.MinMaxRuleable;
import sample.model.rules.RuleFamily;
import sample.model.simulation.Grid;
import sample.model.simulation.Simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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
        GliderDBEntry entry = null;
        boolean matchPeriod, matchSlope, matchRule;
        List<GliderDBEntry> entries = new ArrayList<>();

        Scanner scanner = new Scanner(this.file);
        while (scanner.hasNextLine()) {
            if (entry == null) entry = new GliderDBEntry(scanner.nextLine());
            else entry.setString(scanner.nextLine());

            ship = entry.getSpaceship();

            // Check various parameters
            matchPeriod = ship.getPeriod() == period || period == -1;
            matchSlope = (Math.abs(ship.getDisplacementX()) == Math.abs(dx) &&
                    Math.abs(ship.getDisplacementY()) == Math.abs(dy)) ||
                    (Math.abs(ship.getDisplacementX()) == Math.abs(dy) &&
                    Math.abs(ship.getDisplacementY()) == Math.abs(dx)) || (dx == -1 && dy == -1);
            matchRule = minRule == null || maxRule == null ||
                    ((MinMaxRuleable) ship.getMinRule()).betweenMinMax(minRule, maxRule) ||
                    ((MinMaxRuleable) ship.getMaxRule()).betweenMinMax(minRule, maxRule) ||
                    ((MinMaxRuleable) minRule).betweenMinMax(ship.getMinRule(), ship.getMaxRule()) ||
                    ((MinMaxRuleable) maxRule).betweenMinMax(ship.getMinRule(), ship.getMaxRule());

            // If it matches all conditions, add to the list of entries
            if (matchPeriod && matchSlope && matchRule)
                entries.add(new GliderDBEntry(entry.getSpaceship(), entry.getDiscoverer(), entry.getName()));
        }

        scanner.close();

        if (sorter != null) entries.sort(sorter);  // Sort the ships
        return entries;
    }

    /**
     * Canonises the database (removes duplicates)
     * @param newFile The new file for the database
     * @throws IOException Thrown when there is an issue reading or writing to the file
     */
    public void canoniseDB(File newFile) throws IOException {
        int alternatingPeriod;
        Pattern pattern;
        Simulator simulator;
        GliderDBEntry entry, entry2;
        Quintet<String, String, Integer, Integer, Integer> checkKnown;
        HashSet<Quintet<String, String, Integer, Integer, Integer>> known = new HashSet<>();

        FileWriter writer = new FileWriter(newFile);
        Scanner scanner = new Scanner(this.file);
        while (scanner.hasNextLine()) {
            entry = new GliderDBEntry(scanner.nextLine());
            if (entry.getOscillator() != null) {
                alternatingPeriod = entry.getOscillator().getRule().getAlternatingPeriod();
            } else {
                alternatingPeriod = entry.getSpaceship().getRule().getAlternatingPeriod();
            }

            for (int i = 0; i < alternatingPeriod; i++) {
                if (entry.getOscillator() != null) {
                    simulator = new Simulator(entry.getOscillator().getRule());
                    simulator.setGeneration(i);
                    simulator.insertCells(entry.getOscillator(), new Coordinate(0, 0));
                } else {
                    simulator = new Simulator(entry.getSpaceship().getRule());
                    simulator.setGeneration(i);
                    simulator.insertCells(entry.getSpaceship(), new Coordinate(0, 0));
                }

                pattern = simulator.identify(5000);

                if (pattern instanceof Oscillator) {
                    entry2 = new GliderDBEntry((Oscillator) pattern, entry.getDiscoverer(), entry.getName());
                    checkKnown = new Quintet<>(entry2.getOscillator().getMinRule().getRulestring(),
                            entry2.getOscillator().getMaxRule().getRulestring(),
                            entry2.getOscillator().getPeriod(), 0, 0);
                    if (known.contains(checkKnown)) {
                        System.out.println("Removed: " + entry2.toString());
                        break;
                    }

                    writer.write(entry2.toString() + "\n");
                    known.add(checkKnown);
                    break;
                } else if (pattern instanceof Spaceship) {
                    entry2 = new GliderDBEntry((Spaceship) pattern, entry.getDiscoverer(), entry.getName());
                    checkKnown = new Quintet<>(entry2.getSpaceship().getMinRule().getRulestring(),
                            entry2.getSpaceship().getMaxRule().getRulestring(),
                            entry2.getSpaceship().getPeriod(),
                            entry2.getSpaceship().getDisplacementX(),
                            entry2.getSpaceship().getDisplacementY());
                    if (known.contains(checkKnown)) {
                        System.out.println("Removed: " + entry2.toString());
                        break;
                    }

                    writer.write(entry2.toString() + "\n");

                    known.add(checkKnown);
                    known.add(new Quintet<>(entry2.getSpaceship().getMinRule().getRulestring(),
                            entry2.getSpaceship().getMaxRule().getRulestring(),
                            entry2.getSpaceship().getPeriod(),
                            -entry2.getSpaceship().getDisplacementX(),
                            entry2.getSpaceship().getDisplacementY()));
                    known.add(new Quintet<>(entry2.getSpaceship().getMinRule().getRulestring(),
                            entry2.getSpaceship().getMaxRule().getRulestring(),
                            entry2.getSpaceship().getPeriod(),
                            entry2.getSpaceship().getDisplacementX(),
                            -entry2.getSpaceship().getDisplacementY()));
                    known.add(new Quintet<>(entry2.getSpaceship().getMinRule().getRulestring(),
                            entry2.getSpaceship().getMaxRule().getRulestring(),
                            entry2.getSpaceship().getPeriod(),
                            -entry2.getSpaceship().getDisplacementX(),
                            -entry2.getSpaceship().getDisplacementY()));
                    break;
                }
            }
        }

        scanner.close();
        writer.close();
    }
}
