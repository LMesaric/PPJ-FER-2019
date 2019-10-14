import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluatorTest {

    private static final String JAVA_EXEC = "java";
    private static final String JAVA_PARAMS1 = "-cp";
    private static final String JAVA_PARAMS2 = "target/classes";
    private static final String GENERATOR_CLASS = "GLA";
    private static final String ANALYZER_CLASS = "analizator.LA";

    private static final String TESTCASES_DIR = "testcases";

    @Test
    void test() throws IOException, InterruptedException {
        Path testsDir = Paths.get(TESTCASES_DIR);
        for (Path test : Files.newDirectoryStream(testsDir)) {
            Path input = null;
            Path output = null;
            Path definition = null;
            for (Path file : Files.newDirectoryStream(test)) {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".in")) input = file;
                if (fileName.endsWith(".lan")) definition = file;
                if (fileName.endsWith(".out")) output = file;
            }

            assertTrue(evaluateTestCase(definition, input, output));
        }
    }

    private static void prepareProcess(Path path, Process process) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                process.getOutputStream().write(buffer, 0, len);
            }
        }
    }

    private static boolean evaluateTestCase(Path definition, Path input, Path output) throws IOException, InterruptedException {
        ProcessBuilder genBuilder = new ProcessBuilder()
                .command(JAVA_EXEC, JAVA_PARAMS1, JAVA_PARAMS2, GENERATOR_CLASS);

        Process generator = genBuilder.start();
        prepareProcess(definition, generator);
        generator.getOutputStream().close();
        generator.waitFor();

        ProcessBuilder laBuilder = new ProcessBuilder()
                .command(JAVA_EXEC, JAVA_PARAMS1, JAVA_PARAMS2, ANALYZER_CLASS)
                .redirectErrorStream(false);

        Process analyzer = laBuilder.start();
        prepareProcess(input, analyzer);
        analyzer.getOutputStream().close();

        List<String> expected = Files.readAllLines(output);
        List<String> actual = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(analyzer.getInputStream()))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                actual.add(line);
            }
            analyzer.waitFor();
        }

        return expected.equals(actual);
    }

}
