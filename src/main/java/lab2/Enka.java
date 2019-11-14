package lab2;

import java.util.*;

import static lab2.Constants.*;

public class Enka {

    private Map<Integer, State> states = new HashMap<>();
    private State initialState;
    private Set<State> currentStates = new HashSet<>();

    public void performTransition(char c) {
        doLinkTransitions(c);
        doEpsilonTransitions();
    }

    private void doEpsilonTransitions() {
        Deque<State> stack = new ArrayDeque<>(currentStates);
        while (!stack.isEmpty()) {
            State curr = stack.pop();
            for (State state : curr.epsilonTrans) {
                if (currentStates.add(state)) {
                    stack.push(state);
                }
            }
        }
    }

    private void doLinkTransitions(char c) {
        Set<State> newCurrentStates = new HashSet<>();
        for (State state : currentStates) {
            Set<State> newStates = state.linkTrans.get(c);
            if (newStates != null) {
                newCurrentStates.addAll(newStates);
            }
        }
        currentStates = newCurrentStates;
    }

    private static String swap(String str, int i, int j) {
        char[] ch = str.toCharArray();
        char temp = ch[i];
        ch[i] = ch[j];
        ch[j] = temp;
        return new String(ch);
    }

    private void build(Map<String, List<List<String>>> transitions) {
        transitions.get(INITIAL_STATE).get(0).add(0, MARK);
        initialState = new State(INITIAL_STATE, transitions.get(INITIAL_STATE).get(0),
                new HashSet<>(Collections.singletonList(EPSILON)));
        Set<State> visited = new HashSet<>();
        visited.add(initialState);
        Deque<State> stack = new ArrayDeque<>(visited);
        while (!stack.isEmpty()) {
            State curr = stack.pop();
            int markIndex = curr.rightSide.indexOf(MARK);
            List<String> newList = new LinkedList<>(curr.rightSide);
            if (markIndex < newList.size() - 1) {
                Collections.swap(newList, markIndex, markIndex + 1);
                State newState = new State(curr.nonterminal, newList, curr.charSet);
                String link = newList.get(markIndex);
                linkState(curr, newState, link);
                if (visited.add(newState)) {
                    stack.push(newState);
                }
                for (List<String> right : transitions.get(link)) {
                    Set<String> charSet = markIndex + 2 < newList.size() ? begins(transitions, newList.get(markIndex + 2)) :
                            new HashSet<>(Collections.singletonList(EPSILON));
                    newList = new LinkedList<>(right);
                    newList.add(0, MARK);
                    newState = new State(link, newList, charSet);
                    if (visited.add(newState)) {
                        stack.push(newState);
                    }
                }
            }
        }
    }

    private Set<String> begins(Map<String, List<List<String>>> transitions, String word) {
        // TODO: Implement this
        return new HashSet<>();
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

    }

}
