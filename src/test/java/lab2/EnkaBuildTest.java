package lab2;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static lab2.Constants.EPSILON;
import static lab2.Constants.INITIAL_STATE;
import static org.junit.jupiter.api.Assertions.*;

class EnkaBuildTest {

    private static final String TESTCASES_DIR = "src/test/resources/testcases_ENKA/";

    @Test
    void test() throws IOException {
        assertTrue(test("test1"));
        assertTrue(test("test2"));
    }

    private boolean test(String fileName) throws IOException {
        Path pathIn = Paths.get(TESTCASES_DIR + fileName + ".in");
        Path pathOut = Paths.get(TESTCASES_DIR + fileName + ".out");

        String[] readInput = new String(Files.readAllBytes(pathIn)).split("\n");
        Set<String> nonterminalSymbols = cleanSymbolInput(readInput[0]);
        Set<String> terminalSymbols = cleanSymbolInput(readInput[1]);
        Set<String> synchronizationalSymbols = cleanSymbolInput(readInput[2]);
        Map<String, List<List<String>>> productions = parseProductions(readInput, 3);
        nonterminalSymbols.add(INITIAL_STATE);
        String start = readInput[0].split(" ")[1];
        List<List<String>> production = new LinkedList<>();
        production.add(Collections.singletonList(start));
        productions.put(INITIAL_STATE, production);

        String s = new Enka(INITIAL_STATE, productions, nonterminalSymbols).build().print();
        String[] arr1 = s.split("\n");
        String[] arr2 = new String(Files.readAllBytes(pathOut)).split("\n");
        for (int i = 0; i < arr2.length; i++) {
            arr2[i] = arr2[i].replace("\r", "");
        }

        for (int i = 0; i < arr1.length && i < arr2.length; i++) {
            if (!arr1[i].equals(arr2[i])) {
                System.out.println("Arr1 (" + i + ") :" + arr1[i]);
                System.out.println("Arr2 (" + i + ") :" + arr2[i]);
            }
        }

        return Arrays.equals(arr1, arr2);
    }

    private static Set<String> cleanSymbolInput(String line) {
        String[] tmp = line.trim().split(" ");
        Set<String> set = new HashSet<>();
        for (int i = 1, limit = tmp.length; i < limit; i++)
            set.add(tmp[i]);
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
