package application.model.rules.ruleloader;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Implements the @COLORS / @COLOURS directive in ruletables.<br>
 * <br>
 * Example: <br>
 * <pre>
 * \@COLORS
 * 0 0 0 0
 * 1 255 255 0
 * 2 0 255 255
 * 3 255 0 255
 * </pre>
 */
public class ColourDirective extends Directive implements Exportable {
    private String content = "";
    private Map<Integer, Color> colourMap;

    public ColourDirective(String content) {
        super(content);

        directiveName = "COLOURS";
    }

    @Override
    public void parseContent(String content) {
        if (content.matches("\\s*")) return;

        this.content = content;

        colourMap = new HashMap<>();
        for (String line: content.split("\n")) {
            if (line.equals("@COLOURS") || line.equals("@COLORS")) continue;

            try {
                String[] tokens = line.strip().split("\\s*,?\\s+");

                colourMap.put(Integer.parseInt(tokens[0]),
                        Color.rgb(Integer.parseInt(tokens[1]),
                                Integer.parseInt(tokens[2]),
                                Integer.parseInt(tokens[3])));
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).log(
                        Level.WARNING, "Invalid line in @COLORS: " + line);
            }
        }
    }

    /**
     * Gets the colour of a given state of the cell
     * @param state The state of the cell
     * @return Returns the colour of the cell
     */
    public Color getColour(int state) {
        return colourMap.get(state);
    }

    @Override
    public Object clone() {
        return new ColourDirective(content);
    }

    @Override
    public String export() {
        StringBuilder builder = new StringBuilder("@COLORS\n");
        for (int i = 0; i < colourMap.size(); i++) {
            builder.append(i).append(" ").
                    append((int) (colourMap.get(i).getRed() * 255)).append(" ").
                    append((int) (colourMap.get(i).getGreen() * 255)).append(" ").
                    append((int) (colourMap.get(i).getBlue() * 255)).append("\n");
        }

        return builder.toString();
    }
}
