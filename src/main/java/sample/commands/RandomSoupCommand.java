package sample.commands;

import picocli.CommandLine;
import sample.model.SymmetryGenerator;

@CommandLine.Command(name = "rand", aliases = {"random"}, description = "Generates random soups to pipe into apgluxe")
public class RandomSoupCommand implements Runnable {
    @CommandLine.Option(names = {"-n", "--num"}, description = "The number of soups to generate " +
            "(default: -1, generate soups forever)", defaultValue = "-1")
    private int num;
    
    @CommandLine.Option(names = {"-d", "--density"}, description = "The density of the random soup (default: 50)",
            defaultValue = "50")
    private int density;
    
    @CommandLine.Option(names = {"-x", "--width"}, description = "The width of the random soup (default: 16)",
            defaultValue = "16")
    private int x;
    
    @CommandLine.Option(names = {"-y", "--height"}, description = "The height of the random soup (default: 16)",
            defaultValue = "16")
    private int y;
    
    @CommandLine.Option(names = {"-s", "--symmetry"}, description = "The symmetry of the random soup. " +
            "[C1, D2-, D4+] (default: C1)", defaultValue = "C1")
    private String symmetry;

    @CommandLine.Option(names = {"-S", "--states"}, description = "The states to include in the random soup (default: 1)",
            defaultValue = "1")
    private int[] states;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help;

    @Override
    public void run() {
        if (num == -1) {
            while (true) {
                System.out.println("x = " + x + ", y = " + y + ", rule = B3/S23");
                System.out.println(SymmetryGenerator.generateSymmetry(symmetry, density, states, x, y).toRLE());
            }
        }
        else {
            for (int i = 0; i < num; i++) {
                System.out.println("x = " + x + ", y = " + y + ", rule = B3/S23");
                System.out.println(SymmetryGenerator.generateSymmetry(symmetry, density, states, x, y).toRLE());
            }
        }

        System.exit(0);
    }
}
