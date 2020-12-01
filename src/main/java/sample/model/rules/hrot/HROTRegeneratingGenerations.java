package sample.model.rules.hrot;

import org.javatuples.Pair;
import sample.model.CommentGenerator;
import sample.model.Coordinate;
import sample.model.NeighbourhoodGenerator;
import sample.model.Utils;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.MinMaxRuleable;
import sample.model.rules.RuleFamily;
import sample.model.rules.ruleloader.RuleDirective;
import sample.model.rules.ruleloader.ruletable.Ruletable;
import sample.model.rules.ruleloader.ruletree.RuleTreeGen;
import sample.model.simulation.Grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Implements the HROT Regenerating Generations rule family
 */
public class HROTRegeneratingGenerations extends BaseHROT implements MinMaxRuleable, ApgtableGeneratable {
    /**
     * The birth conditions of the HROT Regenerating Generations rule
     */
    private final HashSet<Integer> birth;

    /**
     * The survival conditions of the HROT Regenerating Generations rule
     */
    private final HashSet<Integer> survival;

    /**
     * The regenerating birth conditions of the HROT Regenerating Generations rule
     */
    private final HashSet<Integer> regenBirth;

    /**
     * The regenerating survival conditions of the HROT Regenerating Generations rule
     */
    private final HashSet<Integer> regenSurvival;

    /**
     * The state weights of the HROT Regenerating Generations rule
     */
    private int[] stateWeights;

    /**
     * The maximum possible neighbourhood count.
     * Used for B0 and min, max rule generation.
     */
    private int maxNeighbourhoodCount;

    /**
     * The birth state of the regenerating generations rule
     */
    private int birthState;

    private final static String hrotRegex = "R[0-9]+,G[0-9]+,L[0-9]+,B" + hrotTransitions + ",S" +
            hrotTransitions + ",RB" + hrotTransitions + ",RS" + hrotTransitions +
            neighbourhoodRegex + "(,([A-Fa-f0-9]+))?";

    /**
     * Creates a HROT Regenerating Generations rule with the rule RegenLife
     */
    public HROTRegeneratingGenerations() {
        this("R1,G3,L1,B3,S2-3,RB3,6,RS5,8,NM");
    }

