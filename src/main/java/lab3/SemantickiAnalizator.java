package lab3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SemantickiAnalizator {

    // TODO: remember scope of variables, stack of maps?
    int currentScope = 0;

    enum PrimitiveType {
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

    private static TypeExpression primaryExpression(Node node) {
        FullType fullType = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "IDN":
                    // TODO: check if IDN exists as function or variable declaration, return proper version of TypeExpression
                    break;
                case "BROJ":
                    if (checkInt(child.elements.get(2)) == null) {
                        error(node);
                    }
                    fullType = new FullType(new Type(false, PrimitiveType.INT));
                    break;
                case "ZNAK":
                    if (checkChar(child.elements.get(2)) == null) {
                        error(node);
                    }
                    fullType = new FullType(new Type(false, PrimitiveType.CHAR));
                case "NIZ_ZNAKOVA":
                    if (!checkConstCharArray(child.elements.get(2))) {
                        error(node);
                    }
                    fullType = new FullType(new Type(true, PrimitiveType.CHAR), child.elements.get(2).substring(1, child.elements.get(2).length() - 1).length() + 1);
                case "<izraz>":
                    return expression(child);
            }
        }
        return new TypeExpression(fullType, true);
    }

    private static TypeExpression postfixExpression(Node node) {
        TypeExpression typeExpression = null;
        for (Node child : node.children) {
            switch (node.elements.get(0)) {
                case "<primarni_izraz>":
                    return primaryExpression(child);
                case "<postfiks_izraz>":
                    typeExpression = postfixExpression(child);
                    break;
                case "<izraz>":
                    if (!checkIntCast(expression(child).fullType)) {
                        error(node);
                    }
                    break;
                case "L_UGL_ZAGRADA":
                    if (Objects.requireNonNull(typeExpression).fullType.arguments != null || Objects.requireNonNull(typeExpression).fullType.array) {
                        error(node);
                    }
                    return new TypeExpression(typeExpression.fullType, typeExpression.lExpression);
                case "<lista_argumenata>":
                    if (Objects.requireNonNull(typeExpression).fullType.arguments == null) {
                        error(node);
                    }
                    List<FullType> arguments = argumentList(child);
                    if (!typeExpression.fullType.arguments.equals(arguments)) {
                        error(node);
                    }
                    return typeExpression;
                case "D_ZAGRADA":
                    if (Objects.requireNonNull(typeExpression).fullType.arguments == null) {
                        error(node);
                    }
                    if (!typeExpression.fullType.arguments.isEmpty()) {
                        error(node);
                    }
                    return typeExpression;
                case "OP_INC":
                case "OP_DEC":
                    if (!Objects.requireNonNull(typeExpression).lExpression || !checkIntCast(typeExpression.fullType)) {
                        error(node);
                    }
                    return new TypeExpression(new FullType(new Type(false, PrimitiveType.INT)), false);
            }
        }
        return typeExpression;
    }

    private static List<FullType> argumentList(Node node) {
        List<FullType> arguments = new LinkedList<>();
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<izraz_pridruzivanja>":
                    arguments.add(Objects.requireNonNull(assigmentExpression(child)).fullType);
                    break;
                case "<lista_argumenata>":
                    arguments.addAll(argumentList(child));
                    break;
            }
        }
        return arguments;
    }

    private static TypeExpression unaryExpression(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<postfiks_izraz>":
                    return postfixExpression(child);
                case "<unarni_izraz>":
                    TypeExpression typeExpression = unaryExpression(child);
                    if (!Objects.requireNonNull(typeExpression).lExpression || !checkIntCast(typeExpression.fullType)) {
                        error(node);
                    }
                    return new TypeExpression(new FullType(new Type(false, PrimitiveType.INT)), false);
                case "<cast_izraz>":
                    TypeExpression typeExp = castExpression(child);
                    if (!checkIntCast(typeExp.fullType)) {
                        error(node);
                    }
                    return new TypeExpression(new FullType(new Type(false, PrimitiveType.INT)), false);
            }
        }
        throw new IllegalStateException();
    }

    private static TypeExpression castExpression(Node node) {
        TypeExpression typeExpression = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<unarni_izraz>":
                    return unaryExpression(child);
                case "<ime_tipa>":
                    Type type = typeName(child);
                    typeExpression = new TypeExpression(new FullType(type), false);
                    break;
                case "<cast_izraz>":
                    if (!checkExplicitCast(Objects.requireNonNull(typeExpression).fullType, castExpression(child).fullType)) {
                        error(node);
                    }
                    return typeExpression;
            }
        }
        throw new IllegalStateException();
    }

    private static Type typeName(Node node) {
        boolean constant = false;
        PrimitiveType primitiveType = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "KR_CONST":
                    constant = true;
                    break;
                case "<specifikator_tipa>":
                    primitiveType = typeSpecifier(child);
                    break;
            }
        }
        if (constant && primitiveType == PrimitiveType.VOID) {
            error(node);
        }
        return new Type(constant, primitiveType);
    }

    private static PrimitiveType typeSpecifier(Node node) {
        switch (node.elements.get(0)) {
            case "KR_VOID":
                return PrimitiveType.VOID;
            case "KR_CHAR":
                return PrimitiveType.CHAR;
            default:
                return PrimitiveType.INT;
        }
    }

    private static TypeExpression simpleExpression(Node node, String firstCase, String secondCase, Function<Node,
            TypeExpression> firstFunction, Function<Node, TypeExpression> secondFunction) {
        boolean seen = false;
        for (Node child : node.children) {
            if (child.elements.get(0).equals(firstCase)) {
                TypeExpression typeExpression = firstFunction.apply(child);
                if (!seen) {
                    return typeExpression;
                }
                if (!checkIntCast(typeExpression.fullType)) {
                    error(node);
                }
                return new TypeExpression(new FullType(new Type(false, PrimitiveType.INT)), false);
            } else if (child.elements.get(0).equals(secondCase)) {
                seen = true;
                if (!checkIntCast(secondFunction.apply(child).fullType)) {
                    error(node);
                }
            }
        }
        throw new IllegalStateException();
    }

    private static TypeExpression multiplicativeExpression(Node node) {
        return simpleExpression(node, "<cast_izraz>", "<multiplikativni_izraz>", SemantickiAnalizator::castExpression, SemantickiAnalizator::multiplicativeExpression);
    }

    private static TypeExpression additiveExpression(Node node) {
        return simpleExpression(node, "<multiplikativni_izraz>", "<aditivni_izraz>", SemantickiAnalizator::multiplicativeExpression, SemantickiAnalizator::additiveExpression);
    }

    private static TypeExpression relationalExpression(Node node) {
        return simpleExpression(node, "<aditivni_izraz>", "<odnosni_izraz>", SemantickiAnalizator::additiveExpression, SemantickiAnalizator::relationalExpression);
    }

    private static TypeExpression equationalExpression(Node node) {
        return simpleExpression(node, "<odnosni_izraz>", "<jednakosni_izraz>", SemantickiAnalizator::relationalExpression, SemantickiAnalizator::equationalExpression);
    }

    private static TypeExpression binaryAndExpression(Node node) {
        return simpleExpression(node, "<jednakosni_izraz>", "<bin i izraz>", SemantickiAnalizator::equationalExpression, SemantickiAnalizator::binaryAndExpression);
    }

    private static TypeExpression binaryXorExpression(Node node) {
        return simpleExpression(node, "<bin_i_izraz>", "<bin_xili_izraz>", SemantickiAnalizator::binaryAndExpression, SemantickiAnalizator::binaryXorExpression);
    }

    private static TypeExpression binaryOrExpression(Node node) {
        return simpleExpression(node, "<bin_xili_izraz>", "<bin_ili_izraz>", SemantickiAnalizator::binaryXorExpression, SemantickiAnalizator::binaryOrExpression);
    }

    private static TypeExpression logAndExpression(Node node) {
        return simpleExpression(node, "<bin_ili_izraz>", "<log_i_izraz>", SemantickiAnalizator::binaryOrExpression, SemantickiAnalizator::logAndExpression);
    }

    private static TypeExpression logOrExpression(Node node) {
        return simpleExpression(node, "<log_i_izraz>", "<log_ili_izraz>", SemantickiAnalizator::logAndExpression, SemantickiAnalizator::logOrExpression);
    }

    private static TypeExpression assigmentExpression(Node node) {
        TypeExpression typeExpression = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<log_ili_izraz>":
                    return logOrExpression(child);
                case "<postfiks_izraz>":
                    TypeExpression postfixExpression = postfixExpression(child);
                    if (!postfixExpression.lExpression) {
                        error(node);
                    }
                    typeExpression = new TypeExpression(postfixExpression.fullType, false);
                    break;
                case "<izraz_pridruzivanja>":
                    TypeExpression equationalExpression = equationalExpression(child);
                    if (!checkImplicitCast(equationalExpression.fullType, Objects.requireNonNull(typeExpression).fullType)) {
                        error(node);
                    }
                    return new TypeExpression(typeExpression.fullType, false);
            }
        }
        throw new IllegalStateException();
    }

    private static TypeExpression expression(Node node) {
        boolean seen = false;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<izraz_pridruzivanja>":
                    TypeExpression typeExpression = assigmentExpression(node);
                    if (!seen) {
                        return typeExpression;
                    } else {
                        return new TypeExpression(Objects.requireNonNull(typeExpression).fullType, false);
                    }
                case "<izraz>":
                    expression(child);
                    seen = true;
                    break;
            }
        }
        throw new IllegalStateException();
    }

    private static void complexCommand(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<lista_deklaracija>":
                    declarationList(child);
                    break;
                case "<lista_naredbi>":
                    commandList(child);
                    break;
            }
        }
    }

    private static void commandList(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<lista_naredbi>":
                    commandList(child);
                    break;
                case "<naredba>":
                    command(child);
                    break;
            }
        }
    }

    private static void command(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<slozena_naredba>":
                    complexCommand(child);
                    break;
                case "<izraz_naredba>":
                    expressionCommand(child);
                    break;
                case "<naredba_grananja>":
                    branchCommand(child);
                    break;
                case "<naredba_petlje>":
                    loopCommand(child);
                    break;
                case "<naredba_skoka>":
                    jumpCommand(child);
                    break;
            }
        }
    }

    private static FullType expressionCommand(Node node) {
        for (Node child : node.children) {
            if ("<izraz>".equals(child.elements.get(0))) {
                return expression(child).fullType;
            }
        }
        return new FullType(new Type(false, PrimitiveType.INT));
    }

    private static void branchCommand(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<izraz>":
                    checkIntCast(expression(child).fullType);
                    break;
                case "<naredba>":
                    command(child);
                    break;
            }
        }
    }

    private static void loopCommand(Node node) {
        boolean seen = false;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<izraz>":
                    TypeExpression expression = expression(child);
                    if (!seen) {
                        checkIntCast(expression.fullType);
                    }
                    break;
                case "<naredba>":
                    command(child);
                    break;
                case "<izraz_naredba>":
                    FullType expressionCommand = expressionCommand(child);
                    if (seen) {
                        checkIntCast(expressionCommand);
                    }
                    seen = true;
                    break;
            }
        }
    }

    private static void jumpCommand(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "KR_CONTINUE":
                case "KR_BREAK":
                    // TODO: check if program is inside loop
                    break;
                case "KR_RETURN":
                    // TODO: check if program is inside function
                    break;
                case "<izraz>":
                    TypeExpression expression = expression(child);
                    // TODO: check if expression type matches current function return type
                    break;
            }
        }
    }

    private static void compileUnit(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
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

    private static void functionDefinition(Node node) {
        String functionName = null;
        Type returnType = null;
        // TODO: create new scope for variables
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<ime_tipa>":
                    returnType = typeName(child);
                    if (returnType.constant) {
                        error(node);
                    }
                    break;
                case "<IDN>":
                    // TODO: check if there is already defined function with given typename
                    functionName = child.elements.get(2);
                    break;
                case "KR_VOID":
                    // TODO: remember function definition and declaration
                    // TODO: if the function is already declared check if it has expected type
                    break;
                case "<lista_parametara>":
                    // TODO: check if function name is already declared if this definition is same as declaration
                    LinkedHashSet<Variable> parameters = parameterList(child);
                    // TODO: add parameters to newly created scope
                    break;
                case "<slozena_naredba>":
                    complexCommand(child);
                    break;
            }
        }
    }

    private static LinkedHashSet<Variable> parameterList(Node node) {
        // We need to preserve order of elements
        LinkedHashSet<Variable> parameters = new LinkedHashSet<>();
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

    private static Variable parameterDeclaration(Node node) {
        String name = null;
        boolean array = false;
        Type type = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<ime_tipa>":
                    type = typeName(child);
                    if (type.primitiveType == PrimitiveType.VOID) {
                        error(node);
                    }
                    break;
                case "IDN":
                    name = child.elements.get(2);
                    break;
                case "L_UGL_ZAGRADA":
                    array = true;
                    break;
            }
        }
        // TODO: remember parameter in a map
        return new Variable(name, new FullType(type));
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

    private static void declaration(Node node) {
        Type type = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<ime_tipa>":
                    type = typeName(child);
                    break;
                case "<lista_init_deklaratora>":
                    initDeclaratorList(child, type);
                    break;
            }
        }
    }

    private static void initDeclaratorList(Node node, Type type) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<init_deklarator>":
                    initDeclarator(child, type);
                    break;
                case "<lista_init_deklaratora>":
                    initDeclaratorList(child, type);
                    break;
            }
        }
    }

    private static void initDeclarator(Node node, Type type) {
        FullType directDeclaratorType = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<izravni_deklarator>":
                    directDeclaratorType = directDeclarator(child, type);
                    break;
                case "<inicijalizator>":
                    if (Objects.requireNonNull(directDeclaratorType).arguments != null) {
                        error(node);
                    }
                    InitializerWrapper wrapper = initializer(child);
                    if (wrapper.type != null) {
                        if (directDeclaratorType.array || !checkImplicitCast(wrapper.type, new FullType(directDeclaratorType.type))) {
                            error(node);
                        }
                    } else {
                        if (!directDeclaratorType.array) {
                            error(node);
                        }
                        for (FullType ft : wrapper.types) {
                            if (!checkImplicitCast(ft, new FullType(Objects.requireNonNull(directDeclaratorType).type))) {
                                error(node);
                            }
                        }
                    }
                    return;
            }
        }
        if (Objects.requireNonNull(directDeclaratorType).type.constant) {
            error(node);
        }
    }

    private static FullType directDeclarator(Node node, Type type) {
        String name = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "IDN":
                    name = child.elements.get(2);
                    break;
                case "BROJ":
                    int brElements = checkInt(child.elements.get(2));
                    if (brElements <= 0 || brElements > 1024) {
                        error(node);
                    }
                    if (type.primitiveType == PrimitiveType.VOID) {
                        error(node);
                    }
                    // TODO: check if name is not already declared in local space
                    // TODO: remember declaration
                    return new FullType(type, brElements);
                case "KR_VOID":
                    // TODO: check if name is already declared and matches current function type
                    // TODO: remember function declaration
                    return new FullType(type, new LinkedList<>());
                case "<lista_parametara>":
                    // TODO: check if name is already declared and matches current function type
                    // TODO: remember function declaration
                    return new FullType(type, parameterList(child).stream().map(Variable::getFullType).collect(Collectors.toList()));
            }
        }
        if (type.primitiveType == PrimitiveType.VOID) {
            error(node);
        }
        // TODO: check if name is not already declared in local space
        // TODO: remember declaration
        return new FullType(type);
    }

    private static InitializerWrapper initializer(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<izraz_pridruzivanja>":
                    FullType assigmentExpressionType = assigmentExpression(child).fullType;
                    if (assigmentExpressionType.array) {
                        List<FullType> charTypes = new LinkedList<>();
                        for (int i = 0; i < assigmentExpressionType.brElements; i++) {
                            charTypes.add(new FullType(new Type(false, PrimitiveType.CHAR)));
                        }
                        return new InitializerWrapper(new LinkedList<>(charTypes));
                    }
                    return new InitializerWrapper(assigmentExpressionType);
                case "<lista_izraza_pridruzivanja> ":
                    return new InitializerWrapper(assigmentExpressionList(child));
            }
        }
        throw new IllegalStateException();
    }

    private static List<FullType> assigmentExpressionList(Node node) {
        List<FullType> types = new LinkedList<>();
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<izraz_pridruzivanja>":
                    types.add(assigmentExpression(child).fullType);
                    break;
                case "<lista_izraza_pridruzivanja> ":
                    types.addAll(assigmentExpressionList(child));
                    break;
            }
        }
        return types;
    }

    private static class Type {
        boolean constant;
        PrimitiveType primitiveType;

        Type(boolean constant, PrimitiveType type) {
            this.constant = constant;
            this.primitiveType = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Type type = (Type) o;
            return constant == type.constant &&
                    primitiveType == type.primitiveType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(constant, primitiveType);
        }
    }

    private static class FullType {
        boolean array;
        Type type;
        // Used if type is array
        int brElements;
        // Not null if type is function
        List<FullType> arguments = null;

        FullType(Type type) {
            this.type = Objects.requireNonNull(type);
        }

        FullType(Type type, int brElements) {
            this(type);
            this.brElements = brElements;
            this.array = true;
        }

        FullType(Type returnType, List<FullType> arguments) {
            this(returnType);
            this.arguments = Objects.requireNonNull(arguments);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FullType fullType = (FullType) o;
            return array == fullType.array &&
                    brElements == fullType.brElements &&
                    type.equals(fullType.type) &&
                    Objects.equals(arguments, fullType.arguments);
        }

        @Override
        public int hashCode() {
            return Objects.hash(array, type, brElements, arguments);
        }
    }

    private static class Variable {
        String name;
        FullType fullType;

        Variable(String name, FullType fullType) {
            this.name = name;
            this.fullType = fullType;
        }

        FullType getFullType() {
            return fullType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Variable variable = (Variable) o;
            return name.equals(variable.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    private static class TypeExpression {
        FullType fullType = null;
        boolean lExpression = false;

        TypeExpression(FullType type, boolean lValue) {
            this.fullType = type;
            this.lExpression = lValue;
        }

    }

    private static class InitializerWrapper {
        FullType type;
        List<FullType> types;

        InitializerWrapper(FullType type) {
            this.type = type;
        }

        InitializerWrapper(List<FullType> types) {
            this.types = types;
        }
    }

    // TODO
    private static Integer checkInt(String i) {
        return 0;
    }

    // TODO
    private static Character checkChar(String c) {
        return 0;
    }

    // TODO
    private static boolean checkConstCharArray(String arr) {
        return true;
    }

    private static boolean checkIntCast(FullType fullType) {
        return !(fullType.array || fullType.type.primitiveType == PrimitiveType.VOID);
    }

    private static boolean checkExplicitCast(FullType from, FullType into) {
        if (checkImplicitCast(from, into)) {
            return true;
        }
        return !from.array && !into.array && from.type.primitiveType == PrimitiveType.INT && into.type.primitiveType == PrimitiveType.CHAR;
    }

    private static boolean checkImplicitCast(FullType from, FullType into) {
        if (from.array && into.array && from.type.primitiveType == into.type.primitiveType && (!from.type.constant || into.type.constant)) {
            return true;
        }
        return !from.array && !into.array && (into.type.primitiveType == PrimitiveType.INT && from.type.primitiveType != PrimitiveType.VOID || from.type.primitiveType == PrimitiveType.CHAR && into.type.primitiveType == PrimitiveType.CHAR);
    }

    private static void error(Node node) {
        System.out.println(node);
        System.exit(0);
    }

    // Tree builder
    static Node buildTree(String[] lines) {
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
