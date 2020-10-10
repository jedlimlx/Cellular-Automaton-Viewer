package sample.model.rules.isotropic.transitions;

import sample.model.Coordinate;
import sample.model.rules.ruleloader.ruletable.Symmetry;

import java.util.ArrayList;

/**
 * Represents range 2 cross INT transitions
 */
public class R2CrossINT extends SingleLetterTransitions {
    public R2CrossINT() {
        this("");
    }

    /**
     * Constructs the range 2 cross INT transitions
     * @param string The string that represents the transitions
     */
    public R2CrossINT(String string) {
        super(string);

        neighbourhood = new Coordinate[]{
                new Coordinate(0, 2),
                new Coordinate(2, 0),
                new Coordinate(0, -2),
                new Coordinate(-2, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 0),
                new Coordinate(0, -1),
                new Coordinate(-1, 0)
        };

        readTransitionsFromFile(getClass().getResourceAsStream("/int/r2_cross.txt"));

        parseTransitions(string);
        transitionString = canoniseTransitions();
    }

    @Override
    protected ArrayList<ArrayList<Integer>> getSymmetries(ArrayList<Integer> transition) {
        Symmetry symmetry = new Symmetry("[[(1, 2, 3, 4), (5, 6, 7, 8)], [(2, 4), (6, 8)]]");
        return symmetry.applySymmetry(transition);
    }
    
    @Override
    public Object clone() {
        return new R2CrossINT(transitionString);
    }
}
