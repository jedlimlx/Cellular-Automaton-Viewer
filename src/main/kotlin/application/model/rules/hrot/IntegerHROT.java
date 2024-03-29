package application.model.rules.hrot;

import org.javatuples.Pair;
import application.model.Coordinate;
import application.model.NeighbourhoodGenerator;
import application.model.Utils;
import application.model.rules.ApgtableGeneratable;
import application.model.rules.MinMaxRuleable;
import application.model.rules.RuleFamily;
import application.model.rules.ruleloader.RuleDirective;
import application.model.rules.ruleloader.ruletree.RuleTreeGen;
import application.model.simulation.Grid;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Implements the Integer HROT rule family
 */
public class IntegerHROT extends BaseHROT implements ApgtableGeneratable, MinMaxRuleable {
    /**
     * The birth conditions of the Integer HROT rule
     */
    private final TreeSet<Integer> birth;

    /**
     * The survival conditions of the Integer HROT rule
     */
    private final TreeSet<Integer> survival;

    /**
     * The maximum possible neighbourhood count.
     * Used for B0 and min, max rule generation.
     */
    private int maxNeighbourhoodCount;

    private final static String hrotRegex = "R[0-9]+,I[0-9]+,S" + hrotTransitions + ",B" +
            hrotTransitions + neighbourhoodRegex;

    /**
     * Creates an Integer HROT rule with the Integer Life rule
     */
    public IntegerHROT() {
        this("R1,I20,S2-3,B3,NM");
    }

