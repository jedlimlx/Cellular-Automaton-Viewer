package application.model.rules.hrot;

import application.model.Coordinate;
import application.model.NeighbourhoodGenerator;
import application.model.Utils;
import application.model.rules.ApgtableGeneratable;
import application.model.rules.ruleloader.RuleDirective;
import application.model.rules.ruleloader.ruletable.Ruletable;
import application.model.rules.ruleloader.ruletree.RuleTreeGen;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the multi-state cyclic HROT rulespace
 */
public class MultistateCyclicHROT extends BaseHROT implements ApgtableGeneratable {
    private final HashMap<ArrayList<Integer>, Integer> transitions;

    private final static String mooreRegex = "B([0-8]|l(-[0-8])*)*/(M([0-8]|l(-[0-8])*)*/)+" +
            "S([0-8]|l(-[0-8])*)*/C[0-9]+";
    private final static String hrotRegex =
            "R[0-9]+,C[0-9]+,B((l(-[0-9]+)*|[0-9]+),?)*,(M((l(-[0-9]+)*|[0-9]+),?)*,)+S((l(-[0-9]+)*|[0-9]+),?)*" +
                    neighbourhoodRegex;

    /**
     * Constructs Yoel's Gluonic rule
     */
    public MultistateCyclicHROT() {
        this("B002021/M/M/S000011300030003120012201210021102111/C4");
    }

    /**
     * Creates a multi-state cyclic HROT rule with the given rulestring
     * @param rulestring The rulestring of the multi-state cyclic HROT rule to be created
     * @throws IllegalArgumentException Thrown if the rulestring is invalid
     */
    public MultistateCyclicHROT(String rulestring) {
        // Initialising variables
        name = "Multi-state Cyclic HROT";
        transitions = new HashMap<>();

        // Loading the rulestring
        setRulestring(rulestring);

        alternatingPeriod = 1;
        updateBackground();
    }

    @Override
    protected void fromRulestring(String rulestring) {
        transitions.clear();

        if (rulestring.matches(mooreRegex)) {
            maxNeighbourhoodCount = 8;
            neighbourhood = NeighbourhoodGenerator.generateMoore(1);
            numStates = Integer.parseInt(Utils.matchRegex("C([0-9]+)", rulestring, 0).substring(1));

            String birthString = Utils.matchRegex("B(l(-[0-8])*|[0-8])*", rulestring, 0).substring(1);
            String survivalString = Utils.matchRegex("S(l(-[0-8])*|[0-8])*", rulestring, 0).substring(1);

            parseString(0, 1, birthString, false);
            parseString(1, 1, survivalString, false);

            int counter = 2;
            Matcher mutateMatcher = Pattern.compile("M(l(-[0-8])*|[0-8])*").matcher(rulestring);
            while (mutateMatcher.find()) {
                String mutate = mutateMatcher.group().substring(1);
                parseString(1, counter, mutate, false);
                counter++;
            }
        } else if (rulestring.matches(hrotRegex)) {
            // Generate Neighbourhood
            int range = Integer.parseInt(Utils.matchRegex("R[0-9]+", rulestring, 0).substring(1));
            loadNeighbourhood(range, getNeighbourhoodSpecifier(rulestring));

            // Load other parameters
            numStates = Integer.parseInt(Utils.matchRegex("C([0-9]+)", rulestring, 0).substring(1));

            String birthString = Utils.matchRegex("B((l(-[0-9]+)*|[0-9]+),?)*", rulestring, 0).substring(1);
            String survivalString = Utils.matchRegex("S((l(-[0-9]+)*|[0-9]+),?)*", rulestring, 0).substring(1);

            parseString(0, 1, birthString, true);
            parseString(1, 1, survivalString, true);

            int counter = 2;
            Matcher mutateMatcher = Pattern.compile("M((l(-[0-9]+)*|[0-9]+),?)*,").matcher(rulestring);
            while (mutateMatcher.find()) {
                String mutate = mutateMatcher.group().substring(1);
                parseString(1, counter, mutate, true);
                counter++;
            }
        } else {
            throw new IllegalArgumentException("This rulestring is invalid!");
        }
    }

    @Override
    public String canonise(String rulestring) {
        return rulestring;
    }

    @Override
    public String[] getRegex() {
        return new String[]{mooreRegex, hrotRegex};
    }

    @Override
    public String getDescription() {
        return "This implements the multi-state cyclic HROT rulespace.\n" +
                "It does not support B0 rules because they are not cyclic.\n\n" +
                "The format is as follows:\n" +
                "B<birth>/M<mutate>/M<mutate2>/M.../S<survival>/C<states>\n" +
                "R<range>,C<states>,B<birth>,M<mutate>,M<mutate2>,...,S<survival>,N<neighbourhood>\n\n" +
                "Examples:\n" +
                "B002021/M/M/S000011300030003120012201210021102111/C4 (Gluonic)\n" +
                "B002/M/M/S000011300030003120012201210021102111/C4 (Gluons)\n" +
                "B30/M/S2030ll-0/C3 (Symbiosis)\n";
    }

