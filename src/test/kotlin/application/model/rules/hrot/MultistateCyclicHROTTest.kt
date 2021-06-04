package application.model.rules.hrot;

import org.junit.jupiter.api.Test;
import application.model.Coordinate;
import application.model.simulation.Grid;
import application.model.simulation.Simulator;

import java.io.InputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultistateCyclicHROTTest {
    public InputStream getStream(String resourcePath) {
        return getClass().getResourceAsStream(resourcePath);
    }

    @Test
    public void testSimulation() {
        // Loading the testcases
        Scanner scanner = new Scanner(getStream("/Cyclic HROT/simulationTest.txt"));

        int generations = 0;
        MultistateCyclicHROT hrot = new MultistateCyclicHROT();
        Simulator inputPattern = null;
        Grid targetPattern = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#R")) {
                hrot = new MultistateCyclicHROT(line.substring(3));
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
                System.out.println(hrot.getRulestring());

                // Run N generations
                assert inputPattern != null;
                for (int i = 0; i < generations; i++) {
                    inputPattern.step();
                }

                assert targetPattern != null;
                assertEquals(targetPattern.toRLE(), inputPattern.toRLE());
            }
        }
    }
}