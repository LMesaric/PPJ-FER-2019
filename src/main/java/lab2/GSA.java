package lab2;

import java.util.*;

public class GSA {

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
        Set<String> terminalSymbols = cleanSymbolInput(readInput[1]);
        Set<String> synchronizationalSymbols = cleanSymbolInput(readInput[2]);

        Map<String, List<List<String>>> productions = parseProductions(readInput, 3);
    }

    private static Set<String> cleanSymbolInput(String line) {
        String[] tmp = line.split(" ");
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
                rightSides.add(Arrays.asList(input[i++].trim().split(" ")));
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