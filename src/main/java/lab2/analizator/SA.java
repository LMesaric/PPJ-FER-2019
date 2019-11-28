package lab2.analizator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SA {

    public static void main(String[] args) throws IOException {
        String inputText = new String(readAllFromStdin(), StandardCharsets.UTF_8);

        List<LexicalToken> inputTokens = new ArrayList<>();
        for (String line : inputText.split("\\r?\\n")) {
            inputTokens.add(LexicalToken.fromLine(line));
        }

        Map<Integer, Map<String, Object>> actionTable = ObjectReaderUtil.readMapFromFile(Constants.ACTION_TABLE_PATH);
        Map<Integer, Map<String, Object>> newStateTable = ObjectReaderUtil.readMapFromFile(Constants.NEW_STATE_TABLE_PATH);
        //TODO newStateTable must be Map<Integer, Map<String, Put>>
    }

    private static byte[] readAllFromStdin() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
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
