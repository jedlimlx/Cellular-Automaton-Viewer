package application.model.rules.isotropic.rules.history;

import javafx.scene.paint.Color;
import application.model.Coordinate;
import application.model.rules.isotropic.rules.INT;

/**
 * Implements INT History
 */
public class INTHistory extends INT {
    /**
     * Creates an INT History rule with the rule LifeHistory
     */
    public INTHistory() {
        this("B3/S23History");
    }

    /**
     * Creates an INT History rule with the specified rulestring
     * @param rulestring The rulestring of the history rule
     */
    public INTHistory(String rulestring) {
        super(rulestring);

        name = "INT History";
        numStates = 7;
    }

    /**
     * Loads the rule's parameters from a rulestring
     * @param rulestring The rulestring of the INT History rule (eg. B3/S23History, B2n3/S23-qHistory)
     * @throws IllegalArgumentException Thrown if an invalid rulestring is passed in
     */
    @Override
    protected void fromRulestring(String rulestring) throws IllegalArgumentException {
        super.fromRulestring(rulestring.replace("History", ""));
    }

    /**
     * Canonises the inputted rulestring with the currently loaded parameters.
     * @param rulestring The rulestring to canonised
     * @return Canonised rulestring
     */
    @Override
    public String canonise(String rulestring) {
        return super.canonise(rulestring.replace("History", "")) + "History";
    }

    /**
     * The regexes that will match a valid rulestring
     * @return An array of regexes that will match a valid rulestring
     */
    @Override
    public String[] getRegex() {
        String[] prevRegex = super.getRegex();
        String[] updatedRegex = new String[prevRegex.length];
        for (int i = 0; i < prevRegex.length; i++) {  // Regex is [R]History
            updatedRegex[i] = prevRegex[i] + "History";
        }

        return updatedRegex;
    }

    /**
     * Returns a plain text description of the INT History rule family to be displayed in the Rule Dialog
     * @return Description of the INT History rule family
     */
    @Override
    public String getDescription() {
        return "This implements 2-state Isotropic Non-Totalistic (INT) History rules.\n" +
                "B0 rules are supported via emulation with alternating rules.\n\n" +
                "The format is as follows:\n" +
                "B<birth>/S<survival>/N<neighbourhood>";
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState, int generations, Coordinate coordinate) {
        if (cellState == 6) return cellState;

        int sum = 0;
        int[] binaryNeighbours = new int[neighbours.length];  // Just 1s and 0s
        for (int i = 0; i < neighbours.length; i++) {
            if (neighbours[i] == 6 && cellState % 2 == 1) return 2;
            binaryNeighbours[i] = neighbours[i] % 2;
        }

        if (cellState == 1) {  // Check Survival
            if (survival.checkTransition(binaryNeighbours)) return cellState;
            else return 2;
        }
        else if (cellState == 3 || cellState == 5) {  // Check Survival
            if (survival.checkTransition(binaryNeighbours)) return cellState;
            else return 4;
        }
        else if (cellState == 0 || cellState == 2) {  // Check Birth
            if (birth.checkTransition(binaryNeighbours)) return 1;
            else return cellState;
        }
        else if (cellState == 4) {  // Check Birth
            if (birth.checkTransition(binaryNeighbours)) return 3;
            else return 4;
        }
        else {
            return 0;
        }
    }

    /**
     * Returns the colour of a cell of the provided state
     * @param state The state of the cell
     * @return The colour of the cell
     */
    @Override
    public Color getColour(int state) {
        if (state == 0) return Color.BLACK;
        else if (state == 1) return Color.rgb(0, 255, 0);
        else if (state == 2) return Color.rgb(0, 0, 128);
        else if (state == 3) return Color.rgb(216, 255, 216);
        else if (state == 4) return Color.rgb(255, 0, 0);
        else if (state == 5) return Color.rgb(255, 255, 0);
        else return Color.rgb(96, 96, 96);
    }

    @Override
    public int dependsOnNeighbours(int state, int generation, Coordinate coordinate) {
        if (state == 6) return 6;
        return super.dependsOnNeighbours(state, generation, coordinate);
    }

    @Override
    public Object clone() {
        return new INTHistory(rulestring);
    }
}
