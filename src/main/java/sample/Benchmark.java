package sample;

import sample.model.Coordinate;
import sample.model.rules.hrot.HROT;
import sample.model.simulation.Simulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Benchmark {
    public static void main(String[] args) throws IOException {
        File file = new File("benchmark.txt");
        FileWriter fileWriter = new FileWriter(file);

        for (int j = 1; j < 6; j++) {
            fileWriter.write("Run " + j + "...\n");

            Simulator simulator = new Simulator(new HROT("R10,C2,S23-28,B72-133,NB"));
            simulator.fromRLE("23bobo$22bobobobo$21bobobobobo$20bobobobobobo$19bobobobobobobo$18bobobobobobobobo$17bobobobobobobobobo$15bob4obobobobobobobo$20b2obobobobobobo$16bobobo5bobobobobo$15bobobobo5bobobobobo$16bobobobo5bobobobo$15bobobobo5bobobobobo$11bobo2bobobobobo3bobobobo$8bobobo4bobobobobobobobobo$7b3obobobo2bobobobob3obobo$6bobobobobobo2bobobobo2b2obo$5bobobobobobobo2bobobobobobo$4bobobobobobobobo2bobobo2b2o$3bobobob3obobobobo2bobobo$2bobobobob3obobobobo2bobo2bo$3bobobobobobobobobo$2bobobobo5bobobobobo$bobobobo7bobobobo$2bobobobo5b3obobobo$bobobobobo5b3obobo$2bobobobobo3bobobobo$3bobobobobobobobob3o$2bobobobobobobobobobo$3bobobobobobobobobo$4bobobobobobobobo$5bobobobobobobo$6bobobobobobo$7bobobobobo$10bobo!",
                    new Coordinate());

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < 100; i++) {
                simulator.step();
            }

            fileWriter.write("Took " + (System.currentTimeMillis() - startTime) / 1000.0 + " s\n");
        }

        fileWriter.close();
    }
}
