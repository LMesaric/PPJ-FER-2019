package lab3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "Duplicates"})
public class SemantickiAnalizator {

    private static FullType currentFunction;

    private static boolean loop;

    private static boolean functionError;

    private static final Deque<Map<String, TypeExpression>> tables = new ArrayDeque<>();

    private static final Map<String, TypeExpression> functionDeclarations = new HashMap<>();

    enum PrimitiveType {
        VOID, CHAR, INT
    }

    public static void main(String[] args) {
        String inputText = new String(readAllFromStdin(), StandardCharsets.UTF_8);
        Node root = buildTree(inputText.split("\r?\n"));
        if (root != null) {
            tables.addFirst(new HashMap<>());
            compileUnit(root);
            TypeExpression main = tables.getFirst().get("main");
            if (main == null || !main.defined || main.fullType.type.primitiveType != PrimitiveType.INT || !main.fullType.arguments.isEmpty()) {
                System.out.println("main");
                System.exit(0);
            }
            if (functionError) {
                System.out.println("funkcija");
                System.exit(0);
            }
            for (Map.Entry<String, TypeExpression> entry : functionDeclarations.entrySet()) {
                if (!entry.getValue().defined) {
                    System.out.println("funkcija");
                    System.exit(0);
                }
            }
            tables.removeFirst();
        }
    }

