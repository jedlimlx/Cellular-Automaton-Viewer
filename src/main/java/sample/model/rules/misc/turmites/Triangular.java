package sample.model.rules.misc.turmites;

import sample.model.Coordinate;
import sample.model.rules.Tiling;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements turmites on the triangular von neumann neighbourhood
 */
public class Triangular extends Neighbourhood {
    private static final int[] oppositeDirection = new int[]{0, 1, 2};
    private static final Map<Integer, int[]> turn = new HashMap<>();

    public Triangular() {
        turn.put(1, new int[]{2, 0, 1});  // left
        turn.put(2, new int[]{1, 2, 0});  // right
        turn.put(4, new int[]{0, 1, 2});  // u-turn
    }

    @Override
    public Tiling getTiling() {
        return Tiling.Triangular;
    }

    @Override
    public int[] getActions() {
        return new int[]{1, 2, 4};
    }

    @Override
    public Coordinate[] getNeighbourhood() {
        return new Coordinate[] {
                new Coordinate(1, 0),
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
