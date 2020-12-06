package sample.model.rules.ruleloader.ruletree;

import sample.model.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

class Key {
   public int[] arr;
   public Key(int[] arr) {
      this.arr = arr;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Key key = (Key) o;
      return Arrays.equals(arr, key.arr);
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(arr);
   }
}

/**
 * Mostly taken from Golly
 */
public class RuleTreeGen {
   private final int numStates;
   private final Coordinate[] neighbourhood;
   private final BiFunction<Integer, int[], Integer> f;

   private int nodeSeq = 0;
   private final int[] params;
   private final ArrayList<Key> r = new ArrayList<>();
   private final HashMap<Key, Integer> world = new HashMap<>();

   private final Logger logger;

   /**
    * Constructs a rule tree with the provided parameters
    * @param numStates The number of states in the ruletree
    * @param neighbourhood The neighbourhood of the ruletree
    * @param f The transition function of the ruletree
    */
   public RuleTreeGen(int numStates, Coordinate[] neighbourhood, BiFunction<Integer, int[], Integer> f) {
      this.f = f;
      this.numStates = numStates;
      this.neighbourhood = neighbourhood;

      params = new int[neighbourhood.length + 1];
      logger = LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);
   }

   private int getNode(Key n) {
      Integer found = world.get(n);
      if (found == null) {
         found = nodeSeq++;
         r.add(n);
         world.put(n, found);
      }

      return found;
   }

   private int recur(int at) {
      if (at == 0)
         return f.apply(params[params.length - 1], Arrays.copyOfRange(params, 0, params.length - 1));

      int[] arr = new int[numStates + 1];
      arr[0] = at;
      for (int i = 0; i < numStates; i++) {
         params[neighbourhood.length + 1 - at] = i;
         arr[i + 1] = recur(at - 1);
      }

      return getNode(new Key(arr));
   }

   public Ruletree getRuleTree() {
      // Generating the rule tree
      long startTime = System.currentTimeMillis();
      recur(neighbourhood.length + 1);
      logger.log(Level.INFO, "Rule tree generation took " + (System.currentTimeMillis() - startTime) + " ms");

      // Writing the rule tree headers
      StringBuilder ruletree = new StringBuilder("@TREE\n");
      ruletree.append("num_states=").append(numStates).append("\n");

      String neighbourhoodString = Arrays.toString(neighbourhood);
      ruletree.append("num_neighbors=").append("[").append(neighbourhoodString, 1,
              neighbourhoodString.length() - 1).append(", (0, 0), (0, 0)]\n");  // Add the 2 (0, 0)s at the back
      ruletree.append("num_nodes=").append(r.size()).append("\n");

      // Ruletree body
      for (Key key: r) {
         for (int val: key.arr)
            ruletree.append(val).append(" ");
         ruletree.append("\n");
      }

      Ruletree ruleTree = new Ruletree("");
      ruleTree.parseContent(ruletree.toString());

      return ruleTree;
   }
}
