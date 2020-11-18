package sample.model.rules.isotropic.transitions;

import sample.model.Coordinate;
import sample.model.rules.ruleloader.ruletable.Symmetry;

import java.util.ArrayList;

/**
 * Represents range 1 hex INT transitions (based on arene substitution patterns)
 */
public class R1HexINT extends SingleLetterTransitions {
    private static final Symmetry symmetry = new Symmetry("[[(1, 2, 3, 4, 5, 6)], [(1, 6), (2, 5), (3, 4)]]");

    public R1HexINT() {
        this("");
    }

    /**
     * Constructs the range 1 moore hex transitions
     * @param string The hex transitions string
     */
    public R1HexINT(String string) {
        super(string);

        neighbourhood = new Coordinate[]{
                new Coordinate(0, -1),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1),
                new Coordinate(-1, 0),
                new Coordinate(-1, -1)
        };

        readTransitionsFromFile(getClass().getResourceAsStream("/int/r1_hex.txt"));

        parseTransitions(string);
        transitionString = canoniseTransitions();
    }

    @Override
    protected ArrayList<ArrayList<Integer>> getSymmetries(ArrayList<Integer> transition) {
        return symmetry.applySymmetry(transition);
    }

    @Override
    public Object clone() {
        return new R1HexINT(transitionString);
    }
}

