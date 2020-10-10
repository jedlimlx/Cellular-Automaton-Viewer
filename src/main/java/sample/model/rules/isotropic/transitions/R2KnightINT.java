package sample.model.rules.isotropic.transitions;

import sample.model.Coordinate;
import sample.model.rules.ruleloader.ruletable.Symmetry;

import java.util.ArrayList;

/**
 * Represents range 2 knight INT transitions
 */
public class R2KnightINT extends SingleLetterTransitions {
    public R2KnightINT() {
        this("");
    }

    /**
     * Constructs the range 2 knight INT transitions
     * @param string The string that represents the transitions
     */
    public R2KnightINT(String string) {
        super(string);

        neighbourhood = new Coordinate[]{
                new Coordinate(1, -2),
                new Coordinate(2, -1),
                new Coordinate(2, 1),
                new Coordinate(1, 2),
                new Coordinate(-1, 2),
                new Coordinate(-2, 1),
                new Coordinate(-2, -1),
                new Coordinate(-1, -2)
        };

        readTransitionsFromFile(getClass().getResourceAsStream("/int/r2_knight.txt"));

        parseTransitions(string);
        transitionString = canoniseTransitions();
    }

    @Override
    protected ArrayList<ArrayList<Integer>> getSymmetries(ArrayList<Integer> transition) {
        Symmetry symmetry = new Symmetry("[[(1, 3, 5, 7), (2, 4, 6, 8)], [(3, 8), (4, 7)]]");
        return symmetry.applySymmetry(transition);
    }
    
    @Override
    public Object clone() {
        return new R2KnightINT(transitionString);
    }
}
