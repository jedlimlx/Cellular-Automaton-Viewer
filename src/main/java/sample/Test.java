package sample;

import sample.model.Coordinate;
import sample.model.Grid;
import sample.model.SymmetryGenerator;

import java.io.FileWriter;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        Grid grid = new Grid();
        grid.fromRLE("bo$obo$obo!", new Coordinate(0, 0));

        Grid grid2 = new Grid();
        grid2.fromRLE("3o$obo2$bo!", new Coordinate(0, 0));

        FileWriter writer = new FileWriter("synth.txt");
        for (int i = 0; i < 100000; i++) {
            writer.write("x = 0, y = 0, rule = Minibugs\n");
            writer.write(SymmetryGenerator.generateSynth(new Grid[]{grid, grid2}, 40, 60, 60).toRLE() + "\n");
            if (i % 1000 == 0) System.out.println(i);
        }
        writer.close();
    }
}
