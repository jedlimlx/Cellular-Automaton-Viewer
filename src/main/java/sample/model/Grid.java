package sample.model;

import org.javatuples.Pair;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grid implements Iterable<Coordinate>, Iterator<Coordinate> {
    private int background;

    private Coordinate startCoordinate, endCoordinate;
    private final HashMap<Coordinate, Integer> dictionary;

    public Grid() {
        this.background = 0;
        this.dictionary = new HashMap<>();
        this.startCoordinate = new Coordinate(Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.endCoordinate = new Coordinate(-Integer.MAX_VALUE, -Integer.MAX_VALUE);
    }

    public Grid(HashMap<Coordinate, Integer> dictionary) {
        this.background = 0;
        this.dictionary = (HashMap<Coordinate, Integer>) dictionary.clone();
        this.startCoordinate = new Coordinate(Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.endCoordinate = new Coordinate(-Integer.MAX_VALUE, -Integer.MAX_VALUE);
    }

    // Setting state of the cell at (x, y)
    public void setCell(Coordinate coordinate, int state) {
        state = convertCell(state);

        if (state == 0)
            dictionary.remove(coordinate);
        else
            dictionary.put(coordinate, state);
    }

    public void setCell(int x, int y, int state) {
        setCell(new Coordinate(x, y), state);
    }

    // Insert cells at coordinate specified
    public void insertCells(Grid pattern, Coordinate coordinate) {
        for (Coordinate coord: pattern) {
            setCell(coord.add(coordinate), pattern.getCell(coord));
        }
    }

    // Clears all cells within the coordinate specified
    public void clearCells(Coordinate start, Coordinate end) {
        for (int x = start.getX(); x < end.getX() + 1; x++) {
            for (int y = start.getY(); y < end.getY() + 1; y++) {
                setCell(x, y, 0);
            }
        }
    }

    // Get the state of the cell at (x, y)
    public int getCell(Coordinate coordinate) {
        Integer state = dictionary.get(coordinate);
        return convertCell(Objects.requireNonNullElse(state, 0));
    }

    public int getCell(int x, int y) {
        return getCell(new Coordinate(x, y));
    }

    // Get all the cells in the pattern
    public HashMap<Coordinate, Integer> getCells() {
        return (HashMap<Coordinate, Integer>) dictionary.clone();
    }

    // Get all cells from start to end
    public Grid getCells(Coordinate start, Coordinate end) {
        Grid grid = new Grid();
        for (int x = start.getX(); x < end.getX() + 1; x++) {
            for (int y = start.getY(); y < end.getY() + 1; y++) {
                grid.setCell(x, y, this.getCell(x, y));
            }
        }

        return grid;
    }

    // Updates the bounds of the grid
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

    // Gets the bounds of the grid
    public Pair<Coordinate, Coordinate> getBounds() {
        return new Pair<>(startCoordinate, endCoordinate);
    }

    // Converts the pattern into an array of coordinates
    public Coordinate[] toArray() {
        int index = 0;
        Coordinate[] array = new Coordinate[this.size()];
        for (Coordinate coordinate: this) {
            array[index] = coordinate;
            index++;
        }

        return array;
    }

    // Reflect grid horizontally
    public void reflectCellsX(Coordinate startCoordinate, Coordinate endCoordinate) {
        Grid grid = this.deepCopy();  // Make a deep copy for reference
        for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
            for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
                setCell(endCoordinate.getX() - x + startCoordinate.getX(), y, grid.getCell(x, y));
            }
        }
    }

    // Reflect grid vertically
    public void reflectCellsY(Coordinate startCoordinate, Coordinate endCoordinate) {
        Grid grid = this.deepCopy();  // Make a deep copy for reference

        // Flipping vertically
        for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
            for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
                setCell(x, endCoordinate.getY() - y + startCoordinate.getY(), grid.getCell(x, y));
            }
        }
    }

    // Rotate grid clockwise
    public void rotateCW(Coordinate startCoordinate, Coordinate endCoordinate) {
        Grid grid = this.deepCopy();  // Make a deep copy for reference

        // Rotating clockwise
        for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
            for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
                setCell(x - startCoordinate.getX() + startCoordinate.getY(),
                        endCoordinate.getX() - y + startCoordinate.getY(), grid.getCell(x, y));
            }
        }
    }

    // Rotate grid counter-clockwise
    public void rotateCCW(Coordinate startCoordinate, Coordinate endCoordinate) {
        Grid grid = this.deepCopy();  // Make a deep copy for reference

        // Rotating counter-clockwise
        for (int x = startCoordinate.getX(); x < endCoordinate.getX() + 1; x++) {
            for (int y = startCoordinate.getY(); y < endCoordinate.getY() + 1; y++) {
                setCell(endCoordinate.getX() - x + startCoordinate.getX(),
                        y - startCoordinate.getY() + startCoordinate.getX(), grid.getCell(x, y));
            }
        }
    }

    // Converts pattern to the run length encoded (RLE) format
    // Only returns the body (no header)
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

    // Converts the entire pattern to RLE
    public String toRLE() {
        updateBounds();
        return toRLE(startCoordinate, endCoordinate);
    }

    // Load a pattern from the RLE format
    // Only accepts the RLE body (no \n)
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

    // Method to deep copy grid
    public Grid deepCopy() {
        return new Grid(dictionary);
    }

    // Method to shallow copy grid
    public Grid shallowCopy() {
        return this;
    }

    // Size of the grid
    public int size() {
        return dictionary.size();
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grid that = (Grid) o;
        return that.hashCode() == this.hashCode();
    }

    @Override  // Independent of translation
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

    public void setBackground(int background) {
        this.background = background;
    }

    public int convertCell(int state) {
        if (state == background)
            return 0;
        else if (state == 0)
            return background;
        else
            return state;
    }
}
