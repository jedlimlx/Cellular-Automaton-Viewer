package sample.model.search.cfind;

import sample.model.LRUCache;
import sample.model.search.SearchProgram;

import java.io.File;
import java.util.*;

/**
 * Implements CAViewer's gfind-like ship search program - cfind for multi-state and higher range rules.
 */
public class ShipSearch extends SearchProgram {
    /**
     * Constructs the ship search program with the provided parameters
     * @param parameters The parameters of the search program
     */
    public ShipSearch(ShipSearchParameters parameters) {
        super(parameters);
    }

    @Override
    public void search(int num) {
        searchThreaded(num, 1);
    }

    @Override
    public void searchThreaded(int num, int numThreads) {
        ShipSearchParameters searchParameters = (ShipSearchParameters) getSearchParameters();

        int maxQueueSize = (int) Math.pow(2, 22);
        int minDeepeningIncrement = searchParameters.getPeriod();
        Queue<State> bfsQueue = new LinkedList<>();

        // Transposition table to detect equivalent states
        LRUCache<List<State>, List<State>> transpositionTable = new LRUCache<>((int) Math.pow(2, 21));

        // Initial width
        int width = 1, treeSize = 0;

        // Adding the initial row (blank row)
        bfsQueue.add(new State(null, null));

        State next;
        List<State> extensions, lastRows, newStates;

        while (true) {
            // Run BFS until the queue exceeds maxQueueSize
            while (!bfsQueue.isEmpty() || bfsQueue.size() < maxQueueSize) {
                next = bfsQueue.poll();

                // Check for equivalent states with the transposition table
                lastRows = new ArrayList<>();
                lastRows.add(next);  // Looking at the last rp rows (r - range, p - period)
                for (int i = 0; i < 1; i++) {
                    lastRows.add(lastRows.get(lastRows.size() - 1).getPredecessor());
                }

                // Checking the transposition table
                if (transpositionTable.containsKey(lastRows)) {
                    // Eliminate the longer of the 2 since that will not lead to the
                    // shortest spaceship for that rule and period
                    newStates = new ArrayList<>();
                    for (State state: transpositionTable.get(lastRows)) {
                        if (next.getDepth() + 1 < state.getDepth()) {
                            state.setPredecessor(next);
                            newStates.add(state);
                        }
                    }

                    next.setExtensions(newStates);
                    transpositionTable.put(lastRows, newStates);

                    // Move onto the next node
                    continue;
                }

                // Extend the node
                extensions = next.extend(width);

                // Adding last rp rows to the transposition table
                transpositionTable.put(lastRows, extensions);

                for (State extension: extensions) {
                    treeSize++;
                    bfsQueue.add(extension);
                }
            }

            if (bfsQueue.isEmpty()) {  // TODO (Adaptive Widening)
                System.out.println("Search complete.");
                return;
            }
            else {
                System.out.print("Queue full, beginning deepening step, " + bfsQueue.size() + " / " + treeSize);

                // Run DFS to decrease the size of the BFS Queue
                while (bfsQueue.iterator().hasNext()) {
                    next = bfsQueue.iterator().next();

                    // Stack to store states to backtrack to
                    State current;
                    Stack<State> stack = new Stack<>();
                    stack.add(next);
                    for (int i = 0; i < minDeepeningIncrement; i++) {
                        current = stack.pop();
                        extensions = current.extend(width);
                        if (extensions.size() > 0) stack.add(current);
                    }
                }

                System.out.print(" -> " + bfsQueue.size() + " / " + treeSize);
            }
        }
    }

    @Override
    public boolean writeToFile(File file) {
        return false;
    }
}
