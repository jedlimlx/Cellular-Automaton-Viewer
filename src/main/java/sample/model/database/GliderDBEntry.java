package sample.model.database;

import sample.model.Utils;
import sample.model.patterns.Spaceship;
import sample.model.simulation.Grid;

/**
 * Represents an entry in the GliderDB database
 */
public class GliderDBEntry extends DatabaseEntry {
    private Spaceship spaceship;
    private String discoverer = "", name = "";

    /**
     * Constructs the database entry from a string
     * @param entry The entry from the database
     */
    public GliderDBEntry(String entry) {
        super(entry);

        String[] tokens = entry.split(":");

        // Handle the <period>/2 notation for glide symmetric
        if (tokens[4].contains("/")) tokens[4] = Integer.parseInt(tokens[4].split("/")[0]) * 2 + "";

        this.name = tokens[0];
        this.discoverer = tokens[1];
        this.spaceship = new Spaceship(Utils.fromRulestring(tokens[2]), new Grid(tokens[9]),
                Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]), Integer.parseInt(tokens[6]));
        this.spaceship.setMinRule(Utils.fromRulestring(tokens[2]));
        this.spaceship.setMaxRule(Utils.fromRulestring(tokens[3]));
    }

    /**
     * Constructs the database entry from a spaceship
     * @param spaceship The spaceship
     * @param discoverer The discoverer of the spaceship
     * @param name The name of the spaceship
     */
    public GliderDBEntry(Spaceship spaceship, String discoverer, String name) {
        super(spaceship);

        this.spaceship = spaceship;
        this.discoverer = discoverer;
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);
        builder.append(":").append(discoverer).append(":").
                append(spaceship.getMinRule()).append(":").
                append(spaceship.getMaxRule()).append(":").
                append(spaceship.getPeriod()).append(":").
                append(spaceship.getDisplacementX()).append(":").
                append(spaceship.getDisplacementY()).append(":");

        spaceship.updateBounds();

        int width = spaceship.getBounds().getValue1().getX() - spaceship.getBounds().getValue0().getX();
        int height = spaceship.getBounds().getValue1().getY() - spaceship.getBounds().getValue0().getY();
        builder.append(width).append(":").append(height).append(":").append(spaceship.toRLE());

        return builder.toString();
    }

    /**
     * Gets the spaceship
     * @return Returns the spaceship
     */
    public Spaceship getSpaceship() {
        return spaceship;
    }

    /**
     * Gets the name of the spaceship
     * @return Returns the name of the spaceship
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the discoverer of the spaceship
     * @return Returns the discoverer of the spaceship
     */
    public String getDiscoverer() {
        return discoverer;
    }
}
