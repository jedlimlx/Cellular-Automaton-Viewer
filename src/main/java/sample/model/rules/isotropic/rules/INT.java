package sample.model.rules.isotropic.rules;

import sample.model.APGTable;
import sample.model.Coordinate;
import sample.model.Utils;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.isotropic.transitions.INTTransitions;

import java.util.ArrayList;

/**
 * Represents 2-state isotropic non-totalistic (INT) rules
 */
public class INT extends BaseINT implements ApgtableGeneratable {
    /**
     * The birth transitions of the INT rule
     */
    private INTTransitions birth;

    /**
     * The survival transitions of the INT rule
     */
    private INTTransitions survival;

    private static String[] transitionRegexes;
    private static String[] regexes;

    /**
     * Constructs an INT rule with the rule LeapLife
     */
    public INT() {
        this("B2n3/S23-q");
    }

    /**
     * Creates a 2-state INT rule with the given rulestring
     * @param rulestring The rulestring of the 2-state INT rule to be created
     * @throws IllegalArgumentException Thrown if the rulestring is invalid
     */
    public INT(String rulestring) {
        super();

        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        name = "INT";

        // Generating regexes
        regexes = new String[neighbourhoodLookup.size()];
        transitionRegexes = new String[neighbourhoodLookup.size()];

        int counter = 0;
        for (String string: neighbourhoodLookup.keySet()) {
            transitionRegexes[counter] = "(" + neighbourhoodLookup.get(string).getRegex() + ")*";
            regexes[counter] = "B" + transitionRegexes[counter] + "/S" + transitionRegexes[counter] +
                    "(/N" + string + ")?";
            counter++;
        }

        // Load rulestring
        setRulestring(rulestring);
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the INT rule (eg. B2n3/S23-q, B3/S23/NK)
     * @throws IllegalArgumentException Thrown if an invalid rulestring is passed in
     */
    @Override
    protected void fromRulestring(String rulestring) throws IllegalArgumentException {
        boolean matched = false;
        for (int i = 0; i < regexes.length; i++) {
            if (rulestring.matches(regexes[i])) {
                birth = getINTTransition(rulestring);
                birth.setTransitionString(Utils.matchRegex("B(" + transitionRegexes[i] + ")",
                        rulestring, 0, 1));

                survival = getINTTransition(rulestring);
                survival.setTransitionString(Utils.matchRegex("S(" + transitionRegexes[i] + ")",
                        rulestring, 0, 1));

                matched = true;
            }
        }

        if (matched) updateBackground();
        else throw new IllegalArgumentException("This rulestring is invalid!");
    }

    /**
     * Canonises the inputted rulestring with the currently loaded parameters.
     * @param rulestring The rulestring to canonised
     * @return Canonised rulestring
     */
    @Override
    public String canonise(String rulestring) {
        return rulestring;
    }

    /**
     * The regexes that will match a valid rulestring
     * @return An array of regexes that will match a valid rulestring
     */
    @Override
    public String[] getRegex() {
        return regexes;
    }

    /**
     * Returns a plain text description of the 2-state INT rule family to be displayed in the Rule Dialog
     * @return Description of the 2-state INT rule family
     */
    @Override
    public String getDescription() {
        return "This implements 2-state Isotropic Non-Totalistic (INT) rules.\n" +
                "B0 rules are supported via emulation with alternating rules.\n\n" +
                "The format is as follows:\n" +
                "B<birth>/S<survival>/N<neighbourhood>";
    }

    /**
     * Generates an apgtable for apgsearch to use
     * @return True if the operation was successful, false otherwise
     */
    @Override
    public APGTable generateApgtable() {
        APGTable apgTable = new APGTable(numStates, "none", getNeighbourhood());

        apgTable.addUnboundedVariable("death", new int[]{0, 1});

        for (ArrayList<Integer> transition: birth.getTransitionTable()) {
            ArrayList<String> stringTransition = new ArrayList<>();
            for (int state: transition) {
                stringTransition.add(state + "");
            }

            apgTable.addTransition(0, 1, stringTransition);
        }

        for (ArrayList<Integer> transition: survival.getTransitionTable()) {
            ArrayList<String> stringTransition = new ArrayList<>();
            for (int state: transition) {
                stringTransition.add(state + "");
            }

            apgTable.addTransition(1, 1, stringTransition);
        }

        apgTable.addOuterTotalisticTransition(1, 0, 0, "death", "1");

        return apgTable;
    }

    /**
     * Sets the birth conditions of the INT rule
     * @param birth The birth conditions
     */
    public void setBirth(INTTransitions birth) {
        this.birth = birth;
    }

    /**
     * Sets the survival conditions of the INT rule
     * @param survival The survival conditions
     */
    public void setSurvival(INTTransitions survival) {
        this.survival = survival;
    }

    /**
     * Gets the birth conditions of the INT rule
     * @return The birth conditions
     */
    public INTTransitions getBirth() {
        return birth;
    }

    /**
     * Gets the survival conditions of the INT rule
     * @return The survival conditions
     */
    public INTTransitions getSurvival() {
        return survival;
    }

    @Override
    public Object clone() {
        return new INT(rulestring);
    }

    @Override
    public Coordinate[] getNeighbourhood(int generation) {
        return birth.getNeighbourhood();
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        if (cellState == 0 && birth.checkTransition(neighbours)) {
            return 1;
        }
        else if (cellState == 1 && survival.checkTransition(neighbours)) {
            return 1;
        }

        return 0;
    }
}
