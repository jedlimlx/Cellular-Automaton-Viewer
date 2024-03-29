package application.model.rules.ruleloader.ruletable;

import application.model.Coordinate;
import application.model.Utils;
import application.model.rules.isotropic.transitions.INTTransitions;
import application.model.rules.ruleloader.RuleDirective;

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
     * The ruletable's weights
     */
    private int[] weights;

    /**
     * The maximum neighbourhood sum (used in OT rules)
     */
    private int maxNeighbourhoodSum;

    /**
     * The transition map for weighted rules
     */
    private Map<Integer, ArrayList<String>> transitionMap;

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

    /**
     * The predefined LIVE, DEAD and ANY variables
     */
    public static Variable LIVE, DEAD, ANY;

    /**
     * Constructs the ruletable with the provided content
     * @param content Content of the ruletable
     */
    public Ruletable(String content) {
        super(content);

        LIVE = new Variable("live", true, new HashSet<>(Collections.singletonList(1)));
        symmetry = new Symmetry("");
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
                    String[] tokens = Utils.matchRegex("\\{?(\\d+,\\s*)+(\\d+\\s*)}?", line, 0).
                            split(",\\s*");

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
                String[] tokens = Utils.matchRegex("\\{?(\\d+,\\s*)+(\\d+\\s*)}?", line, 0).
                        split(",\\s*");

                HashSet<Integer> values = new HashSet<>();
                for (String token: tokens) {
                    values.add(Integer.parseInt(token.replaceAll("[{}]", "")));
                }

                variables.put(name, new Variable(name, true, values));
            } else if (line.matches("([a-zA-Z0-9._-]+,?\\s*)+") && line.contains(",")) {
                addTransition(line);
            }
        }
    }

    @Override
    public Coordinate[] getNeighbourhood() {
        return neighbourhood;
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState) {
        int result;
        for (Transition transition: transitions) {
            result = transition.applyTransition(cellState, neighbours);
            if (result != -1) {
                return result;
            }
        }

        // TODO (Make ruletables faster, somehow...)
        return cellState;
    }

    @Override
    public Object clone() {
        return new Ruletable(content);
    }

    /**
     * Adds a variable to the ruletable
     * @param variable The variable to be added
     */
    public void addVariable(Variable variable) {
        variables.put(variable.getName(), variable);
    }

    /**
     * Adds a transition to the ruletable
     * @param transitionLine The string representing the transition
     */
    public void addTransition(String transitionLine) {
        String[] tokens = transitionLine.split(",\\s*");
        ArrayList<String> toPermute = new ArrayList<>(Arrays.asList(tokens).subList(1, tokens.length - 1));

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
            transitions.add(new Transition(numStates, true, transitionLine, variables));
        }
    }

    /**
     * Adds an outer totalistic transition to the ruletable
     * @param transition The transition to add to the ruletable
     * @param input The input state / variable
     * @param output The output state / variable
     * @param var0 The state / variable that represents state 0
     * @param var1 The state / variable that represents state 1
     */
    public void addOTTransition(int transition, String input, String output,
                                String var0, String var1) {
        if (weights == null) {
            StringBuilder builder = new StringBuilder(input + ",");
            for (int i = 0; i < neighbourhood.length; i++) {
                if (i < transition) builder.append(var1);
                else builder.append(var0);
                builder.append(",");
            }

            builder.append(output);
            addTransition(builder.toString());
        } else {
            StringBuilder builder;
            for (String transitionString: transitionMap.get(transition)) {
                builder = new StringBuilder(input + ",");
                for (int i = 0; i < neighbourhood.length; i++) {
                    if (transitionString.charAt(i) == '1') builder.append(var1);
                    else builder.append(var0);

                    builder.append(",");
                }

                builder.append(output);

                //System.out.println(builder + " " + neighbourhood.length);
                addTransition(builder.toString());
            }
        }
    }

    /**
     * Adds an outer totalistic transition with multiple states to the ruletable
     * @param transition The transition to add to the ruletable
     * @param input The input state / variable
     * @param output The output state / variable
     * @param vars The variables that represent each state
     */
    public void addOTTransition(List<Integer> transition, String input, String output,
                                List<String> vars) {
        StringBuilder builder = new StringBuilder(input + ",");

        int sum = 0;
        for (int i = 0; i < transition.size(); i++) {
            sum += transition.get(i);
            builder.append((vars.get(i + 1) + ",").repeat(transition.get(i)));
        }

        builder.append((vars.get(0) + ",").repeat(neighbourhood.length - sum));

        builder.append(output);
        addTransition(builder.toString());
    }

    /**
     * Adds outer totalistic transitions to the ruletable
     * @param transitions The transitions to add to the ruletable
     * @param input The input state / variable
     * @param output The output state / variable
     * @param var0 The state / variable that represents state 0
     * @param var1 The state / variable that represents state 1
     */
    public void addOTTransitions(Iterable<Integer> transitions, String input, String output,
                                String var0, String var1) {
        for (int transition: transitions) addOTTransition(transition, input, output, var0, var1);
    }

    /**
     * Adds isotropic non-totalistic transitions to the ruletable
     * @param transitions The transitions to add to the ruletable
     * @param input The input state / variable
     * @param output The output state / variable
     * @param var0 The state / variable that represents state 0
     * @param var1 The state / variable that represents state 1
     */
    public void addINTTransitions(INTTransitions transitions, String input, String output,
                                  String var0, String var1) {
        StringBuilder builder;
        for (ArrayList<Integer> transition: transitions.getTransitionTable()) {
            builder = new StringBuilder(input + ",");
            for (int number: transition) {
                if (number == 1) builder.append(var1);
                else builder.append(var0);

                builder.append(",");
            }

            builder.append(output);
            addTransition(builder.toString());
        }
    }

    /**
     * Adds isotropic non-totalistic transitions to the ruletable
     * @param transitions1 The 1st transition to add to the ruletable
     * @param transitions2 The 2nd transition to add to the ruletable
     * @param input The input state / variable
     * @param output The output state / variable
     * @param var0 The state / variable that represents state 0
     * @param var1 The state / variable that represents state 1
     * @param var2 The state / variable that represents state 2
     */
    public void addINTTransitions(INTTransitions transitions1, INTTransitions transitions2,
                                  String input, String output, String var0, String var1, String var2) {
        StringBuilder builder;
        for (ArrayList<Integer> transition1: transitions1.getTransitionTable()) {
            for (ArrayList<Integer> transition2: transitions2.getTransitionTable()) {
                // Creating the transition
                ArrayList<Integer> transition = (ArrayList<Integer>) transition1.clone();
                for (int i = 0; i < transition2.size(); i++) {
                    if (transition2.get(i) == 1) {
                        if (transition.get(i) == 0) {
                            transition.set(i, 2);
                        } else {  // Not a possible condition (1 and 2 overlap)
                            transition = null;
                            break;
                        }
                    }
                }

                if (transition == null) continue;  // Move on to the next transition

                // Writing the transition into the ruletable
                builder = new StringBuilder(input + ",");
                for (int number: transition) {
                    if (number == 1) builder.append(var1);
                    else if (number == 2) builder.append(var2);
                    else builder.append(var0);

                    builder.append(",");
                }

                builder.append(output);
                addTransition(builder.toString());
            }
        }
    }

    /**
     * Enables permute symmetry
     */
    public void setPermute() {
        this.permute = true;
        this.symmetry = new Symmetry("");
    }

    /**
     * Is permute symmetry used?
     * @return True if permute symmetry is used, false otherwise
     */
    public boolean isPermute() {
        return permute;
    }

    /**
     * Sets the symmetry of the ruletable
     * @param symmetry The symmetry of the ruletable
     */
    public void setSymmetry(Symmetry symmetry) {
        this.symmetry = symmetry;
    }

    /**
     * Sets the number of states of the ruletable
     * @param numStates The number of states of the ruletable
     */
    public void setNumStates(int numStates) {
        this.numStates = numStates;

        Set<Integer> any = new HashSet<>();
        for (int i = 0; i < numStates; i++) any.add(i);
        ANY = new Variable("any", true, any);

        Set<Integer> any2 = new HashSet<>();
        for (int i = 0; i < numStates; i++) any2.add(i);

        any2.remove(1);
        DEAD = new Variable("dead", true, any2);
    }

    /**
     * Sets the weights of the ruletable
     * @param weights The weights of the ruletable
     */
    public void setWeights(int[] weights) {
        this.weights = weights;
        updateNeighbourhoodSum();
    }

    /**
     * Sets the neighbourhood of the ruletable
     * @param neighbourhood The neighbourhood of the ruletable
     */
    public void setNeighbourhood(Coordinate[] neighbourhood) {
        this.neighbourhood = neighbourhood;
        updateNeighbourhoodSum();
    }

    /**
     * Updates the neighbourhood sum
     */
    public void updateNeighbourhoodSum() {
        if (weights != null) {
            maxNeighbourhoodSum = 0;
            for (int i = 0; i < neighbourhood.length; i++) {
                if (weights[i] > 0) maxNeighbourhoodSum += weights[i];
            }

            transitionMap = new HashMap<>();  // TODO (Add caching)
            String formatSpecifier = "%" + neighbourhood.length + "s";
            for (int i = 0; i < Math.pow(2, neighbourhood.length); i++) {
                String binaryString = Integer.toBinaryString(i);  // Generate the binary string
                String paddedBinaryString = String.format(formatSpecifier, binaryString).replace(' ', '0');

                int sum = 0;  // Getting weight
                for (int j = 0; j < paddedBinaryString.length(); j++) {
                    if (paddedBinaryString.charAt(j) == '1') {
                        sum += weights[j];
                    }
                }

                if (!transitionMap.containsKey(sum)) {
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(paddedBinaryString);  // Initialise array list with a string

                    transitionMap.put(sum, arrayList);
                }
                else {
                    transitionMap.get(sum).add(paddedBinaryString);
                }
            }
        }
        else {
            maxNeighbourhoodSum = neighbourhood.length;
        }
    }

    /**
     * Gets all the variables of the ruletable
     * @return Returns all the variables of the ruletable
     */
    public Map<String, Variable> getVariables() {
        return variables;
    }

    /**
     * Gets all the transitions of the ruletable
     * @return Returns the transitions of the ruletable
     */
    public ArrayList<Transition> getTransitions() {
        return transitions;
    }
}
