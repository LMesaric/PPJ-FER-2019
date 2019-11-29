package lab2.analizator;

import lab2.Put;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SA {

    public static void main(String[] args) throws IOException {
        String inputText = new String(readAllFromStdin(), StandardCharsets.UTF_8);

        // force get() in O(1)
        ArrayList<LexicalToken> inputTokens = new ArrayList<>();
        if (!inputText.trim().isEmpty()) {
            for (String line : inputText.split("\\r?\\n")) {
                inputTokens.add(LexicalToken.fromLine(line));
            }
        }

        Map<Integer, Map<String, Object>> actionTable = ObjectReaderUtil.readMapFromFile(Constants.ACTION_TABLE_PATH);
        Map<Integer, Map<String, Put>> newStateTable = ObjectReaderUtil.readMapFromFile(Constants.NEW_STATE_TABLE_PATH);
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

    private Object getFromTable(Map<Integer, Map<String, Object>> map, Integer state, String symbol) {
        Map<String, Object> row = map.get(state);
        if (row == null) return null;
        return row.get(symbol);
    }

}
