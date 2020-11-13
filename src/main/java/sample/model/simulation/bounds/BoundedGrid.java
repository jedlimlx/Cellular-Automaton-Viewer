package sample.model.simulation.bounds;

import sample.model.Coordinate;

/**
 * The base class for all bounded grid types
 */
public abstract class BoundedGrid {
    protected String specifier;
    protected Coordinate initialCoordinate;
    protected int width, height;

    /**
     * Constructs a bounded grid
     * @param specifier The bounded grid specifier
     * @param initialCoordinate The initial coordinate of the bounded grid
     */
    public BoundedGrid(String specifier, Coordinate initialCoordinate) {
        setSpecifier(specifier);
        this.initialCoordinate = initialCoordinate;
    }

    /**
     * Parses the identifier string of the bounded grid (e.g. T100,50)
     * @param string The string to parse
     */
    public abstract void parse(String string);

    /**
     * Canonises the bounded grid specifier
     * @return Returns the canonised string
     */
    public abstract String canonise();

    /**
     * Gets the regex for the identifier string of the bounded grid
     * @return Returns the regexes for identifying the bounded grid
     */
    public abstract String[] getRegex();

    /**
     * Maps a coordinate to its new position
     * @param coordinate The coordinate to map
     * @return Returns the new mapped coordinate
     */
    public abstract Coordinate map(Coordinate coordinate);

    /**
     * Checks if a coordinate is at the edge of the bounded grid
     * @param coordinate Coordinate to check
     * @return Returns true if the coordinate is at the edge of the bounded grid, false otherwise
     */
    public boolean atEdge(Coordinate coordinate) {
        if (width != 0 && height != 0) {
            return coordinate.getX() < initialCoordinate.getX() ||
                    coordinate.getY() < initialCoordinate.getY() ||
                    coordinate.getX() >= initialCoordinate.getX() + width ||
                    coordinate.getY() >= initialCoordinate.getY() + height;
        } else if (width == 0) {
            return coordinate.getX() < initialCoordinate.getX() ||
                    coordinate.getX() >= initialCoordinate.getX() + width;
        } else {
            return coordinate.getY() < initialCoordinate.getY() ||
                    coordinate.getY() >= initialCoordinate.getY() + height;
        }
    }

    /**
     * Gets the bounded grid specifier
     * @return Returns the bounded grid specifier
     */
    public String getSpecifier() {
        return specifier;
    }

    /**
     * Gets the initial coordinate of the bounded grid
     * @return Returns the initial coordinate of the bounded grid
     */
    public Coordinate getInitialCoordinate() {
        return initialCoordinate;
    }

    /**
     * Sets the bounded grid specifier
     * @param specifier The bounded grid specifier
     */
    public void setSpecifier(String specifier) {
        parse(specifier);
        this.specifier = canonise();
    }

    /**
     * Sets the initial coordinate of the bounded grid
     * @param initialCoordinate The initial coordinate of the bounded grid
     */
    public void setInitialCoordinate(Coordinate initialCoordinate) {
        this.initialCoordinate = initialCoordinate;
    }

    public abstract Object clone();
}
