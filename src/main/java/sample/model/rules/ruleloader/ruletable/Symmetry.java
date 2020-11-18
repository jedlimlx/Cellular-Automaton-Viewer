package sample.model.rules.ruleloader.ruletable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents symmetries as disjoint cycles like lifelib.
 * See https://groupprops.subwiki.org/wiki/Cycle_decomposition_for_permutations.
 */
public class Symmetry {
    /**
     * The disjoint cycles used to represent the symmetry
     */
    private final int[][][] disjointCycles;

    /**
     * Constructs a new symmetry with an inputted string.
     * For example, [[(1, 3, 5, 7), (2, 4, 6, 8)], [(2, 8), (3, 7), (4, 6)]]
     * @param symmetry The symmetry string that represents the symmetry in the form of disjoint cycles.
     */
    public Symmetry(String symmetry) {
        Matcher matcher = Pattern.compile("\\[(\\((\\d+,?\\s*)+\\),?\\s*)+]").matcher(symmetry);
        Matcher matcher2;

        ArrayList<Integer> cycle;
        ArrayList<int[]> cycles;
        ArrayList<int[][]> disjointCyclesList = new ArrayList<>();
        while (matcher.find()) {
            cycles = new ArrayList<>();
            matcher2 = Pattern.compile("(\\d+,?\\s*)+").matcher(matcher.group());
            while (matcher2.find()) {
                cycle = new ArrayList<>();
                String[] tokens = matcher2.group().split(",\\s*");
                for (String token: tokens) {
                    cycle.add(Integer.parseInt(token));
                }

                cycles.add(cycle.stream().mapToInt(Integer::intValue).toArray());
            }

            disjointCyclesList.add(cycles.toArray(new int[0][]));
        }

        this.disjointCycles = disjointCyclesList.toArray(new int[0][][]);
    }

    /**
     * Constructs a new symmetry with the provided disjoint cycles
     * @param disjointCycles The disjoint cycles that represent the symmetry
     */
    public Symmetry(int[][][] disjointCycles) {
        this.disjointCycles = disjointCycles;
    }

    /**
     * Applies the symmetry on the provided array list
     * @param neighbours The neighbours of the cell that the symmetry should be applied on
     * @param <T> The type of the array list
     * @return Returns an array list of array lists represents the permutations
     */
    public <T> ArrayList<ArrayList<T>> applySymmetry(ArrayList<T> neighbours) {
        ArrayList<Integer> symmetry;
        ArrayList<ArrayList<Integer>> symmetries = new ArrayList<>();

        // Converting disjoint cycles to permutation generators
        for (int[][] cycles: disjointCycles) {
            symmetry = new ArrayList<>();
            for (int i = 0; i < neighbours.size(); i++) {
                symmetry.add(i + 1);
            }

            if (neighbours.size() != 0) {
                for (int[] cycle : cycles) {  // Definitely not taken from lifelib
                    for (int i = 0; i < cycle.length; i++) {
                        if (i - 1 < 0) symmetry.set(cycle[cycle.length + i - 1] - 1, cycle[i]);
                        else symmetry.set(cycle[i - 1] - 1, cycle[i]);
                    }
                }
            }

            symmetries.add(symmetry);
        }

        ArrayList<ArrayList<T>> permutations = new ArrayList<>();
        permutations.add((ArrayList<T>) neighbours.clone());

        int i = 0;  // Generating the permutations
        HashSet<ArrayList<T>> used = new HashSet<>();
        ArrayList<T> currentPermutation, composedPermutation;
        while (i < permutations.size()) {
            currentPermutation = permutations.get(i++);

            // Compose the current permutation with the and include any
            // previously-unseen elements to the permutation group
            for (ArrayList<Integer> symmetry1: symmetries) {
                // Composing permutations
                composedPermutation = new ArrayList<>();
                for (int x: symmetry1) composedPermutation.add(currentPermutation.get(x - 1));

                if (!used.contains(composedPermutation)) {
                    used.add(composedPermutation);
                    permutations.add(composedPermutation);
                }
            }
        }

        return permutations;
    }
}
