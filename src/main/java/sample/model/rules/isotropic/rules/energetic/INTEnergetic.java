package sample.model.rules.isotropic.rules.energetic;

import javafx.scene.paint.Color;
import sample.model.Coordinate;
import sample.model.Utils;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.isotropic.rules.INT;
import sample.model.rules.isotropic.transitions.R1MooreINT;
import sample.model.rules.ruleloader.RuleDirective;
import sample.model.rules.ruleloader.ruletable.Ruletable;
import sample.model.rules.ruleloader.ruletable.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Implements Energetic INT rules where anti-matter and matter annihilate to produce photons
 */
public class INTEnergetic extends INT implements ApgtableGeneratable {
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
        super.fromRulestring(rulestring.replaceAll("E(x)?nergetic", ""));

        numStates = 4 + getNeighbourhood().length;
    }

    /**
     * Canonises the inputted rulestring with the currently loaded parameters.
     * @param rulestring The rulestring to canonised
     * @return Canonised rulestring
     */
    @Override
    public String canonise(String rulestring) {
        return super.canonise(rulestring.replaceAll("E(x)?nergetic", "")) +
                Utils.matchRegex("E(x)?nergetic", rulestring, 0);
    }

    /**
     * The regexes that will match a valid rulestring
     * @return An array of regexes that will match a valid rulestring
     */
    @Override
    public String[] getRegex() {
        String[] prevRegex = super.getRegex();
        String[] updatedRegex = new String[prevRegex.length];
        for (int i = 0; i < prevRegex.length; i++) {  // Regex is [R][Anti]E[x]nergetic
            updatedRegex[i] = prevRegex[i] + "E(x)?nergetic";
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
                "B<birth>/S<survival>Energetic\n" +
                "B<birth>/S<survival>Exnergetic\n";
    }

    /**
     * Generates an apgtable for apgsearch to use
     * @return True if the operation was successful, false otherwise
     */
    @Override
    public RuleDirective[] generateApgtable() {
        // Generating the ruletable
        Ruletable ruletable = new Ruletable("");
        ruletable.setNumStates(numStates);
        ruletable.setNeighbourhood(birth.getNeighbourhood());

        ruletable.addVariable(Ruletable.ANY);

        // Variable for live states
        Ruletable.LIVE = new Variable("live", true, new HashSet<>(Arrays.asList(1, 2)));
        ruletable.addVariable(Ruletable.LIVE);

        // Variable for dead states
        HashSet<Integer> dead = new HashSet<>();
        dead.add(0);
        for (int i = 3; i < numStates; i++) dead.add(i);

        Ruletable.DEAD = new Variable("dead", true, dead);
        ruletable.addVariable(Ruletable.DEAD);

        // Variable for states that are not matter
        HashSet<Integer> notMatter = new HashSet<>();
        for (int i = 0; i < numStates; i++) notMatter.add(i);
        notMatter.remove(1);

        ruletable.addVariable(new Variable("not_matter", true, notMatter));

        // Variable for states that are not antimatter
        HashSet<Integer> notAntimatter = new HashSet<>();
        for (int i = 0; i < numStates; i++) notAntimatter.add(i);
        notAntimatter.remove(2);

        ruletable.addVariable(new Variable("not_antimatter", true, notAntimatter));

        // Variables for photons
        for (int i = 4; i < numStates; i++) {
            // Variable for states that are not photon i
            HashSet<Integer> notPhoton = new HashSet<>();
            for (int j = 0; j < numStates; j++) notPhoton.add(j);
            notPhoton.remove(i);
            notPhoton.remove(3);

            ruletable.addVariable(new Variable("not_p" + i, true, notPhoton));

            // Variable for energy + photon i
            ruletable.addVariable(new Variable("e" + i, true, new HashSet<>(Arrays.asList(3, i))));
        }

        // Energy Particle Creation (Both birth conditions fulfilled)
        ruletable.addINTTransitions(birth, birth, "0", "3", "dead", "1", "2");

        // Energy Particle Creation (Matter & Anti-matter annihilate)
        StringBuilder builder;
        for (int i = 0; i < getNeighbourhood().length; i++) {
            // Matter -> Energy
            builder = new StringBuilder("1,");
            for (int j = 0; j < getNeighbourhood().length; j++) {
                builder.append(j == i ? "2," : "any,");
            }
            builder.append("3");
            ruletable.addTransition(builder.toString());

            // Anti-matter -> Energy
            builder = new StringBuilder("2,");
            for (int j = 0; j < getNeighbourhood().length; j++) {
                builder.append(j == i ? "1," : "any,");
            }
            builder.append("3");
            ruletable.addTransition(builder.toString());
        }

        // Birth and survival transitions
        ruletable.addINTTransitions(birth, "0", "1", "not_matter", "1");
        ruletable.addINTTransitions(survival, "1", "1", "not_matter", "1");

        ruletable.addINTTransitions(birth, "0", "2", "not_antimatter", "2");
        ruletable.addINTTransitions(survival, "2", "2", "not_antimatter", "2");

        // All Photons -> Energy Cell
        builder = new StringBuilder("0,");
        for (int i = 0; i < getNeighbourhood().length; i++) {
            builder.append(getOppositeIndex(i) + 4).append(",");
        }

        builder.append("3");
        ruletable.addTransition(builder.toString());

        if (birth instanceof R1MooreINT) {  // 2 Energy Cells -> Photons
            // Variable for states that are not energy cells
            HashSet<Integer> notEnergy = new HashSet<>();
            for (int i = 0; i < numStates; i++) notEnergy.add(i);
            notEnergy.remove(3);

            ruletable.addVariable(new Variable("not_energy", true, notEnergy));

            for (int i = 0; i < getNeighbourhood().length; i++) {
                builder = new StringBuilder("0,");
                for (int j = 0; j < getNeighbourhood().length; j++) {
                    if (i == j) builder.append("3,");
                    else if (i == (j + 1) % getNeighbourhood().length) builder.append("3,");
                    else builder.append("not_energy,");
                }

                int photonIndex = getOppositeIndex(i);
                builder.append(photonIndex % 2 == 1 ? photonIndex + 3 : (photonIndex + 3) / 2 * 2 + 2);
                ruletable.addTransition(builder.toString());
            }
        }

        // Photon Creation and Movement
        for (int i = 0; i < getNeighbourhood().length; i++) {
            builder = new StringBuilder("0,");
            for (int j = 0; j < getNeighbourhood().length; j++) {
                builder.append(j == i ? "e" + (getOppositeIndex(j) + 4) + "," :
                        "not_p" + (getOppositeIndex(j) + 4) + ",");
            }

            builder.append(getOppositeIndex(i) + 4);
            ruletable.addTransition(builder.toString());
        }

        // Photon Conversion
        if (rulestring.contains("Exnergetic")) {  // [R]Exnergetic
            for (int i = 0; i < getNeighbourhood().length; i++) {
                // Collision with Matter -> Antimatter
                builder = new StringBuilder((i + 4) + ",");
                for (int j = 0; j < getNeighbourhood().length; j++) {
                    builder.append(j == i ? "1," : "any,");
                }
                builder.append("2");
                ruletable.addTransition(builder.toString());

                // Collision with Antimatter -> Matter
                builder = new StringBuilder((i + 4) + ",");
                for (int j = 0; j < getNeighbourhood().length; j++) {
                    builder.append(j == i ? "2," : "any,");
                }
                builder.append("1");
                ruletable.addTransition(builder.toString());
            }
        } else {  // [R]Energetic
            for (int i = 0; i < getNeighbourhood().length; i++) {
                // Collision with Matter -> Matter
                builder = new StringBuilder((i + 4) + ",");
                for (int j = 0; j < getNeighbourhood().length; j++) {
                    builder.append(j == i ? "1," : "any,");
                }
                builder.append("1");
                ruletable.addTransition(builder.toString());

                // Collision with Antimatter -> Antimatter
                builder = new StringBuilder((i + 4) + ",");
                for (int j = 0; j < getNeighbourhood().length; j++) {
                    builder.append(j == i ? "2," : "any,");
                }
                builder.append("2");
                ruletable.addTransition(builder.toString());
            }
        }

        // Death transitions
        for (int i = 1; i < numStates; i++)
            ruletable.addOTTransition(0, i + "", "0", "any", "0");

        return new RuleDirective[]{ruletable};
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        ArrayList<Integer> photons = new ArrayList<>();
        boolean containsMatter = cellState == 1, containsAntimatter = cellState == 2;
        boolean prevEnergy = false;

        int numEnergy = 0;
        if (birth instanceof R1MooreINT) {
            for (int neighbour: neighbours) {
                if (neighbour == 3) numEnergy++;
            }
        }

        int[] binaryNeighbours = new int[neighbours.length];
        int[] binaryNeighbours2 = new int[neighbours.length];
        for (int i = 0; i < neighbours.length; i++) {
            if (neighbours[i] == 1 && !containsMatter) containsMatter = true;
            if (neighbours[i] == 2 && !containsAntimatter) containsAntimatter = true;

            // Only applies to R1 Moore rules
            if (birth instanceof R1MooreINT && cellState == 0) {
                // Adjacent energy cells -> photons are born
                if (prevEnergy && neighbours[i] == 3 && numEnergy == 2) {
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
                    (neighbours[i] == 1 || neighbours[i] == 2)) {
                if (rulestring.contains("Exnergetic")) return neighbours[i] == 1 ? 2 : 1;
                else return neighbours[i];
            }

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
        if (birth instanceof R1MooreINT && cellState == 0 && numEnergy == 2) {
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
