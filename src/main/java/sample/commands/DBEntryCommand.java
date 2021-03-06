package sample.commands;

import picocli.CommandLine;
import sample.model.Utils;
import sample.model.database.GliderDBEntry;
import sample.model.patterns.Oscillator;
import sample.model.patterns.Pattern;
import sample.model.patterns.Spaceship;
import sample.model.rules.hrot.HROT;
import sample.model.simulation.Simulator;

import java.io.File;
import java.io.FileNotFoundException;

@CommandLine.Command(name = "entry", aliases = {"db_entry"}, description =
        "Generates an entry for the GliderDB database")
public class DBEntryCommand implements Runnable {
    private Simulator simulator;

    @CommandLine.Option(names = {"-i", "--input"},
            description = "Input file containing pattern to be simulated", required = true)
    private File inputFile;

    @Override
    public void run() {
        try {
            // Initialise the Simulator
            simulator = new Simulator(new HROT("B3/S23"));
            Utils.loadPattern(simulator, inputFile);

            Pattern pattern = simulator.identify();
            if (pattern instanceof Spaceship) {
                System.out.println(new GliderDBEntry((Spaceship) simulator.identify(), "", ""));
            } else if (pattern instanceof Oscillator) {
                System.out.println(new GliderDBEntry((Oscillator) simulator.identify(), "", ""));
            } else {
                System.err.println("The object is not a spaceship / oscillator!");
                System.exit(-1);
            }
        } catch (FileNotFoundException exception) {
            System.err.println("Input file not found!");
            System.exit(-1);
        } catch (ClassCastException exception) {
            System.err.println("The object is not a spaceship / oscillator!");
            System.exit(-1);
        }

        System.exit(0);
    }
}
