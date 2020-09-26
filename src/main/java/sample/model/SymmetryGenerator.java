package sample.model;

import sample.model.simulation.Grid;

import java.util.Random;

/**
 * Contains useful methods for generating random soups and implementing symmetries
 */
public class SymmetryGenerator {
    /**
     * The list of symmetries
     */
    public final static String[] symmetries = new String[]{"C1", "C2", "C4", "D2-", "D4+"};

    /**
     * Generates a symmetry based on the provided symmetry name.
     * @param symmetry The symmetry name
     * @param density The density of the random soup
     * @param states The number of states to include in the random soup
     * @param x The width of the random soup
     * @param y The height of the random soup
     * @return Returns the random soup generated
     */
    public static Grid generateSymmetry(String symmetry, int density, int[] states, int x, int y) {
        switch (symmetry) {
            case "C2": return generateC2(density, states, x, y);
            case "C4": return generateC4(density, states, x, y);
            case "D2-": return generateD2(density, states, x, y);
            case "D4+": return generateD4(density, states, x, y);
            default: return generateC1(density, states, x, y);
        }
    }

    // Random Soup Generating Functions
    public static Grid generateC1(int density, int[] states, int x, int y) {
        Random random = new Random();  // Random Number Generator
        Grid soup = new Grid();  // Stores the soup
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (random.nextInt(100) < density) {
                    soup.setCell(i, j, states[random.nextInt(states.length)]);
                }
            }
        }

        return soup;
    }

    public static Grid generateD2(int density, int[] states, int x, int y) {
        Grid grid = generateC1(density, states, x, y / 2 + 1);
        Grid grid2 = grid.deepCopy();
        grid2.reflectCellsY(new Coordinate(0, 0), new Coordinate(x, y / 2));

        Grid soup = new Grid();
        soup.insertCells(grid, new Coordinate(0, 0));
        if (y % 2 == 0) soup.insertCells(grid2, new Coordinate(0, y / 2 - 1));
        else soup.insertCells(grid2, new Coordinate(0, y / 2));

        return soup;
    }

    public static Grid generateD4(int density, int[] states, int x, int y) {
        Grid grid = generateD2(density, states, x / 2 + 1, y);
        Grid grid2 = grid.deepCopy();
        grid2.reflectCellsX(new Coordinate(0, 0), new Coordinate(x / 2, y));

        Grid soup = new Grid();
        soup.insertCells(grid, new Coordinate(0, 0));
        if (x % 2 == 0) soup.insertCells(grid2, new Coordinate(x / 2 - 1, 0));
        else soup.insertCells(grid2, new Coordinate(x / 2, 0));

        return soup;
    }

    public static Grid generateC2(int density, int[] states, int x, int y) {
        Grid grid = generateC1(density, states, x / 2, x / 2);
        Grid grid2 = grid.deepCopy();
        grid2.rotateCCW(new Coordinate(0, 0), new Coordinate(x / 2, x / 2));
        grid2.rotateCCW(new Coordinate(0, 0), new Coordinate(x / 2, x / 2));

        Grid soup = new Grid();
        soup.insertCells(grid, new Coordinate(0, 0));
        soup.insertCells(grid2, new Coordinate(x / 2 - 1, x / 2 - 1));

        return soup;
    }

    public static Grid generateC4(int density, int[] states, int x, int y) {
        Grid grid = generateC1(density, states, x / 2 + 1, x / 2 + 1);

        Grid grid2 = grid.deepCopy();
        grid2.rotateCCW(new Coordinate(0, 0), new Coordinate(x / 2 + 1, x / 2 + 1));

        Grid grid3 = grid2.deepCopy();
        grid3.rotateCCW(new Coordinate(0, 0), new Coordinate(x / 2 + 1, x / 2 + 1));

        Grid grid4 = grid3.deepCopy();
        grid4.rotateCCW(new Coordinate(0, 0), new Coordinate(x / 2 + 1, x / 2 + 1));

        Grid soup = new Grid();
        soup.insertCells(grid, new Coordinate(0, 0));
        soup.insertCells(grid2, new Coordinate(0, x / 2));
        soup.insertCells(grid3, new Coordinate(x / 2, x / 2));
        soup.insertCells(grid4, new Coordinate(x / 2, 0));

        return soup;
    }

    // Synthesis Generating Function
    // Assume spaceship goes the the NNW
    public static Grid generateSynth(Grid[] spaceships, int num, int x, int y) {
        Grid[] NNW = new Grid[spaceships.length];
        for (int i = 0; i < spaceships.length; i++) {
            NNW[i] = spaceships[i].deepCopy();
        }

        Grid[] NNE = new Grid[spaceships.length];
        for (int i = 0; i < spaceships.length; i++) {
            NNE[i] = NNW[i].deepCopy();
            NNE[i].updateBounds();
            NNE[i].reflectCellsX(NNE[i].getBounds().getValue0(), NNE[i].getBounds().getValue1());
        }

        Grid[] SSW = new Grid[spaceships.length];
        for (int i = 0; i < spaceships.length; i++) {
            SSW[i] = NNW[i].deepCopy();
            SSW[i].updateBounds();
            SSW[i].reflectCellsY(SSW[i].getBounds().getValue0(), SSW[i].getBounds().getValue1());
        }

        Grid[] SSE = new Grid[spaceships.length];
        for (int i = 0; i < spaceships.length; i++) {
            SSE[i] = SSW[i].deepCopy();
            SSE[i].updateBounds();
            SSE[i].reflectCellsX(SSE[i].getBounds().getValue0(), SSE[i].getBounds().getValue1());
        }

        Grid[] SEE = new Grid[spaceships.length];
        for (int i = 0; i < spaceships.length; i++) {
            SEE[i] = NNE[i].deepCopy();
            SEE[i].updateBounds();
            SEE[i].rotateCW(SEE[i].getBounds().getValue0(), SEE[i].getBounds().getValue1());
        }

        Grid[] NEE = new Grid[spaceships.length];
        for (int i = 0; i < spaceships.length; i++) {
            NEE[i] = SEE[i].deepCopy();
            NEE[i].updateBounds();
            NEE[i].reflectCellsY(NEE[i].getBounds().getValue0(), NEE[i].getBounds().getValue1());
        }

        Grid[] SWW = new Grid[spaceships.length];
        for (int i = 0; i < spaceships.length; i++) {
            SWW[i] = SEE[i].deepCopy();
            SWW[i].updateBounds();
            SWW[i].reflectCellsX(SWW[i].getBounds().getValue0(), SWW[i].getBounds().getValue1());
        }

        Grid[] NWW = new Grid[spaceships.length];
        for (int i = 0; i < spaceships.length; i++) {
            NWW[i] = NEE[i].deepCopy();
            NWW[i].updateBounds();
            NWW[i].reflectCellsX(NWW[i].getBounds().getValue0(), NWW[i].getBounds().getValue1());
        }

        Grid soup = new Grid();
        Random random = new Random();
        for (int i = 0; i < num; i++) {
            switch (random.nextInt(8)) {
                case 0: soup.insertCells(NNW[random.nextInt(spaceships.length)],
                        new Coordinate(random.nextInt(x), random.nextInt(y) + y)); break;
                case 1: soup.insertCells(NNE[random.nextInt(spaceships.length)],
                        new Coordinate(-random.nextInt(x), random.nextInt(y) + y)); break;
                case 2: soup.insertCells(SSW[random.nextInt(spaceships.length)],
                        new Coordinate(random.nextInt(x), -random.nextInt(y) - y)); break;
                case 3: soup.insertCells(SSE[random.nextInt(spaceships.length)],
                        new Coordinate(-random.nextInt(x), -random.nextInt(y) - y)); break;
                case 4: soup.insertCells(SEE[random.nextInt(spaceships.length)],
                        new Coordinate(-random.nextInt(x) - x, -random.nextInt(y))); break;
                case 5: soup.insertCells(NEE[random.nextInt(spaceships.length)],
                        new Coordinate(-random.nextInt(x) - x, random.nextInt(y))); break;
                case 6: soup.insertCells(SWW[random.nextInt(spaceships.length)],
                        new Coordinate(random.nextInt(x) + x, -random.nextInt(y))); break;
                case 7: soup.insertCells(NWW[random.nextInt(spaceships.length)],
                        new Coordinate(random.nextInt(x) + x, random.nextInt(y))); break;
                default: break;
            }
        }

        return soup;
    }
}
