package analizator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Simulator {

    private List<Rule> allRules;
    private Map<String, Rule> rulesMap;
    private Rule startingRule;

    public Simulator(BufferedReader definitionReader) throws IOException {
        while (true) {
            Rule rule = loadSingleDefinition(definitionReader);
            if (rule == null) break;
            allRules.add(rule);
            rulesMap.put(rule.getState(), rule);
        }
    }

    private Rule loadSingleDefinition(BufferedReader definitionReader) throws IOException {
        if (definitionReader.readLine() == null) return null;

        String state = definitionReader.readLine().trim();
        definitionReader.readLine();

        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = definitionReader.readLine().trim();
            if (line.startsWith("ACTIONS")) break;
            sb.append(line + "\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        Enka enka = new Enka();
        enka.buildFromTable(sb.toString());

        List<String> actions = new ArrayList<>();
        while (true) {
            String line = definitionReader.readLine().trim();
            if (line.startsWith("STATE")) break;
            actions.add(line);
        }

        return new Rule(state, enka, actions);
    }

    public void simulate(String input, BufferedWriter resultWriter) {

    }

}
