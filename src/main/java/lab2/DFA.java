package lab2;

import java.util.*;

//TODO: Fix ordering

@SuppressWarnings("DuplicatedCode")
class DFA {

    private final State initialState;

    private final Set<State> states = new TreeSet<>();

    DFA(ENFA enfa) {
        initialState = new State(enfa.reset());
        states.add(initialState);
        buildFromENFA(enfa);
    }

    String print() {
        StringBuilder sb = new StringBuilder();
        for (State state : states) {
            sb.append("trenutno stanje: (").append(state.id).append(")\n");
            sb.append(state);
            sb.append("\nprijelazi prema:\n");
            for (Map.Entry<String, State> entry : state.symbolTransitions.entrySet()) {
                sb.append("\t").append(entry.getKey()).append(": (").append(entry.getValue().id).append(")\n");
                sb.append(entry.getValue().toString("\t")).append("\n");
            }
        }
        return sb.toString();
    }

    private void buildFromENFA(ENFA enfa) {
        Queue<State> queue = new LinkedList<>();
        queue.add(initialState);
        while (!queue.isEmpty()) {
            State currentState = queue.remove();
            for (String symbol : enfa.getSymbols()) {
                State newState;
                List<ENFA.State> items = new LinkedList<>(enfa.performTransitionFrom(currentState.states, symbol));
                if (items.isEmpty()) continue;
                Optional<State> optionalState = states.stream().filter(s -> s.states.equals(items)).findAny();
                if (optionalState.isPresent()) {
                    newState = optionalState.get();
                } else {
                    newState = new State(items);
                    states.add(newState);
                    queue.add(newState);
                }
                symbolLinkStates(currentState, newState, symbol);
            }
        }
    }

    private void symbolLinkStates(State left, State right, String link) {
        left.symbolTransitions.put(link, right);
    }

    static class State implements Comparable<State> {

        static int nextId = 0;

        private final int id;

        private final List<ENFA.State> states = new LinkedList<>();

        private final Map<String, State> symbolTransitions = new LinkedHashMap<>();

        private State(Collection<ENFA.State> states) {
            id = nextId++;
            this.states.addAll(states);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return id == state.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        String toString(String prefix) {
            StringBuilder sb = new StringBuilder();
            states.forEach(s -> sb.append(prefix).append(s.toString()).append("\n"));
            return sb.toString();
        }

        @Override
        public String toString() {
            return toString("");
        }

        @Override
        public int compareTo(State o) {
            return Integer.compare(id, o.id);
        }
    }

}
