package application.model.rules.isotropic.transitions;

import application.model.Coordinate;
import application.model.rules.ruleloader.ruletable.Symmetry;

import java.util.ArrayList;

/**
 * Represents range 2 knight INT transitions
 */
public class R2KnightINT extends SingleLetterTransitions {
    private static final Symmetry symmetry = new Symmetry("[[(1, 3, 5, 7), (2, 4, 6, 8)], " +
            "[(1, 8), (2, 7), (3, 6), (4, 5)]]");

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
                new Coordinate(-2, 1),
                new Coordinate(-1, 2),
                new Coordinate(1, 2),
                new Coordinate(2, 1),
                new Coordinate(2, -1),
                new Coordinate(1, -2),
                new Coordinate(-1, -2),
                new Coordinate(-2, -1)
        };

        readTransitionsFromFile(getClass().getResourceAsStream("/int/r2_knight.txt"));

        parseTransitions(string);
        transitionString = canoniseTransitions();
    }

    @Override
    protected ArrayList<ArrayList<Integer>> getSymmetries(ArrayList<Integer> transition) {
        return symmetry.applySymmetry(transition);
    }
    
    @Override
    public Object clone() {
        return new R2KnightINT(transitionString);
    }
}
