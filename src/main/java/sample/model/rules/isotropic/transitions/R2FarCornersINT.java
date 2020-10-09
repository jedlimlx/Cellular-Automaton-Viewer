package sample.model.rules.isotropic.transitions;

import sample.model.Coordinate;
import sample.model.rules.ruleloader.ruletable.Symmetry;

import java.util.ArrayList;

/**
 * Represents range 2 far corners INT transitions (hensel notation)
 */
public class R2FarCornersINT extends SingleLetterTransitions {
    /**
     * Constructs the range 2 far corners INT transitions with hensel notation
     * @param string The hensel notation string
     */
    public R2FarCornersINT(String string) {
        super(string);

        neighbourhood = new Coordinate[]{
                new Coordinate(-2, 2),
                new Coordinate(0, 1),
                new Coordinate(2, 2),
                new Coordinate(1, 0),
                new Coordinate(2, -2),
                new Coordinate(0, -1),
                new Coordinate(-2, -2),
                new Coordinate(-1, 0)
        };

        readTransitionsFromFile(getClass().getResourceAsStream("/int/r1_moore.txt"));

        parseTransitions(string);
        transitionString = canoniseTransitions();
    }

    @Override
    protected ArrayList<ArrayList<Integer>> getSymmetries(ArrayList<Integer> transition) {
        Symmetry symmetry = new Symmetry("[[(1, 3, 5, 7), (2, 4, 6, 8)], [(4, 8), (1, 3), (5, 7)]]");
        return symmetry.applySymmetry(transition);
    }
    
    @Override
    public Object clone() {
        return new R2FarCornersINT(transitionString);
    }
}
