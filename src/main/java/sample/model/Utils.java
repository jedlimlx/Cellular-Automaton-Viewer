package sample.model;

import sample.model.rules.RuleFamily;
import sample.model.rules.hrot.*;
import sample.model.rules.hrot.enemies.HROTDeadlyEnemies;
import sample.model.rules.hrot.history.HROTHistory;
import sample.model.rules.hrot.symbiosis.HROTSymbiosis;
import sample.model.rules.isotropic.rules.INT;
import sample.model.rules.isotropic.rules.INTGenerations;
import sample.model.rules.isotropic.rules.energetic.INTEnergetic;
import sample.model.rules.isotropic.rules.history.INTHistory;
import sample.model.rules.misc.AlternatingRule;
import sample.model.rules.misc.OneDimensional;
import sample.model.rules.misc.naive.Orthogonal;
import sample.model.rules.misc.naive.ReadingOrder;
import sample.model.rules.misc.turmites.Turmites;
import sample.model.rules.ruleloader.RuleLoader;
import sample.model.simulation.Simulator;
import sample.model.simulation.bounds.Bounded;
import sample.model.simulation.bounds.BoundedGrid;
import sample.model.simulation.bounds.Torus;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static RuleFamily[] ruleFamilies = {new HROT(), new HROTHistory(), new HROTSymbiosis(),
            new HROTDeadlyEnemies(), new HROTGenerations(), new HROTExtendedGenerations(),
            new HROTRegeneratingGenerations(), new IntegerHROT(), new DeficientHROT(), new MultistateCyclicHROT(),
            new INT(), new INTHistory(), new INTEnergetic(), new INTGenerations(), new OneDimensional(), new Turmites(),
            new RuleLoader(), new AlternatingRule()};
    public static BoundedGrid[] boundedGrids = {new Torus(), new Bounded()};
    public static ReadingOrder[] readingOrders = {new Orthogonal()};

    public static void loadPattern(Simulator simulator, File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        ArrayList<String> tokens = new ArrayList<>();
        while (scanner.hasNextLine()) {
            tokens.add(scanner.nextLine());
        }

        String rulestring = "";
        Pattern rulestringRegex = Pattern.compile("rule\\s*=\\s*\\S+");
        ArrayList<String> comments = new ArrayList<>();  // Comments to feed into RuleFamily.loadComments()

        // Parsing code - Removes headers, comments
        StringBuilder rleFinal = new StringBuilder();
        for (String token: tokens) {
            if (token.length() == 0) continue;
            if (token.startsWith("#R")) {  // Check for comment
                comments.add(token);
            }
            else if (token.charAt(0) == 'x') {  // Check for header
                Matcher rulestringMatcher = rulestringRegex.matcher(token);
                if (rulestringMatcher.find()) {
                    rulestring = rulestringMatcher.group().substring(7);
                }
            }
            else if (token.charAt(0) != '#') {  // Not a comment
                rleFinal.append(token);
            }
        }

        if (!rulestring.equals("")) {
            RuleFamily rule = fromRulestring(rulestring);

            if (rule != null) {
                // Generate the additional information from comments
                String[] commentsArray = new String[comments.size()];
                for (int i = 0; i < commentsArray.length; i++) {
                    commentsArray[i] = comments.get(i);
                }

                rule.loadComments(commentsArray);

                // Set the rulestring
                simulator.setRule(rule);
            }
        }

        // Inserting cells
        simulator.fromRLE(rleFinal.toString(), new Coordinate(0, 0));

        // Setting the generation count back to 0
        simulator.setGeneration(0);
    }

    public static RuleFamily fromRulestring(String rulestring) {
        // Identify the rule family based on regex
        boolean found = false;
        RuleFamily rule = null;
        for (RuleFamily ruleFamily: ruleFamilies) {
            for (String regex: ruleFamily.getRegex()) {
                if (rulestring.split(":")[0].matches(regex)) {
                    found = true;
                    break;
                }
            }

            // Completely break out of the loop
            if (found) {
                rule = (RuleFamily) ruleFamily.clone();
                rule.setRulestring(rulestring);
                break;
            }
        }

        if (!found) throw new IllegalArgumentException("The rulestring is invalid!");
        return rule;
    }

    public static BoundedGrid getBoundedGrid(String rulestring) {
        boolean found = false;
        BoundedGrid boundedGrid = null;
        if (rulestring.contains(":")) {
            String specifier = rulestring.split(":")[1];
            for (BoundedGrid grid: boundedGrids) {
                for (String regex: grid.getRegex()) {
                    if (specifier.matches(regex)) {
                        found = true;
                        boundedGrid = (BoundedGrid) grid.clone();
                        boundedGrid.setSpecifier(specifier);
                        break;
                    }
                }

                if (found) break;
            }

            if (!found) throw new IllegalArgumentException("This bounded grid specifier is invalid!");
        }

        return boundedGrid;
    }

    public static ReadingOrder getReadingOrder(String rulestring) {
        boolean found = false;
        ReadingOrder readingOrder = null;
        if (rulestring.contains(":N")) {
            String specifier = rulestring.split(":N")[1];
            for (ReadingOrder order: readingOrders) {
                for (String regex: order.getRegex()) {
                    if (specifier.matches(regex)) {
                        found = true;
                        readingOrder = (ReadingOrder) order.clone();
                        readingOrder.setSpecifier(specifier);
                        break;
                    }
                }

                if (found) {
                    if (rulestring.split(":").length <= 2)
                        throw new IllegalArgumentException("Naive reading orders can only be applied " +
                                "in the presence of a bounded grid.");
                    break;
                }
            }

            if (!found) throw new IllegalArgumentException("This reading order specifier is invalid!");
        }

        return readingOrder;
    }

    public static String fullRLE(Simulator simulator) {
        // Add header & comments
        String rle = simulator.toRLE();
        StringBuilder rleFinal = new StringBuilder();

        // Adding comments
        String[] comments = ((RuleFamily) simulator.getRule()).generateComments();
        if (comments != null) {
            for (String comment: comments) {
                rleFinal.append(comment).append("\n");
            }
        }

        // Adding header
        rleFinal.append("x = ").append(simulator.getBounds().getValue1().getX() -
                simulator.getBounds().getValue0().getX() + 1).
                append(", y = ").append(simulator.getBounds().getValue1().getY() -
                simulator.getBounds().getValue0().getY() + 1).
                append(", rule = ").append(((RuleFamily) simulator.getRule()).getRulestring()).append("\n");
        rleFinal.append(rle);

        return rleFinal.toString();
    }

    public static String matchRegex(String regex, String input, int num, int groupIndex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        for (int i = 0; i < num + 1; i++) {
            matcher.find();
        }

        return matcher.group(groupIndex);
    }

    public static String matchRegex(String regex, String input, int num) {
        return matchRegex(regex, input, num, 0);
    }

    public static void getTransitionsFromString(HashSet<Integer> transitions, String string) {
        for (int i = 0; i < string.length(); i++) {
            transitions.add(Integer.parseInt(String.valueOf(string.charAt(i))));
        }
    }

    public static void getTransitionsFromStringWithCommas(Set<Integer> transitions, String string) {
        if (string.length() == 0)
            return;  // Check for an empty string

        String[] tokens = string.split(",");
        for (String token: tokens) {
            if (token.contains("-")) {
                for (int i = Integer.parseInt(token.split("-")[0]);
                     i <= Integer.parseInt(token.split("-")[1]); i++) {
                    transitions.add(i);
                }
            }
            else {
                transitions.add(Integer.parseInt(token));
            }
        }
    }

    public static void getTransitionsFromStringWithCommas(List<Integer> transitions, String string) {
        if (string.length() == 0)
            return;  // Check for an empty string

        String[] tokens = string.split(",");
        for (String token: tokens) {
            if (token.contains("-")) {
                for (int i = Integer.parseInt(token.split("-")[0]);
                     i <= Integer.parseInt(token.split("-")[1]); i++) {
                    transitions.add(i);
                }
            }
            else {
                transitions.add(Integer.parseInt(token));
            }
        }
    }

    public static String canoniseTransitions(HashSet<Integer> transitions) {
        Object[] array = transitions.toArray();
        int[] sortedTransitions = new int[transitions.size()];
        for (int i = 0; i < transitions.size(); i++) {
            sortedTransitions[i] = (int) array[i];
        }

        Arrays.sort(sortedTransitions); // Sorting transitions

        StringBuilder rulestringBuilder = new StringBuilder();
        for (int transition: sortedTransitions) {
            rulestringBuilder.append(transition);
        }

        return rulestringBuilder.toString();
    }

    public static String canoniseTransitionsWithCommas(HashSet<Integer> transitions) {
        Object[] array = transitions.toArray();
        int[] sortedTransitions = new int[transitions.size()];
        for (int i = 0; i < transitions.size(); i++) {
            sortedTransitions[i] = (int) array[i];
        }

        Arrays.sort(sortedTransitions); // Sorting transitions
        return canoniseTransitionsWithCommas(sortedTransitions);
    }

    public static String canoniseTransitionsWithCommas(List<Integer> transitions) {
        int[] sortedTransitions = transitions.stream().mapToInt(i->i).toArray();
        Arrays.sort(sortedTransitions);

        return canoniseTransitionsWithCommas(sortedTransitions);
    }

    private static String canoniseTransitionsWithCommas(int[] sortedTransitions) {
        // Code that I totally didn't steal from somewhere
        int idx = 0, idx2 = 0;
        int len = sortedTransitions.length;
        StringBuilder rulestring = new StringBuilder();
        while (idx < len) {
            while (++idx2 < len && sortedTransitions[idx2] - sortedTransitions[idx2 - 1] == 1);
            if (idx2 - idx > 1) {
                rulestring.append(String.format("%s-%s,", sortedTransitions[idx], sortedTransitions[idx2 - 1]));
                idx = idx2;
            } else {
                for (; idx < idx2; idx++)
                    rulestring.append(String.format("%s,", sortedTransitions[idx]));
            }
        }

        if (rulestring.length() == 0)
            rulestring.append(",");

        return rulestring.toString();
    }

    public static void randomiseTransitions(Set<Integer> transitions, Set<Integer> minTransitions,
                                            Set<Integer> maxTransitions) {
        Random random = new Random();

        // Use a deepcopy
        minTransitions = new HashSet<>(minTransitions);
        maxTransitions = new HashSet<>(maxTransitions);

        // Clear existing transtions
        transitions.clear();

        // Adding compulsory transitions
        transitions.addAll(minTransitions);

        // Remove compulsory transitions
        maxTransitions.removeAll(minTransitions);

        // Add to rule at random
        // TODO (Improve RNG function)
        int transitionProbability = random.nextInt(500) + 250;
        for (int transition: maxTransitions) {
            if (random.nextInt(1000) > transitionProbability) {
                transitions.add(transition);
            }
        }
    }

    public static void randomiseTransitions(List<Integer> transitions, List<Integer> minTransitions,
                                            List<Integer> maxTransitions) {
        Random random = new Random();

        // Use a deepcopy
        minTransitions = new ArrayList<>(minTransitions);
        maxTransitions = new ArrayList<>(maxTransitions);

        // Clear existing transtions
        transitions.clear();

        // Adding compulsory transitions
        transitions.addAll(minTransitions);

        // Remove compulsory transitions
        maxTransitions.removeAll(minTransitions);

        // Add to rule at random
        // TODO (Improve RNG function)
        int transitionProbability = random.nextInt(500) + 250;
        for (int transition: maxTransitions) {
            if (random.nextInt(1000) > transitionProbability) {
                transitions.add(transition);
            }
        }
    }

    public static boolean checkSubset(Set<Integer> subset, Set<Integer> superset) {
        // Ensure subset is a subset of superset
        for (var transition: subset) {
            if (!superset.contains(transition)) {
                return false;
            }
        }

        return true;
    }

    public static boolean checkSubset(List<Integer> subset, List<Integer> superset) {
        // Ensure subset is a subset of superset
        for (var transition: subset) {
            if (!superset.contains(transition)) {
                return false;
            }
        }

        return true;
    }
}
