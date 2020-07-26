package sample.model;

public class ApgtableGenerator {
    public static String generateOT(Coordinate[] neighbourhood, int transition) {
        return generateOT(neighbourhood, transition, 1);
    }

    public static String generateOT(Coordinate[] neighbourhood, int transition, int state) {
        StringBuilder transitionString = new StringBuilder();
        for (int i = 0; i < neighbourhood.length; i++) {
            if (i < transition) {
                transitionString.append(state).append(",");
            }
            else {
                transitionString.append("0,");
            }
        }

        return transitionString.toString();
    }
}
