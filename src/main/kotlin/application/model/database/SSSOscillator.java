package application.model.database;

import application.model.Utils;
import application.model.patterns.Oscillator;
import application.model.rules.RuleFamily;
import application.model.simulation.Grid;

/**
 * Represents an oscillator from the Smallest Oscillators Supporting Specific Periods project.
 */
public class SSSOscillator extends DatabaseEntry {
    private final Oscillator oscillator;

    public SSSOscillator(String entry) {
        super(entry);

        String[] tokens = entry.split(", \\s*");
        oscillator = new Oscillator(Utils.fromRulestring(tokens[1]), new Grid(tokens[5]),
                Integer.parseInt(tokens[4]));
    }

    public SSSOscillator(Oscillator oscillator) {
        super(oscillator);
        this.oscillator = oscillator;
    }

    @Override
    public String toString() {
        return oscillator.getPopulation() + ", " + ((RuleFamily) oscillator.getRule()).getRulestring() +
                ", 0, 0, " + oscillator.getPeriod() + ", " + oscillator.toRLE();
    }

    public Oscillator getOscillator() {
        return oscillator;
    }
}
