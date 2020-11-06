package sample.model.rules.isotropic.rules.energetic;

import javafx.scene.paint.Color;
import sample.model.Coordinate;
import sample.model.rules.isotropic.rules.INT;
import sample.model.rules.isotropic.transitions.R1MooreINT;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Implements Energetic INT rules where anti-matter and matter annihilate to produce photons
 */
public class INTEnergetic extends INT {
    /**
     * Creates an Energetic INT rule with the rule LifeEnergetic
     */
    public INTEnergetic() {
        this("B3/S23Energetic");
    }

    /**
     * Creates an Energetic INT rule with the specified rulestring
     * @param rulestring The rulestring of the Energetic rule
     */
    public INTEnergetic(String rulestring) {
        super(rulestring);

        name = "Energetic INT";
        numStates = 12;
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the Energetic INT rule (eg. B3/S23Energetic, B2n3/S23-qEnergetic)
     * @throws IllegalArgumentException Thrown if an invalid rulestring is passed in
     */
    @Override
    protected void fromRulestring(String rulestring) throws IllegalArgumentException {
        super.fromRulestring(rulestring.replace("Energetic", ""));

        numStates = 4 + getNeighbourhood().length;
    }

    /**
     * Canonises the inputted rulestring with the currently loaded parameters.
     * @param rulestring The rulestring to canonised
     * @return Canonised rulestring
     */
    @Override
    public String canonise(String rulestring) {
        return super.canonise(rulestring.replace("Energetic", "")) + "Energetic";
    }

    /**
     * The regexes that will match a valid rulestring
     * @return An array of regexes that will match a valid rulestring
     */
    @Override
    public String[] getRegex() {
        String[] prevRegex = super.getRegex();
        String[] updatedRegex = new String[prevRegex.length];
        for (int i = 0; i < prevRegex.length; i++) {  // Regex is [R]Energetic
            updatedRegex[i] = prevRegex[i] + "Energetic";
        }

        return updatedRegex;
    }

    /**
     * Returns a plain text description of the Energetic INT rule family to be displayed in the Rule Dialog
     * @return Description of the Energetic INT rule family
     */
    @Override
    public String getDescription() {
        return "This implements 2-state Isotropic Non-Totalistic (INT) Energetic rules.\n" +
                "In this rulespace, when matter and anti-matter cells collide, " +
                "they form energy cells which explode into 8 photons.\n" +
                "The format is as follows:\n" +
                "B<birth>/S<survival>Energetic";
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        ArrayList<Integer> photons = new ArrayList<>();
        boolean containsMatter = cellState == 1, containsAntimatter = cellState == 2;
        boolean prevEnergy = false;

        int[] binaryNeighbours = new int[neighbours.length];
        int[] binaryNeighbours2 = new int[neighbours.length];
        for (int i = 0; i < neighbours.length; i++) {
            if (neighbours[i] == 1 && !containsMatter) containsMatter = true;
            if (neighbours[i] == 2 && !containsAntimatter) containsAntimatter = true;

            // Only applies to R1 Moore rules
            if (birth instanceof R1MooreINT) {
                // Adjacent energy cells -> photons are born
                if (prevEnergy && neighbours[i] == 3) {
                    if (i % 2 == 1) return i + 3;
                    else return (i + 3) / 2 * 2 + 2;
                }

                // If the previous neighbour was an energy cell
                prevEnergy = neighbours[i] == 3;
            }

            // Matter and antimatter annihilate, creating pure energy (state 3)
            if (containsMatter && containsAntimatter && (cellState == 1 || cellState == 2)) return 3;

            // Photon Transformation
            if (cellState >= 4 && i == getOppositeIndex(cellState - 4) &&
                    (neighbours[i] == 1 || neighbours[i] == 2))
                return neighbours[i];

            // Counting number of photons heading here
            if (neighbours[i] == 3 || (neighbours[i] >= 4 && neighbours[i] - 4 == i))
                photons.add(i);

            // For normal INT transitions
            if (neighbours[i] == 1) binaryNeighbours[i] = 1;
            else if (neighbours[i] == 2) binaryNeighbours2[i] = 1;
            else {
                binaryNeighbours[i] = 0;
                binaryNeighbours2[i] = 0;
            }
        }

        // Only applies to R1 Moore rules
        if (birth instanceof R1MooreINT) {
            // Adjacent energy cells -> photons are born
            if (prevEnergy && neighbours[0] == 3) return 4;
        }

        // Birth & Survival
        if (cellState == 0) {
            boolean birthMatter = birth.checkTransition(binaryNeighbours);
            boolean birthAntimatter = birth.checkTransition(binaryNeighbours2);

            // Energy cell is created when both matter and antimatter are born
            if (birthMatter && birthAntimatter) return 3;
            else if (birthMatter) return 1;
            else if (birthAntimatter) return 2;
        } else if (cellState == 1 && survival.checkTransition(binaryNeighbours)) {
            return cellState;
        } else if (cellState == 2 && survival.checkTransition(binaryNeighbours2)) {
            return cellState;
        }

        // Handles photon movement
        if (photons.size() == 1 && cellState == 0) {
            if (neighbours[photons.get(0)] != 3) return neighbours[photons.get(0)];
            return photons.get(0) + 4;
        } else if (photons.size() == neighbours.length) {
            for (int index: photons) {  // When all photons converge, an energy cell is produced
                if (neighbours[index] == 3) return 0;
            }

            return 3;
        }
        else return 0;
    }

    @Override
    public int dependsOnNeighbours(int state, int generation, Coordinate coordinate) {
        if (state == 3) return 0;
        else return -1;
    }

    @Override
    public Object clone() {
        return new INTEnergetic(rulestring);
    }

    @Override
    public Color getColour(int state) {
        if (state == 0) return Color.rgb(0, 0, 0);
        else if (state == 1) return Color.rgb(255, 0, 128);
        else if (state == 2) return Color.rgb(0, 255, 128);
        else if (state == 3) return Color.rgb(255, 255, 255);
        else return super.getColour(state);
    }

    private int getOppositeIndex(int index) {
        Coordinate target = new Coordinate(-getNeighbourhood()[index].getX(), -getNeighbourhood()[index].getY());
        for (int i = 0; i < getNeighbourhood().length; i++) {
            if (getNeighbourhood()[i].equals(target)) return i;
        }

        return -1;
    }
}