    @Override
    public RuleDirective[] generateApgtable() throws UnsupportedOperationException {
        if (weights != null) {
            RuleTreeGen ruleTreeGen = new RuleTreeGen(numStates, neighbourhood, (neighbours, cellState) ->
                    transitionFunc(cellState, neighbours, 0, new Coordinate()));
            return new RuleDirective[]{ruleTreeGen.getRuleTree()};
        }

        Ruletable ruletable = new Ruletable("");

        ruletable.setPermute();  // Enable permute symmetry
        ruletable.setNumStates(numStates);

        ruletable.setNeighbourhood(neighbourhood);

        ruletable.addVariable(Ruletable.ANY);

        ArrayList<String> vars = new ArrayList<>();
        for (int i = 0; i < numStates; i++) vars.add(i + "");

        for (ArrayList<Integer> transition: transitions.keySet()) {
            ruletable.addOTTransition(transition.subList(1, transition.size()),
                    transition.get(0) + "", transitions.get(transition) + "", vars);
        }

        for (int i = 1; i < numStates; i++)
            ruletable.addOTTransition(0, i + "", "0", "any", "0");

        return new RuleDirective[]{ruletable};
    }

    @Override
    public Object clone() {
        return new MultistateCyclicHROT(rulestring);
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        ArrayList<Integer> stateCounts = new ArrayList<>(Collections.nCopies(numStates, 0));
        stateCounts.set(0, cellState);

        for (int neighbour: neighbours) {
            if (neighbour == 0) continue;
            stateCounts.set(neighbour, stateCounts.get(neighbour) + 1);
        }

        Integer output = transitions.get(stateCounts);
        if (output == null) return 0;

        return output;
    }

    private void parseString(int input, int output, String string, boolean withCommas) {
        int counter = 0;
        ArrayList<ArrayList<Integer>> transition = null;

        int index;
        String transString;
        String[] tokens;
        Set<String> tokenSet;

        Matcher matcher;
        if (withCommas) matcher = Pattern.compile("l(-[0-9]+)*|[0-9]+").matcher(string);
        else matcher = Pattern.compile("l(-[0-8])*|[0-8]").matcher(string);
        while (matcher.find()) {
            if (counter == 0) {
                if (transition != null) {
                    for (ArrayList<Integer> trans: transition) addTransition(trans, output);
                }

                // Resetting the transition
                transition = new ArrayList<>();
                transition.add(new ArrayList<>());
                transition.get(0).add(input);  // Add the input state
            }

            transString = matcher.group();
            index = matcher.start();

            if (transString.matches("[0-9]+")) {
                for (ArrayList<Integer> trans: transition) trans.add(Integer.parseInt(string.charAt(index) + ""));
            } else {
                if (transString.length() > 2) tokens = transString.substring(2).split("-");
                else tokens = new String[0];

                tokenSet = new HashSet<>(Arrays.asList(tokens));

                ArrayList<Integer> transClone;
                ArrayList<ArrayList<Integer>> transitionClone = (ArrayList<ArrayList<Integer>>) transition.clone();
                transition.clear();

                for (ArrayList<Integer> trans: transitionClone) {
                    int sum = 0;
                    for (int j = 1; j < trans.size(); j++) sum += trans.get(j);
                    for (int j = 0; j < maxNeighbourhoodCount - sum; j++) {
                        if (tokenSet.contains(j + "")) continue;
                        transClone = (ArrayList<Integer>) trans.clone();
                        transClone.add(j);
                        transition.add(transClone);
                    }
                }
            }

            counter++;  // Cycle from 0 - numStates - 1
            counter %= numStates - 1;
        }

        if (counter == 0) {
            if (transition != null) {
                for (ArrayList<Integer> trans : transition) addTransition(trans, output);
            }
        }
    }

    private void addTransition(ArrayList<Integer> transition, int output) {
        // Cyclic (1 -> 2 -> 3 -> 1)
        for (int i = 0; i < numStates - 1; i++) {
            ArrayList<Integer> transitionClone = new ArrayList<>();
            if (transition.get(0) == 0) transitionClone.add(transition.get(0));
            else transitionClone.add((transition.get(0) + i) % numStates);

            int index;
            for (int j = 1; j < transition.size(); j++) {
                index = j - i;
                if (index < 1) index = transition.size() - 1 + index;

                transitionClone.add(transition.get(index));
            }

            int output2 = (output + i) % (numStates - 1);
            transitions.put(transitionClone, output2 != 0 ? output2 : numStates - 1);
        }
    }
}
