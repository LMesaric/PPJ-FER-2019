package lab2;

import java.util.*;

import static lab2.Constants.*;

@SuppressWarnings({"DuplicatedCode"})
class ENFA {

    private final State initialState;

    private final Set<String> symbols;

    private final Map<String, List<List<String>>> productions;
    private final Set<String> nonterminalSymbols;

    private final Set<String> emptyNonterminalSymbols = new HashSet<>();
    private final Map<String, Set<String>> beginsWithTerminal = new HashMap<>();

    private final Map<String, Set<String>> graph = new HashMap<>();

    private Set<State> currentStates = new LinkedHashSet<>();

    ENFA(String initialState, Map<String, List<List<String>>> productions, Set<String> symbols, Set<String> nonterminalSymbols) {
        this.productions = productions;
        this.symbols = symbols;
        this.nonterminalSymbols = nonterminalSymbols;
        List<String> initialProduction = new LinkedList<>(productions.get(initialState).get(0));
        initialProduction.add(0, MARK);
        this.initialState = new State(initialState, initialProduction,
                new HashSet<>(Collections.singletonList(END)));
    }

    Set<State> reset() {
        currentStates.clear();
        currentStates.add(initialState);
        doEpsilonTransitions();
        return currentStates;
    }

    Set<State> performTransitionFrom(Collection<State> states, String symbol) {
        currentStates.clear();
        currentStates.addAll(states);
        doSymbolTransitions(symbol);
        doEpsilonTransitions();
        return currentStates;
    }

    private void doEpsilonTransitions() {
        Queue<State> queue = new LinkedList<>(currentStates);
        while (!queue.isEmpty()) {
            State curr = queue.remove();
            for (State state : curr.epsilonTransitions) {
                if (currentStates.add(state)) {
                    queue.add(state);
                }
            }
        }
    }

    private void doSymbolTransitions(String symbol) {
        Set<State> newCurrentStates = new LinkedHashSet<>();
        for (State state : currentStates) {
            Set<State> newStates = state.symbolTransitions.get(symbol);
            if (newStates != null) {
                newCurrentStates.addAll(newStates);
            }
        }
        currentStates = newCurrentStates;
        doEpsilonTransitions();
    }

    String print() {
        Set<State> visited = new HashSet<>(Collections.singletonList(initialState));
        Queue<State> queue = new LinkedList<>(visited);
        StringBuilder sb = new StringBuilder();
        while (!queue.isEmpty()) {
            State state = queue.remove();
            sb.append("trenutno stanje: ").append(state).append("\n");
            sb.append("epsilon prijelazi prema:\n");
            for (State s : state.epsilonTransitions) {
                sb.append("\t").append(s).append("\n");
            }
            sb.append("prijelazi prema:\n");
            for (Map.Entry<String, Set<State>> entry : state.symbolTransitions.entrySet()) {
                sb.append("\t").append(entry.getKey()).append(":");
                for (State s : entry.getValue()) {
                    sb.append(" ").append(s);
                }
                sb.append("\n");
            }
            for (Map.Entry<String, Set<State>> entry : state.symbolTransitions.entrySet()) {
                for (State s : entry.getValue()) {
                    if (visited.add(s)) {
                        queue.add(s);
                    }
                }
            }
            for (State s : state.epsilonTransitions) {
                if (visited.add(s)) {
                    queue.add(s);
                }
            }
        }
        return sb.toString();
    }

    ENFA build() {
        findEmptyNonterminalSymbols();
        calculateBeginsWithTerminal();
        Set<State> visitedStates = new HashSet<>();
        visitedStates.add(initialState);
        Deque<State> stack = new ArrayDeque<>(visitedStates);
        while (!stack.isEmpty()) {
            findNewStates(stack.pop(), visitedStates, stack);
        }
        return this;
    }

    private void findEmptyNonterminalSymbols() {
        int size = -1;
        while (size != emptyNonterminalSymbols.size()) {
            size = emptyNonterminalSymbols.size();
            // TODO: possible optimization for iterating only over nonempty terminal symbols
            for (String nonterminalSymbol : nonterminalSymbols) {
                if (emptyNonterminalSymbols.contains(nonterminalSymbol)) continue;
                for (List<String> production : productions.get(nonterminalSymbol)) {
                    if (production.isEmpty()) {
                        emptyNonterminalSymbols.add(nonterminalSymbol);
                        break;
                    }
                    boolean isEmpty = true;
                    for (String element : production) {
                        if (!emptyNonterminalSymbols.contains(element)) {
                            isEmpty = false;
                            break;
                        }
                    }
                    if (isEmpty) {
                        emptyNonterminalSymbols.add(nonterminalSymbol);
                        break;
                    }
                }
            }
        }
    }

    private void calculateBeginsWithTerminal() {
        for (String nonterminalSymbol : nonterminalSymbols) {
            createGraph(nonterminalSymbol);
        }
        for (String nonterminalSymbol : nonterminalSymbols) {
            bfs(nonterminalSymbol);
        }
    }

