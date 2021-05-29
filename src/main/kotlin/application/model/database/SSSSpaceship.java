package application.model.database;

import application.model.Utils;
import application.model.patterns.Spaceship;
import application.model.rules.RuleFamily;
import application.model.simulation.Grid;

/**
 * A spaceship from the 5S database
 */
public class SSSSpaceship extends DatabaseEntry implements Comparable<SSSSpaceship> {
    private final Spaceship spaceship;

    public SSSSpaceship(String entry) {
        super(entry);

        String[] tokens = entry.split(",\\s*");
        spaceship = new Spaceship(Utils.fromRulestring(tokens[1]), new Grid(tokens[5]),
                Integer.parseInt(tokens[4]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
    }

    public SSSSpaceship(Spaceship spaceship) {
        super(spaceship);
        this.spaceship = spaceship;
    }

    @Override
    public String toString() {
        return spaceship.getPopulation() + ", " + ((RuleFamily) spaceship.getRule()).getRulestring() +
                ", " + spaceship.getDisplacementX() + ", " + spaceship.getDisplacementY() + ", " +
                spaceship.getPeriod() + ", " + spaceship.toRLE();
    }

    public Spaceship getSpaceship() {
        return spaceship;
    }

    @Override
    public int compareTo(SSSSpaceship sssSpaceship) {
        if (sssSpaceship.getSpaceship().getPeriod() == spaceship.getPeriod()) {
            if (spaceship.getDisplacementX() == sssSpaceship.getSpaceship().getDisplacementX())
                return Integer.compare(sssSpaceship.getSpaceship().getDisplacementY(), spaceship.getDisplacementY());
            return Integer.compare(sssSpaceship.getSpaceship().getDisplacementX(), spaceship.getDisplacementX());
        }

        return Integer.compare(spaceship.getPeriod(), sssSpaceship.getSpaceship().getPeriod());
    }
}
