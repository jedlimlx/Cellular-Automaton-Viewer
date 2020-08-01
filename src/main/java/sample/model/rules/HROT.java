package sample.model.rules;

import sample.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class HROT extends RuleFamily {
    private final HashSet<Integer> birth;
    private final HashSet<Integer> survival;
    private Coordinate[] neighbourhood;
    private int[] weights;

    private final static String hrotTransitions = "(((\\d,(?=\\d))|(\\d-(?=\\d))|\\d)+)?";
    private final static String moore = "([BS]([0-8]+)?/?[BS]([0-8]+)?+|[BS]?([0-8]+)?/[BS]?([0-8]+)?)";
    private final static String vonNeumann = "([BS]([0-4]+)?/?[BS]([0-4]+)?|[BS]?([0-4]+)?/[BS]?([0-4]+)?)V";
    private final static String hexagonal = "([BS]([0-6]+)?/?[BS]([0-6]+)?|[BS]?([0-6]+)?/[BS]?([0-6]+)?)H";
    private final static String higherRangePredefined = "R[0-9]+,C[0|2],S" + hrotTransitions + ",B" + hrotTransitions +
            ",N[" + NeighbourhoodGenerator.neighbourhoodSymbols + "]";
    private final static String higherRangeCustom = "R[0-9]+,C[0|2],S" + hrotTransitions +
            ",B" + hrotTransitions + ",N@([A-Fa-f0-9]+)?";

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

    @Override
    public void fromRulestring(String rulestring) {
        // Clear birth and survival
        birth.clear();
        survival.clear();

        if (rulestring.matches(moore)) {
            // Generate Birth & Survival Transitions
            Utils.getTransitionsFromString(birth,
                    Utils.matchRegex("B[0-8]+", rulestring, 0).substring(1));
            Utils.getTransitionsFromString(survival,
                    Utils.matchRegex("S[0-8]+", rulestring, 0).substring(1));

            // Generate Neighbourhood
            neighbourhood = NeighbourhoodGenerator.generateMoore(1);
        }
        else if (rulestring.matches(vonNeumann)) {
            // Add to birth & survival
            Utils.getTransitionsFromString(birth,
                    Utils.matchRegex("B[0-4]+", rulestring, 0).substring(1));
            Utils.getTransitionsFromString(survival,
                    Utils.matchRegex("S[0-4]+", rulestring, 0).substring(1));

            // Generate Neighbourhood
            neighbourhood = NeighbourhoodGenerator.generateVonNeumann(1);
        }
        else if (rulestring.matches(hexagonal)) {
            // Add to birth & survival
            Utils.getTransitionsFromString(birth,
                    Utils.matchRegex("B[0-6]+", rulestring, 0).substring(1));
            Utils.getTransitionsFromString(survival,
                    Utils.matchRegex("S[0-6]+", rulestring, 0).substring(1));

            // Generate Neighbourhood
            neighbourhood = NeighbourhoodGenerator.generateHexagonal(1);
        }
        else if (rulestring.matches(higherRangePredefined)) {
            // Generate Neighbourhood
            int range = Integer.parseInt(Utils.matchRegex("R[0-9]+", rulestring, 0).substring(1));
            char neighbourhoodSymbol = Utils.matchRegex("N["+
                    NeighbourhoodGenerator.neighbourhoodSymbols +"]", rulestring, 0).charAt(1);

            neighbourhood = NeighbourhoodGenerator.generateFromSymbol(neighbourhoodSymbol, range);
            weights = NeighbourhoodGenerator.generateWeightsFromSymbol(neighbourhoodSymbol, range);

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
        else if (rulestring.matches(higherRangeCustom) || rulestring.matches(higherRangePredefined)) {
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

    @Override
    public String[] getRegex() {
        return new String[]{moore, vonNeumann, hexagonal, higherRangeCustom, higherRangePredefined};
    }

    @Override
    public String getDescription() {
        return "This implements the 2 state Higher Range Outer Totalistic (HROT) rulespace.\n" +
                "It supports arbitrary neighbourhoods via the CoordCA format (Specify with N@).\n" +
                "It supports B0 rules via emulation by alternating rules.\n\n" +
                "The format is as follows:\n" +
                "R<range>,C2,S<survival>,B<birth>,N@<CoordCA> or\n" +
                "R<range>,C2,S<survival>,B<birth>,N<ABCHMNX23*+#>\n\n" +
                "Examples:\n" +
                "B36/S23 (High Life)\n" +
                "B2/S34H (Hexagonal Life)\n" +
                "R2,C2,S6-9,B7-8,NM (Minibugs)\n" +
                "R2,C2,S2-3,B3,N@891891 (Far Corners Life)";
    }

    @Override
    public void randomise(RuleFamily minRule, RuleFamily maxRule) throws IllegalArgumentException {
        // TODO (Fine tune the RNG function more)
        if (minRule instanceof HROT && maxRule instanceof HROT) {
            Random random = new Random();

            // Clear the current birth and survival
            birth.clear();
            survival.clear();

            // Adding compulsory transitions
            birth.addAll(((HROT) minRule).getBirth());
            survival.addAll(((HROT) minRule).getSurvival());

            // Remove compulsory transitions
            // Deep copy to avoid affecting the original object
            HashSet<Integer> optionalBirths = (HashSet<Integer>) ((HROT) maxRule).getBirth().clone();
            optionalBirths.removeAll(((HROT) minRule).getBirth());

            HashSet<Integer> optionalSurvival = (HashSet<Integer>) ((HROT) maxRule).getSurvival().clone();
            optionalBirths.removeAll(((HROT) minRule).getSurvival());

            // Add to rule at random
            // TODO (Improve RNG function)
            int transitionProbability = random.nextInt(500) + 250;
            for (int transition: optionalBirths) {
                if (random.nextInt(1000) > transitionProbability) {
                    birth.add(transition);
                }
            }

            // Add to rule at random
            for (int transition: optionalSurvival) {
                if (random.nextInt(1000) > transitionProbability) {
                    survival.add(transition);
                }
            }
        }
        else {
            throw new IllegalArgumentException("The rule families selected have to be the same!");
        }
    }

    @Override
    public boolean generateApgtable(File file) throws UnsupportedOperationException {
        try {
            // Open the file
            FileWriter writer = new FileWriter(file);

            // Writing header
            String neighbourhoodString = Arrays.toString(neighbourhood);
            writer.write("# This ruletable is automatically generated by CAViewer\n\n");

            if (alternatingPeriod == 2)  // Use 3 states to emulate B0
                writer.write("n_states:3\n");
            else
                writer.write("n_states:2\n");

            writer.write("neighborhood:[(0, 0), " + neighbourhoodString.substring(1,
                    neighbourhoodString.length() - 1) + ", (0, 0)]\n");  // Add the (0, 0) at the front and back

            if (weights == null)
                writer.write("symmetries:permute\n\n");
            else
                writer.write("symmetries:none\n\n");  // Use none symmetry for HROT

            if (alternatingPeriod == 2) {  // For B0 rules
                // Writing variables for any state
                writer.write("# Variables to represent any state\n");
                for (int i = 0; i < neighbourhood.length; i++) {
                    writer.write("var any" + i + " = {0, 1, 2}\n");
                }

                // Represents the inverted neighbour counts
                writer.write("\n# Inverted Birth Transitions\n");
                for (int i = 0; i < neighbourhood.length + 1; i++) {
                    if (!birth.contains(i)) {
                        writer.write("0," + ApgtableGenerator.generateOT(neighbourhood, i) + "2\n");
                    }
                }

                writer.write("\n# Inverted Survival Transitions\n");
                for (int i = 0; i < neighbourhood.length + 1; i++) {
                    if (!survival.contains(i)) {
                        writer.write("1," + ApgtableGenerator.generateOT(neighbourhood, i) + "2\n");
                    }
                }

                // Death Transitions
                writer.write("\n# Death Transitions\n");
                writer.write("1,");
                for (int i = 0; i < neighbourhood.length; i++) {
                    writer.write("any" + i + ",");
                }
                writer.write("0\n");

                // Write in (Smax - survival) transitions
                writer.write("\n# (Smax - survival) Transitions\n");
                for (int transition: survival) {
                    writer.write("0," + ApgtableGenerator.generateOT(neighbourhood,
                            neighbourhood.length - transition, 2) + "1\n");
                }

                // Write in (Smax - birth) transitions
                writer.write("\n# (Smax - birth) Transitions\n");
                for (int transition: birth) {
                    writer.write("2," + ApgtableGenerator.generateOT(neighbourhood,
                            neighbourhood.length - transition, 2) + "1\n");
                }

                // Death Transitions
                writer.write("\n# Death Transitions\n");
                writer.write("2,");
                for (int i = 0; i < neighbourhood.length; i++) {
                    writer.write("any" + i + ",");
                }

            }
            else {
                // Writing variables for death transitions
                writer.write("# Variables for Death Transitions\n");
                for (int i = 0; i < neighbourhood.length; i++) {
                    writer.write("var any" + i + " = {0, 1}\n");
                }

                if (weights == null) {
                    // Write in birth transitions
                    writer.write("\n# Birth Transitions\n");
                    for (int transition: birth) {
                        writer.write("0," + ApgtableGenerator.generateOT(neighbourhood, transition) + "1\n");
                    }

                    // Write in survival transitions
                    writer.write("\n# Survival Transitions\n");
                    for (int transition: survival) {
                        writer.write("1," + ApgtableGenerator.generateOT(neighbourhood, transition) + "1\n");
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
                                writer.write(string.charAt(i) + ",");
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
                                writer.write(string.charAt(i) + ",");
                            }
                            writer.write("1\n");
                        }
                    }

                }

                // Write in death transitions
                writer.write("\n# Death Transitions\n");

                writer.write("1,");
                for (int i = 0; i < neighbourhood.length; i++) {
                    writer.write("any" + i + ",");
                }

            }
            writer.write("0");

            // Closing the file
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
                comments[i + range] = "#C ";
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
        HROT newRule = new HROT(rulestring);
        newRule.setWeights(getWeights());
        newRule.setNeighbourhood(getNeighbourhood(0).clone());

        return newRule;
    }

    @Override  // Transition function
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

        if (alternatingPeriod == 1) { // Not B0
            if (cellState == 1 && survival.contains(sum)) {  // Check Survival
                return 1;
            }
            else if (cellState == 0 && birth.contains(sum)) {  // Check Birth
                return 1;
            }
        }
        else {
            if (generations % 2 == 0) {
                // Inverted neighbour counts
                if (cellState == 1 && !survival.contains(sum)) {
                    return 1;
                }
                else if (cellState == 0 && !birth.contains(sum)) {
                    return 1;
                }
            }
            else {
                // Swap Smax - birth and Smax - survival
                if (cellState == 1 && birth.contains(neighbourhood.length - sum)) {
                    return 1;
                }
                else if (cellState == 0 && survival.contains(neighbourhood.length - sum)) {
                    return 1;
                }
            }
        }
        return 0;
    }
}
