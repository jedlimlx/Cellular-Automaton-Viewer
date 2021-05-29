package application.model.rules.isotropic.rules;

import application.model.Coordinate;
import application.model.Utils;
import application.model.rules.ApgtableGeneratable;
import application.model.rules.isotropic.transitions.INTTransitions;
import application.model.rules.isotropic.transitions.R1MooreINT;
import application.model.rules.ruleloader.RuleDirective;
import application.model.rules.ruleloader.ruletable.Ruletable;
import application.model.rules.ruleloader.ruletable.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Implements deficient isotropic non-totalistic rules
 */
public class DeficientINT extends BaseINT implements ApgtableGeneratable {
    /**
     * The birth transitions of the deficient INT rule
     */
    protected INTTransitions birth;

    /**
     * The survival transitions of the deficient INT rule
     */
    protected INTTransitions survival;

    /**
     * The deficient transitions of the deficient INT rule
     */
    protected INTTransitions deficient;

    /**
     * Maps the transition string to the associated deficient state
     */
    protected Map<String, Integer> stateLookup;

    /**
     * Maps the deficient state to the associated transition string
     */
    protected Map<Integer, String> transitionLookup;

    private static String[] transitionRegexes;
    private static String[] regexes;

    /**
     * Constructs the rule, DeficientLife
     */
    public DeficientINT() {
        this("B3/S23/D");
    }

