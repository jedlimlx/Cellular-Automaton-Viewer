package application.model.rules.ruleloader.ruletree;

import java.util.Arrays;

public class Node {
    int level, nodeNumber;
    int[] children;

    public Node(int level, int nodeNumber, int[] children) {
        this.level = level;
        this.nodeNumber = nodeNumber;
        this.children = children;
    }

    public int getLevel() {
        return level;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public int[] getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "Node{" +
                "level=" + level +
                ", nodeNumber=" + nodeNumber +
                ", children=" + Arrays.toString(children) +
                '}';
    }
}
