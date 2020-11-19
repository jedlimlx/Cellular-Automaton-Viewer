package sample.model.rules.misc.turmites;

import sample.model.Coordinate;
import sample.model.rules.Tiling;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements turmites on the von neumann neighbourhood
 */
public class VonNeumann extends Neighbourhood {
    private static final int[] oppositeDirection = new int[]{2, 3, 0, 1};
    private static final Map<Integer, int[]> turn = new HashMap<>();

    public VonNeumann() {
        turn.put(1, new int[]{0, 1, 2, 3});  // no turn
        turn.put(2, new int[]{1, 2, 3, 0});  // right
        turn.put(4, new int[]{2, 3, 0, 1});  // u-turn
        turn.put(8, new int[]{3, 0, 1, 2});  // left
    }

    @Override
    public Tiling getTiling() {
        return Tiling.Square;
    }

    @Override
    public int[] getActions() {
        return new int[]{1, 2, 4, 8};
    }

    @Override
    public Coordinate[] getNeighbourhood() {
        return new Coordinate[] {
                new Coordinate(1, 0),
                new Coordinate(0, -1),
                new Coordinate(-1, 0),
                new Coordinate(0, 1)
        };
    }

    @Override
    public int getOppositeDirection(int direction) {
        return oppositeDirection[direction];
    }

    @Override
    public int getNewDirection(int action, int turmiteDirection) {
        return turn.get(action)[turmiteDirection];
    }
}
