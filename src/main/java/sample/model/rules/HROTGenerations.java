package sample.model.rules;

import org.javatuples.Pair;
import sample.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Represents the HROT Generations rule family
 */
public class HROTGenerations extends RuleFamily {
    /**
     * The birth conditions of the HROT generations rule
     */
    private final HashSet<Integer> birth;

    /**
     * The survival conditions of the HROT generations rule
     */
    private final HashSet<Integer> survival;

    /**
     * The neighbourhood of the HROT generations rule
     */
    private Coordinate[] neighbourhood;

    /**
     * The neighbourhood weights of the HROT generations rule.
     * For example, {1, 2, 1, 2, 0, 2, 1, 2, 1}
     */
    private int[] weights;

    /**
     * The maximum possible neighbourhood count.
     * Used for B0 and min, max rule generation.
     */
    private int maxNeighbourhoodCount;

    private final static String hrotTransitions = "(((\\d,(?=\\d))|(\\d-(?=\\d))|\\d)+)?";
    private final static String higherRangePredefined = "R[0-9]+,C[0-9]+,S" + hrotTransitions + ",B" +
            hrotTransitions + ",N[" + NeighbourhoodGenerator.neighbourhoodSymbols + "]";
    private final static String higherRangeCustom = "R[0-9]+,C[0-9]+,S" + hrotTransitions +
            ",B" + hrotTransitions + ",N@([A-Fa-f0-9]+)?";
    private final static String higherRangeWeightedCustom = "R[0-9]+,C[0-9]+,S" + hrotTransitions +
            ",B" + hrotTransitions + ",NW[A-Fa-f0-9]+";

    /**
     * Creates a HROT Generations rule with the rule Frogs
     */
    public HROTGenerations() {
        this("R1,C4,S1-2,B3-4,NM");
    }

    /**
     * Creates a HROT Generations rule with the given rulestring
     * @param rulestring The rulestring of the HROT Generations rule to be created
     * @throws IllegalArgumentException Thrown if the rulestring is invalid
     */
    public HROTGenerations(String rulestring) {
        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        name = "HROT Generations";

        birth = new HashSet<>();
        survival = new HashSet<>();

        // Load rulestring
        setRulestring(rulestring);
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the HROT Generations rule (eg. R1,C3,S1-2,B3-4,NM)
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

            // Set the number of states
            numStates = Integer.parseInt(Utils.matchRegex("C[0-9]+", rulestring, 0).substring(1));

            // Get transitions
            Utils.getTransitionsFromStringWithCommas(birth,
                    Utils.matchRegex("B" + hrotTransitions, rulestring, 0).substring(1));
            Utils.getTransitionsFromStringWithCommas(survival,
                    Utils.matchRegex("S" + hrotTransitions, rulestring, 0).substring(1));
        }
        else if (rulestring.matches(higherRangeCustom)) {
            // Generate Neighbourhood
            int range = Integer.parseInt(Utils.matchRegex("R[0-9]+", rulestring, 0).substring(1));
            String CoordCA = Utils.matchRegex("N@([A-Fa-f0-9]+)?", rulestring, 0).substring(2);
            weights = null;
            if (CoordCA.length() > 0)
                neighbourhood = NeighbourhoodGenerator.fromCoordCA(CoordCA, range);

            // Set the number of states
            numStates = Integer.parseInt(Utils.matchRegex("C[0-9]+", rulestring, 0).substring(1));

            // Get transitions
            Utils.getTransitionsFromStringWithCommas(birth,
                    Utils.matchRegex("B" + hrotTransitions, rulestring, 0).substring(1));
            Utils.getTransitionsFromStringWithCommas(survival,
                    Utils.matchRegex("S" + hrotTransitions, rulestring, 0).substring(1));
        }
        else if (rulestring.matches(higherRangeWeightedCustom)) {
            // Generate Neighbourhood
            int range = Integer.parseInt(Utils.matchRegex("R[0-9]+", rulestring, 0).substring(1));
            String LifeViewer = Utils.matchRegex("NW[A-Fa-f0-9]+", rulestring, 0).substring(2);

            Pair<Coordinate[], int[]> neighbourhoodAndWeights =
                    NeighbourhoodGenerator.getNeighbourhoodWeights(LifeViewer, range);
            neighbourhood = neighbourhoodAndWeights.getValue0();
            weights = neighbourhoodAndWeights.getValue1();

            // Set the number of states
            numStates = Integer.parseInt(Utils.matchRegex("C[0-9]+", rulestring, 0).substring(1));

            // Get transitions
            Utils.getTransitionsFromStringWithCommas(birth,
                    Utils.matchRegex("B" + hrotTransitions, rulestring, 0).substring(1));
            Utils.getTransitionsFromStringWithCommas(survival,
                    Utils.matchRegex("S" + hrotTransitions, rulestring, 0).substring(1));
        }
        else {
            throw new IllegalArgumentException("This rulestring is invalid!");
        }

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

