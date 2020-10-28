package sample.model.rules.hrot;

import org.javatuples.Pair;
import sample.model.*;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.MinMaxRuleable;
import sample.model.rules.RuleFamily;
import sample.model.rules.Tiling;
import sample.model.rules.ruleloader.RuleDirective;
import sample.model.rules.ruleloader.ruletable.Ruletable;
import sample.model.simulation.Grid;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Represents the 2-state HROT rule family
 */
public class HROT extends BaseHROT implements MinMaxRuleable, ApgtableGeneratable {
    /**
     * The birth conditions of the HROT rule
     */
    protected final HashSet<Integer> birth;

    /**
     * The survival conditions of the HROT rule
     */
    protected final HashSet<Integer> survival;

    /**
     * The maximum possible neighbourhood count.
     * Used for B0 and min, max rule generation.
     */
    protected int maxNeighbourhoodCount;

    private final static String moore = "([BSbs][0-8]*/?[BSbs][0-8]*|[BSbs]?[0-8]*/[BSbs]?[0-8]*)";
    private final static String vonNeumann = "([BSbs][0-4]*/?[BSbs][0-4]*?|[BSbs]?[0-4]*/[BSbs]?[0-4]*)V";
    private final static String hexagonal = "([BSbs][0-6]*/?[BSbs][0-6]*|[BSbs]?[0-6]*/[BSbs]?[0-6]*)H";
    private final static String hrot = "R[0-9]+,C[0|2],S" + hrotTransitions + ",B" +
            hrotTransitions + "," + neighbourhoodRegex;

    /**
     * Creates a 2-state HROT rule with the Minibugs rule
     */
    public HROT() {
        this("R2,C2,S6-9,B7-8,NM");
    }

