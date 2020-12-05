package sample.model.rules.isotropic.rules;

import org.junit.Test;
import sample.model.Coordinate;
import sample.model.rules.isotropic.transitions.R1MooreINT;
import sample.model.simulation.Grid;
import sample.model.simulation.Simulator;

import java.io.InputStream;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DeficientINTTest {
    public InputStream getStream(String resourcePath) {
        return getClass().getResourceAsStream(resourcePath);
    }

    @Test
    public void testCanonise() {
        // Loading the testcases
        Scanner scanner = new Scanner(getStream("/Deficient INT/parsingTest.txt"));

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
                DeficientINT intRule = new DeficientINT(rulestring);
                assertEquals(canonisedRulestring, intRule.getRulestring());
            }
        }
    }

    @Test
    public void testClone() {
        DeficientINT intRule = new DeficientINT("B2n3/S23-q/D");
        DeficientINT intRuleCloned = (DeficientINT) intRule.clone();

        intRule.setBirth(new R1MooreINT("34c"));
        assertNotEquals(intRuleCloned.getBirth(), intRule.getBirth());
    }

    @Test
    public void testSimulation() {
        // Loading the testcases
        Scanner scanner = new Scanner(getStream("/Deficient INT/simulationTest.txt"));

        int generations = 0;
        DeficientINT intRule = new DeficientINT();
        Simulator inputPattern = null;
        Grid targetPattern = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#R")) {
                intRule = new DeficientINT(line.substring(3));
            }
            else if (line.startsWith("#G")) {
                generations = Integer.parseInt(line.substring(3));
            }
            else if (line.startsWith("#I")) {
                inputPattern = new Simulator(intRule);
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