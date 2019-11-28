package lab2;

import java.util.*;

import static lab2.Constants.*;

public class GSA {

    private static final Map<Integer, Map<String, Object>> actionTable = new HashMap<>();

    private static final Map<Integer, Map<String, Put>> newStateTable = new HashMap<>();

    static final List<Production> productionsOrder = new LinkedList<>();

    public static void main(String[] args) {
        StringBuilder readInput = new StringBuilder();
        try (Scanner sc = new Scanner(System.in)) {
            while (sc.hasNext()) {
                readInput.append(sc.nextLine()).append("\n");
            }
        }
        generateOutput(readInput.toString().split("\\r?\\n"));
    }

    private static void generateOutput(String[] readInput) {
        Set<String> nonterminalSymbols = cleanSymbolInput(readInput[0]);
        // Add new initial state
        nonterminalSymbols.add(INITIAL_STATE);

        Set<String> terminalSymbols = cleanSymbolInput(readInput[1]);
        List<String> symbols = new LinkedList<>(nonterminalSymbols);
        symbols.addAll(terminalSymbols);

        Set<String> synchronizationalSymbols = cleanSymbolInput(readInput[2]);

        // Create initial production
        String start = readInput[0].split(" ")[1];
        List<List<String>> production = new LinkedList<>();
        production.add(Collections.singletonList(start));
        productionsOrder.add(new Production(INITIAL_STATE, Collections.singletonList(start)));

        Map<String, List<List<String>>> productions = parseProductions(readInput, 3);
        productions.put(INITIAL_STATE, production);

        DFA dfa = new DFA(new ENFA(INITIAL_STATE, productions, symbols, nonterminalSymbols, productionsOrder));
        generateTables(dfa, terminalSymbols, nonterminalSymbols);
        printTables(dfa, terminalSymbols, nonterminalSymbols);
    }

    static Set<String> cleanSymbolInput(String line) {
        String[] tmp = line.split(" ");
        Set<String> set = new LinkedHashSet<>();
        for (int i = 1, limit = tmp.length; i < limit; i++) {
            set.add(tmp[i]);
        }
        return set;
    }

    static Map<String, List<List<String>>> parseProductions(String[] input, @SuppressWarnings("SameParameterValue") int offset) {
        Map<String, List<List<String>>> map = new HashMap<>();
        int i = offset, last = input.length;
        while (i < last) {
            String nonterminal = input[i++].trim();
            List<List<String>> rightSides = new ArrayList<>();
            while (i < last && input[i].charAt(0) == ' ') {
                List<String> rightSide;
                if (input[i].trim().equals(EPSILON)) {
                    rightSide = Collections.emptyList();
                } else {
                    rightSide = Arrays.asList(input[i].trim().split(" "));
                }
                rightSides.add(rightSide);
                productionsOrder.add(new Production(nonterminal, rightSide));
                i++;
            }
            if (map.containsKey(nonterminal)) {
                map.get(nonterminal).addAll(rightSides);
            } else {
                map.put(nonterminal, rightSides);
            }
        }
        return map;
    }

    private static void generateTables(DFA dfa, Set<String> terminalSymbols, Set<String> nonterminalSymbols) {
        terminalSymbols.add(END);
        for (DFA.State state : dfa.getStates()) {
            actionTable.put(state.id, new HashMap<>());
            newStateTable.put(state.id, new HashMap<>());
            // Fill action table row
            for (String terminalSymbol : terminalSymbols) {
                // Determine whether there is transition for the current symbol
                DFA.State newState = state.symbolTransitions.get(terminalSymbol);
                if (newState != null) {
                    actionTable.get(state.id).put(terminalSymbol, new Move(newState.id));
                } else {
                    // Determine whether there is reduction for the current symbol
                    for (ENFA.State enfaState : state.reducibleStates) {
                        if (enfaState.terminalSymbolsAfter.contains(terminalSymbol)) {
                            Production production = new Production(enfaState.nonterminalSymbol,
                                    enfaState.rightSide.subList(0, enfaState.rightSide.size() - 1));
                            actionTable.get(state.id).put(terminalSymbol, new Reduce(production));
                        }
                    }
                }
            }
            if (state.acceptable) {
                actionTable.get(state.id).put(END, new Accept());
            }
            // Fill newState table row
            for (String symbol : nonterminalSymbols) {
                DFA.State newState = state.symbolTransitions.get(symbol);
                if (newState != null) {
                    newStateTable.get(state.id).put(symbol, new Put(newState.id));
                }
            }
        }
    }

    private static void printTables(DFA dfa, Set<String> terminalSymbols, Set<String> nonterminalSymbols) {
        int space = 20;
        StringBuilder sb = new StringBuilder();
        sb.append(printCentered("Stanje", space));
        for (String symbol : terminalSymbols) {
            sb.append(printCentered(symbol, space));
        }
        sb.append("||");
        for (String symbol : nonterminalSymbols) {
            sb.append(printCentered(symbol, space));
        }
        sb.append("\n");
        for (DFA.State state : dfa.getStates()) {
            sb.append(printCentered(String.valueOf(state.id), space));
            for (String symbol : terminalSymbols) {
                Object action = actionTable.get(state.id).get(symbol);
                if (action == null) {
                    sb.append(printCentered("-", space));
                } else {
                    if (action instanceof Accept) {
                        sb.append(printCentered("Prihvati", space));
                    } else if (action instanceof Move) {
                        sb.append(printCentered("Pomakni " + ((Move) action).newState, space));
                    } else if (action instanceof Reduce) {
                        sb.append(printCentered(((Reduce) action).production.toString(), space));
                    } else {
                        throw new IllegalStateException("Wrong object type!");
                    }
                }
            }
            sb.append("||");
            for (String symbol : nonterminalSymbols) {
                Put newState = newStateTable.get(state.id).get(symbol);
                if (newState == null) {
                    sb.append(printCentered("-", space));
                } else {
                    sb.append(printCentered("Stavi " + newState.newState, space));
                }
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }

    private static String printCentered(String str, int size) {
        int left = (size - str.length()) / 2;
        int right = size - left - str.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < left; i++) {
            sb.append(" ");
        }
        sb.append(str);
        for (int i = 0; i < right; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

}
