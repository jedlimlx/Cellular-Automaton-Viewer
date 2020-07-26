package sample.model;

import java.util.Random;

public class SymmetryGenerator {
    // List of symmetries
    public static String[] symmetries = new String[]{"C1"};

    public static Grid generateSymmetry(String symmetry, int density, int[] states, int x, int y) {
        switch (symmetry) {
            default: return generateC1(density, states, x, y);
        }
    }

    public static Grid generateC1(int density, int[] states, int x, int y) {
        Random random = new Random();  // Random Number Generator
        Grid soup = new Grid();  // Stores the soup
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (random.nextInt(100) < density) {
                    soup.setCell(i, j, states[random.nextInt(states.length)]);
                }
            }
        }

        return soup;
    }
}
