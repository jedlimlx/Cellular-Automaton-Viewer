package sample.model.patterns;

import sample.model.Coordinate;
import sample.model.rules.Rule;
import sample.model.simulation.Grid;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an oscillator
 */
public class Oscillator extends Pattern {
    /**
     * The period of the oscillator
     */
    private final int period;

    /**
     * Phases of the oscillator
     */
    private Grid[] phases;

    /**
     * Constructs an oscillator
     * @param rule The rule the oscillator works in
     * @param pattern The oscillator's pattern
     * @param period The period of the oscillator
     */
    public Oscillator(Rule rule, Grid pattern, int period) {
        super(rule);

        this.period = period;
        this.insertCells(pattern, new Coordinate(0, 0));
    }

    @Override
    public void generateMinMaxRule(Grid[] grids) {
        super.generateMinMaxRule(grids);

        phases = new Grid[grids.length];
        for (int i = 0; i < grids.length; i++) phases[i] = grids[i].deepCopy();
    }

    @Override
    public String toString() {
        if (period != 1) {
            return "P" + period + " Oscillator";
        }
        else {
            return "Still Life";
        }
    }

    /**
     * Additional information about the oscillator. Namely the period, minimum rule and maximum rule
     * @return A map containing the additional information about the oscillator
     */
    @Override
    public Map<String, String> additionalInfo() {
        LinkedHashMap<String, String> information = new LinkedHashMap<>();
        information.put("Period", "" + period);

        if (minRule != null && maxRule != null) {
            information.put("Minimum Rule", "" + minRule);
            information.put("Maximum Rule", "" + maxRule);
        }

        return information;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Oscillator that = (Oscillator) o;
        return period == that.period &&
                Arrays.equals(phases, that.phases);
    }

    /**
     * Returns the period of the oscillator
     * @return Returns the period of the oscillator
     */
    public int getPeriod() {
        return period;
    }
}
