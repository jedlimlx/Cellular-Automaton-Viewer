package application.model.rules.misc.naive;

import application.model.Coordinate;

import java.util.Comparator;

/**
 * Represents the orthogonal naive reading order
 */
public class Orthogonal extends ReadingOrder {
    private final Comparator<Coordinate> coordinateComparator;

    public Orthogonal() {
        this("O");
    }

    public Orthogonal(String specifier) {
        super(specifier);

        coordinateComparator = (o1, o2) -> {
            if (o1.getY() == o2.getY()) {
                return Integer.compare(o1.getX(), o2.getX());
            }
            else {
                return Integer.compare(o1.getY(), o2.getY());
            }
        };
    }

    @Override
    public void setSpecifier(String specifier) {
        this.specifier = specifier;
    }

    @Override
    public String[] getRegex() {
        return new String[]{"O"};
    }

    @Override
    public Comparator<Coordinate> getCoordinateComparator() {
        return coordinateComparator;
    }

    @Override
    public Object clone() {
        return new Orthogonal(specifier);
    }
}
