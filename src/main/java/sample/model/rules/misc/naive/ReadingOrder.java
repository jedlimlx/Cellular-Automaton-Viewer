package sample.model.rules.misc.naive;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import sample.model.Coordinate;

import java.util.Comparator;

/**
 * The base class for naive reading orders
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class ReadingOrder {
    /**
     * The specifier for the naive reading order
     */
    protected String specifier;

    /**
     * Constructs a new naive reading order
     * @param specifier The specifier for the naive reading order
     */
    public ReadingOrder(String specifier) {
        setSpecifier(specifier);
    }

    /**
     * Sets the specifier for the naive reading order
     * @param specifier The specifier of the naive reading order
     */
    public abstract void setSpecifier(String specifier);

    /**
     * Gets the specifier for the naive reading order
     * @return Returns the specifier for the naive reading order
     */
    public String getSpecifier() {
        return specifier;
    }

    /**
     * Gets the regex that will identify the naive reading order
     * @return Returns the regexes that match a naive reading order
     */
    public abstract String[] getRegex();

    /**
     * Gets the coordinate comparator
     * @return Returns the coordinate comparator
     */
    public abstract Comparator<Coordinate> getCoordinateComparator();

    /**
     * Clones the naive reading order
     * @return Returns the cloned naive reading order
     */
    public abstract Object clone();
}
