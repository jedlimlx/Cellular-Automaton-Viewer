package sample.model.rules;

import org.javatuples.Pair;
import sample.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Represents the Integer HROT rule family
 */
public class IntegerHROT extends RuleFamily {
    /**
     * The birth conditions of the Integer HROT rule
     */
    private final ArrayList<Integer> birth;

    /**
     * The survival conditions of the Integer HROT rule
     */
    private final ArrayList<Integer> survival;

    /**
     * The neighbourhood of the Integer HROT rule
     */
    private Coordinate[] neighbourhood;

    /**
     * The neighbourhood weights of the Integer HROT rule.
     * For example, {1, 2, 1, 2, 0, 2, 1, 2, 1}
     */
    private int[] weights;

    /**
     * The maximum possible neighbourhood count.
     * Used for B0 and min, max rule generation.
     */
    private int maxNeighbourhoodCount;

    private final static String hrotTransitions = "(((\\d,(?=\\d))|(\\d-(?=\\d))|\\d)+)?";
    private final static String higherRangePredefined = "R[0-9]+,I[0-9]+,S" + hrotTransitions + ",B" +
            hrotTransitions + ",N[" + NeighbourhoodGenerator.neighbourhoodSymbols + "]";
    private final static String higherRangeCustom = "R[0-9]+,I[0-9]+,S" + hrotTransitions +
            ",B" + hrotTransitions + ",N@([A-Fa-f0-9]+)?[HL]?";
    private final static String higherRangeWeightedCustom = "R[0-9]+,I[0-9]+,S" + hrotTransitions +
            ",B" + hrotTransitions + ",NW[A-Fa-f0-9]+[HL]?";

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

        birth = new ArrayList<>();
        survival = new ArrayList<>();

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

        if (rulestring.matches(higherRangePredefined)) {
            // Generate Neighbourhood
            int range = Integer.parseInt(Utils.matchRegex("R[0-9]+", rulestring, 0).substring(1));
            char neighbourhoodSymbol = Utils.matchRegex("N["+
                    NeighbourhoodGenerator.neighbourhoodSymbols +"]", rulestring, 0).charAt(1);

            neighbourhood = NeighbourhoodGenerator.generateFromSymbol(neighbourhoodSymbol, range);
            weights = NeighbourhoodGenerator.generateWeightsFromSymbol(neighbourhoodSymbol, range);
            tiling = NeighbourhoodGenerator.generateTilingFromSymbol(neighbourhoodSymbol);
        }
        else if (rulestring.matches(higherRangeCustom)) {
            // Generate Neighbourhood
            int range = Integer.parseInt(Utils.matchRegex("R[0-9]+", rulestring, 0).substring(1));
            String CoordCA = Utils.matchRegex("N@([A-Fa-f0-9]+)?", rulestring, 0).substring(2);
            weights = null;

            if (CoordCA.length() > 0)
                neighbourhood = NeighbourhoodGenerator.fromCoordCA(CoordCA, range);

            try {
                String tilingString = Utils.matchRegex("N@(?:[A-Fa-f0-9]+)?([HL]?)",
                        rulestring, 0, 1);
                if (tilingString.equals("H")) tiling = Tiling.Hexagonal;
                else if (tilingString.equals("L")) tiling = Tiling.Triangular;
            } catch (IllegalStateException exception) {
                tiling = Tiling.Square;
            }
        }
        else if (rulestring.matches(higherRangeWeightedCustom)) {
            // Generate Neighbourhood
            int range = Integer.parseInt(Utils.matchRegex("R[0-9]+", rulestring, 0).substring(1));
            String LifeViewer = Utils.matchRegex("NW[A-Fa-f0-9]+", rulestring, 0).substring(2);

            Pair<Coordinate[], int[]> neighbourhoodAndWeights =
                    NeighbourhoodGenerator.getNeighbourhoodWeights(LifeViewer, range);
            neighbourhood = neighbourhoodAndWeights.getValue0();
            weights = neighbourhoodAndWeights.getValue1();

            try {
                String tilingString = Utils.matchRegex("NW[A-Fa-f0-9]+([HL]?)",
                        rulestring, 0, 1);
                if (tilingString.equals("H")) tiling = Tiling.Hexagonal;
                else if (tilingString.equals("L")) tiling = Tiling.Triangular;
            } catch (IllegalStateException exception) {
                tiling = Tiling.Square;
            }
        }
        else {
            throw new IllegalArgumentException("This rulestring is invalid!");
        }

