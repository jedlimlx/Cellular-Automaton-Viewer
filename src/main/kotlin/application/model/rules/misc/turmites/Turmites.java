package application.model.rules.misc.turmites;

import javafx.scene.paint.Color;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import application.model.Coordinate;
import application.model.rules.RuleFamily;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a 2D turing machine or a turmite
 */
public class Turmites extends RuleFamily {
    private final Map<Pair<Integer, Integer>, Triplet<Integer, Integer, Integer>> transitionTable;

    private int turmiteStates, gridStates;
    private Neighbourhood neighbourhood;

    private static final String langtonAnt = "LangtonAnt_[RL]{2,}";
    private static final String turmite = "\\{(\\{(\\{([0-9]+,\\s*){2,}[0-9]+}(,\\s*)?)+}(,\\s*)?)+}[VMHL]?";

    public Turmites() {
        this("{{{1, 2, 0}, {0, 8, 0}}}");
    }

    public Turmites(String rulestring) {
        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        name = "Turmites";

        transitionTable = new HashMap<>();

        // Load rulestring
        setRulestring(rulestring);
    }

    @Override
    protected void fromRulestring(String rulestring) {
        if (rulestring.matches(turmite)) {
            Matcher matcher = Pattern.compile("\\{(\\{([0-9]+,\\s*){2,}[0-9]+}(,\\s*)?)+}").matcher(rulestring);

            turmiteStates = 0;
            while (matcher.find()) {
                Matcher matcher2 = Pattern.compile("\\{(([0-9]+,\\s*){2,}[0-9]+)}").matcher(matcher.group());
                gridStates = 0;
                while (matcher2.find()) {
                    String[] tokens = matcher2.group(1).split(",\\s*");

                    transitionTable.put(new Pair<>(turmiteStates, gridStates),
                            new Triplet<>(Integer.parseInt(tokens[0]),
                                    Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2])));
                    gridStates++;
                }
                turmiteStates++;
            }

            // Setting neighbourhood
            char lastChar = rulestring.charAt(rulestring.length() - 1);
            switch (lastChar) {
                case 'H': neighbourhood = new Hexagonal(); break;
                case 'L': neighbourhood = new Triangular(); break;
                default: neighbourhood = new VonNeumann(); break;
            }

            tiling = neighbourhood.getTiling();

            numStates = gridStates + gridStates * turmiteStates * getNeighbourhood().length;
        }
        else {
            throw new IllegalArgumentException("This rulestring is invalid!");
        }

        background = new int[]{0};
    }

    @Override
    public String canonise(String rulestring) {
        return rulestring;
    }

    @Override
    public void updateBackground() {
        background = new int[]{0};
    }

    @Override
    public String[] getRegex() {
        return new String[]{turmite};
    }

    @Override
    public String getDescription() {
        return "This implements the turmites rule family.\n" +
                "Currently, only relative turmites are supported.\n" +
                "See https://github.com/GollyGang/ruletablerepository/wiki/TwoDimensionalTuringMachines " +
                "for information." +
                "The format is as follows:\n" +
                "{{{a1, b1, c1}, {a2, b2, c2}, ...}, {{a3, b3, c3}, {a4, b4, c4}, ...}, ...}[VH]\n\n" +
                "Examples:\n" +
                "{{{1, 2, 0}, {0, 8, 0}}} (Langton's Ant)\n" +
                "{{{1, 2, 0}, {0, 1, 0}}} (Binary Counter)\n" +
                "{{{1, 10, 0}, {0, 1, 0}}} (Snowflake-like Growth)\n" +
                "{{{0, 2, 1}, {0, 1, 1}}, {{1, 2, 1}, {1, 8, 0}}} (Contoured Island)";
    }

    @Override
    public Object clone() {
        return new Turmites(rulestring);
    }

    @Override
    public Coordinate[] getNeighbourhood(int generation) {
        return neighbourhood.getNeighbourhood();
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        // Key is (turmiteState, gridState)
        Pair<Integer, Integer> key = new Pair<>(getTurmiteState(cellState), getGridColour(cellState));

        // Performing print action
        if (isTurmite(cellState)) return transitionTable.get(key).getValue0();

        boolean isTurmiteArriving = false;
        int turmiteNewDirection = 0, turmiteNewState = 0;
        for (int fromDirection = 0; fromDirection < neighbours.length; fromDirection++) {
            Pair<Integer, Integer> key2 = new Pair<>(getTurmiteState(neighbours[fromDirection]),
                    getGridColour(neighbours[fromDirection]));
            if (!isTurmite(neighbours[fromDirection])) continue;

            int turmiteFacing = getDirection(neighbours[fromDirection]);
            int turmiteTurnAction = transitionTable.get(key2).getValue1();

            // Try all possible actions and see if one leads the turmite here
            boolean comingHere = false;
            for (int action: neighbourhood.getActions()) {
                if ((turmiteTurnAction & action) > 0) {
                    // Turmite perform this action
                    turmiteNewDirection = neighbourhood.getNewDirection(action, turmiteFacing);
                    if (fromDirection == neighbourhood.getOppositeDirection(turmiteNewDirection)) {
                        comingHere = true;
                        break;
                    }
                }
            }

            // Turmite coming from this direction?
            if (comingHere) {
                if (isTurmiteArriving) {
                    isTurmiteArriving = false;
                    break;
                }

                isTurmiteArriving = true;
                turmiteNewState = transitionTable.get(key2).getValue2();
            }
        }

        if (isTurmiteArriving) return encodeState(getGridColour(cellState), turmiteNewState, turmiteNewDirection);
        return cellState;
    }

    @Override
    public Color getColour(int state) {
        if (state == 0) return Color.rgb(0, 0, 0);
        else if (state < gridStates) {
            if (gridStates == 2) return Color.rgb(255, 255, 255);
            else return Color.rgb(255, 255 * (state - 1) / (gridStates - 2), 0);
        }
        else {
            return Color.rgb(0, 255 * (state - gridStates) /
                    (numStates - gridStates - 1), 255);
        }
    }

    private int encodeState(int gridState, int turmiteState, int direction) {
        return gridStates + getNeighbourhood().length *
                (turmiteStates * gridState + turmiteState) + direction;
    }

    private boolean isTurmite(int state) {
        return state >= gridStates;
    }

    private int getDirection(int state) {
        return (state - gridStates) % getNeighbourhood().length;
    }

    private int getTurmiteState(int state) {
        return (((state - gridStates) - getDirection(state)) / getNeighbourhood().length) % turmiteStates;
    }

    private int getGridColour(int state) {
        if (!isTurmite(state)) return state;
        else return (state - gridStates) / (turmiteStates * getNeighbourhood().length);
    }
}
