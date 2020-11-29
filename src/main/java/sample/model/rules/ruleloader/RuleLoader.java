package sample.model.rules.ruleloader;

import javafx.scene.paint.Color;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import sample.controller.MainController;
import sample.model.Coordinate;
import sample.model.rules.RuleFamily;
import sample.model.rules.ruleloader.ruletable.Ruletable;
import sample.model.rules.ruleloader.ruletable.Transition;
import sample.model.rules.ruleloader.ruletable.Variable;
import sample.model.rules.ruleloader.ruletree.Ruletree;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the RuleLoader algorithm which CAViewer uses to load *.rule files.
 */
public class RuleLoader extends RuleFamily {
    public static String RULE_DIRECTORY = "rules";

    private ColourDirective colourDirective;

    private ArrayList<Directive> directives;
    private ArrayList<RuleDirective> ruleDirectives;
    private final Map<String, Directive> directiveMap;

    private static ArrayList<String> rules;

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

        rules = new ArrayList<>();

        // Read settings.json to find the new RULE_DIRECTORY
        try {
            JSONTokener tokener = new JSONTokener(new FileInputStream(MainController.SETTINGS_FILE));
            JSONObject settings = new JSONObject(tokener);
            RULE_DIRECTORY = settings.getString("rule_directory");
        } catch (IOException | JSONException exception) {
            RULE_DIRECTORY = "rules";
        }
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
            if (rulestring.endsWith(".rule")) {
                File file = new File(rulestring);
                scanner = new Scanner(file);
            } else {
                File file = new File(RULE_DIRECTORY + "/" + rulestring + ".rule");
                scanner = new Scanner(file);
            }
        } catch (FileNotFoundException exception) {  // If the file cannot be found
            try {
                InputStream stream = new URL("https://conwaylife.com/w/api.php?action=parse&format=json&" +
                        "prop=wikitext&page=RULE:" + rulestring).openStream();
                Scanner s = new Scanner(stream).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";

                JSONObject json = new JSONObject(result);

                try  {
                    scanner = new Scanner(json.getJSONObject("parse").getJSONObject("wikitext").getString("*"));
                } catch (JSONException jsonException) {
                    throw new IllegalArgumentException("This rule does not exist on the LifeWiki:Rule Namespace!");
                }
            } catch (IOException | JSONException exception2) {
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

                if (directiveMap.get(line.split("\\s+")[0]) == null) {
                    LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).log(
                            Level.WARNING, "Unknown directive " + line.split("\\s+")[0] + " found");
                    directive = null;
                    continue;
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
        if (rules.isEmpty()) {
            try {
                String url = "https://conwaylife.com/wiki/Special:AllPages?from=&to=&namespace=3794";
                while (true) {
                    InputStream stream = new URL(url).openStream();
                    Scanner s = new Scanner(stream).useDelimiter("\\A");
                    String result = s.hasNext() ? s.next() : "";

                    Matcher matcher = Pattern.compile("\"/wiki/Rule:([a-zA-Z0-9_()-]+)\"").matcher(result);
                    while (matcher.find()) {
                        rules.add(matcher.group(1));
                    }

                    Matcher matcher2 = Pattern.compile("<a href=\"(\\S+)\" title=\"Special:AllPages\">" +
                            "Next page \\(.*?\\)</a>").matcher(result);
                    if (matcher2.find()) url = "https://conwaylife.com" +
                            matcher2.group(1).replace("amp;", "");
                    else break;
                }
            } catch (IOException ignored) {}
        }

        // Fetching from file
        File f = new File(RULE_DIRECTORY);
        String[] pathnames = f.list();

        if (pathnames != null) {
            for (String pathname: pathnames) {
                if (pathname.endsWith(".rule")) rules.add(pathname.replace(".rule", ""));
            }
        }

        // Match file paths
        rules.add("[\\s\\S]+.rule");

        return rules.toArray(new String[0]);
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

    public void addDirective(Directive directive) {
        directives.add(directive);
    }

    public void addRuleDirective(RuleDirective directive) {
        directives.add(directive);
        ruleDirectives.add(directive);
    }

    public String export() {
        alternatingPeriod = ruleDirectives.size();
        updateBackground();  // Update the background first

        StringBuilder exported = new StringBuilder("# This ruletable is automatically generated by CAViewer\n" +
                "# Note that is ruletable may not work properly in Golly as it is meant to be run by lifelib.\n" +
                "# If this ruletable operates in with a range 1 neighbourhood, switch neighbourhood:... with " +
                "neighbourhood:Moore or any other appropriate neighbourhood to run it in Golly\n\n");
        for (Directive directive: directives) {
            if (directive instanceof Exportable && !(directive instanceof RuleDirective))
                exported.append(((Exportable) directive).export()).append("\n");
        }

        // Pre-processing
        if (ruleDirectives.size() > 1 || (ruleDirectives.size() == 1 && ruleDirectives.get(0) instanceof Ruletable)) {
            boolean permute = false, permuteDefined = false;
            for (RuleDirective directive: ruleDirectives) {
                if (!(directive instanceof Ruletable))
                    throw new UnsupportedOperationException("Cannot export more than 1 non-ruletable rule directive!");

                Ruletable ruletable = (Ruletable) directive;
                if (permuteDefined && permute != ruletable.isPermute())
                    throw new UnsupportedOperationException("The ruletable must either all have or " +
                            "all don't have permute symmetry.");

                permute = ruletable.isPermute();
                permuteDefined = true;

                numStates = ruletable.getNumStates();

                // All cells will survive
                if (background.length > 1) {
                    for (int i = 0; i < numStates; i++) ruletable.addOTTransition(0, i + "",
                            i + "", "any", "0");
                }
            }

            // Adding headers
            String neighbourhoodString = Arrays.toString(getNeighbourhood());

            exported.append("@TABLE\n");
            exported.append("n_states:").append((numStates - 1) * background.length + 1).append("\n");
            exported.append("neighborhood:[(0, 0), ").append(neighbourhoodString, 1,
                    neighbourhoodString.length() - 1).append(", (0, 0)]\n");  // Add the (0, 0) at the front and back

            if (!permute) exported.append("symmetries:none\n\n");
            else exported.append("symmetries:permute\n\n");

            for (int bgIndex = 0; bgIndex < background.length; bgIndex++) {
                Ruletable ruletable = (Ruletable) ruleDirectives.get(bgIndex % ruleDirectives.size());

                Variable variable;  // Adding variable clauses
                for (String name: ruletable.getVariables().keySet()) {
                    variable = ruletable.getVariables().get(name);

                    ArrayList<Integer> tempValues = new ArrayList<>(variable.getValues());
                    ArrayList<Integer> values = new ArrayList<>();

                    // Converting the values of the variables according to the background
                    for (Integer tempValue : tempValues) values.add(getCellForBackground(tempValue, bgIndex));

                    Collections.sort(values);

                    String valuesString = values.toString().replace("[", "{").
                            replace("]", "}");

                    if (variable.isUnbounded()) {
                        // Need to be handled separately because they are not supported by lifelib and Golly
                        for (int j = 0; j < getNeighbourhood().length; j++) {
                            if (j == 0) {
                                // var _{varName}.{pos}.{bgIndex} = {values}
                                exported.append("var _").append(variable.getName()).append(".").append(j).
                                        append(".").append(bgIndex).append(" = ").append(valuesString);
                            } else {
                                // var _{varName}.{pos}.{bgIndex} = {1st_bounded_var}
                                exported.append("var _").append(variable.getName()).append(".").append(j).
                                        append(".").append(bgIndex).append(" = _").append(variable.getName()).
                                        append(".0.").append(bgIndex);
                            }

                            if (j != getNeighbourhood().length - 1) exported.append("\n");
                        }
                    } else {
                        // var {varName}.{bgIndex} = {values}
                        exported.append("var ").append(variable.getName()).append(".").append(bgIndex).
                                append(" = ").append(valuesString);
                    }

                    exported.append("\n\n");
                }

                // Adding transition clauses
                for (Transition transition: ruletable.getTransitions()) {
                    // Iterate through the variables and values
                    for (int i = 0; i < transition.getValues().size() + transition.getVariables().size(); i++) {
                        if (i != getNeighbourhood().length + 1) {
                            if (transition.getValues().get(i) != null) {
                                exported.append(getCellForBackground(transition.getValues().get(i), bgIndex));
                            } else {
                                if (transition.getVariables().get(i).isUnbounded()) {
                                    exported.append("_").append(transition.getVariables().get(i).getName()).append(".").
                                            append(i - 1).append(".").append(bgIndex);
                                } else {
                                    exported.append(transition.getVariables().get(i).getName()).append(".").
                                            append(bgIndex);
                                }
                            }

                            exported.append(", ");
                        } else {  // Handle output state differently
                            if (transition.getValues().get(i) != null) {
                                exported.append(getCellForBackground(transition.getValues().get(i), bgIndex + 1));
                            } else {
                                exported.append(transition.getVariables().get(i).getName()).append(".").
                                        append((bgIndex + 1) % background.length);
                            }
                        }
                    }

                    exported.append("\n");
                }

                exported.append("\n");
            }

            return exported.toString();
        } else if (ruleDirectives.size() == 1 && ruleDirectives.get(0) instanceof Exportable) {
            // Handling other rule directives
            exported.append(((Exportable) ruleDirectives.get(0)).export()).append("\n");
            return exported.toString();
        } else if (ruleDirectives.size() == 0) {
            throw new IllegalArgumentException("No rule directives provided to export!");
        } else {
            throw new IllegalArgumentException("Cannot export more than 1 non-ruletable rule directive!");
        }
    }

    /**
     * Gets the cell's state for the specified background
     * @param state The state of the cell
     * @param index The index of the background in the background attribute
     * @return Returns the cell's state for the specified background
     */
    private int getCellForBackground(int state, int index) {
        index = Math.floorMod(index, background.length);  // Make index wrap around

        if (state == 0) {
            if (background[index] != 0) {
                return background[index] + ((numStates - 1) * index);
            } else {
                return 0;
            }
        }
        else if (state == background[index]) return 0;
        return state + (numStates - 1) * index;
    }
}
