package sample.commands;

import picocli.CommandLine;
import sample.model.APGTable;
import sample.model.rules.ApgtableGeneratable;
import sample.model.rules.RuleFamily;

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
            RuleFamily family = CommandUtils.fromRulestring(ruleString);
            if (!(family instanceof ApgtableGeneratable))
                throw new UnsupportedOperationException("This rulespace does not support apgtable generation!");

            APGTable apgTable = ((ApgtableGeneratable) family).generateApgtable();

            FileWriter fileWriter = new FileWriter(outputFile);
            fileWriter.write("@RULE" + outputFile.getName().replace(".rule", ""));
            fileWriter.write("\n@TABLE\n");
            fileWriter.write(apgTable.compileAPGTable());
            fileWriter.close();
        }
        catch (UnsupportedOperationException | IOException exception) {
            System.err.println(exception.getMessage());
            System.exit(-1);
        }
    }
}
