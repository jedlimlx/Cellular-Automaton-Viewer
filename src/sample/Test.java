package sample;

import sample.model.NeighbourhoodGenerator;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        System.out.println(NeighbourhoodGenerator.generateHexagonal(2).length);
        System.out.println(Arrays.toString(NeighbourhoodGenerator.generateHexagonal(2)));
    }
}
