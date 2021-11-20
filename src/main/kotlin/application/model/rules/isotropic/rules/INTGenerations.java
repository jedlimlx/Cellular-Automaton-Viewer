package application.model.rules.isotropic.rules;

import org.javatuples.Pair;
import application.model.Coordinate;
import application.model.Utils;
import application.model.rules.ApgtableGeneratable;
import application.model.rules.MinMaxRuleable;
import application.model.rules.RuleFamily;
import application.model.rules.isotropic.transitions.INTTransitions;
import application.model.rules.ruleloader.RuleDirective;
import application.model.rules.ruleloader.ruletable.Ruletable;
import application.model.rules.ruleloader.ruletable.Variable;
import application.model.simulation.Grid;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Implements isotropic non-totalistic (INT) generations rules
 */
public class INTGenerations extends BaseINT implements ApgtableGeneratable, MinMaxRuleable {
    /**
     * The birth transitions of the INT generations rule
     */
    protected INTTransitions birth;

    /**
     * The survival transitions of the INT generations rule
     */
    protected INTTransitions survival;

    private static String[] transitionRegexes;
    private static String[] regexes;

    /**
     * Constructs an INT rule with the rule Frogs
     */
    public INTGenerations() {
        this("12/34/3");
    }

    /**
     * Creates a 2-state INT rule with the given rulestring
     * @param rulestring The rulestring of the 2-state INT rule to be created
     * @throws IllegalArgumentException Thrown if the rulestring is invalid
     */
    public INTGenerations(String rulestring) {
        super();

        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        name = "INT Generations";

        // Generating regexes
        regexes = new String[neighbourhoodLookup.size()];
        transitionRegexes = new String[neighbourhoodLookup.size()];

        int counter = 0;
        for (String string: neighbourhoodLookup.keySet()) {
            transitionRegexes[counter] = "(" + neighbourhoodLookup.get(string).getRegex() + ")*";
            if (string.equals("M")) {
                regexes[counter] = "[Gg][0-9]+[_/]?[BbSs]?" + transitionRegexes[counter] + "[_/]?[BbSs]?" +
                        transitionRegexes[counter] + "|[BbSs]?" + transitionRegexes[counter] + "[_/]?[BbSs]?" +
                        transitionRegexes[counter] + "[_/][0-9]+";
            } else {
                regexes[counter] = "[Gg][0-9]+[_/]?[BbSs]?" + transitionRegexes[counter] + "[_/]?[BbSs]?" +
                        transitionRegexes[counter] + "[_/]?N?" + string + "|[BbSs]?" +
                        transitionRegexes[counter] + "[_/]?[BbSs]?" +
                        transitionRegexes[counter] + "[_/][0-9]+[_/]?N?" + string;
            }
            counter++;
        }

        // Load rulestring
        setRulestring(rulestring);
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the INT generations rule
     * @throws IllegalArgumentException Thrown if an invalid rulestring is passed in
     */
    @Override
    protected void fromRulestring(String rulestring) throws IllegalArgumentException {
        boolean matched = false;
        for (int i = 0; i < regexes.length; i++) {
            if (rulestring.matches(regexes[i])) {
                try {  // Using B, S
                    birth = getINTTransition(rulestring);
                    birth.setTransitionString(Utils.matchRegex("[Bb](" + transitionRegexes[i] + ")",
                            rulestring, 0, 1));

                    survival = getINTTransition(rulestring);
                    survival.setTransitionString(Utils.matchRegex("[Ss](" + transitionRegexes[i] + ")",
                            rulestring, 0, 1));
                } catch (IllegalStateException exception) {
                    birth = getINTTransition(rulestring);
                    birth.setTransitionString(rulestring.split("/")[1]);

                    survival = getINTTransition(rulestring);
                    survival.setTransitionString(rulestring.split("/")[0]);
                }

                matched = true;
            }
        }

        if (matched) {
            try {
                numStates = Integer.parseInt(Utils.matchRegex("^[Gg]([0-9]+)", rulestring,
                        0, 1));
            } catch (IllegalStateException exception) {
                try {
                    numStates = Integer.parseInt(Utils.matchRegex("[0-9]+",
                            rulestring.split("/")[2], 0));
                } catch (IllegalStateException exception2) {
                    throw new IllegalArgumentException("State count should be specified!");
                }
            }

            updateBackground();
        }
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
            return survival.canoniseTransitions() + "/" + birth.canoniseTransitions() + "/" + numStates +
                    "/" + neighbourhoodString;
        else
            return survival.canoniseTransitions() + "/" + birth.canoniseTransitions() + "/" + numStates;
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
     * Returns a plain text description of the INT generations rule family to be displayed in the Rule Dialog
     * @return Description of the INT generations rule family
     */
    @Override
    public String getDescription() {
        return "This implements Isotropic Non-Totalistic (INT) Generations rules.\n" +
                "B0 rules are supported via emulation with alternating rules.\n\n" +
                "The format is as follows:\n" +
                "G<state>/B<birth>/S<survival>/N<neighbourhood>\n" +
                "<survival>/<birth>/<states>/<neighbourhood>";
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

        HashSet<Integer> dead = new HashSet<>();
        dead.add(0);
        for (int i = 2; i < numStates; i++) dead.add(i);
        Ruletable.DEAD = new Variable("dead", true, dead);
        ruletable.addVariable(Ruletable.DEAD);

        // Birth and survival transitions
        ruletable.addINTTransitions(birth, "0", "1", "dead", "1");
        ruletable.addINTTransitions(survival, "1", "1", "dead", "1");

        // Decay transitions
        for (int i = 1; i < numStates; i++)
            ruletable.addOTTransition(0, i + "", (i + 1) % numStates + "", "any", "0");

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
            setBirth(INTTransitions.randomise(((INTGenerations) minRule).getBirth(),
                    ((INTGenerations) maxRule).getBirth()));
            setSurvival(INTTransitions.randomise(((INTGenerations) minRule).getSurvival(),
                    ((INTGenerations) maxRule).getSurvival()));
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
            for (int i = 1; i < neighbours.length - 1; i++) {
                if (neighbours[i] == 0 || neighbours[i] > 1) cellNeighbours.add(0);
                else cellNeighbours.add(1);
            }

            if (currentCell == 0 && nextCell == 1) {  // Birth (0 -> 1)
                minBirth.addTransition(cellNeighbours);
            }
            else if (currentCell == 0 && nextCell == 0) {  // No Birth (0 -> 0)
                maxBirth.removeTransition(cellNeighbours);
            }
            else if (currentCell == 1 && nextCell == 1) {  // Survival (1 -> 1)
                minSurvival.addTransition(cellNeighbours);
            }
            else if (currentCell == 1 && nextCell == 2) {  // No Survival (1 -> 2)
                maxSurvival.removeTransition(cellNeighbours);
            }
        }

        // Creating the min and max rules
        INTGenerations minRule = (INTGenerations) this.clone();
        minRule.setBirth(minBirth);
        minRule.setSurvival(minSurvival);

        INTGenerations maxRule = (INTGenerations) this.clone();
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
            return ((INTGenerations) minRule).getBirth().checkSubset(birth) &&
                    ((INTGenerations) minRule).getSurvival().checkSubset(survival) &&
                    birth.checkSubset(((INTGenerations) maxRule).getBirth()) &&
                    survival.checkSubset(((INTGenerations) maxRule).getSurvival());
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
        if (minRule instanceof INTGenerations && maxRule instanceof INTGenerations) {
            return ((INTGenerations) minRule).getBirth().checkSubset(((INTGenerations) maxRule).getBirth()) &&
                    ((INTGenerations) minRule).getSurvival().checkSubset(((INTGenerations) maxRule).getSurvival());
        }

        return false;
    }

    /**
     * Sets the birth conditions of the INT generations rule
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
     * Sets the survival conditions of the INT generations rule
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
     * Gets the birth conditions of the INT generations rule
     * @return The birth conditions
     */
    public INTTransitions getBirth() {
        return birth;
    }

    /**
     * Gets the survival conditions of the INT generations rule
     * @return The survival conditions
     */
    public INTTransitions getSurvival() {
        return survival;
    }

    @Override
    public Object clone() {
        return new INTGenerations(rulestring);
    }

    @Override
    public Coordinate[] getNeighbourhood(int generation) {
        return birth.getNeighbourhood();
    }

    @Override
    public int dependsOnNeighbours(int state, int generation, Coordinate coordinate) {
        if (state < 2) return -1;
        else return (state + 1) % numStates;
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        for (int i = 0; i < neighbours.length; i++) neighbours[i] = neighbours[i] == 1 ? 1 : 0;
        if (cellState == 0 && birth.checkTransition(neighbours)) {
            return 1;
        }
        else if (cellState == 1 && survival.checkTransition(neighbours)) {
            return 1;
        }

        if (cellState == 0) return 0;
        return (cellState + 1) % numStates;
    }

    @Override
    public int[][] possibleSuccessors(int generation) {
        int[][] array = new int[numStates][];

        // Dead cells can only become alive or stay dead
        array[0] = new int[] {0, 1};

        // Alive cells can only stay alive or become dying state 1
        if (survival.getSortedTransitionTable().size() == 0) array[1] = new int[] {2};
        else array[1] = new int[] {1, 2};

        // Dying cells can only dying more or become fully dead
        for (int i = 2; i < numStates; i++) {
            array[i] = new int[] {(i + 1) % numStates};
        }

        return array;
    }
}
