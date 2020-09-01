package sample.model;

import java.util.*;

/**
 * Used to construct an APGTable (ruletable for apgsearch) <br>
 * <br>
 * Example Usage: <br>
 * <pre>
 * Work In Progress...
 * </pre>
 */
public class APGTable {
    /**
     * Stores the variables of the APGTable
     */
    private Map<String, int[]> variables;

    /**
     * Stores which variables are unbounded
     */
    private Set<String> unboundedVariables;

    /**
     * The list of transitions for the APGTable
     */
    private ArrayList<ArrayList<String>> transitions;

    /**
     * The number of states for the APGTable
     */
    private int numStates;

    /**
     * The weights of the APGTable
     */
    private int[] weights;

    /**
     * The background of the APGTable (for strobing rules)
     */
    private int[] background;

    /**
     * Symmetry of the APGTable (none, permute)
     */
    private String symmetry;

    /**
     * The neighbourhood of the APGTable
     */
    private Coordinate[] neighbourhood;

    /**
     * A map that stores the transitions for every weighted rule
     */
    private Map<Integer, ArrayList<String>> weightedTransitions;

    /**
     * The maximum neighbourhood count of the APGTable
     */
    private int maxNeighbourhoodCount;

    /**
     * Initialises the APGTable
     * @param numStates The number of states of the APGTable
     * @param symmetry The symmetry of the APGTable
     * @param neighbourhood The neighbourhood of the APGTable
     */
    public APGTable(int numStates, String symmetry, Coordinate[] neighbourhood) {
        variables = new LinkedHashMap<>();
        unboundedVariables = new HashSet<>();
        transitions = new ArrayList<>();

        weights = null;
        background = new int[]{0};
        
        this.numStates = numStates;
        this.symmetry = symmetry;
        this.neighbourhood = neighbourhood;

        updateMaxNeighbourhoodCount();
    }

    /**
     * Adds a new transition to the APGTable
     * @param inputState The initial state of the cell
     * @param outputState The final state of the cell
     * @param transition The transition of to be inserted
     */
    public void addTransition(int inputState, int outputState, ArrayList<String> transition) {
        ArrayList<String> transitionToAdd = new ArrayList<>();

        transitionToAdd.add(inputState + "");
        transitionToAdd.addAll(transition);
        transitionToAdd.add(outputState + "");

        transitions.add(transitionToAdd);
    }

    /**
     * Adds an outer-totalistic transition.
     * @param inputState The initial state of the cell
     * @param outputState The final state of the cell
     * @param num The outer totalistic number
     * @param variable0 There will be Smax - num of this variable in the final transition
     * @param variable1 There will be num of this variable in the final transition
     */
    public void addOuterTotalisticTransition(int inputState, int outputState, int num, 
                                             String variable0, String variable1) {
        ArrayList<String> transition = new ArrayList<>();

        // Adding in the transitions
        if (weights == null) {
            // Input state at the front
            transition.add(inputState + "");

            for (int i = 0; i < neighbourhood.length; i++) {
                if (i < num) {
                    transition.add(variable1);
                }
                else {
                    transition.add(variable0);
                }
            }

            // Output state at the back
            transition.add(outputState + "");

            // Add transitions to list of transitions
            transitions.add(transition);
        }
        else {
            for (String transitionString: weightedTransitions.get(num)) {
                transition = new ArrayList<>();

                // Input state at the front
                transition.add(inputState + "");

                for (int i = 0; i < transitionString.length(); i++) {
                    if (transitionString.charAt(i) == '0') transition.add(variable0);
                    else transition.add(variable1);
                }

                // Output state at the back
                transition.add(outputState + "");

                // Add transitions to list of transitions
                transitions.add(transition);
            }
        }
    }

    /**
     * Adds an unbounded variable. See https://github.com/GollyGang/ruletablerepository/wiki/UnboundVariables.
     * @param variableName Name of the variable
     * @param value Value of the variable. For example, {0, 1}.
     */
    public void addUnboundedVariable(String variableName, int[] value) {
        unboundedVariables.add(variableName);
        variables.put(variableName, value);
    }

    /**
     * Adds a variable.
     * @param variableName Name of the variable.
     * @param value Value of the variable. For example, {0, 1}.
     */
    public void addVariable(String variableName, int[] value) {
        variables.put(variableName, value);
    }

    /**
     * "Compiles" the APGTable
     * @return Returns the compiled / generated APGTable as a string
     */
    public String compileAPGTable() {
        StringBuilder apgtableString = new StringBuilder();

        // Writing header
        apgtableString.append("# This ruletable is automatically generated by CAViewer.\n");

        String neighbourhoodString = Arrays.toString(neighbourhood);

        apgtableString.append("n_states:").append(numStates * background.length).append("\n");
        apgtableString.append("neighborhood:[(0, 0), ").append(neighbourhoodString, 1,
                neighbourhoodString.length() - 1).append(", (0, 0)]\n");  // Add the (0, 0) at the front and back
        apgtableString.append("symmetries:").append(symmetry).append("\n\n");

        // TODO (B0 rules)
        // Writing in the variables
        for (String variable: variables.keySet()) {
            if (unboundedVariables.contains(variable)) {
                for (int i = 0; i < neighbourhood.length; i++) {
                    apgtableString.append("var ub_").append(variable).append("_").append(i);

                    // Adding in variable values
                    String valuesString = Arrays.toString(variables.get(variable));
                    apgtableString.append(" = {").append(valuesString, 1,
                            valuesString.length() - 1).append("}\n");
                }
            }
            else {
                apgtableString.append("var ").append(variable);

                // Adding in variable values
                String valuesString = Arrays.toString(variables.get(variable));
                apgtableString.append(" = {").append(valuesString, 1, valuesString.length() - 1).append("}\n");
            }
        }

        apgtableString.append("\n");

        // Writing in the transitions
        for (var transition: transitions) {
            int counter = 0;
            for (String cell: transition) {
                if (cell.matches("[0-9]+") || !unboundedVariables.contains(cell)) {
                    apgtableString.append(cell);
                }
                else {  // For unbounded variables (since they are unsupported by Golly)
                    apgtableString.append("ub_").append(cell).append("_").append(counter - 1);
                }

                counter++;
                if (counter < transition.size()) apgtableString.append(",");
            }

            apgtableString.append("\n");  // Don't forget the new line
        }

        return apgtableString.toString();
    }

    /**
     * Sets the background of the APGTable.
     * Used in B0 rules.
     * @param background The background for the APGTable
     */
    public void setBackground(int[] background) {
        this.background = background;
    }

    /**
     * Sets the weights of the APGTable.
     * Used in weighted rules.
     * @param weights The weights for the APGTable
     */
    public void setWeights(int[] weights) {
        if (weights == null) return;
        this.weights = weights;

        updateMaxNeighbourhoodCount();

        weightedTransitions = new Hashtable<>();  // TODO (Add caching)
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

            if (!weightedTransitions.containsKey(sum)) {
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(paddedBinaryString);  // Initialise array list with a string

                weightedTransitions.put(sum, arrayList);
            }
            else {
                weightedTransitions.get(sum).add(paddedBinaryString);
                System.out.println(weightedTransitions.get(sum));
            }
        }
    }

    /**
     * Updates the maximum neighbourhood count
     */
    public void updateMaxNeighbourhoodCount() {
        if (weights != null) {
            maxNeighbourhoodCount = 0;
            for (int i = 0; i < neighbourhood.length; i++) {
                if (weights[i] > 0) maxNeighbourhoodCount += weights[i];
            }
        }
        else {
            maxNeighbourhoodCount = neighbourhood.length;
        }
    }
}
