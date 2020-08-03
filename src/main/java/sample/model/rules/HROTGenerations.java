package sample.model.rules;

import sample.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

public class HROTGenerations extends RuleFamily {
    private final HashSet<Integer> birth;
    private final HashSet<Integer> survival;
    private Coordinate[] neighbourhood;
    private int[] weights;

    private final static String hrotTransitions = "(((\\d,(?=\\d))|(\\d-(?=\\d))|\\d)+)?";
    private final static String higherRangePredefined = "R[0-9]+,C[0-9]+,S" + hrotTransitions + ",B" + hrotTransitions +
            ",N[" + NeighbourhoodGenerator.neighbourhoodSymbols + "]";
    private final static String higherRangeCustom = "R[0-9]+,C[0-9]+,S" + hrotTransitions +
            ",B" + hrotTransitions + ",N@([A-Fa-f0-9]+)?";

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

    @Override
    public void fromRulestring(String rulestring) {
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

        if (birth.contains(0)) {
            alternatingPeriod = 2;
        }
    }

    @Override
    public String canonise(String rulestring) {
        // Define Regexes
        String newRulestring = "";
        StringBuilder rulestringBuilder = new StringBuilder(newRulestring);

        if (rulestring.matches(higherRangeCustom) || rulestring.matches(higherRangePredefined)) {
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
    public String[] getRegex() {
        return new String[]{higherRangeCustom, higherRangePredefined};
    }

    @Override
    public String getDescription() {
        return "This implements the Higher Range Outer Totalistic (HROT) generations rulespace.\n" +
                "It supports arbitrary neighbourhoods via the CoordCA format (Specify with N@).\n" +
                "B0 support and state weight suppport is planned.\n\n" +
                "The format is as follows:\n" +
                "R<range>,C<numStates>,S<survival>,B<birth>,N@<CoordCA> or\n" +
                "R<range>,C<numStates>,S<survival>,B<birth>,N<ABCHMNX23*+#>\n\n" +
                "Examples:\n" +
                "R1,C2,S1-2,B3-4,NM (Frogs)\n" +
                "R2,C2,S1-2,B3-4,N@22A544 (Skew Frogs)";
    }

    @Override
    public void randomise(RuleFamily minRule, RuleFamily maxRule) throws IllegalArgumentException {
        if (minRule instanceof HROTGenerations && maxRule instanceof HROTGenerations) {
            Utils.randomiseTransitions(birth, ((HROTGenerations) minRule).birth, ((HROTGenerations) maxRule).birth);
            Utils.randomiseTransitions(survival, ((HROTGenerations) minRule).survival, ((HROTGenerations) maxRule).survival);
        }
        else {
            throw new IllegalArgumentException("The rule families selected have to be the same!");
        }
    }

    @Override
    public boolean generateApgtable(File file) throws UnsupportedOperationException {
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

    @Override  // Accessors
    public Coordinate[] getNeighbourhood(int generation) {
        return neighbourhood;
    }

    public int[] getWeights() {
        return weights;
    }

    public HashSet<Integer> getBirth() {
        return birth;
    }

    public HashSet<Integer> getSurvival() {
        return survival;
    }

    // Mutators
    public void setNeighbourhood(Coordinate[] neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public void setWeights(int[] weights) {
        this.weights = weights;
    }

    @Override  // Clones the object
    public Object clone() {
        HROTGenerations newRule = new HROTGenerations(rulestring);
        newRule.setWeights(getWeights());
        newRule.setNeighbourhood(getNeighbourhood(0).clone());

        return newRule;
    }

    @Override  // Transition function
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

    @Override
    public void step(Grid grid, HashSet<Coordinate> cellsChanged, int generation) {
        Grid gridCopy = grid.deepCopy();
        HashSet<Coordinate> cellsToCheck = new HashSet<>();
        Coordinate[] neighbourhood = getNeighbourhood(generation);

        // Generate set of cells to run update function on
        // Use a set to avoid duplicates
        for (Coordinate cell: cellsChanged) {
            for (Coordinate neighbour: neighbourhood) {
                cellsToCheck.add(cell.add(neighbour));
            }
            cellsToCheck.add(cell);
        }

        // Clear the cells changed
        cellsChanged.clear();

        int[] neighbours;
        int neighbourhood_size = neighbourhood.length, new_state, prev_state;
        for (Coordinate cell: cellsToCheck) {
            prev_state = gridCopy.getCell(cell);

            if (prev_state <= 1) {
                // Getting neighbour states
                neighbours = new int[neighbourhood_size];
                for (int i = 0; i < neighbourhood_size; i++) {
                    neighbours[i] = gridCopy.getCell(cell.add(neighbourhood[i]));
                }

                // Call the transition function on the new state
                new_state = transitionFunc(neighbours, prev_state, generation);
            }
            else {  // Slight optimisation specific to generations rules
                new_state = (prev_state + 1) % numStates;
            }

            if (new_state != prev_state) {
                cellsChanged.add(cell);
                grid.setCell(cell, new_state);
            }
        }
    }
}
