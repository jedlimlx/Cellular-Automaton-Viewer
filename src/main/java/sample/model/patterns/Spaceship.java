package sample.model.patterns;

import sample.model.Coordinate;
import sample.model.rules.Rule;
import sample.model.simulation.Grid;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a spaceship
 */
public class Spaceship extends Pattern {
    private int period, displacementX, displacementY;

    /**
     * Constructs a spaceship
     * @param rule The rule the spaceship works in
     * @param pattern The pattern of the spaceship
     * @param period The period of the spaceship
     * @param displacementX The horizontal displacement of the spaceship
     * @param displacementY The vertical displacement of the spaceship
     */
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

    /**
     * Additional information about the spaceship.
     * Namely the period, horizontal and vertical displacement and the minimum and maximum rule.
     * @return Returns a map containing the additional information about the spaceship
     */
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

    /**
     * Gets the period of the spaceship
     * @return Returns the period of the spaceship
     */
    public int getPeriod() {
        return period;
    }

    /**
     * Gets the displacement of the spaceship in the x-direction
     * @return Returns the displacement of the spaceship in the x-direction
     */
    public int getDisplacementX() {
        return displacementX;
    }

    /**
     * Gets the displacement of the spaceship in the y-direction
     * @return Returns the displacement of the spaceship in the y-direction
     */
    public int getDisplacementY() {
        return displacementY;
    }
}
