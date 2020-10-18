package sample.model.search;

import sample.model.Coordinate;
import sample.model.rules.Rule;
import sample.model.simulation.Grid;

import java.util.ArrayList;
import java.util.List;

public class CatalystSearchParameters extends SearchParameters {
    private final Grid target;
    private final List<Grid> catalysts;
    private final List<Coordinate> coordinateList;

    private final boolean bruteForce;
    private final int maxRepeatTime;
    private final int numCatalysts;

    private final Rule rule;

    public CatalystSearchParameters(int maxRepeatTime, int numCatalysts, boolean bruteForce,
                                    List<Grid> catalysts, Grid target,
                                    List<Coordinate> coordinateList, Rule rule) {
        this.maxRepeatTime = maxRepeatTime;
        this.numCatalysts = numCatalysts;
        this.bruteForce = bruteForce;
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
