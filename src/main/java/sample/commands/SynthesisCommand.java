package sample.commands;

import picocli.CommandLine;
import sample.model.Utils;
import sample.model.SymmetryGenerator;
import sample.model.rules.hrot.HROT;
import sample.model.simulation.Simulator;

import java.io.File;
import java.io.FileNotFoundException;

@CommandLine.Command(name = "synth", aliases = {"synthesis"}, description = "Generates random configurations " +
        "of spaceships to be piped into apgsearch")
public class SynthesisCommand implements Runnable {
    @CommandLine.Option(names = {"-n", "--num"}, description = "The number of ships to generate " +
            "(default: 25)", defaultValue = "20")
    private int num;

    @CommandLine.Option(names = {"-x", "--width"}, description =
            "The width of the area where ships are generated (default: 50)",  defaultValue = "50")
    private int x;

    @CommandLine.Option(names = {"-y", "--height"}, description =
            "The height of the area where ships are generated (default: 50)", defaultValue = "50")
    private int y;

    @CommandLine.Option(names = {"-s", "--ships"}, description = "The ships to include in the random configuration",
            required = true)
    private File[] ships;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help;

    @Override
    public void run() {
        try {
            Simulator[] spaceships = new Simulator[ships.length];
            for (int i = 0; i < spaceships.length; i++) {
                spaceships[i] = new Simulator(new HROT("B3/S23"));
                Utils.loadPattern(spaceships[i], ships[i]);
            }

            while (true) {
                System.out.println("x = 0, y = 0, rule = B3/S23");
                System.out.println(SymmetryGenerator.generateSynth(spaceships, num, x, y).toRLE());
            }
        }
        catch (FileNotFoundException exception) {
            System.err.println("The file could not be found!");
            System.exit(-1);
        }

        System.exit(0);
    }
}
