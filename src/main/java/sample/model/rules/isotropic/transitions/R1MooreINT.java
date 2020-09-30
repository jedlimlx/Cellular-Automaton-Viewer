package sample.model.rules.isotropic.transitions;

import sample.model.Coordinate;

import java.util.ArrayList;

/**
 * Represents range 1 moore INT transitions (hensel notation)
 */
public class R1MooreINT extends SingleLetterTransitions {
    /**
     * Constructs the range 1 moore INT transitions with hensel notation
     * @param string The hensel notation string
     */
    public R1MooreINT(String string) {
        super(string);

        neighbourhood = new Coordinate[]{
                new Coordinate(-1, 1),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(1, -1),
                new Coordinate(0, -1),
                new Coordinate(-1, -1),
                new Coordinate(-1, 0)
        };

        readTransitionsFromFile(getClass().getResourceAsStream("/int/r1_moore.txt"));

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
        return new R1MooreINT(transitionString);
    }

    private ArrayList<Integer> rotate(ArrayList<Integer> transition) {
        int[] rotateSymmetry = new int[]{2, 3, 4, 5, 6, 7, 0, 1};

        ArrayList<Integer> rotated = new ArrayList<>();
        for (int index: rotateSymmetry) {
            rotated.add(transition.get(index));
        }

        return rotated;
    }

    private ArrayList<Integer> reflect1(ArrayList<Integer> transition) {
        int[] reflectSymmetry = new int[]{2, 1, 0, 7, 6, 5, 4, 3};

        ArrayList<Integer> reflected = new ArrayList<>();
        for (int index: reflectSymmetry) {
            reflected.add(transition.get(index));
        }

        return reflected;
    }

    private ArrayList<Integer> reflect2(ArrayList<Integer> transition) {
        int[] reflectSymmetry = new int[]{6, 5, 4, 3, 2, 1, 0, 7};

        ArrayList<Integer> reflected = new ArrayList<>();
        for (int index: reflectSymmetry) {
            reflected.add(transition.get(index));
        }

        return reflected;
    }
}