        // Get transitions
        Utils.getTransitionsFromStringWithCommas(birth,
                Utils.matchRegex("B" + hrotTransitions, rulestring, 0).substring(1));
        Utils.getTransitionsFromStringWithCommas(survival,
                Utils.matchRegex("S" + hrotTransitions, rulestring, 0).substring(1));

        Collections.sort(this.birth);
        Collections.sort(this.survival);

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

        if (rulestring.matches(higherRangeCustom) || rulestring.matches(higherRangePredefined) ||
                rulestring.matches(higherRangeWeightedCustom)) {
            rulestringBuilder.append(Utils.matchRegex("R[0-9]+,I[0-9]+,", rulestring, 0));

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
        return new String[]{higherRangeCustom, higherRangePredefined, higherRangeWeightedCustom};
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
        ArrayList<Integer> minBirth = new ArrayList<>(), maxBirth = new ArrayList<>();
        ArrayList<Integer> minSurvival = new ArrayList<>(), maxSurvival = new ArrayList<>();

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
        minRule.setBirth(new ArrayList<>(new HashSet<>(minBirth)));
        minRule.setSurvival(new ArrayList<>(new HashSet<>(minSurvival)));

        IntegerHROT maxRule = (IntegerHROT) this.clone();
        maxRule.setBirth(new ArrayList<>(new HashSet<>(maxBirth)));
        maxRule.setSurvival(new ArrayList<>(new HashSet<>(maxSurvival)));

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
     * This method returns the neighbourhood of a given cell at a certain generation
     * @param generation The generation of the simulation
     * @return A list of Coordinates that represent the neighbourhood
     */
    @Override
    public Coordinate[] getNeighbourhood(int generation) {
        return neighbourhood;
    }

    /**
     * Gets the weights of the rule
     * @return Weights of the rule
     */
    public int[] getWeights() {
        return weights;
    }

    /**
     * The birth conditions of the rule (e.g. {2, 3})
     * @return Birth conditions of the rule
     */
    public ArrayList<Integer> getBirth() {
        return birth;
    }

    /**
     * The survival conditions of the rule (e.g. {2, 3})
     * @return Survival conditions of the rule
     */
    public ArrayList<Integer> getSurvival() {
        return survival;
    }

    /**
     * Sets the birth conditions of the rule
     * @param birth Birth conditions of the rule
     */
    public void setBirth(ArrayList<Integer> birth) {
        this.birth.clear();
        this.birth.addAll(birth);
        Collections.sort(this.birth);

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
    }

    /**
     * Sets the survival conditions of the rule
     * @param survival Birth conditions of the rule
     */
    public void setSurvival(ArrayList<Integer> survival) {
        this.survival.clear();
        this.survival.addAll(survival);
        Collections.sort(this.survival);

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
    }

    /**
     * Sets the neighbourhood of the rule
     * @param neighbourhood Neighbourhood of the rule
     */
    public void setNeighbourhood(Coordinate[] neighbourhood) {
        this.neighbourhood = neighbourhood;
        updateBackground();
    }

    /**
     * Sets the weights of the rule
     * @param weights Weights of the rule
     */
    public void setWeights(int[] weights) {
        this.weights = weights;
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

    /**
     * This method represents the transition function of the rule
     * @param neighbours The cell's neighbours in the order of the neighbourhood provided
     * @param cellState The current state of the cell
     * @param generations The current generation of the simulation
     * @return The state of the cell in the next generation
     */
    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations) {
        int sum = 0;
        for (int i = 0; i < neighbours.length; i++) {
            if (weights != null) {
                sum += neighbours[i] * weights[i];
            }
            else {
                sum += neighbours[i];
            }
        }

        if (cellState == 0) {  // Check Birth
            for (int transition: birth) {  // Checking divisibility
                if (sum % transition == 0 && sum != 0 && sum / transition < numStates) {
                    return sum / transition;
                }
            }
        }
        else {  // Checking survival
            for (int transition: survival) {  // Checking divisibility
                if (transition * cellState <= sum && sum < (transition + 1) * cellState) {
                    return cellState;
                }
            }
        }

        return 0;
    }
}
