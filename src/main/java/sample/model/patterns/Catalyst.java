package sample.model.patterns;

import sample.model.Coordinate;
import sample.model.rules.Rule;
import sample.model.simulation.Grid;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a catalyst
 */
public class Catalyst extends Pattern {
    /**
     * Repeat time of the catalyst
     */
    private int repeatTime;

    /**
     * Is the catalyst a partial catalyst?
     */
    private boolean partial;

    /**
     * Contructs a catalyst
     * @param rule The rule the catalyst operates in
     * @param pattern The catalyst's pattern
     * @param repeatTime The catalyst's repeat time
     * @param partial Is the catalyst a partial catalyst (not all still lives interacted with are regenerated)
     */
    public Catalyst(Rule rule, Grid pattern, int repeatTime, boolean partial) {
        super(rule);

        this.partial = partial;
        this.repeatTime = repeatTime;
        this.insertCells(pattern, new Coordinate());
    }

    @Override
    public String toString() {
        if (partial) return "Partial catalyst of repeat time " + repeatTime;
        else return "Catalyst of repeat time " + repeatTime;
    }

    @Override
    public Map<String, String> additionalInfo() {
        LinkedHashMap<String, String> information = new LinkedHashMap<>();

        information.put("Repeat Time", repeatTime + "");

        if (minRule != null && maxRule != null) {
            information.put("Minimum Rule", "" + minRule);
            information.put("Maximum Rule", "" + maxRule);
        }

        return information;
    }
}
