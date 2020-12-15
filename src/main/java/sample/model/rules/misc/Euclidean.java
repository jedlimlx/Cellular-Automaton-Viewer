package sample.model.rules.misc;

import org.javatuples.Pair;
import sample.model.Coordinate;
import sample.model.NeighbourhoodGenerator;
import sample.model.Utils;
import sample.model.rules.RuleFamily;
import sample.model.simulation.Block;
import sample.model.simulation.Grid;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Adds up numbers in an array with perfect precision, and in O(n).
 * From https://code.activestate.com/recipes/393090/
 */
class Summarizer {
    /**
     * Perfectly sums up numbers, without rounding errors (if at all possible).
     * @param values The values to sum up.
     * @return The sum.
     */
    public static double msum(double... values) {
        List<Double> partials = new ArrayList<>();
        for (double x : values) {
            int i = 0;
            for (double y : partials) {
                if (Math.abs(x) < Math.abs(y)) {
                    double tmp = x;
                    x = y;
                    y = tmp;
                }
                double hi = x + y;
                double lo = y - (hi - x);
                if (lo != 0.0) {
                    partials.set(i, lo);
                    ++i;
                }
                x = hi;
            }
            if (i < partials.size()) {
                partials.set(i, x);
                partials.subList(i + 1, partials.size()).clear();
            } else {
                partials.add(x);
            }
        }

        return sum(partials);
    }

    /**
     * Perfectly sums up numbers, without rounding errors (if at all possible).
     * @param values The values to sum up.
     * @return The sum.
     */
    public static double msum(Collection<Double> values) {
        List<Double> partials = new ArrayList<>();
        for (double x : values) {
            int i = 0;
            for (double y : partials) {
                if (Math.abs(x) < Math.abs(y)) {
                    double tmp = x;
                    x = y;
                    y = tmp;
                }
                double hi = x + y;
                double lo = y - (hi - x);
                if (lo != 0.0) {
                    partials.set(i, lo);
                    ++i;
                }
                x = hi;
            }
            if (i < partials.size()) {
                partials.set(i, x);
                partials.subList(i + 1, partials.size()).clear();
            } else {
                partials.add(x);
            }
        }

        return sum(partials);
    }

    /**
     * Sums up the rest of the partial numbers which cannot be summed up without
     * loss of precision.
     */
    public static double sum(Collection<Double> values) {
        double s = 0.0;
        for (Double d : values) {
            s += d;
        }
        return s;
    }
}

/**
 * Implements euclidean cellular automaton which has an infinite neighbourhood with weights that
 * drop off with f(r) where r is the euclidean / manhattan distance
 *
 * TODO (Fix freaking floating-point summation)
 */
public class Euclidean extends RuleFamily {
    /**
     * The birth conditions of the Euclidean CA
     */
    private HashSet<Pair<Double, Double>> birth;

    /**
     * The survival conditions of the Euclidean CA
     */
    private HashSet<Pair<Double, Double>> survival;

    /**
     * The minimum birth condition (used to find cells who might be born)
     */
    private double minBirth;

    /**
     * Precomputing the neighbourhoods required for the simulation
     */
    private static Coordinate[][] neighbourhoods;

    /**
     * R/M ^ p
     */
    private int power;

    /**
     * R/M ^ p or p ^ R/M
     */
    private boolean exp;

    /**
     * Should euclidean or manhattan distance
     */
    private boolean euclidean;

    /**
     * The maximum heat a cell can experience
     */
    private double MAX_HEAT;

    private static final String regex = "B(\\d+(.\\d+)?-\\d+(.\\d+)?,)*(\\d+(.\\d+)?-\\d+(.\\d+)?)/" +
            "S(\\d+(.\\d+)?-\\d+(.\\d+)?,)*(\\d+(.\\d+)?-\\d+(.\\d+)?)/E([MR][0-9]+|[0-9]+[MR])";

    public Euclidean() {
        this("B0.75-0.95,1.5-1.7,2.25-2.5,3-3.3/S0.5-0.95,1.25-1.7,2.25-2.5,3-3.3/ER4");
    }