    /**
     * Constructs a deficient INT rule with the provided rulestring
     * @param rulestring The rulestring to use to construct the deficient INT rule
     */
    public DeficientINT(String rulestring) {
        // Initialise variables
        numStates = 2;
        alternatingPeriod = 1;
        name = "Deficient INT";

        // Generating regexes
        regexes = new String[neighbourhoodLookup.size()];
        transitionRegexes = new String[neighbourhoodLookup.size()];

        int counter = 0;
        for (String string: neighbourhoodLookup.keySet()) {
            transitionRegexes[counter] = "(" + neighbourhoodLookup.get(string).getRegex() + ")*";
            if (string.equals("M")) {
                regexes[counter] = "[BbSsDd]" + transitionRegexes[counter] +
                        "[_/]?[BbSsDd]" + transitionRegexes[counter] +
                        "[_/]?[BbSsDd]" + transitionRegexes[counter];
            } else {
                regexes[counter] = "[BbSsDd]" + transitionRegexes[counter] +
                        "[_/]?[BbSsDd]" + transitionRegexes[counter] +
                        "[_/]?[BbSsDd]" + transitionRegexes[counter] + "[_/]?N?" + string;
            }
            counter++;
        }

        // Load rulestring
        setRulestring(rulestring);
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the INT rule (eg. B2n3/S23-q, B3/S23/NK)
     * @throws IllegalArgumentException Thrown if an invalid rulestring is passed in
     */
    @Override
    protected void fromRulestring(String rulestring) {
        boolean matched = false;
        for (int i = 0; i < regexes.length; i++) {
            if (rulestring.matches(regexes[i])) {
                birth = getINTTransition(rulestring);
                birth.setTransitionString(Utils.matchRegex("[Bb](" + transitionRegexes[i] + ")",
                        rulestring, 0, 1));

                survival = getINTTransition(rulestring);
                survival.setTransitionString(Utils.matchRegex("[Ss](" + transitionRegexes[i] + ")",
                        rulestring, 0, 1));

                deficient = getINTTransition(rulestring);

                String deficientString = Utils.matchRegex("[Dd](" + transitionRegexes[i] + ")",
                        rulestring, 0, 1);
                if (deficientString.length() == 0) deficient = birth;
                else deficient.setTransitionString(deficientString);

                matched = true;
            }
        }

        numStates = 2 + deficient.getSortedTransitionTable().size();

        stateLookup = new HashMap<>();
        transitionLookup = new HashMap<>();

        int counter = 2;
        if (birth instanceof R1MooreINT) {
            String hensel = "cekainyqjrtwz";
            ArrayList<String> transitions = new ArrayList<>(deficient.getSortedTransitionTable());
            transitions.sort((s1, s2) -> {
                if (s1.charAt(0) == s2.charAt(0))
                    return Integer.compare(hensel.indexOf(s1.charAt(1)), hensel.indexOf(s2.charAt(1)));
                return Character.compare(s1.charAt(0), s2.charAt(0));
            });

            for (String transitionString: transitions) {
                stateLookup.put(transitionString, counter);
                transitionLookup.put(counter, transitionString);
                counter++;
            }
        } else {
            for (String transitionString: deficient.getSortedTransitionTable()) {
                stateLookup.put(transitionString, counter);
                transitionLookup.put(counter, transitionString);
                counter++;
            }
        }

        if (matched) updateBackground();
        else throw new IllegalArgumentException("This rulestring is invalid!");
    }

    /**
     * Canonises the inputted rulestring with the currently loaded parameters.
     * @param rulestring The rulestring to canonised
     * @return Canonised rulestring
     */
    @Override
    public String canonise(String rulestring) {
        if (!neighbourhoodString.equals("")) {
            if (birth.canoniseTransitions().equals(deficient.canoniseTransitions())) {
                return "B" + birth.canoniseTransitions() + "/S" + survival.canoniseTransitions() +
                        "/D/N" + neighbourhoodString;
            } else {
                return "B" + birth.canoniseTransitions() + "/S" + survival.canoniseTransitions() +
                        "/D" + deficient.canoniseTransitions() +
                        "/N" + neighbourhoodString;
            }
        }
        else {
            if (birth.canoniseTransitions().equals(deficient.canoniseTransitions()))
                return "B" + birth.canoniseTransitions() + "/S" + survival.canoniseTransitions() + "/D";
            else
                return "B" + birth.canoniseTransitions() + "/S" + survival.canoniseTransitions() +
                        "/D" + deficient.canoniseTransitions();
        }
    }

    /**
     * The regexes that will match a valid rulestring
     * @return An array of regexes that will match a valid rulestring
     */
    @Override
    public String[] getRegex() {
        return regexes;
    }

    /**
     * Returns a plain text description of the Deficient INT rule family to be displayed in the Rule Dialog
     * @return Description of the Deficient INT rule family
     */
    @Override
    public String getDescription() {
        return "This implements Deficient Isotropic Non-Totalistic (INT) rules.\n" +
                "B0 rules are supported via emulation with alternating rules.\n\n" +
                "The format is as follows:\n" +
                "B<birth>/S<survival>/D<deficientTransitions>/N<neighbourhood>";
    }

    /**
     * Generates an apgtable for apgsearch to use
     * @return Returns an array of rule directives to be placed in the ruletable
     */
    @Override
    public RuleDirective[] generateApgtable() {
        // Generating the ruletable
        Ruletable ruletable = new Ruletable("");
        ruletable.setNumStates(numStates);
        ruletable.setNeighbourhood(birth.getNeighbourhood());

        ruletable.addVariable(Ruletable.ANY);

        HashSet<Integer> live = new HashSet<>();
        for (int i = 1; i < numStates; i++) live.add(i);

        Ruletable.LIVE = new Variable("live", true, live);
        ruletable.addVariable(Ruletable.LIVE);

        // Deficient Variables
        HashSet<Integer> deficient;
        INTTransitions deficientTransition;
        for (String transitionString: stateLookup.keySet()) {
            deficient = new HashSet<>();
            for (int i = 1; i < numStates; i++) deficient.add(i);

            deficient.remove(stateLookup.get(transitionString));
            ruletable.addVariable(new Variable("deficient." + transitionString.replace("!", ""),
                    true, deficient));

            deficientTransition = birth.getMinTransition();
            deficientTransition.setTransitionString(transitionString);
            ruletable.addINTTransitions(deficientTransition, "0", stateLookup.get(transitionString) + "",
                    "0", "deficient." + transitionString.replace("!", ""));
        }

        // Birth and survival transitions
        for (int i = 1; i < numStates; i ++) {
            ruletable.addINTTransitions(survival, i + "", "1", "0", "live");
        }

        // Death transitions
        for (int i = 1; i < numStates; i ++) {
            ruletable.addOTTransition(0, i + "", "0", "any", "0");
        }

        return new RuleDirective[]{ruletable};
    }

    /**
     * Sets the birth conditions of the deficient INT rule
     * @param birth The birth conditions
     */
    public void setBirth(INTTransitions birth) {
        this.birth = birth;

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
    }

    /**
     * Sets the survival conditions of the deficient INT rule
     * @param survival The survival conditions
     */
    public void setSurvival(INTTransitions survival) {
        this.survival = survival;

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
    }

    /**
     * Gets the birth conditions of the deficient INT rule
     * @return The birth conditions
     */
    public INTTransitions getBirth() {
        return birth;
    }

    /**
     * Gets the survival conditions of the deficient INT rule
     * @return The survival conditions
     */
    public INTTransitions getSurvival() {
        return survival;
    }

    @Override
    public Coordinate[] getNeighbourhood(int generation) {
        return birth.getNeighbourhood();
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        ArrayList<Integer> neighbourList = new ArrayList<>(neighbours.length);
        HashSet<String> forbiddenTransitions = new HashSet<>(neighbours.length);
        for (int neighbour: neighbours) {
            neighbourList.add(neighbour == 0 ? 0 : 1);
            forbiddenTransitions.add(transitionLookup.get(neighbour));
        }

        Integer birthState;
        if (cellState == 0 && birth.checkTransition(neighbourList) &&
                !forbiddenTransitions.contains(deficient.getTransitionsFromNeighbours(neighbourList))) {
            birthState = stateLookup.get(deficient.getTransitionsFromNeighbours(neighbourList));
            return birthState == null ? 1 : birthState;
        } else if (cellState >= 1 && survival.checkTransition(neighbourList)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String getName(int state) {
        if (state == 0) {
            return "Dead";
        } else if (state == 1) {
            return "Alive";
        } else {
            return "Deficient " + transitionLookup.get(state);
        }
    }

    @Override
    public Object clone() {
        return new DeficientINT(rulestring);
    }
}
