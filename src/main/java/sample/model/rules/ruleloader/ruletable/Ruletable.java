package sample.model.rules.ruleloader.ruletable;

import sample.model.Coordinate;
import sample.model.LRUCache;
import sample.model.Utils;
import sample.model.rules.ruleloader.RuleDirective;

import java.util.*;

/**
 * Implements Golly ruletables with addition features such as unbounded rules and arbitrary neighbourhoods.
 * See https://github.com/GollyGang/ruletablerepository/wiki.
 *
 * TODO (Nutshell syntax)
 */
public class Ruletable extends RuleDirective {
    /**
     * The contents of the ruletable
     */
    private String content;

    /**
     * The ruletable's neighbourhood
     */
    private Coordinate[] neighbourhood;

    /**
     * Does the ruletable use permute symmetry?
     */
    private boolean permute;

    /**
     * The symmetry used by the ruletable (null if permute symmetry is used)
     */
    private Symmetry symmetry;

    /**
     * The ruletable transitions
     */
    private ArrayList<Transition> transitions;

    /**
     * The variables (bounded or unbounded) that are used by the ruletable
     */
    private Map<String, Variable> variables;

    private LRUCache<Integer, Integer> lruCache;

    /**
     * Constructs the ruletable with the provided content
     * @param content Content of the ruletable
     */
    public Ruletable(String content) {
        super(content);
    }

    /**
     * Parses the content of the ruletable
     * @param content The content of the ruletable
     * @throws IllegalArgumentException Thrown when the ruletable inputted is invalid
     */
    @Override
    public void parseContent(String content) throws IllegalArgumentException {
        this.content = content;
        this.transitions = new ArrayList<>();
        this.variables = new HashMap<>();
        this.lruCache = new LRUCache<>(20000);

        for (String line: content.split("\n")) {
            if (line.startsWith("n_states") || line.startsWith("states")) {
                numStates = Integer.parseInt(line.replaceAll("(n_)?states:", ""));
            } else if (line.startsWith("neighbourhood") || line.startsWith("neighborhood")) {
                neighbourhood = getNeighbourhood(line);
            } else if (line.startsWith("tiling")) {
                tiling = getTiling(line.replace("tiling:", ""));
            } else if (line.startsWith("symmetries")) {
                if (line.contains("permute")) {
                    permute = true;
                    symmetry = new Symmetry("");
                }
                else {
                    symmetry = getSymmetry(line.replace("symmetries:", ""));
                }
            } else if (line.startsWith("var")) {  // TODO (Handle {0, var}) Eg. 2c7and3c14
                String name = Utils.matchRegex("var\\s*([a-zA-Z0-9_.-]+)\\s*=", line, 0, 1);

                try {
                    String[] tokens = Utils.matchRegex("\\{?(\\d+,?\\s*)+}?", line, 0).split(",\\s*");

                    HashSet<Integer> values = new HashSet<>();
                    for (String token: tokens) {
                        values.add(Integer.parseInt(token.replaceAll("[{}]", "")));
                    }

                    variables.put(name, new Variable(name, false, values));
                } catch (IllegalStateException exception) {
                    String name2 = Utils.matchRegex("\\s*=\\s*(\\S+)", line, 0, 1);
                    variables.put(name, new Variable(name, false, variables.get(name2).getValues()));
                }
            } else if (line.startsWith("unbound")) {
                String name = Utils.matchRegex("unbound\\s*([a-zA-Z0-9_.-]+)\\s*=", line, 0, 1);
                String[] tokens = Utils.matchRegex("\\{?(\\d+,?\\s*)+}?", line, 0).split(",\\s*");

                HashSet<Integer> values = new HashSet<>();
                for (String token: tokens) {
                    values.add(Integer.parseInt(token.replaceAll("[{}]", "")));
                }

                variables.put(name, new Variable(name, true, values));
            } else if (line.matches("([a-zA-Z0-9._-]+,?\\s*)+") && line.contains(",")) {
                String[] tokens = line.split(",\\s*");
                ArrayList<String> toPermute = new ArrayList<>(Arrays.asList(tokens).subList(1, tokens.length));

                StringBuilder reconstructed;
                if (!permute) {
                    for (ArrayList<String> permutation: symmetry.applySymmetry(toPermute)) {
                        // Reconstructing the permutation
                        reconstructed = new StringBuilder(tokens[0] + ",");
                        for (String s : permutation) {
                            reconstructed.append(s).append(",");
                        }

                        reconstructed.append(tokens[tokens.length - 1]);

                        // Adding the transition
                        transitions.add(new Transition(numStates, permute, reconstructed.toString(), variables));
                    }
                }
                else {
                    // Adding the transition
                    transitions.add(new Transition(numStates, true, line, variables));
                }
            }
        }
    }

    @Override
    public Coordinate[] getNeighbourhood() {
        return neighbourhood;
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState) {
        Integer cached = lruCache.get(Arrays.hashCode(neighbours) ^ cellState);
        if (cached == null) {
            int result;
            for (Transition transition: transitions) {
                result = transition.applyTransition(cellState, neighbours);
                if (result != -1) {
                    // lruCache.setValue(Arrays.hashCode(neighbours) ^ cellState, result);
                    return result;
                }
            }

            // TODO (Make ruletables faster, somehow...)
            // lruCache.setValue(Arrays.hashCode(neighbours) ^ cellState, cellState);
            return cellState;
        }
        else {
            return cached;
        }
    }

    @Override
    public Object clone() {
        return new Ruletable(content);
    }
}
