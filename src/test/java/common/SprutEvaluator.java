package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SprutEvaluator {

    private static final String JAVA_EXEC = "java";
    private static final String JAVA_PARAMS1 = "-cp";
    private static final String JAVA_PARAMS2 = "target/classes";
    private static final int BUFFER_LENGTH = 1024;
    private static final int MAX_TIMEOUT_MS = 15000;

    private final boolean twoProcesses;
    private final Consumer<String> outputConsumer;
    // private final Consumer<String> errorConsumer;
    private final Path testsDirectory;
    private final String generatorClass;
    private final String analyzerClass;
    private final String inputRegex;
    private final String definitionRegex;
    private final String outputRegex;

    public SprutEvaluator(Consumer<String> outputConsumer, String testsDirectory,
                          String generatorClass, String analyzerClass,
                          String inputRegex, String definitionRegex, String outputRegex) {
        this(true, outputConsumer, testsDirectory, generatorClass, analyzerClass,
                inputRegex, definitionRegex, outputRegex);
    }

    public SprutEvaluator(Consumer<String> outputConsumer, String testsDirectory,
                          String analyzerClass, String inputRegex, String outputRegex) {
        this(false, outputConsumer, testsDirectory, null, analyzerClass,
                inputRegex, null, outputRegex);
    }

    private SprutEvaluator(boolean twoProcesses, Consumer<String> outputConsumer, String testsDirectory,
                           String generatorClass, String analyzerClass,
                           String inputRegex, String definitionRegex, String outputRegex) {
        this.twoProcesses = twoProcesses;
        this.outputConsumer = outputConsumer;
        this.testsDirectory = Paths.get(testsDirectory);
        this.generatorClass = generatorClass;
        this.analyzerClass = analyzerClass;
        this.inputRegex = inputRegex;
        this.definitionRegex = definitionRegex;
        this.outputRegex = outputRegex;
    }

    public boolean evaluateAll() throws IOException, InterruptedException {
        int testCounter = 0;
        int passCounter = 0;
        for (Path caseDir : Files.newDirectoryStream(testsDirectory)) {
            String caseName = caseDir.getFileName().toString();
            Path input = null;
            Path expected = null;
            Path definition = null;
            for (Path file : Files.newDirectoryStream(caseDir)) {
                String fileName = file.getFileName().toString();
                if (fileName.matches(inputRegex)) input = file;
                if (twoProcesses && fileName.matches(definitionRegex)) definition = file;
                if (fileName.matches(outputRegex)) expected = file;
            }

            if (input == null || expected == null || (twoProcesses && definition == null)) {
                print("Test %s does not have all required files!", caseName);
                continue;
            }

            testCounter++;
            boolean result = evaluateTestCase(caseName, definition, input, expected);
            if (result) passCounter++;
        }

        print("Test result: %d/%d passed.", passCounter, testCounter);
        return passCounter == testCounter;
    }

    private boolean evaluateTestCase(String caseName, Path definition, Path input, Path output)
            throws IOException, InterruptedException {
        print("--------------------");
        print("TEST %s", caseName);

        long generatorStart = 0;
        long generatorEnd = 0;
        if (twoProcesses) {
            ProcessBuilder genBuilder = new ProcessBuilder()
                    .command(JAVA_EXEC, JAVA_PARAMS1, JAVA_PARAMS2, generatorClass);

            generatorStart = System.nanoTime();
            Process generator = genBuilder.start();
            injectFileAsStdinToProcess(definition, generator);
            generator.getOutputStream().close();
            generator.waitFor(MAX_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (generator.isAlive()) {
                print("Generator Time Limit Exceeded.\n");
                generator.destroy();
                return false;
            }
            generatorEnd = System.nanoTime();
        }

        ProcessBuilder laBuilder = new ProcessBuilder()
                .command(JAVA_EXEC, JAVA_PARAMS1, JAVA_PARAMS2, analyzerClass);

        long analyzerStart = System.nanoTime();
        Process analyzer = laBuilder.start();
        injectFileAsStdinToProcess(input, analyzer);
        analyzer.getOutputStream().close();

        List<String> expected = Files.readAllLines(output);
        List<String> stdout = readLinesFromInputStream(analyzer.getInputStream());
        List<String> stderr = readLinesFromInputStream(analyzer.getErrorStream());
        analyzer.waitFor(MAX_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (analyzer.isAlive()) {
            print("Analyzer Time Limit Exceeded.\n");
            analyzer.destroy();
            return false;
        }
        long analyzerEnd = System.nanoTime();

        boolean isExpected = expected.equals(stdout);

        print("Status: %s", isExpected ? "PASS" : "FAIL");
        if (twoProcesses)
            print("Generator time: %.3f s", getTimeInSeconds(generatorStart, generatorEnd));
        print("Analyzer time: %.3f s", getTimeInSeconds(analyzerStart, analyzerEnd));
        print("Analyzer stderr:");
        stderr.forEach(s -> outputConsumer.accept("\t" + s));
        if (!isExpected) {
            print("Expected:");
            expected.forEach(s -> outputConsumer.accept("\t" + s));
            print("Actual:");
            stdout.forEach(s -> outputConsumer.accept("\t" + s));
        }
        print("--------------------");
        print("");

        return isExpected;
    }

    private void injectFileAsStdinToProcess(Path filePath, Process process) throws IOException {
        try (InputStream is = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[BUFFER_LENGTH];
            int len;
            while ((len = is.read(buffer)) != -1) {
                process.getOutputStream().write(buffer, 0, len);
            }
        }
    }

    private List<String> readLinesFromInputStream(InputStream is) throws IOException {
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

    private void print(String format, Object... args) {
        outputConsumer.accept(String.format(format, args));
    }

    private double getTimeInSeconds(long measurementStart, long measurementEnd) {
        return (measurementEnd - measurementStart) / 1000000000.0;
    }
}
