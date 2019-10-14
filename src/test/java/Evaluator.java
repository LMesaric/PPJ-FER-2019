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

class Evaluator {

    private static final String JAVA_EXEC = "java";
    private static final String JAVA_PARAMS1 = "-cp";
    private static final String JAVA_PARAMS2 = "target/classes";
    private static final String GENERATOR_CLASS = "GLA";
    private static final String ANALYZER_CLASS = "analizator.LA";
    private static final String GENERATED_DEFINITION = "generated.txt";

    private static final String TESTCASES_DIR = "testcases";
    private static final String TMP_DIR = ".";

    @Test
    static void main(String[] args) throws IOException, InterruptedException {
        Path testsDir = Paths.get(TESTCASES_DIR);
        for (Path test : Files.newDirectoryStream(testsDir)) {
            Path input = null;
            Path output = null;
            Path definition = null;
            for (Path file : Files.newDirectoryStream(test)) {
                if (file.getFileName().toString().endsWith(".in")) input = file;
                if (file.getFileName().toString().endsWith(".lan")) definition = file;
                if (file.getFileName().toString().endsWith(".out")) output = file;
            }

            evaluateTestCase(test.getFileName().toString(), definition, input, output);
        }
    }

    private static void evaluateTestCase(String testName, Path definition, Path input, Path output) throws IOException, InterruptedException {
        ProcessBuilder genBuilder = new ProcessBuilder()
                .command(JAVA_EXEC, JAVA_PARAMS1, JAVA_PARAMS2, GENERATOR_CLASS);
                //.directory(Paths.get(TMP_DIR).toFile());

        Process generator = genBuilder.start();
        try (InputStream is = Files.newInputStream(definition)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                generator.getOutputStream().write(buffer, 0, len);
            }
        }
        generator.getOutputStream().close();
        generator.waitFor();

        ProcessBuilder laBuilder = new ProcessBuilder()
                .command(JAVA_EXEC, JAVA_PARAMS1, JAVA_PARAMS2, ANALYZER_CLASS)
                .redirectErrorStream(false);
                //.directory(Paths.get(TMP_DIR).toFile());

        Process analyzer = laBuilder.start();
        try (InputStream is = Files.newInputStream(input)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                analyzer.getOutputStream().write(buffer, 0, len);
            }
        }
        analyzer.getOutputStream().close();

        List<String> excepted = Files.readAllLines(output);
        List<String> actual = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(analyzer.getInputStream()))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                actual.add(line);
            }
            analyzer.waitFor();
        }

        boolean pass;
        if (excepted.size() == actual.size()) {
            pass = true;
            for (int i = 0; i < excepted.size(); i++) {
                if (!excepted.get(i).trim().equals(actual.get(i).trim())) {
                    pass = false;
                    break;
                }
            }
        } else {
            pass = false;
        }

        System.out.println("TEST: " + testName + ": " + (pass ? "PASS" : "FAIL"));
        assertTrue(pass);
    }

}
