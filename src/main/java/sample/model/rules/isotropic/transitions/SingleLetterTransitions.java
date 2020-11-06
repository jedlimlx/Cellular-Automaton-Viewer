package sample.model.rules.isotropic.transitions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles INT transitions that consist of single letters (e.g. 2n3-q4c8, 2an4w)
 */
public abstract class SingleLetterTransitions extends INTTransitions {
    protected HashMap<Integer, HashMap<Character, ArrayList<Integer>>> transitionLookup = null;

    /**
     * Constructs INT transitions that consist of single letters
     * @param string The string representation of the INT transitions
     */
    public SingleLetterTransitions(String string) {
        super(string);
    }

    /**
     * Reads the transitions from a file with the provided filename and loads them into a transition lookup
     * @param stream The input stream of the file
     */
    public void readTransitionsFromFile(InputStream stream) {
        if (transitionLookup == null) {
            Scanner scanner = new Scanner(stream);
            transitionLookup = new HashMap<>();

            String line;
            HashMap<Character, ArrayList<Integer>> hashMap = null;

            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.matches("\\d\\s*")) {
                    hashMap = new HashMap<>();
                    transitionLookup.put(Integer.parseInt(line), hashMap);
                }
                else if (!line.matches("\\s*")) {
                    String[] tokens = line.split("\\s+");

                    ArrayList<Integer> transition = new ArrayList<>();
                    for (int i = 1; i < tokens.length; i++) {
                        transition.add(Integer.parseInt(tokens[i]));
                    }

                    assert hashMap != null;
                    hashMap.put(tokens[0].charAt(0), transition);
                }
            }

            scanner.close();
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

        // Looping through all the transitions
        while (matcher.find()) {
            String transition = matcher.group();

            int number = Integer.parseInt(transition.charAt(0) + "");
            if (transition.contains("-")) {  // Checking for stuff like 2-a
                for (char letter: transitionLookup.get(number).keySet()) {
                    if (!transition.contains(letter + "")) {  // Add all but the ones that are specified
                        addTransition(transitionLookup.get(number).get(letter));
                        sortedTransitionTable.add(number + "" + letter);
                    }
                }
            }
            else {
                if (transition.length() == 1) {  // Check for OT transition
                    for (char letter: transitionLookup.get(number).keySet()) {
                        addTransition(transitionLookup.get(number).get(letter));
                        sortedTransitionTable.add(number + "" + letter);
                    }
                }
                else {
                    for (int i = 1; i < transition.length(); i++) {
                        addTransition(transitionLookup.get(number).get(transition.charAt(i)));
                        sortedTransitionTable.add(number + "" + transition.charAt(i));
                    }
                }
            }
        }

        Collections.sort(sortedTransitionTable);
    }

    /**
     * Canonises the transitions based on the currently loaded parameters
     */
    @Override
    public String canoniseTransitions() {
        StringBuilder canonTransitions = new StringBuilder();
        StringBuilder letters = new StringBuilder();

        int currentNumber, prevNumber = -1;
        for (String transition: sortedTransitionTable) {
            currentNumber = Integer.parseInt(transition.charAt(0) + "");
            if (prevNumber == currentNumber) {
                letters.append(transition.charAt(1));
            }
            else {
                // Accounting for OT transitions
                if (prevNumber != -1 && letters.length() < transitionLookup.get(prevNumber).size()) {
                    // Accounting for negate
                    if (letters.length() > transitionLookup.get(prevNumber).size() / 2) {
                        canonTransitions.append("-");
                        for (char letter: transitionLookup.get(prevNumber).keySet()) {
                            if (!letters.toString().contains(letter + "")) canonTransitions.append(letter);
                        }
                    }
                    else {  // Adding the letters
                        canonTransitions.append(letters);
                    }
                }

                canonTransitions.append(transition.charAt(0));

                letters = new StringBuilder();  // Clear the letters
                letters.append(transition.charAt(1));

                prevNumber = currentNumber;
            }
        }

        // Accounting for OT transitions
        if (prevNumber != -1 && letters.length() != transitionLookup.get(prevNumber).size()) {
            // Accounting for negate
            if (letters.length() > transitionLookup.get(prevNumber).size() / 2) {
                canonTransitions.append("-");
                for (char letter: transitionLookup.get(prevNumber).keySet()) {
                    if (!letters.toString().contains(letter + "")) canonTransitions.append(letter);
                }
            }
            else {  // Adding the letters
                canonTransitions.append(letters);
            }
        }

        return canonTransitions.toString();
    }

    /**
     * Gets the regex to identify a single transition block (number + characters)
     * @return Returns the regex that identifies that transition block
     */
    @Override
    public String getRegex() {
        int counter = 0;
        StringBuilder regex = new StringBuilder("(");
        for (int number: transitionLookup.keySet()) {
            if (transitionLookup.get(number).containsKey('!')) {  // Checking for OT transition
                regex.append(number);

                if (counter < transitionLookup.size() - 1) regex.append("|");  // Pipe for OR
                else regex.append(")");  // Closing off the regex
                counter++;

                continue;
            }

            regex.append(number).append("-[");  // [number]-[possibleChars] (e.g. 2-ak)
            for (char character: transitionLookup.get(number).keySet()) {
                regex.append(character);
            }

            regex.append("]+|").append(number).append("[");  // [number][possibleChars] (e.g. 2an)
            for (char character: transitionLookup.get(number).keySet()) {
                if (character == '!') break;
                regex.append(character);
            }

            if (counter < transitionLookup.size() - 1) regex.append("]*|");  // Pipe for OR
            else regex.append("]*)");  // Closing off the regex
            counter++;
        }

        return regex.toString();
    }
}
