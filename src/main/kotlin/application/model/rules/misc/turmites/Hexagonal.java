package application.model.rules.misc.turmites;

import application.model.Coordinate;
import application.model.rules.Tiling;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements turmites on the hexagonal neighbourhood
 */
public class Hexagonal extends Neighbourhood {
    private static final int[] oppositeDirection = new int[]{3, 4, 5, 0, 1, 2};
    private static final Map<Integer, int[]> turn = new HashMap<>();

    public Hexagonal() {
        turn.put(1, new int[]{0, 1, 2, 3, 4, 5});  // forward
        turn.put(2, new int[]{1, 2, 3, 4, 5, 0});  // left
        turn.put(4, new int[]{5, 0, 1, 2, 3, 4});  // right
        turn.put(8, new int[]{2, 3, 4, 5, 0, 1});  // back-left
        turn.put(16, new int[]{4, 5, 0, 1, 2, 3});  // back-right
        turn.put(32, new int[]{3, 4, 5, 0, 1, 2});  // u-turn
    }

    @Override
    public Tiling getTiling() {
        return Tiling.Hexagonal;
    }

    @Override
    public int[] getActions() {
        return new int[]{1, 2, 4, 8, 16, 32};
    }

    @Override
    public Coordinate[] getNeighbourhood() {
        return new Coordinate[] {
                new Coordinate(0, -1),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1),
                new Coordinate(-1, 0),
                new Coordinate(-1, -1)
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
