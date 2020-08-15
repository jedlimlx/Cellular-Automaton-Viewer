package sample.model.patterns;

import sample.model.Coordinate;
import sample.model.Grid;
import sample.model.rules.Rule;

import java.util.LinkedHashMap;
import java.util.Map;

public class Oscillator extends Pattern {
    int period;
    public Oscillator(Rule rule, Grid pattern, int period) {
        super(rule);

        this.period = period;
        this.insertCells(pattern, new Coordinate(0, 0));
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
}
