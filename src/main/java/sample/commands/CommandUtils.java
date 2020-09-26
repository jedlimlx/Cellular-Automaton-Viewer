package sample.commands;

import sample.model.Coordinate;
import sample.model.rules.RuleFamily;
import sample.model.rules.hrot.*;
import sample.model.rules.hrot.history.HROTHistory;
import sample.model.rules.misc.OneDimensional;
import sample.model.rules.misc.Turmites;
import sample.model.simulation.Simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandUtils {
    public static RuleFamily[] ruleFamilies = {new HROT(), new HROTHistory(), new HROTGenerations(),
            new HROTExtendedGenerations(), new IntegerHROT(), new DeficientHROT(), new OneDimensional(),
            new Turmites()};
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
                if (rulestring.matches(regex)) {
                    found = true;
                    break;
                }
            }

            // Completely break out of the loop
            if (found) {
                ruleFamily.setRulestring(rulestring);
                rule = ruleFamily;
                break;
            }
        }

        if (!found) throw new IllegalArgumentException("The rulestring is invalid!");
        return rule;
    }
}
