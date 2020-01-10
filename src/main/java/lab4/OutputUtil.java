package lab4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class OutputUtil {

    // TODO: Keep only methods that will be used.

    static void writeToFileOutput(Iterable<String> lines) {
        writeToFileOutput(String.join(System.lineSeparator(), lines));
    }

    static void writeToFileOutput(String[] lines) {
        writeToFileOutput(String.join(System.lineSeparator(), lines));
    }

    static void writeToFileOutput(String text) {
        // Try up to three times in case of failures.
        @SuppressWarnings("unused")
        boolean b = writeToFileOutputInternal(text)
                || writeToFileOutputInternal(text)
                || writeToFileOutputInternal(text);
    }

    private static boolean writeToFileOutputInternal(String text) {
        Path path = Paths.get(Constants.FRISC_OUTPUT_PATH);
        try {
            Files.write(path, text.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
