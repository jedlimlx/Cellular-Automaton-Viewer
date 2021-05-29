package application.model.rules.hrot;

import org.junit.jupiter.api.Test;
import application.model.Coordinate;
import application.model.simulation.Grid;
import application.model.simulation.Simulator;

import java.io.InputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class HROTBSFKLTest {
    public InputStream getStream(String resourcePath) {
        return getClass().getResourceAsStream(resourcePath);
    }

    @Test
    public void testCanonise() {
        // Loading the testcases
        Scanner scanner = new Scanner(getStream("/HROT BSFKL/parsingTest.txt"));

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
                HROTBSFKL hrot = new HROTBSFKL(rulestring);
                assertEquals(canonisedRulestring, hrot.getRulestring());
            }
        }
    }

    @Test
    public void testClone() {
        HROTBSFKL hrot = new HROTBSFKL("R1,B1,S2,F3,K4,L5,NM");
        HROTBSFKL hrotClone = (HROTBSFKL) hrot.clone();

        hrot.setRulestring("R2,B2,S3,F4,K5,L6,N@891891");

        // Ensure they are different
        assertNotEquals(hrotClone.getBirth(), hrot.getBirth());
        assertNotEquals(hrotClone.getSurvival(), hrot.getSurvival());
        assertNotEquals(hrotClone.getForcing(), hrot.getForcing());
        assertNotEquals(hrotClone.getKilling(), hrot.getKilling());
        assertNotEquals(hrotClone.getLiving(), hrot.getLiving());
        assertNotEquals(hrotClone.getNeighbourhood(), hrot.getNeighbourhood());
    }

    @Test
    public void testSimulation() {
        // Loading the testcases
        Scanner scanner = new Scanner(getStream("/HROT BSFKL/simulationTest.txt"));

        int generations = 0;
        HROTBSFKL hrot = new HROTBSFKL();
        Simulator inputPattern = null;
        Grid targetPattern = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#R")) {
                hrot = new HROTBSFKL(line.substring(3));
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