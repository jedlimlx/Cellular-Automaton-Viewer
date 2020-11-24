package sample.model;

import org.javatuples.Pair;
import sample.model.rules.Tiling;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;

public class NeighbourhoodGenerator {
    // Modify to add new symbols for new neighbourhoods
    public static String neighbourhoodSymbols = "ABbCGHLMNX23*+#";
    public static Coordinate[] generateFromSymbol(char symbol, int range) {
        switch (symbol) {  // Add new neighbourhood types as cases here
            case 'A': return generateAsterisk(range);
            case 'B': return generateCheckerboard(range);
            case 'b': return generateAlignedCheckerboard(range);
            case 'C': return generateCircular(range);
            case 'G': return generateGaussianNeighbourhood(range);
            case 'H': return generateHexagonal(range);
            case 'L': return generateTriangularNeighbourhood(range);
            case 'N': return generateVonNeumann(range);
            case 'X': return generateSaltire(range);
            case '2': return generateEuclidean(range);
            case '3': return generateTripod(range);
            case '*': return generateStar(range);
            case '+': return generateCross(range);
            case '#': return generateHash(range);
            default: return generateMoore(range);
        }
    }
    public static int[] generateWeightsFromSymbol(char symbol, int range) {
        switch (symbol) {  // Add new weighted neighbourhood types as cases here
            case 'G': return generateGaussian(range);
            default: return null;
        }
    }
    public static Tiling generateTilingFromSymbol(char symbol) {
        switch (symbol) {  // Add new neighbourhood types as cases here
            case 'A':
            case 'H':
            case '3':
                return Tiling.Hexagonal;
            case 'L':
                return Tiling.Triangular;
            default: return Tiling.Square;
        }
    }

    // Neighbourhood generating functions
    public static Coordinate[] generateMoore(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i == 0 && j == 0) continue;  // Ignore center cell

                neighbourhood.add(new Coordinate(i, j));
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateVonNeumann(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i == 0 && j == 0) continue;  // Ignore center cell

