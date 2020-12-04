package sample.model.rules.ruleloader.ruletree;

import sample.model.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Mostly taken from Golly
 */
public class RuleTreeGen {
   private final int numStates;
   private final Coordinate[] neighbourhood;
   private final BiFunction<Integer, int[], Integer> f;

   private int nodeSeq = 0;
   private final int[] params;
   private final ArrayList<String> r = new ArrayList<>();
   private final HashMap<String, Integer> world = new HashMap<>();

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

   private int getNode(String n) {
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

      StringBuilder n = new StringBuilder("" + at);
      for (int i = 0; i < numStates; i++) {
         params[neighbourhood.length + 1 - at] = i;
         n.append(" ").append(recur(at - 1));
      }

      return getNode(n.toString());
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
      ruletree.append("num_neighbors=").append("[(0, 0), ").append(neighbourhoodString, 1,
              neighbourhoodString.length() - 1).append(", (0, 0)]\n");  // Add the (0, 0) at the front and back
      ruletree.append("num_nodes=").append(r.size()).append("\n");

      // Ruletree body
      for (String s : r) ruletree.append(s).append("\n");

      Ruletree ruleTree = new Ruletree("");
      ruleTree.parseContent(ruletree.toString());

      return ruleTree;
   }
}
