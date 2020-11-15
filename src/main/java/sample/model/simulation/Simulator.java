package sample.model.simulation;

import org.javatuples.Pair;
import sample.model.Coordinate;
import sample.model.patterns.*;
import sample.model.rules.Rule;
import sample.model.rules.RuleFamily;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Simulations are run using Simulator. <br>
 * <br>
 * Example Usage: <br>
 * <pre>
 * Simulator simulator = new Simulator(new HROT("B3/S23"));
 * simulator.fromRLE("bo$obo$o2bo$bobo$2bo!", new Coordinate(0, 0));
 * simulator.step();
 * iterateCells(cell -> {
 *      System.out.println(cell);
 * });
 * </pre>
 */
public class Simulator extends Grid {
    /**
     * The generation of the simulation.
     */
    private int generation;

    /**
     * The rule of the simulation.
     */
    private Rule rule;

    /**
     * The cells that changed in the previous generation and the previous previous generation...
     * Something like {{(0, 1)...}, ...} with the length of the loaded rule's alternating period.
     */
    private final ArrayList<Set<Coordinate>> cellsChangedArray;

    /**
     * Initialises the simulator
     * @param rule The rule to be loaded
     */
    public Simulator(Rule rule) {
        this.rule = rule;
        this.generation = 0;
        this.cellsChangedArray = new ArrayList<>();

        for (int i = 0; i < rule.getAlternatingPeriod(); i++) {
            cellsChangedArray.add(new HashSet<>());
        }
    }

    /**
     * Gets the rule of the simulator
     * @return Returns the rule of the simulator
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * Gets the generation of the simulator
     * @return Returns the generation of the simulator
     */
    public int getGeneration() {
        return generation;
    }

    /**
     * Gets the cells that changed in the previous generation
     * @return Returns the cells that changed in the previous generation
     */
    public Set<Coordinate> getCellsChanged() {
        return cellsChangedArray.get(0);
    }

    /**
     * Sets the rule of the simulator. <br>
     * IMPORTANT: DO NOT CHANGE THE RULE OF THE SIMULATOR WITH ANY OTHER FUNCTION! (Because bugzzz...)
     * @param rule Rule to set the simulator to
     */
    public void setRule(Rule rule) {
        cellsChangedArray.clear();  // Changing length to alternating period of the rule
        for (int i = 0; i < rule.getAlternatingPeriod(); i++) {
            cellsChangedArray.add(new HashSet<>());
        }

        // If the rule changes, you have to re-evaluate each cell
        this.iterateCells(coordinate -> {
            cellsChangedArray.get(0).add(coordinate);
            if (getCell(coordinate) >= rule.getNumStates()) setCell(coordinate, 0);
        });
        this.rule = rule;
    }

    /**
     * Set the generation of the simulator
     * @param generation Generation to set the simulator to
     */
    public void setGeneration(int generation) {
        this.generation = generation;
    }

    /**
     * Identify a pattern (still life / oscillator / spaceship) with a max period of 2000
     * @return The identified pattern
     */
    public Pattern identify() {
        return identify(2000);
    }

    /**
     * Identify a pattern (still life / oscillator / spaceship) with the specified max period
     * @param maxPeriod The max period to check for periodicity
     * @return The identified pattern
     */
    public Pattern identify(int maxPeriod) {
        return identify(maxPeriod, pattern -> true);
    }

