package sample.commands;

import org.javatuples.Pair;
import picocli.CommandLine;
import sample.model.Coordinate;
import sample.model.Simulator;
import sample.model.rules.HROT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

@CommandLine.Command(name = "sim", aliases = {"run"}, description = "Simulate a pattern")
public class SimulationCommand implements Runnable {
    private Simulator simulator;

    @CommandLine.Option(names = {"-i", "--input"},
            description = "Input file containing pattern to be simulated", required = true)
    private File inputFile;

    @CommandLine.Option(names = {"-o", "--out"}, description = "Output file for the pattern.", required = true)
    private File outputFile;

    @CommandLine.Option(names = {"-m", "-g", "--generation"},
            description = "Number of generations to run the pattern", required = true)
    private int generations;

    @CommandLine.Option(names = {"-s", "--step"}, defaultValue = "1",
            description = "Patterns will be printed to the console every step size generations " +
                    "(default: 1)")
    private int stepSize;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help;

    private Coordinate startingCoordinate;
    private StringBuilder rleFinal = new StringBuilder();

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
                System.err.println("Input file must be specified!");
                System.exit(-1);
            }

            simulator.updateBounds();
            startingCoordinate = simulator.getBounds().getValue0();

            for (int i = 0; i < generations; i++) {
                // Printing every stepSize
                if (stepSize != 0 && i % stepSize == 0 && i != 0) {
                    getRLE();
                }

                simulator.step();  // Stepping forward 1 generation
            }

            savePattern(outputFile);
        }
        catch (FileNotFoundException exception) {
            System.err.println("Input / Output file could not be found!");
            System.exit(-1);
        }
        catch (IOException exception) {
            exception.printStackTrace();
            System.exit(-1);
        }

        System.exit(0);
    }

    public void getRLE() {
        // Getting bounding box
        simulator.updateBounds();
        Pair<Coordinate, Coordinate> bounds = simulator.getBounds();

        Coordinate start = bounds.getValue0();
        Coordinate end = bounds.getValue1();

        // Add header & comments
        String rle = simulator.toRLE(start, end);

        // Adding header
        rleFinal.append(start.getX() - startingCoordinate.getX()).append(",").
                append(start.getY() - startingCoordinate.getY()).append("\n");
        rleFinal.append(end.getX() - start.getX()).append(",").
                append(end.getY() - start.getY()).append("\n");
        rleFinal.append(rle).append("\n");
    }
    
    public void savePattern(File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(rleFinal.toString());
        fileWriter.close();
    }
}