package sample;

import sample.model.rules.ruleloader.RuleLoader;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        System.out.println(Arrays.toString(new RuleLoader().getRegex()));
    }
}
