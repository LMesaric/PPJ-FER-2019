package lab2;

import java.util.*;

@SuppressWarnings("DuplicatedCode")
class DFA {

    private final State initialState;

    private final Set<State> states = new TreeSet<>();

    DFA(ENFA enfa) {
        List<ENFA.State> items = new LinkedList<>(enfa.reset());
        Set<ENFA.State> nonReducibleStates = new HashSet<>();
        Set<ENFA.State> reducibleStates = new TreeSet<>();
        for (ENFA.State item : items) {
            if (item.reducible) {
                reducibleStates.add(item);
            } else {
                nonReducibleStates.add(item);
            }
        }
        initialState = new State(nonReducibleStates, reducibleStates, false);
        states.add(initialState);
        buildFromENFA(enfa);
    }

    private void buildFromENFA(ENFA enfa) {
        Queue<State> queue = new LinkedList<>();
        queue.add(initialState);
        while (!queue.isEmpty()) {
            State currentState = queue.remove();
            if (currentState.nonReducibleStates.isEmpty()) continue;
            for (String symbol : enfa.getSymbols()) {
                List<ENFA.State> items = new LinkedList<>(enfa.performTransitionFrom(currentState.nonReducibleStates, symbol));
                if (items.isEmpty()) continue;
                Set<ENFA.State> nonReducibleStates = new HashSet<>();
                Set<ENFA.State> reducibleStates = new HashSet<>();
                boolean acceptable = false;
                for (ENFA.State item : items) {
                    acceptable |= item.acceptable;
                    if (item.reducible) {
                        reducibleStates.add(item);
                    } else {
                        nonReducibleStates.add(item);
                    }
                }
                State optionalState = null;
                for (State state : states) {
                    if (state.nonReducibleStates.equals(nonReducibleStates) && new HashSet<>(state.reducibleStates).equals(reducibleStates)) {
                        optionalState = state;
                        break;
                    }
                }
                State newState;
                if (optionalState != null) {
                    newState = optionalState;
                } else {
                    newState = new State(nonReducibleStates, reducibleStates, acceptable);
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

    Set<State> getStates() {
        return states;
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

    static class State implements Comparable<State> {

        static int nextId = 0;

        final int id;

        final Set<ENFA.State> nonReducibleStates = new HashSet<>();

        final Set<ENFA.State> reducibleStates = new HashSet<>();

        final Map<String, State> symbolTransitions = new LinkedHashMap<>();

        final boolean acceptable;

        private State(Collection<ENFA.State> nonReducibleStates, Collection<ENFA.State> reducibleStates, boolean acceptable) {
            id = nextId++;
            this.nonReducibleStates.addAll(nonReducibleStates);
            this.reducibleStates.addAll(reducibleStates);
            this.acceptable = acceptable;
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
            nonReducibleStates.forEach(s -> sb.append(prefix).append(s.toString()).append("\n"));
            reducibleStates.forEach(s -> sb.append(prefix).append(s.toString()).append("\n"));
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
