package sample;

import sample.model.Coordinate;
import sample.model.rules.hrot.HROT;
import sample.model.rules.hrot.IntegerHROT;
import sample.model.rules.ruleloader.RuleLoader;
import sample.model.search.CatalystSearch;
import sample.model.search.CatalystSearchParameters;
import sample.model.simulation.Grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        ArrayList<Grid> catalysts = new ArrayList<>();
        for (String RLE: new String[]{"4b2o$5bo$4bo$3bo$obo$2o!", "b2o$obo$bo!", "2o$bo$bobo$2b2o!",
                "2o$bo$bobo$2bobo$3bo!", "2o$obo$2bo$bo$b2o!", "bo$obo$obo$bo!", "2o$2o!", "A2.$.2B$.2B$!"}) {
            Grid grid = new Grid();
            grid.fromRLE(RLE, new Coordinate());

            catalysts.add(grid);
        }

        Grid target = new Grid();
        target.fromRLE(".3A$A.B$A3B$A3.A!", new Coordinate(0, 0));

        List<Coordinate> coordinateList = new ArrayList<>();
        for (int i = -25; i < -5; i++) {
            for (int j = -20; j < 20; j++) {
                coordinateList.add(new Coordinate(i, j));
            }
        }

        CatalystSearchParameters parameters = new CatalystSearchParameters(500, 4,
                false, new ArrayList<>(catalysts), target,
                coordinateList, new IntegerHROT("R1,I8,S2-3,B3,NM"));
        CatalystSearch search = new CatalystSearch(parameters);

        search.search(5000);
    }
}
