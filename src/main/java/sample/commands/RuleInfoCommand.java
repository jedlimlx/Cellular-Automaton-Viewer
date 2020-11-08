package sample.commands;

import picocli.CommandLine;
import sample.model.Utils;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.RuleFamily;
import sample.model.rules.ruleloader.ColourDirective;
import sample.model.rules.ruleloader.RuleDirective;
import sample.model.rules.ruleloader.RuleLoader;
import sample.model.rules.ruleloader.RuleNameDirective;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@CommandLine.Command(name = "info", aliases = {"information"}, description =
        "Get more information about a particular rule")
public class RuleInfoCommand implements Runnable {
    @CommandLine.Option(names = {"-r", "--rulestring"}, description = "Rulestring of the rule",
            required = true)
    private String ruleString;
    
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help;

    @Override
    public void run() {
        System.out.println(Utils.fromRulestring(ruleString).getRuleInfo());
        System.exit(0);
    }
}