    public Euclidean(String rulestring) {
        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        background = new int[]{0};
        name = "Euclidean CA";

        birth = new HashSet<>();
        survival = new HashSet<>();

        setRulestring(rulestring);
    }

    @Override
    protected void fromRulestring(String rulestring) {
        birth.clear();
        survival.clear();

        minBirth = 100000000;

        String expressionString;
        String[] birthTokens, survivalTokens;
        if (rulestring.matches(regex)) {
            // Computing birth and survival conditions
            birthTokens = Utils.matchRegex("B((\\d+(.\\d+)?-\\d+(.\\d+)?,)*(\\d+(.\\d+)?-\\d+(.\\d+)?))",
                    rulestring, 0, 1).split(",");
            for (String string: birthTokens) {
                minBirth = Math.min(minBirth, Double.parseDouble(string.split("-")[0]));
                birth.add(new Pair<>(Double.parseDouble(string.split("-")[0]),
                        Double.parseDouble(string.split("-")[1])));
            }

            survivalTokens = Utils.matchRegex("S((\\d+(.\\d+)?-\\d+(.\\d+)?,)*(\\d+(.\\d+)?-\\d+(.\\d+)?))",
                    rulestring, 0, 1).split(",");
            for (String string: survivalTokens) {
                survival.add(new Pair<>(Double.parseDouble(string.split("-")[0]),
                        Double.parseDouble(string.split("-")[1])));
            }

            expressionString = Utils.matchRegex("E([MR][0-9]+|[0-9]+[MR])",
                    rulestring, 0, 1);
            if (expressionString.charAt(0) == 'R' || expressionString.charAt(0) == 'M') {
                exp = false;
                euclidean = expressionString.charAt(0) == 'R';
                power = Integer.parseInt(expressionString.substring(1));
            } else {
                exp = true;
                euclidean = expressionString.charAt(expressionString.length() - 1) == 'R';
                power = Integer.parseInt(expressionString.substring(0, expressionString.length() - 1));
            }
        } else {
            throw new IllegalArgumentException("This rulestring is invalid!");
        }

        // Precomputing the circular / von neumann neighbourhoods
        neighbourhoods = new Coordinate[50][];
        for (int range = 1; range < 50; range++) {
            if (euclidean)
                neighbourhoods[range] = NeighbourhoodGenerator.generateCircular(range, range - 1);
            else
                neighbourhoods[range] = NeighbourhoodGenerator.generateVonNeumann(range, range - 1);
        }

        // Compute max heat
        MAX_HEAT = 0;
        if (euclidean) {
            for (Coordinate coordinate: NeighbourhoodGenerator.generateCircular(100))
                MAX_HEAT += compute(coordinate);
        } else {
            for (Coordinate coordinate: NeighbourhoodGenerator.generateVonNeumann(100))
                MAX_HEAT += compute(coordinate);
        }
    }

    @Override
    public String canonise(String rulestring) {
        return rulestring;
    }

    @Override
    public String[] getRegex() {
        return new String[]{regex};
    }

    @Override
    public String getDescription() {
        return "This implements the Euclidean CA which have an infinite neighbourhood.\n" +
                "B0 rules are not supported.\n" +
                "The format is as follows:\n" +
                "B<range>,<range2>,.../S<range>,<range2>,.../E<expression>";
    }

    @Override
    public Object clone() {
        return new Euclidean(getRulestring());
    }

    @Override
    public Coordinate[] getNeighbourhood(int generation) {
        return new Coordinate[0];
    }

