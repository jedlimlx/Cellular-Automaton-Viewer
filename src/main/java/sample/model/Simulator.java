package sample.model;

import javafx.util.Pair;
import sample.model.patterns.Oscillator;
import sample.model.patterns.Pattern;
import sample.model.patterns.Spaceship;
import sample.model.rules.Rule;
import sample.model.rules.RuleFamily;

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

    public Pattern identify(int maxPeriod) {  // TODO (Identification for guns, puffers, rakes and replicators)
        // Hash map to store hashes among other things
        updateBounds();

        HashMap<Integer, Object[]> hashMap = new HashMap<>();
        hashMap.put(this.hashCode(), new Object[]{generation, size(), getBounds()});

        // The pattern
        Pattern pattern = null;

        for (int i = 0; i < maxPeriod; i++) {
            step();

            int hash = this.hashCode();  // Compute the hash
            if (hashMap.containsKey(hash)) {
                if ((int) hashMap.get(hash)[1] != size())
                    continue;

                if (getBounds().equals(hashMap.get(hash)[2])) {  // Checking for movement
                    pattern = new Oscillator((Rule) ((RuleFamily) rule).clone(), this.deepCopy(),
                            generation - (int) hashMap.get(hash)[0]);
                }
                else {
                    // Calculates the displacement (I hate casting)
                    int displacementX = ((Coordinate) ((Pair) hashMap.get(hash)[2]).getKey()).getX() -
                            getBounds().getKey().getX();
                    int displacementY = ((Coordinate) ((Pair) hashMap.get(hash)[2]).getKey()).getY() -
                            getBounds().getKey().getY();

                    pattern = new Spaceship((Rule) ((RuleFamily) rule).clone(), this.deepCopy(),
                            generation - (int) hashMap.get(hash)[0],
                            displacementX, displacementY);
                }
                break;
            }
            else {
                hashMap.put(hash, new Object[]{generation, size(), getBounds()});
            }
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
