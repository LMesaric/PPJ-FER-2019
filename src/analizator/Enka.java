package analizator;

import java.util.*;

public class Enka {

    private State startState = null;
    private State acceptableState = null;
    private StringBuilder table = new StringBuilder();

    private static final char EPSILON = '$';

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

    }

}