    @Override  // Dummy method that is not to be used
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        return 0;
    }

    @Override
    public void step(Grid grid, ArrayList<Set<Coordinate>> cellsChanged, int generation,
                     Function<Coordinate, Boolean> step) throws IllegalArgumentException {
        if (readingOrder != null) throw new IllegalArgumentException("Naive reading orders are not supported " +
                "by Euclidean CA");

        cellsChanged.get(0).clear();

        Grid gridCopy = grid.deepCopy();
        HashSet<Coordinate> visited = new HashSet<>(grid.getPopulation() * 8);

        AtomicBoolean born = new AtomicBoolean(false);
        for (int range = 1; range < 50; range++) {
            born.set(false);
            int finalRange = range;
            gridCopy.iterateCells(cell -> {
                Coordinate neighbour;

                boolean satisfied;
                for (Coordinate neighbour2: neighbourhoods[finalRange]) {
                    neighbour = cell.add(neighbour2);
                    if (!visited.contains(neighbour)) {
                        visited.add(neighbour);
                        if (grid.getCell(neighbour) == 0) {
                            satisfied = checkSatisfy(birth, neighbour, gridCopy);
                            if (satisfied) {
                                born.set(true);
                                grid.setCell(neighbour, 1);
                                cellsChanged.get(0).add(neighbour);
                            }
                        } else {
                            satisfied = checkSatisfy(survival, neighbour, gridCopy);
                            if (!satisfied) {
                                grid.setCell(neighbour, 0);
                                cellsChanged.get(0).add(neighbour);
                            }
                        }
                    }
                }

                if (!visited.contains(cell)) {
                    satisfied = checkSatisfy(survival, cell, gridCopy);
                    if (!satisfied) {
                        grid.setCell(cell, 0);
                        cellsChanged.get(0).add(cell);
                    }
                }
            });

            if (!born.get()) break;  // If no cells are born, quit
        }
    }

    private boolean checkSatisfy(Set<Pair<Double, Double>> transitions, Coordinate coordinate, Grid grid) {
        int range = 1, i, j;
        boolean done;
        double heat = 0, maxHeatForRange = 0;

        do {
            done = true;

            // Computing heat for a given range
            for (Coordinate neighbour: neighbourhoods[range]) {
                i = neighbour.getX();
                j = neighbour.getY();

                // x^2 + y^2 <= r^2 + r
                if ((i * i + j * j) <= range * range + range) {
                    if (grid.getCell(coordinate.getX() + i, coordinate.getY() + j) == 1)
                        heat += compute(new Coordinate(i, j));

                    maxHeatForRange += compute(new Coordinate(i, j));
                }
            }

            // Checking for boundary conditions within the range
            double maxHeat = heat + MAX_HEAT + maxHeatForRange;
            for (Pair<Double, Double> transition : transitions) {
                if ((heat < transition.getValue0() && transition.getValue0() < maxHeat) ||
                        (heat < transition.getValue1() && transition.getValue1() < maxHeat)) {
                    done = false;
                    break;
                }
            }

            range++;

            if (range >= 2) break;
        } while (!done);

        // Checking if satisfied
        if (done) {
            for (Pair<Double, Double> transition: transitions) {
                if (heat >= transition.getValue0() && heat <= transition.getValue1()) {
                    return true;
                }
            }
        } else {
            double heat2 = 0;
            for (Block block: grid) {
                if (block.getPopulation() == 0) continue;
                for (int k = block.getStartCoordinate().getX(); k < block.getStartCoordinate().getX() + Grid.BLOCK_SIZE; k++) {
                    for (int l = block.getStartCoordinate().getY(); l < block.getStartCoordinate().getY() + Grid.BLOCK_SIZE; l++) {
                        if (block.getCell(k, l) > 0 && !coordinate.equals(new Coordinate(k, l)))
                            heat2 += compute(new Coordinate(k, l).subtract(coordinate));
                    }
                }
            }

            for (Pair<Double, Double> transition: transitions) {
                if (heat2 > transition.getValue0() && heat2 < transition.getValue1()) {
                    return true;
                }
            }
        }

        return false;
    }

    private double compute(Coordinate diff) {
        double dist;
        if (euclidean) dist = Math.pow(diff.getX() * diff.getX() + diff.getY() * diff.getY(), 0.5);
        else dist = Math.abs(diff.getX()) + Math.abs(diff.getY());

        if (exp) return 1 / Math.pow(power, dist);
        else return 1 / Math.pow(dist, power);
    }
}
