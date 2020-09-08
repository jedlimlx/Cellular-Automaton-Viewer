package sample.model;

import org.javatuples.Pair;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents and serves as pattern storage. <br>
 * You may also iterate over it like in a loop. <br>
 * <br>
 * Example Usage: <br>
 * <pre>
 * Grid grid = new Grid();
 * grid.fromRLE("bo$obo$o2bo$bobo$2bo!", new Coordinate(0, 0))
 * for (Coordinate cell: grid) {
 *      System.out.println(cell);
 * }
 * </pre>
 */
public class Grid implements Iterable<Coordinate>, Iterator<Coordinate> {
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
     * The dictionary that stores the pattern.
     * {(0, 0): 1, (0, 2): 2, ...}
     */
    private final HashMap<Coordinate, Integer> dictionary;

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
    private Grid(HashMap<Coordinate, Integer> dictionary) {
        this.background = 0;
        this.dictionary = (HashMap<Coordinate, Integer>) dictionary.clone();
        this.startCoordinate = new Coordinate(Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.endCoordinate = new Coordinate(-Integer.MAX_VALUE, -Integer.MAX_VALUE);
    }

    /**
     * Sets the cell at position coordinate to the specified state
     * @param coordinate The coordinate of the cell
     * @param state The state of the cell
     */
    public void setCell(Coordinate coordinate, int state) {
        state = convertCell(state);

        if (state == 0)
            dictionary.remove(coordinate);
        else
            dictionary.put(coordinate, state);
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
     * Inserts the provided pattern at the specified coordinate
     * @param pattern The pattern to be inserted
     * @param coordinate The coordinate where the pattern is to be inserted
     */
    public void insertCells(Grid pattern, Coordinate coordinate) {
        for (Coordinate coord: pattern) {
            setCell(coord.add(coordinate), pattern.getCell(coord));
        }
    }

    /**
     * Clears all cells between the coordinates specified
     * @param start The starting coordinate
     * @param end The end coordinate
     */
    public void clearCells(Coordinate start, Coordinate end) {
        for (int x = start.getX(); x < end.getX() + 1; x++) {
            for (int y = start.getY(); y < end.getY() + 1; y++) {
                setCell(x, y, 0);
            }
        }
    }

    /**
     * Gets the state of the cell at the specified coordinate
     * @param coordinate The coordinate of the cell
     * @return Returns the state of the cell
     */
    public int getCell(Coordinate coordinate) {
        Integer state = dictionary.get(coordinate);
        return convertCell(Objects.requireNonNullElse(state, 0));
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
     * Returns a deepcopy of the dictionary used to store the pattern
     * @return Returns a dictionary contains all the cells of the pattern
     */
    public HashMap<Coordinate, Integer> getCells() {
        return (HashMap<Coordinate, Integer>) dictionary.clone();
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

        for (Coordinate coordinate: this) {
            // Updating the bounds
            if (coordinate.getX() < startCoordinate.getX())
                startCoordinate = new Coordinate(coordinate.getX(), startCoordinate.getY());
            if (coordinate.getY() < startCoordinate.getY())
                startCoordinate = new Coordinate(startCoordinate.getX(), coordinate.getY());
            if (coordinate.getX() > endCoordinate.getX())
                endCoordinate = new Coordinate(coordinate.getX(), endCoordinate.getY());
            if (coordinate.getY() > endCoordinate.getY())
                endCoordinate = new Coordinate(endCoordinate.getX(), coordinate.getY());
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
        int index = 0;
        Coordinate[] array = new Coordinate[this.size()];
        for (Coordinate coordinate: this) {
            array[index] = coordinate;
            index++;
        }

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
        ArrayList<Character> rleArray = new ArrayList<>();
        for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
            for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
                if (getCell(x, y) == 0) {
                    rleArray.add('.');
                }
                else {
                    rleArray.add((char) (getCell(x, y) + 64));
                }
            }
            rleArray.add('$');
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

        for (int i = 0; i < size(); i++) {
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
        // TODO (Copy Golly's Hash Function)
        updateBounds();

        /*
        int GSF_hash(int x, int y, int wd, int ht)
        {
            // calculate a hash value for pattern in given rect
            int hash = 31415962;
            int right = x + wd - 1;
            int bottom = y + ht - 1;
            int cx, cy;
            int v = 0;
            lifealgo* curralgo = currlayer->algo;
            bool multistate = curralgo->NumCellStates() > 2;

            for ( cy=y; cy<=bottom; cy++ ) {
                int yshift = cy - y;
                for ( cx=x; cx<=right; cx++ ) {
                    int skip = curralgo->nextcell(cx, cy, v);
                    if (skip >= 0) {
                        // found next live cell in this row (v is >= 1 if multistate)
                        cx += skip;
                        if (cx <= right) {
                            // need to use a good hash function for patterns like AlienCounter.rle
                            hash = (hash * 1000003) ^ yshift;
                            hash = (hash * 1000003) ^ (cx - x);
                            if (multistate) hash = (hash * 1000003) ^ v;
                        }
                    } else {
                        cx = right;  // done this row
                    }
                }
            }

            return hash;
        }
         */

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
     * Set the background of the grid
     * @param background The background
     */
    public void setBackground(int background) {
        this.background = background;
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

    @Override  // Implementing these methods allow it to be used in a for each loop
    public boolean hasNext() {
        return dictionary.keySet().iterator().hasNext();
    }

    @Override
    public Coordinate next() {
        return dictionary.keySet().iterator().next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Coordinate> iterator() {
        return dictionary.keySet().iterator();
    }
}
