package sample;

import sample.model.rules.HROT;

public class Test {
    public static void main(String[] args) {
        System.out.print(new HROT("B3/S23").validMinMax(
                new HROT("B36/S238"), new HROT("B3678/S23678")));
    }
}
