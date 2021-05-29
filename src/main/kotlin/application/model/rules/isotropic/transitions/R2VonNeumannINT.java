package application.model.rules.isotropic.transitions;

import application.model.Coordinate;
import application.model.rules.ruleloader.ruletable.Symmetry;

import java.util.ArrayList;

/**
 * Represents range 2 von neumann transitions based on the Feb 24 notation by AForAmpere and MiloJacquet
 */
public class R2VonNeumannINT extends DoubleLetterTransitions {
    private static final Symmetry symmetry = new Symmetry("[[(1, 3, 5, 7), (2, 4, 6, 8), (9, 10, 11, 12)], " +
            "[(2, 8), (3, 7), (4, 6), (10, 12)]]");
    private static final Symmetry isotropicSymmetry = new Symmetry("[[(1, 3, 5, 7), (2, 4, 6, 8)], " +
            "[(2, 8), (3, 7), (4, 6)]]");

    public R2VonNeumannINT() {
        this("");
    }

    /**
     * Constructs the INT transitions that consist of 2 letters
     * @param string The string representation of the INT transitions
     */
    public R2VonNeumannINT(String string) {
        super(string);

        neighbourhood = new Coordinate[]{
                new Coordinate(0, 1),
                new Coordinate(-1, 1),
                new Coordinate(-1, 0),
                new Coordinate(-1, -1),
                new Coordinate(0, -1),
                new Coordinate(1, -1),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 2),
                new Coordinate(-2, 0),
                new Coordinate(0, -2),
                new Coordinate(2, 0)
        };

        readTransitionsFromFile(getClass().getResourceAsStream("/int/anisotropic_vn.txt"),
                getClass().getResourceAsStream("/int/r1_moore.txt"));

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
        return new R2VonNeumannINT(transitionString);
    }
}
