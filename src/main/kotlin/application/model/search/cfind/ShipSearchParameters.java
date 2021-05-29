package application.model.search.cfind;

import application.model.rules.Rule;
import application.model.search.SearchParameters;

/**
 * The parameters for CAViewer's ship search program - cfind.
 */
public class ShipSearchParameters extends SearchParameters {
    private final Rule rule;
    private final int dx, dy, period;

    public ShipSearchParameters(Rule rule, int dx, int dy, int period) {
        this.rule = rule;
        this.dx = dx;
        this.dy = dy;
        this.period = period;
    }

    public Rule getRule() {
        return rule;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getPeriod() {
        return period;
    }
}
