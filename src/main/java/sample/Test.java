package sample;

import sample.model.rules.isotropic.transitions.R2CrossINT;

public class Test {
    public static void main(String[] args) {
        System.out.println(new R2CrossINT("1").getSortedTransitionTable());
    }
}
