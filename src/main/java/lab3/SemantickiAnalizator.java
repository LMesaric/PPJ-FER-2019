package lab3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Objects;

public class SemantickiAnalizator {

    // TODO: remember scope of variables, stack of maps?
    int currentScope = 0;

    enum Type {
        VOID, CHAR, INT
    }

    public static void main(String[] args) {
        String inputText = new String(readAllFromStdin(), StandardCharsets.UTF_8);
        Node root = buildTree(inputText.split("\r?\n"));
        if (root != null) {
            compileUnit(root);
        }
        //TODO Epsilon production has a node with text set to "$" (Constants.EPSILON)
    }

    private static void compileUnit(Node node) {
        for (Node child : node.children) {
            switch (node.elements.get(0)) {
                case "<prijevodna_jedinica>":
                    compileUnit(child);
                    break;
                case "<vanjska_deklaracija>":
                    outerDeclaration(child);
                    break;
            }
        }
    }

    private static void outerDeclaration(Node node) {
        for (Node child : node.children) {
            switch (node.elements.get(0)) {
                case "<definicija_funkcije>":
                    functionDefinition(child);
                    break;
                case "<deklaracija>":
                    declaration(child);
                    break;
            }
        }
    }

    // TODO remember function definition
    private static void functionDefinition(Node node) {
        String functionName = null;
        TypeWrapper returnType = null;
        LinkedHashSet<ParameterDeclaration> parameters = null;
        // TODO: create new scope for variables
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<ime_tipa>":
                    // TODO: check
                    returnType = typeName(child);
                    break;
                case "<IDN>":
                    // TODO: check
                    functionName = child.elements.get(2);
                    break;
                case "<lista_parametara>":
                    // TODO: check if function name is already declared if this definition is same as declaration
                    parameters = parameterList(child);
                    // TODO: add parameters to newly created scope
                    break;
                case "<slozena_naredba>":
                    complexCommand(child);
                    break;
            }
        }
    }

    private static LinkedHashSet<ParameterDeclaration> parameterList(Node node) {
        // We need to preserve order of elements
        LinkedHashSet<ParameterDeclaration> parameters = new LinkedHashSet<>();
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<lista_parametara>":
                    parameters.addAll(parameterList(child));
                    break;
                case "<deklaracija_parametra>":
                    if (!parameters.add(parameterDeclaration(child))) {
                        error(node);
                    }
                    break;
            }
        }
        return parameters;
    }

    private static void complexCommand(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<lista_deklaracija>":

                    break;
                case "<lista_naredbi>":
                    break;
            }
        }
    }

    private static void declarationList(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<lista_deklaracija>":
                    declarationList(child);
                    break;
                case "<deklaracija>":
                    declaration(child);
                    break;
            }
        }
    }

    // TODO: remember parameter in a map
    private static ParameterDeclaration parameterDeclaration(Node node) {
        boolean array = false;
        String name = null;
        TypeWrapper type = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<ime_tipa>":
                    type = typeName(child);
                    break;
                case "IDN":
                    name = child.elements.get(2);
                    break;
                case "L_UGL_ZAGRADA":
                    array = true;
                    break;
            }
        }
        if (Objects.requireNonNull(type).type == Type.VOID) {
            error(node);
        }
        return new ParameterDeclaration(array, name, type);
    }

    private static TypeWrapper typeName(Node node) {
        boolean constant = false;
        Type type = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "KR_CONST":
                    constant = true;
                    break;
                case "<specifikator_tipa>":
                    type = typeSpecifier(child);
                    break;
            }
        }
        if (constant && type == Type.VOID) {
            error(node);
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

    // TODO: remember function declaration
    private static void declaration(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<ime_tipa>":
                    typeName(node);
                    break;
                case "<lista_init_deklaratora>":
                    // TODO: implement
                    break;
            }
        }
    }

    private static class TypeWrapper {
        boolean constant;
        Type type;

        TypeWrapper(boolean constant, Type type) {
            this.constant = constant;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypeWrapper that = (TypeWrapper) o;
            return constant == that.constant &&
                    type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(constant, type);
        }
    }

    private static class ParameterDeclaration {
        boolean array;
        String name;
        TypeWrapper type;

        ParameterDeclaration(boolean array, String name, TypeWrapper type) {
            this.array = array;
            this.name = name;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParameterDeclaration that = (ParameterDeclaration) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    private static void error(Node node) {
        System.out.println(node);
        System.exit(0);
    }

    // Tree builder
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

}
