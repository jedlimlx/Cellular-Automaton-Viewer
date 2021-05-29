package application.model.rules.isotropic.transitions;

import application.model.Coordinate;
import application.model.rules.ruleloader.ruletable.Symmetry;

import java.util.ArrayList;

/**
 * Represents range 1 moore INT transitions (hensel notation)
 */
public class R1MooreINT extends SingleLetterTransitions {
    private static final Symmetry symmetry = new Symmetry("[[(1, 3, 5, 7), (2, 4, 6, 8)], [(2, 8), (3, 7), (4, 6)]]");

    public R1MooreINT() {
        this("");
    }

    /**
     * Constructs the range 1 moore INT transitions with hensel notation
     * @param string The hensel notation string
     */
    public R1MooreINT(String string) {
        super(string);

        neighbourhood = new Coordinate[]{
                new Coordinate(0, 1),
                new Coordinate(-1, 1),
                new Coordinate(-1, 0),
                new Coordinate(-1, -1),
                new Coordinate(0, -1),
                new Coordinate(1, -1),
                new Coordinate(1, 0),
                new Coordinate(1, 1)
        };

        readTransitionsFromFile(getClass().getResourceAsStream("/int/r1_moore.txt"));

        parseTransitions(string);
        transitionString = canoniseTransitions();
    }

    @Override
    protected ArrayList<ArrayList<Integer>> getSymmetries(ArrayList<Integer> transition) {
        return symmetry.applySymmetry(transition);
    }

    @Override
    public Object clone() {
        return new R1MooreINT(transitionString);
    }
}
