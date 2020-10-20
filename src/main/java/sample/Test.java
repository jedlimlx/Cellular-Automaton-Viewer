package sample;

import sample.model.database.SOSSPReader;
import sample.model.patterns.Oscillator;

import java.io.File;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        SOSSPReader reader = new SOSSPReader(new File("sossp.sss.txt"));

        Oscillator oscillator = reader.getOscByPeriod(2047);
        System.out.println(oscillator.getRule() + " " + oscillator.toRLE() + " " + oscillator.getPeriod());
    }
}
