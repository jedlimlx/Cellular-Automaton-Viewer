package sample.model.rules.ruleloader.ruletree;

import sample.model.Coordinate;
import sample.model.rules.ruleloader.Exportable;
import sample.model.rules.ruleloader.RuleDirective;

import java.util.Arrays;

/**
 * Implements Golly ruletrees with additional features such as arbitary neighbourhoods.
 * See http://golly.sourceforge.net/Help/formats.html for more information.
 */
public class Ruletree extends RuleDirective implements Exportable {
    /**
     * Content of the ruletree
     */
    private String content;

    /**
     * List of nodes used by the ruletree
     */
    private Node[] nodeList;

    /**
     * Neighbourhood of the ruletree
     */
    private Coordinate[] neighbourhood;

    /**
     * Constructs a ruletree with the provided content
     * @param content Content to use to construct the ruletree
     */
    public Ruletree(String content) {
        super(content);
    }


    /**
     * Parses the content of the ruletree
     * @param content The content of the ruletree
     */
    @Override
    public void parseContent(String content) {
        this.content = content;

        int nodeNumber = 0;  // Keep track of the current node number
        for (String line: content.split("\n")) {
            if (line.startsWith("num_states")) {
                numStates = Integer.parseInt(line.replace("num_states=", ""));
            }
            else if (line.startsWith("num_neighbo")) {  // Account for british and american spelling
                neighbourhood = getNeighbourhood(line.replaceAll("num_neighbou?rs=", ""));
            }
            else if (line.startsWith("num_nodes")) {
                nodeList = new Node[Integer.parseInt(line.replace("num_nodes=", ""))];
            }
            else if (line.startsWith("tiling")) {
                tiling = getTiling(line.replace("tiling=", ""));
            }
            else if (line.matches("(\\d+\\s?)+")) {
                String[] tokens = line.split("\\s+");
                int[] children = new int[tokens.length - 1];
                for (int i = 1; i < tokens.length; i++) {
                    children[i - 1] = Integer.parseInt(tokens[i]);
                }

                nodeList[nodeNumber] = new Node(Integer.parseInt(tokens[0]), nodeNumber, children);
                nodeNumber++;
            }
        }
    }

    @Override
    public Coordinate[] getNeighbourhood() {
        return neighbourhood;
    }

    @Override
    public int transitionFunc(int[] neighbours, int cellState) {
        int currentNodeNumber = nodeList.length - 1;  // Begin at root node
        for (int neighbour: neighbours) {  // Going down the tree
            currentNodeNumber = nodeList[currentNodeNumber].getChildren()[neighbour];
        }

        return nodeList[currentNodeNumber].getChildren()[cellState];  // Finally get the value
    }

    @Override
    public Object clone() {
        return new Ruletree(content);
    }

    @Override
    public String export() {
        return content;
    }
}