    /**
     * Creates a HROT Regenerating Generations rule with the given rulestring
     * @param rulestring The rulestring of the HROT Regenerating Generations rule to be created
     * @throws IllegalArgumentException Thrown if the rulestring is invalid
     */
    public HROTRegeneratingGenerations(String rulestring) {
        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        name = "HROT Regenerating Generations";

        birth = new HashSet<>();
        survival = new HashSet<>();
        regenBirth = new HashSet<>();
        regenSurvival = new HashSet<>();

        // Load rulestring
        setRulestring(rulestring);
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the HROT Regenerating Generations rule (eg. R1,C3,S1-2,B3-4,NM)
     * @throws IllegalArgumentException Thrown if an invalid rulestring is passed in
     */
    @Override
    protected void fromRulestring(String rulestring) throws IllegalArgumentException {
        // Clear birth and survival
        birth.clear();
        survival.clear();
        regenBirth.clear();
        regenSurvival.clear();

        if (rulestring.matches(hrotRegex)) {
            // Generate Neighbourhood
            int range = Integer.parseInt(Utils.matchRegex("R[0-9]+", rulestring, 0).substring(1));
            loadNeighbourhood(range, getNeighbourhoodSpecifier(rulestring));

            stateWeights = null;

            // Set the number of states
            numStates = Integer.parseInt(Utils.matchRegex("G[0-9]+", rulestring, 0).substring(1));

            // State Weights
            try {
                String LifeViewerStateWeights = Utils.matchRegex("NW([A-Fa-f0-9]+),([A-Fa-f0-9]+)",
                        rulestring, 0, 2);
                if (LifeViewerStateWeights.length() == numStates)
                    stateWeights = NeighbourhoodGenerator.getStateWeights(LifeViewerStateWeights);
                else
                    throw new IllegalArgumentException("State weights must have the same length as number of states");
            } catch (IllegalStateException exception) {
                stateWeights = null;
            }
        }

        // Get transitions
        Utils.getTransitionsFromStringWithCommas(birth,
                Utils.matchRegex("B" + hrotTransitions, rulestring, 0).substring(1));
        Utils.getTransitionsFromStringWithCommas(survival,
                Utils.matchRegex("S" + hrotTransitions, rulestring, 0).substring(1));
        Utils.getTransitionsFromStringWithCommas(regenBirth,
                Utils.matchRegex("RB" + hrotTransitions, rulestring, 0).substring(2));
        Utils.getTransitionsFromStringWithCommas(regenSurvival,
                Utils.matchRegex("RS" + hrotTransitions, rulestring, 0).substring(2));

        // Getting the birth state
        birthState = Integer.parseInt(Utils.matchRegex("L[0-9]+", rulestring, 0).substring(1));

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
            rulestringBuilder.append(Utils.matchRegex("R[0-9]+,G[0-9]+,L[0-9]+,", rulestring, 0));

            // Adding Birth
            rulestringBuilder.append("B").append(Utils.canoniseTransitionsWithCommas(birth));

            // Adding Survival
            rulestringBuilder.append("S").append(Utils.canoniseTransitionsWithCommas(survival));

            // Adding Regen Birth
            rulestringBuilder.append("RB").append(Utils.canoniseTransitionsWithCommas(regenBirth));

            // Adding Regen Survival
            rulestringBuilder.append("RS").append(Utils.canoniseTransitionsWithCommas(regenSurvival));

            // Adding neighbourhood
            rulestringBuilder.append(getNeighbourhoodSpecifier(rulestring)).
                    append(Utils.matchRegex("(,([A-Fa-f0-9]+))?", rulestring, 0));
        }

        newRulestring = rulestringBuilder.toString();
        return newRulestring;
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
     * Returns a plain text description of the HROT Regenerating Generations rule family to be displayed in the Rule Dialog
     * @return Description of the HROT Regenerating Generations rule family
     */
    @Override
    public String getDescription() {
        return "This implements the Higher Range Outer Totalistic (HROT) Regenerating Generations rulespace.\n" +
                "It supports arbitrary neighbourhoods via the CoordCA format (Specify with N@).\n" +
                "It supports arbitary weighted neighbourhoods via the LifeViewer format (Specify with NW).\n" +
                "B0 rules are supported via emulation alternating rules.\n" +
                "The format is as follows:\n" +
                "R<range>,C<numStates>,L<birthState>,B<birth>,S<survival>,RB<regenBirth>,RS<regenSurvival>" +
                "N<" + NeighbourhoodGenerator.neighbourhoodSymbols + "> or\n" +
                "R<range>,C<numStates>,L<birthState>,B<birth>,S<survival>,RB<regenBirth>,RS<regenSurvival>,N@<CoordCA> or\n" +
                "R<range>,C<numStates>,L<birthState>,B<birth>,S<survival>,RB<regenBirth>,RS<regenSurvival>,NW<Weights> or\n" +
                "R<range>,C<numStates>,L<birthState>,B<birth>,S<survival>,RB<regenBirth>,RS<regenSurvival>,NW<Weights>,<State Weights> or\n" +
                "Examples:\n" +
                "R1,G3,L1,B3,S2-3,RB3,6,RS5,8,NM (RegenLife)";
    }

    /**
     * Updates the background based on the currently loaded parameters
     */
    @Override
    public void updateBackground() {
        super.updateBackground();

        // Determine maximum neighbourhood count
        maxNeighbourhoodCount = 0;

        if (stateWeights != null) {
            int maxStateWeight = 0;
            for (int i = 0; i < numStates; i++) {
                maxStateWeight = Math.max(stateWeights[i], maxStateWeight);
            }

            if (weights != null) {
                for (int weight: weights) {
                    if (weight > 0) maxNeighbourhoodCount += weight * maxStateWeight;
                }
            }
            else {
                maxNeighbourhoodCount = neighbourhood.length * maxStateWeight;
            }
        }
        else {
            if (weights != null) {
                for (int weight: weights) {
                    if (weight > 0) maxNeighbourhoodCount += weight;
                }
            }
            else {
                maxNeighbourhoodCount = neighbourhood.length;
            }
        }
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
        if (minRule instanceof HROTRegeneratingGenerations && maxRule instanceof HROTRegeneratingGenerations) {
            Utils.randomiseTransitions(birth, ((HROTRegeneratingGenerations) minRule).birth,
                    ((HROTRegeneratingGenerations) maxRule).birth);
            Utils.randomiseTransitions(survival, ((HROTRegeneratingGenerations) minRule).survival,
                    ((HROTRegeneratingGenerations) maxRule).survival);
            Utils.randomiseTransitions(regenBirth, ((HROTRegeneratingGenerations) minRule).regenBirth,
                    ((HROTRegeneratingGenerations) maxRule).regenBirth);
            Utils.randomiseTransitions(regenSurvival, ((HROTRegeneratingGenerations) minRule).regenSurvival,
                    ((HROTRegeneratingGenerations) maxRule).regenSurvival);

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
        HashSet<Integer> minRegenBirth = new HashSet<>(), maxRegenBirth = new HashSet<>();
        HashSet<Integer> minRegenSurvival = new HashSet<>(), maxRegenSurvival = new HashSet<>();

        // Populate maxBirth & maxSurvival with numbers from 0 - max neighbour sum
        for (int i = 0; i < maxNeighbourhoodCount + 1; i++) {
            maxBirth.add(i);
            maxSurvival.add(i);
            maxRegenBirth.add(i);
            maxRegenSurvival.add(i);
        }

        // Running through every generation and check what transitions are required
        int sum;
        for (int[] neighbours: getNeighbourList(grids)) {
            sum = 0;

            // Computes the neighbourhood sum for every cell
            for (int i = 1; i < neighbours.length - 1; i++) {
                int cell = neighbours[i];
                if (stateWeights == null && cell == 1) {
                    if (weights != null) {
                        sum += cell * weights[i - 1];
                    } else {
                        sum += cell;
                    }
                } else if (stateWeights != null) {
                    if (weights != null) {
                        sum += stateWeights[cell] * weights[i - 1];
                    } else {
                        sum += stateWeights[cell];
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
            else if (currentCell == 1 && nextCell == 1) {  // Survival (1 -> 1)
                minSurvival.add(sum);
            }
            else if (currentCell == 1 && nextCell == 2) {  // No Survival (1 -> 2)
                maxSurvival.remove(sum);
            }
            else if (currentCell > 1 && nextCell == currentCell - 1) {  // Regeneration (n -> n - 1)
                minRegenBirth.add(sum);
            }
            else if (currentCell > 1 && nextCell == currentCell) {  // Survival (n -> n)
                minRegenSurvival.add(sum);
            }
            else if (currentCell > 1 && nextCell == (currentCell + 1) % numStates) {
                maxRegenBirth.remove(sum);
                maxRegenSurvival.remove(sum);
            }
        }

        // Construct the new rules and return them
        HROTRegeneratingGenerations minRule = (HROTRegeneratingGenerations) this.clone();
        minRule.setBirth(minBirth);
        minRule.setSurvival(minSurvival);
        minRule.setRegenBirth(minRegenBirth);
        minRule.setRegenSurvival(minRegenSurvival);

        HROTRegeneratingGenerations maxRule = (HROTRegeneratingGenerations) this.clone();
        maxRule.setBirth(maxBirth);
        maxRule.setSurvival(maxSurvival);
        maxRule.setRegenBirth(maxRegenBirth);
        maxRule.setRegenSurvival(maxRegenSurvival);

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
            return Utils.checkSubset(((HROTRegeneratingGenerations) minRule).getBirth(), this.getBirth()) &&
                    Utils.checkSubset(((HROTRegeneratingGenerations) minRule).getSurvival(), this.getSurvival()) &&
                    Utils.checkSubset(((HROTRegeneratingGenerations) minRule).getRegenBirth(), this.getRegenBirth()) &&
                    Utils.checkSubset(((HROTRegeneratingGenerations) minRule).getRegenSurvival(), this.getRegenSurvival()) &&
                    Utils.checkSubset(this.getBirth(), ((HROTRegeneratingGenerations) maxRule).getBirth()) &&
                    Utils.checkSubset(this.getSurvival(), ((HROTRegeneratingGenerations) maxRule).getSurvival()) &&
                    Utils.checkSubset(this.getRegenBirth(), ((HROTRegeneratingGenerations) maxRule).getRegenBirth()) &&
                    Utils.checkSubset(this.getRegenSurvival(), ((HROTRegeneratingGenerations) maxRule).getRegenSurvival());
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
        if (minRule instanceof HROTRegeneratingGenerations && maxRule instanceof HROTRegeneratingGenerations) {
            // Checks that the birth & survival of the min rule are a subset of the birth & survival of the max rule
            return Utils.checkSubset(((HROTRegeneratingGenerations) minRule).getBirth(),
                    ((HROTRegeneratingGenerations) maxRule).getBirth()) &&
                    Utils.checkSubset(((HROTRegeneratingGenerations) minRule).getSurvival(),
                            ((HROTRegeneratingGenerations) maxRule).getSurvival()) &&
                    Utils.checkSubset(((HROTRegeneratingGenerations) minRule).getRegenBirth(),
                            ((HROTRegeneratingGenerations) maxRule).getRegenBirth()) &&
                    Utils.checkSubset(((HROTRegeneratingGenerations) minRule).getRegenSurvival(),
                            ((HROTRegeneratingGenerations) maxRule).getRegenSurvival());
        }

        return false;
    }

    /**
     * Generates an apgtable for apgsearch to use
     * @return True if the operation was successful, false otherwise
     */
    @Override
    public RuleDirective[] generateApgtable() throws UnsupportedOperationException {
        if (stateWeights != null) {
            RuleTreeGen ruleTreeGen = new RuleTreeGen(numStates, neighbourhood, (neighbours, cellState) ->
                    transitionFunc(cellState, neighbours, 0, new Coordinate()));
            return new RuleDirective[]{ruleTreeGen.getRuleTree()};
        }

        // Generating the ruletable
        Ruletable ruletable = new Ruletable("");

        if (weights == null) ruletable.setPermute();  // Enable permute symmetry
        ruletable.setNumStates(numStates);

        ruletable.setNeighbourhood(neighbourhood);
        ruletable.setWeights(weights);

        ruletable.addVariable(Ruletable.ANY);
        ruletable.addVariable(Ruletable.DEAD);

        // Birth and survival transitions
        ruletable.addOTTransitions(birth, "0", birthState + "", "dead", "1");
        ruletable.addOTTransitions(survival, "1", "1", "dead", "1");

        for (int i = 2; i < numStates; i++) {
            ruletable.addOTTransitions(regenBirth, i + "", (i - 1) + "", "dead", "1");
            ruletable.addOTTransitions(regenSurvival, i + "", i + "", "dead", "1");
        }

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

            StringBuilder comment = new StringBuilder("#R");
            if (stateWeights != null) {
                for (int i = 0; i < numStates; i++) {
                    comment.append(stateWeights[i]).append(" ");
                }

                comments.add(comment.toString());
            }

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
            Pair<int[], Coordinate[]> weightsAndNeighbourhood;

            if (comments.length % 2 == 0) {  // If its even, there are state weights
                int index = 0;  // Getting state weights
                String stateWeightsComment = comments[comments.length - 1];
                for (String token: stateWeightsComment.split("\\s+")) {
                    if (!token.equals("#R")) {
                        stateWeights[index] = Integer.parseInt(token);
                        index++;
                    }
                }

                weightsAndNeighbourhood = CommentGenerator.getWeightsFromComments(
                        Arrays.copyOfRange(comments, 0, comments.length - 1));
            }
            else {
                weightsAndNeighbourhood = CommentGenerator.getWeightsFromComments(comments);
            }

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
     * The regenerating birth conditions of the rule (e.g. {2, 3})
     * @return Regenerating birth conditions of the rule
     */
    public HashSet<Integer> getRegenBirth() {
        return regenBirth;
    }

    /**
     * The survival conditions of the rule (e.g. {2, 3})
     * @return Regenerating survival conditions of the rule
     */
    public HashSet<Integer> getRegenSurvival() {
        return regenSurvival;
    }

    /**
     * Gets the state weights of the HROT Regenerating Generations rule
     * @return State weights of the HROT Regenerating Generations rule
     */
    public int[] getStateWeights() {
        return stateWeights;
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
     * @param survival Survival conditions of the rule
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
     * Sets the regenerating birth conditions of the rule
     * @param birth Regenerating birth conditions of the rule
     */
    public void setRegenBirth(HashSet<Integer> birth) {
        this.regenBirth.clear();
        this.regenBirth.addAll(birth);

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
    }

    /**
     * Sets the survival conditions of the rule
     * @param survival Regenerating survival conditions of the rule
     */
    public void setRegenSurvival(HashSet<Integer> survival) {
        this.regenSurvival.clear();
        this.regenSurvival.addAll(survival);

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
    }

    /**
     * Sets the states weights of the rule
     * @param stateWeights State weights of the rule
     */
    public void setStateWeights(int[] stateWeights) {
        this.stateWeights = stateWeights;
        updateBackground();
    }

    /**
     * Clones the rule
     * @return Returns a deep copy of the HROT Regenerating Generations rule
     */
    @Override
    public Object clone() {
        HROTRegeneratingGenerations newRule = new HROTRegeneratingGenerations(rulestring);
        newRule.setWeights(getWeights());
        newRule.setStateWeights(getStateWeights());
        newRule.setNeighbourhood(getNeighbourhood().clone());

        return newRule;
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        int sum = 0;
        for (int i = 0; i < neighbours.length; i++) {
            if (stateWeights == null && neighbours[i] == 1) {
                if (weights != null) {
                    sum += neighbours[i] * weights[i];
                }
                else {
                    sum += neighbours[i];
                }
            }
            else if (stateWeights != null){
                if (weights != null) {
                    sum += stateWeights[neighbours[i]] * weights[i];
                }
                else {
                    sum += stateWeights[neighbours[i]];
                }
            }
        }

        if (cellState == 1 && survival.contains(sum)) {  // Check Survival
            return 1;
        }
        else if (cellState == 0 && birth.contains(sum)) {  // Check Birth
            return birthState;
        }
        else if (cellState > 1 && regenBirth.contains(sum)) {
            return cellState - 1;
        }
        else if (cellState > 1 && regenSurvival.contains(sum)) {
            return cellState;
        }
        else {
            if (cellState != 0)
                return (cellState + 1) % numStates;
            else
                return 0;
        }
    }
}
