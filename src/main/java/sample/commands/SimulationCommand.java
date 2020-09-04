package sample.commands;

import org.javatuples.Pair;
import picocli.CommandLine;
import sample.model.Coordinate;
import sample.model.Simulator;
import sample.model.rules.HROT;
import sample.model.rules.RuleFamily;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

@CommandLine.Command(name = "sim", aliases = {"run"}, description = "Simulate a pattern")
public class SimulationCommand implements Runnable {
    private Simulator simulator;

    @CommandLine.Option(names = {"i", "input"}, description = "Input file containing pattern to be simulated")
    private File inputFile;

    @CommandLine.Option(names = {"o", "out"}, description = "Output file for the pattern. (default: print to console)")
    private File outputFile;

    @CommandLine.Option(names = {"m", "g", "generation"}, description = "Number of generations to run the pattern")
    private int generations;

    @CommandLine.Option(names = {"s", "step"}, defaultValue = "0",
            description = "Patterns will be printed to the console every step size generations " +
                    "(default: only print the final pattern)")
    private int stepSize;

    @CommandLine.Option(names = {"p", "print"}, defaultValue = "true",
             description = "Print pattern to console (default)")
    private boolean printPattern;

    @Override
    public void run() {
        try {
            // Initialise the Simulator
            simulator = new Simulator(new HROT("B3/S23"));

            // Loading the pattern
            if (inputFile != null) {
                CommandUtils.loadPattern(simulator, inputFile);
            }
            else {
                System.out.println("Input file must be specified!");
                System.exit(-1);
            }

            for (int i = 0; i < generations; i++) {
                // Printing every stepSize
                if (stepSize != 0 && outputFile == null && i % stepSize == 0 && i != 0) {
                    System.out.println(getRLE());
                }

                simulator.step();  // Stepping forward 1 generation
            }

            if (outputFile != null) savePattern(outputFile);
            if (printPattern && outputFile == null) System.out.println(getRLE());
        }
        catch (FileNotFoundException exception) {
            System.out.println("Input / Output file could not be found!");
            System.exit(-1);
        }
        catch (IOException exception) {
            exception.printStackTrace();
            System.exit(-1);
        }

        System.exit(0);
    }

    public String getRLE() {
        // Getting bounding box
        simulator.updateBounds();
        Pair<Coordinate, Coordinate> bounds = simulator.getBounds();

        Coordinate start = bounds.getValue0();
        Coordinate end = bounds.getValue1();

        // Add header & comments
        String rle = simulator.toRLE(start, end);
        StringBuilder rleFinal = new StringBuilder();

        // Adding comments
        String[] comments = ((RuleFamily) simulator.getRule()).generateComments();
        if (comments != null) {
            for (String comment: comments) {
                rleFinal.append(comment).append("\n");
            }
        }

        // Adding header
        rleFinal.append("x = ").append(end.getX() - start.getX()).
                append(", y = ").append(end.getY() - start.getY()).
                append(", rule = ").append(((RuleFamily) simulator.getRule()).getRulestring()).append("\n");
        rleFinal.append(rle);

        return rleFinal.toString();
    }
    
    public void savePattern(File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(getRLE());
        fileWriter.close();
    }
}