    private static TypeExpression primaryExpression(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "IDN":
                    String idn = child.elements.get(2);
                    for (Map<String, TypeExpression> table : tables) {
                        if (table.containsKey(idn)) {
                            return table.get(idn);
                        }
                    }
                    error(node);
                case "BROJ":
                    if (checkInt(child.elements.get(2)) == null) {
                        error(node);
                    }
                    return new TypeExpression(new FullType(new Type(false, PrimitiveType.INT)), false);
                case "ZNAK":
                    if (checkChar(child.elements.get(2)) == null) {
                        error(node);
                    }
                    return new TypeExpression(new FullType(new Type(false, PrimitiveType.CHAR)), false);
                case "NIZ_ZNAKOVA":
                    String arr;
                    if ((arr = checkConstCharArray(child.elements.get(2))) == null) {
                        error(node);
                    } else {
                        TypeExpression typeExpression = new TypeExpression(new FullType(new Type(true, PrimitiveType.CHAR), arr.length() + 1), false);
                        typeExpression.constCharArray = true;
                        return typeExpression;
                    }
                case "<izraz>":
                    return expression(child);
            }
        }
        throw new IllegalStateException();
    }

    private static TypeExpression postfixExpression(Node node) {
        TypeExpression typeExpression = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
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
                case "D_UGL_ZAGRADA":
                    if (!Objects.requireNonNull(typeExpression).fullType.array) {
                        error(node);
                    }
                    return new TypeExpression(new FullType(typeExpression.fullType.type), !typeExpression.fullType.type.constant);
                case "<lista_argumenata>":
                    if (Objects.requireNonNull(typeExpression).fullType.arguments == null) {
                        error(node);
                    }
                    List<FullType> arguments = argumentList(child);
                    if (typeExpression.fullType.arguments.size() != arguments.size()) {
                        error(node);
                    }
                    for (int i = 0; i < arguments.size(); i++) {
                        if (!checkImplicitCast(arguments.get(i), typeExpression.fullType.arguments.get(i))) {
                            error(node);
                        }
                    }
                    return new TypeExpression(new FullType(typeExpression.fullType.type), false);
                case "D_ZAGRADA":
                    if (Objects.requireNonNull(typeExpression).fullType.arguments == null) {
                        error(node);
                    }
                    if (!typeExpression.fullType.arguments.isEmpty()) {
                        error(node);
                    }
                    return new TypeExpression(new FullType(typeExpression.fullType.type), false);
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
                    arguments.add(Objects.requireNonNull(assignmentExpression(child)).fullType);
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
        switch (node.children.get(0).elements.get(0)) {
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

    private static TypeExpression assignmentExpression(Node node) {
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
                    TypeExpression equationalExpression = assignmentExpression(child);
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
                    TypeExpression typeExpression = assignmentExpression(child);
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

    private static void complexCommand(Node node, boolean newBlock, LinkedHashSet<Variable> parameters) {
        if (newBlock) {
            tables.addFirst(new HashMap<>());
            parameters.forEach(p -> {
                boolean lValue = !p.fullType.array && p.fullType.arguments == null && !p.fullType.type.constant;
                tables.getFirst().put(p.name, new TypeExpression(p.fullType, lValue));
            });
        }
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
        if (newBlock) {
            tables.removeFirst();
        }
    }

    private static void commandList(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<lista_naredbi>":
                    commandList(child);
                    break;
                case "<naredba>":
                    command(child, true);
                    break;
            }
        }
    }

    private static void command(Node node, boolean newBlock) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<slozena_naredba>":
                    complexCommand(child, newBlock, new LinkedHashSet<>());
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
                    if (!checkIntCast(expression(child).fullType)) {
                        error(node);
                    }
                    break;
                case "<naredba>":
                    tables.addFirst(new HashMap<>());
                    command(child, false);
                    tables.removeFirst();
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
                        tables.addFirst(new HashMap<>());
                    }
                    break;
                case "<naredba>":
                    boolean old = loop;
                    loop = true;
                    command(child, false);
                    loop = old;
                    break;
                case "<izraz_naredba>":
                    if (!seen) {
                        tables.addFirst(new HashMap<>());
                    }
                    FullType expressionCommand = expressionCommand(child);
                    if (seen && !checkIntCast(expressionCommand)) {
                        error(node);
                    }
                    seen = true;
                    break;
            }
        }
        tables.removeFirst();
    }

    private static void jumpCommand(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "KR_CONTINUE":
                case "KR_BREAK":
                    if (!loop) {
                        error(node);
                    }
                    return;
                case "TOCKAZAREZ":
                    if (currentFunction == null || currentFunction.type.primitiveType != PrimitiveType.VOID) {
                        error(node);
                    }
                    break;
                case "<izraz>":
                    TypeExpression expression = expression(child);
                    if (currentFunction == null || !checkImplicitCast(expression.fullType, new FullType(currentFunction.type))) {
                        error(node);
                    }
                    return;
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
            switch (child.elements.get(0)) {
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
        LinkedHashSet<Variable> parameters = new LinkedHashSet<>();
        FullType oldFunction = currentFunction;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<ime_tipa>":
                    returnType = typeName(child);
                    if (returnType.constant) {
                        error(node);
                    }
                    break;
                case "IDN":
                    functionName = child.elements.get(2);
                    break;
                case "KR_VOID":
                    TypeExpression function = tables.getFirst().get(functionName);
                    FullType definedFunction = new FullType(returnType, new ArrayList<>());
                    if (function != null && (function.defined || !definedFunction.equals(function.fullType))) {
                        error(node);
                    }
                    if (function == null) {
                        function = new TypeExpression(definedFunction, false);
                    }
                    function.defined = true;
                    currentFunction = function.fullType;
                    tables.getFirst().put(functionName, function);
                    functionDeclarations.put(functionName, function);
                    break;
                case "<lista_parametara>":
                    parameters = parameterList(child);
                    function = tables.getFirst().get(functionName);
                    definedFunction = new FullType(returnType, parameters.stream().map(Variable::getFullType).collect(Collectors.toList()));
                    if (function != null && (function.defined || !definedFunction.equals(function.fullType))) {
                        error(node);
                    }
                    if (function == null) {
                        function = new TypeExpression(definedFunction, false);
                    }
                    function.defined = true;
                    tables.getFirst().put(functionName, function);
                    currentFunction = function.fullType;
                    functionDeclarations.put(functionName, function);
                    break;
                case "<slozena_naredba>":
                    complexCommand(child, true, parameters);
                    currentFunction = oldFunction;
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
        FullType fullType = new FullType(type);
        fullType.array = array;
        return new Variable(name, fullType);
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
                        if (wrapper.types.size() > directDeclaratorType.brElements) {
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
        FullType fullType = new FullType(type);
        boolean lValue = !type.constant;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "IDN":
                    name = child.elements.get(2);
                    break;
                case "BROJ":
                    Integer brElements = checkInt(child.elements.get(2));
                    if (brElements == null) {
                        error(node);
                    } else {
                        if (brElements <= 0 || brElements > 1024) {
                            error(node);
                        }
                        fullType = new FullType(type, brElements);
                        lValue = false;
                    }
                    break;
                case "KR_VOID":
                    fullType = new FullType(type, new LinkedList<>());
                    TypeExpression functionExpression = tables.getFirst().get(name);
                    FullType function = functionExpression != null ? functionExpression.fullType : null;
                    if (function != null && !function.equals(fullType)) {
                        error(node);
                    }
                    if (!checkFunction(name, fullType)) {
                        functionError = true;
                    }
                    tables.getFirst().putIfAbsent(name, new TypeExpression(fullType, false));
                    functionDeclarations.putIfAbsent(name, new TypeExpression(fullType, false));
                    return fullType;
                case "<lista_parametara>":
                    fullType = new FullType(type, parameterList(child).stream().map(Variable::getFullType).collect(Collectors.toList()));
                    functionExpression = tables.getFirst().get(name);
                    function = functionExpression != null ? functionExpression.fullType : null;
                    if (function != null && !function.equals(fullType)) {
                        error(node);
                    }
                    if (!checkFunction(name, fullType)) {
                        functionError = true;
                    }
                    tables.getFirst().putIfAbsent(name, new TypeExpression(fullType, false));
                    functionDeclarations.putIfAbsent(name, new TypeExpression(fullType, false));
                    return fullType;
            }
        }
        if (type.primitiveType == PrimitiveType.VOID) {
            error(node);
        }
        if (tables.getFirst().put(name, new TypeExpression(fullType, lValue)) != null) {
            error(node);
        }
        return fullType;
    }

    private static boolean checkFunction(String name, FullType function) {
        TypeExpression te = functionDeclarations.get(name);
        return te == null || te.fullType.equals(function);
    }

    private static InitializerWrapper initializer(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<izraz_pridruzivanja>":
                    TypeExpression assignmentExpressionType = assignmentExpression(child);
                    if (assignmentExpressionType.constCharArray) {
                        List<FullType> charTypes = new LinkedList<>();
                        for (int i = 0; i < assignmentExpressionType.fullType.brElements; i++) {
                            charTypes.add(new FullType(new Type(false, PrimitiveType.CHAR)));
                        }
                        return new InitializerWrapper(new LinkedList<>(charTypes));
                    }
                    return new InitializerWrapper(assignmentExpressionType.fullType);
                case "<lista_izraza_pridruzivanja>":
                    return new InitializerWrapper(assignmentExpressionList(child));
            }
        }
        throw new IllegalStateException();
    }

    private static List<FullType> assignmentExpressionList(Node node) {
        List<FullType> types = new LinkedList<>();
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<izraz_pridruzivanja>":
                    types.add(assignmentExpression(child).fullType);
                    break;
                case "<lista_izraza_pridruzivanja>":
                    types.addAll(assignmentExpressionList(child));
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
        FullType fullType;
        boolean lExpression;
        // Tells whether function is defined
        boolean defined;
        boolean constCharArray;

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

    private static Integer checkInt(String i) {
        try {
            return Integer.decode(i);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Character checkChar(String c) {
        if (c.startsWith("\'")) {
            c = c.substring(1, c.length() - 1);
        }
        if (c.length() == 1 && c.charAt(0) != '\\') {
            return c.charAt(0);
        }
        switch (c) {
            case "\\t":
                return '\t';
            case "\\n":
                return '\n';
            case "\\0":
                return '\0';
            case "\\'":
                return '\'';
            case "\\\"":
                return '\"';
            case "\\\\":
                return '\\';
            default:
                return null;
        }
    }

    private static String checkConstCharArray(String arr) {
        arr = arr.substring(1, arr.length() - 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            if (arr.charAt(i) == '"') {
                return null;
            }
            if (arr.charAt(i) == '\\') {
                if (i >= arr.length() - 1) {
                    return null;
                }
                Character c = checkChar(arr.substring(i, i + 2));
                if (c == null) return null;
                i++;
                sb.append(c);
            } else {
                sb.append(arr.charAt(i));
            }
        }
        return sb.toString();
    }

    private static boolean checkIntCast(FullType fullType) {
        return !(fullType.array || fullType.arguments != null || fullType.type.primitiveType == PrimitiveType.VOID);
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
        return !from.array && !into.array && from.arguments == null && into.arguments == null &&
                (into.type.primitiveType == PrimitiveType.INT && from.type.primitiveType != PrimitiveType.VOID ||
                        from.type.primitiveType == PrimitiveType.CHAR && into.type.primitiveType == PrimitiveType.CHAR);
    }

    private static void error(Node node) {
        System.out.println(node.getString());
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
