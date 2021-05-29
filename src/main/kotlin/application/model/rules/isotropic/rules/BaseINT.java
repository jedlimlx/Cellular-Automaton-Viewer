package application.model.rules.isotropic.rules;

import application.model.CommentGenerator;
import application.model.Utils;
import application.model.rules.RuleFamily;
import application.model.rules.isotropic.transitions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * All INT rules should inherit from this class
 */
public abstract class BaseINT extends RuleFamily {
    /**
     * The neighbourhood string of the INT rule
     */
    protected String neighbourhoodString = "";

    /**
     * Regexes for identifying the neighbourhood
     */
    protected final String neighbourhoodRegex = "(M|K|H|B|FC|FE|C2|V2|C3)";

    /**
     * Looks up which INT neighbourhood corresponds to which neighbourhood identifier
     */
    protected final Map<String, INTTransitions> neighbourhoodLookup = new HashMap<>();

    /**
     * Initialises the neighbourhood lookup map
     */
    public BaseINT() {
        neighbourhoodLookup.put("M", new R1MooreINT(""));
        neighbourhoodLookup.put("H", new R1HexINT(""));
        neighbourhoodLookup.put("K", new R2KnightINT(""));
        neighbourhoodLookup.put("B", new R2CheckerboardINT(""));
        neighbourhoodLookup.put("FC", new R2FarCornersINT(""));
        neighbourhoodLookup.put("FE", new R3FarEdgesINT(""));
        neighbourhoodLookup.put("C2", new R2CrossINT(""));
        neighbourhoodLookup.put("V2", new R2VonNeumannINT(""));
        neighbourhoodLookup.put("C3", new R3CrossINT(""));
    }

    /**
     * Gets the INT neighbourhood associated with the neighbourhood identifier
     * @param rulestring The rulestring of the rule
     * @return Returns the INT neighbourhood / transition associated with the neighbourhood identifier
     */
    protected INTTransitions getINTTransition(String rulestring) {
        try {
            neighbourhoodString = rulestring.substring(rulestring.length() - 2);
            neighbourhoodString = Utils.matchRegex(neighbourhoodRegex, neighbourhoodString, 0, 1);
            return neighbourhoodLookup.get(neighbourhoodString).getMinTransition();
        }
        catch (IllegalStateException exception) {
            neighbourhoodString = "";
            return new R1MooreINT("");
        }
    }

    @Override
    public Map<String, String> getRuleInfo() {
        Map<String, String> map = super.getRuleInfo();

        StringBuilder weightsString = new StringBuilder("\n");
        for (String string: CommentGenerator.generateFromWeights(null, getNeighbourhood())) {
            weightsString.append(string.replaceAll("#R\\s*", "")).append("\n");
        }

        map.put("Weights / Neighbourhood", weightsString.toString());
        return map;
    }
}
