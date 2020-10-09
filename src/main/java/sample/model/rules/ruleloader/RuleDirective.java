package sample.model.rules.ruleloader;

import org.javatuples.Pair;
import sample.model.Coordinate;
import sample.model.rules.Tiling;
import sample.model.rules.ruleloader.ruletable.Symmetry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a directive that represents the CA rule.
 */
public abstract class RuleDirective extends Directive {
    /**
     * The number of states of the CA rule
     */
    protected int numStates;

    /**
     * The tiling of the CA rule
     */
    protected Tiling tiling;

    /**
     * Built-in neighbourhood aliases (read in from ruleloader/neighbourhoods.txt)
     */
    private Map<String, String> neighbourhoodAliases;

    /**
     * Built-in symmetry aliases (read in from ruleloader/neighbourhoods.txt)
     */
    private Map<Pair<Coordinate[], String>, Symmetry> symmetryAliases;

    /**
     * Constructs the directive with the provided content
     * @param content Content of the directive
     */
    public RuleDirective(String content) {
        super(content);

        // Reading aliases
        symmetryAliases = new LinkedHashMap<>();
        neighbourhoodAliases = new HashMap<>();

        String line;
        Scanner scanner = new Scanner(getClass().getResourceAsStream("/ruleloader/neighbourhoods.txt"));

        Coordinate[] currentNeighbourhood = new Coordinate[0];
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (line.startsWith("#N")) {
                currentNeighbourhood = getNeighbourhood(line.split(":")[1]);
                neighbourhoodAliases.put(line.split(":")[0].replaceAll("#N\\s*", ""),
                        line.split(":")[1]);
            } else if (line.startsWith("#S")) {
                symmetryAliases.put(new Pair<>(currentNeighbourhood, line.split(":")[0].
                        replaceAll("#S\\s*", "")), new Symmetry(line.split(":")[1]));
            }
        }
    }

    /**
     * Gets the tiling of the rule
     * @return Returns the tiling of the rule
     */
    public Tiling getTiling() {
        return tiling;
    }

    /**
     * Gets the number of states of the rule
     * @return Returns the number of states of the rule
     */
    public int getNumStates() {
        return numStates;
    }

    /**
     * This method returns the neighbourhood of a given cell
     * @return A list of Coordinates that represent the neighbourhood
     */
    public abstract Coordinate[] getNeighbourhood();

    /**
     * This method represents the transition function of the rule
     * @param neighbours The cell's neighbours in the order of the neighbourhood provided
     * @param cellState The current state of the cell
     * @return The state of the cell in the next generation
     */
    public abstract int transitionFunc(int[] neighbours, int cellState);

    /**
     * Gets the neighbourhood of the rule given the line where the neighbourhood is defined.
     * For example, "neighbourhood:Moore"
     * @param content The line where the neighbourhood is defined
     * @return Returns the neighbourhood of the rule
     */
    protected Coordinate[] getNeighbourhood(String content) {
        // ~~~ to avoid infinte recursion
        if (!content.contains("~~~")) {  // Check for aliases
            for (String aliases: neighbourhoodAliases.keySet()) {
                if (content.matches("^[a-zA-Z0-9_.:=]*\\s*\\s*" + aliases)) {
                    return getNeighbourhood(neighbourhoodAliases.get(aliases) + "~~~");
                }
            }
        }

        ArrayList<Coordinate> neighbourhood = new ArrayList<>();
        Matcher matcher = Pattern.compile("(-?\\d+),\\s*(-?\\d+)").matcher(content);
        while (matcher.find()) neighbourhood.add(new Coordinate(Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2))));

        return neighbourhood.toArray(new Coordinate[0]);
    }

    /**
     * Gets the weights of the rule given the line where the weights are defined.
     * For example, "neighbourhood:[2 * (0, 1), ...]"
     * @param content The line where the weights is defined
     * @return Returns the weights of the rule
     */
    protected int[] getWeights(String content) {
        // ~~~ to avoid infinte recursion
        if (!content.contains("~~~")) {  // Check for aliases
            for (String aliases: neighbourhoodAliases.keySet()) {
                if (content.matches("[a-zA-Z0-9_.:=]*\\s*\\s*" + aliases)) {
                    return getWeights(aliases + "~~~");
                }
            }
        }

        ArrayList<Integer> weights = new ArrayList<>();
        Matcher matcher = Pattern.compile("((-?\\d+)\\s*\\*)?\\s*\\((-?\\d+,\\s*-?\\d+)\\)").matcher(content);
        while (matcher.find()) {
            try {
                weights.add(Integer.parseInt(matcher.group(2)));
            } catch (NumberFormatException exception) {
                weights.add(1);
            }
        }

        // Converting to arrays because java is annoying
        int[] weightsArray = new int[weights.size()];
        for (int i = 0; i < weights.size(); i++) {
            weightsArray[i] = weights.get(i);
        }

        return weightsArray;
    }

    /**
     * Gets the symmetry from the line where the symmetry is defined
     * @param content The content of the line
     * @return Returns the symmetry obtained
     */
    protected Symmetry getSymmetry(String content) {
        // ~~~ to avoid infinte recursion
        if (!content.contains("~~~")) {  // Check for aliases
            for (Pair<Coordinate[], String> aliases: symmetryAliases.keySet()) {
                if (Arrays.equals(aliases.getValue0(), getNeighbourhood())) {
                    if (content.matches("[a-zA-Z0-9_.]*\\s*:?\\s*" + aliases.getValue1())) {
                        return symmetryAliases.get(aliases);
                    }
                }
            }
        }

        return new Symmetry(content);
    }

    /**
     * Gets the state weights of the rule given the line where the state weights are defined.
     * For example, "state_weights:0,1,0,0..."
     * @param content The line where the state weights is defined
     * @return Returns the state weights of the rule
     */
    protected int[] getStateWeights(String content) {
        content = content.replaceAll("state_weights:\\s*", "");

        String[] tokens = content.split(",\\s*");
        int[] stateWeights = new int[tokens.length];

        for (int i = 0; i < stateWeights.length; i++) {
            stateWeights[i] = Integer.parseInt(tokens[i]);
        }

        return stateWeights;
    }

    /**
     * Gets the tiling of the rule given the line where the tiling is defined
     * @param content The line where the tiling is defined
     * @return Returns the tiling of the rule
     */
    protected Tiling getTiling(String content) {
        content = content.replaceAll("tiling:\\s*", "");

        if (content.toLowerCase().equals("hexagonal")) {
            return Tiling.Hexagonal;
        }
        else if (content.toLowerCase().equals("triangular")) {
            return Tiling.Triangular;
        }
        else {
            return Tiling.Square;
        }
    }
}