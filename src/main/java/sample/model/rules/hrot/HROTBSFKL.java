package sample.model.rules.hrot;

import org.javatuples.Pair;
import sample.model.Coordinate;
import sample.model.NeighbourhoodGenerator;
import sample.model.Utils;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.MinMaxRuleable;
import sample.model.rules.RuleFamily;
import sample.model.rules.ruleloader.RuleDirective;
import sample.model.rules.ruleloader.ruletable.Ruletable;
import sample.model.rules.ruleloader.ruletable.Variable;
import sample.model.rules.ruleloader.ruletree.RuleTreeGen;
import sample.model.simulation.Grid;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Implements the HROT BSFKL rule family
 */
public class HROTBSFKL extends BaseHROT implements ApgtableGeneratable, MinMaxRuleable {
    /**
     * The birth conditions of the HROT BSFKL rule
     */
    private HashSet<Integer> birth;

    /**
     * The survival conditions of the HROT BSFKL rule
     */
    private HashSet<Integer> survival;

    /**
     * The birth condition of the HROT BSFKL rule
     */
    private HashSet<Integer> forcing;

    /**
     * The birth condition of the HROT BSFKL rule
     */
    private HashSet<Integer> killing;

    /**
     * The living condition of the HROT BSFKL rule
     */
    private HashSet<Integer> living;

    private final static String hrot = "R[0-9]+,B" + hrotTransitions + ",S" + hrotTransitions +
            ",F" + hrotTransitions + ",K" + hrotTransitions + ",L" + hrotTransitions + neighbourhoodRegex;

    /**
     * Creates a HROT BSFKL rule
     */
    public HROTBSFKL() {
        this("R1,B3,4,7,S3,4,5,6,F0,3,8,K2,3,4,5,6,7,L0,3,4,6,NM");
    }

