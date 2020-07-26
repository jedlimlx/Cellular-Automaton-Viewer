package sample.model;

import java.io.File;

public abstract class RuleFamily extends Rule implements Cloneable {
    protected String name;
    protected String rulestring;

    // Child class must implement
    // Loads the rulestring
    public abstract void fromRulestring(String rulestring);

    // Canonises the rulestring
    public abstract String canonise(String rulestring);

    // Sets the rulestring
    public void setRulestring(String rulestring) {
        fromRulestring(rulestring);
        this.rulestring = canonise(rulestring);
    }

    // Returns the regexes to identify the rule family
    public abstract String[] getRegex();

    // Get description of this rule family
    public abstract String getDescription();

    // Randomise rule between minimum and maximum rules
    // Throw IllegalArgumentException if the rule families are not the correct type
    public abstract void randomise(RuleFamily minRule, RuleFamily maxRule) throws IllegalArgumentException;

    // Output false if not successful, true if successful
    // Generates apgtable for apgsearch to use
    public abstract boolean generateApgtable(File file);

    // Output false if not successful, true if successful
    // Generates a *.ca_rule file
    public abstract boolean generateCARule(File file);

    // Load from *.ca_rule file
    // Output false if not successful, true if successful
    public abstract boolean loadCARule(File file);

    @Override  // Clones the object
    public abstract Object clone();

    // Accessor
    public String getName() {
        return name;
    }

    public String getRulestring() {
        return rulestring;
    }
}
