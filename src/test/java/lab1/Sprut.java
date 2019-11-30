package lab1;

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

class Sprut {

    private static final String JAVA_EXEC = "java";
    private static final String JAVA_PARAMS1 = "-cp";
    private static final String JAVA_PARAMS2 = "target/classes";
    private static final String GENERATOR_CLASS = "lab1.GLA";
    private static final String ANALYZER_CLASS = "lab1.analizator.LA";

    private static final String TESTCASES_DIR = "src/test/resources/testcases_GLA_LA";

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("SPRUT evaluator v1.0");
        System.out.println("Starting tests");
        System.out.println();

        long testStart = System.nanoTime();
        testAll(false);
        long testEnd = System.nanoTime();
        System.out.printf("Total time time: %.3f s%n", (testEnd - testStart) / 1000000000.0);
    }

    static void testAll(boolean runFromJunit) throws IOException, InterruptedException {
        Path testsDir = Paths.get(TESTCASES_DIR);
        int failed = 0;
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

            boolean result = evaluateTestCase(test.getFileName().toString(), definition, input, output, runFromJunit);
            if (!result) failed++;
            if (runFromJunit) assertTrue(result);
        }
        if (!runFromJunit) System.out.println("Failed: " + failed + " tests.");
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

    private static List<String> inputStreamToLines(InputStream is) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                lines.add(line);
            }
        }
        return lines;
    }

    private static boolean evaluateTestCase(String testName, Path definition, Path input, Path output, boolean runFromJunit)
            throws IOException, InterruptedException {
        ProcessBuilder genBuilder = new ProcessBuilder()
                .command(JAVA_EXEC, JAVA_PARAMS1, JAVA_PARAMS2, GENERATOR_CLASS);

        long generatorStart = System.nanoTime();
        Process generator = genBuilder.start();
        prepareProcess(definition, generator);
        generator.getOutputStream().close();
        generator.waitFor();
        long generatorEnd = System.nanoTime();

        ProcessBuilder laBuilder = new ProcessBuilder()
                .command(JAVA_EXEC, JAVA_PARAMS1, JAVA_PARAMS2, ANALYZER_CLASS)
                .redirectErrorStream(false);

        long analyzerStart = System.nanoTime();
        Process analyzer = laBuilder.start();
        prepareProcess(input, analyzer);
        analyzer.getOutputStream().close();

        List<String> expected = Files.readAllLines(output);
        List<String> stdout = inputStreamToLines(analyzer.getInputStream());
        List<String> stderr = inputStreamToLines(analyzer.getErrorStream());
        analyzer.waitFor();
        long analyzerEnd = System.nanoTime();

        boolean isExpected = expected.equals(stdout);

        if (!runFromJunit) {
            System.out.println("--------------------");
            System.out.printf("TEST %s: %s%n", testName, isExpected ? "PASS" : "FAIL");
            System.out.printf("Generator time: %.3f s%n", (generatorEnd - generatorStart) / 1000000000.0);
            System.out.printf("Analyzer time: %.3f s%n", (analyzerEnd - analyzerStart) / 1000000000.0);
            //System.out.println("Analyzer stderr:");
            //stderr.forEach(s -> System.out.println("\t" + s));
            if (!isExpected) {
                System.out.println("Expected:");
                expected.forEach(s -> System.out.println("\t" + s));
                System.out.println("Actual:");
                stdout.forEach(s -> System.out.println("\t" + s));
            }
            System.out.println("--------------------");
            System.out.println();
        }

        return isExpected;
    }

}
