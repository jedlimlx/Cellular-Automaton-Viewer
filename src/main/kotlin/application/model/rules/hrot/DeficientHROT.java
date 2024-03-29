package application.model.rules.hrot;

import org.javatuples.Pair;
import application.model.Coordinate;
import application.model.NeighbourhoodGenerator;
import application.model.Utils;
import application.model.rules.ApgtableGeneratable;
import application.model.rules.MinMaxRuleable;
import application.model.rules.RuleFamily;
import application.model.rules.ruleloader.RuleDirective;
import application.model.rules.ruleloader.ruletable.Ruletable;
import application.model.rules.ruleloader.ruletable.Variable;
import application.model.simulation.Grid;

import java.util.*;

/**
 * Implements the Deficient HROT rule family
 */
public class DeficientHROT extends BaseHROT implements MinMaxRuleable, ApgtableGeneratable {
    /**
     * The birth conditions of the Deficient HROT rule
     */
    private final HashSet<Integer> birth;

    /**
     * The survival conditions of the Deficient HROT rule
     */
    private final HashSet<Integer> survival;

    /**
     * A map that looks up the state associated with a given birth condition
     */
    private final Map<Integer, Integer> stateLookup;

    /**
     * A map that looks up the birth condition associated with the state
     */
    private final Map<Integer, Integer> transitionLookup;

    /**
     * Does the rule have permanent deficiency?
     */
    private boolean permanentDeficiency;

    /**
     * The maximum possible neighbourhood count.
     * Used for B0 and min, max rule generation.
     */
    private int maxNeighbourhoodCount;

    private final static String hrotRegex = "R[0-9]+,D[0|1],S" + hrotTransitions + ",B" +
            hrotTransitions + neighbourhoodRegex;

    /**
     * Creates a Deficient HROT rule with the Minibugs rule
     */
    public DeficientHROT() {
        this("R2,D0,S6-9,B7-8,NM");
    }

