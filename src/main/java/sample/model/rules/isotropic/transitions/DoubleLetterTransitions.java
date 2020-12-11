package sample.model.rules.isotropic.transitions;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements INT transitions that consist of 2 letters (one isotropic, the another anisotropic)
 * such as the range 2 von neumann INT transitions
 */
public abstract class DoubleLetterTransitions extends INTTransitions {
    protected HashMap<Character, ArrayList<Integer>> anisotropicTransitionLookup = null;
    protected HashMap<Integer, HashMap<Character, ArrayList<Integer>>> isotropicTransitionLookup = null;
    protected HashMap<ArrayList<Integer>, String> reverseAnisotropicTransitionLookup = null;
    protected HashMap<ArrayList<Integer>, String> reverseIsotropicTransitionLookup = null;
    protected HashMap<ArrayList<Integer>, String> reverseTransitionLookup = null;

    protected ArrayList<ArrayList<Integer>>[] transitionsByNeighbourhoodCount;

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
        if (anisotropicTransitionLookup == null) {
            Scanner scanner = new Scanner(stream);
            anisotropicTransitionLookup = new HashMap<>();
            reverseAnisotropicTransitionLookup = new HashMap<>();

            String line;

            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (!line.matches("\\s*")) {
                    String[] tokens = line.split("\\s+");

                    ArrayList<Integer> transition = new ArrayList<>();
                    for (int i = 1; i < tokens.length; i++) {
                        transition.add(Integer.parseInt(tokens[i]));
                    }

                    anisotropicTransitionLookup.put(tokens[0].charAt(0), transition);
                    reverseAnisotropicTransitionLookup.put(transition, "" + tokens[0].charAt(0));
                }
            }

