package analizator;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// S_starting_state
// STATE:
// S_state
// TABLE:
// <enka table for regex>
// ACTIONS:
// <list of actions to be performed>
// ENDRULE
// TABLE:
// <enka table for regex>
// ACTIONS:
// <list of actions to be performed>
// ...
// ENDRULE
// ENDSTATE
// STATE:
// ...
public class LA {

    public static void main(String[] args) throws IOException {
        String inputText = new String(readAllFromStdin(), StandardCharsets.UTF_8);
        String startingState;
        Map<String, List<Rule>> stateRules;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(Constants.DEFINITION_FILENAME))) {
            startingState = reader.readLine().trim();
            stateRules = loadStateRules(reader);
        }

        Simulator simulator = new Simulator(startingState, stateRules, inputText, System.out::println, System.err::println);
        simulator.simulate();
    }

    private static Map<String, List<Rule>> loadStateRules(BufferedReader reader) throws IOException {
        Map<String, List<Rule>> stateRules = new HashMap<>();
        while (true) {
            String line = reader.readLine();          // Read STATE:
            if (line == null) break;

            String stateName = reader.readLine().trim();
            List<Rule> rules = readAllRules(reader);
            stateRules.put(stateName, rules);
        }
        return stateRules;
    }

    private static List<Rule> readAllRules(BufferedReader reader) throws IOException {
        List<Rule> rules = new ArrayList<>();
        while (reader.readLine().startsWith("TABLE:")) {
            StringBuilder sb = new StringBuilder();
            while (true) {
                String ln = reader.readLine().trim();
                if (ln.startsWith("ACTIONS")) break;
                sb.append(ln).append("\n");
            }
            sb.deleteCharAt(sb.length() - 1);
            Enka enka = new Enka();
            enka.buildFromTable(sb.toString());

            List<String> actions = new ArrayList<>();
            while (true) {
                String ln = reader.readLine().trim();
                if (ln.startsWith("ENDRULE")) break;
                actions.add(ln);
            }

            rules.add(new Rule(enka, actions));
        }
        return rules;
    }

    private static byte[] readAllFromStdin() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[32 * 1024];

            int bytesRead;
            while ((bytesRead = System.in.read(buffer)) > 0) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
