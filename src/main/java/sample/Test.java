package sample;

import sample.model.NeighbourhoodGenerator;
import sample.model.rules.ruleloader.RuleLoader;
import sample.model.rules.ruleloader.RuleNameDirective;
import sample.model.rules.ruleloader.ruletable.Ruletable;
import sample.model.rules.ruleloader.ruletable.Symmetry;

public class Test {
    public static void main(String[] args) {
        Ruletable ruletable = new Ruletable("");

        ruletable.setNumStates(2);
        ruletable.setPermute();
        ruletable.setSymmetry(new Symmetry(""));
        ruletable.setNeighbourhood(NeighbourhoodGenerator.generateMoore(1));

        ruletable.addVariable(Ruletable.ANY);

        ruletable.addOTTransition(0, "0", "1", "0", "1");
        ruletable.addOTTransition(2, "0", "1", "0", "1");
        ruletable.addOTTransition(4, "0", "1", "0", "1");

        ruletable.addOTTransition(0, "1", "1", "0", "1");
        ruletable.addOTTransition(1, "1", "1", "0", "1");
        ruletable.addOTTransition(2, "1", "1", "0", "1");
        ruletable.addOTTransition(3, "1", "1", "0", "1");

        ruletable.addOTTransition(0, "1", "0", "any", "0");

        RuleLoader ruleLoader = new RuleLoader();
        ruleLoader.addDirective(new RuleNameDirective("@RULE HelloWorld"));
        ruleLoader.addRuleDirective(ruletable);

        System.out.println(ruleLoader.export());
    }
}
