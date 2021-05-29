package application.controller;

import javafx.scene.layout.GridPane;

/**
 * A widget that selects state weights for a rule.
 */
public class StateWeightsSelector extends GridPane {
    /**
     * The number of states of the state weight selector.
     */
    private final int numStates;

    /**
     * Constructs a state weight selector with the specified number of states
     * @param numStates The number of states of the state weight selector
     */
    public StateWeightsSelector(int numStates) {
        super();

        this.numStates = numStates;
    }
}
