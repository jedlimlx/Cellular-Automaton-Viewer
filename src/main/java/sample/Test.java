package sample;

import sample.model.ApgtableGenerator;
import sample.model.NeighbourhoodGenerator;

public class Test {
    public static void main(String[] args) {
        ApgtableGenerator.generateWeightedTransitions(
                NeighbourhoodGenerator.generateGaussianNeighbourhood(2),
                NeighbourhoodGenerator.generateGaussian(2));
    }
}
