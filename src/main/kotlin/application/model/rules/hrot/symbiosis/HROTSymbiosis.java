package application.model.rules.hrot.symbiosis;

import application.model.Coordinate;
import application.model.NeighbourhoodGenerator;
import application.model.rules.ApgtableGeneratable;
import application.model.rules.hrot.HROT;
import application.model.rules.ruleloader.RuleDirective;
import application.model.rules.ruleloader.ruletable.Ruletable;

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
    public RuleDirective[] generateApgtable() {
        // Generating the ruletable
        Ruletable ruletable = new Ruletable("");

        if (weights == null) ruletable.setPermute();  // Enable permute symmetry

        ruletable.setNumStates(3);

        ruletable.setNeighbourhood(neighbourhood);
        ruletable.setWeights(weights);

        ruletable.addVariable(Ruletable.ANY);

        // Birth and survival transitions
        ruletable.addOTTransitions(birth, "0", "1", "0", "1");
        ruletable.addOTTransitions(birth, "0", "2", "0", "2");

        ruletable.addOTTransitions(survival, "1", "1", "0", "1");
        ruletable.addOTTransitions(survival, "2", "2", "0", "2");

        // Mututal Stabilisation
        ruletable.addOTTransition(1, "1", "1", "any", "2");
        ruletable.addOTTransition(2, "2", "2", "any", "1");

        // Death transitions
        ruletable.addOTTransition(0, "1", "0", "any", "0");
        ruletable.addOTTransition(0, "2", "0", "any", "0");

        return new RuleDirective[]{ruletable};
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

    @Override
    public Object clone() {
        HROTSymbiosis newRule = new HROTSymbiosis(rulestring);
        newRule.setWeights(getWeights());
        newRule.setNeighbourhood(getNeighbourhood(0).clone());

        return newRule;
    }
}
