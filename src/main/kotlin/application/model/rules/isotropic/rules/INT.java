package application.model.rules.isotropic.rules;

import org.javatuples.Pair;
import application.model.Coordinate;
import application.model.Utils;
import application.model.rules.ApgtableGeneratable;
import application.model.rules.MinMaxRuleable;
import application.model.rules.RuleFamily;
import application.model.rules.isotropic.transitions.INTTransitions;
import application.model.rules.isotropic.transitions.R1MooreINT;
import application.model.rules.ruleloader.RuleDirective;
import application.model.rules.ruleloader.ruletable.Ruletable;
import application.model.simulation.Grid;

import java.util.ArrayList;
import java.util.Map;

/**
 * Implements 2-state isotropic non-totalistic (INT) rules
 */
public class INT extends BaseINT implements ApgtableGeneratable, MinMaxRuleable {
    /**
     * The birth transitions of the INT rule
     */
    protected INTTransitions birth;

    /**
     * The survival transitions of the INT rule
     */
    protected INTTransitions survival;

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
            if (string.equals("M")) {
                regexes[counter] = "[BbSs]" + transitionRegexes[counter] + "[_/]?[BbSs]" + transitionRegexes[counter];
            } else {
                regexes[counter] = "[BbSs]" + transitionRegexes[counter] + "[_/]?[BbSs]" + transitionRegexes[counter] +
                        "[_/]?N?" + string;
            }
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
                birth.setTransitionString(Utils.matchRegex("[Bb](" + transitionRegexes[i] + ")",
                        rulestring, 0, 1));

