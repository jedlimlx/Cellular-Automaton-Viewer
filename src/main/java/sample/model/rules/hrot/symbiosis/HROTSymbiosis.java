package sample.model.rules.hrot.symbiosis;

import sample.model.APGTable;
import sample.model.Coordinate;
import sample.model.NeighbourhoodGenerator;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.hrot.HROT;

/**
 * Represents a HROT Symbiosis rule where opposite states mutually stablise each other.
 */
public class HROTSymbiosis extends HROT implements ApgtableGeneratable {
    /**
     * Creates a HROT Symbiosis rule with the rule Minibugs Symbiosis
     */
    public HROTSymbiosis() {
        this("R2,C2,S6-9,B7-8,NMSymbiosis");
    }

    /**
     * Creates a HROT Symbiosis rule with the specified rulestring
     * @param rulestring The rulestring of the symbiosis rule
     */
    public HROTSymbiosis(String rulestring) {
        super(rulestring);

        name = "HROT Symbiosis";
        numStates = 3;
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the HROT Symbiosis rule (eg. B3/S23Symbiosis, R2,C2,S6-9,B7-8,NMSymbiosis)
     * @throws IllegalArgumentException Thrown if an invalid rulestring is passed in
     */
    @Override
    protected void fromRulestring(String rulestring) throws IllegalArgumentException {
        super.fromRulestring(rulestring.replace("Symbiosis", ""));

        if (birth.contains(0)) throw new IllegalArgumentException("Symbiosis rules do not support B0!");
    }

    /**
     * Canonises the inputted rulestring with the currently loaded parameters.
     * @param rulestring The rulestring to canonised
     * @return Canonised rulestring
     */
    @Override
    public String canonise(String rulestring) {
        return super.canonise(rulestring.replace("Symbiosis", "")) + "Symbiosis";
    }

    /**
     * The regexes that will match a valid rulestring
     * @return An array of regexes that will match a valid rulestring
     */
    @Override
    public String[] getRegex() {
        String[] prevRegex = super.getRegex();
        String[] updatedRegex = new String[prevRegex.length];
        for (int i = 0; i < prevRegex.length; i++) {  // Regex is [R]Symbiosis
            updatedRegex[i] = prevRegex[i] + "Symbiosis";
        }

        return updatedRegex;
    }

    /**
     * Returns a plain text description of the HROT Symbiosis rule family to be displayed in the Rule Dialog
     * @return Description of the HROT Symbiosis rule family
     */
    @Override
    public String getDescription() {
        return "This implements the Higher Range Outer Totalistic (HROT) Symbosis rulespace.\n" +
                "It supports arbitrary neighbourhoods via the CoordCA format (Specify with N@).\n" +
                "It supports arbitrary weighted neighbourhoods via the LV format (Specify with NW).\n" +
                "It supports B0 rules via emulation by alternating rules.\n\n" +
                "The format is as follows:\n" +
                "R<range>,C2,S<survival>,B<birth>,N@<CoordCA>Symbiosis or\n" +
                "R<range>,C2,S<survival>,B<birth>,NW<Weights>Symbiosis or\n" +
                "R<range>,C2,S<survival>,B<birth>,N<" + NeighbourhoodGenerator.neighbourhoodSymbols + ">Symbiosis\n\n" +
                "Examples:\n" +
                "B36/S23Symbiosis (High Life Symbiosis)\n" +
                "B2/S34HSymbiosis (Hexagonal Life Symbiosis)\n" +
                "R2,C2,S6-9,B7-8,NMSymbiosis (Minibugs Symbiosis)\n" +
                "R2,C2,S2-3,B3,N@891891Symbiosis (Far Corners Life Symbiosis)";
    }

    /**
     * Generates an apgtable for apgsearch to use
     * @return True if the operation was successful, false otherwise
     */
    @Override
    public APGTable generateApgtable() {
        // Generating the APGTable
        APGTable apgTable = new APGTable(numStates, weights == null ? "permute" : "none", neighbourhood);
        apgTable.setWeights(weights);
        apgTable.setBackground(background);

        // Death Variables
        apgTable.addUnboundedVariable("any", new int[]{0, 1, 2});

        // Transitions
        for (int transition: birth) {
            apgTable.addOuterTotalisticTransition(0, 1, transition,
                    "0", "1");
            apgTable.addOuterTotalisticTransition(0, 2, transition,
                    "0", "2");
        }

        for (int transition: survival) {
            apgTable.addOuterTotalisticTransition(1, 1, transition,
                    "0", "1");
            apgTable.addOuterTotalisticTransition(2, 2, transition,
                    "0", "2");
        }

        // Mutual Stabilisation
        apgTable.addOuterTotalisticTransition(1, 1, maxNeighbourhoodCount - 1,
                "2", "any");
        apgTable.addOuterTotalisticTransition(2, 2, maxNeighbourhoodCount - 1,
                "1", "any");

        // Death
        apgTable.addOuterTotalisticTransition(1, 0, maxNeighbourhoodCount,
                "0", "any");
        apgTable.addOuterTotalisticTransition(2, 0, maxNeighbourhoodCount,
                "0", "any");

        return apgTable;
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        int sum = 0;

        boolean containsOne = false, containsTwo = false;
        for (int i = 0; i < neighbours.length; i++) {
            if (neighbours[i] == 0) continue;

            // Mutual Stabilisation
            if (neighbours[i] == 1) {
                containsOne = true;
                if (cellState == 2) return 2;
            }
            else if (neighbours[i] == 2) {
                containsTwo = true;
                if (cellState == 1) return 1;
            }

            if (weights != null) {
                sum += weights[i];
            }
            else {
                sum += 1;
            }
        }

        if (cellState > 0 && survival.contains(sum)) {  // Check Survival
            return cellState;
        }
        else if (cellState == 0 && birth.contains(sum)) {  // Check Birth
            if (containsOne && containsTwo) return 0;
            else if (containsOne) return 1;
            else if (containsTwo) return 2;
        }

        return 0;
    }
}
