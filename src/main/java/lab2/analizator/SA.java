package lab2.analizator;

import lab2.Put;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class SA {

    public static void main(String[] args) throws IOException {
        String inputText = new String(readAllFromStdin(), StandardCharsets.UTF_8);

        // force get() in O(1)
        ArrayList<LexicalToken> inputTokens = new ArrayList<>();
        if (!inputText.trim().isEmpty()) {
            for (String line : inputText.split("\\r?\\n"))
                inputTokens.add(LexicalToken.fromLine(line));
        }

        Map<Integer, Map<String, Object>> actionTable = ObjectReaderUtil.readMapFromFile(Constants.ACTION_TABLE_PATH);
        Map<Integer, Map<String, Put>> newStateTable = ObjectReaderUtil.readMapFromFile(Constants.NEW_STATE_TABLE_PATH);
        Set<String> synchronizationalSymbols = ObjectReaderUtil.readSetFromFile(Constants.SYNCHRONIZATIONAL_SYMBOLS_PATH);

        try {
            Node root = new LR(inputTokens, actionTable, newStateTable, synchronizationalSymbols).parse();
            StringBuilder sb = new StringBuilder();
            buildTreeDFS(root, 0, sb);
            System.out.print(sb.toString());
        } catch (NoSuchElementException | IllegalStateException ignored) {
        }
    }

    @SuppressWarnings("Duplicates")
    private static byte[] readAllFromStdin() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[32 * 1024];
            int bytesRead;
            while ((bytesRead = System.in.read(buffer)) > 0)
                baos.write(buffer, 0, bytesRead);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void buildTreeDFS(Node root, int depth, StringBuilder output) {
        for (int i = 0; i < depth; i++)
            output.append(' ');
        output.append(root.text).append('\n');
        for (Node child : root.children)
            buildTreeDFS(child, depth + 1, output);
    }

}
