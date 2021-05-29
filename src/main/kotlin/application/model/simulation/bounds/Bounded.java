package application.model.simulation.bounds;

import application.model.Coordinate;

/**
 * Implements a bounded grid
 */
public class Bounded extends BoundedGrid {
    public Bounded() {
        this("P10", new Coordinate());
    }

    /**
     * Constructs a bounded grid
     * @param specifier The specifier for the bounded grid
     * @param initialCoordinate The initial coordinate for the bounded grid
     */
    public Bounded(String specifier, Coordinate initialCoordinate) {
        super(specifier, initialCoordinate);
    }

    /**
     * Parses the identifier string for the bounded grid
     * @param string The string to parse
     */
    @Override
    public void parse(String string) {
        if (string.matches("P\\d+,\\d+")) {
            width = Integer.parseInt(string.split(",")[0].substring(1));
            height = Integer.parseInt(string.split(",")[1]);
        } else if (string.matches("P\\d+")) {
            width = Integer.parseInt(string.substring(1));
            height = width;
        } else {
            throw new IllegalArgumentException("Invalid bounded grid specifier!");
        }
    }

    /**
     * Canonises the specifier string of the bounded grid
     * @return Returns the canonised string
     */
    @Override
    public String canonise() {
        if (width != height) return "P" + width + "," + height;
        else return "P" + width;
    }

    @Override
    public String[] getRegex() {
        return new String[]{"P\\d+(,\\d+)?"};
    }

    @Override
    public Coordinate map(Coordinate coordinate) {
        return new Coordinate(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public Object clone() {
        return new Bounded(specifier, initialCoordinate);
    }
}
