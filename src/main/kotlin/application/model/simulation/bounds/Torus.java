package application.model.simulation.bounds;

import application.model.Coordinate;

/**
 * Implements a toroidal grid
 *
 * TODO (Offsets such as T300+10,30)
 */
public class Torus extends BoundedGrid {
    public Torus() {
        this("T10", new Coordinate());
    }

    /**
     * Constructs a toroidal grid
     * @param specifier The specifier for the toroidal grid
     * @param initialCoordinate The initial coordinate for the torus
     */
    public Torus(String specifier, Coordinate initialCoordinate) {
        super(specifier, initialCoordinate);
    }

    /**
     * Parses the identifier string for the toroidal grid
     * @param string The string to parse
     */
    @Override
    public void parse(String string) {
        if (string.matches("T\\d+,\\d+")) {
            width = Integer.parseInt(string.split(",")[0].substring(1));
            height = Integer.parseInt(string.split(",")[1]);
        } else if (string.matches("T\\d+")) {
            width = Integer.parseInt(string.substring(1));
            height = width;
        } else {
            throw new IllegalArgumentException("Invalid bounded grid specifier!");
        }
    }

    /**
     * Canonises the specifier string of the toroidal grid
     * @return Returns the canonised string
     */
    @Override
    public String canonise() {
        if (width != height) return "T" + width + "," + height;
        else return "T" + width;
    }

    @Override
    public String[] getRegex() {
        return new String[]{"T\\d+(,\\d+)?"};
    }

    @Override
    public Coordinate map(Coordinate coordinate) {
        Coordinate newCoordinate = coordinate.subtract(initialCoordinate);

        if (width != 0 && height != 0) {
            return new Coordinate(Math.floorMod(newCoordinate.getX(), width),
                    Math.floorMod(newCoordinate.getY(), height)).add(initialCoordinate);
        } else if (width == 0) {
            return new Coordinate(newCoordinate.getX(),
                    Math.floorMod(newCoordinate.getY(), height)).add(initialCoordinate);
        } else {
            return new Coordinate(Math.floorMod(newCoordinate.getX(), width),
                    newCoordinate.getY()).add(initialCoordinate);
        }
    }

    @Override
    public Object clone() {
        return new Torus(specifier, initialCoordinate);
    }
}
