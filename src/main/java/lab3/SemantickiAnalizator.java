package lab3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SemantickiAnalizator {

    int currentScope = 0;

    enum Type {
        VOID, CHAR, INT
    }

    public static void main(String[] args) {
        String inputText = new String(readAllFromStdin(), StandardCharsets.UTF_8);
        Node root = buildTree(inputText.split("\r?\n"));

        //TODO Epsilon production has a node with text set to "$" (Constants.EPSILON)
    }

    private static void processCompileUnit(Node node) {
        for (Node child: node.children) {
            switch (node.elements.get(0)) {
                case "<prijevodna_jedinica>":
                    processCompileUnit(child);
                    break;
                case "<vanjska_deklaracija>":
                    processOuterDeclaration(child);
                    break;
            }
        }
    }

    private static void processOuterDeclaration(Node node) {
        for (Node child: node.children) {
            switch (node.elements.get(0)) {
                case "<definicija_funkcije>":
                    processFunctionDefinition(child);
                    break;
                case "<deklaracija>":
                    processDeclaration(child);
                    break;
            }
        }
    }

    private static void processFunctionDefinition(Node node) {
        int typeDefPos;
        String functionName;
        TypeWrapper type;
        List<Type> parameters = null;
        for (Node child: node.children) {
            switch (node.elements.get(0)) {
                case "<ime_tipa>":
                    type = typeName(child);
                    break;
                case "<IDN>":
                    functionName = node.elements.get(2);
                    break;
                case "<lista_parametara>":

                    break;
                case "<slozena_naredba>":
                    break;
            }
        }
    }

    private static TypeWrapper typeName(Node node) {
        boolean constant = false;
        Type type = null;
        for (Node child: node.children) {
            switch (child.elements.get(0)) {
                case "KR_CONST":
                    constant = true;
                    break;
                case "<specifikator_tipa>":
                    type = typeSpecifier(child);
                    break;
            }
        }
        return new TypeWrapper(constant, type);
    }

    private static Type typeSpecifier(Node node) {
        switch (node.elements.get(0)) {
            case "KR_VOID":
                return Type.VOID;
            case "KR_CHAR":
                return Type.CHAR;
            default:
                return Type.INT;
        }
    }

    private static void processDeclaration(Node node) {
        for (Node child: node.children) {
            switch (node.elements.get(0)) {
                case "<definicija_funkcije>":
                    processCompileUnit(child);
                    break;
                case "<deklaracija>":
                    processOuterDeclaration(child);
            }
        }
    }

    private static Node buildTree(String[] lines) {
        if (lines.length == 0 || lines.length == 1 && lines[0].trim().length() == 0)
            return null;

        Node root = new Node(lines[0].trim(), null);
        Node currentRefNode = root;
        int currentRefLevel = 0;

        for (int i = 1, linesLength = lines.length; i < linesLength; i++) {
            String line = lines[i];
            int level = countLeadingSpaces(line);
            String trimmed = line.trim();
            Node newNode;
            if (level <= currentRefLevel) {
                currentRefNode = currentRefNode.getDistantParent(currentRefLevel - level + 1);
                currentRefLevel -= currentRefLevel - level + 1;
            }
            newNode = currentRefNode.addChildLast(trimmed);
            currentRefLevel++;
            currentRefNode = newNode;
        }

        return root;
    }

    static int countLeadingSpaces(String s) {
        int n = 0;
        for (char c : s.toCharArray())
            if (c == ' ') n++;
            else break;
        return n;
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

    private static class TypeWrapper {
        boolean constant;
        Type type;

        TypeWrapper(boolean constant, Type type) {
            this.constant = constant;
            this.type = type;
        }
    }

}
