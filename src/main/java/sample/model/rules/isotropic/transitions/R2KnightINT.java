package sample.model.rules.isotropic.transitions;

import sample.model.Coordinate;

import java.util.ArrayList;

/**
 * Represents range 2 knight INT transitions
 */
public class R2KnightINT extends SingleLetterTransitions {
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
        // TODO (Remove unnecessary transitions)

        ArrayList<ArrayList<Integer>> symmetries = new ArrayList<>();
        ArrayList<Integer> rotate1 = rotate(transition);
        ArrayList<Integer> rotate2 = rotate(rotate1);
        ArrayList<Integer> rotate3 = rotate(rotate2);

        symmetries.add(transition);
        symmetries.add(reflect1(transition));
        symmetries.add(reflect2(transition));

        symmetries.add(rotate1);
        symmetries.add(reflect1(rotate1));
        symmetries.add(reflect2(rotate1));

        symmetries.add(rotate2);
        symmetries.add(reflect1(rotate2));
        symmetries.add(reflect2(rotate2));

        symmetries.add(rotate3);
        symmetries.add(reflect1(rotate3));
        symmetries.add(reflect2(rotate3));

        return symmetries;
    }
    
    @Override
    public Object clone() {
        return new R2KnightINT(transitionString);
    }

    private ArrayList<Integer> rotate(ArrayList<Integer> transition) {
        int[] rotateSymmetry = new int[]{6, 7, 0, 1, 2, 3, 4, 5};

        ArrayList<Integer> rotated = new ArrayList<>();
        for (int index: rotateSymmetry) {
            rotated.add(transition.get(index));
        }

        return rotated;
    }

    private ArrayList<Integer> reflect1(ArrayList<Integer> transition) {
        int[] reflectSymmetry = new int[]{3, 2, 1, 0, 7, 6, 5, 4};

        ArrayList<Integer> reflected = new ArrayList<>();
        for (int index: reflectSymmetry) {
            reflected.add(transition.get(index));
        }

        return reflected;
    }

    private ArrayList<Integer> reflect2(ArrayList<Integer> transition) {
        int[] reflectSymmetry = new int[]{7, 6, 5, 4, 3, 2, 1, 0};

        ArrayList<Integer> reflected = new ArrayList<>();
        for (int index: reflectSymmetry) {
            reflected.add(transition.get(index));
        }

        return reflected;
    }
}
