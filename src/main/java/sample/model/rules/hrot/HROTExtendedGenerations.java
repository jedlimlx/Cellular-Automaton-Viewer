package sample.model.rules.hrot;

import org.javatuples.Pair;
import sample.model.*;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.MinMaxRuleable;
import sample.model.rules.RuleFamily;
import sample.model.rules.Tiling;
import sample.model.rules.ruleloader.RuleDirective;
import sample.model.rules.ruleloader.ruletable.Ruletable;
import sample.model.rules.ruleloader.ruletable.Variable;
import sample.model.simulation.Grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HROTExtendedGenerations extends BaseHROT implements MinMaxRuleable, ApgtableGeneratable {
    /**
     * The birth conditions of the HROT Extended Generations rule
     */
    private final HashSet<Integer> birth;

    /**
     * The survival conditions of the HROT Extended Generations rule
     */
    private final HashSet<Integer> survival;

    /**
     * The maximum possible neighbourhood count.
     * Used for B0 and min, max rule generation.
     */
    private int maxNeighbourhoodCount;

    /**
     * The active cells of the HROT extended generations rule
     */
    private HashSet<Integer> activeStates;

    private final static String extendedGenerations = "(0[-])?[1-9][0-9]*([-][1-9][0-9]*)*";
    private final static String hrotRegex = "R[0-9]+,B" + hrotTransitions + ",S" +
            hrotTransitions + ",G" + extendedGenerations + "," + neighbourhoodRegex;

    /**
     * Creates a HROT Extended Generations rule with the rule Reverse Frogs
     */
    public HROTExtendedGenerations() {
        this("R1,B3-4,S1-2,G0-1-1,NM");
    }

    /**
     * Creates a HROT Extended Generations rule with the given rulestring
     * @param rulestring The rulestring of the HROT Extended Generations rule to be created
     * @throws IllegalArgumentException Thrown if the rulestring is invalid
     */
    public HROTExtendedGenerations(String rulestring) {
        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        name = "HROT Extended Generations";

        birth = new HashSet<>();
        survival = new HashSet<>();

        // Load rulestring
        setRulestring(rulestring);
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the HROT rule (eg. B3/S23, R2,C2,S6-9,B7-8,NM)
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
            String specifier = Utils.matchRegex("N.*", rulestring, 0);
            loadNeighbourhood(range, specifier);

            // Get transitions
            Utils.getTransitionsFromStringWithCommas(birth,
                    Utils.matchRegex("B" + hrotTransitions, rulestring, 0).substring(1));
            Utils.getTransitionsFromStringWithCommas(survival,
                    Utils.matchRegex("S" + hrotTransitions, rulestring, 0).substring(1));
        }
        else {
            throw new IllegalArgumentException("This rulestring is invalid!");
        }

        String genExtString = Utils.matchRegex("G("+ extendedGenerations +")", rulestring, 0, 1);
        activeStates = NeighbourhoodGenerator.getActiveGenExtStates(genExtString);
        numStates = 1;
        for (String token: genExtString.split("-")) {
            numStates += Integer.parseInt(token);
        }

        // Get transitions
        Utils.getTransitionsFromStringWithCommas(birth,
                Utils.matchRegex("B" + hrotTransitions, rulestring, 0).substring(1));
        Utils.getTransitionsFromStringWithCommas(survival,
                Utils.matchRegex("S" + hrotTransitions, rulestring, 0).substring(1));

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
            rulestringBuilder.append(Utils.matchRegex("R[0-9]+,", rulestring, 0));

            // Adding Birth
            rulestringBuilder.append("B").append(Utils.canoniseTransitionsWithCommas(birth));

            // Adding Survival
            rulestringBuilder.append("S").append(Utils.canoniseTransitionsWithCommas(survival));

            // Adding neighbourhood & the extended generations string
            rulestringBuilder.append(Utils.matchRegex("G.*", rulestring, 0));
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

            // Handling B0 Rules
            if (birth.contains(0)) {
                // Checking for Smax
                if (survival.contains(maxNeighbourhoodCount)) {
                    int minimumState = Integer.MAX_VALUE;
                    for (int state: activeStates) {
                        minimumState = Math.min(minimumState, state);
                    }

                    background = new int[]{minimumState};
                    alternatingPeriod = 1;
                }
                else {
                    // Background -> {0, 1, 2, ...}
                    background = new int[numStates];
                    for (int i = 0; i < numStates; i++) {
                        background[i] = i;
                    }

                    // Setting the alternating period
                    alternatingPeriod = numStates;
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
     * Returns a plain text description of the HROT Extended Generations rule family to be displayed in the Rule Dialog
     * @return Description of the HROT Extended Generation rule family
     */
    @Override
    public String getDescription() {
        return "This implements the Higher Range Outer Totalistic (HROT) Extended Generations rulespace.\n" +
                "It supports arbitrary neighbourhoods via the CoordCA format (Specify with N@).\n" +
                "It supports arbitrary weighted neighbourhoods via the LV format (Specify with NW).\n" +
                "It supports B0 rules via emulation by alternating rules.\n\n" +
                "The format is as follows:\n" +
                "R<range>,S<survival>,B<birth>,G<genext>,N@<CoordCA> or\n" +
                "R<range>,S<survival>,B<birth>,G<genext>,NW<Weights> or\n" +
                "R<range>,S<survival>,B<birth>,G<genext>,N<" + NeighbourhoodGenerator.neighbourhoodSymbols + ">\n\n" +
                "Examples:\n" +
                "R1,B3-4,S1-2,G0-1-1,NM (sgorF)\n" +
                "R1,B2,S,G0-1-1,NM (niarB s'nairB)";
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
        if (minRule instanceof HROTExtendedGenerations && maxRule instanceof HROTExtendedGenerations) {
            Utils.randomiseTransitions(birth, ((HROTExtendedGenerations) minRule).birth,
                    ((HROTExtendedGenerations) maxRule).birth);
            Utils.randomiseTransitions(survival, ((HROTExtendedGenerations) minRule).survival,
                    ((HROTExtendedGenerations) maxRule).survival);

            rulestring = canonise(rulestring);  // Reload the rulestring with the new birth / survival conditions
            updateBackground();  // Update the background
        }
        else {
            throw new IllegalArgumentException("The rule families selected have to be the same!");
        }
    }

    /**
     * Returns the minimum and maximum rule of the provided evolutionary sequence
     * @param grids An array of grids representing the evolutionary sequence
     * @return A pair containing the min rule as the first value and the max rule as the second value
     */
    @Override
    public Pair<RuleFamily, RuleFamily> getMinMaxRule(Grid[] grids) {
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
            for (int j = 1; j < neighbours.length - 1; j++) {
                int cell = neighbours[j];
                if (activeStates.contains(cell)) {
                    if (weights != null) {
                        sum += weights[j - 1];
                    }
                    else {
                        sum += 1;
                    }
                }
            }

            // Determining the required birth / survival condition
            int currentCell = neighbours[0];
            int nextCell = neighbours[neighbours.length - 1];

            if (currentCell == 0 && nextCell == 1) {  // Birth (0 -> 1)
                minBirth.add(sum);
            }
            else if (currentCell == 0 && nextCell == 0) {  // No Birth (0 -> 0)
                maxBirth.remove(sum);
            }
            else if (activeStates.contains(currentCell) && nextCell == currentCell) {  // Survival (1 -> 1)
                minSurvival.add(sum);
            }
            else if (activeStates.contains(currentCell) && nextCell == (currentCell + 1) % numStates) {  // No Survival (1 -> 2)
                maxSurvival.remove(sum);
            }
        }

        // Construct the new rules and return them
        HROTExtendedGenerations minRule = (HROTExtendedGenerations) this.clone();
        minRule.setBirth(minBirth);
        minRule.setSurvival(minSurvival);

        HROTExtendedGenerations maxRule = (HROTExtendedGenerations) this.clone();
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
            return Utils.checkSubset(((HROTGenerations) minRule).getBirth(), this.getBirth()) &&
                    Utils.checkSubset(((HROTGenerations) minRule).getSurvival(), this.getSurvival()) &&
                    Utils.checkSubset(this.getBirth(), ((HROTGenerations) maxRule).getBirth()) &&
                    Utils.checkSubset(this.getSurvival(), ((HROTGenerations) maxRule).getSurvival());
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
        if (minRule instanceof HROTGenerations && maxRule instanceof HROTGenerations) {
            // Checks that the birth & survival of the min rule are a subset of the birth & survival of the max rule
            return Utils.checkSubset(((HROTGenerations) minRule).getBirth(), ((HROTGenerations) maxRule).getBirth()) &&
                    Utils.checkSubset(((HROTGenerations) minRule).getSurvival(), ((HROTGenerations) maxRule).getSurvival());
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

        // Active Variables
        Set<Integer> inactive = new HashSet<>();
        for (int i = 0; i < numStates; i++) {
            if (!activeStates.contains(i)) inactive.add(i);
        }

        Ruletable.LIVE = new Variable("live", true, activeStates);
        Ruletable.DEAD = new Variable("dead", true, inactive);

        ruletable.addVariable(Ruletable.ANY);
        ruletable.addVariable(Ruletable.LIVE);
        ruletable.addVariable(Ruletable.DEAD);

        // Birth and survival transitions
        ruletable.addOTTransitions(birth, "0", "1", "dead", "live");

        for (int state: activeStates)
            ruletable.addOTTransitions(survival, state + "", state + "", "dead", "live");

        // Decay transitions
        for (int i = 1; i < numStates; i++)
            ruletable.addOTTransition(0, i + "", (i + 1) % numStates + "", "any", "0");

        return new RuleDirective[]{ruletable};
    }

    /**
     * Generates comments that will be placed in the RLE.
     * These comments represent weights.
     * They are only generated if the neighbourhood specifier is N@ with no additional hex digits
     * @return An array of comments each starting with "#R" (eg. {"#R 1 2 3 2 1", "#R 2 4 6 4 2"}).
     * If no additional information needs to be added return null or an empty string array.
     */
    @Override
    public String[] generateComments() {
        if (rulestring.charAt(rulestring.length() - 1) == '@') {
            ArrayList<String> comments = CommentGenerator.generateFromWeights(weights, neighbourhood);
            return comments.toArray(new String[0]);
        }
        else {
            return null;
        }
    }

    /**
     * Loads the weights stored in the comments generated by generateComments
     * @param comments The comments from the RLE (all starting with #R)
     */
    @Override
    public void loadComments(String[] comments) {
        if (comments.length > 0) {  // Check if there are even any comments
            Pair<int[], Coordinate[]> weightsAndNeighbourhood =
                    CommentGenerator.getWeightsFromComments(comments);

            // Setting weights and neighbourhood
            setWeights(weightsAndNeighbourhood.getValue0());
            setNeighbourhood(weightsAndNeighbourhood.getValue1());
        }
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
        HROTExtendedGenerations newRule = new HROTExtendedGenerations(rulestring);
        newRule.setWeights(getWeights());
        newRule.setNeighbourhood(getNeighbourhood(0).clone());

        return newRule;
    }


    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        int sum = 0;
        for (int i = 0; i < neighbours.length; i++) {
            if (activeStates.contains(neighbours[i])) {
                if (weights != null) {
                    sum += weights[i];
                }
                else {
                    sum += 1;
                }
            }
        }

        if (activeStates.contains(cellState) && survival.contains(sum)) {  // Check Survival
            return cellState;
        }
        else if (cellState == 0 && birth.contains(sum)) {  // Check Birth
            return 1;
        }
        else {
            if (cellState != 0)
                return (cellState + 1) % numStates;
            else
                return 0;
        }
    }

    @Override
    public int dependsOnNeighbours(int state, int generation, Coordinate coordinate) {
        if (activeStates.contains(state) || state == 0) return super.dependsOnNeighbours(state, generation, coordinate);
        else return (state + 1) % numStates;
    }
}
