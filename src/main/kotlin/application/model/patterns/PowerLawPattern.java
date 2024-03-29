package application.model.patterns;

import org.javatuples.Pair;
import application.model.Coordinate;
import application.model.rules.Rule;
import application.model.simulation.Grid;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a pattern whose average population follows a power law.
 * Essentially apgsearch's zz_REPLICATOR, zz_LINEAR, zz_EXPLOSIVE and zz_QUADRATIC.
 */
public class PowerLawPattern extends Pattern {
    private final double power;

    /**
     * Constructs a power law pattern with the specified growth rate
     * @param rule The rule that the pattern works in
     * @param pattern The power law pattern
     * @param power The pattern's growth rate
     */
    public PowerLawPattern(Rule rule, Grid pattern, double power) {
        super(rule);

        this.power = power;
        this.name = "Chaotic Growth";
        this.insertCells(pattern, new Coordinate(0, 0));
    }

    @Override
    public String toString() {
        if (power < 1.65)
            return "zz_REPLICATOR";
        else if (power < 2.05)
            return "zz_LINEAR";
        else if (power < 2.8)
            return "zz_EXPLOSIVE";
        else
            return "zz_QUADRATIC";
    }

    @Override
    public Map<String, String> additionalInfo() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        map.put("Power", power + "");
        if (minRule != null && maxRule != null) {
            map.put("Minimum Rule", minRule.getRulestring());
            map.put("Maximum Rule", maxRule.getRulestring());
        }

        return map;
    }

    /**
     * Finds the gradient of a list of points.
     * Totally not stolen from apgsearch 1.x.
     * @param pointList The list of points
     * @return Returns the gradient of the list of points
     */
    public static double regress(ArrayList<Pair<Double, Double>> pointList) {
        double cumX = 0, cumY = 0, cumVar = 0, cumCov = 0; // id
        for (var point: pointList) {
            cumX += point.getValue0();
            cumY += point.getValue1();
        }

        cumX = cumX / pointList.size();
        cumY = cumY / pointList.size();

        for (var point: pointList) {
            cumVar += (point.getValue0() - cumX) * (point.getValue0() - cumX);
            cumCov += (point.getValue1() - cumY) * (point.getValue0() - cumX);
        }

        return cumCov / cumVar;
    }
}
