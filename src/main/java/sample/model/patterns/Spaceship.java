package sample.model.patterns;

import sample.model.Coordinate;
import sample.model.Grid;
import sample.model.rules.Rule;

import java.util.HashMap;

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
    public HashMap<String, String> additionalInfo() {
        HashMap<String, String> information = new HashMap<>();
        information.put("Period", "" + period);

        return information;
    }
}
