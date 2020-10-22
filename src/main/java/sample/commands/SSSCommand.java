package sample.commands;

import picocli.CommandLine;
import sample.model.Utils;
import sample.model.database.SOSSPReader;
import sample.model.database.SSSSSReader;
import sample.model.patterns.Oscillator;
import sample.model.patterns.Spaceship;

import java.io.File;
import java.io.IOException;

@CommandLine.Command(name = "5s", aliases = {"sssss"}, description =
        "Queries the 5S / SOSSP database")
public class SSSCommand implements Runnable {
    @CommandLine.Option(names = {"-v", "--velocity"}, description = "Velocity of the ship")
    private String velocity;

    @CommandLine.Option(names = {"-p", "--period"}, description = "Period of the oscillator", defaultValue = "0")
    private int period;

    @CommandLine.Option(names = {"-db", "--database"}, description = "The database file", required = true)
    private File databaseFile;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help;

    @Override
    public void run() {
        try {
            if (period == 0 && velocity == null) {
                System.err.println("Either period or velocity must be specified");
            } else if (period == 0) {
                SSSSSReader reader = new SSSSSReader(databaseFile);

                int period, dx, dy = 0;
                if (velocity.endsWith("o")) {
                    String regex = "(\\d+)?c/(\\d+)o";
                    try {
                        dx = Integer.parseInt(Utils.matchRegex("(\\d+)c/\\d+o", velocity, 0, 1));
                    } catch (IllegalStateException exception) {
                        dx = 1;
                    }
                    period = Integer.parseInt(Utils.matchRegex(regex, velocity, 0, 2));
                } else if (velocity.endsWith("d")) {
                    String regex = "(\\d+)?c/(\\d+)d";
                    try {
                        dx = Integer.parseInt(Utils.matchRegex("(\\d+)c/\\d+d", velocity, 0, 1));
                    } catch (IllegalStateException exception) {
                        dx = 1;
                    }

                    dy = dx;
                    period = Integer.parseInt(Utils.matchRegex(regex, velocity, 0, 2));
                } else {
                    String regex = "\\((\\d+),\\s*(\\d+)\\)c/(\\d+)";
                    dx = Integer.parseInt(Utils.matchRegex(regex, velocity, 0, 1));
                    dy = Integer.parseInt(Utils.matchRegex(regex, velocity, 0, 2));
                    period = Integer.parseInt(Utils.matchRegex(regex, velocity, 0, 3));
                }

                Spaceship result = reader.getShipBySpeed(period, dx, dy);
                if (result == null) {
                    System.err.println("No such spaceship found in database!");
                    System.exit(-1);
                } else {
                    System.out.println("#C " + result);
                    System.out.println("#C Population: " + result.getPopulation());
                    System.out.println(Utils.fullRLE(result));
                }
            } else {
                SOSSPReader reader = new SOSSPReader(databaseFile);

                Oscillator result = reader.getOscByPeriod(period);
                if (result == null) {
                    System.err.println("No such oscillator found in database!");
                    System.exit(-1);
                } else {
                    System.out.println("#C " + result);
                    System.out.println("#C Population: " + result.getPopulation());
                    System.out.println(Utils.fullRLE(result));
                }
            }
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
            System.exit(-1);
        }

        System.exit(0);
    }
}
