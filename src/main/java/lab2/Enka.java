package lab2;

import java.util.*;

public class Enka {

    private Map<Integer, State> states = new HashMap<>();
    private State initialState = null;
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
            Set<State> newStates = state.charTrans.get(c);
            if (newStates != null) {
                newCurrentStates.addAll(newStates);
            }
        }
        currentStates = newCurrentStates;
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

    private static class State {

        String leftSide;
        String rightSide;
        Set<String> charSet = new HashSet<>();
        Set<State> epsilonTrans = new HashSet<>();
        Map<Character, Set<State>> charTrans = new HashMap<>();

        public State(String leftSide, String rightSide, Set<String> charSet) {
            this.leftSide = leftSide;
            this.rightSide = rightSide;
            this.charSet = charSet;
        }

    }

}
