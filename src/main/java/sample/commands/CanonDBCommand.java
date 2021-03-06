package sample.commands;

import picocli.CommandLine;
import sample.model.Utils;
import sample.model.database.GliderDBReader;
import sample.model.database.SOSSPReader;
import sample.model.database.SSSSSReader;
import sample.model.patterns.Oscillator;
import sample.model.patterns.Spaceship;

import java.io.File;
import java.io.IOException;

@CommandLine.Command(name = "canon", aliases = {"canonise"}, description =
        "Canonises a GliderDB database")
public class CanonDBCommand implements Runnable {
    @CommandLine.Option(names = {"-db", "--database"}, description = "The database file", required = true)
    private File databaseFile;

    @CommandLine.Option(names = {"-out", "--output"}, description = "The output file", required = true)
    private File outputFile;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help;

    @Override
    public void run() {
        try {
            GliderDBReader reader = new GliderDBReader(databaseFile);
            reader.canoniseDB(outputFile);
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
            System.exit(-1);
        }

        System.exit(0);
    }
}
