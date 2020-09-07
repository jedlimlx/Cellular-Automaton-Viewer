package sample.controller.dialogs.rule;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import sample.model.rules.RuleFamily;

public abstract class RuleWidget extends GridPane {
    protected RuleFamily ruleFamily;
    // protected TextField rulestringField;
    // protected Button confirmRule;

    public RuleWidget() {
        super();

        // Formatting the grid
        super.setHgap(5);
        super.setVgap(5);
        super.setAlignment(Pos.CENTER);

        // Adding controls
        // rulestringField = new TextField();
        // super.add(rulestringField, 0, 0);

        // confirmRule = new Button("Confirm Rule");
        // super.add(confirmRule, 0, 10);
    }

    // Accessors
    public RuleFamily getRuleFamily() {
        return ruleFamily;
    }

    // Mutators
    public void setRuleFamily(RuleFamily ruleFamily) {
        this.ruleFamily = ruleFamily;
    }

    // Updates the rule with the necessary information
    public abstract void updateRule(String rulestring);

    @Override
    public String toString() {
        return ruleFamily.getName();
    }
}
