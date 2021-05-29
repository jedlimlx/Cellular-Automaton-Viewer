package application.model.patterns;

import application.model.Coordinate;
import application.model.rules.Rule;
import application.model.simulation.Grid;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a catalyst
 */
public class Catalyst extends Pattern {
    /**
     * Repeat time of the catalyst
     */
    private final int repeatTime;

    /**
     * Contructs a catalyst
     * @param rule The rule the catalyst operates in
     * @param pattern The catalyst's pattern
     * @param repeatTime The catalyst's repeat time
     */
    public Catalyst(Rule rule, Grid pattern, int repeatTime) {
        super(rule);

        this.name = "Catalysts";
        this.repeatTime = repeatTime;
        this.insertCells(pattern, new Coordinate());
    }

    @Override
    public String toString() {
        return "Catalyst of repeat time " + repeatTime;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Catalyst catalyst = (Catalyst) o;
        return repeatTime == catalyst.repeatTime &&
                this.hashCode() == catalyst.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), repeatTime);
    }
}
