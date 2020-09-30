package sample.model.patterns;

import sample.model.Coordinate;
import sample.model.rules.Rule;
import sample.model.simulation.Grid;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a linear growth
 */
public class LinearGrowth extends Pattern {
    private int popPeriod;

    /**
     * Constructs a linear growth
     * @param rule The rule that the pattern works in
     * @param pattern The linear growth
     * @param popPeriod The population period of the linear growth
     */
    public LinearGrowth(Rule rule, Grid pattern, int popPeriod) {
        super(rule);

        this.popPeriod = popPeriod;
        this.insertCells(pattern, new Coordinate(0, 0));
    }

    @Override
    public String toString() {
        return "Linear Growth";
    }

    @Override
    public Map<String, String> additionalInfo() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("Population Period", popPeriod + "");

        if (minRule != null && maxRule != null) {
            map.put("Minimum Rule", minRule.getRulestring());
            map.put("Maximum Rule", maxRule.getRulestring());
        }

        return map;
    }

    /**
     * Gets the period of an interleaving of degree-d polynomials.
     * Totally not stolen from apgsearch 1.x.
     * @param sequence The sequence of values of the polynomial
     * @param maxPeriod The max period of the polynomial
     * @param degree The degree of the polynomial
     * @return Returns the period of the polynomial (-1 for no period)
     */
    public static int deepPeriod(int[] sequence, int maxPeriod, int degree) {
        boolean good;
        int[] difference;
        for (int p = 1; p < maxPeriod; p++) {
            good = true;
            difference = new int[degree + 2];
            for (int i = 0; i < maxPeriod; i++) {
                for (int j = 0; j < degree + 2; j++) {
                    difference[j] = sequence[i + j * p];
                }

                // Produce successive differences
                for (int j = 0; j < degree + 1; j++) {
                    for (int k = 0; k < degree + 1; k++) {
                        difference[k] = difference[k] - difference[k + 1];
                    }
                }

                if (difference[0] != 0) {
                    good = false;
                    break;
                }
            }

            if (good) return p;
        }

        return -1;
    }
}
