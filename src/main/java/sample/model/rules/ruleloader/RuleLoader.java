package sample.model.rules.ruleloader;

import javafx.scene.paint.Color;
import sample.model.Coordinate;
import sample.model.rules.RuleFamily;
import sample.model.rules.ruleloader.ruletable.Ruletable;
import sample.model.rules.ruleloader.ruletree.Ruletree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the RuleLoader algorithm which CAViewer uses to load *.rule files.
 */
public class RuleLoader extends RuleFamily {
    private final static String RULE_DIRECTORY = "rules";

    private ColourDirective colourDirective;

    private ArrayList<Directive> directives;
    private ArrayList<RuleDirective> ruleDirectives;
    private Map<String, Directive> directiveMap;

    /**
     * Constructs a new RuleLoader rule.
     */
    public RuleLoader() {
        name = "RuleLoader";

        directiveMap = new HashMap<>();
        directiveMap.put("@RULE", new RuleNameDirective(""));
        directiveMap.put("@COLORS", new ColourDirective(""));
        directiveMap.put("@COLOURS", new ColourDirective(""));
        directiveMap.put("@SQC", new SQC(""));
        directiveMap.put("@TREE", new Ruletree(""));
        directiveMap.put("@TABLE", new Ruletable(""));

        directives = new ArrayList<>();
        ruleDirectives = new ArrayList<>();
    }

    /**
     * Constructs a new RuleLoader rule with the provided name.
     * @param rulestring The name of the RuleLoader rule (from rules directory or LifeWiki:Rule namespace)
     */
    public RuleLoader(String rulestring) {
        this();
        setRulestring(rulestring);
    }

    /**
     * Loads the *.rule file from the LifeWiki:Rule namespace or from the rules directory
     * @param rulestring The name of the *.rule file
     */
    @Override
    protected void fromRulestring(String rulestring) {
        Scanner scanner;

        directives = new ArrayList<>();
        ruleDirectives = new ArrayList<>();

        colourDirective = null;  // Reset colours

        try {
            File file = new File(RULE_DIRECTORY + "/" + rulestring + ".rule");
            scanner = new Scanner(file);
        } catch (FileNotFoundException exception) {  // If the file cannot be found
            try {
                InputStream stream = new URL("https://conwaylife.com/w/index.php?title=Rule:" + rulestring +
                        "&action=edit").openStream();
                Scanner s = new Scanner(stream).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";

                Matcher matcher = Pattern.compile("<textarea.*?>([\\s\\S]*)</textarea>").matcher(result);
                if (matcher.find()) {
                    scanner = new Scanner(matcher.group(1));
                } else {
                    throw new IllegalArgumentException("This rule does not exist on the LifeWiki:Rule Namespace!");
                }

            } catch (IOException exception2) {
                throw new IllegalArgumentException("An error occured while reading the rule from the " +
                        "LifeWiki:Rule namespace! It is possible that the rule does not exist or " +
                        "you are not connected to the Internet.");
            }
        }


        String line;
        Directive directive = null;
        StringBuilder content = new StringBuilder();

        while (scanner.hasNextLine()) {  // Looping through all the lines
            line = scanner.nextLine();

            if (line.startsWith("#")) continue;  // Ignore comments
            if (line.matches("\\s*")) continue;  // Ignore blank lines

            if (line.startsWith("@")) {
                if (directive != null) {
                    directive.parseContent(content.toString());
                    directives.add(directive);  // Adding directive

                    // Adding to rule directive
                    if (directive instanceof RuleDirective) ruleDirectives.add((RuleDirective) directive);
                    else if (directive instanceof ColourDirective) colourDirective = (ColourDirective) directive;

                    content = new StringBuilder();
                }

                directive = (Directive) directiveMap.get(line.split("\\s+")[0]).clone();
            }

            if (directive != null) content.append(line).append("\n");  // Adding to the directive's content
        }

        if (directive != null) {
            directive.parseContent(content.toString());
            directives.add(directive);  // Adding directive

            // Adding to rule directive
            if (directive instanceof RuleDirective) ruleDirectives.add((RuleDirective) directive);
            else if (directive instanceof ColourDirective) colourDirective = (ColourDirective) directive;
        }

        // Running some checks
        tiling = ruleDirectives.get(0).getTiling();
        numStates = ruleDirectives.get(0).getNumStates();
        for (RuleDirective ruleDirective: ruleDirectives) {
            if (ruleDirective.getNumStates() != numStates) {
                throw new IllegalArgumentException("Alternating rules must have the same number of states!");
            }

            if (ruleDirective.getTiling() != tiling) {
                throw new IllegalArgumentException("Alternating rules must operate on the same tiling!");
            }
        }

        // Calculating the alternating period and background
        alternatingPeriod = ruleDirectives.size();
        updateBackground();
    }

    @Override
    public String canonise(String rulestring) {
        return rulestring;
    }

    @Override
    public String[] getRegex() {
        /*
        File f = new File(RULE_DIRECTORY);
        String[] pathnames = f.list();

        if (pathnames != null) {
            ArrayList<String> rules = new ArrayList<>();
            for (String pathname: pathnames) {
                if (pathname.endsWith(".rule")) rules.add(pathname.replace(".rule", ""));
            }

            return rules.toArray(new String[0]);
        }
        else {
            return new String[0];
        }
         */

        // TODO (Fetch the list of possible rulenames directly)
        return new String[]{"[a-zA-Z0-9_\\-/()]+"};
    }

    @Override
    public String getDescription() {
        return "This implements the RuleLoader Algorithm that is used to load *.rule files.";
    }

    @Override
    public Object clone() {
        return new RuleLoader(rulestring);
    }

    @Override
    public Coordinate[] getNeighbourhood(int generation) {
        return ruleDirectives.get(generation % ruleDirectives.size()).getNeighbourhood();
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        return ruleDirectives.get(generations % ruleDirectives.size()).transitionFunc(neighbours, cellState);
    }

    @Override
    public Color getColour(int state) {
        if (colourDirective == null) return super.getColour(state);
        else return colourDirective.getColour(state);
    }
}
