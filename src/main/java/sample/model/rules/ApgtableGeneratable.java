package sample.model.rules;

import sample.model.rules.ruleloader.RuleDirective;

/**
 * Rule families that support apgtable generation implement this interface
 */
public interface ApgtableGeneratable {
    /**
     * Generates an apgtable for apgsearch to use
     * @return True if the operation was successful, false otherwise
     */
    RuleDirective[] generateApgtable();
}
