package sample.model.search.cfind;

import java.util.ArrayList;
import java.util.List;

/**
 * A state (p rows / columns) representing a single row across all phases
 */
public class State {
    /**
     * The list of rows in the state
     */
    private final List<List<Integer>> state;

    /**
     * Pointer to the state's predecessors
     */
    private State predecessor;

    /**
     * Possible extensions of this state
     */
    private List<State> extensions;

    /**
     * The depth of the state in the de Brujin Graph
     */
    private final int depth;

    /**
     * Constructs a state
     * @param state A list of list of integers representing the rows in the state
     * @param predecessor The state's predecessor
     */
    public State(List<List<Integer>> state, State predecessor) {
        this.state = state;
        this.extensions = null;
        this.predecessor = predecessor;

        if (this.predecessor == null) this.depth = 0;
        else this.depth = this.predecessor.getDepth() + 1;
    }

    /**
     * Finds the states / states that this states can to extended to
     * @param width The width of the extension
     * @return Returns an array of possible successor states
     */
    public List<State> extend(int width) {
        List<State> extensions = new ArrayList<>();

        this.extensions = extensions;
        return extensions;
    }

    public State getPredecessor() {
        return predecessor;
    }

    public List<State> getExtensions() {
        return extensions;
    }

    public List<Integer> getPhase(int phase) {
        return state.get(phase);
    }

    public int getDepth() {
        return depth;
    }

    public void setPredecessor(State predecessor) {
        this.predecessor = predecessor;
    }

    public void setExtensions(List<State> extensions) {
        this.extensions = extensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state1 = (State) o;
        return state1.state.equals(this.state);
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }
}
