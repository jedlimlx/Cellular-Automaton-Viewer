package sample.model;

import org.javatuples.Pair;
import sample.model.patterns.Oscillator;
import sample.model.patterns.Pattern;
import sample.model.patterns.Spaceship;
import sample.model.rules.Rule;
import sample.model.rules.RuleFamily;

import java.util.ArrayList;
import java.util.HashMap;
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
    public Pattern identify() {
        return identify(5000);
    }

    public Pattern identify(int maxPeriod) {
        return identify(maxPeriod, false, 0);
    }

    public Pattern identify(int maxPeriod, boolean checkBoundExpansion, int maxBound) {
        // TODO (Identification for guns, puffers, rakes and replicators)
        // Hash map to store hashes among other things
        updateBounds();

        HashMap<Integer, Object[]> hashMap = new HashMap<>();
        hashMap.put(this.hashCode(), new Object[]{generation, size(), getBounds(), this.deepCopy()});

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

            int hash = this.hashCode();  // Compute the hash
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

                if ((int) hashMap.get(hash)[1] != size() || displacementX != displacementX2 ||
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
                grids.add(this.deepCopy());
                break;
            }
            else {
                // Adding to the grids list
                Grid deepCopy = this.deepCopy();
                grids.add(deepCopy);

                // Adding to the hashmap
                hashMap.put(hash, new Object[]{generation, size(), getBounds(), deepCopy});

                // If it exceeds the maximum bound
                if (checkBoundExpansion) {
                    if ((getBounds().getValue1().getX() - getBounds().getValue0().getX()) > maxBound ||
                            (getBounds().getValue1().getY() - getBounds().getValue0().getY()) > maxBound) {
                        return null;
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
