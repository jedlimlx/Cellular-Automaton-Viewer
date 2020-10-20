package sample;

import sample.model.Utils;
import sample.model.rules.RuleFamily;

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
    }
}