    private void createGraph(String nonterminalSymbol) {
        graph.put(nonterminalSymbol, new HashSet<>());
        for (List<String> production : productions.get(nonterminalSymbol)) {
            for (String element : production) {
                graph.get(nonterminalSymbol).add(element);
                if (!emptyNonterminalSymbols.contains(element)) break;
            }
        }
    }

    private void bfs(String currNonterminalSymbol) {
        beginsWithTerminal.put(currNonterminalSymbol, new HashSet<>());
        Set<String> visitedNonterminalSymbols = new HashSet<>();
        visitedNonterminalSymbols.add(currNonterminalSymbol);
        Queue<String> queue = new LinkedList<>(visitedNonterminalSymbols);
        while (!queue.isEmpty()) {
            String nonterminalSymbol = queue.remove();
            for (String symbol : graph.get(nonterminalSymbol)) {
                if (symbol.equals(currNonterminalSymbol)) continue;
                if (nonterminalSymbols.contains(symbol)) {
                    if (beginsWithTerminal.containsKey(symbol)) {
                        beginsWithTerminal.get(currNonterminalSymbol).addAll(beginsWithTerminal.get(symbol));
                        visitedNonterminalSymbols.add(symbol);
                    }
                    if (visitedNonterminalSymbols.add(symbol)) {
                        queue.add(symbol);
                    }
                } else {
                    beginsWithTerminal.get(currNonterminalSymbol).add(symbol);
                }
            }
        }
    }

    private void findNewStates(State currState, Set<State> visitedStates, Deque<State> stack) {
        int markIndex = currState.rightSide.indexOf(MARK);
        if (markIndex >= currState.rightSide.size() - 1) return;
        List<String> newRightSide = new LinkedList<>(currState.rightSide);
        String symbolLink = newRightSide.get(markIndex + 1);
        Collections.swap(newRightSide, markIndex, markIndex + 1);
        State newState = new State(currState.nonterminalSymbol, newRightSide, currState.terminalSymbolsAfter);
        if (visitedStates.add(newState)) {
            stack.push(newState);
        } else {
            // TODO: optimize this
            for (State state : visitedStates) {
                if (newState.equals(state)) {
                    newState = state;
                    break;
                }
            }
        }
        symbolLinkStates(currState, newState, symbolLink);
        if (nonterminalSymbols.contains(symbolLink)) {
            findEpsilonProductions(currState, symbolLink, markIndex, visitedStates, stack);
        }
    }

    private void findEpsilonProductions(State currState, String link, int markIndex, Set<State> visitedStates, Deque<State> stack) {
        Set<String> terminalSymbolsAfter = new HashSet<>();
        boolean isEmpty = true;
        for (int i = markIndex + 2; i < currState.rightSide.size(); i++) {
            String symbol = currState.rightSide.get(i);
            if (nonterminalSymbols.contains(symbol)) {
                terminalSymbolsAfter.addAll(beginsWithTerminal.get(symbol));
            } else {
                terminalSymbolsAfter.add(symbol);
            }
            if (!emptyNonterminalSymbols.contains(currState.rightSide.get(i))) {
                isEmpty = false;
                break;
            }
        }
        if (isEmpty) {
            terminalSymbolsAfter.addAll(currState.terminalSymbolsAfter);
        }
        for (List<String> rightSide : productions.get(link)) {
            List<String> newRightSide = new LinkedList<>(rightSide);
            newRightSide.add(0, MARK);
            State newState = new State(link, newRightSide, terminalSymbolsAfter);
            if (visitedStates.add(newState)) {
                stack.push(newState);
            } else {
                // TODO: optimize this
                for (State state : visitedStates) {
                    if (newState.equals(state)) {
                        newState = state;
                        break;
                    }
                }
            }
            epsilonLinkStates(currState, newState);
        }
    }

    private void epsilonLinkStates(State left, State right) {
        left.epsilonTransitions.add(right);
    }

    private void symbolLinkStates(State left, State right, String link) {
        left.symbolTransitions.putIfAbsent(link, new HashSet<>());
        left.symbolTransitions.get(link).add(right);
    }

    public Set<String> getSymbols() {
        return symbols;
    }

    static class State {

        final String nonterminalSymbol;
        final List<String> rightSide;
        final Set<String> terminalSymbolsAfter;
        final Set<State> epsilonTransitions = new LinkedHashSet<>();
        final Map<String, Set<State>> symbolTransitions = new HashMap<>();

        State(String nonterminal, List<String> rightSide, Set<String> charSet) {
            this.nonterminalSymbol = nonterminal;
            this.rightSide = rightSide;
            this.terminalSymbolsAfter = charSet;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return nonterminalSymbol.equals(state.nonterminalSymbol) &&
                    rightSide.equals(state.rightSide) &&
                    terminalSymbolsAfter.equals(state.terminalSymbolsAfter);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nonterminalSymbol, rightSide, terminalSymbolsAfter);
        }

        @Override
        public String toString() {
            StringBuilder symb = new StringBuilder();
            for (String symbol : terminalSymbolsAfter) {
                symb.append(symbol).append(" ");
            }
            return nonterminalSymbol + " -> " + Arrays.toString(rightSide.toArray()).replace(",", "").replace("[", "")
                    .replace("]", "") + ", { " + symb.toString() + "}";
        }

    }

}