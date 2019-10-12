import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

// State description:
// STATE:
// <state name>
// TABLE:
// <enka table for regex>
// ACTIONS:
// <list of actions to be performed>
// TABLE:
// ...
public class LexerGenerator {

    public static void main(String[] args) throws IOException {
        StringBuilder readInput = new StringBuilder();
        try (Scanner sc = new Scanner(System.in)) {
            while (sc.hasNext()) {
                readInput.append(sc.nextLine()).append("\n");
            }
        }
        RegexPreprocessor rp = new RegexPreprocessor();
        // Expand regular expressions
        String input = rp.parse(readInput.toString());
        generateFinalTable(input.split("\n"));
    }

    private static void generateFinalTable(String[] input) throws IOException {
        // Maps each state to its description
        Map<String, StringBuilder> states = new HashMap<>();
        // Ignore all irrelevant lines
        String[] relevantInput = Arrays.copyOfRange(input, ignoreIrrelevantInput(input), input.length);
        for (int i = 0; i < relevantInput.length; i++) {
            i = addState(states, relevantInput, i);
        }
        createTable(states);
    }

    private static int ignoreIrrelevantInput(String[] input) {
        for (int i = 0; i < input.length; i++) {
            if (input[i].startsWith("%L")) {
                return i + 1;
            }
        }
        throw new IllegalStateException();
    }

    private static int addState(Map<String, StringBuilder> states, String[] input, int index) {
        // Extract state name
        StringBuilder stateBuilder = new StringBuilder();
        int i = 1;
        for (; i < input[index].length() && input[index].charAt(i) != '>'; i++) {
            stateBuilder.append(input[index].charAt(i));
        }
        String state = stateBuilder.toString();

        // Build table for the regex appended to the state name
        TableGenerator tg = new TableGenerator();
        String table = tg.buildTable(input[index].substring(i + 1));

        StringBuilder actions = new StringBuilder();
        for (index += 2; index < input.length && !input[index].equals("}"); index++) {
            actions.append(input[index]).append("\n");
        }

        states.putIfAbsent(state, new StringBuilder());
        states.get(state).append("TABLE:\n").append(table).append("ACTIONS:\n").append(actions);

        return index;
    }

    private static void createTable(Map<String, StringBuilder> states) throws IOException {
        Path path = Paths.get("./src/main/java/analizator/generated.txt");
        StringBuilder table = new StringBuilder();
        for (Map.Entry<String, StringBuilder> entry: states.entrySet()) {
            table.append("STATE:\n").append(entry.getKey()).append("\n").append(entry.getValue());
        }
        Files.write(path, table.toString().getBytes());
    }

}
