package lab2;

import java.util.*;

import static lab2.Constants.*;

@SuppressWarnings("DuplicatedCode")
class Enka {

    private final EnkaState initialState;

    private final Map<String, List<List<String>>> productions;
    private final Set<String> nonterminalSymbols;

    private final Set<String> emptyNonterminalSymbols = new HashSet<>();
    private final Map<String, Set<String>> beginsWithTerminal = new HashMap<>();

    Enka(String initialState, Map<String, List<List<String>>> productions, Set<String> nonterminalSymbols) {
        this.productions = productions;
        this.nonterminalSymbols = nonterminalSymbols;
        List<String> initialProduction = new LinkedList<>(productions.get(initialState).get(0));
        initialProduction.add(0, MARK);
        this.initialState = new EnkaState(initialState, initialProduction,
                new HashSet<>(Collections.singletonList(END)));
    }

    void print() {
        Set<EnkaState> visited = new HashSet<>(Collections.singletonList(initialState));
        Deque<EnkaState> stack = new ArrayDeque<>(visited);
        while (!stack.isEmpty()) {
            EnkaState state = stack.pop();
            System.out.println("State: " + state);
            System.out.println("Epsilon productions:");
            for (EnkaState s : state.epsilonProductions) {
                System.out.println(s);
                if (visited.add(s)) {
                    stack.push(s);
                }
            }
            System.out.println("Link productions:");
            for (Map.Entry<String, Set<EnkaState>> entry : state.symbolProductions.entrySet()) {
                System.out.println("Link symbol: " + entry.getKey());
                for (EnkaState s : entry.getValue()) {
                    System.out.println(s);
                    if (visited.add(s)) {
                        stack.push(s);
                    }
                }
            }
            System.out.println();
        }
    }

    Enka build() {
        findEmptyNonterminalSymbols();
        calculateBeginsWithTerminal();
        Set<EnkaState> visitedStates = new HashSet<>();
        visitedStates.add(initialState);
        Deque<EnkaState> stack = new ArrayDeque<>(visitedStates);
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
        Set<String> visitedNonterminalSymbols = new HashSet<>();
        for (String nonterminalSymbol : nonterminalSymbols) {
            calculateBeginsWithTerminal(nonterminalSymbol, visitedNonterminalSymbols);
        }
    }

    private void calculateBeginsWithTerminal(String nonterminalSymbol, Set<String> visitedNonterminalSymbols) {
        if (!visitedNonterminalSymbols.add(nonterminalSymbol)) return;
        beginsWithTerminal.putIfAbsent(nonterminalSymbol, new HashSet<>());
        for (List<String> production : productions.get(nonterminalSymbol)) {
            for (String element : production) {
                if (nonterminalSymbols.contains(element)) {
                    calculateBeginsWithTerminal(element, visitedNonterminalSymbols);
                    beginsWithTerminal.get(nonterminalSymbol).addAll(beginsWithTerminal.get(element));
                    if (!emptyNonterminalSymbols.contains(element)) break;
                } else {
                    beginsWithTerminal.get(nonterminalSymbol).add(element);
                    break;
                }
            }
        }
    }

    private void findNewStates(EnkaState currState, Set<EnkaState> visited, Deque<EnkaState> stack) {
        int markIndex = currState.rightSide.indexOf(MARK);
        if (markIndex >= currState.rightSide.size() - 1) return;
        List<String> newRightSide = new LinkedList<>(currState.rightSide);
        String symbolLink = newRightSide.get(markIndex + 1);
        Collections.swap(newRightSide, markIndex, markIndex + 1);
        EnkaState newState = new EnkaState(currState.nonterminalSymbol, newRightSide, currState.terminalSymbolsAfter);
        if (visited.add(newState)) {
            stack.push(newState);
        } else {
            // TODO: optimize this
            for (EnkaState state : visited) {
                if (newState.equals(state)) {
                    newState = state;
                    break;
                }
            }
        }
        symbolLinkStates(currState, newState, symbolLink);
        if (nonterminalSymbols.contains(symbolLink)) {
            findEpsilonProductions(currState, symbolLink, markIndex, visited, stack);
        }
    }

    private void findEpsilonProductions(EnkaState currState, String link, int markIndex, Set<EnkaState> visitedStates, Deque<EnkaState> stack) {
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
            EnkaState newState = new EnkaState(link, newRightSide, terminalSymbolsAfter);
            if (visitedStates.add(newState)) {
                stack.push(newState);
            } else {
                // TODO: optimize this
                for (EnkaState state : visitedStates) {
                    if (newState.equals(state)) {
                        newState = state;
                        break;
                    }
                }
            }
            epsilonLinkStates(currState, newState);
        }
    }

    private void epsilonLinkStates(EnkaState left, EnkaState right) {
        left.epsilonProductions.add(right);
    }

    private void symbolLinkStates(EnkaState left, EnkaState right, String link) {
        left.symbolProductions.putIfAbsent(link, new HashSet<>());
        left.symbolProductions.get(link).add(right);
    }

    private static class EnkaState {

        final String nonterminalSymbol;
        final List<String> rightSide;
        final Set<String> terminalSymbolsAfter;
        final Set<EnkaState> epsilonProductions = new HashSet<>();
        final Map<String, Set<EnkaState>> symbolProductions = new HashMap<>();

        EnkaState(String nonterminal, List<String> rightSide, Set<String> charSet) {
            this.nonterminalSymbol = nonterminal;
            this.rightSide = rightSide;
            this.terminalSymbolsAfter = charSet;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EnkaState state = (EnkaState) o;
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
            return nonterminalSymbol + "->" + Arrays.toString(rightSide.toArray()) + "{" + Arrays.toString(terminalSymbolsAfter.toArray()) + "}";
        }

    }

}
