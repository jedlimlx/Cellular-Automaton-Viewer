package sample.model;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grid implements Iterable<Coordinate>, Iterator<Coordinate> {
    private final HashMap<Coordinate, Integer> dictionary;

    public Grid() {
        this.dictionary = new HashMap<>();
    }

    public Grid(HashMap<Coordinate, Integer> dictionary) {
        this.dictionary = (HashMap<Coordinate, Integer>) dictionary.clone();
    }

    // Setting state of the cell at (x, y)
    public void setCell(Coordinate coordinate, int state) {
        if (state == 0) {
            dictionary.remove(coordinate);
        }
        else {
            dictionary.put(coordinate, state);
        }
    }

    public void setCell(int x, int y, int state) {
        if (state == 0) {
            dictionary.remove(new Coordinate(x, y));
        }
        else {
            dictionary.put(new Coordinate(x, y), state);
        }
    }

    // Insert cells at coordinate specified
    public void insertCells(Grid pattern, Coordinate coordinate) {
        for (Coordinate coord: pattern) {
            setCell(coord.add(coordinate), pattern.getCell(coord));
        }
    }

    // Clears all cells withnin the coordinate specified
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
        return Objects.requireNonNullElse(state, 0);
    }

    public int getCell(int x, int y) {
        Integer state = dictionary.get(new Coordinate(x, y));
        return Objects.requireNonNullElse(state, 0);
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
        for (int x = startCoordinate.getX(); x < endCoordinate.getX(); x++) {
            for (int y = startCoordinate.getY(); y < endCoordinate.getY(); y++) {
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
        return rleString.toString();
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
                        setCell(x++, y, (int) lastChar - 1); break;
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
