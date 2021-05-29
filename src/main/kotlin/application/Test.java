package application;

import application.model.Coordinate;
import application.model.simulation.bounds.Torus;

public class Test {
    public static void main(String[] args) {
        Torus torus = new Torus("T3", new Coordinate(1, 1));
        System.out.println(torus.map(new Coordinate(0, 4)) + " " + torus.map(new Coordinate(1, 0)) + " " +
                torus.map(new Coordinate(-1, -1)) + " " + torus.map(new Coordinate(5, 4)));
    }
}