                // Ignore center cell and cells whose coordinate sum >range
                if ((Math.abs(i) + Math.abs(j)) <= range) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateHexagonal(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                // Ignore center cell and cells whose coordinate sum >range
                if ((i == 0 && j == 0)) continue;

                if (i >= 0 && j >= 0 || i <= 0 && j <= 0 || i <= range + j && j < 0 || i >= -(range - j) && j > 0) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateEuclidean(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i == 0 && j == 0) continue;  // Ignore center cell

                // x^2 + y^2 <= r^2
                if ((i * i + j * j) <= range * range) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateCircular(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i == 0 && j == 0) continue;  // Ignore center cell

                // x^2 + y^2 <= r^2 + r
                if ((i * i + j * j) <= range * range + range) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateCross(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i == 0 && j == 0) continue;  // Ignore center cell

                if (i == 0 || j == 0) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateSaltire(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i == 0 && j == 0) continue;  // Ignore center cell

                if (Math.abs(i) == Math.abs(j)) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateStar(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i == 0 && j == 0) continue;  // Ignore center cell

                if (Math.abs(i) == Math.abs(j) || i == 0 || j == 0) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateHash(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i == 0 && j == 0) continue;  // Ignore center cell

                if (Math.abs(i) == 1 || Math.abs(j) == 1) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateCheckerboard(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i == 0 && j == 0) continue;  // Ignore center cell

                if (Math.abs(i) % 2 != Math.abs(j) % 2) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateAlignedCheckerboard(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i == 0 && j == 0) continue;  // Ignore center cell

                if (Math.abs(i) % 2 == Math.abs(j) % 2) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateTripod(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if ((i == 0 && j == 0)) continue;  // Ignore center cell

                if (j <= 0 && i <= 0 && (i == 0 || j == 0)) {
                    neighbourhood.add(new Coordinate(i, j));
                }
                else if (j > 0 && i == j) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateAsterisk(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                // Ignore center cell
                if ((i == 0 && j == 0)) continue;

                if (i == j || i == 0 || j == 0) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    public static Coordinate[] generateGaussianNeighbourhood(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                neighbourhood.add(new Coordinate(i, j));
            }
        }

        return toArray(neighbourhood);
    }

    public static int[] generateGaussian(int range) {
        int[] weights = new int[(2 * range + 1) * (2 * range + 1)];
        Coordinate[] neighbourhood = generateGaussianNeighbourhood(range);
        for (int i = 0; i < neighbourhood.length; i++) {
            weights[i] = (range + 1 - Math.abs(neighbourhood[i].getX())) *
                    (range + 1 - Math.abs(neighbourhood[i].getY()));
        }

        return weights;
    }

    public static Coordinate[] generateTriangularNeighbourhood(int range) {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -2 * range; i < 2 * range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i == 0 && j == 0) continue;

                if (j == 0 || j == -1 || Math.abs(i) <= range ||
                        (j < 0 && Math.abs(i) > range && Math.abs(i) - range < range + j + 2) ||
                        (j > 0 && Math.abs(i) > range && Math.abs(i) - range < range - j + 1)) {
                    neighbourhood.add(new Coordinate(i, j));
                }
            }
        }

        return toArray(neighbourhood);
    }

    // Convert cell list to array
    public static Coordinate[] toArray(ArrayList<Coordinate> neighbourhood) {
        // Turn into array
        Coordinate[] neighbourhoodArray = new Coordinate[neighbourhood.size()];
        for (int i = 0; i < neighbourhood.size(); i++) {
            neighbourhoodArray[i] = neighbourhood.get(i);
        }

        return neighbourhoodArray;
    }

    // Get Neighbourhood from CoordCA Format
    public static Coordinate[] fromCoordCA(String CoordCA, int range) {
        if (CoordCA.equals("")) return null;
        if (!CoordCA.matches("[A-Fa-f0-9]+")) {
            throw new IllegalArgumentException("Invalid character in CoordCA neighbourhood specification.");
        }

        // Convert to binary
        String flattenedNeighbourhood = new BigInteger(CoordCA, 16).toString(2);
        flattenedNeighbourhood = "0".repeat(Math.max(0, (2 * range + 1) * (2 * range + 1) - 1 -
                flattenedNeighbourhood.length())) + flattenedNeighbourhood;  // Replace it with the corrected one

        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i != 0 || j != 0) {
                    int index = (i + range) * (2 * range + 1) + (j + range);
                    if ((i == 0 && j > 0) || i > 0) {
                        if (flattenedNeighbourhood.charAt(index - 1) == '1') {
                            neighbourhood.add(new Coordinate(-j, -i));
                        }
                    }
                    else {
                        if (flattenedNeighbourhood.charAt(index) == '1') {
                            neighbourhood.add(new Coordinate(-j, -i));
                        }
                    }
                }
            }
        }

        return toArray(neighbourhood);
    }

    // Get Neighbourhood Weights from LifeViewer Format
    public static Pair<Coordinate[], int[]> getNeighbourhoodWeights(String LifeViewer, int range)
            throws IllegalArgumentException {
        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        ArrayList<Integer> weights = new ArrayList<>();
        if (!LifeViewer.matches("[A-Fa-f0-9]+")) {
            throw new IllegalArgumentException("Invalid character in neighbourhood weights specification.");
        }

        if (LifeViewer.length() == Math.pow(2 * range + 1, 2)) {
            int weight;
            for (int i = 0; i < LifeViewer.length(); i++) {
                weight = Integer.parseInt(new BigInteger(LifeViewer.charAt(i) + "", 16).toString(10));
                if (weight >= 8) {  // Negative weight
                    weights.add(weight - 8);
                    neighbourhood.add(new Coordinate(i % (2 * range + 1) - range, i / (2 * range + 1) - range));
                }
                else if (weight != 0){
                    weights.add(weight);
                    neighbourhood.add(new Coordinate(i % (2 * range + 1) - range, i / (2 * range + 1) - range));
                }
            }
        }
        else if (LifeViewer.length() == Math.pow(2 * range + 1, 2) * 2) {
            int weight;
            for (int i = 0; i < Math.pow(2 * range + 1, 2); i++) {
                weight = Integer.parseInt(new BigInteger(LifeViewer.charAt(i * 2) + "" +
                        LifeViewer.charAt(i * 2 + 1), 16).toString(10));
                if (weight >= 128) {  // Negative weight
                    weights.add(weight - 128);
                    neighbourhood.add(new Coordinate(i % (2 * range + 1) - range, i / (2 * range + 1) - range));
                }
                else if (weight != 0){
                    weights.add(weight);
                    neighbourhood.add(new Coordinate(i % (2 * range + 1) - range, i / (2 * range + 1) - range));
                }
            }
        }
        else {
            throw new IllegalArgumentException("Weighted neighbourhood string must be of length " +
                    Math.pow(2 * range + 1, 2) + " or " + Math.pow(2 * range + 1, 2) * 2);
        }

        return new Pair<>(toArray(neighbourhood), weights.stream().mapToInt(i -> i).toArray());
    }

    // Get State Weights from LifeViewer Format
    public static int[] getStateWeights(String LifeViewer) {
        int[] stateWeights = new int[LifeViewer.length()];
        for (int i = 0; i < LifeViewer.length(); i++) {
            stateWeights[i] = Integer.parseInt(LifeViewer.charAt(i) + "", 16);
        }

        return stateWeights;
    }

    // Get the active states from the Extended Generations String
    public static HashSet<Integer> getActiveGenExtStates(String genExtString) {
        int currentState = 1;
        boolean active = true;
        HashSet<Integer> activeStates = new HashSet<>();
        for (String token: genExtString.split("-")) {
            int temp = currentState + Integer.parseInt(token);
            for (int i = currentState; i < temp; i++) {
                if (active) activeStates.add(currentState);
                currentState += 1;
            }

            active = !active;
        }

        return activeStates;
    }
}