    /**
     * Creates a HROT BSFKL rule with the given rulestring
     * @param rulestring The rulestring of the HROT BSFKL rule to be created
     * @throws IllegalArgumentException Thrown if the rulestring is invalid
     */
    public HROTBSFKL(String rulestring) {
        // Initialise variables
        numStates = 3;
        alternatingPeriod = 1;
        name = "HROT BSFKL";

        birth = new HashSet<>();
        survival = new HashSet<>();
        forcing = new HashSet<>();
        killing = new HashSet<>();
        living = new HashSet<>();

        // Load rulestring
        setRulestring(rulestring);
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the HROT rule (eg. B3/S23, R2,C2,S6-9,B7-8,NM)
     * @throws IllegalArgumentException Thrown if an invalid rulestring is passed in
     */
    @Override
    protected void fromRulestring(String rulestring) {
        // Clear parameters
        birth.clear();
        survival.clear();
        forcing.clear();
        killing.clear();
        living.clear();

        if (rulestring.matches(hrot)) {
            // Generate Neighbourhood
            int range = Integer.parseInt(Utils.matchRegex("R[0-9]+", rulestring, 0).substring(1));
            loadNeighbourhood(range, getNeighbourhoodSpecifier(rulestring));

            // Get transitions
            Utils.getTransitionsFromStringWithCommas(birth,
                    Utils.matchRegex("B" + hrotTransitions, rulestring, 0).substring(1));
            Utils.getTransitionsFromStringWithCommas(survival,
                    Utils.matchRegex("S" + hrotTransitions, rulestring, 0).substring(1));
            Utils.getTransitionsFromStringWithCommas(forcing,
                    Utils.matchRegex("F" + hrotTransitions, rulestring, 0).substring(1));
            Utils.getTransitionsFromStringWithCommas(killing,
                    Utils.matchRegex("K" + hrotTransitions, rulestring, 0).substring(1));
            Utils.getTransitionsFromStringWithCommas(living,
                    Utils.matchRegex("L" + hrotTransitions, rulestring, 0).substring(1));

            // Update the background
            updateBackground();
        } else {
            throw new IllegalArgumentException("This rulestring is invalid!");
        }
    }

    /**
     * Canonises the inputted rulestring with the currently loaded parameters.
     * @param rulestring The rulestring to canonised
     * @return Canonised rulestring
     */
    @Override
    public String canonise(String rulestring) {
        StringBuilder rulestringBuilder = new StringBuilder();
        rulestringBuilder.append(Utils.matchRegex("R[0-9]+,", rulestring, 0));

        // Adding Birth
        rulestringBuilder.append("B").append(Utils.canoniseTransitionsWithCommas(birth));

        // Adding Survival
        rulestringBuilder.append("S").append(Utils.canoniseTransitionsWithCommas(survival));

        // Adding Forcing
        rulestringBuilder.append("F").append(Utils.canoniseTransitionsWithCommas(forcing));

        // Adding Killing
        rulestringBuilder.append("K").append(Utils.canoniseTransitionsWithCommas(killing));

        // Adding Living
        rulestringBuilder.append("L").append(Utils.canoniseTransitionsWithCommas(living));

        // Adding neighbourhood
        rulestringBuilder.append(getNeighbourhoodSpecifier(rulestring));
        return rulestringBuilder.toString();
    }

    /**
     * The regexes that will match a valid rulestring
     * @return An array of regexes that will match a valid rulestring
     */
    @Override
    public String[] getRegex() {
        return new String[]{hrot};
    }

    /**
     * Returns a plain text description of the HROT BSFKL rule family to be displayed in the Rule Dialog
     * @return Description of the HROT BSFKL rule family
     */
    @Override
    public String getDescription() {
        return "This implements the Higher-range Outer Totalistic (HROT) BSFKL rulespace.\n" +
                "It supports arbitrary neighbourhoods via the CoordCA format (Specify with N@).\n" +
                "It supports arbitrary weighted neighbourhoods via the LV format (Specify with NW).\n" +
                "It supports B0 rules via emulation by alternating rules.\n\n" +
                "The format is as follows:\n" +
                "R<range>,B<birth>,S<survival>,F<forcing>,K<killing>,L<living>,N@<CoordCA> or\n" +
                "R<range>,B<birth>,S<survival>,F<forcing>,K<killing>,L<living>,NW<Weights> or\n" +
                "R<range>,B<birth>,S<survival>,F<forcing>,K<killing>,L<living>," +
                "N<" + NeighbourhoodGenerator.neighbourhoodSymbols + ">\n\n";
    }

    /**
     * Generates an apgtable for apgsearch to use
     * @return Returns an array of rule directives to be placed in the ruletable
     */
    @Override
    public RuleDirective[] generateApgtable() {
        if (weights != null) {
            // Generating the ruletree
            RuleTreeGen ruleTreeGen = new RuleTreeGen(numStates, neighbourhood, (neighbours, cellState) ->
                    transitionFunc(cellState, neighbours, 0, new Coordinate()));
            return new RuleDirective[]{ruleTreeGen.getRuleTree()};
        } else {
            // Generating the ruletable
            Ruletable ruletable = new Ruletable("");

            ruletable.setPermute();  // Enable permute symmetry
            ruletable.setNumStates(3);

            ruletable.setNeighbourhood(neighbourhood);
            ruletable.addVariable(Ruletable.ANY);

            HashSet<Integer> not1 = new HashSet<>(Arrays.asList(0, 2));
            HashSet<Integer> not2 = new HashSet<>(Arrays.asList(0, 1));
            ruletable.addVariable(new Variable("not1", true, not1));
            ruletable.addVariable(new Variable("not2", true, not2));

            // Birth and forcing transitions
            for (int birthTransition: birth) {
                for (int forcingTransition: forcing) {
                    if (birthTransition + forcingTransition > neighbourhood.length) continue;
                    ruletable.addOTTransition(Arrays.asList(birthTransition, forcingTransition),
                            "0", "1", Arrays.asList("0", "1", "2"));
                }
            }

            // Killing transitions
            ruletable.addOTTransitions(killing, "1", "0", "not2", "2");

            // Survival Transitions
            ruletable.addOTTransitions(survival, "1", "1", "not1", "1");

            // Living Transitions
            ruletable.addOTTransitions(living, "2", "0", "not1", "1");

            // Death transitions
            ruletable.addOTTransition(0, "1", "2", "any", "0");

            return new RuleDirective[]{ruletable};
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
        if (validMinMax(minRule, maxRule)) {
            Utils.randomiseTransitions(birth, ((HROTBSFKL) minRule).getBirth(), ((HROTBSFKL) maxRule).getBirth());
            Utils.randomiseTransitions(survival, ((HROTBSFKL) minRule).getSurvival(), ((HROTBSFKL) maxRule).getSurvival());
            Utils.randomiseTransitions(forcing, ((HROTBSFKL) minRule).getForcing(), ((HROTBSFKL) maxRule).getForcing());
            Utils.randomiseTransitions(killing, ((HROTBSFKL) minRule).getKilling(), ((HROTBSFKL) maxRule).getKilling());
            Utils.randomiseTransitions(living, ((HROTBSFKL) minRule).getLiving(), ((HROTBSFKL) maxRule).getLiving());

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
    public Pair<RuleFamily, RuleFamily> getMinMaxRule(Grid[] grids) {
        // TODO (Fix BSFKL rule range generation)
        HashSet<Integer> minBirth = new HashSet<>(), maxBirth = new HashSet<>();
        HashSet<Integer> minSurvival = new HashSet<>(), maxSurvival = new HashSet<>();
        HashSet<Integer> minForcing = new HashSet<>(), maxForcing = new HashSet<>();
        HashSet<Integer> minKilling = new HashSet<>(), maxKilling = new HashSet<>();
        HashSet<Integer> minLiving = new HashSet<>(), maxLiving = new HashSet<>();

        // Populate maxBirth & maxSurvival with numbers from 0 - max neighbour sum
        for (int i = 0; i < maxNeighbourhoodCount + 1; i++) {
            maxBirth.add(i);
            maxSurvival.add(i);
            maxForcing.add(i);
            maxKilling.add(i);
            maxLiving.add(i);
        }

        // Running through every generation and check what transitions are required
        int sum1, sum2;
        for (int[] neighbours: getNeighbourList(grids)) {
            sum1 = 0;
            sum2 = 0;

            // Computes the neighbourhood sum for every cell
            for (int i = 1; i < neighbours.length - 1; i++) {
                if (neighbours[i] == 1) {
                    sum1 += weights == null ? 1 : weights[i - 1];
                } else if (neighbours[i] == 2) {
                    sum2 += weights == null ? 1 : weights[i - 1];
                }
            }

            // Determining the required birth / survival condition
            int currentCell = neighbours[0];
            int nextCell = neighbours[neighbours.length - 1];

            if (currentCell == 0 && nextCell == 1) {  // Birth (0 -> 1)
                minBirth.add(sum1);
                minForcing.add(sum2);
            }
            else if (currentCell == 0 && nextCell == 0) {  // No Birth (0 -> 0)
                maxBirth.remove(sum1);
                maxForcing.remove(sum2);
            }
            else if (currentCell == 1 && nextCell == 1) {  // Survival (1 -> 1)
                minSurvival.add(sum1);
                maxKilling.remove(sum2);
            }
            else if (currentCell == 1 && nextCell == 0) {  // Killing (1 -> 0)
                minKilling.add(sum2);
            }
            else if (currentCell == 1 && nextCell == 2) {  // No Survival / Killing (1 -> 2)
                maxSurvival.remove(sum1);
                maxKilling.remove(sum2);
            }
            else if (currentCell == 2 && nextCell == 0) {  // Living (2 -> 0)
                minLiving.add(sum1);
            }
            else if (currentCell == 2 && nextCell == 2) {  // No Living (2 -> 2)
                maxLiving.remove(sum1);
            }
        }

        // Construct the new rules and return them
        HROTBSFKL minRule = (HROTBSFKL) this.clone();
        minRule.setBirth(minBirth);
        minRule.setSurvival(minSurvival);
        minRule.setForcing(minForcing);
        minRule.setKilling(minKilling);
        minRule.setLiving(minLiving);

        HROTBSFKL maxRule = (HROTBSFKL) this.clone();
        maxRule.setBirth(maxBirth);
        maxRule.setSurvival(maxSurvival);
        maxRule.setForcing(maxForcing);
        maxRule.setKilling(maxKilling);
        maxRule.setLiving(maxLiving);

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
            return Utils.checkSubset(((HROTBSFKL) minRule).getBirth(), this.getBirth()) &&
                    Utils.checkSubset(((HROTBSFKL) minRule).getSurvival(), this.getSurvival()) &&
                    Utils.checkSubset(((HROTBSFKL) minRule).getForcing(), this.getForcing()) &&
                    Utils.checkSubset(((HROTBSFKL) minRule).getKilling(), this.getKilling()) &&
                    Utils.checkSubset(((HROTBSFKL) minRule).getLiving(), this.getLiving()) &&
                    Utils.checkSubset(this.getBirth(), ((HROTBSFKL) maxRule).getBirth()) &&
                    Utils.checkSubset(this.getSurvival(), ((HROTBSFKL) maxRule).getSurvival()) &&
                    Utils.checkSubset(this.getForcing(), ((HROTBSFKL) maxRule).getForcing()) &&
                    Utils.checkSubset(this.getKilling(), ((HROTBSFKL) maxRule).getKilling()) &&
                    Utils.checkSubset(this.getLiving(), ((HROTBSFKL) maxRule).getLiving());
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
        if (minRule instanceof HROTBSFKL && maxRule instanceof HROTBSFKL) {
            return Utils.checkSubset(((HROTBSFKL) minRule).getBirth(), ((HROTBSFKL) maxRule).getBirth()) &&
                    Utils.checkSubset(((HROTBSFKL) minRule).getSurvival(), ((HROTBSFKL) maxRule).getSurvival()) &&
                    Utils.checkSubset(((HROTBSFKL) minRule).getForcing(), ((HROTBSFKL) maxRule).getForcing()) &&
                    Utils.checkSubset(((HROTBSFKL) minRule).getKilling(), ((HROTBSFKL) maxRule).getKilling()) &&
                    Utils.checkSubset(((HROTBSFKL) minRule).getLiving(), ((HROTBSFKL) maxRule).getLiving());
        }

        return false;
    }

    /**
     * Gets the birth conditions of the HROT BSFKL rule
     * @return Returns the birth conditions of the HROT BSFKL rule
     */
    public HashSet<Integer> getBirth() {
        return birth;
    }

    /**
     * Gets the survival conditions of the HROT BSFKL rule
     * @return Returns the survival conditions of the HROT BSFKL rule
     */
    public HashSet<Integer> getSurvival() {
        return survival;
    }

    /**
     * Gets the forcing conditions of the HROT BSFKL rule
     * @return Returns the forcing conditions of the HROT BSFKL rule
     */
    public HashSet<Integer> getForcing() {
        return forcing;
    }

    /**
     * Gets the killing conditions of the HROT BSFKL rule
     * @return Returns the killing conditions of the HROT BSFKL rule
     */
    public HashSet<Integer> getKilling() {
        return killing;
    }

    /**
     * Gets the living conditions of the HROT BSKFL rule
     * @return Returns the living conditions of the HROT BSFKL rule
     */
    public HashSet<Integer> getLiving() {
        return living;
    }

    /**
     * Sets the birth conditions of the HROT BSFKL rule
     * @param birth The birth conditions
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
     * Sets the survival conditions of the HROT BSFKL rule
     * @param survival The survival conditions
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
     * Sets the forcing conditions of the HROT BSFKL rule
     * @param forcing The forcing conditions
     */
    public void setForcing(HashSet<Integer> forcing) {
        this.forcing.clear();
        this.forcing.addAll(forcing);

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
    }

    /**
     * Sets the killing conditions of the HROT BSFKL rule
     * @param killing The killing conditions
     */
    public void setKilling(HashSet<Integer> killing) {
        this.killing.clear();
        this.killing.addAll(killing);

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
    }

    /**
     * Sets the living conditions of the HROT BSFKL rule
     * @param living The living conditions
     */
    public void setLiving(HashSet<Integer> living) {
        this.living.clear();
        this.living.addAll(living);

        // Updating rulestring
        this.rulestring = canonise(rulestring);

        // Updating the background
        updateBackground();
    }

    @Override
    public Object clone() {
        return new HROTBSFKL(getRulestring());
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        int sum1 = 0, sum2 = 0;
        for (int i = 0; i < neighbours.length; i++) {
            if (neighbours[i] == 1) {
                sum1 += weights == null ? 1 : weights[i];
            } else if (neighbours[i] == 2) {
                sum2 += weights == null ? 1 : weights[i];
            }
        }

        if (cellState == 1) {
            if (killing.contains(sum2)) return 0;
            else if (survival.contains(sum1)) return 1;
            else return 2;
        } else if (cellState == 2) {
            if (living.contains(sum1)) return 0;
            return 2;
        } else {
            if (birth.contains(sum1) && forcing.contains(sum2)) return 1;
            return 0;
        }
    }
}
