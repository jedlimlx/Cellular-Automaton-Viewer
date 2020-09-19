package sample.model.rules.misc;

import sample.model.Coordinate;
import sample.model.Grid;
import sample.model.Utils;
import sample.model.rules.RuleFamily;

import java.math.BigInteger;
import java.util.*;

/**
 * Represents a 1D cellular automaton rule
 */
public class OneDimensional extends RuleFamily {
    // TODO (Fix B0 Rules)
    private Coordinate[] neighbourhood;
    private Map<String, Integer> transitions;

    private static final String wolfram = "W[0-9]+";
    private static final String wolframWithRangeAndStates = "R[1-9][0-9]*,C[2-9][0-9]*,W[0-9]+";

    /**
     * Creates the rule W110
     */
    public OneDimensional() {
        this("W110");
    }

    /**
     * Creates a 1D rule based on the provided rulestring
     * @param rulestring The rulestring of the 1D rule
     */
    public OneDimensional(String rulestring) {
        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        name = "One Dimensional";

        transitions = new HashMap<>();

        // Load rulestring
        setRulestring(rulestring);
    }

    /**
     * Loads the 1D rule's parameters from a rulestring
     * @param rulestring The rulestring of the 1D rule (e.g. W110, R1,C3,W1902)
     * @throws IllegalArgumentException Thrown if the rulestring is invalid
     */
    @Override
    protected void fromRulestring(String rulestring) {
        if (rulestring.matches(wolfram)) {
            numStates = 2;
            neighbourhood = new Coordinate[]
                    {new Coordinate(-1, -1), new Coordinate(0, -1), new Coordinate(1, -1)};
        }
        else if (rulestring.matches(wolframWithRangeAndStates)) {
            numStates = Integer.parseInt(Utils.matchRegex("C([2-9][0-9]*)", rulestring, 0, 1));

            int range = Integer.parseInt(Utils.matchRegex("R([1-9][0-9]*)", rulestring, 0, 1));
            neighbourhood = new Coordinate[2 * range + 1];
            for (int i = -range; i < range + 1; i++) {
                neighbourhood[i + range] = new Coordinate(i, -1);
            }
        }
        else {
            throw new IllegalArgumentException("This rulestring is invalid!");
        }

        BigInteger ruleNumber = new BigInteger(
                Utils.matchRegex("W([0-9]+)", rulestring, 0, 1));
        String ruleNumberString = ruleNumber.toString(numStates);
        ruleNumberString = "0".repeat(Math.max(0, (int)Math.pow(numStates, neighbourhood.length) -
                ruleNumberString.length())) + ruleNumberString;  // Replace it with the corrected one

        for (int i = 0; i < Math.pow(numStates, neighbourhood.length); i++) {
            BigInteger bigInteger = new BigInteger(String.valueOf(i), 10);
            String paddedTransitions = bigInteger.toString(numStates);
            paddedTransitions = "0".repeat(Math.max(0, neighbourhood.length - paddedTransitions.length())) +
                    paddedTransitions;  // Replace it with the corrected one

            transitions.put(paddedTransitions, Integer.parseInt(
                    ruleNumberString.charAt((int)Math.pow(numStates, neighbourhood.length) - i - 1) + ""));
        }

        updateBackground();
    }

    @Override
    public String canonise(String rulestring) {
        return rulestring;
    }

    @Override
    public void updateBackground() {
        int currentState = 0;
        ArrayList<Integer> bgStates = new ArrayList<>();
        while (!bgStates.contains(currentState)) {
            bgStates.add(currentState);
            currentState = transitions.get((currentState + "").repeat(neighbourhood.length));
        }

        background = new int[bgStates.size() - bgStates.indexOf(currentState)];
        for (int i = bgStates.indexOf(currentState); i < bgStates.size(); i++) {
            background[i - bgStates.indexOf(currentState)] = bgStates.get(i);
        }

        alternatingPeriod = background.length;
    }

    @Override
    public String[] getRegex() {
        return new String[]{wolfram, wolframWithRangeAndStates};
    }

    @Override
    public String getDescription() {
        return "This implements 1 dimensional rules based on the syntax used by Wolfram Alpha.\n" +
                "The format is as follows:\n" +
                "W<wolframNumber>\n" +
                "R<range>,C<states>,W<wolframNumber>\n" +
                "\n" +
                "Examples:\n" +
                "W110\n" +
                "R1,C3,W14584";
    }

    /**
     * Gets the deep copy of the 1D rule
     * @return Returns a deep copy of the 1D rule
     */
    @Override
    public Object clone() {
        return new OneDimensional(getRulestring());
    }

    /**
     * Gets the neighbourhood of the 1D rule
     * @param generation The generation of the simulation
     * @return Returns the neighbourhood of the 1D rule
     */
    @Override
    public Coordinate[] getNeighbourhood(int generation) {
        return neighbourhood;
    }

    /**
     * The transition function of the 1D rule
     * @param neighbours The cell's neighbours in the order of the neighbourhood provided
     * @param cellState The current state of the cell
     * @param generations The current generation of the simulation
     * @return Returns the new state of the cell
     */
    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations) {
        StringBuilder transitionString = new StringBuilder();
        for (int neighbour : neighbours) {
            transitionString.append(neighbour);
        }

        Integer newState = transitions.get(transitionString.toString());
        if (newState == null) return 0;
        return newState;
    }

    @Override
    public void step(Grid grid, ArrayList<Set<Coordinate>> cellsChanged, int generation) throws IllegalArgumentException {
        if (cellsChanged.size() != alternatingPeriod)
            throw new IllegalArgumentException("cellsChanged parameter should have length " + alternatingPeriod + "!");

        Grid gridCopy = grid.deepCopy();
        HashSet<Coordinate> cellsToCheck = new HashSet<>();
        Coordinate[] neighbourhood = getNeighbourhood(generation);

        // Generate set of cells to run update function on
        // Use a set to avoid duplicates
        for (Set<Coordinate> cellSet: cellsChanged) {
            for (Coordinate cell: cellSet) {
                for (Coordinate neighbour: neighbourhood) {
                    cellsToCheck.add(cell.subtract(neighbour));
                }
            }
        }

        cellsChanged.get(0).clear();

        int[] neighbours;
        int newState, prevState;
        for (Coordinate cell: cellsToCheck) {
            prevState = gridCopy.getCell(cell);

            // Getting neighbour states
            if (prevState == 0) {
                neighbours = new int[neighbourhood.length];
                for (int i = 0; i < neighbourhood.length; i++) {
                    // Converting based on background
                    neighbours[i] = convertState(gridCopy.getCell(cell.add(neighbourhood[i])), generation);
                }

                // Call the transition function on the new state
                // Don't forget to convert back to the current background
                newState = convertState(transitionFunc(neighbours,
                        convertState(prevState, generation), generation), generation + 1);
            }
            else {
                newState = prevState;
            }

            if (newState != prevState) {
                cellsChanged.get(0).add(cell);
                grid.setCell(cell, newState);
            }
        }
    }
}