    /**
     * Creates a 2-state HROT rule with the given rulestring
     * @param rulestring The rulestring of the 2-state HROT rule to be created
     * @throws IllegalArgumentException Thrown if the rulestring is invalid
     */
    public HROT(String rulestring) {
        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        name = "HROT";

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

        if (rulestring.matches(moore)) {
            // Generate Birth & Survival Transitions
            Utils.getTransitionsFromString(birth,
                    Utils.matchRegex("[Bb][0-8]*", rulestring, 0).substring(1));
            Utils.getTransitionsFromString(survival,
                    Utils.matchRegex("[Ss][0-8]*", rulestring, 0).substring(1));

            // Setting tiling
            tiling = Tiling.Square;

            // Generate Neighbourhood
            neighbourhood = NeighbourhoodGenerator.generateMoore(1);
            weights = null;
        }
        else if (rulestring.matches(vonNeumann)) {
            // Add to birth & survival
            Utils.getTransitionsFromString(birth,
                    Utils.matchRegex("[Bb][0-4]*", rulestring, 0).substring(1));
            Utils.getTransitionsFromString(survival,
                    Utils.matchRegex("[Ss][0-4]*", rulestring, 0).substring(1));

            // Setting tiling
            tiling = Tiling.Square;

            // Generate Neighbourhood
            neighbourhood = NeighbourhoodGenerator.generateVonNeumann(1);
            weights = null;
        }
        else if (rulestring.matches(hexagonal)) {
            // Add to birth & survival
            Utils.getTransitionsFromString(birth,
                    Utils.matchRegex("[Bb][0-6]*", rulestring, 0).substring(1));
            Utils.getTransitionsFromString(survival,
                    Utils.matchRegex("[Ss][0-6]*", rulestring, 0).substring(1));

            // Setting tiling
            tiling = Tiling.Hexagonal;

            // Generate Neighbourhood
            neighbourhood = NeighbourhoodGenerator.generateHexagonal(1);
            weights = null;
        }
        else if (rulestring.matches(hrot)) {
            // Generate Neighbourhood
            int range = Integer.parseInt(Utils.matchRegex("R[0-9]+", rulestring, 0).substring(1));
            String specifier = Utils.matchRegex("N["+
                    NeighbourhoodGenerator.neighbourhoodSymbols +"]", rulestring, 0);
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

        // Not using HROT notation
        if (rulestring.matches(moore) || rulestring.matches(vonNeumann) || rulestring.matches(hexagonal)) {
            // Adding Birth
            rulestringBuilder.append("B").append(Utils.canoniseTransitions(birth));

            // Adding Survival
            rulestringBuilder.append("/S").append(Utils.canoniseTransitions(survival));

            if (rulestring.charAt(rulestring.length() - 1) == 'V') {
                rulestringBuilder.append("V");
            }
            else if (rulestring.charAt(rulestring.length() - 1) == 'H') {
                rulestringBuilder.append("H");
            }
        } // Using HROT notation
        else if (rulestring.matches(hrot)) {
            rulestringBuilder.append(Utils.matchRegex("R[0-9]+,C[0|2],", rulestring, 0));

            // Adding Survival
            rulestringBuilder.append("S").append(Utils.canoniseTransitionsWithCommas(survival));

            // Adding Birth
            rulestringBuilder.append("B").append(Utils.canoniseTransitionsWithCommas(birth));

            // Adding neighbourhood
            rulestringBuilder.append(Utils.matchRegex("N.*", rulestring, 0));
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
                    background = new int[]{1};
                    alternatingPeriod = 1;
                }
                else {
                    background = new int[]{0, 1};
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
        return new String[]{moore, vonNeumann, hexagonal, hrot};
    }

    /**
     * Returns a plain text description of the 2-state HROT rule family to be displayed in the Rule Dialog
     * @return Description of the 2-state HROT rule family
     */
    @Override
    public String getDescription() {
        return "This implements the 2 state Higher Range Outer Totalistic (HROT) rulespace.\n" +
                "It supports arbitrary neighbourhoods via the CoordCA format (Specify with N@).\n" +
                "It supports arbitrary weighted neighbourhoods via the LV format (Specify with NW).\n" +
                "It supports B0 rules via emulation by alternating rules.\n\n" +
                "The format is as follows:\n" +
                "R<range>,C2,S<survival>,B<birth>,N@<CoordCA> or\n" +
                "R<range>,C2,S<survival>,B<birth>,NW<Weights> or\n" +
                "R<range>,C2,S<survival>,B<birth>,N<" + NeighbourhoodGenerator.neighbourhoodSymbols + ">\n\n" +
                "Examples:\n" +
                "B36/S23 (High Life)\n" +
                "B2/S34H (Hexagonal Life)\n" +
                "R2,C2,S6-9,B7-8,NM (Minibugs)\n" +
                "R2,C2,S2-3,B3,N@891891 (Far Corners Life)";
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
            Utils.randomiseTransitions(birth, ((HROT) minRule).getBirth(), ((HROT) maxRule).getBirth());
            Utils.randomiseTransitions(survival, ((HROT) minRule).getSurvival(), ((HROT) maxRule).getSurvival());

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
            for (int i = 1; i < neighbours.length - 1; i++) {
                if (weights == null) sum += neighbours[i];
                else sum += neighbours[i] * weights[i - 1];
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
            else if (currentCell == 1 && nextCell == 1) {  // Survival (1 -> 1)
                minSurvival.add(sum);
            }
            else if (currentCell == 1 && nextCell == 0) {  // No Survival (1 -> 0)
                maxSurvival.remove(sum);
            }
        }

        // Construct the new rules and return them
        HROT minRule = (HROT) this.clone();
        minRule.setBirth(minBirth);
        minRule.setSurvival(minSurvival);

        HROT maxRule = (HROT) this.clone();
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
            return Utils.checkSubset(((HROT) minRule).getBirth(), this.getBirth()) &&
                    Utils.checkSubset(((HROT) minRule).getSurvival(), this.getSurvival()) &&
                    Utils.checkSubset(this.getBirth(), ((HROT) maxRule).getBirth()) &&
                    Utils.checkSubset(this.getSurvival(), ((HROT) maxRule).getSurvival());
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
        if (minRule instanceof HROT && maxRule instanceof HROT) {
            // Checks that the birth & survival of the min rule are a subset of the birth & survival of the max rule
            return Utils.checkSubset(((HROT) minRule).getBirth(), ((HROT) maxRule).getBirth()) &&
                    Utils.checkSubset(((HROT) minRule).getSurvival(), ((HROT) maxRule).getSurvival());
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
        ruletable.setNumStates(2);

        ruletable.setNeighbourhood(neighbourhood);
        ruletable.setWeights(weights);

        ruletable.addVariable(Ruletable.ANY);

        // Birth and survival transitions
        ruletable.addOTTransitions(birth, "0", "1", "0", "1");
        ruletable.addOTTransitions(survival, "1", "1", "0", "1");

        // Death transitions
        ruletable.addOTTransition(0, "1", "0", "any", "0");

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
        HROT newRule = new HROT(rulestring);
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
            }
            else {
                sum += neighbours[i];
            }
        }

        if (cellState == 1 && survival.contains(sum)) {  // Check Survival
            return 1;
        }
        else if (cellState == 0 && birth.contains(sum)) {  // Check Birth
            return 1;
        }

        return 0;
    }
}
