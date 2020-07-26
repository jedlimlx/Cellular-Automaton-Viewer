package sample.model;

import java.math.BigInteger;
import java.util.ArrayList;

public class NeighbourhoodGenerator {
    public static Coordinate[] generateFromSymbol(char symbol, int range) {
        switch (symbol) {
            case 'A': return generateAsterisk(range);
            case 'B': return generateCheckerboard(range);
            case 'C': return generateCircular(range);
            case 'H': return generateHexagonal(range);
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
        // Convert to binary
        String flattenedNeighbourhood = new BigInteger(CoordCA, 16).toString(2);

        flattenedNeighbourhood = "0".repeat(Math.max(0, 24 - flattenedNeighbourhood.length())) +
                flattenedNeighbourhood;  // Replace it with the corrected one

        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        for (int i = -range; i < range + 1; i++) {
            for (int j = -range; j < range + 1; j++) {
                if (i != 0 || j != 0) {
                    int index = (i + range) * (2 * range + 1) + (j + range);
                    if ((i == 0 && j > 0) || i > 0) {
                        if (flattenedNeighbourhood.charAt(index - 1) == '1') {
                            neighbourhood.add(new Coordinate(i, j));
                        }
                    }
                    else {
                        if (flattenedNeighbourhood.charAt(index) == '1') {
                            neighbourhood.add(new Coordinate(i, j));
                        }
                    }
                }
            }
        }

        return toArray(neighbourhood);
    }
}