                survival = getINTTransition(rulestring);
                survival.setTransitionString(Utils.matchRegex("[Ss](" + transitionRegexes[i] + ")",
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
        if (!neighbourhoodString.equals(""))
            return "B" + birth.canoniseTransitions() + "/S" + survival.canoniseTransitions() +
                    "/N" + neighbourhoodString;
        else
            return "B" + birth.canoniseTransitions() + "/S" + survival.canoniseTransitions();
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
    public RuleDirective[] generateApgtable() {
        // Generating the ruletable
        Ruletable ruletable = new Ruletable("");
        ruletable.setNumStates(2);

        ruletable.setNeighbourhood(birth.getNeighbourhood());

        ruletable.addVariable(Ruletable.ANY);

        // Birth and survival transitions
        ruletable.addINTTransitions(birth, "0", "1", "0", "1");
        ruletable.addINTTransitions(survival, "1", "1", "0", "1");

        // Death transitions
        ruletable.addOTTransition(0, "1", "0", "any", "0");

        return new RuleDirective[]{ruletable};
    }

    /**
     * Randomise the parameters of the current rule to be between minimum and maximum rules
     * Used in CAViewer's rule search program
     * @param minRule The minimum rule for randomisation
     * @param maxRule The maximum rule for randomisation
     * @throws IllegalArgumentException Thrown if the minimum and maximum rules are invalid
     */
    @Override
    public void randomise(RuleFamily minRule, RuleFamily maxRule) {
        if (validMinMax(minRule, maxRule)) {
            setBirth(INTTransitions.randomise(((INT) minRule).getBirth(), ((INT) maxRule).getBirth()));
            setSurvival(INTTransitions.randomise(((INT) minRule).getSurvival(), ((INT) maxRule).getSurvival()));
        } else {
            throw new IllegalArgumentException("Invalid minimum and maximum rules!");
        }
    }

    /**
     * Returns the minimum and maximum rule of the provided evolutionary sequence
     * @param grids An array of grids representing the evolutionary sequence
     * @return A pair containing the min rule as the first value and the max rule as the second value
     */
    @Override
    public Pair<RuleFamily, RuleFamily> getMinMaxRule(Grid[] grids) {
        // Getting min and max transitions
        INTTransitions minBirth = birth.getMinTransition(), minSurvival = survival.getMinTransition();
        INTTransitions maxBirth = birth.getMaxTransition(), maxSurvival = survival.getMaxTransition();

        // Running through every generation and check what transitions are required
        for (int[] neighbours: getNeighbourList(grids)) {
            // Determining the required birth / survival condition
            int currentCell = neighbours[0];
            int nextCell = neighbours[neighbours.length - 1];

            ArrayList<Integer> cellNeighbours = new ArrayList<>();
            for (int i = 1; i < neighbours.length - 1; i++)
                cellNeighbours.add(neighbours[i]);

            if (currentCell == 0 && nextCell == 1) {  // Birth (0 -> 1)
                minBirth.addTransition(cellNeighbours);
            }
            else if (currentCell == 0 && nextCell == 0) {  // No Birth (0 -> 0)
                maxBirth.removeTransition(cellNeighbours);
            }
            else if (currentCell == 1 && nextCell == 1) {  // Survival (1 -> 1)
                minSurvival.addTransition(cellNeighbours);
            }
            else if (currentCell == 1 && nextCell == 0) {  // No Survival (1 -> 0)
                maxSurvival.removeTransition(cellNeighbours);
            }
        }

        // Creating the min and max rules
        INT minRule = (INT) this.clone();
        minRule.setBirth(minBirth);
        minRule.setSurvival(minSurvival);

        INT maxRule = (INT) this.clone();
        maxRule.setBirth(maxBirth);
        maxRule.setSurvival(maxSurvival);

        return new Pair<>(minRule, maxRule);
    }

    /**
     * Checks if the current rule is between the given minimum and maximum rules
     * @param minRule The minimum rule
     * @param maxRule The maximum rule
     * @return True if the current rule is between minimum and maximum rules and false
     * if the current rule is not between the minimum and maximum rules
     * @throws IllegalArgumentException Thrown if the minimum rule and maximum rule are invalid
     */
    @Override
    public boolean betweenMinMax(RuleFamily minRule, RuleFamily maxRule) {
        if (validMinMax(minRule, maxRule)) {
            return ((INT) minRule).getBirth().checkSubset(birth) &&
                    ((INT) minRule).getSurvival().checkSubset(survival) &&
                    birth.checkSubset(((INT) maxRule).getBirth()) &&
                    survival.checkSubset(((INT) maxRule).getSurvival());
        }

        throw new IllegalArgumentException("Invalid minimum and maximum rules!");
    }

    /**
     * Checks if the minimum rule and maximum rules provided are valid
     * @param minRule The minimum rule to check
     * @param maxRule The maximum rule to check
     * @return True if the minimum and maximum rules are valid and false if the minimum and maximum rules are not valid
     */
    @Override
    public boolean validMinMax(RuleFamily minRule, RuleFamily maxRule) {
        if (minRule instanceof INT && maxRule instanceof INT) {
            return ((INT) minRule).getBirth().checkSubset(((INT) maxRule).getBirth()) &&
                    ((INT) minRule).getSurvival().checkSubset(((INT) maxRule).getSurvival());
        }

        return false;
    }

    /**
     * Sets the birth conditions of the INT rule
     * @param birth The birth conditions
     */
    public void setBirth(INTTransitions birth) {
        this.birth = birth;

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
    }

    /**
     * Sets the survival conditions of the INT rule
     * @param survival The survival conditions
     */
    public void setSurvival(INTTransitions survival) {
        this.survival = survival;

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
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

    /**
     * Returns the name of the provided state
     * @param state The state of the cell
     * @return The name of the state
     */
    public String getName(int state) {
        if (state == 0) return "Dead";
        else return "Alive";
    }

    @Override
    public Object clone() {
        return new INT(rulestring);
    }

    @Override
    public Map<String, String> getRuleInfo() {
        Map<String, String> info = super.getRuleInfo();

        String neighbourhoodString = info.get("Weights / Neighbourhood");
        info.remove("Weights / Neighbourhood");

        ArrayList<String> symmetries = new ArrayList<>();
        if (birth instanceof R1MooreINT) {   // See https://conwaylife.com/wiki/Symmetry
            if (!birth.getSortedTransitionTable().contains("0!") &&
                    !birth.getSortedTransitionTable().contains("2c") &&
                    !birth.getSortedTransitionTable().contains("2i") &&
                    !birth.getSortedTransitionTable().contains("4i") &&
                    !birth.getSortedTransitionTable().contains("6i")) {
                symmetries.add("gO1S0");
            }

            if (!birth.getSortedTransitionTable().contains("0!") &&
                    !birth.getSortedTransitionTable().contains("2n") &&
                    !birth.getSortedTransitionTable().contains("2e") &&
                    !birth.getSortedTransitionTable().contains("4e") &&
                    !birth.getSortedTransitionTable().contains("4w") &&
                    !birth.getSortedTransitionTable().contains("6n")) {
                symmetries.add("gD1S0");
            }

            if (!birth.getSortedTransitionTable().contains("0!") &&
                    !birth.getSortedTransitionTable().contains("1c") &&
                    !birth.getSortedTransitionTable().contains("2k") &&
                    !birth.getSortedTransitionTable().contains("2n") &&
                    !birth.getSortedTransitionTable().contains("3n") &&
                    !birth.getSortedTransitionTable().contains("4y") &&
                    !birth.getSortedTransitionTable().contains("4z") &&
                    !birth.getSortedTransitionTable().contains("5r") &&
                    !birth.getSortedTransitionTable().contains("6i")) {
                symmetries.add("gO1S1");
            }

            if (!birth.getSortedTransitionTable().contains("0!") &&
                    !birth.getSortedTransitionTable().contains("1c") &&
                    !birth.getSortedTransitionTable().contains("1e") &&
                    !birth.getSortedTransitionTable().contains("2a") &&
                    !birth.getSortedTransitionTable().contains("2k") &&
                    !birth.getSortedTransitionTable().contains("3k") &&
                    !birth.getSortedTransitionTable().contains("3q") &&
                    !birth.getSortedTransitionTable().contains("4q")) {
                symmetries.add("gD1S1");
            }

            if (!birth.getSortedTransitionTable().contains("0!") &&
                    !birth.getSortedTransitionTable().contains("1c") &&
                    !birth.getSortedTransitionTable().contains("1e") &&
                    !birth.getSortedTransitionTable().contains("2a") &&
                    !birth.getSortedTransitionTable().contains("2i") &&
                    !birth.getSortedTransitionTable().contains("2k") &&
                    !birth.getSortedTransitionTable().contains("3c") &&
                    !birth.getSortedTransitionTable().contains("3q") &&
                    !birth.getSortedTransitionTable().contains("3r") &&
                    !birth.getSortedTransitionTable().contains("4c") &&
                    !birth.getSortedTransitionTable().contains("4n") &&
                    !birth.getSortedTransitionTable().contains("4y") &&
                    !birth.getSortedTransitionTable().contains("4z") &&
                    !birth.getSortedTransitionTable().contains("5e") &&
                    !birth.getSortedTransitionTable().contains("5r") &&
                    !birth.getSortedTransitionTable().contains("6i")) {
                symmetries.add("gO1S2");
            }
        }

        if (symmetries.size() != 0) info.put("Symmetries", symmetries.toString());
        info.put("Neighbourhood", neighbourhoodString);
        return info;
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

    @Override
    public int[][] possibleSuccessors(int generation) {
        int[][] array = new int[numStates][];

        // Dead cells can only become alive or stay dead
        array[0] = new int[] {0, 1};

        // Alive cells can only stay alive or die
        if (survival.getSortedTransitionTable().size() == 0) array[1] = new int[] {0};
        else array[1] = new int[] {1, 0};

        return array;
    }
}
