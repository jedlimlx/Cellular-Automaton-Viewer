package application.model.rules.isotropic.transitions;

import application.model.Coordinate;
import application.model.rules.ruleloader.ruletable.Symmetry;

import java.util.ArrayList;

/**
 * Represents range 3 cross INT transitions
 */
public class R3CrossINT extends DoubleLetterTransitions {
    private static final Symmetry symmetry = new Symmetry("[[(1, 2, 3, 4), (5, 6, 7, 8), (9, 10, 11, 12)], " +
            "[(2, 4), (6, 8), (10, 12)]]");
    private static final Symmetry isotropicSymmetry = new Symmetry("[[(1, 2, 3, 4), (5, 6, 7, 8)], [(2, 4), (6, 8)]]");

    public R3CrossINT() {
        this("");
    }

    /**
     * Constructs the INT transitions that consist of 2 letters
     * @param string The string representation of the INT transitions
     */
    public R3CrossINT(String string) {
        super(string);

        neighbourhood = new Coordinate[]{
                new Coordinate(0, 2),
                new Coordinate(2, 0),
                new Coordinate(0, -2),
                new Coordinate(-2, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 0),
                new Coordinate(0, -1),
                new Coordinate(-1, 0),
                new Coordinate(0, 3),
                new Coordinate(3, 0),
                new Coordinate(0, -3),
                new Coordinate(-3, 0)
        };

        readTransitionsFromFile(getClass().getResourceAsStream("/int/anisotropic_vn.txt"),
                getClass().getResourceAsStream("/int/r2_cross.txt"));

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
        return new R3CrossINT(transitionString);
    }
}
