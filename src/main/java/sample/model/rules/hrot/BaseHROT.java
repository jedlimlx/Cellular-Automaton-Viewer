package sample.model.rules.hrot;

import org.javatuples.Pair;
import sample.model.CommentGenerator;
import sample.model.Coordinate;
import sample.model.NeighbourhoodGenerator;
import sample.model.Utils;
import sample.model.rules.RuleFamily;
import sample.model.rules.Tiling;

import java.util.Map;

/**
 * The base class of all HROT rules
 */
public abstract class BaseHROT extends RuleFamily {
    /**
     * Weights of the HROT rule
     */
    protected int[] weights;

    /**
     * Neighbourhood of the HROT rule
     */
    protected Coordinate[] neighbourhood;

    /**
     * The regex for HROT birth / survival / whatever transitions
     */
    protected final static String hrotTransitions = "(((\\d,(?=\\d))|(\\d-(?=\\d))|\\d)+)?";

    /**
     * The regex for the HROT neighbourhood specifier
     */
    protected final static String neighbourhoodRegex =
            "N(@([A-Fa-f0-9]+)?[HL]?|W[A-Fa-f0-9]+[HL]?|[" + NeighbourhoodGenerator.neighbourhoodSymbols + "])";

    /**
     * The maximum possible neighbourhood count.
     * Used for B0 and min, max rule generation.
     */
    protected int maxNeighbourhoodCount;

    /**
     * Loads the neighbourhood from the N... specifier
     * @param range The range of the neighbourhood
     * @param specifier The specifier for the neighbourhood
     */
    protected void loadNeighbourhood(int range, String specifier) {
        if (specifier.matches("N@([A-Fa-f0-9]+)?[HL]?")) {  // CoordCA Format
            weights = null;

            if (specifier.length() > 0)
                neighbourhood = NeighbourhoodGenerator.fromCoordCA(
                        Utils.matchRegex("N@([A-Fa-f0-9]+)?([HL]?)", specifier, 0).substring(2).
                        replaceAll("[HL]", ""), range);

            try {
                String tilingString = Utils.matchRegex("N@(?:[A-Fa-f0-9]+)?([HL]?)",
                        specifier, 0, 1);
                if (tilingString.equals("H")) tiling = Tiling.Hexagonal;
                else if (tilingString.equals("L")) tiling = Tiling.Triangular;
            } catch (IllegalStateException exception) {
                tiling = Tiling.Square;
            }
        } else if (specifier.matches("NW[A-Fa-f0-9]+[HL]?")) {  // Weighted Rules
            Pair<Coordinate[], int[]> neighbourhoodAndWeights =
                    NeighbourhoodGenerator.getNeighbourhoodWeights(Utils.matchRegex("NW([A-Fa-f0-9]+)([HL]?)",
                            specifier, 0).substring(2).replaceAll("[HL]", ""), range);
            neighbourhood = neighbourhoodAndWeights.getValue0();
            weights = neighbourhoodAndWeights.getValue1();

            try {
                String tilingString = Utils.matchRegex("NW[A-Fa-f0-9]+([HL]?)",
                        specifier, 0, 1);
                if (tilingString.equals("H")) tiling = Tiling.Hexagonal;
                else if (tilingString.equals("L")) tiling = Tiling.Triangular;
            } catch (IllegalStateException exception) {
                tiling = Tiling.Square;
            }
        } else if (specifier.matches("N[" + NeighbourhoodGenerator.neighbourhoodSymbols + "]")) {
            char neighbourhoodSymbol = specifier.charAt(1);

            neighbourhood = NeighbourhoodGenerator.generateFromSymbol(neighbourhoodSymbol, range);
            weights = NeighbourhoodGenerator.generateWeightsFromSymbol(neighbourhoodSymbol, range);
            tiling = NeighbourhoodGenerator.generateTilingFromSymbol(neighbourhoodSymbol);
        } else {
            throw new IllegalArgumentException("Unknown neighbourhood specifier " + specifier + "!");
        }


        // Determine maximum neighbourhood count
        maxNeighbourhoodCount = 0;

        if (neighbourhood != null) {
            if (weights != null) {
                for (int weight: weights) {
                    if (weight > 0)
                        maxNeighbourhoodCount += weight;
                }
            }
            else {
                maxNeighbourhoodCount = neighbourhood.length;
            }
        }
    }

    /**
     * Sets the neighbourhood of the HROT rule
     * @param neighbourhood Neighbourhood of the HROT rule
     */
    public void setNeighbourhood(Coordinate[] neighbourhood) {
        this.neighbourhood = neighbourhood;
        updateBackground();
    }

    /**
     * Sets the weights of the HROT rule
     * @param weights Weights of the HROT rule
     */
    public void setWeights(int[] weights) {
        this.weights = weights;
        updateBackground();
    }

    /**
     * This method returns the neighbourhood of a given cell at a certain generation
     * @param generation The generation of the simulation
     * @return A list of Coordinates that represent the neighbourhood
     */
    @Override
    public Coordinate[] getNeighbourhood(int generation) {
        return neighbourhood;
    }

    /**
     * Gets the weights of the HROT rule
     * @return Weights of the HROT rule
     */
    public int[] getWeights() {
        return weights;
    }

    @Override
    public Map<String, String> getRuleInfo() {
        Map<String, String> map = super.getRuleInfo();

        StringBuilder weightsString = new StringBuilder("\n");
        for (String string: CommentGenerator.generateFromWeights(weights, neighbourhood)) {
            weightsString.append(string.replaceAll("#R\\s*", "")).append("\n");
        }

        map.put("Weights / Neighbourhood", weightsString.toString());
        return map;
    }
}
