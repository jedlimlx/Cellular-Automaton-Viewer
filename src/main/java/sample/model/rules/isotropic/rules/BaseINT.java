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
     * Regexes for identifying the neighbourhood
     */
    protected final String neighbourhoodRegex = "(M|K|FC|FE|C2)";

    /**
     * Looks up which INT neighbourhood corresponds to which neighbourhood identifier
     */
    protected final Map<String, INTTransitions> neighbourhoodLookup = new HashMap<>();

    /**
     * Initialises the neighbourhood lookup map
     */
    public BaseINT() {
        neighbourhoodLookup.put("M", new R1MooreINT(""));
        neighbourhoodLookup.put("FC", new R2FarCornersINT(""));
        neighbourhoodLookup.put("FE", new R3FarEdgesINT(""));
        neighbourhoodLookup.put("C2", new R2CrossINT(""));
        neighbourhoodLookup.put("K", new R2KnightINT(""));
    }

    /**
     * Gets the INT neighbourhood associated with the neighbourhood identifier
     * @param rulestring The rulestring of the rule
     * @return Returns the INT neighbourhood / transition associated with the neighbourhood identifier
     */
    protected INTTransitions getINTTransition(String rulestring) {
        try {
            return (INTTransitions) neighbourhoodLookup.get(
                    Utils.matchRegex(neighbourhoodRegex, rulestring, 0, 1)).clone();
        }
        catch (IllegalStateException exception) {
            return new R1MooreINT("");
        }
    }
}
