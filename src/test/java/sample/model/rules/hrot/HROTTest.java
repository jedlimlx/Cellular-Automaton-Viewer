package sample.model.rules.hrot;

import org.junit.Test;
import sample.model.Coordinate;
import sample.model.SymmetryGenerator;
import sample.model.rules.ruleloader.RuleDirective;
import sample.model.rules.ruleloader.RuleLoader;
import sample.model.simulation.Grid;
import sample.model.simulation.Simulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class HROTTest {
    public InputStream getStream(String resourcePath) {
        return getClass().getResourceAsStream(resourcePath);
    }

    @Test
    public void testFromRulestring() {
        // Loading the testcases
        Scanner scanner = new Scanner(getStream("/HROT/parsingTest1.txt"));

        // Run through them
        String rulestring = "";
        HashSet<Integer> birth = new HashSet<>(), survival = new HashSet<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#R")) {
                // Loading rulestring
                rulestring = line.substring(3);
            }
            else if (line.startsWith("#B")) {
                // Loading birth conditions
                String[] tokens = new String[0];
                if (line.length() > 2) {
                    String withoutHeader = line.substring(3);
                    tokens = withoutHeader.split(",");
                }

                birth = new HashSet<>();
                for (String token : tokens) {
                    birth.add(Integer.parseInt(token));
                }
            }
            else if (line.startsWith("#S")) {
                // Loading survival conditions
                String[] tokens = new String[0];
                if (line.length() > 2) {
                    String withoutHeader = line.substring(3);
                    tokens = withoutHeader.split(",");

                }

                survival = new HashSet<>();
                for (String token: tokens) {
                    survival.add(Integer.parseInt(token));
                }
            }
            else if (!line.startsWith("#")){
                // Running the testcase
                HROT hrot = new HROT(rulestring);
                assertEquals(birth, hrot.getBirth());
                assertEquals(survival, hrot.getSurvival());
            }
        }
    }

    @Test
    public void testCanonise() {
        // Loading the testcases
        Scanner scanner = new Scanner(getStream("/HROT/parsingTest1.txt"));

        // Run through them
        String rulestring = "", canonisedRulestring = "";

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#R")) {
                // Loading rulestring
                rulestring = line.substring(3);
            }
            else if (line.startsWith("#C")) {
                // Loading canonised rulestring
                canonisedRulestring = line.substring(3);
            }
            else if (!line.startsWith("#")){
                // Running the testcase
                HROT hrot = new HROT(rulestring);
                assertEquals(canonisedRulestring, hrot.getRulestring());
            }
        }
    }

    @Test
    public void testGenerateComments() {
        // Loading the testcases
        Scanner scanner = new Scanner(getStream("/HROT/parsingTest2.txt"));

        int[] weights = new int[0];
        Coordinate[] neighbourhood = new Coordinate[0];
        ArrayList<String> comments = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#R")) {
                comments.add(line);
            }
            else if (line.startsWith("#W")) {
                int counter = 0;
                String[] tokens = line.substring(3).split(",");

                weights = new int[tokens.length];
                for (String token: tokens) {
                    weights[counter++] = Integer.parseInt(token);
                }
            }
            else if (line.startsWith("#N")) {
                ArrayList<Coordinate> neighbourhoodList = new ArrayList<>();
                Matcher matcher = Pattern.compile("(?:(-?[0-9]+),\\s?(-?[0-9]+))").matcher(line);
                while (matcher.find()) {
                    Coordinate coordinate = new Coordinate(Integer.parseInt(matcher.group(1)),
                            Integer.parseInt(matcher.group(2)));
                    neighbourhoodList.add(coordinate);
                }

                neighbourhood = neighbourhoodList.toArray(new Coordinate[0]);
            }
            else {
                HROT hrot = new HROT("R2,C2,S2-3,B3,N@");
                hrot.setWeights(weights);
                hrot.setNeighbourhood(neighbourhood);

                assertArrayEquals(comments.toArray(new String[0]), hrot.generateComments());
            }
        }
    }

    @Test
    public void testLoadComments() {
        // Loading the testcases
        Scanner scanner = new Scanner(getStream("/HROT/parsingTest2.txt"));

        int[] weights = new int[0];
        Coordinate[] neighbourhood = new Coordinate[0];
        ArrayList<String> comments = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#R")) {
                comments.add(line);
            }
            else if (line.startsWith("#W")) {
                int counter = 0;
                String[] tokens = line.substring(3).split(",");

                weights = new int[tokens.length];
                for (String token: tokens) {
                    weights[counter++] = Integer.parseInt(token);
                }
            }
            else if (line.startsWith("#N")) {
                ArrayList<Coordinate> neighbourhoodList = new ArrayList<>();
                Matcher matcher = Pattern.compile("(?:(-?[0-9]+),\\s?(-?[0-9]+))").matcher(line);
                while (matcher.find()) {
                    Coordinate coordinate = new Coordinate(Integer.parseInt(matcher.group(1)),
                            Integer.parseInt(matcher.group(2)));
                    neighbourhoodList.add(coordinate);
                }

                neighbourhood = neighbourhoodList.toArray(new Coordinate[0]);
            }
            else {
                HROT hrot = new HROT("R2,C2,S2-3,B3,N@");
                hrot.loadComments(comments.toArray(new String[0]));

                assertArrayEquals(weights, hrot.getWeights());
                assertArrayEquals(neighbourhood, hrot.getNeighbourhood());
            }
        }
    }

    @Test
    public void testRuleRange() {
        // Loading the testcases
        Scanner scanner = new Scanner(getStream("/HROT/ruleRangeTest.txt"));

        HROT rule = new HROT(), minRule = null, maxRule = null;
        Simulator inputPattern = null;
        String targetPattern = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#R")) {
                rule = new HROT(line.substring(3));
            }
            else if (line.startsWith("#I")) {
                inputPattern = new Simulator(rule);
                inputPattern.fromRLE(line.substring(3), new Coordinate(0, 0));
            }
            else if (line.startsWith("#MIN")) {
                minRule = new HROT(line.substring(5));
            }
            else if (line.startsWith("#MAX")) {
                maxRule = new HROT(line.substring(5));
            }
            else if (line.startsWith("#T")) {
                targetPattern = line.substring(3);
            }
            else {
                assert inputPattern != null;
                sample.model.patterns.Pattern pattern = inputPattern.identify();
                assertEquals(targetPattern, pattern.toString());

                assert minRule != null;
                assertEquals(minRule.getRulestring(), pattern.getMinRule().getRulestring());

                assert maxRule != null;
                assertEquals(maxRule.getRulestring(), pattern.getMaxRule().getRulestring());
            }
        }
    }

    @Test
    public void testClone() {
        HROT hrot = new HROT("B2/S");
        HROT hrotClone = (HROT) hrot.clone();

        hrot.setRulestring("R2,C2,S2-3,B3,N@891891");

        // Ensure they are different
        assertNotEquals(hrotClone.getBirth(), hrot.getBirth());
        assertNotEquals(hrotClone.getSurvival(), hrot.getSurvival());
        assertNotEquals(hrotClone.getNeighbourhood(), hrot.getNeighbourhood());
    }
    
    @Test
    public void testGenerateApgtable() throws IOException {
        String[] rules = new String[]{"B3/S23", "R1,C2,S2-3,B3,N@891891", "R2,C2,S9,B0-3,NN",
                "R2,C2,S5-9,B7-8,NM", "R2,C2,S6-11,B9-11,NW0010003330130310333000100"};
        for (String rule: rules) {
            HROT hrotRule = new HROT(rule);
            Simulator simulator = new Simulator(hrotRule);
            simulator.insertCells(SymmetryGenerator.generateC1(50, new int[]{1}, 16, 16),
                    new Coordinate());

            RuleLoader ruleLoader = new RuleLoader();
            for (RuleDirective ruleDirective: hrotRule.generateApgtable())
                ruleLoader.addRuleDirective(ruleDirective);

            FileWriter file = new FileWriter("rules/Temp.rule");
            file.write(ruleLoader.export());
            file.close();

            Simulator simulator2 = new Simulator(new RuleLoader("Temp"));
            simulator2.insertCells(simulator, new Coordinate());

            for (int i = 0; i < 5 * hrotRule.getAlternatingPeriod(); i++) simulator.step();
            for (int i = 0; i < 5 * hrotRule.getAlternatingPeriod(); i++) simulator2.step();

            assertEquals(simulator.toRLE().replace("o", "A").replace("b", "."),
                    simulator2.toRLE().replace("o", "A").replace("b", "."));
        }
    }

    @Test
    public void testSimulation() {
        // Loading the testcases
        Scanner scanner = new Scanner(getStream("/HROT/simulationTest.txt"));

        int generations = 0;
        HROT hrot = new HROT();
        Simulator inputPattern = null;
        Grid targetPattern = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#R")) {
                hrot = new HROT(line.substring(3));
            }
            else if (line.startsWith("#G")) {
                generations = Integer.parseInt(line.substring(3));
            }
            else if (line.startsWith("#I")) {
                inputPattern = new Simulator(hrot);
                inputPattern.fromRLE(line.substring(3), new Coordinate(0, 0));
            }
            else if (line.startsWith("#O")) {
                targetPattern = new Grid();
                targetPattern.fromRLE(line.substring(3), new Coordinate(0, 0));
            }
            else {
                // Run N generations
                assert inputPattern != null;
                for (int i = 0; i < generations; i++) {
                    inputPattern.step();
                }

                assert targetPattern != null;
                assertEquals(targetPattern.toRLE(), inputPattern.toRLE().
                        replace("o", "A").replace("b", "."));
            }
        }
    }
}