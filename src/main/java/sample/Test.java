package sample;

import sample.model.database.GliderDBReader;

import java.io.File;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        GliderDBReader reader = new GliderDBReader(
                new File("C:/Users/jedli/Documents/CA/GliderDB/R2-C2-NM-oscillators.db.txt"));
        reader.canoniseDB(new File("C:/Users/jedli/Documents/CA/GliderDB/R2-C2-NM-oscillators-canon.db.txt"));
    }
}
