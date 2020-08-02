package sample.model.patterns;

import sample.model.Coordinate;
import sample.model.Grid;
import sample.model.rules.Rule;

import java.util.HashMap;

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
    public HashMap<String, String> additionalInfo() {
        HashMap<String, String> information = new HashMap<>();
        information.put("Period", "" + period);

        return information;
    }
}
