package application.model;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Useful methods for generating comments to be placed in the RLE (multi-line rulestrings).
 */
public class CommentGenerator {
    /**
     * Generates comments from the provided weights and neighbourhood
     * @param weights The weights of the rule
     * @param neighbourhood The neighbourhood of the rule
     * @return Returns an arraylist containing the comments each starting with "#R"
     */
    public static ArrayList<String> generateFromWeights(int[] weights, Coordinate[] neighbourhood) {
        if (weights == null) {
            weights = new int[neighbourhood.length];
            Arrays.fill(weights, 1);
        }

        int range = 0;
        ArrayList<Coordinate> neighbourhoodList = new ArrayList<>();
        for (Coordinate coordinate: neighbourhood) {
            neighbourhoodList.add(coordinate);
            range = Math.max(range, Math.max(Math.abs(coordinate.getX()), Math.abs(coordinate.getY())));
        }

        // The array of RLE comments
        String[] comments = new String[2 * range + 1];

        for (int i = -range; i <= range; i++) {
            comments[i + range] = "#R ";
            for (int j = -range; j <= range; j++) {
                int index = neighbourhoodList.indexOf(new Coordinate(i, j));
                if (index != -1) {
                    comments[i + range] += weights[index];
                }
                else {
                    comments[i + range] += 0;
                }

                comments[i + range] += " ";
            }
        }

        return new ArrayList<>(Arrays.asList(comments));
    }

    /**
     * Gets the weights and neighbourhood of a rule from the comments
     * @param comments The comments from the RLE
     * @return A pair containing the weights and the neighbourhood.
     */
    public static Pair<int[], Coordinate[]> getWeightsFromComments(String[] comments) {
        int range = comments.length / 2;
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        ArrayList<Integer> weights = new ArrayList<>();

        for (int j = 0; j < comments.length; j++) {  // Parsing comments for the neighbourhood
            String[] tokens = comments[j].split(" ");
            for (int i = 1; i < tokens.length; i++) {
                if (!tokens[i].matches("0?\\s*")) {
                    neighbourhood.add(new Coordinate(i - 1 - range, j - range));
                    weights.add(Integer.parseInt(tokens[i]));
                }
            }
        }

        // Converting to arrays because java is annoying
        int[] weightsArray = new int[weights.size()];
        Coordinate[] neighbourhoodArray = new Coordinate[neighbourhood.size()];
        for (int i = 0; i < weights.size(); i++) {
            weightsArray[i] = weights.get(i);
            neighbourhoodArray[i] = neighbourhood.get(i);
        }

        return new Pair<>(weightsArray, neighbourhoodArray);
    }
}
