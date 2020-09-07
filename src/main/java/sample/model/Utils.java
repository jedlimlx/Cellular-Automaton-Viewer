package sample.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
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

    public static void getTransitionsFromStringWithCommas(HashSet<Integer> transitions, String string) {
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

        // Code that I totally didn't steal from somewhere
        int idx = 0, idx2 = 0;
        int len = sortedTransitions.length;
        StringBuilder rulestring = new StringBuilder();
        while (idx < len) {
            while (++idx2 < len && sortedTransitions[idx2] - sortedTransitions[idx2 - 1] == 1);
            if (idx2 - idx > 2) {
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

    public static void randomiseTransitions(HashSet<Integer> transitions, HashSet<Integer> minTransitions,
                                            HashSet<Integer> maxTransitions) {
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

    public static boolean checkSubset(Set<Integer> subset, Set<Integer> superset) {
        // Ensure subset is a subset of superset
        for (var transition: subset) {
            if (!superset.contains(transition)) {
                return false;
            }
        }

        return true;
    }
}
