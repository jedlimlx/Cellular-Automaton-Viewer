package sample.commands;

import picocli.CommandLine;
import sample.model.Utils;
import sample.model.database.GliderDBEntry;
import sample.model.database.GliderDBReader;
import sample.model.rules.RuleFamily;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.List;

@CommandLine.Command(name = "db", aliases = {"database"}, description =
        "Queries the GliderDB database")
public class DBCommand implements Runnable {
    @CommandLine.Option(names = {"-db", "--database"}, description = "The database file", required = true)
    private File file;

    @CommandLine.Option(names = {"-p", "--period"}, description = "The period of the ship", defaultValue = "-1")
    private int period;

    @CommandLine.Option(names = {"-dx"}, description = "The x displacement of the ship", defaultValue = "-1")
    private int dx;

    @CommandLine.Option(names = {"-dy"}, description = "The y displacement of the ship", defaultValue = "-1")
    private int dy;

    @CommandLine.Option(names = {"-min", "--min_rule"}, description = "The minimum rule", defaultValue = "")
    private String minRule;

    @CommandLine.Option(names = {"-max", "--max_rule"}, description = "The maximum rule", defaultValue = "")
    private String maxRule;

    @CommandLine.Option(names = {"-sort", "--sort"}, description = "Sort by [period, slope, population]",
            defaultValue = "")
    private String sort;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help;

    @Override
    public void run() {
        RuleFamily minRule, maxRule;
        try {
            minRule = Utils.fromRulestring(this.minRule);
            maxRule = Utils.fromRulestring(this.maxRule);
        } catch (IllegalArgumentException exception) {
            minRule = null;
            maxRule = null;
        }

        Comparator<GliderDBEntry> comparator;
        switch (sort) {
            case "period":
                comparator = Comparator.comparingInt(o -> o.getSpaceship().getPeriod());
                break;
            case "slope":
                comparator = (o1, o2) -> {
                    if (o1.getSpaceship().getDisplacementX() == o2.getSpaceship().getDisplacementX())
                        return Integer.compare(o1.getSpaceship().getDisplacementY(),
                                o2.getSpaceship().getDisplacementY());
                    return Integer.compare(o1.getSpaceship().getDisplacementX(),
                            o2.getSpaceship().getDisplacementX());
                };
                break;
            case "population":
                comparator = Comparator.comparingInt(o -> o.getSpaceship().getPopulation());
                break;
            default:
                comparator = null;
                break;
        }

        try {
            GliderDBReader reader = new GliderDBReader(file);
            List<GliderDBEntry> entries = reader.getEntries(period, dx, dy, minRule, maxRule, comparator);

            for (GliderDBEntry entry: entries) {
                System.out.println("#C " + entry.getSpaceship());

                if (!entry.getName().equals(""))
                    System.out.println("#C Name: " + entry.getName());
                if (!entry.getDiscoverer().equals(""))
                    System.out.println("#C Discovered by: " + entry.getDiscoverer());

                System.out.println("#C Min Rule: " + entry.getSpaceship().getMinRule());
                System.out.println("#C Max Rule: " + entry.getSpaceship().getMaxRule());
                System.out.println("#C Population: " + entry.getSpaceship().getPopulation());
                System.out.println(Utils.fullRLE(entry.getSpaceship()));

                System.out.println();
            }
        } catch (FileNotFoundException exception) {
            System.err.println("The file specified cannot be found!");
            System.exit(-1);
        }

        System.exit(0);
    }
}