    /**
     * Identify a pattern (still life / oscillator / spaceship) with the specified max period
     * @param maxPeriod The max period to check for periodicity
     * @param continueIdentification Checks if the identification should continue
     * @return The identified pattern
     */
    public Pattern identify(int maxPeriod, Function<Grid, Boolean> continueIdentification) {
        // TODO (More specific identification for guns and replicators)
        // Hash map to store hashes among other things
        updateBounds();

        HashMap<Integer, Object[]> hashMap = new HashMap<>();
        hashMap.put(this.hashCode(), new Object[]{generation, getPopulation(), getBounds(), this.deepCopy()});

        // Population list for linear growth identification
        int[] popList = new int[maxPeriod + 1];

        // Stuff for zz identification
        int[] popList2 = new int[maxPeriod + 2];
        popList2[0] = getPopulation();

        ArrayList<Pair<Double, Double>> pointList = new ArrayList<>();

        // List of grids to find min, max rule
        ArrayList<Grid> grids = new ArrayList<>();
        grids.add(this.deepCopy());  // Adding initial grid

        // The pattern
        Pattern pattern = null;

        // Take note of the generation numbers
        int firstPhaseGeneration = 0;
        int initialGeneration = generation;

        // Running the pattern for maxPeriod and see what happens
        for (int i = 0; i < maxPeriod; i++) {
            step();

            int hash = this.hashCode() + 31 * generation % rule.getAlternatingPeriod();  // Compute the hash
            if (hashMap.containsKey(hash)) {
                // Calculates the displacement between the first 2 bounds
                int displacementX = ((Coordinate) ((Pair) hashMap.get(hash)[2]).getValue0()).getX() -
                        getBounds().getValue0().getX();
                int displacementY = ((Coordinate) ((Pair) hashMap.get(hash)[2]).getValue0()).getY() -
                        getBounds().getValue0().getY();

                // Calculates the displacement between the other 2 bounds
                int displacementX2 = ((Coordinate) ((Pair) hashMap.get(hash)[2]).getValue1()).getX() -
                        getBounds().getValue1().getX();
                int displacementY2 = ((Coordinate) ((Pair) hashMap.get(hash)[2]).getValue1()).getY() -
                        getBounds().getValue1().getY();

                if ((int) hashMap.get(hash)[1] != getPopulation() || displacementX != displacementX2 ||
                        displacementY != displacementY2)
                    continue;

                // In case of hash collisions
                if (!slowEquals((Grid) hashMap.get(hash)[3], displacementX, displacementY))
                    continue;

                if (displacementX == 0 && displacementY == 0) {  // Checking for movement
                    pattern = new Oscillator((Rule) ((RuleFamily) rule).clone(), this.deepCopy(),
                            generation - (int) hashMap.get(hash)[0]);
                }
                else {
                    pattern = new Spaceship((Rule) ((RuleFamily) rule).clone(), this.deepCopy(),
                            generation - (int) hashMap.get(hash)[0],
                            displacementX, displacementY);
                }

                // Taking note of the generation
                firstPhaseGeneration = (int) hashMap.get(hash)[0];

                // Add the last phase into the dictionary
                Grid deepCopy = this.deepCopy();
                deepCopy.setBackground(rule.convertState(0, generation));

                grids.add(deepCopy);
                break;
            }
            else {
                // Adding to the grids list
                Grid deepCopy = this.deepCopy();
                
                Grid deepCopy2 = deepCopy.deepCopy();  // Accounting for B0 rules
                deepCopy2.setBackground(rule.convertState(0, generation));
                grids.add(deepCopy2);

                // Adding to the hashmap and population list
                hashMap.put(hash, new Object[]{generation, getPopulation(), getBounds(), deepCopy});

                popList[i] = getPopulation();
                popList2[i + 1] = getPopulation() + popList2[i];
                if (i > maxPeriod / 2) pointList.add(new Pair<>(Math.log10(i), Math.log10(popList2[i] + 1)));

                // Should identification continue
                if (!continueIdentification.apply(deepCopy2))
                    return null;

                // Checking for linear growth / zz_WHATEVER at the end of period detection
                if (i == maxPeriod - 1) {
                    // Taking note of the generation
                    firstPhaseGeneration = initialGeneration;

                    // Checking for linear growth
                    int popPeriod = LinearGrowth.deepPeriod(popList, i / 3, 1);
                    if (popPeriod != -1) {
                        pattern = new LinearGrowth((Rule) ((RuleFamily) rule).clone(), this.deepCopy(), popPeriod);
                        break;
                    }

                    // Checking for zz_WHATEVER
                    double power = PowerLawPattern.regress(pointList);
                    if (power > 1.10) {
                        pattern = new PowerLawPattern((Rule) ((RuleFamily) rule).clone(), this.deepCopy(), power);
                        break;
                    }
                }
            }
        }

        if (pattern != null) {
            // Adding grids to an array
            Grid[] gridsArray = new Grid[generation - firstPhaseGeneration + 1];
            for (int i = firstPhaseGeneration - initialGeneration; i < grids.size(); i++) {
                gridsArray[i - (firstPhaseGeneration - initialGeneration)] = grids.get(i);
            }

            pattern.generateMinMaxRule(gridsArray);
        }

        return pattern;
    }

    /**
     * Step the simulation forward 1 generation
     */
    public void step() {
        rule.step(super.shallowCopy(), cellsChangedArray, generation, null);
        generation += 1;
    }

    /**
     * Step a portion of the simulation forward 1 generation
     * @param step A function that returns whether the cell at that coordinate should be stepped forward.
     */
    public void step(Function<Coordinate, Boolean> step) {
        rule.step(super.shallowCopy(), cellsChangedArray, generation, step);
        generation += 1;
    }

    /**
     * Sets the cell at position coordinate to the specified state
     * @param coordinate The coordinate of the cell
     * @param state The state of the cell
     */
    @Override
    public void setCell(Coordinate coordinate, int state) {
        super.setCell(coordinate, state);
        cellsChangedArray.get(0).add(coordinate);
    }

    /**
     * Sets the cell at position (x, y) to the specified state
     * @param x The x-coordinate of the cell
     * @param y The y-coordinate of the cell
     * @param state The state of the cell
     */
    @Override
    public void setCell(int x, int y, int state) {
        super.setCell(x, y, state);
        cellsChangedArray.get(0).add(new Coordinate(x, y));
    }

    /**
     * Clears all cells between the coordinates specified
     * @param start The starting coordinate
     * @param end The end coordinate
     */
    @Override
    public void clearCells(Coordinate start, Coordinate end) {
        super.clearCells(start, end);

        for (int x = start.getX(); x < end.getX() + 1; x++) {
            for (int y = start.getY(); y < end.getY() + 1; y++) {
                cellsChangedArray.get(0).add(new Coordinate(x, y));
            }
        }
    }

    /**
     * Converts the pattern in the given bounds into an RLE
     * @param startCoordinate The start coordinate
     * @param endCoordinate The end coordinate
     * @return Returns the RLE
     */
    @Override
    public String toRLE(Coordinate startCoordinate, Coordinate endCoordinate) {
        if (rule.getNumStates() == 2)  // For 2-state rules
            return super.toRLE(startCoordinate, endCoordinate).replace("A", "o").
                    replace(".", "b");
        return super.toRLE(startCoordinate, endCoordinate);
    }
}
