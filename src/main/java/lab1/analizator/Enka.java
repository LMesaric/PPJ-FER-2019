package lab1.analizator;

import java.util.*;

public class Enka {

    private Map<Integer, State> states = new HashMap<>();
    private State startState = null;
    private State acceptableState = null;

    private Set<State> currentStates = new HashSet<>();

    private EnkaStatus lastStatus = EnkaStatus.IN_PROGRESS;

    public void reset() {
        lastStatus = EnkaStatus.IN_PROGRESS;
        currentStates.clear();
        currentStates.add(startState);
        doEpsilonTransitions();
        setLastStatus();
    }

    private void setLastStatus() {
        if (currentStates.isEmpty()) {
            lastStatus = EnkaStatus.DENIED;
        } else if (currentStates.contains(acceptableState)) {
            lastStatus = EnkaStatus.ACCEPTED;
        } else {
            lastStatus = EnkaStatus.IN_PROGRESS;
        }
    }

    public EnkaStatus performTransition(char c) {
        doLinkTransitions(c);
        doEpsilonTransitions();
        setLastStatus();
        return lastStatus;
    }

    public EnkaStatus getLastStatus() {
        return lastStatus;
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

    public void buildFromTable(String table) {
        states.clear();
        String[] rows = table.split("\n");
        for (String row : rows) {
            String[] columns = row.split(" ");
            int index = 0;
            char link = Constants.EPSILON;
            if (columns.length == 3) {
                 link = (char) Integer.parseInt(columns[index++]);
            }
            int stateIdLeft = Integer.parseInt(columns[index++]);
            State left = putIfAbsentAndReturn(stateIdLeft);
            int stateIdRight = Integer.parseInt(columns[index]);
            State right = putIfAbsentAndReturn(stateIdRight);
            if (columns.length == 2) {
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
