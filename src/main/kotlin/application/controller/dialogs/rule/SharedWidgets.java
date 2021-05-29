package application.controller.dialogs.rule;

import application.controller.NeighbourhoodSelector;

public class SharedWidgets {
    private static final NeighbourhoodSelector neighbourhoodSelector = new NeighbourhoodSelector(2);

    public static NeighbourhoodSelector getNeighbourhoodSelector() {
        NeighbourhoodSelector newNeighbourhoodSelector = new NeighbourhoodSelector(2);

        // Bidirectional Binding
        neighbourhoodSelector.getSpinnerRange().valueProperty().addListener((obs, oldValue, newValue) ->
                newNeighbourhoodSelector.getSpinnerRange().getValueFactory().setValue(newValue));
        newNeighbourhoodSelector.getSpinnerRange().valueProperty().addListener((obs, oldValue, newValue) ->
                neighbourhoodSelector.getSpinnerRange().getValueFactory().setValue(newValue));

        //neighbourhoodSelector.setOnWeightsChanged(() ->
        //        newNeighbourhoodSelector.setWeights(neighbourhoodSelector.getRawWeights()));
        //newNeighbourhoodSelector.setOnWeightsChanged(() ->
        //        neighbourhoodSelector.setWeights(newNeighbourhoodSelector.getRawWeights()));

        return newNeighbourhoodSelector;
    }
}
