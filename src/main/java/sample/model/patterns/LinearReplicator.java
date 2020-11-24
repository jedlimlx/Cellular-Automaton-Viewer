package sample.model.patterns;

import org.javatuples.Pair;
import sample.model.Coordinate;
import sample.model.rules.Rule;
import sample.model.simulation.Grid;

import java.util.List;
import java.util.Map;

/**
 * Represents a linear replicator
 */
public class LinearReplicator extends Pattern {
    /**
     * Replication rule of the replicator
     */
    private final List<Integer> replicationRule;

    /**
     * The period of replication of the replicator
     */
    private final int period;

    /**
     * Constructs a linear replicator
     * @param rule The rule the replicator works in
     * @param pattern The replicator pattern
     * @param replicationRule The replicator's replication rule
     * @param period The replicator's period
     */
    public LinearReplicator(Rule rule, Grid pattern, List<Integer> replicationRule, int period) {
        super(rule);

        this.replicationRule = replicationRule;
        this.period = period;

        this.insertCells(pattern, new Coordinate(0, 0));
    }

    @Override
    public String toString() {
        return "P " + period + " replicator";
    }

    @Override
    public Map<String, String> additionalInfo() {

        return null;
    }

    public LinearReplicator checkPopulation(int[] populationList) {
        return null;  // TODO (Work out how to make this work)
    }
}
