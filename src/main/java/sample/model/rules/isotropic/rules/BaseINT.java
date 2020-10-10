package sample.model.rules.isotropic.rules;

import sample.model.Utils;
import sample.model.rules.RuleFamily;
import sample.model.rules.isotropic.transitions.*;

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
    protected final String neighbourhoodRegex = "(M|K|H|FC|FE|C2)";

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
        neighbourhoodLookup.put("C2", new R2CrossINT(""));
        neighbourhoodLookup.put("K", new R2KnightINT(""));
        neighbourhoodLookup.put("FC", new R2FarCornersINT(""));
        neighbourhoodLookup.put("FE", new R3FarEdgesINT(""));
    }

    /**
     * Gets the INT neighbourhood associated with the neighbourhood identifier
     * @param rulestring The rulestring of the rule
     * @return Returns the INT neighbourhood / transition associated with the neighbourhood identifier
     */
    protected INTTransitions getINTTransition(String rulestring) {
        try {
            neighbourhoodString = Utils.matchRegex(neighbourhoodRegex, rulestring, 0, 1);
            return (INTTransitions) neighbourhoodLookup.get(neighbourhoodString).clone();
        }
        catch (IllegalStateException exception) {
            neighbourhoodString = "";
            return new R1MooreINT("");
        }
    }
}
