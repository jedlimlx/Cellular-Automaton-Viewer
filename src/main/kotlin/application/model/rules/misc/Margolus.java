package application.model.rules.misc;

import application.model.Coordinate;
import application.model.rules.RuleFamily;
import application.model.simulation.Grid;

import java.util.*;
import java.util.function.Function;

/**
 * Represents a 2-state Margolus rule
 */
public class Margolus extends RuleFamily {
    private static String regex = "M([0-9]+,){15}[0-9]+";
    private ArrayList<Integer> neighbourhoods;

    /**
     * Creates a margolus rule
     */
    public Margolus() {
        this("M0,2,8,9,1,6,12,7,4,5,10,11,9,13,14,15");
    }

    /**
     * Creates a Margolus rule based on the provided rulestring
     * @param rulestring The rulestring of the Margolus rule
     */
    public Margolus(String rulestring) {
        // Initialise variables
        numStates = 2;
        name = "Margolus";
        locationPeriod = 2;

        // Load rulestring
        setRulestring(rulestring);
    }

    /**
     * Loads the Margolus rule's parameters from a rulestring
     * @param rulestring The rulestring of the Margolus rule
     * @throws IllegalArgumentException Thrown if the rulestring is invalid
     */
    @Override
    protected void fromRulestring(String rulestring) {
        alternatingPeriod = 2;
        background = new int[]{0, 0};

        String newRulestring = rulestring.replace("M","");  // Remove front M
        String[] tokens = newRulestring.split(",");  // Split by commas

        neighbourhoods = new ArrayList<>();
        for (String token: tokens) {
            int neighbourhoodInt = Integer.parseInt(token);
            if (neighbourhoodInt < 0 || neighbourhoodInt > 15)  // Error handling
                throw new IllegalArgumentException("Integer must be between 0 and 15!");

            neighbourhoods.add(neighbourhoodInt);
        }

        // Update background
        updateBackground();
    }

    @Override
    public String canonise(String rulestring) {
        return rulestring;
    }

    @Override
    public void updateBackground() {
        if (neighbourhoods.get(0) == 15) {
            if (neighbourhoods.get(15) == 15) {
                background = new int[]{1, 1};
            } else {
                background = new int[]{0, 1};
            }
        } else if (neighbourhoods.get(0) == 0){
            background = new int[]{0, 0};
        } else {
            throw new IllegalArgumentException("Strobing margolus rules (without 0 or 15) are not supported!");
        }
    }

    @Override
    public String[] getRegex() {
        return new String[]{regex};
    }

    @Override
    public String getDescription() {
        return "This implements Margolus rules based on the syntax used by LifeViewer & Golly.\n" +
                "B0 Margolus rules are not supported.\n" +
                "The format is as follows:\n" +
                "M<neighbourhood0>,<neighbourhood1>,...,<neighbourhood16>\n" +
                "\n" +
                "Examples:\n" +
                "M0,14,13,0,11,0,0,0,7,0,0,0,0,0,0,0\n" +
                "M0,2,8,9,1,6,12,7,4,5,10,11,9,13,14,15";
    }

    /**
     * Gets the deep copy of the Margolus rule
     * @return Returns a deep copy of the Margolus rule
     */
    @Override
    public Object clone() {
        return new Margolus(getRulestring());
    }

    /**
     * Gets the neighbourhood of the 1D rule
     * @param generation The generation of the simulation
     * @return Returns the neighbourhood of the 1D rule
     */
    @Override
    public Coordinate[] getNeighbourhood(int generation) {
        return new Coordinate[]{new Coordinate(0, 0)};
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        // Trick the updateBackground algorithm in Rule.java to do the right thing when doing alternating rules
        if (cellState == 0) {
            return background[1];
        } else if (cellState == 1) {
            return background[0];
        } else {
            return 0;
        }
    }

    @Override
    public void step(Grid grid, ArrayList<Set<Coordinate>> cellsChanged, int generation, Function<Coordinate, Boolean> step)
            throws IllegalArgumentException {
        if (cellsChanged.size() != alternatingPeriod)
            throw new IllegalArgumentException("cellsChanged parameter should have length " + alternatingPeriod + "!");

        HashSet<Coordinate> cellsToCheck = new HashSet<>();

        // Obtain the cells that should be checked in this generation
        for (Set<Coordinate> cellSet: cellsChanged) {
            for (Coordinate coordinate : cellSet) {
                if (generation % 2 == 0) {
                    cellsToCheck.add(new Coordinate(coordinate.getX() - Math.floorMod(coordinate.getX(), 2),
                            coordinate.getY() - Math.floorMod(coordinate.getY(), 2)));
                } else {
                    cellsToCheck.add(new Coordinate(
                            coordinate.getX() % 2 == 0 ? coordinate.getX() - 1 : coordinate.getX(),
                            coordinate.getY() % 2 == 0 ? coordinate.getY() - 1 : coordinate.getY()));
                }
            }
        }

        // Loop over those cells
        Grid gridCopy = grid.deepCopy();
        for (Coordinate cellToCheck: cellsToCheck) {
            // Convert 2x2 block to integer
            int blockInt = convertState(gridCopy.getCell(cellToCheck), generation)
                    + 2 * convertState(gridCopy.getCell(new Coordinate(cellToCheck.getX() + 1, cellToCheck.getY())), generation)
                    + 4 * convertState(gridCopy.getCell(new Coordinate(cellToCheck.getX(), cellToCheck.getY() + 1)), generation)
                    + 8 * convertState(gridCopy.getCell(new Coordinate(cellToCheck.getX() + 1, cellToCheck.getY() + 1)), generation);

            // Update 2x2 block to new configuration
            String binaryString = Integer.toBinaryString(neighbourhoods.get(blockInt));
            binaryString = "0".repeat(4 - binaryString.length()) + binaryString;

            addToCellChanged(grid, gridCopy, cellsChanged, cellToCheck,
                    convertState(Character.getNumericValue(binaryString.charAt(3)), generation + 1));
            addToCellChanged(grid, gridCopy, cellsChanged,
                    new Coordinate(cellToCheck.getX() + 1, cellToCheck.getY()),
                    convertState(Character.getNumericValue(binaryString.charAt(2)), generation + 1));
            addToCellChanged(grid, gridCopy, cellsChanged,
                    new Coordinate(cellToCheck.getX(), cellToCheck.getY() + 1),
                    convertState(Character.getNumericValue(binaryString.charAt(1)), generation + 1));
            addToCellChanged(grid, gridCopy, cellsChanged,
                    new Coordinate(cellToCheck.getX() + 1, cellToCheck.getY() + 1),
                    convertState(Character.getNumericValue(binaryString.charAt(0)), generation + 1));
        }
    }

    private void addToCellChanged(Grid grid, Grid gridCopy, ArrayList<Set<Coordinate>> cellsChanged,
                                  Coordinate cellToCheck, int state) {
        if (gridCopy.getCell(cellToCheck) != state) {
            cellsChanged.get(0).add(cellToCheck);
            grid.setCell(cellToCheck, state);
        } else {
            if (cellsChanged.get(0).contains(cellToCheck)) {
                cellsChanged.get(0).remove(cellToCheck);
                cellsChanged.get(1).add(cellToCheck);
            } else {
                cellsChanged.get(1).remove(cellToCheck);
            }
        }
    }
}
