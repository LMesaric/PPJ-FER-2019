package lab2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static lab2.Constants.INITIAL_STATE;
import static lab2.GSA.cleanSymbolInput;
import static lab2.GSA.parseProductions;

class TestUtil {

    static final String TESTCASES_DIR_ENFA = "src/test/resources/testcases_ENFA/";
    static final String TESTCASES_DIR_DFA = "src/test/resources/testcases_DFA/";

    static ENFA createENFA(String fileName) throws IOException {
        Path pathIn = Paths.get(TESTCASES_DIR_ENFA + fileName + ".in");

        String[] readInput = new String(Files.readAllBytes(pathIn)).split("\\r?\\n");
        Set<String> nonterminalSymbols = cleanSymbolInput(readInput[0]);
        Set<String> terminalSymbols = cleanSymbolInput(readInput[1]);
        List<String> symbols = new LinkedList<>(nonterminalSymbols);
        symbols.addAll(terminalSymbols);
        Map<String, List<List<String>>> productions = parseProductions(readInput, 3);
        nonterminalSymbols.add(INITIAL_STATE);
        String start = readInput[0].split(" ")[1];
        List<List<String>> production = new LinkedList<>();
        production.add(Collections.singletonList(start));
        productions.put(INITIAL_STATE, production);

        return new ENFA(INITIAL_STATE, productions, symbols, nonterminalSymbols);
    }

}
