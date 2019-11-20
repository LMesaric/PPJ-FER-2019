package lab2;

import java.util.*;

import static lab2.Constants.*;

@SuppressWarnings("DuplicatedCode")
class Enka {

    private State initialState;

    private Set<String> empty;
    private Map<String, Set<String>> beginsWithTerminal;

    void print() {
        Set<State> visited = new HashSet<>(Collections.singletonList(initialState));
        Deque<State> stack = new ArrayDeque<>(visited);
        while (!stack.isEmpty()) {
            State state = stack.pop();
            System.out.println("State: " + state);
            System.out.println("Epsilon:");
            for (State s : state.epsilonTrans) {
                System.out.println(s);
                if (visited.add(s)) {
                    stack.push(s);
                }
            }
            System.out.println("Link:");
            for (Map.Entry<String, Set<State>> entry : state.linkTrans.entrySet()) {
                System.out.println("Link word: " + entry.getKey());
                for (State s : entry.getValue()) {
                    System.out.println(s);
                    if (visited.add(s)) {
                        stack.push(s);
                    }
                }
            }
            System.out.println();
        }
    }

    void build(Map<String, List<List<String>>> transitions, Set<String> nonterminal) {
        beginsWithTerminal = new HashMap<>();
        empty = new HashSet<>();
        calculateEmptyNonterminals(transitions, nonterminal);
        calculateBeginsWithTerminal(transitions, nonterminal);
        List<String> initialProduction = new LinkedList<>(transitions.get(INITIAL_STATE).get(0));
        initialProduction.add(0, MARK);
        initialState = new State(INITIAL_STATE, initialProduction,
                new HashSet<>(Collections.singletonList(EPSILON)));
        Set<State> visited = new HashSet<>();
        visited.add(initialState);
        Deque<State> stack = new ArrayDeque<>(visited);
        while (!stack.isEmpty()) {
            linkItems(transitions, stack.pop(), visited, stack, nonterminal);
        }
    }

    private void calculateEmptyNonterminals(Map<String, List<List<String>>> transitions, Set<String> nonterminal) {
        Set<String> empty = new HashSet<>();
        int size = -1;
        while (size != empty.size()) {
            size = empty.size();
            for (String ch : nonterminal) {
                if (empty.contains(ch)) continue;
                List<List<String>> productions = transitions.get(ch);
                for (List<String> production : productions) {
                    if (production.get(0).equals(EPSILON)) {
                        empty.add(ch);
                        break;
                    }
                    boolean isEmpty = true;
                    for (String element : production) {
                        if (!empty.contains(element)) {
                            isEmpty = false;
                            break;
                        }
                    }
                    if (isEmpty) {
                        empty.add(ch);
                        break;
                    }
                }
            }
        }
    }

    private void linkItems(Map<String, List<List<String>>> transitions, State currState, Set<State> visited,
                           Deque<State> stack, Set<String> nonterminal) {
        int markIndex = currState.rightSide.indexOf(MARK);
        if (markIndex >= currState.rightSide.size() - 1) return;
        List<String> newRightSide = new LinkedList<>(currState.rightSide);
        String link = newRightSide.get(markIndex + 1);
        Collections.swap(newRightSide, markIndex, markIndex + 1);
        State newState = new State(currState.nonterminal, newRightSide, currState.charSet);
        if (visited.add(newState)) {
            stack.push(newState);
        } else {
            for (State state : visited) {
                if (newState.equals(state)) {
                    newState = state;
                    break;
                }
            }
        }
        linkState(currState, newState, link);
        if (!nonterminal.contains(link)) return;
        epsilonLinkItems(transitions, currState, link, markIndex, visited, stack);
    }

    private void epsilonLinkItems(Map<String, List<List<String>>> transitions, State currState, String link,
                                  int markIndex, Set<State> visited, Deque<State> stack) {
        Set<String> charSet = markIndex + 2 < currState.rightSide.size() ? beginsWithTerminal.get(currState.rightSide.get(markIndex + 2)) :
                new HashSet<>(Collections.singletonList(EPSILON));
        boolean isEmpty = true;
        for (int i = markIndex + 2; i < currState.rightSide.size(); i++) {
            if (empty.contains(currState.rightSide.get(i))) {
                isEmpty = false;
                break;
            }
        }
        if (isEmpty) {
            charSet.addAll(currState.charSet);
        }
        for (List<String> right : transitions.get(link)) {
            List<String> newRightSide = new LinkedList<>(right);
            if (newRightSide.get(0).equals(EPSILON)) {
                newRightSide = new LinkedList<>(Collections.singletonList("*"));
            } else {
                newRightSide.add(0, MARK);
            }
            State newState = new State(link, newRightSide, charSet);
            if (visited.add(newState)) {
                stack.push(newState);
            } else {
                for (State state : visited) {
                    if (newState.equals(state)) {
                        newState = state;
                        break;
                    }
                }
            }
            epsilonLinkState(currState, newState);
        }
    }

    private void calculateBeginsWithTerminal(Map<String, List<List<String>>> transitions, Set<String> nonterminal) {
        Set<String> visited = new HashSet<>();
        for (String element : nonterminal) {
            calculateBeginsWithTerminal(element, beginsWithTerminal, visited, transitions, empty, nonterminal);
        }
    }

    private void calculateBeginsWithTerminal(String element, Map<String, Set<String>> beginsWithTerminal, Set<String> visited,
                                             Map<String, List<List<String>>> transitions, Set<String> empty, Set<String> nonterminal) {
        if (!visited.add(element)) return;
        beginsWithTerminal.putIfAbsent(element, new HashSet<>());
        for (List<String> production : transitions.get(element)) {
            for (String el : production) {
                if (nonterminal.contains(el)) {
                    calculateBeginsWithTerminal(el, beginsWithTerminal, visited, transitions, empty, nonterminal);
                    beginsWithTerminal.get(element).addAll(beginsWithTerminal.get(el));
                    if (!empty.contains(el)) break;
                } else {
                    beginsWithTerminal.get(element).add(el);
                    break;
                }
            }
        }
    }

    private void epsilonLinkState(State left, State right) {
        left.epsilonTrans.add(right);
    }

    private void linkState(State left, State right, String link) {
        if (!left.linkTrans.containsKey(link)) {
            left.linkTrans.put(link, new HashSet<>());
        }
        left.linkTrans.get(link).add(right);
    }

    private static class State {

        final String nonterminal;
        final List<String> rightSide;
        final Set<String> charSet;
        Set<State> epsilonTrans = new HashSet<>();
        Map<String, Set<State>> linkTrans = new HashMap<>();

        State(String nonterminal, List<String> rightSide, Set<String> charSet) {
            this.nonterminal = nonterminal;
            this.rightSide = rightSide;
            this.charSet = charSet;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return nonterminal.equals(state.nonterminal) &&
                    rightSide.equals(state.rightSide) &&
                    charSet.equals(state.charSet);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nonterminal, rightSide, charSet);
        }

        @Override
        public String toString() {
            return nonterminal + "->" + Arrays.toString(rightSide.toArray()) + "{" + Arrays.toString(charSet.toArray()) + "}";
        }
    }

}
