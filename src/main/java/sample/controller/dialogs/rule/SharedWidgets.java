package sample.controller.dialogs.rule;

import sample.controller.NeighbourhoodSelector;

public class SharedWidgets {
    private static final NeighbourhoodSelector neighbourhoodSelector = new NeighbourhoodSelector(2);

    public static NeighbourhoodSelector getNeighbourhoodSelector() {
        NeighbourhoodSelector newNeighbourhoodSelector = new NeighbourhoodSelector(2);

        // Bidirectional Binding
        neighbourhoodSelector.getSpinnerRange().valueProperty().addListener((obs, oldValue, newValue) ->
                newNeighbourhoodSelector.getSpinnerRange().getValueFactory().setValue(newValue));
        newNeighbourhoodSelector.getSpinnerRange().valueProperty().addListener((obs, oldValue, newValue) ->
                neighbourhoodSelector.getSpinnerRange().getValueFactory().setValue(newValue));

        neighbourhoodSelector.setOnWeightsChanged(() -> {
            try {
                newNeighbourhoodSelector.setWeights(neighbourhoodSelector.getRawWeights());
            } catch (StackOverflowError ignored) {}  // Recursive stuff going on, don't ask questions
        });
        newNeighbourhoodSelector.setOnWeightsChanged(() -> {
            try {
                neighbourhoodSelector.setWeights(newNeighbourhoodSelector.getRawWeights());
            } catch (StackOverflowError ignored) {}  // Recursive stuff going on, don't ask questions
        });

        return newNeighbourhoodSelector;
    }
}
