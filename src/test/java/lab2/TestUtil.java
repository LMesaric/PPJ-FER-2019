package lab2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static lab2.Constants.EPSILON;
import static lab2.Constants.INITIAL_STATE;

class TestUtil {

    static final String TESTCASES_DIR_ENFA = "src/test/resources/testcases_ENFA/";
    static final String TESTCASES_DIR_DFA = "src/test/resources/testcases_DFA/";

    static ENFA createENFA(String fileName) throws IOException {
        Path pathIn = Paths.get(TESTCASES_DIR_ENFA + fileName + ".in");
        Path pathOut = Paths.get(TESTCASES_DIR_ENFA + fileName + ".out");

        String[] readInput = new String(Files.readAllBytes(pathIn)).split("\n");
        Set<String> nonterminalSymbols = cleanSymbolInput(readInput[0]);
        Set<String> terminalSymbols = cleanSymbolInput(readInput[1]);
        List<String> symbols = new LinkedList<>(nonterminalSymbols);
        symbols.addAll(terminalSymbols);
        Set<String> synchronizationalSymbols = cleanSymbolInput(readInput[2]);
        Map<String, List<List<String>>> productions = parseProductions(readInput, 3);
        nonterminalSymbols.add(INITIAL_STATE);
        String start = readInput[0].split(" ")[1];
        List<List<String>> production = new LinkedList<>();
        production.add(Collections.singletonList(start));
        productions.put(INITIAL_STATE, production);

        return new ENFA(INITIAL_STATE, productions, symbols, nonterminalSymbols);
    }

    private static Set<String> cleanSymbolInput(String line) {
        String[] tmp = line.trim().split(" ");
        Set<String> set = new LinkedHashSet<>();
        for (int i = 1, limit = tmp.length; i < limit; i++) {
            set.add(tmp[i]);
        }
        return set;
    }

    private static Map<String, List<List<String>>> parseProductions(
            String[] input,
            @SuppressWarnings("SameParameterValue") int offset
    ) {
        Map<String, List<List<String>>> map = new HashMap<>();
        int i = offset, last = input.length;
        while (i < last) {
            String nonterminal = input[i++].trim();
            List<List<String>> rightSides = new ArrayList<>();
            while (i < last && input[i].charAt(0) == ' ') {
                if (input[i].trim().equals(EPSILON)) {
                    rightSides.add(Collections.emptyList());
                } else {
                    rightSides.add(Arrays.asList(input[i].trim().split(" ")));
                }
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

}
