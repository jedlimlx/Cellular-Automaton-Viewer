package application.model.rules.misc.turmites;

import application.model.Coordinate;
import application.model.rules.Tiling;

/**
 * The base class for various turmite neighbourhoods
 */
public abstract class Neighbourhood {
    /**
     * Gets the tiling the neighbourhood applies to
     * @return Returns the tiling of the neighbourhood
     */
    public abstract Tiling getTiling();

    /**
     * Gets the actions the turmite can take
     * @return Returns the actions that the turmite can take
     */
    public abstract int[] getActions();

    /**
     * Gets the neighbourhood of the turmite
     * @return Returns the neighbourhood of the turmite
     */
    public abstract Coordinate[] getNeighbourhood();

    /**
     * Gets direction opposite to the direction passed in
     * @param direction The direction
     * @return Returns the opposite direction of the direction passed in
     */
    public abstract int getOppositeDirection(int direction);

    /**
     * Gets the new direction of the turmite
     * @param action The action taken by the turmite
     * @param turmiteDirection The turmite's direction
     * @return Returns the new direction of the turmite
     */
    public abstract int getNewDirection(int action, int turmiteDirection);
}
