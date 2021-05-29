package application.model.rules.hrot.history;

import javafx.scene.paint.Color;
import application.model.Coordinate;
import application.model.NeighbourhoodGenerator;
import application.model.rules.ApgtableGeneratable;
import application.model.rules.hrot.HROT;
import application.model.rules.ruleloader.RuleDirective;
import application.model.rules.ruleloader.ruletable.Ruletable;
import application.model.rules.ruleloader.ruletable.Variable;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Represents a HROT History rule.
 */
public class HROTHistory extends HROT implements ApgtableGeneratable {
    /**
     * Creates a HROT History rule with the rule Minibugs History
     */
    public HROTHistory() {
        this("R2,C2,S6-9,B7-8,NMHistory");
    }

    /**
     * Creates a HROT History rule with the specified rulestring
     * @param rulestring The rulestring of the history rule
     */
    public HROTHistory(String rulestring) {
        super(rulestring);

        name = "HROT History";
        numStates = 7;
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the HROT History rule (eg. B3/S23History, R2,C2,S6-9,B7-8,NMHistory)
     * @throws IllegalArgumentException Thrown if an invalid rulestring is passed in
     */
    @Override
    protected void fromRulestring(String rulestring) throws IllegalArgumentException {
        super.fromRulestring(rulestring.replace("History", ""));
    }

    /**
     * Canonises the inputted rulestring with the currently loaded parameters.
     * @param rulestring The rulestring to canonised
     * @return Canonised rulestring
     */
    @Override
    public String canonise(String rulestring) {
        return super.canonise(rulestring.replace("History", "")) + "History";
    }

    /**
     * The regexes that will match a valid rulestring
     * @return An array of regexes that will match a valid rulestring
     */
    @Override
    public String[] getRegex() {
        String[] prevRegex = super.getRegex();
        String[] updatedRegex = new String[prevRegex.length];
        for (int i = 0; i < prevRegex.length; i++) {  // Regex is [R]History
            updatedRegex[i] = prevRegex[i] + "History";
        }

        return updatedRegex;
    }

    /**
     * Returns a plain text description of the 2-state HROT History rule family to be displayed in the Rule Dialog
     * @return Description of the 2-state HROT History rule family
     */
    @Override
    public String getDescription() {
        return "This implements the 2-state Higher Range Outer Totalistic (HROT) History rulespace.\n" +
                "It supports arbitrary neighbourhoods via the CoordCA format (Specify with N@).\n" +
                "It supports arbitrary weighted neighbourhoods via the LV format (Specify with NW).\n" +
                "It supports B0 rules via emulation by alternating rules.\n\n" +
                "The format is as follows:\n" +
                "R<range>,C2,S<survival>,B<birth>,N@<CoordCA>History or\n" +
                "R<range>,C2,S<survival>,B<birth>,NW<Weights>History or\n" +
                "R<range>,C2,S<survival>,B<birth>,N<" + NeighbourhoodGenerator.neighbourhoodSymbols + ">History\n\n" +
                "Examples:\n" +
                "B36/S23History (High Life History)\n" +
                "B2/S34HHistory (Hexagonal Life History)\n" +
                "R2,C2,S6-9,B7-8,NMHistory (Minibugs History)\n" +
                "R2,C2,S2-3,B3,N@891891History (Far Corners Life History)";
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
        ruletable.setNumStates(7);

        ruletable.setNeighbourhood(neighbourhood);
        ruletable.setWeights(weights);

        ruletable.addVariable(Ruletable.ANY);
        ruletable.addVariable(Ruletable.DEAD);
        ruletable.addVariable(Ruletable.LIVE);

        Ruletable.DEAD = new Variable("dead", true, new HashSet<>(Arrays.asList(0, 2, 4)));
        Ruletable.LIVE = new Variable("live", true, new HashSet<>(Arrays.asList(1, 3, 5)));

        // Birth and survival transitions
        ruletable.addOTTransitions(birth, "0", "1", "dead", "live");
        ruletable.addOTTransitions(birth, "2", "1", "dead", "live");
        ruletable.addOTTransitions(birth, "4", "3", "dead", "live");

        ruletable.addOTTransitions(survival, "1", "1", "dead", "live");
        ruletable.addOTTransitions(survival, "3", "3", "dead", "live");
        ruletable.addOTTransitions(survival, "5", "5", "dead", "live");

        // Death transitions
        ruletable.addOTTransition(0, "1", "2", "any", "0");
        ruletable.addOTTransition(0, "3", "4", "any", "0");
        ruletable.addOTTransition(0, "5", "4", "any", "0");

        return new RuleDirective[]{ruletable};
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        if (cellState == 6) return cellState;

        int sum = 0;
        for (int i = 0; i < neighbours.length; i++) {
            if (neighbours[i] == 6 && cellState % 2 == 1) return 2;
            if (neighbours[i] % 2 == 0) continue;

            if (weights != null) {
                sum += weights[i];
            }
            else {
                sum += 1;
            }
        }

        if (cellState == 1) {  // Check Survival
            if (survival.contains(sum)) return cellState;
            else return 2;
        }
        else if (cellState == 3 || cellState == 5) {  // Check Survival
            if (survival.contains(sum)) return cellState;
            else return 4;
        }
        else if (cellState == 0 || cellState == 2) {  // Check Birth
            if (birth.contains(sum)) return 1;
            else return cellState;
        }
        else if (cellState == 4) {  // Check Birth
            if (birth.contains(sum)) return 3;
            else return 4;
        }
        else {
            return 0;
        }
    }

    /**
     * Returns the colour of a cell of the provided state
     * @param state The state of the cell
     * @return The colour of the cell
     */
    @Override
    public Color getColour(int state) {
        if (state == 0) return Color.BLACK;
        else if (state == 1) return Color.rgb(0, 255, 0);
        else if (state == 2) return Color.rgb(0, 0, 128);
        else if (state == 3) return Color.rgb(216, 255, 216);
        else if (state == 4) return Color.rgb(255, 0, 0);
        else if (state == 5) return Color.rgb(255, 255, 0);
        else return Color.rgb(96, 96, 96);
    }

    @Override
    public int dependsOnNeighbours(int state, int generation, Coordinate coordinate) {
        if (state == 6) return 6;
        return super.dependsOnNeighbours(state, generation, coordinate);
    }

    @Override
    public Object clone() {
        HROTHistory newRule = new HROTHistory(rulestring);
        newRule.setWeights(getWeights());
        newRule.setNeighbourhood(getNeighbourhood(0).clone());

        return newRule;
    }
}
