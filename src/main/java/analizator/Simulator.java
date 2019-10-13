package analizator;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Simulator {

    private String startingState;
    private Map<String, List<Rule>> stateRules;
    private char[] input;
    private Consumer<String> outputConsumer;
    private Consumer<String> errorConsumer;

    private String currentState;


    public Simulator(String startingState, Map<String, List<Rule>> stateRules, String input,
                     Consumer<String> outputConsumer, Consumer<String> errorConsumer) {
        this.startingState = startingState;
        this.stateRules = stateRules;
        this.input = input.toCharArray();
        this.outputConsumer = outputConsumer;
        this.errorConsumer = errorConsumer;
    }

    public void simulate() {
        currentState = startingState;
        int firstPos = 0;
        int line = 1;

        while (firstPos < input.length) {
            List<Rule> rules = stateRules.get(currentState);
            rules.forEach(r -> r.getEnka().reset());
            int currPos = firstPos;
            int lastPos = -1;
            Rule lastMatched = null;

            while (currPos < input.length) {
                char c = input[currPos];
                currPos++;
                performTransitions(rules, c);

                Rule currentMatched = getFirstAccepted(rules);
                if (currentMatched != null) {
                    lastMatched = currentMatched;
                    lastPos = currPos;
                } else if (isAllDenied(rules)) {
                    break;
                }
            }

            if (lastMatched == null) {
                errorConsumer.accept(String.format("Error pos: %d, line: %d, char: %c", firstPos, line, input[firstPos]));
                firstPos++;
            } else {
                if (lastMatched.getTakeOnlyNChars() != null) {
                    lastPos = firstPos + lastMatched.getTakeOnlyNChars() + 1;
                }

                if (lastMatched.getTokenName() != null) {
                    String str = new String(input, firstPos, lastPos - firstPos);
                    outputConsumer.accept(String.format("%s %d %s", lastMatched.getTokenName(), line, str));
                } else {
                    // Do nothing
                    //debug(String.format("DEBUG: POS: %d STATE: %s, str: %s", firstPos, currentState, new String(input, firstPos, lastPos - firstPos)));
                }

                if (lastMatched.getNextLexerState() != null) {
                    currentState = lastMatched.getNextLexerState();
                }
                if (lastMatched.isGoToNewLine()) {
                    line++;
                }
                firstPos = lastPos;
            }

        }
    }

    private void debug(String format) {
        System.err.println(format);
    }

    private Rule getFirstAccepted(List<Rule> rules) {
        for (Rule rule : rules) {
            if (rule.getEnka().getLastStatus() == EnkaStatus.ACCEPTED) return rule;
        }
        return null;
    }

    private boolean isAllDenied(List<Rule> rules) {
        return rules.stream().noneMatch(rule -> rule.getEnka().getLastStatus() != EnkaStatus.DENIED);
    }

    private void performTransitions(List<Rule> rules, char c) {
        rules.forEach(rule -> rule.getEnka().performTransition(c));
    }

}
