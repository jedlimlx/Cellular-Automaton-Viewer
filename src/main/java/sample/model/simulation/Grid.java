package sample.model.simulation;

import org.javatuples.Pair;
import sample.model.Coordinate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents and serves as pattern storage. <br>
 * You may also iterate over it like in a loop. <br>
 * <br>
 * Example Usage: <br>
 * <pre>
 * Grid grid = new Grid("bo$obo$o2bo$bobo$2bo!");
 * iterateCells(cell -> {
 *      System.out.println(cell);
 * });
 * </pre>
 */
public class Grid implements Iterable<Block>, Iterator<Block> {
    /**
     * The background of the grid.
     * Used for minimum and maximum B0 rules (i.e. strobing rules).
     */
    private int background;

    /**
     * The bounds of the grid.
     */
    private Coordinate startCoordinate, endCoordinate;

    /**
     * The dictionary that stores the blocks according to their start coordinates
     */
    private final HashMap<Coordinate, Block> dictionary;

    /**
     * The size of the blocks used
     */
    private final int BLOCK_SIZE = 8;

    /**
     * Constructs a grid with an empty pattern and a background of 0.
     * The bounds of the grid are also uninitialised.
     */
    public Grid() {
        this.background = 0;
        this.dictionary = new HashMap<>();
        this.startCoordinate = new Coordinate(Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.endCoordinate = new Coordinate(-Integer.MAX_VALUE, -Integer.MAX_VALUE);
    }

    /**
     * Constructs a grid with the provided dictionary with a background of 0.
     * The bounds of the grid are also uninitialised.
     */
    private Grid(HashMap<Coordinate, Block> dictionary) {
        this.background = 0;
        this.startCoordinate = new Coordinate(Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.endCoordinate = new Coordinate(-Integer.MAX_VALUE, -Integer.MAX_VALUE);

        // Deep copying
        this.dictionary = new HashMap<>();

        Block block;
        for (Coordinate coordinate: dictionary.keySet()) {
            block = dictionary.get(coordinate);
            if (block.getPopulation() == 0) continue;

            this.dictionary.put(coordinate, (Block) block.clone());
        }
    }

    /**
     * Constructs a grid the RLE pattern and a background of 0.
     * @param RLE The RLE of the pattern
     */
    public Grid(String RLE) {
        this.background = 0;
        this.dictionary = new HashMap<>();
        this.startCoordinate = new Coordinate(Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.endCoordinate = new Coordinate(-Integer.MAX_VALUE, -Integer.MAX_VALUE);

        fromRLE(RLE, new Coordinate());
    }

    /**
     * Sets the cell at position coordinate to the specified state
     * @param coordinate The coordinate of the cell
     * @param state The state of the cell
     */
    public void setCell(Coordinate coordinate, int state) {
        state = convertCell(state);

        Coordinate blockCoordinate = getBlockCoordinate(coordinate);
        if (dictionary.get(blockCoordinate) == null) {
            if (state == 0) return;
            Block block = new Block(blockCoordinate, BLOCK_SIZE, BLOCK_SIZE);
            block.setCell(coordinate.getX(), coordinate.getY(), state);

            dictionary.put(blockCoordinate, block);
        }
        else {
            dictionary.get(blockCoordinate).setCell(coordinate.getX(), coordinate.getY(), state);
        }
    }

    /**
     * Sets the cell at position (x, y) to the specified state
     * @param x The x-coordinate of the cell
     * @param y The y-coordinate of the cell
     * @param state The state of the cell
     */
    public void setCell(int x, int y, int state) {
        setCell(new Coordinate(x, y), state);
    }

    /**
     * Adds a block at coordinate (x, y) into the hashmap
     * @param coordinate The coordinate of the block that is to be removed
     */
    public void addBlock(Coordinate coordinate) {
        dictionary.put(coordinate, new Block(coordinate, BLOCK_SIZE, BLOCK_SIZE));
    }

    /**
     * Removes the block at coordinate (x, y) from the hashmap
     * @param coordinate The coordinate of the block that is to be removed
     */
    public void removeBlock(Coordinate coordinate) {
        dictionary.remove(coordinate);
    }

    /**
     * Inserts the provided pattern at the specified coordinate
     * @param pattern The pattern to be inserted
     * @param coordinate The coordinate where the pattern is to be inserted
     */
    public void insertCells(Grid pattern, Coordinate coordinate) {
        pattern.iterateCells(coord -> setCell(coord.add(coordinate), pattern.getCell(coord)));
    }

    /**
     * Clears all cells between the coordinates specified
     * @param start The starting coordinate
     * @param end The end coordinate
     */
    public void clearCells(Coordinate start, Coordinate end) {
        Coordinate blockStart = new Coordinate(getBlockCoordinate(start).getX() + BLOCK_SIZE,
                getBlockCoordinate(start).getY() + BLOCK_SIZE);
        Coordinate blockEnd = new Coordinate(getBlockCoordinate(end).getX() - BLOCK_SIZE,
                getBlockCoordinate(end).getY() - BLOCK_SIZE);

        // Removing all the blocks
        for (int x = blockStart.getX(); x < blockEnd.getX() + 1; x++) {
            for (int y = blockStart.getY(); y < blockEnd.getY() + 1; y++) {
                dictionary.remove(new Coordinate(x, y));
            }
        }

        // Removing the remaining cells
        for (int x = start.getX(); x < end.getX() + 1; x++) {
            for (int y = start.getY(); y < end.getY() + 1; y++) {
                /* TODO (Fix the delete cells bug)
                if (x >= blockStart.getX() && x <= blockEnd.getX() &&
                        y >= blockStart.getY() && y <= blockEnd.getY()) {
                    x = blockEnd.getX() + 1;
                }
                */

                setCell(x, y, 0);
            }
        }
    }

    /**
     * Clears all cells in the grid
     */
    public void clearCells() {
        dictionary.clear();
    }

    /**
     * Gets the state of the cell at the specified coordinate
     * @param coordinate The coordinate of the cell
     * @return Returns the state of the cell
     */
    public int getCell(Coordinate coordinate) {
        Coordinate blockCoordinate = getBlockCoordinate(coordinate);

        Block block = dictionary.get(blockCoordinate);
        if (block == null) {
            return convertCell(0);
        }
        else {
            return convertCell(block.getCell(coordinate.getX(), coordinate.getY()));
        }
    }

    /**
     * Gets the state of the cell at (x, y)
     * @param x The x-coordinate of the cell
     * @param y The y-coordinate of the cell
     * @return Returns the state of the cell
     */
    public int getCell(int x, int y) {
        return getCell(new Coordinate(x, y));
    }

    /**
     * Get all cells between the start and end coordinates
     * @param start The start coordinate
     * @param end The end coordinate
     * @return Returns a grid contains the pattern
     */
    public Grid getCells(Coordinate start, Coordinate end) {
        Grid grid = new Grid();
        for (int x = start.getX(); x < end.getX() + 1; x++) {
            for (int y = start.getY(); y < end.getY() + 1; y++) {
                grid.setCell(x, y, this.getCell(x, y));
            }
        }

        return grid;
    }

    /**
     * Updates the bounds of the grid.
     */
    public void updateBounds() {
        if (size() == 0) {  // Check for empty grid
            startCoordinate = new Coordinate(0, 0);
            endCoordinate = new Coordinate(0, 0);
            return;
        }

        // Set to maximum possible values
        startCoordinate = new Coordinate(Integer.MAX_VALUE, Integer.MAX_VALUE);
        endCoordinate = new Coordinate(-Integer.MAX_VALUE, -Integer.MAX_VALUE);

        Coordinate blockStartCoordinate = new Coordinate(Integer.MAX_VALUE, Integer.MAX_VALUE);
        Coordinate blockEndCoordinate = new Coordinate(-Integer.MAX_VALUE, -Integer.MAX_VALUE);

        Coordinate blockCoordinate;
        for (Block block: this) {
            if (block.getPopulation() == 0) continue;
            blockCoordinate = block.getStartCoordinate();

            // Updating the bounds
            if (blockCoordinate.getX() <= blockStartCoordinate.getX()) {
                blockStartCoordinate = new Coordinate(blockCoordinate.getX(), blockStartCoordinate.getY());
                iterateCellsInBlock(cellCoordinate -> {
                    if (cellCoordinate.getX() < startCoordinate.getX())
                        startCoordinate = new Coordinate(cellCoordinate.getX(), startCoordinate.getY());
                    if (cellCoordinate.getY() < startCoordinate.getY())
                        startCoordinate = new Coordinate(startCoordinate.getX(), cellCoordinate.getY());
                }, block);
            }
            if (blockCoordinate.getY() <= blockStartCoordinate.getY()) {
                blockStartCoordinate = new Coordinate(blockStartCoordinate.getX(), blockCoordinate.getY());
                iterateCellsInBlock(cellCoordinate -> {
                    if (cellCoordinate.getX() < startCoordinate.getX())
                        startCoordinate = new Coordinate(cellCoordinate.getX(), startCoordinate.getY());
                    if (cellCoordinate.getY() < startCoordinate.getY())
                        startCoordinate = new Coordinate(startCoordinate.getX(), cellCoordinate.getY());
                }, block);
            }
            if (blockCoordinate.getX() >= blockEndCoordinate.getX()) {
                blockEndCoordinate = new Coordinate(blockCoordinate.getX(), blockEndCoordinate.getY());
                iterateCellsInBlock(cellCoordinate -> {
                    if (cellCoordinate.getX() > endCoordinate.getX())
                        endCoordinate = new Coordinate(cellCoordinate.getX(), endCoordinate.getY());
                    if (cellCoordinate.getY() > endCoordinate.getY())
                        endCoordinate = new Coordinate(endCoordinate.getX(), cellCoordinate.getY());
                }, block);
            }
            if (blockCoordinate.getY() >= blockEndCoordinate.getY()) {
                blockEndCoordinate = new Coordinate(blockEndCoordinate.getX(), blockCoordinate.getY());
                iterateCellsInBlock(cellCoordinate -> {
                    if (cellCoordinate.getX() > endCoordinate.getX())
                        endCoordinate = new Coordinate(cellCoordinate.getX(), endCoordinate.getY());
                    if (cellCoordinate.getY() > endCoordinate.getY())
                        endCoordinate = new Coordinate(endCoordinate.getX(), cellCoordinate.getY());
                }, block);
            }
        }
    }

    /**
     * Gets the bounds of the grid
     * @return Returns a pair with the first entry being the start coordinate and the second entry being the end coordinate
     */
    public Pair<Coordinate, Coordinate> getBounds() {
        return new Pair<>(startCoordinate, endCoordinate);
    }

    /**
     * Converts the pattern into an array of coordinates
     * @return Returns an array conains the coordinates
     */
    public Coordinate[] toArray() {
        AtomicInteger index = new AtomicInteger(0);
        Coordinate[] array = new Coordinate[getPopulation()];

        iterateCells(coordinate -> {
            array[index.intValue()] = coordinate;
            index.getAndIncrement();
        });

        return array;
    }

    /**
     * Reflects the cells in the grid between the start and end coordinates horizontally
     * @param startCoordinate The start coordinate
     * @param endCoordinate The end coordinate
     */
    public void reflectCellsX(Coordinate startCoordinate, Coordinate endCoordinate) {
        Grid grid = this.deepCopy();  // Make a deep copy for reference
        for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
            for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
                setCell(endCoordinate.getX() - x + startCoordinate.getX(), y, grid.getCell(x, y));
            }
        }
    }

    /**
     * Reflects the cells in the grid between the start and end coordinates vertically
     * @param startCoordinate The start coordinate
     * @param endCoordinate The end coordinate
     */
    public void reflectCellsY(Coordinate startCoordinate, Coordinate endCoordinate) {
        Grid grid = this.deepCopy();  // Make a deep copy for reference

        // Flipping vertically
        for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
            for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
                setCell(x, endCoordinate.getY() - y + startCoordinate.getY(), grid.getCell(x, y));
            }
        }
    }

    /**
     * Rotates the cells in the grid between the start and end coordinates clockwise
     * @param startCoordinate The start coordinate
     * @param endCoordinate The end coordinate
     */
    public void rotateCW(Coordinate startCoordinate, Coordinate endCoordinate) {
        Grid grid = this.deepCopy();  // Make a deep copy for reference

        if ((endCoordinate.getX() - startCoordinate.getX()) % 2 == 1) {
            endCoordinate = new Coordinate(endCoordinate.getX() + 1, endCoordinate.getY());
        }

        if ((endCoordinate.getY() - startCoordinate.getY()) % 2 == 1) {
            endCoordinate = new Coordinate(endCoordinate.getX(), endCoordinate.getY() + 1);
        }

        int centerX = (endCoordinate.getX() - startCoordinate.getX()) / 2 + startCoordinate.getX();
        int centerY = (endCoordinate.getY() - startCoordinate.getY()) / 2 + startCoordinate.getY();

        clearCells(startCoordinate, endCoordinate);

        // Rotating clockwise
        for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
            for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
                int dx = x - centerX, dy = y - centerY;
                setCell(centerX - dy, centerY + dx, grid.getCell(x, y));
            }
        }
    }

    /**
     * Rotates the cells in the grid between the start and end coordinates counter-clockwise
     * @param startCoordinate The start coordinate
     * @param endCoordinate The end coordinate
     */
    public void rotateCCW(Coordinate startCoordinate, Coordinate endCoordinate) {
        Grid grid = this.deepCopy();  // Make a deep copy for reference

        if ((endCoordinate.getX() - startCoordinate.getX()) % 2 == 1) {
            endCoordinate = new Coordinate(endCoordinate.getX() + 1, endCoordinate.getY());
        }

        if ((endCoordinate.getY() - startCoordinate.getY()) % 2 == 1) {
            endCoordinate = new Coordinate(endCoordinate.getX(), endCoordinate.getY() + 1);
        }

        int centerX = (endCoordinate.getX() - startCoordinate.getX()) / 2 + startCoordinate.getX();
        int centerY = (endCoordinate.getY() - startCoordinate.getY()) / 2 + startCoordinate.getY();

        clearCells(startCoordinate, endCoordinate);

        // Rotating counter-clockwise
        for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
            for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
                int dx = x - centerX, dy = y - centerY;
                setCell(centerX + dy, centerY - dx, grid.getCell(x, y));
            }
        }
    }

    /**
     * Converts pattern between the start and end coordinates to the run length encoded (RLE) format.
     * @param startCoordinate The start coordinate
     * @param endCoordinate The end coordinate
     * @return Returns the RLE body (no header)
     */
    public String toRLE(Coordinate startCoordinate, Coordinate endCoordinate) {
        // First, add characters to a string
        ArrayList<Character> buffer = new ArrayList<>();
        ArrayList<Character> rleArray = new ArrayList<>();
        for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
            for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
                if (getCell(x, y) == 0) {
                    buffer.add('.');
                }
                else {
                    rleArray.addAll(buffer);
                    rleArray.add((char) (getCell(x, y) + 64));

                    buffer.clear();
                }
            }

            buffer.add('$');
        }

        // Next, compress it (I totally didn't steal this code from somewhere)
        int n = rleArray.size();
        StringBuilder rleString = new StringBuilder();

        for (int i = 0; i < n; i++) {
            // Count occurrences of current character
            int count = 1;
            while (i < n - 1 && rleArray.get(i) == rleArray.get(i + 1)) {
                count++;
                i++;
            }

            // Add to the RLE
            if (count > 1) {
                rleString.append(count).append(rleArray.get(i));
            }
            else {
                rleString.append(rleArray.get(i));
            }
        }

        // Finish off the encoding
        // Don't forget the '!'
        return rleString.toString() + "!";
    }

    /**
     * Converts the entire pattern into an RLE
     * @return Returns the RLE body (no headers)
     */
    public String toRLE() {
        updateBounds();
        return toRLE(startCoordinate, endCoordinate);
    }

    /**
     * Inserts the RLE into the grid
     * @param rle The RLE body (no headers, no \n)
     * @param startCoordinate The coordinate to insert the new cells
     */
    public void fromRLE(String rle, Coordinate startCoordinate) {
        int x = startCoordinate.getX(), y = startCoordinate.getY();
        Pattern rleRegex = Pattern.compile("([0-9]+)?[ob$.A-Z]");
        Matcher matcher = rleRegex.matcher(rle);

        String rleGroup;
        char lastChar;
        int num;
        while (matcher.find()) {
            rleGroup = matcher.group();

            lastChar = rleGroup.charAt(rleGroup.length() - 1);
            if (rleGroup.substring(0, rleGroup.length() - 1).length() > 0)  // Check if a number is there
                num = Integer.parseInt(rleGroup.substring(0, rleGroup.length() - 1));
            else
                num = 1; // If not set it to one by default

            for (int i = 0; i < num; i++) {
                switch (lastChar) {  // Check the characters
                    case 'b':
                    case '.':
                        x++; break;
                    case '$':
                        y++; x = startCoordinate.getX(); break;
                    case 'o':
                        setCell(x++, y, 1); break;
                    default:
                        setCell(x++, y, (int) lastChar - 64); break;
                }
            }
        }
    }

    /**
     * Inserts the apgcode into the grid
     * @param apgcode The apgcode to insert into the grid
     * @param startCoordinate The coordinate to insert the new cells
     */
    public void fromApgcode(String apgcode, Coordinate startCoordinate) {
        String[] tokens = apgcode.split("_");
        apgcode = String.join("_", Arrays.copyOfRange(tokens, 1, tokens.length));

        String chars = "0123456789abcdefghijklmnopqrstuvwxyz";

        char c;
        boolean blank = false;
        int x = 0, y = 0, plane = 1;
        for (int i = 0; i < apgcode.length(); i++) {
            c = apgcode.charAt(i);
            if (blank) {
                x += chars.indexOf(c);
            } else if (c == 'y') {
                x += 4;
                blank = true;
            } else if (c == 'x') {
                x += 3;
            } else if (c == 'w') {
                x += 2;
            } else if (c == 'z') {
                x = 0;
                y += 5;
            } else if (c == '_') {
                x = 0;
                y = 0;
                plane += 1;
            } else {
                for (int j = 0; j < 5; j++) {
                    if ((chars.indexOf(c) & (1 << j)) != 0) {
                        setCell(x + startCoordinate.getX(), y + j + startCoordinate.getY(), plane);
                    }
                }

                x += 1;
            }
        }
    }

    /**
     * Performs BFS (breath-first search) on the pattern and returns a
     * list of coordinates represents the cells that are a certain distance from the living cells.
     * @param distance The "distance" from the living cell
     * @param neighbourhood The neighbourhood to use in the BFS
     * @return Returns the list of cells that are a certain distance from the living cells
     */
    public List<Coordinate> bfs(int distance, Coordinate[] neighbourhood) {
        Set<Coordinate> visited = new HashSet<>();
        Queue<Pair<Coordinate, Integer>> bfsQueue = new LinkedList<>();
        iterateCells(coordinate -> bfsQueue.add(new Pair<>(coordinate, 0)));

        Pair<Coordinate, Integer> currentPair;
        Coordinate current, neighbour;
        while (!bfsQueue.isEmpty()) {
            currentPair = bfsQueue.poll();  // Pop from queue head
            current = currentPair.getValue0();
            visited.add(current);  // Adding to visited

            // Skip the rest since the cell is at max distance
            if (currentPair.getValue1() >= distance) continue;

            for (Coordinate coordinate: neighbourhood) {
                neighbour = current.add(coordinate);
                if (!visited.contains(neighbour)) {
                    bfsQueue.add(new Pair<>(neighbour, currentPair.getValue1() + 1));
                }
            }
        }

        return new ArrayList<>(visited);
    }

    /**
     * Deep copies the grid
     * @return A deep copy of the grid
     */
    public Grid deepCopy() {
        return new Grid(dictionary);
    }

    /**
     * Shallow copies the grid
     * @return A shallow copy of the grid
     */
    public Grid shallowCopy() {
        return this;
    }

    /**
     * Gets the size of the grid
     * @return Returns the size of the grid
     */
    public int size() {
        return dictionary.size();
    }

    /**
     * Checks if 2 grids are identical given a translation.
     * Used to check for hash collisions.
     * @param grid The grid to check.
     * @param displacementX The distance the first grid is translated in the x-direction compared to the second grid
     * @param displacementY The distance the first grid is translated in the y-direction compared to the second grid
     * @return Returns true if the grids are equal, false if they aren't
     */
    public boolean slowEquals(Grid grid, int displacementX, int displacementY) {
        Coordinate[] gridArray1 = grid.toArray();
        Coordinate[] gridArray2 = this.toArray();

        // Sorting so that the displacements will line up
        Arrays.sort(gridArray1);
        Arrays.sort(gridArray2);

        for (int i = 0; i < getPopulation(); i++) {
            if (gridArray1[i].subtract(gridArray2[i]).getX() != displacementX ||
                    gridArray1[i].subtract(gridArray2[i]).getY() != displacementY) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if the inputted grid is equal to this grid (independent of translation)
     * @param o The other grid
     * @return Returns true if the grids are equal, false if they aren't
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grid that = (Grid) o;
        return that.hashCode() == this.hashCode();
    }

    /**
     * Gets the hash of the grid.
     * @return Returns the grid's hash (uses Golly's hash algorithm).
     */
    @Override
    public int hashCode() {
        updateBounds();

        int hash = 31415962;
        for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
            int yShift = y - startCoordinate.getY();
            for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
                if (getCell(x, y) > 0) {
                    hash = (hash * 1000003) ^ yShift;
                    hash = (hash * 1000003) ^ (x - startCoordinate.getX());
                    hash = (hash * 1000003) ^ getCell(x, y);
                }
            }
        }

        return hash;
    }

    /**
     * Gets the hash of the grid.
     * @param startCoordinate The start coordinate of the region where the hash is calculated
     * @param endCoordinate The end coordinate of the region where the hash is calculated
     * @return Returns the grid's hash (uses Golly's hash algorithm).
     */
    public int hashCode(Coordinate startCoordinate, Coordinate endCoordinate) {
        int hash = 31415962;
        for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
            int yShift = y - startCoordinate.getY();
            for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
                if (getCell(x, y) > 0) {
                    hash = (hash * 1000003) ^ yShift;
                    hash = (hash * 1000003) ^ (x - startCoordinate.getX());
                    hash = (hash * 1000003) ^ getCell(x, y);
                }
            }
        }

        return hash;
    }

    /**
     * Gets the hash of the grid.
     * @param coordinates The coordinates to consider in the hash algorithm
     * @param startCoordinate The start coordinate of the list of coordinates provided
     * @return Returns the grid's hash (uses Golly's hash algorithm).
     */
    public int hashCode(List<Coordinate> coordinates, Coordinate startCoordinate) {
        int hash = 31415962;
        for (Coordinate coordinate: coordinates) {
            int yShift = coordinate.getY() - startCoordinate.getY();
            if (getCell(coordinate) > 0) {
                hash = (hash * 1000003) ^ yShift;
                hash = (hash * 1000003) ^ (coordinate.getX() - startCoordinate.getX());
                hash = (hash * 1000003) ^ getCell(coordinate);
            }
        }

        return hash;
    }

    /**
     * Set the background of the grid
     * @param background The background
     */
    public void setBackground(int background) {
        this.background = background;
    }

    /**
     * Gets the background of the grid
     * @return Returns the background of the grid
     */
    public int getBackground() {
        return background;
    }

    /**
     * Convert the cell state based on the background
     * Used to for B0 / strobing rules
     * @param state The current state of the cell
     * @return Returns the new cell state
     */
    public int convertCell(int state) {
        if (state == background)
            return 0;
        else if (state == 0)
            return background;
        else
            return state;
    }

    /**
     * Calls a method on every live cell (not state 0) in the grid
     * @param methodToCall The method to call with the coordinate of the cells as a parameter
     */
    public void iterateCells(Consumer<Coordinate> methodToCall) {
        for (Block block: this) {
            if (block.getPopulation() == 0) continue;
            iterateCellsInBlock(methodToCall, block);
        }
    }

    /**
     * Calls a method on every live cell (not state 0) in the provided block
     * @param methodToCall The method to call with the coordinate of the cells as a parameter
     * @param block The block to iterate through
     */
    public void iterateCellsInBlock(Consumer<Coordinate> methodToCall, Block block) {
        for (int i = block.getStartCoordinate().getX(); i < block.getStartCoordinate().getX() + BLOCK_SIZE; i++) {
                for (int j = block.getStartCoordinate().getY(); j < block.getStartCoordinate().getY() + BLOCK_SIZE; j++) {
                    if (block.getCell(i, j) > 0) methodToCall.accept(new Coordinate(i, j));
                }
            }
    }

    /**
     * Gets the coordinate of the block of a cell at a given coordinate (x, y)
     * @param coordinate The coordinate of the cell
     * @return Returns the block coordinate
     */
    public Coordinate getBlockCoordinate(Coordinate coordinate) {
        return new Coordinate(coordinate.getX() >= 0 ? (coordinate.getX() / BLOCK_SIZE) * BLOCK_SIZE :
                ((coordinate.getX() - BLOCK_SIZE + 1) / BLOCK_SIZE) * BLOCK_SIZE,
                coordinate.getY() >= 0 ? (coordinate.getY() / BLOCK_SIZE) * BLOCK_SIZE :
                        ((coordinate.getY() - BLOCK_SIZE + 1) / BLOCK_SIZE) * BLOCK_SIZE);
    }

    /**
     * Gets the block that the cell is in
     * @param coordinate The coordinate of the cell
     * @return Returns the block of the cell
     */
    public Block getBlock(Coordinate coordinate) {
        return dictionary.get(getBlockCoordinate(coordinate));
    }

    /**
     * Gets the population of the grid (all cells above state 0)
     * @return Returns the population of the grid
     */
    public int getPopulation() {
        int population = 0;
        for (Block block: this) {
            population += block.getPopulation();
        }

        return population;
    }

    /**
     * Gets the blocks in the grid
     * @return Returns the blocks in the grid
     */
    public Collection<Block> getBlocks() {
        return dictionary.values();
    }

    /**
     * Gets the coordinates of all the blocks in the grid
     * @return Returns the coordinates of all the blocks in the grid.
     */
    public Set<Coordinate> getBlockCoordinates() {
        return dictionary.keySet();
    }

    @Override  // Implementing these methods allow it to be used in a for each loop
    public boolean hasNext() {
        return dictionary.values().iterator().hasNext();
    }

    @Override
    public Block next() {
        return dictionary.values().iterator().next();
    }

    @Override
    public Iterator<Block> iterator() {
        return dictionary.values().iterator();
    }
}
