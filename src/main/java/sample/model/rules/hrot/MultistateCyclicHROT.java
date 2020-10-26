package sample.model.rules.hrot;

import sample.model.Coordinate;
import sample.model.NeighbourhoodGenerator;
import sample.model.Utils;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.ruleloader.RuleDirective;
import sample.model.rules.ruleloader.ruletable.Ruletable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the multi-state cyclic HROT rulespace
 *
 * TODO (Support weights and arbitary neighbourhoods)
 */
public class MultistateCyclicHROT extends BaseHROT implements ApgtableGeneratable {
    private HashMap<ArrayList<Integer>, Integer> transitions;

    private final static String mooreRegex = "B([0-8]|l(-[0-8])*)*/(M([0-8]|l(-[0-8])*)*/)+" +
            "S([0-8]|l(-[0-8])*)*/C[0-9]+";
    private final static String hrotRegex =
            "R[0-9]+,C[0-9]+,B([0-9]+|l(-[0-9]+)+,?)*,(M([0-9]+|l(-[0-9]+)+,?)*,)+S([0-9]+|l(-[0-9]+)+,?)*,";
    private final static String higherRangePredefined = hrotRegex + "N[" +
            NeighbourhoodGenerator.neighbourhoodSymbols + "]";
    private final static String higherRangeCustom = hrotRegex + "N@([A-Fa-f0-9]+)?[HL]?";
    private final static String higherRangeWeightedCustom = hrotRegex + "NW[A-Fa-f0-9]+[HL]?";

    /**
     * Constructs Yoel's Gluonic rule
     */
    public MultistateCyclicHROT() {
        this("B002021/M/M/S000011300030003120012201210021102111/C4");
    }

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
            neighbourhood = NeighbourhoodGenerator.generateMoore(1);
            numStates = Integer.parseInt(Utils.matchRegex("C([0-9]+)", rulestring, 0).substring(1));

            String birthString = Utils.matchRegex("B([0-8]|l(-[0-8])+)*", rulestring, 0).substring(1);
            String survivalString = Utils.matchRegex("S([0-8]|l(-[0-8])+)*", rulestring, 0).substring(1);

            parseString(0, 1, birthString);
            parseString(1, 1, survivalString);
            int counter = 0;
            Matcher mutateMatcher = Pattern.compile("M([0-8]|l(-[0-8])+)*/").matcher(rulestring);
            while (mutateMatcher.find()) {
                String mutate = mutateMatcher.group(1);
                if (mutate != null) parseString(1, counter, mutate);
                counter++;
            }
        }
    }

    @Override
    public String canonise(String rulestring) {
        return rulestring;
    }

    @Override
    public String[] getRegex() {
        return new String[]{mooreRegex};
    }

    @Override
    public String getDescription() {
        return "This implements the multi-state cyclic HROT rulespace.\n" +
                "It does not support B0 rules because they are not cyclic.\n\n" +
                "The format is as follows:\n" +
                "B<birth>/M<mutate>/M<mutate2>/M.../S<survival>/C<states>\n" +
                "R<range>,C<states>,B<birth>,M<mutate>,M<mutate2>,...,S<survival>,N<neighbourhood>\n\n" +
                "Examples:\n" +
                "B002021/M/M/S000300030003120012201210021102111/C4 (Gluonic)\n" +
                "B002/M/M/S000011l-0l-0l-0/C4 (Gluons)\n" +
                "B30/M/S2030ll-0/C3 (Symbiosis)\n";
    }

    @Override
    public RuleDirective[] generateApgtable() {
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

    private void parseString(int input, int output, String string) {
        int counter = 0;
        ArrayList<ArrayList<Integer>> transition = null;

        int i = 0;
        String transString;
        String[] tokens;
        Set<String> tokenSet;
        Matcher matcher = Pattern.compile("l(-[0-8])*|[0-8]").matcher(string);
        while (matcher.find()) {
            transString = matcher.group();
            if (transString.matches("[0-8]")) {
                if (counter == 0) {
                    if (transition != null) {
                        for (ArrayList<Integer> trans: transition) addTransition(trans, output);
                    }

                    // Resetting the transition
                    transition = new ArrayList<>();
                    transition.add(new ArrayList<>());
                    transition.get(0).add(input);  // Add the input state
                }

                for (ArrayList<Integer> trans: transition) trans.add(Integer.parseInt(string.charAt(i) + ""));
            } else {  // TODO (Fix l-[x] syntax)
                tokens = transString.substring(2).split("-");
                tokenSet = new HashSet<>(Arrays.asList(tokens));

                ArrayList<Integer> transClone;
                ArrayList<ArrayList<Integer>> transitionClone = (ArrayList<ArrayList<Integer>>) transition.clone();
                transition.clear();

                for (ArrayList<Integer> trans: transitionClone) {
                    for (int j = 0; j < neighbourhood.length; j++) {
                        if (tokenSet.contains(j + "")) continue;
                        transClone = (ArrayList<Integer>) trans.clone();
                        transClone.add(j);
                        transition.add(transClone);
                    }
                }
            }

            i++;
            counter++;  // Cycle from 0 - numStates - 1
            counter %= numStates - 1;
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

            transitions.put(transitionClone, (output + i) % numStates);
        }
    }
}
