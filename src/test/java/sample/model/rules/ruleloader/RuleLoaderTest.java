package sample.model.rules.ruleloader;

import junit.framework.TestCase;
import sample.model.Coordinate;
import sample.model.rules.misc.AlternatingRule;
import sample.model.simulation.Grid;
import sample.model.simulation.Simulator;

import java.io.InputStream;
import java.util.Scanner;

public class RuleLoaderTest extends TestCase {
    public InputStream getStream(String resourcePath) {
        return getClass().getResourceAsStream(resourcePath);
    }

    public void testSimulation() {
        // Loading the testcases
        Scanner scanner = new Scanner(getStream("/RuleLoader/simulationTest.txt"));

        int generations = 0;
        RuleLoader rule = new RuleLoader();
        Simulator inputPattern = null;
        Grid targetPattern = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#R")) {
                rule = new RuleLoader(line.substring(3));
            }
            else if (line.startsWith("#G")) {
                generations = Integer.parseInt(line.substring(3));
            }
            else if (line.startsWith("#I")) {
                inputPattern = new Simulator(rule);
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

                Grid inputPattern2 = new Grid(inputPattern.toRLE());

                assert targetPattern != null;
                assertEquals(targetPattern.toRLE(), inputPattern2.toRLE());
            }
        }
    }
}