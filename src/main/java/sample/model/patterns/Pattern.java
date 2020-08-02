package sample.model.patterns;

import sample.model.Simulator;
import sample.model.rules.Rule;

import java.util.HashMap;

public abstract class Pattern extends Simulator {
    public Pattern(Rule rule) {
        super(rule);
    }

    // Returns a brief description of the pattern
    public abstract String toString();

    // Returns a dictionary of attribute: value to be displayed to the user
    public abstract HashMap<String, String> additionalInfo();
}
