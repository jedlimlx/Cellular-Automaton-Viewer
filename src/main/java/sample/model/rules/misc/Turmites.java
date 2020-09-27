package sample.model.rules.misc;

import javafx.scene.paint.Color;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import sample.model.Coordinate;
import sample.model.rules.RuleFamily;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a 2D turing machine or a turmite
 */
public class Turmites extends RuleFamily {
    private Map<Pair<Integer, Integer>, Triplet<Integer, Integer, Integer>> transitionTable;

    private int turmiteStates, gridStates;

    private static final String langtonAnt = "LangtonAnt_[RL]{2,}";
    private static final String turmite = "\\{(\\{(\\{([0-9]+,\\s?){2,}[0-9]+}(,\\s)?)+}(,\\s)?)+}";

    private static final int[] oppositeDirections = new int[]{2, 3, 0, 1};
    private static final Map<Integer, int[]> turn = new HashMap<>();

    public Turmites() {
        this("{{{1, 2, 0}, {0, 8, 0}}}");
    }

    public Turmites(String rulestring) {
        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        name = "Turmites";

        transitionTable = new HashMap<>();

        turn.put(1, new int[]{0, 1, 2, 3});
        turn.put(2, new int[]{1, 2, 3, 0});
        turn.put(4, new int[]{2, 3, 0, 1});
        turn.put(8, new int[]{3, 0, 1, 2});

        // Load rulestring
        setRulestring(rulestring);
    }

    @Override
    protected void fromRulestring(String rulestring) {
        if (rulestring.matches(turmite)) {
            Matcher matcher = Pattern.compile("\\{(\\{([0-9]+,\\s){2,}[0-9]+}(,\\s)?)+}").matcher(rulestring);

            turmiteStates = 0;
            while (matcher.find()) {
                Matcher matcher2 = Pattern.compile("\\{(([0-9]+,\\s){2,}[0-9]+)}").matcher(matcher.group());
                gridStates = 0;
                while (matcher2.find()) {
                    String[] tokens = matcher2.group(1).split(",\\s?");

                    transitionTable.put(new Pair<>(turmiteStates, gridStates),
                            new Triplet<>(Integer.parseInt(tokens[0]),
                                    Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2])));
                    gridStates++;
                }
                turmiteStates++;
            }

            // System.out.println(turmiteStates + " " + gridStates);

            numStates = gridStates + gridStates * turmiteStates * 4;
        }
        else {
            throw new IllegalArgumentException("This rulestring is invalid!");
        }

        background = new int[]{0};

        //for (var key: transitionTable.keySet()) {
            //System.out.println(key + ": " + transitionTable.get(key));
        //}
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
        return "This implements the turmites rule family which isn't working at the moment.";
    }

    @Override
    public Object clone() {
        return new Turmites(rulestring);
    }

    @Override
    public Coordinate[] getNeighbourhood(int generation) {
        return new Coordinate[]{
                new Coordinate(1, 0),
                new Coordinate(0, -1),
                new Coordinate(-1, 0),
                new Coordinate(0, 1)};
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        int newColour = getGridColour(cellState);

        // Key is (turmiteState, gridState)
        Pair<Integer, Integer> key = new Pair<>(getTurmiteState(cellState), getGridColour(cellState));

        // Performing print action
        if (isTurmite(cellState)) return transitionTable.get(key).getValue0();

        boolean isTurmiteArriving = false;
        int turmiteNewDirection = 0, turmiteNewState = 0;
        for (int fromDirection = 0; fromDirection < 4; fromDirection++) {
            Pair<Integer, Integer> key2 = new Pair<>(getTurmiteState(neighbours[fromDirection]),
                    getGridColour(neighbours[fromDirection]));
            if (!isTurmite(neighbours[fromDirection])) continue;

            int turmiteFacing = getDirection(neighbours[fromDirection]);
            int turmiteTurnAction = transitionTable.get(key2).getValue1();

            // Try all possible actions and see if one leads the turmite here
            boolean comingHere = false;
            for (int action: new int[]{1, 2, 4, 8}) {
                if ((turmiteTurnAction & action) > 0) {
                    // Turmite perform this action
                    turmiteNewDirection = turn.get(action)[turmiteFacing];
                    if (fromDirection == oppositeDirections[turmiteFacing]) {
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

        if (isTurmiteArriving) return encodeState(newColour, turmiteNewState, turmiteNewDirection);
        return newColour;
    }

    @Override
    public Color getColour(int state) {
        if (state < gridStates) {
            if (state == 0) return Color.rgb(0, 0, 0);

            if (gridStates == 2) return Color.rgb(255, 255, 255);
            else return Color.rgb(255, 255 * (state - 1) / (gridStates - 2), 0);
        }
        else {
            return Color.AQUA;
        }
    }

    private int encodeState(int gridState, int turmiteState, int direction) {
        return gridStates + 4 * (turmiteStates * gridState + turmiteState) + direction;
    }

    private boolean isTurmite(int state) {
        return state >= gridStates;
    }

    private int getDirection(int state) {
        return (state - gridStates) % 4;
    }

    private int getTurmiteState(int state) {
        return (((state - gridStates) - getDirection(state)) / 4) % turmiteStates;
    }

    private int getGridColour(int state) {
        if (!isTurmite(state)) return state;
        else return (state - gridStates) / (turmiteStates * 4);
    }
}
