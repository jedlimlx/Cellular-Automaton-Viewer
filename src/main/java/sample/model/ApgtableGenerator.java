package sample.model;

import java.util.ArrayList;
import java.util.Hashtable;

public class ApgtableGenerator {
    public static String generateOT(Coordinate[] neighbourhood, int transition) {
        return generateOT(neighbourhood, transition, 1);
    }

    public static String generateOT(Coordinate[] neighbourhood, int transition,
                                    String alive, String dead,
                                    boolean incrementAlive, boolean incrementDead) {
        StringBuilder transitionString = new StringBuilder();
        for (int i = 0; i < neighbourhood.length; i++) {
            if (i < transition) {
                transitionString.append(alive);
                if (incrementAlive)  // Check if the alive variable should incremented
                    transitionString.append(i);
            }
            else {
                transitionString.append(dead);
                if (incrementDead)  // Check if the dead variable should incremented
                    transitionString.append(i);
            }

            transitionString.append(",");
        }

        return transitionString.toString();
    }

    public static String generateOT(Coordinate[] neighbourhood, int transition, int state) {
        return generateOT(neighbourhood, transition, "" + state, "" + 0,
                false, false);
    }

    public static Hashtable<Integer, ArrayList<String>> generateWeightedTransitions(Coordinate[] neighbourhood, int[] weights) {
        Hashtable<Integer, ArrayList<String>> transitions = new Hashtable<>();  // TODO (Add caching)

        String formatSpecifier = "%" + neighbourhood.length + "s";
        for (int i = 0; i < Math.pow(2, neighbourhood.length); i++) {
            String binaryString = Integer.toBinaryString(i);  // Generate the binary string
            String paddedBinaryString = String.format(formatSpecifier, binaryString).replace(' ', '0');

            int sum = 0;  // Getting weights
            for (int j = 0; j < paddedBinaryString.length(); j++) {
                if (paddedBinaryString.charAt(j) == '1') {
                    sum += weights[j];
                }
            }

            if (!transitions.containsKey(sum)) {
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(paddedBinaryString);  // Initialise array list with a string

                transitions.put(sum, arrayList);
            }
            else {
                transitions.get(sum).add(paddedBinaryString);
            }
        }

        return transitions;
    }
}
