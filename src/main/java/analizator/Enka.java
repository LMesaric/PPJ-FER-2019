package analizator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Enka {

    public static final char EPSILON = '$';

    private Map<Integer, State> states = new HashMap<>();
    private State startState = null;
    private State acceptableState = null;

    private Set<State> currentStates = new HashSet<>();

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
            for (State state : currentStatesCopy) {
                currentStates.addAll(state.epsilonTrans);
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

    public void buildFromTable(String table) {
        states.clear();
        String[] rows = table.split("\n");
        for (String row : rows) {
            String[] columns = row.split(" ");
            char link = (char) Integer.parseInt(columns[0]);
            int stateIdLeft = Integer.parseInt(columns[1]);
            State left = putIfAbsentAndReturn(stateIdLeft);
            int stateIdRight = Integer.parseInt(columns[2]);
            State right = putIfAbsentAndReturn(stateIdRight);
            if (link == EPSILON) {
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

    private static class State {

        int id;
        Set<State> epsilonTrans = new HashSet<>();
        Map<Character, Set<State>> charTrans = new HashMap<>();

        State(int id) {
            this.id = id;
        }

    }

}