    /**
     * Creates an Integer HROT rule with the given rulestring
     * @param rulestring The rulestring of the Integer HROT rule to be created
     * @throws IllegalArgumentException Thrown if the rulestring is invalid
     */
    public IntegerHROT(String rulestring) {
        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        name = "Integer HROT";

        birth = new TreeSet<>();
        survival = new TreeSet<>();

        // Load rulestring
        setRulestring(rulestring);
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the Integer HROT rule (eg. R2,I20,S6-9,B7-8,NM)
     * @throws IllegalArgumentException Thrown if an invalid rulestring is passed in
     */
    @Override
    protected void fromRulestring(String rulestring) throws IllegalArgumentException {
        // Clear birth and survival
        birth.clear();
        survival.clear();

        if (rulestring.matches(hrotRegex)) {
            // Generate Neighbourhood
            int range = Integer.parseInt(Utils.matchRegex("R[0-9]+", rulestring, 0).substring(1));
            loadNeighbourhood(range, getNeighbourhoodSpecifier(rulestring));

            // Get transitions
            Utils.getTransitionsFromStringWithCommas(birth,
                    Utils.matchRegex("B" + hrotTransitions, rulestring, 0).substring(1));
            Utils.getTransitionsFromStringWithCommas(survival,
                    Utils.matchRegex("S" + hrotTransitions, rulestring, 0).substring(1));
        }
        else {
            throw new IllegalArgumentException("This rulestring is invalid!");
        }

        // Get transitions
        Utils.getTransitionsFromStringWithCommas(birth,
                Utils.matchRegex("B" + hrotTransitions, rulestring, 0).substring(1));
        Utils.getTransitionsFromStringWithCommas(survival,
                Utils.matchRegex("S" + hrotTransitions, rulestring, 0).substring(1));

        // Setting number of states
        numStates = Integer.parseInt(Utils.matchRegex("I([0-9]+)", rulestring, 0, 1));

        // Update the background
        updateBackground();

        if (birth.contains(0)) {
            throw new IllegalArgumentException("B0 is not supported for Integer HROT!");
        }
    }

    /**
     * Canonises the inputted rulestring with the currently loaded parameters.
     * @param rulestring The rulestring to canonised
     * @return Canonised rulestring
     */
    @Override
    public String canonise(String rulestring) {
        // Define Regexes
        String newRulestring = "";
        StringBuilder rulestringBuilder = new StringBuilder(newRulestring);

        if (rulestring.matches(hrotRegex)) {
            rulestringBuilder.append(Utils.matchRegex("R[0-9]+,I[0-9]+,", rulestring, 0));

            // Adding Survival
            rulestringBuilder.append("S").append(Utils.canoniseTransitionsWithCommas(survival));

            // Adding Birth
            rulestringBuilder.append("B").append(Utils.canoniseTransitionsWithCommas(birth));

            // Adding neighbourhood
            rulestringBuilder.append(getNeighbourhoodSpecifier(rulestring));
        }

        newRulestring = rulestringBuilder.toString();
        return newRulestring;
    }

    /**
     * Updates the background based on the currently loaded parameters
     */
    @Override
    public void updateBackground() {
        // Determine maximum neighbourhood count
        maxNeighbourhoodCount = 0;

        if (neighbourhood != null) {
            if (weights != null) {
                for (int weight: weights) {
                    if (weight > 0)
                        maxNeighbourhoodCount += weight;
                }
            }
            else {
                maxNeighbourhoodCount = neighbourhood.length;
            }
        }

        background = new int[]{0};
        alternatingPeriod = 1;
    }

    /**
     * The regexes that will match a valid rulestring
     * @return An array of regexes that will match a valid rulestring
     */
    @Override
    public String[] getRegex() {
        return new String[]{hrotRegex};
    }

    /**
     * Returns a plain text description of the Integer HROT rule family to be displayed in the Rule Dialog
     * @return Description of the Integer HROT rule family
     */
    @Override
    public String getDescription() {
        return "This implements the Integer Higher Range Outer Totalistic (HROT) rulespace based " +
                "Mark Niemiec's Integer Life.\n" +
                "It supports arbitrary neighbourhoods via the CoordCA format (Specify with N@).\n" +
                "It supports arbitrary weighted neighbourhoods via the LV format (Specify with NW).\n\n" +
                "The format is as follows:\n" +
                "R<range>,I<states>,S<survival>,B<birth>,N@<CoordCA> or\n" +
                "R<range>,I<states>,S<survival>,B<birth>,NW<Weights> or\n" +
                "R<range>,I<states>,S<survival>,B<birth>,N<" + NeighbourhoodGenerator.neighbourhoodSymbols + ">\n\n" +
                "Examples:\n" +
                "R2,I20,S6-9,B7-8,NM (Integer Minibugs)\n" +
                "R2,I20,S2-3,B3,NM (Integer Life)";
    }

    /**
     * Randomise the parameters of the current rule to be between minimum and maximum rules
     * Used in CAViewer's rule search program
     * @param minRule The minimum rule for randomisation
     * @param maxRule The maximum rule for randomisation
     * @throws IllegalArgumentException Thrown if the minimum and maximum rules are invalid
     */
    @Override
    public void randomise(RuleFamily minRule, RuleFamily maxRule) throws IllegalArgumentException {
        if (validMinMax(minRule, maxRule)) {
            Utils.randomiseTransitions(birth, ((IntegerHROT) minRule).getBirth(), ((IntegerHROT) maxRule).getBirth());
            Utils.randomiseTransitions(survival, ((IntegerHROT) minRule).getSurvival(), ((IntegerHROT) maxRule).getSurvival());

            rulestring = canonise(rulestring);  // Reload the rulestring with the new birth / survival conditions
            updateBackground(); // Updating the background (in case its B0)
        }
        else {
            throw new IllegalArgumentException("Invalid minimum and maximum rules!");
        }
    }

    /**
     * Returns the minimum and maximum rule of the provided evolutionary sequence
     * @param grids An array of grids representing the evolutionary sequence
     * @return A pair containing the min rule as the first value and the max rule as the second value
     */
    @Override
    public Pair<RuleFamily, RuleFamily> getMinMaxRule(Grid[] grids)  {
        TreeSet<Integer> minBirth = new TreeSet<>(), maxBirth = new TreeSet<>();
        TreeSet<Integer> minSurvival = new TreeSet<>(), maxSurvival = new TreeSet<>();

        // Populate maxBirth & maxSurvival with numbers from 1 - max neighbour sum
        for (int i = 1; i < maxNeighbourhoodCount + 1; i++) {
            maxBirth.add(i);
            maxSurvival.add(i);
        }

        // Running through every generation and check what transitions are required
        int sum;
        for (int[] neighbours: getNeighbourList(grids)) {
            sum = 0;

            // Computes the neighbourhood sum for every cell
            for (int i = 1; i < neighbours.length - 1; i++) {
                if (weights == null) sum += neighbours[i];
                else sum += neighbours[i] * weights[i - 1];
            }

            // Determining the required birth / survival condition
            int currentCell = neighbours[0];
            int nextCell = neighbours[neighbours.length - 1];

            if (currentCell == 0 && nextCell != 0) {  // Birth (0 -> n)
                minBirth.add(sum / nextCell);
            }
            else if (currentCell == 0) {  // No Birth (0 -> 0)
                ArrayList<Integer> toBeRemoved = new ArrayList<>();
                for (int transition: maxBirth) {  // Checking divisibility
                    if (sum % transition == 0 && sum != 0 && sum / transition < numStates) {
                        toBeRemoved.add(transition);
                    }
                }

                maxBirth.removeAll(toBeRemoved);
            }
            else if (currentCell > 0 && nextCell == currentCell) {  // Survival (1 -> 1)
                minSurvival.add(sum / currentCell);
            }
            else if (currentCell > 0 && nextCell == 0) {  // No Survival (1 -> 0)
                maxSurvival.remove(Integer.valueOf(sum / currentCell));
            }
        }

        // Construct the new rules and return them
        IntegerHROT minRule = (IntegerHROT) this.clone();
        minRule.setBirth(minBirth);
        minRule.setSurvival(minSurvival);

        IntegerHROT maxRule = (IntegerHROT) this.clone();
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
    public boolean betweenMinMax(RuleFamily minRule, RuleFamily maxRule) throws IllegalArgumentException {
        if (validMinMax(minRule, maxRule)) {
            // Checking that this rule is a superset of minRule and a subset of maxRule
            return Utils.checkSubset(((IntegerHROT) minRule).getBirth(), this.getBirth()) &&
                    Utils.checkSubset(((IntegerHROT) minRule).getSurvival(), this.getSurvival()) &&
                    Utils.checkSubset(this.getBirth(), ((IntegerHROT) maxRule).getBirth()) &&
                    Utils.checkSubset(this.getSurvival(), ((IntegerHROT) maxRule).getSurvival());
        }
        else {
            throw new IllegalArgumentException("Invalid minimum and maximum rules!");
        }
    }

    /**
     * Checks if the minimum rule and maximum rules provided are valid
     * @param minRule The minimum rule to check
     * @param maxRule The maximum rule to check
     * @return True if the minimum and maximum rules are valid and false if the minimum and maximum rules are not valid
     */
    @Override
    public boolean validMinMax(RuleFamily minRule, RuleFamily maxRule) {
        if (minRule instanceof IntegerHROT && maxRule instanceof IntegerHROT) {
            // Checks that the birth & survival of the min rule are a subset of the birth & survival of the max rule
            return Utils.checkSubset(((IntegerHROT) minRule).getBirth(), ((IntegerHROT) maxRule).getBirth()) &&
                    Utils.checkSubset(((IntegerHROT) minRule).getSurvival(), ((IntegerHROT) maxRule).getSurvival());
        }

        return false;
    }

    /**
     * Generates a ruletree for apgsearch to use
     * @return Returns a ruletree for apgsearch to use
     */
    @Override
    public RuleDirective[] generateApgtable() {
        RuleTreeGen ruleTreeGen = new RuleTreeGen(numStates, neighbourhood, (neighbours, cellState) ->
                transitionFunc(cellState, neighbours, 0, new Coordinate()));
        return new RuleDirective[]{ruleTreeGen.getRuleTree()};
    }

    /**
     * The birth conditions of the rule (e.g. {2, 3})
     * @return Birth conditions of the rule
     */
    public TreeSet<Integer> getBirth() {
        return birth;
    }

    /**
     * The survival conditions of the rule (e.g. {2, 3})
     * @return Survival conditions of the rule
     */
    public TreeSet<Integer> getSurvival() {
        return survival;
    }

    /**
     * Sets the birth conditions of the rule
     * @param birth Birth conditions of the rule
     */
    public void setBirth(TreeSet<Integer> birth) {
        this.birth.clear();
        this.birth.addAll(birth);

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
    }

    /**
     * Sets the survival conditions of the rule
     * @param survival Birth conditions of the rule
     */
    public void setSurvival(TreeSet<Integer> survival) {
        this.survival.clear();
        this.survival.addAll(survival);

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
    }

    /**
     * Clones the rule
     * @return Returns a deep copy of the HROT rule
     */
    @Override
    public Object clone() {
        IntegerHROT newRule = new IntegerHROT(rulestring);
        newRule.setWeights(getWeights());
        newRule.setNeighbourhood(getNeighbourhood(0).clone());

        return newRule;
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        int sum = 0;
        for (int i = 0; i < neighbours.length; i++) {
            if (weights != null) {
                sum += neighbours[i] * weights[i];
            } else {
                sum += neighbours[i];
            }
        }

        if (cellState == 0) {  // Check Birth
            for (int transition : birth) {  // Checking divisibility
                if (sum % transition == 0 && sum != 0 && sum / transition < numStates) {
                    return sum / transition;
                }
            }
        } else {  // Checking survival
            for (int transition : survival) {  // Checking divisibility
                if (transition * cellState <= sum && sum < (transition + 1) * cellState) {
                    return cellState;
                }
            }
        }

        return 0;
    }
}
