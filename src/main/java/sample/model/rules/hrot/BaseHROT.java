package sample.model.rules.hrot;

import sample.model.Coordinate;
import sample.model.rules.RuleFamily;

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
}
