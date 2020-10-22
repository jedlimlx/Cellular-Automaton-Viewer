package sample.model.rules.ruleloader.ruletable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a ruletable transition
 */
public class Transition {
    /**
     * The values (or state literals) of the transition
     */
    private final Map<Integer, Integer> values;

    /**
     * The variables of the transition
     */
    private final Map<Integer, Variable> variables;

    /**
     * Stores the information about the bound variable values when checking transitions
     */
    private Map<String, Integer> boundVariableValue;

    /**
     * Is the transition on permute symmetry
     */
    private final boolean permute;

    /**
     * Used in permute symmetry
     */
    private final int[][] cellNumRange;

    /**
     * Number of states of the ruletable using the transition
     */
    private final int numStates;

    /**
     * Constructs a transition
     * @param numStates The number of states of the ruletable
     * @param permute Is there permute symmetry?
     * @param transition The string representing the transition
     * @param variables The variables representing the transition
     */
    public Transition(int numStates, boolean permute, String transition, Map<String, Variable> variables) {
        this.numStates = numStates;
        this.permute = permute;

        this.values = new HashMap<>();
        this.variables = new HashMap<>();

        int index = 0;
        cellNumRange = new int[numStates][2];

        String[] tokens = transition.split(",\\s*");
        for (String value: tokens) {
            if (value.matches("\\d+")) {
                if (index != 0 && index != tokens.length - 1) {
                    cellNumRange[Integer.parseInt(value)][0]++;
                    cellNumRange[Integer.parseInt(value)][1]++;
                }

                this.values.put(index, Integer.parseInt(value));
            }
            else {
                if (index != 0 && index != tokens.length - 1) {
                    for (int i : variables.get(value).getValues()) {
                        cellNumRange[i][1]++;
                    }
                }
                this.variables.put(index, variables.get(value));
            }

            index++;
        }
    }

    /**
     * Applies the transition to get the output state
     * @param cellState The state of the cell
     * @param neighbours The neighbours of the cell
     * @return Returns -1 if the transition does not match, returns the cell's next state if it does
     */
    public int applyTransition(int cellState, int[] neighbours) {
        boundVariableValue = new HashMap<>();

        if (!checkIndex(cellState, 0)) return -1;
        if (permute) {
            int[] numStatesArray = new int[numStates];
            for (int neighbour: neighbours) {
                numStatesArray[neighbour]++;
            }

            for (int i = 0; i < numStates; i++) {
                if (numStatesArray[i] > cellNumRange[i][1]) return -1;
                if (numStatesArray[i] < cellNumRange[i][0]) return -1;
            }
        }
        else {
            for (int i = 1; i < neighbours.length + 1; i++) {
                if (!checkIndex(neighbours[i - 1], i)) return -1;
            }
        }

        //System.out.println(values + " " + variables);
        if (values.get(neighbours.length + 1) != null) return values.get(neighbours.length + 1);
        else return boundVariableValue.get(variables.get(neighbours.length + 1).getName());
    }

    private boolean checkIndex(int cellState, int index) {
        if (values.get(index) != null) {
            return values.get(index) == cellState;
        }
        else {
            Variable var = variables.get(index);

            if (var.getValues().contains(cellState)) {
                if (!var.isUnbounded()) {
                    if (boundVariableValue.get(var.getName()) != null &&
                            boundVariableValue.get(var.getName()) != cellState) {
                        return false;
                    }

                    boundVariableValue.putIfAbsent(var.getName(), cellState);
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public Map<Integer, Integer> getValues() {
        return values;
    }

    public Map<Integer, Variable> getVariables() {
        return variables;
    }
}