            scanner.close();
        }

        if (isotropicTransitionLookup == null) {
            Scanner scanner = new Scanner(stream2);
            isotropicTransitionLookup = new HashMap<>();
            reverseIsotropicTransitionLookup = new HashMap<>();

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

                    for (ArrayList<Integer> transition2: getIsotropicSymmetries(transition)) {
                        reverseIsotropicTransitionLookup.put(transition2, number +
                                "" + tokens[0].charAt(0));
                    }
                }
            }

            scanner.close();
        }

        if (reverseTransitionLookup == null) {
            reverseTransitionLookup = new HashMap<>();
            anisotropicTransitionLookup.keySet().stream().sorted().forEach(anisotropicChar -> {
                for (int i = 0; i <= 8; i++) {
                    int finalI = i;
                    isotropicTransitionLookup.get(i).keySet().stream().sorted().forEach(isotropicChar -> {
                        ArrayList<Integer> transition =
                                (ArrayList<Integer>) isotropicTransitionLookup.get(finalI).get(isotropicChar).clone();
                        transition.addAll(anisotropicTransitionLookup.get(anisotropicChar));

                        if (isotropicChar == '!') isotropicChar = 'x';  // Account for transition 0 and 8
                        for (ArrayList<Integer> transition2: getSymmetries(transition)) {
                            if (reverseTransitionLookup.containsKey(transition2)) continue;
                            reverseTransitionLookup.put(transition2, "" + finalI + isotropicChar + anisotropicChar);
                        }
                    });
                }
            });
        }

        if (transitionsByNeighbourhoodCount == null) {
            transitionsByNeighbourhoodCount = new ArrayList[neighbourhood.length + 1];
            for (int i = 0; i <= neighbourhood.length; i++) {
                transitionsByNeighbourhoodCount[i] = new ArrayList<>();
            }

            int count;
            ArrayList<Integer> neighbours;
            String binaryString, formatSpecifier = "%" + neighbourhood.length + "s";
            for (int i = 0; i < Math.pow(2, neighbourhood.length); i++) {
                binaryString = String.format(formatSpecifier, Integer.toBinaryString(i));

                count = 0;
                neighbours = new ArrayList<>();
                for (int j = 0; j < neighbourhood.length; j++) {
                    if (binaryString.charAt(j) == '1') {
                        count++;
                        neighbours.add(1);
                    } else {
                        neighbours.add(0);
                    }
                }

                transitionsByNeighbourhoodCount[count].add(neighbours);
            }
        }
    }

    /**
     * Parses transitions from the provided string
     * @param string The string representation of the INT transitions
     */
    @Override
    protected void parseTransitions(String string) {
        Matcher matcher = Pattern.compile(getRegex()).matcher(string);
        sortedTransitionTable.clear();

        String transition;

        // Looping through all the transitions
        while (matcher.find()) {
            transition = matcher.group();

            // Checking for negated transitions (eg. 2x-1ec) or outer totalistic transitions
            if (transition.contains("-") || transition.endsWith("x")) {
                int number;
                if (transition.charAt(1) != 'x') {
                    number = Integer.parseInt(transition.charAt(0) + "" + transition.charAt(1));
                } else {
                    number = Integer.parseInt(transition.charAt(0) + "");
                }

                for (ArrayList<Integer> trans: transitionsByNeighbourhoodCount[number])
                    addTransition(trans);

                // Negating the transitions
                if (transition.contains("-")) {
                    Matcher matcher2 = Pattern.compile(getRegex()).matcher(transition.split("-")[1]);
                    while (matcher2.find()) {
                        transition = matcher2.group();

                        String numberString = transition.charAt(0) + "";
                        for (int i = 0; i < transition.length() / 2; i++) {
                            removeTransition(numberString + transition.charAt(i * 2 + 1) +
                                    transition.charAt(i * 2 + 2));
                        }
                    }
                }

            } else {  // Normal transitions
                String numberString = transition.charAt(0) + "";
                for (int i = 0; i < transition.length() / 2; i++) {
                    addTransition(numberString + transition.charAt(i * 2 + 1) + transition.charAt(i * 2 + 2));
                }
            }
        }
    }

    /**
     * Canonises the transitions based on the currently loaded parameters
     * @return Returns the canonised transitions in the form of a string
     */
    @Override
    public String canoniseTransitions() {
        int count;
        int[] total = new int[neighbourhood.length + 1];
        StringBuilder transitionString = new StringBuilder();

        // Counting the number of neighbours in each transition
        for (List<Integer> transition: transitionTable) {
            count = 0;
            for (int state: transition) {
                if (state == 1) count++;
            }

            total[count]++;
        }

        // Decide whether to use negated transitions or not
        int maxPossible;
        StringBuilder transitionBlockString;
        TreeSet<String> sortedTransitions, sortedTransitionTable =
                (TreeSet<String>) this.sortedTransitionTable.clone();
        HashSet<ArrayList<Integer>> transitionTable = (HashSet<ArrayList<Integer>>) this.transitionTable.clone();
        ArrayList<String> transitionBlocks = new ArrayList<>();
        for (int i = 0; i <= neighbourhood.length; i++) {
            maxPossible = nCr(neighbourhood.length, i);
            if (total[i] == maxPossible) {
                transitionBlocks.add(i + "x");
                for (ArrayList<Integer> transition: transitionsByNeighbourhoodCount[i]) {
                    // Removing the transitions use they can't be used the later step
                    transitionTable.remove(transition);
                    sortedTransitionTable.remove(getTransitionsFromNeighbours(transition));
                }
            }
            else if (total[i] > maxPossible / 2) {
                sortedTransitions = new TreeSet<>();  // Storing the transitions in a tree set
                for (ArrayList<Integer> transition: transitionsByNeighbourhoodCount[i]) {
                    if (!transitionTable.contains(transition)) {
                        sortedTransitions.add(getTransitionsFromNeighbours(transition));
                    } else {  // Removing the transitions use they can't be used the later step
                        transitionTable.remove(transition);
                        sortedTransitionTable.remove(getTransitionsFromNeighbours(transition));
                    }
                }

                char currNum = 'a';  // Adding in the negated transitions
                transitionBlockString = new StringBuilder(i + "x-");
                for (String transitionString2: sortedTransitions) {
                    if (currNum != transitionString2.charAt(0)) {
                        transitionBlockString.append(transitionString2);
                        currNum = transitionString2.charAt(0);
                    } else {
                        transitionBlockString.append(transitionString2.substring(1));
                    }
                }

                transitionBlocks.add(transitionBlockString.toString());
            }
        }

        char currNum = 'a';  // For the normal transitions
        transitionBlockString = new StringBuilder();
        for (String transitionString2: sortedTransitionTable) {
            if (currNum != transitionString2.charAt(0)) {
                transitionBlocks.add(transitionBlockString.toString());

                transitionBlockString = new StringBuilder();
                transitionBlockString.append(transitionString2);
                currNum = transitionString2.charAt(0);
            } else {
                transitionBlockString.append(transitionString2.substring(1));
            }
        }

        transitionBlocks.add(transitionBlockString.toString());

        // Finally, adding the transition blocks into the final string
        transitionBlocks.sort((o1, o2) -> {
            try {
                int num1 = o1.length() >= 1 && !(o1.charAt(1) + "").matches("[0-9]+") ?
                        Integer.parseInt(o1.charAt(0) + "") :
                        Integer.parseInt(o1.charAt(0) + "" + o1.charAt(1));
                int num2 = o2.length() >= 1 && !(o2.charAt(1) + "").matches("[0-9]+") ?
                        Integer.parseInt(o2.charAt(0) + "") :
                        Integer.parseInt(o2.charAt(0) + "" + o2.charAt(1));
                if (num1 != num2) return Integer.compare(num1, num2);
                else return o1.compareTo(o2);
            } catch (StringIndexOutOfBoundsException exception) {
                return o1.compareTo(o2);
            }
        });

        for (String transitionBlock: transitionBlocks) {
            transitionString.append(transitionBlock);
        }

        return transitionString.toString();
    }

    /**
     * Gets the regex to identify a single transition block (number + characters)
     * @return Returns the regex that identifies that transition block
     */
    @Override
    public String getRegex() {
        StringBuilder ordinaryTransitions = new StringBuilder("[0-8]([x");
        for (char isotropicChar: isotropicTransitionLookup.get(isotropicTransitionLookup.
                keySet().size() / 2).keySet()) {
            ordinaryTransitions.append(isotropicChar);
        }

        ordinaryTransitions.append("][");
        for (char anisotropicChar: anisotropicTransitionLookup.keySet()) {
            ordinaryTransitions.append(anisotropicChar);
        }

        ordinaryTransitions.append("])+");

        return "[0-9]+x-(" + ordinaryTransitions + ")+|" + ordinaryTransitions + "|[0-9]+x";
    }

    /**
     * Gets the INT transition from the neighbours
     * @param neighbours The neighbours of the cell
     * @return Returns the INT transition (e.g. 2aa, 3ca, 8xl)
     */
    @Override
    public String getTransitionsFromNeighbours(ArrayList<Integer> neighbours) {
        return reverseTransitionLookup.get(neighbours);
    }

    /**
     * Adds an INT transition
     * @param transition The INT transition to add
     */
    @Override
    public void addTransition(String transition) {
        int number = Integer.parseInt(transition.charAt(0) + "");
        char isotropicChar = transition.charAt(1);
        char anisotropicChar = transition.charAt(2);

        if (isotropicChar == 'x') isotropicChar = '!';

        ArrayList<Integer> neighbours =
                (ArrayList<Integer>) isotropicTransitionLookup.get(number).get(isotropicChar).clone();
        neighbours.addAll(anisotropicTransitionLookup.get(anisotropicChar));

        transitionTable.addAll(getSymmetries(neighbours));
        sortedTransitionTable.add(reverseTransitionLookup.get(neighbours));
    }

    /**
     * Removes an INT transition
     * @param transition The INT transition to remove
     */
    @Override
    public void removeTransition(String transition) {
        int number = Integer.parseInt(transition.charAt(0) + "");
        char isotropicChar = transition.charAt(1);
        char anisotropicChar = transition.charAt(2);

        if (isotropicChar == 'x') isotropicChar = '!';

        ArrayList<Integer> neighbours =
                (ArrayList<Integer>) isotropicTransitionLookup.get(number).get(isotropicChar).clone();
        neighbours.addAll(anisotropicTransitionLookup.get(anisotropicChar));

        transitionTable.removeAll(getSymmetries(neighbours));
        sortedTransitionTable.remove(reverseTransitionLookup.get(neighbours));
    }

    /**
     * Applies the symmetries to the provided isotropic transitions
     * @param transition The transition on which the symmetries will be applied
     * @return Returns the applied symmetries
     */
    protected abstract ArrayList<ArrayList<Integer>> getIsotropicSymmetries(ArrayList<Integer> transition);

    /**
     * Computes the number of ways to choose r items out of a total of n items
     * @param n The total number of items
     * @param r The number of items to pick out
     * @return Returns the number of ways to choose r items out of a total of n items
     */
    private int nCr(int n, int r) {
        return fact(n) / (fact(r) *  fact(n - r));
    }

    /**
     * Computes the factorial of the provided number
     * @param n The number to calculate the factorial of
     * @return Returns n!
     */
    private int fact(int n) {
        int res = 1;
        for (int i = 2; i <= n; i++)
            res = res * i;
        return res;
    }
}
