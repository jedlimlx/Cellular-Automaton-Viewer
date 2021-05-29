package application.model.rules.isotropic.transitions;

import application.model.Coordinate;
import application.model.rules.ruleloader.ruletable.Symmetry;

import java.util.ArrayList;

/**
 * Represents range 2 checkboard INT transitions
 */
public class R2CheckerboardINT extends DoubleLetterTransitions {
    private static final Symmetry symmetry = new Symmetry("[[(1, 3, 5, 7), (2, 4, 6, 8)], " +
            "[(1, 8), (2, 7), (3, 6), (4, 5), (9, 11)]]");
    private static final Symmetry isotropicSymmetry = new Symmetry("[[(1, 3, 5, 7), (2, 4, 6, 8)], " +
            "[(1, 8), (2, 7), (3, 6), (4, 5)]");

    public R2CheckerboardINT() {
        this("");
    }

    /**
     * Constructs the INT transitions that consist of 2 letters
     * @param string The string representation of the INT transitions
     */
    public R2CheckerboardINT(String string) {
        super(string);

        neighbourhood = new Coordinate[]{
                new Coordinate(-2, 1),
                new Coordinate(-1, 2),
                new Coordinate(1, 2),
                new Coordinate(2, 1),
                new Coordinate(2, -1),
                new Coordinate(1, -2),
                new Coordinate(-1, -2),
                new Coordinate(-2, -1),
                new Coordinate(0, 1),
                new Coordinate(-1, 0),
                new Coordinate(0, -1),
                new Coordinate(1, 0)
        };

        readTransitionsFromFile(getClass().getResourceAsStream("/int/anisotropic_vn.txt"),
                getClass().getResourceAsStream("/int/r2_knight.txt"));

        parseTransitions(string);
        transitionString = canoniseTransitions();
    }

    @Override
    protected ArrayList<ArrayList<Integer>> getSymmetries(ArrayList<Integer> transition) {
        return symmetry.applySymmetry(transition);
    }

    @Override
    protected ArrayList<ArrayList<Integer>> getIsotropicSymmetries(ArrayList<Integer> transition) {
        return isotropicSymmetry.applySymmetry(transition);
    }

    @Override
    public Object clone() {
        return new R2CheckerboardINT(transitionString);
    }
}
