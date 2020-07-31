package sample.model;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;

public class Simulator extends Grid {
    private int generation;
    private Rule rule;
    private final HashSet<Coordinate> cellsChanged;

    public Simulator(Rule rule) {
        this.rule = rule;
        this.generation = 0;
        this.cellsChanged = new HashSet<>();
    }

    // Accessor
    public Rule getRule() {
        return rule;
    }

    public int getGeneration() {
        return generation;
    }

    public HashSet<Coordinate> getCellsChanged() {
        return cellsChanged;
    }

    // Mutators
    public void setRule(Rule rule) {
        // If the rule changes, you have to re-evaluate each cell
        for (Coordinate cell: this) {
            cellsChanged.add(cell);
        }

        this.rule = rule;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    // Identification functions
    public String identify() {
        return identify(5000);
    }

    public String identify(int maxPeriod) {  // TODO (Identification for puffers, rakes and replicators)
        boolean found;
        int period = 0, displacementX = 0, displacementY = 0;

        Grid grid = super.deepCopy();  // Make a deep copy for reference

        Coordinate[] originalGridArray = grid.toArray();  // Converting to array
        Arrays.sort(originalGridArray);  // Sorting the array

        for (int i = 0; i < maxPeriod; i++) {
            step();

            Coordinate[] gridArray = this.toArray();
            Arrays.sort(gridArray);  // Sorting the array
            if (gridArray.length == 0)
                return "Still Life";

            // Initialise variables
            found = true;
            displacementX = originalGridArray[0].getX() - gridArray[0].getX();
            displacementY = originalGridArray[0].getY() - gridArray[0].getY();

            if (grid.size() == this.size()) {  // Check for population periodicity
                for (Coordinate cell: originalGridArray) {
                    if (grid.getCell(cell) != this.getCell(
                            cell.add(new Coordinate(-displacementX, -displacementY)))) {
                        found = false;
                        break;
                    }
                }

                if (found) {
                    period = i + 1;
                    break;
                }
            }
        }

        if (period > 0) {  // If your period is 0, nothing was found
            if (displacementX == 0 && displacementY == 0) {  // Checking if it is stationary
                if (period == 1) {
                    return "Still Life";
                }
                else {
                    return "P" + period + " Oscillator";
                }
            }
            else {
                return "(" + displacementX + ", " + displacementY + ")c/" + period;
            }
        }

        return "Identification Failed :(";
    }

    // Step 1 generation
    public void step() {
        rule.step(super.shallowCopy(), cellsChanged, generation);
        generation += 1;
    }

    @Override // Adds cell to grid and to cells changed
    public void setCell(Coordinate coordinate, int state) {
        super.setCell(coordinate, state);
        cellsChanged.add(coordinate);
    }

    @Override
    public void setCell(int x, int y, int state) {
        super.setCell(x, y, state);
        cellsChanged.add(new Coordinate(x, y));
    }
}
