package analizator;

import java.util.*;

public class Enka {

    private static final char EPSILON = '$';

    private Map<Integer, State> states = new HashMap<>();
    private State startState = null;
    private State acceptableState = null;

    private Set<State> currentStates = new HashSet<>();

    private StringBuilder table = new StringBuilder();

    public void reset() {
        currentStates.clear();
        currentStates.add(startState);
        doEpsilonTransitions();
    }

    public EnkaStatus performTransition(char c) {
        doLinkTransitions(c);
        doEpsilonTransitions();
        if (currentStates.isEmpty()) {
            return EnkaStatus.DENIED;
        } else if (currentStates.contains(acceptableState)) {
            return EnkaStatus.ACCEPTED;
        } else {
            return EnkaStatus.IN_PROGRESS;
        }
    }

    private void doEpsilonTransitions() {
        int size = 0;
        while (size != currentStates.size()) {
            size = currentStates.size();
            Set<State> currentStatesCopy = new HashSet<>(currentStates);
            for (State state: currentStatesCopy) {
                currentStates.addAll(state.epsilonTrans);
            }
        }
    }

    private void doLinkTransitions(char c) {
        Set<State> newCurrentStates = new HashSet<>();
        for (State state: currentStates) {
            Set<State> states = state.charTrans.get(c);
            if (states != null) {
                newCurrentStates.addAll(states);
            }
        }
        currentStates = newCurrentStates;
    }

    public void buildFromTable(String table) {
        states.clear();
        String[] rows = table.split("\n");
        for (String row: rows) {
            String[] columns = row.split(" ");
            char link = (char) Integer.parseInt(columns[0]);
            int stateIdLeft = Integer.parseInt(columns[1]);
            State left = putIfAbsentAndReturn(stateIdLeft);
            int stateIdRight = Integer.parseInt(columns[2]);
            State right = putIfAbsentAndReturn(stateIdRight);

            if (link == '$') {
                epsilonLinkState(left, right);
            } else {
                charLinkState(left, right, link);
            }
        }
        startState = states.get(0);
        acceptableState = states.get(1);
    }

    private State putIfAbsentAndReturn(int stateId) {
        states.putIfAbsent(stateId, new State(stateId));
        return states.get(stateId);
    }

    private void epsilonLinkState(State left, State right) {
        left.epsilonTrans.add(right);
    }

    private void charLinkState(State left, State right, char link) {
        if (!left.charTrans.containsKey(link)) {
            left.charTrans.put(link, new HashSet<>());
        }
        left.charTrans.get(link).add(right);
    }

    public String buildTable(String expression) {
        table.setLength(0);
        build(expression);
        return table.toString();
    }

    private StatePair build(String expression) {
        List<String> Choices = getChoices(expression);
        State left = new State();
        State right = new State();
        if (startState == null) {
            startState = left;
            acceptableState = right;
        }
        if (Choices.size() > 1) {
            for (String choice: Choices) {
                StatePair tmp = build(choice);
                epsilonLink(left, tmp.left);
                epsilonLink(tmp.right, right);
            }
        } else {
            linkTogether(left, right, expression);
        }

        return new StatePair(left, right);
    }

    private List<String> getChoices(String expression) {
        List<String> Choices = new LinkedList<>();
        StringBuilder Choice = new StringBuilder();
        int parenthesis = 0;
        for (int index = 0; index < expression.length(); index++) {
            char c = expression.charAt(index);
            if (c == '\\') {
                Choice.append(c);
                index++;
                c = expression.charAt(index);
            } else if (c == '(') {
                parenthesis++;
            } else if (c == ')') {
                parenthesis--;
            } else if (c == '|' && parenthesis == 0) {
                Choices.add(Choice.toString());
                Choice.setLength(0);
                continue;
            }
            Choice.append(c);
        }
        Choices.add(Choice.toString());
        return Choices;
    }

    private void epsilonLink(State left, State right) {
        table.append(String.format("%d %d %d\n", (int) EPSILON, left.id, right.id));
    }

    private void charLink(State left, State right, char link) {
        table.append(String.format("%d %d %d\n", (int) link, left.id, right.id));
    }

    private void linkTogether(State left, State right, String expression) {
        State last = left;
        for (int index = 0; index < expression.length(); index++) {
            char c = expression.charAt(index);
            State a, b;

            if (c == '\\') {
                if (index >= expression.length() - 1) break;
                index++;
                char next = expression.charAt(index);
                char link;
                if (next == 't') {
                    link = '\t';
                } else if (next == 'n') {
                    link = '\n';
                } else if (next == '_') {
                    link = ' ';
                } else {
                    link = next;
                }
                a = new State();
                b = new State();
                charLink(a, b, link);

            } else if (c == '(') {
                int closingBracket = indexOfClosingBracket(expression, index + 1);
                if (closingBracket == -1) {
                    throw new IllegalStateException("Closing bracket could not be found! Index = " + index);
                }
                StatePair tmp = build(expression.substring(index + 1, closingBracket));
                a = tmp.left;
                b = tmp.right;
                index = closingBracket;
            } else {
                a = new State();
                b = new State();
                if (c == '$') {
                    epsilonLink(a, b);
                } else {
                    charLink(a, b, c);
                }
            }
            epsilonLink(last, a);
            last = b;
        }
        epsilonLink(last, right);
    }

    private int indexOfClosingBracket(String expression, int index) {
        int closingBracket = index;
        int brackets = 0;
        while (expression.charAt(closingBracket) != ')' || brackets != 0) {
            if (expression.charAt(closingBracket) == '(') brackets++;
            if (expression.charAt(closingBracket) == ')') brackets--;
            if (++closingBracket >= expression.length()) return -1;
        }
        return closingBracket;
    }


    private static class StatePair {

        State left;
        State right;

        private StatePair(State left, State right) {
            this.left = left;
            this.right = right;
        }

    }

    private static class State {

        static int stateId = 0;

        int id;
        Set<State> epsilonTrans = new HashSet<>();
        Map<Character, Set<State>> charTrans = new HashMap<>();

        State() {
            id = stateId++;
        }

        State(int id) {
            this.id = id;
        }

    }

}
