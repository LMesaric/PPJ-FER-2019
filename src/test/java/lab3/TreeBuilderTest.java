package lab3;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.ThrowingConsumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static lab3.SemantickiAnalizator.buildTree;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TreeBuilderTest {

    private static final String TESTCASES_DIR = "src/test/resources/testcases_GSA_SA";
    private static final String TEST_FILE_NAME = "test.out";

    @org.junit.jupiter.api.TestFactory
    Stream<DynamicTest> testAll() throws IOException {
        List<Pair> inputList = new ArrayList<>();
        for (Path caseDir : Files.newDirectoryStream(Paths.get(TESTCASES_DIR))) {
            String caseName = caseDir.getFileName().toString();
            Path inputFile = caseDir.resolve(TEST_FILE_NAME);
            String inputText = new String(Files.readAllBytes(inputFile), StandardCharsets.UTF_8);
            inputList.add(new Pair(caseName, inputText));
        }
        Function<Pair, String> displayNameGenerator = p -> p.name;
        ThrowingConsumer<Pair> testExecutor = p -> assertEquals(p.input, parse(p.input));
        return DynamicTest.stream(inputList.iterator(), displayNameGenerator, testExecutor);
    }

    private static String parse(String input) {
        Node root = buildTree(input.split("\r?\n"));
        if (root == null)
            return "";
        StringBuilder sb = new StringBuilder();
        buildTreeDFS(root, 0, sb);
        return sb.toString();
    }

    private static void buildTreeDFS(Node root, int depth, StringBuilder output) {
        for (int i = 0; i < depth; i++)
            output.append(' ');
        output.append(root).append('\n');
        for (Node child : root.children)
            buildTreeDFS(child, depth + 1, output);
    }

    private static class Pair {
        private final String name;
        private final String input;

        private Pair(String name, String input) {
            this.name = name;
            this.input = input;
        }
    }

}
