package sample;

import sample.model.rules.isotropic.R1MooreINT;

public class Test {
    public static void main(String[] args) {
        R1MooreINT mooreINT = new R1MooreINT("2n3");
        System.out.println(mooreINT.getSortedTransitionTable());
    }
}