    /**
     * Creates a Deficient HROT rule with the given rulestring
     * @param rulestring The rulestring of the 2-state HROT rule to be created
     * @throws IllegalArgumentException Thrown if the rulestring is invalid
     */
    public DeficientHROT(String rulestring) {
        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        name = "Deficient HROT";

        birth = new HashSet<>();
        survival = new HashSet<>();
        stateLookup = new HashMap<>();
        transitionLookup = new HashMap<>();

        // Load rulestring
        setRulestring(rulestring);
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the Deficient HROT rule (eg. R2,D0,S6-9,B7-8,NM)
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

        // Checking for permanent deficiency
        permanentDeficiency = Utils.matchRegex("D([0|1])", rulestring, 0, 1).equals("1");

        // Get transitions
        Utils.getTransitionsFromStringWithCommas(birth,
                Utils.matchRegex("B" + hrotTransitions, rulestring, 0).substring(1));
        Utils.getTransitionsFromStringWithCommas(survival,
                Utils.matchRegex("S" + hrotTransitions, rulestring, 0).substring(1));

        // Setting up the deficient states
        Integer[] birthArray = birth.toArray(new Integer[0]);
        Arrays.sort(birthArray);

        numStates = 2;
        for (int transition: birthArray) {
            stateLookup.put(transition, numStates);
            transitionLookup.put(numStates, transition);

            numStates++;
        }

        // Update the background
        updateBackground();
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
            rulestringBuilder.append(Utils.matchRegex("R[0-9]+,D[0|1],", rulestring, 0));

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

            // Handling B0
            if (birth.contains(0)) {
                // Checking for Smax
                if (survival.contains(maxNeighbourhoodCount)) {
                    if (permanentDeficiency) background = new int[]{1};
                    else background = new int[]{stateLookup.get(0)};

                    alternatingPeriod = 1;
                }
                else {
                    background = new int[]{0, stateLookup.get(0)};
                    alternatingPeriod = 2;
                }
            }
            else {
                background = new int[]{0};
                alternatingPeriod = 1;
            }
        }
        else {
            background = new int[]{0};
            alternatingPeriod = 1;
        }
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
     * Returns a plain text description of the Deficient HROT rule family to be displayed in the Rule Dialog
     * @return Description of the Deficient HROT rule family
     */
    @Override
    public String getDescription() {
        return "This implements the Deficient Higher Range Outer Totalistic (HROT) rulespace.\n" +
                "It supports arbitrary neighbourhoods via the CoordCA format (Specify with N@).\n" +
                "It supports arbitrary weighted neighbourhoods via the LV format (Specify with NW).\n" +
                "It supports B0 rules via emulation by alternating rules.\n\n" +
                "The format is as follows:\n" +
                "R<range>,D<permanentDeficiency?>,S<survival>,B<birth>,N@<CoordCA> or\n" +
                "R<range>,D<permanentDeficiency?>,S<survival>,B<birth>,NW<Weights> or\n" +
                "R<range>,D<permanentDeficiency?>,S<survival>,B<birth>,N<" +
                NeighbourhoodGenerator.neighbourhoodSymbols + ">\n\n" +
                "Examples:\n" +
                "R2,D0,S6-9,B7-8,NM (Deficient Minibugs)\n" +
                "R2,D0,S2-3,B3,N@891891 (Deficient Far Corners Life)";
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
            Utils.randomiseTransitions(birth,
                    ((DeficientHROT) minRule).getBirth(), ((DeficientHROT) maxRule).getBirth());
            Utils.randomiseTransitions(survival,
                    ((DeficientHROT) minRule).getSurvival(), ((DeficientHROT) maxRule).getSurvival());

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
        HashSet<Integer> minBirth = new HashSet<>(), maxBirth = new HashSet<>();
        HashSet<Integer> minSurvival = new HashSet<>(), maxSurvival = new HashSet<>();

        // Populate maxBirth & maxSurvival with numbers from 0 - max neighbour sum
        for (int i = 0; i < maxNeighbourhoodCount + 1; i++) {
            maxBirth.add(i);
            maxSurvival.add(i);
        }

        // Running through every generation and check what transitions are required
        int sum;
        for (int[] neighbours: getNeighbourList(grids)) {
            sum = 0;

            // Computes the neighbourhood sum for every cell
            HashSet<Integer> forbiddenTransitions = new HashSet<>();
            for (int i = 1; i < neighbours.length - 1; i++) {
                if (neighbours[i] == 0) continue;
                if (weights == null) sum += 1;
                else sum += weights[i - 1];

                forbiddenTransitions.add(transitionLookup.get(neighbours[i]));
            }

            // Determining the required birth / survival condition
            int currentCell = neighbours[0];
            int nextCell = neighbours[neighbours.length - 1];

            if (currentCell == 0 && nextCell > 0) {  // Birth (0 -> n)
                minBirth.add(sum);
            }
            else if (currentCell == 0 && nextCell == 0 && !forbiddenTransitions.contains(sum)) {  // No Birth (0 -> 0)
                maxBirth.remove(sum);
            }
            else if (currentCell > 0 && nextCell > 0) {  // Survival (n -> n)
                minSurvival.add(sum);
            }
            else if (currentCell > 0 && nextCell == 0) {  // No Survival (n -> 0)
                maxSurvival.remove(sum);
            }
        }

        // Construct the new rules and return them
        DeficientHROT minRule = (DeficientHROT) this.clone();
        minRule.setBirth(minBirth);
        minRule.setSurvival(minSurvival);

        DeficientHROT maxRule = (DeficientHROT) this.clone();
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
            return Utils.checkSubset(((DeficientHROT) minRule).getBirth(), this.getBirth()) &&
                    Utils.checkSubset(((DeficientHROT) minRule).getSurvival(), this.getSurvival()) &&
                    Utils.checkSubset(this.getBirth(), ((DeficientHROT) maxRule).getBirth()) &&
                    Utils.checkSubset(this.getSurvival(), ((DeficientHROT) maxRule).getSurvival());
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
        if (minRule instanceof DeficientHROT && maxRule instanceof DeficientHROT) {
            // Checks that the birth & survival of the min rule are a subset of the birth & survival of the max rule
            return Utils.checkSubset(((DeficientHROT) minRule).getBirth(), ((DeficientHROT) maxRule).getBirth()) &&
                    Utils.checkSubset(((DeficientHROT) minRule).getSurvival(), ((DeficientHROT) maxRule).getSurvival());
        }

        return false;
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
        ruletable.setNumStates(numStates);

        ruletable.setNeighbourhood(neighbourhood);
        ruletable.setWeights(weights);

        ruletable.addVariable(Ruletable.ANY);

        // Living Variables
        Integer[] living = new Integer[numStates - 1];
        for (int i = 1; i < numStates; i++) living[i - 1] = i;

        Ruletable.LIVE = new Variable("live", true, new HashSet<>(Arrays.asList(living)));
        ruletable.addVariable(Ruletable.LIVE);

        // Deficient Variables
        for (int transition: birth) {
            int index = 0;
            Integer[] deficient = new Integer[numStates - 2];  // Contains all states but one
            for (int state = 1; state < numStates; state++) {
                if (state == stateLookup.get(transition)) continue;
                deficient[index] = state;
                index++;
            }

            ruletable.addVariable(new Variable("d" + transition, true,
                    new HashSet<>(Arrays.asList(deficient))));
        }

        // Birth and survival transitions
        for (int transition: birth)
            ruletable.addOTTransition(transition, "0", stateLookup.get(transition) + "",
                    "0", "d" + transition);

        for (int state = 1; state < numStates; state++) {
            if (permanentDeficiency)
                ruletable.addOTTransitions(survival, state + "", state + "", "0", "live");
            else
                ruletable.addOTTransitions(survival, state + "", "1", "0", "live");
        }

        // Death transitions
        for (int state = 1; state < numStates; state++)
            ruletable.addOTTransition(0, state + "", "0", "any", "0");

        return new RuleDirective[]{ruletable};
    }

    /**
     * The birth conditions of the rule (e.g. {2, 3})
     * @return Birth conditions of the rule
     */
    public HashSet<Integer> getBirth() {
        return birth;
    }

    /**
     * The survival conditions of the rule (e.g. {2, 3})
     * @return Survival conditions of the rule
     */
    public HashSet<Integer> getSurvival() {
        return survival;
    }

    /**
     * Sets the birth conditions of the rule
     * @param birth Birth conditions of the rule
     */
    public void setBirth(HashSet<Integer> birth) {
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
    public void setSurvival(HashSet<Integer> survival) {
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
        DeficientHROT newRule = new DeficientHROT(rulestring);
        newRule.setWeights(getWeights());
        newRule.setNeighbourhood(getNeighbourhood(0).clone());

        return newRule;
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        int sum = 0;
        HashSet<Integer> forbiddenTransitions = new HashSet<>(neighbours.length);
        for (int i = 0; i < neighbours.length; i++) {
            if (neighbours[i] == 0) continue;
            if (weights != null) {
                sum += weights[i];
            }
            else {
                sum += 1;
            }

            forbiddenTransitions.add(transitionLookup.get(neighbours[i]));
        }

        if (cellState == 0 && birth.contains(sum) && !forbiddenTransitions.contains(sum)) {  // Check Birth
            return stateLookup.get(sum);
        }
        else if (cellState > 0 && survival.contains(sum)) {  // Check Survival
            if (permanentDeficiency) return cellState;
            else return 1;
        }

        return 0;
    }
}
