package application.model.search.catsrc;

import application.model.Coordinate;
import application.model.rules.Rule;
import application.model.search.SearchParameters;
import application.model.simulation.Grid;

import java.util.List;

public class CatalystSearchParameters extends SearchParameters {
    private final Grid target;
    private final List<Grid> catalysts;
    private final List<Coordinate> coordinateList;

    private final boolean bruteForce;
    private final boolean rotateCatalyst;
    private final boolean flipCatalyst;

    private final int maxRepeatTime;
    private final int numCatalysts;

    private final Rule rule;

    public CatalystSearchParameters(int maxRepeatTime, int numCatalysts, boolean bruteForce,
                                    boolean rotateCatalyst, boolean flipCatalyst, List<Grid> catalysts, Grid target,
                                    List<Coordinate> coordinateList, Rule rule) {
        this.maxRepeatTime = maxRepeatTime;
        this.numCatalysts = numCatalysts;
        this.bruteForce = bruteForce;
        this.rotateCatalyst = rotateCatalyst;
        this.flipCatalyst = flipCatalyst;
        this.catalysts = catalysts;
        this.target = target;
        this.coordinateList = coordinateList;
        this.rule = rule;
    }

    public int getMaxRepeatTime() {
        return maxRepeatTime;
    }

    public int getNumCatalysts() {
        return numCatalysts;
    }

    public boolean getBruteForce() {
        return bruteForce;
    }

    public boolean getRotateCatalyst() {
        return rotateCatalyst;
    }

    public boolean getFlipCatalyst() {
        return flipCatalyst;
    }

    public List<Grid> getCatalysts() {
        return catalysts;
    }

    public Grid getTarget() {
        return target;
    }

    public List<Coordinate> getCoordinateList() {
        return coordinateList;
    }

    public Rule getRule() {
        return rule;
    }
}
