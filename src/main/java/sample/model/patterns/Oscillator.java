package sample.model.patterns;

import sample.model.Coordinate;
import sample.model.rules.Rule;
import sample.model.simulation.Grid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
     * Number of rotor cells in the oscillator
     */
    private int rotorCells;

    /**
     * Number of stator cells in the oscillator
     */
    private int statorCells;

    /**
     * The heat of the oscillator
     */
    private double heat;

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
    public void setPhases(Grid[] grids) {
        super.setPhases(grids);

        // Finding smallest phase of oscillator
        int minPop = Integer.MAX_VALUE;
        Grid minPhase = new Grid();

        Map<Coordinate, Boolean> changed = new HashMap<>();

        phases = new Grid[grids.length];
        for (int i = 0; i < grids.length; i++) {
            phases[i] = grids[i].deepCopy();
            if (grids[i].getPopulation() < minPop && grids[i].getBackground() == 0) minPhase = phases[i];

            // Calculating heat, stator cells, rotor cells
            if (i > 0) {
                int finalI = i;
                grids[i].iterateCells(cell -> {
                    if (grids[finalI].getCell(cell) != grids[finalI - 1].getCell(cell)) heat++;
                    if (changed.get(cell) == null || !changed.get(cell))
                        changed.put(cell, grids[finalI].getCell(cell) != grids[finalI - 1].getCell(cell));
                });

                grids[i - 1].iterateCells(cell -> {
                    if (grids[finalI].getCell(cell) == 0 &&
                            grids[finalI].getCell(cell) != grids[finalI - 1].getCell(cell)) heat++;
                });
            }
            else {
                grids[i].iterateCells(cell -> {
                    if (grids[0].getCell(cell) != grids[grids.length - 1].getCell(cell)) heat++;
                    if (changed.get(cell) == null || !changed.get(cell))
                        changed.put(cell, grids[0].getCell(cell) != grids[grids.length - 1].getCell(cell));
                });

                grids[grids.length - 1].iterateCells(cell -> {
                    if (grids[0].getCell(cell) == 0 &&
                            grids[0].getCell(cell) != grids[grids.length - 1].getCell(cell)) heat++;
                });
            }
        }

        this.clearCells();
        this.insertCells(minPhase, new Coordinate(0, 0));

        // Counting cells
        rotorCells = 0;
        statorCells = 0;
        for (Coordinate cell: changed.keySet()) {
            if (changed.get(cell)) rotorCells++;
            else statorCells++;
        }

        heat /= period;
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

        information.put("Heat", String.format("%.2f", heat));
        information.put("Temperature", String.format("%.2f | %.2f",
                heat / (rotorCells + statorCells), heat / rotorCells));
        information.put("Volatility", String.format("%.2f", (double) rotorCells / (rotorCells + statorCells)));
        information.put("Active Cells", rotorCells + " | " + statorCells + " | " + (rotorCells + statorCells));

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

    @Override
    public int hashCode() {
        return 31 * period + Arrays.hashCode(phases);
    }

    /**
     * Returns the period of the oscillator
     * @return Returns the period of the oscillator
     */
    public int getPeriod() {
        return period;
    }
}
