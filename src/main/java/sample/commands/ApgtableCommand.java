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
            RuleFamily family = Utils.fromRulestring(ruleString);
            if (!(family instanceof ApgtableGeneratable))
                throw new UnsupportedOperationException("This rulespace does not support apgtable generation!");

            RuleDirective[] ruleDirectives = ((ApgtableGeneratable) family).generateApgtable();

            RuleLoader ruleLoader = new RuleLoader();
            ruleLoader.addDirective(new RuleNameDirective("@RULE " +
                    outputFile.getName().replace(".rule", "")));

            StringBuilder colourDirective = new StringBuilder("@COLORS\n");
            for (int i = 0; i < family.getNumStates(); i++) {
                colourDirective.append(i).
                        append(" ").append((int) (family.getColour(i).getRed() * 255)).
                        append(" ").append((int) (family.getColour(i).getGreen() * 255)).
                        append(" ").append((int) (family.getColour(i).getBlue() * 255)).
                        append("\n");
            }
            ruleLoader.addDirective(new ColourDirective(colourDirective.toString()));

            for (RuleDirective directive: ruleDirectives)
                ruleLoader.addRuleDirective(directive);

            FileWriter fileWriter = new FileWriter(outputFile);
            fileWriter.write(ruleLoader.export());
            fileWriter.close();
        }
        catch (UnsupportedOperationException | IOException exception) {
            System.err.println(exception.getMessage());
            System.exit(-1);
        }
    }
}