        if (rulestring.matches(higherRangeCustom) || rulestring.matches(higherRangePredefined) ||
                rulestring.matches(higherRangeWeightedCustom)) {
            rulestringBuilder.append(Utils.matchRegex("R[0-9]+,C[0-9]+,", rulestring, 0));

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
                    background = new int[]{1};
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
        }

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
     * Returns a plain text description of the HROT Generations rule family to be displayed in the Rule Dialog
     * @return Description of the HROT Generations rule family
     */
    @Override
    public String getDescription() {
        return "This implements the Higher Range Outer Totalistic (HROT) generations rulespace.\n" +
                "It supports arbitrary neighbourhoods via the CoordCA format (Specify with N@).\n" +
                "It supports arbitary weighted neighbourhoods via the LifeViewer format (Specify with NW).\n" +
                "B0 rules are supported via emulation alternating rules.\n" +
                "The format is as follows:\n" +
                "R<range>,C<numStates>,S<survival>,B<birth>," +
                "N<" + NeighbourhoodGenerator.neighbourhoodSymbols + "> or\n" +
                "R<range>,C<numStates>,S<survival>,B<birth>,N@<CoordCA> or\n" +
                "R<range>,C<numStates>,S<survival>,B<birth>,NW<Weights> or\n" +
                "R<range>,C<numStates>,S<survival>,B<birth>,NW<Weights>, <State Weights> or\n" +
                "Examples:\n" +
                "R1,C2,S1-2,B3-4,NM (Frogs)\n" +
                "R2,C2,S1-2,B3-4,N@22A544 (Skew Frogs)";
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
        if (minRule instanceof HROTGenerations && maxRule instanceof HROTGenerations) {
            Utils.randomiseTransitions(birth, ((HROTGenerations) minRule).birth, ((HROTGenerations) maxRule).birth);
            Utils.randomiseTransitions(survival, ((HROTGenerations) minRule).survival,
                    ((HROTGenerations) maxRule).survival);

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
        for (int i = 0; i < grids.length - 1; i++) {
            grids[i].updateBounds();  // Getting the bounds of the grid
            Pair<Coordinate, Coordinate> bounds = grids[i].getBounds();

            int sum;  // Neighbourhood sum
            Coordinate coordinate;  // Current coordinate
            for (int x = bounds.getValue0().getX() - 5; x < bounds.getValue1().getX() + 5; x++) {
                for (int y = bounds.getValue0().getY() - 5; y < bounds.getValue1().getY() + 5; y++) {
                    sum = 0;
                    coordinate = new Coordinate(x, y);

                    // Skip this part if its a dying cell
                    if (grids[i].getCell(coordinate) > 1) {
                        continue;
                    }

                    // Computes the neighbourhood sum for every cell
                    for (int j = 0; j < neighbourhood.length; j++) {
                        if (weights == null) {
                            if (grids[i].getCell(coordinate.add(neighbourhood[j])) == 1)
                                sum += 1;
                        }
                        else {
                            if (grids[i].getCell(coordinate.add(neighbourhood[j])) == 1)
                                sum += weights[j];
                        }
                    }

                    // Determining the required birth / survival condition
                    int currentCell = grids[i].getCell(coordinate);
                    int nextCell = grids[i + 1].getCell(coordinate);

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
                }
            }
        }

        // Construct the new rules and return them
        HROTGenerations minRule = (HROTGenerations) this.clone();
        minRule.setBirth(minBirth);
        minRule.setSurvival(minSurvival);

        HROTGenerations maxRule = (HROTGenerations) this.clone();
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
     * @param file The file to save the apgtable in
     * @return True if the operation was successful, false otherwise
     */
    @Override
    public boolean generateApgtable(File file) {
        try {
            FileWriter writer = new FileWriter(file);

            // Writing header
            String neighbourhoodString = Arrays.toString(neighbourhood);
            writer.write("# This ruletable is automatically generated by CAViewer\n\n");
            writer.write("n_states:" + numStates + "\n");
            writer.write("neighborhood:[(0, 0), " + neighbourhoodString.substring(1,
                    neighbourhoodString.length() - 1) + ", (0, 0)]\n");  // Add the (0, 0) at the front and back

            if (weights == null)
                writer.write("symmetries:permute\n\n");
            else
                writer.write("symmetries:none\n\n");  // Use none symmetry for weighted rules

            // Writing variables for death transitions
            writer.write("# Dying cell variables\n");
            for (int i = 0; i < neighbourhood.length; i++) {
                writer.write("var dying" + i + " = {0, ");
                for (int state = 2; state < numStates; state++) {  // Add all states except 1
                    writer.write("" + state);
                    if (state < numStates - 1) {  // Don't add ", " for the last one
                        writer.write( ", ");
                    }
                }

                writer.write("}\n");
            }

            // Writing variables for death transitions
            writer.write("\n# Variables for Decay Transitions\n");
            for (int i = 0; i < neighbourhood.length; i++) {
                writer.write("var decay" + i + " = {");
                for (int state = 0; state < numStates; state++) {  // Add in every state
                    writer.write("" + state);
                    if (state < numStates - 1) {  // Don't add ", " for the last one
                        writer.write( ", ");
                    }
                }

                writer.write("}\n");
            }

            if (weights == null) {
                // Write in birth transitions
                int counter = 0;
                writer.write("\n# Birth Transitions\n");
                for (int transition: birth) {
                    writer.write("0," + ApgtableGenerator.generateOT(neighbourhood, transition,
                            "1", "dying", false, true) + "1\n");
                    counter++;  // I should use a for loop but whatever
                }

                // Write in survival transitions
                counter = 0;
                writer.write("\n# Survival Transitions\n");
                for (int transition: survival) {
                    writer.write("1," + ApgtableGenerator.generateOT(neighbourhood, transition,
                            "1", "dying", false, true) + "1\n");
                    counter++;  // I should use a for loop but whatever
                }
            }
            else {
                Hashtable<Integer, ArrayList<String>> transitions =  // Generate all possible transitions
                        ApgtableGenerator.generateWeightedTransitions(neighbourhood, weights);

                // Write in birth transitions
                writer.write("\n# Birth Transitions\n");
                for (int transition: birth) {
                    for (String string: transitions.get(transition)) {
                        writer.write("0,");
                        for (int i = 0; i < string.length(); i++) {
                            if (string.charAt(i) == '0')
                                writer.write("dying" + i);
                            else
                                writer.write(1);

                            writer.write(",");
                        }
                        writer.write("1\n");
                    }
                }

                // Write in survival transitions
                writer.write("\n# Survival Transitions\n");
                for (int transition: survival) {
                    for (String string: transitions.get(transition)) {
                        writer.write("1,");
                        for (int i = 0; i < string.length(); i++) {
                            if (string.charAt(i) == '0')
                                writer.write("dying" + i);
                            else
                                writer.write(1);

                            writer.write(",");
                        }
                        writer.write("1\n");
                    }
                }

            }

            // Write in decay transitions
            writer.write("\n# Decay Transitions\n");

            for (int state = 1; state < numStates; state++) {
                writer.write(state + ",");
                for (int i = 0; i < neighbourhood.length; i++) {
                    writer.write("decay" + i + ",");
                }
                writer.write((state + 1) % numStates + "\n");
            }

            writer.close();
            return true;
        }
        catch (IOException exception) {
            return false;
        }
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
            int range = 0;
            ArrayList<Coordinate> neighbourhoodList = new ArrayList<>();
            for (Coordinate coordinate: neighbourhood) {
                neighbourhoodList.add(coordinate);
                range = Math.max(range, Math.max(Math.abs(coordinate.getX()), Math.abs(coordinate.getY())));
            }

            String[] comments = new String[2 * range + 1];  // The array of RLE comments
            for (int i = -range; i <= range; i++) {
                comments[i + range] = "#R ";
                for (int j = -range; j <= range; j++) {
                    int index = neighbourhoodList.indexOf(new Coordinate(i, j));
                    if (index != -1) {
                        comments[i + range] += weights[index];
                    }
                    else {
                        comments[i + range] += 0;
                    }
                    comments[i + range] += " ";
                }
            }

            return comments;
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
            int range = comments.length / 2;
            ArrayList<Coordinate> neighbourhood = new ArrayList<>();
            ArrayList<Integer> weights = new ArrayList<>();

            for (int j = 0; j < comments.length; j++) {  // Parsing comments for the neighbourhood
                String[] tokens = comments[j].split(" ");
                for (int i = 1; i < tokens.length; i++) {
                    if (!tokens[i].equals("0")) {
                        neighbourhood.add(new Coordinate(i - 1 - range, j - range));
                        weights.add(Integer.parseInt(tokens[i]));
                    }
                }
            }

            // Converting to arrays because java is annoying
            int[] weightsArray = new int[weights.size()];
            Coordinate[] neighbourhoodArray = new Coordinate[neighbourhood.size()];
            for (int i = 0; i < weights.size(); i++) {
                weightsArray[i] = weights.get(i);
                neighbourhoodArray[i] = neighbourhood.get(i);
            }

            // Setting weights and neighbourhood
            setWeights(weightsArray);
            setNeighbourhood(neighbourhoodArray);
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
        HROTGenerations newRule = new HROTGenerations(rulestring);
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
            if (neighbours[i] == 1) {
                if (weights != null) {
                    sum += neighbours[i] * weights[i];
                }
                else {
                    sum += neighbours[i];
                }
            }
        }

        if (cellState == 1 && survival.contains(sum)) {  // Check Survival
            return 1;
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

    /**
     * Steps the grid provided forward one generation
     * Includes some generations specific optimisation
     * @param grid The grid that will be stepped forward one generation
     * @param cellsChanged An array of sets that contains the cells the changed in the previous generations.
     *                     The first entry will contains the cells that changed in the previous generation
     *                     and the next entry will contain the cells that changed the previous previous generation
     *                     and so on. It should be the same length as the alternating period of the rule
     * @param generation The current generation of the simulation
     * @throws IllegalArgumentException Thrown if the length of cellsChanged is not the same as the alternating period
     */
    public void step(Grid grid, ArrayList<Set<Coordinate>> cellsChanged, int generation)
            throws IllegalArgumentException {
        if (cellsChanged.size() != alternatingPeriod)
            throw new IllegalArgumentException("cellsChanged parameter should have length " + alternatingPeriod + "!");

        Grid gridCopy = grid.deepCopy();
        HashSet<Coordinate> cellsToCheck = new HashSet<>();
        Coordinate[] neighbourhood = getNeighbourhood(generation);

        // Generate set of cells to run update function on
        // Use a set to avoid duplicates
        for (Set<Coordinate> cellSet: cellsChanged) {
            for (Coordinate cell: cellSet) {
                for (Coordinate neighbour: neighbourhood) {
                    cellsToCheck.add(cell.add(neighbour));
                }
                cellsToCheck.add(cell);
            }
        }

        int[] neighbours;
        int newState, prevState, convertedPrevState;
        for (Coordinate cell: cellsToCheck) {
            prevState = gridCopy.getCell(cell);
            convertedPrevState = convertState(prevState, generation);

            // Skip for dying cells
            if (convertedPrevState <= 1) {
                // Getting neighbour states
                neighbours = new int[neighbourhood.length];
                for (int i = 0; i < neighbourhood.length; i++) {
                    // Converting based on background
                    neighbours[i] = convertState(gridCopy.getCell(cell.add(neighbourhood[i])), generation);
                }

                newState = convertState(transitionFunc(neighbours, convertedPrevState, generation),
                        generation + 1);
            }
            else {
                newState = convertState((convertedPrevState + 1) % numStates, generation + 1);
            }

            // Call the transition function on the new state
            // Don't forget to convert back to the current backgroun
            if (newState != prevState) {
                cellsChanged.get(0).add(cell);
                grid.setCell(cell, newState);
            }
            else {
                for (int i = 0; i < alternatingPeriod; i++) {
                    if (cellsChanged.get(i).contains(cell)) {
                        cellsChanged.get(i).remove(cell);

                        // Move the cell forward into the next entry until it can't be moved forward anymore
                        if (i < alternatingPeriod - 1) cellsChanged.get(i + 1).add(cell);
                        break;
                    }
                }
            }
        }

        /* Old code
        int[] neighbours;
        int prevState, newState;
        Coordinate neighbour;
        HashSet<Coordinate> visited = new HashSet<>();
        HashSet<Coordinate> cellNeighbours = new HashSet<>();

        // Clear the cells changed
        cellsToCheck = (HashSet<Coordinate>) cellsChanged.clone();
        cellsChanged.clear();

        for (Coordinate cell: cellsToCheck) {
            visited.add(cell);

            prevState = gridCopy.getCell(cell);

            // Getting neighbour states
            neighbours = new int[neighbourhood.length];
            for (int i = 0; i < neighbourhood.length; i++) {
                neighbour = cell.add(neighbourhood[i]);
                neighbours[i] = gridCopy.getCell(neighbour);
                if (!cellsToCheck.contains(neighbour))
                    cellNeighbours.add(neighbour);
            }

            // Call the transition function on the new state
            newState = transitionFunc(neighbours, prevState, generation);
            if (newState != prevState) {
                cellsChanged.add(cell);
                grid.setCell(cell, newState);
            }
        }

        for (Coordinate cell: cellNeighbours) {
            visited.add(cell);

            prevState = gridCopy.getCell(cell);

            // Getting neighbour states
            neighbours = new int[neighbourhood.length];
            for (int i = 0; i < neighbourhood.length; i++) {
                neighbour = cell.add(neighbourhood[i]);
                neighbours[i] = gridCopy.getCell(neighbour);
            }

            // Call the transition function on the new state
            newState = transitionFunc(neighbours, prevState, generation);
            if (newState != prevState) {
                cellsChanged.add(cell);
                grid.setCell(cell, newState);
            }
        }
         */
    }
}
