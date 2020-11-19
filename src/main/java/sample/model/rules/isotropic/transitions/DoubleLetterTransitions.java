package sample.model.rules.isotropic.transitions;

import sample.model.Coordinate;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class DoubleLetterTransitions extends INTTransitions {
    protected HashMap<Integer, HashMap<Character, ArrayList<Integer>>> anisotropicTransitionLookup = null;
    protected HashMap<Integer, HashMap<Character, ArrayList<Integer>>> isotropicTransitionLookup = null;
    protected HashMap<ArrayList<Integer>, String> reverseAnisotropicTransitionLookup = null;
    protected HashMap<ArrayList<Integer>, String> reverseIsotropicTransitionLookup = null;

    protected HashMap<ArrayList<Integer>, String> reverseTransitionLookup = null;

    protected Coordinate[] anistropicNeighbourhood, isotropicNeighbourhood;

    /**
     * Constructs the INT transitions that consist of 2 letters
     * @param string The string representation of the INT transitions
     */
    public DoubleLetterTransitions(String string) {
        super(string);
    }

    /**
     * Reads the transitions from 2 files with the provided filename and loads them into 2 transition lookups
     * @param stream The input stream of the file with the anisotropic transitions
     * @param stream2 The input stream of the file with the isotropic transitions
     */
    public void readTransitionsFromFile(InputStream stream, InputStream stream2) {
        reverseTransitionLookup = new HashMap<>();

        if (anisotropicTransitionLookup == null) {
            Scanner scanner = new Scanner(stream);
            anisotropicTransitionLookup = new HashMap<>();
            reverseAnisotropicTransitionLookup = new HashMap<>();

            String line;
            HashMap<Character, ArrayList<Integer>> hashMap = null;

            int number = 0;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.matches("\\d\\s*")) {
                    hashMap = new HashMap<>();
                    number = Integer.parseInt(line);
                    anisotropicTransitionLookup.put(number, hashMap);
                }
                else if (!line.matches("\\s*")) {
                    String[] tokens = line.split("\\s+");

                    ArrayList<Integer> transition = new ArrayList<>();
                    for (int i = 1; i < tokens.length; i++) {
                        transition.add(Integer.parseInt(tokens[i]));
                    }

                    assert hashMap != null;
                    hashMap.put(tokens[0].charAt(0), transition);

                    for (ArrayList<Integer> transition2: getSymmetries(transition)) {
                        reverseAnisotropicTransitionLookup.put(transition2, number +
                                "" + tokens[0].charAt(0));
                    }
                }
            }

            scanner.close();
        }

        if (isotropicTransitionLookup == null) {
            Scanner scanner = new Scanner(stream);
            isotropicTransitionLookup = new HashMap<>();
            reverseAnisotropicTransitionLookup = new HashMap<>();

            String line;
            HashMap<Character, ArrayList<Integer>> hashMap = null;

            int number = 0;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.matches("\\d\\s*")) {
                    hashMap = new HashMap<>();
                    number = Integer.parseInt(line);
                    isotropicTransitionLookup.put(number, hashMap);
                }
                else if (!line.matches("\\s*")) {
                    String[] tokens = line.split("\\s+");

                    ArrayList<Integer> transition = new ArrayList<>();
                    for (int i = 1; i < tokens.length; i++) {
                        transition.add(Integer.parseInt(tokens[i]));
                    }

                    assert hashMap != null;
                    hashMap.put(tokens[0].charAt(0), transition);

                    for (ArrayList<Integer> transition2: getSymmetries(transition)) {
                        reverseIsotropicTransitionLookup.put(transition2, number +
                                "" + tokens[0].charAt(0));
                    }
                }
            }

            scanner.close();
        }
    }

    @Override
    protected void parseTransitions(String string) {

    }

    @Override
    public String canoniseTransitions() {
        return null;
    }

    @Override
    protected ArrayList<ArrayList<Integer>> getSymmetries(ArrayList<Integer> transition) {
        return null;
    }

    @Override
    public String getRegex() {
        return null;
    }

    @Override
    public String getTransitionsFromNeighbours(ArrayList<Integer> neighbours) {
        return null;
    }

    @Override
    public void addTransition(String transition) {

    }

    @Override
    public void removeTransition(String transition) {

    }

    @Override
    public Object clone() {
        return null;
    }
}
