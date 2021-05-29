package application;

import application.model.Utils;
import application.model.rules.RuleFamily;
import application.model.rules.misc.naive.ReadingOrder;
import application.model.simulation.bounds.BoundedGrid;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Regex {
    public static void main(String[] args) throws IOException {
        FileWriter writer = new FileWriter(new File("regex.txt"));
        for (RuleFamily family: Utils.ruleFamilies) {
            for (String string: family.getRegex()) {
                writer.write(string + "\n");
            }
        }

        writer.close();

        writer = new FileWriter(new File("regex2.txt"));
        for (BoundedGrid grid: Utils.boundedGrids) {
            for (String string: grid.getRegex()) {
                writer.write(string + "\n");
            }
        }

        writer.close();


        writer = new FileWriter(new File("regex3.txt"));
        for (ReadingOrder readingOrder: Utils.readingOrders) {
            for (String string: readingOrder.getRegex()) {
                writer.write(string + "\n");
            }
        }

        writer.close();
    }
}
