package sample.commands;

import picocli.CommandLine;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.RuleFamily;

import java.io.File;

@CommandLine.Command(name = "apgtable", aliases = {"ruletable"}, description = 
        "Generates an apgtable / ruletable for apgsearch")
public class ApgtableCommand implements Runnable {
    @CommandLine.Option(names = {"-r", "--rulestring"}, description = "Rulestring of the rule",

            required = true)
    private String ruleString;
    
    @CommandLine.Option(names = {"-o", "--output"}, description = "Output file", required = true)
    private File outputFile;
    
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help;

    @Override
    public void run() {
        try {
            RuleFamily family = CommandUtils.fromRulestring(ruleString);
            if (!(family instanceof ApgtableGeneratable))
                throw new UnsupportedOperationException("This rulespace does not support apgtable generation!");

            if ((((ApgtableGeneratable) family).generateApgtable(outputFile))) {
                System.exit(0);
            }
            else {
                System.err.println("Something went wrong.");
                System.exit(-1);
            }
        }
        catch (UnsupportedOperationException exception) {
            System.err.println(exception.getMessage());
        }
    }
}
