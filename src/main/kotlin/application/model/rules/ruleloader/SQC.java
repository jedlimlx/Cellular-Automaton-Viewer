package application.model.rules.ruleloader;

import application.model.Coordinate;

/**
 * Implements Square Cell Ruletables (@SQC) which are essentially transition tables
 * that work based on neighbourhood sum. <br>
 * <br>_
 * Example: <br>
 * <pre>
 * \@SQC
 * neighborhood:[(0, 0), (-2, 0), (-1, 0), (0, -2), (0, -1), (0, 1), (0, 2), (1, 0), (2, 0), (0, 0)]
 * state_weights:0,1
 * 0,0,1,0,0,0,0,0
 * 0,0,0,0,0,0,0,0
 * </pre>
 */
public class SQC extends RuleDirective {
    private final String content;

    private Coordinate[] neighbourhood;
    private int[] weights;
    private int[] stateWeights;

    private int[][] ruletable;

    public SQC(String content) {
        super(content);

        this.content = content;
        directiveName = "SQC";
    }

    @Override
    public void parseContent(String content) {
        int row = 0;  // The row of the ruletable
        for (String line: content.split("\n")) {
            if (line.startsWith("neighbourhood") || line.startsWith("neighborhood")) {
                neighbourhood = getNeighbourhood(line);
                weights = getWeights(line);
            }
            else if (line.startsWith("state_weights")) {
                stateWeights = getStateWeights(line);
                numStates = stateWeights.length;

                ruletable = new int[stateWeights.length][];
            }
            else if (line.startsWith("tiling")) {
                tiling = getTiling(line);
            }
            else if (line.matches("(\\d+,?)+")) {
                String[] tokens = line.split(",\\s*");

                // Creating a new row
                ruletable[row] = new int[tokens.length];

                for (int i = 0; i < tokens.length; i++) {
                    ruletable[row][i] = Integer.parseInt(tokens[i]);
                }

                row++;
            }
        }
    }

    @Override
    public Coordinate[] getNeighbourhood() {
        return neighbourhood;
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState) {
        int sum = 0;
        for (int i = 0; i < neighbours.length; i++) {
            sum += stateWeights[neighbours[i]] * weights[i];
        }

        if (cellState >= ruletable.length) return 0;
        if (sum >= ruletable[cellState].length) return 0;
        return ruletable[cellState][sum];
    }

    @Override
    public Object clone() {
        return new SQC(content);
    }
}
