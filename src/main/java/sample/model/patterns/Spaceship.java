package sample.model.patterns;

import sample.model.Coordinate;
import sample.model.Grid;
import sample.model.rules.Rule;

import java.util.LinkedHashMap;
import java.util.Map;

public class Spaceship extends Pattern {
    int period, displacementX, displacementY;
    public Spaceship(Rule rule, Grid pattern, int period, int displacementX, int displacementY) {
        super(rule);

        this.period = period;
        this.displacementX = displacementX;
        this.displacementY = displacementY;
        this.insertCells(pattern, new Coordinate(0, 0));
    }

    @Override
    public String toString() {
        return "(" + displacementX + "," + displacementY + ")c/" + period;
    }

    @Override
    public Map<String, String> additionalInfo() {
        LinkedHashMap<String, String> information = new LinkedHashMap<>();
        information.put("Period", "" + period);
        information.put("Displacement X", "" + displacementX);
        information.put("Displacement Y", "" + displacementY);

        if (minRule != null && maxRule != null) {
            information.put("Minimum Rule", "" + minRule);
            information.put("Maximum Rule", "" + maxRule);
        }

        return information;
    }
}
