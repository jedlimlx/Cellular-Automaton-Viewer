package application.commands;

import picocli.CommandLine;
import application.model.Utils;

import java.util.Map;

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
        Map<String, String> info = Utils.fromRulestring(ruleString).getRuleInfo();

        for (String key: info.keySet()) {
            System.out.println(key + ": " + info.get(key));
        }

        System.exit(0);
    }
}
