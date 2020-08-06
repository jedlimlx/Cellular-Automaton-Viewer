package sample.model.rules;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class HROTTest {
    public String getPathToResource(String resourcePath) {
        return getClass().getClassLoader().getResource(resourcePath).getPath();
    }

    @Test
    public void testFromRulestring() throws FileNotFoundException {
        // Loading the testcases
        File file = new File("src/test/resources/HROT/fromRulestring.txt");
        Scanner scanner = new Scanner(file);

        // Run through them
        String rulestring = "";
        HashSet<Integer> birth = null, survival = null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("#R")) {
                // Loading rulestring
                rulestring = line.substring(3);
            }
            else if (line.startsWith("#B")) {
                // Loading birth conditions
                String withoutHeader = line.substring(3);
                birth = new HashSet<>();
                String[] tokens = withoutHeader.split(",");

                for (String token: tokens) {
                    birth.add(Integer.parseInt(token));
                }
            }
            else if (line.startsWith("#S")) {
                // Loading survival conditions
                String withoutHeader = line.substring(3);
                survival = new HashSet<>();
                String[] tokens = withoutHeader.split(",");

                for (String token: tokens) {
                    survival.add(Integer.parseInt(token));
                }
            }
            else {
                // Running the testcase
                HROT hrot = new HROT(rulestring);
                assertEquals(hrot.getBirth(), birth);
                assertEquals(hrot.getSurvival(), survival);
            }
        }
    }

    @Test
    public void testCanonise() {
    }

    @Test
    public void testGenerateApgtable() {
    }

    @Test
    public void testGenerateComments() {
    }

    @Test
    public void testLoadComments() {
    }

    @Test
    public void testTestClone() {
    }
}