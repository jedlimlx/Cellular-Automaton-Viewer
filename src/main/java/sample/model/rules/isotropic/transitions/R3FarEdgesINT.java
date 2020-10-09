package sample.model.rules.isotropic.transitions;

import sample.model.Coordinate;
import sample.model.rules.ruleloader.ruletable.Symmetry;

import java.util.ArrayList;

/**
 * Represents range 3 far edges INT transitions (hensel notation)
 */
public class R3FarEdgesINT extends SingleLetterTransitions {
    /**
     * Constructs the range 3 far edges INT transitions with hensel notation
     * @param string The hensel notation string
     */
    public R3FarEdgesINT(String string) {
        super(string);

        neighbourhood = new Coordinate[]{
                new Coordinate(-1, 1),
                new Coordinate(0, 3),
                new Coordinate(1, 1),
                new Coordinate(3, 0),
                new Coordinate(1, -1),
                new Coordinate(0, -3),
                new Coordinate(-1, -1),
                new Coordinate(-3, 0)
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
        return new R3FarEdgesINT(transitionString);
    }
}
