package sample;

import sample.model.Coordinate;
import sample.model.rules.hrot.IntegerHROT;

import java.util.ArrayList;
import java.util.HashMap;
public class RuleTreeGen {
    /* Put your state count, neighbor count, and function here */
    final static int numStates = 5;
    final static int numNeighbors = 8;
    /* order for nine neighbors is nw, ne, sw, se, n, w, e, s, c */
    /* order for five neighbors is n, w, e, s, c */
    int f(int[] a) {
        int[] neighbours = new int[8];
        System.arraycopy(a, 0, neighbours, 0, 8);

        IntegerHROT hrot = new IntegerHROT("R1,I5,S2,3,B3,N+");
        return hrot.transitionFunc(neighbours, a[8], 0, new Coordinate(0, 0));
    }

    final static int numParams = numNeighbors + 1;
    HashMap<String, Integer> world = new HashMap<>();
    ArrayList<String> r = new ArrayList<>();
    int[] params = new int[numParams];
    int nodeSeq = 0;

    int getNode(String n) {
        Integer found = world.get(n);
        if (found == null) {
            found = nodeSeq++;
            r.add(n);
            world.put(n, found);
        }
        return found;
    }

    int recur(int at) {
        if (at == 0) return f(params);
        StringBuilder n = new StringBuilder("" + at);
        for (int i = 0; i < numStates; i++) {
            params[numParams - at] = i;
            n.append(" ").append(recur(at - 1));
        }
        return getNode(n.toString());
    }

    void writeRuleTree() {
        System.out.println("num_states=" + numStates) ;
        System.out.println("num_neighbors=" + numNeighbors) ;
        System.out.println("num_nodes=" + r.size()) ;
        for (String s : r) System.out.println(s);
    }

    public static void main(String[] args) {
        RuleTreeGen rtg = new RuleTreeGen();
        rtg.recur(numParams);
        rtg.writeRuleTree();
    }
}
