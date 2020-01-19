package lab4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "Duplicates"})
public class GeneratorKoda {

    private static FullType currentFunction;

    private static boolean functionError;

    private static final Deque<Map<String, TypeExpression>> tables = new ArrayDeque<>();

    private static final Map<String, TypeExpression> functionDeclarations = new HashMap<>();

    private static final Map<String, FunctionContext> functionImplementations = new HashMap<>();

    private static final Set<String> allLabels = new HashSet<>();

    private static final StringBuilder completeOutput = new StringBuilder();

    private static final Map<String, Integer> constants = new HashMap<>();

    private static FunctionContext initGlobals;

    private static FunctionContext currentFunc = null;

    private static Map<String, String> globalVariableLabels = new HashMap<>();



    public static void main(String[] args) {
        String inputText = new String(readAllFromStdin(), StandardCharsets.UTF_8);
        Node root = buildTree(inputText.split("\r?\n"));
        if (root != null) {
            BuilderUtil.appendLine(completeOutput, "MOVE 40000, R7");
            BuilderUtil.appendLine(completeOutput, "CALL F_INIT_GLOBALS");
            BuilderUtil.appendLine(completeOutput, "CALL F_MAIN");
            BuilderUtil.appendLine(completeOutput, "HALT");
            allLabels.add("F_MAIN");

            FunctionContext mul = MathUtil.generateMultiplicationImplementation();
            mul.functionLabel = generateRandomLabel();
            functionImplementations.put(mul.functionName, mul);

            FunctionContext div = MathUtil.generateDivisionImplementation();
            div.functionLabel = generateRandomLabel();
            functionImplementations.put(div.functionName, div);

            FunctionContext mod = MathUtil.generateModuloImplementation();
            mod.functionLabel = generateRandomLabel();
            functionImplementations.put(mod.functionName, mod);

            initGlobals = new FunctionContext();
            initGlobals.functionName = "INIT_GLOBALS";
            initGlobals.functionLabel = generateRandomLabel();
            functionImplementations.put(initGlobals.functionName, initGlobals);
            initGlobals.addCommand("MOVE R0, R0", "F_INIT_GLOBALS");

            tables.addFirst(new HashMap<>());
            compileUnit(root);

            initGlobals.addCommand("RET");

            for (Map.Entry<String, FunctionContext> implementation : functionImplementations.entrySet()) {
                List<String> commands = implementation.getValue().getCommands();
                if (commands.isEmpty()) continue;
                //String functionLabel = "F_" + implementation.getKey().toUpperCase();
                for (int i = 0; i < commands.size(); i++) {
                    completeOutput.append(commands.get(i)).append("\n");
                }
                completeOutput.append("\n");
            }

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
            OutputUtil.writeToFileOutput(concatenateGeneratedOutputs());
        }
    }

    private static String createNewConstant(int value) {
        String label = generateRandomLabel();
        constants.put(label, value);
        return label;
    }

    private static String generateRandomLabel() {
        while (true) {
            String generatedString = new Random().ints('A', 'Z' + 1)
                    .limit(10)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

            if (allLabels.add(generatedString))
                return generatedString;
        }
    }

    private static String concatenateGeneratedOutputs() {
        return completeOutput.append("\n")
                .append(generateConstantsCode())
                .toString();
    }

    private static String generateConstantsCode() {
        StringBuilder consts = new StringBuilder();
        BuilderUtil.appendLine(consts, "");
        BuilderUtil.appendLine(consts, "; Constants");
        for (Map.Entry<String, Integer> c : constants.entrySet()) {
            // TODO: Update if needed to distinguish DW from DB
            BuilderUtil.appendLine(consts, String.format("DW %%D %s", c.getValue()), c.getKey());
        }
        return consts.toString();
    }

    private static Node findParentWithName(Node node, String name) {
        Node tmp = node.parent;
        while (!tmp.elem(0).equals(name)) tmp = tmp.parent;
        return tmp;
    }

    private static void appendCode(String code, String label) {
        if (currentFunc != null) currentFunc.addCommand(code, label);
        else initGlobals.addCommand(code, label);
    }

    private static void appendCode(String code) {
        appendCode(code, null);
    }

    private static void variableLocationToR4(String variableName) {
        Variable var = currentFunc == null ? null : currentFunc.findVariable(variableName);
        if (var != null) {
            int offset = var.addressingOffset;
            appendCode((offset >= 0 ? "ADD" : "SUB") + " R5, %D " + Math.abs(offset) + ", R4");
        } else {
            String globalLabel = globalVariableLabels.get(variableName);
            appendCode("MOVE " + globalLabel + ", R4");
        }
    }

    private static String variableToR0(String variableName) {
        Variable var = currentFunc == null ? null : currentFunc.findVariable(variableName);
        if (var != null) {
            int offset = var.addressingOffset;
            return "LOAD R0, (R5" + (offset >= 0 ? "+" : "-") + " %D " + Math.abs(offset) + ")";
        } else {
            String globalLabel = globalVariableLabels.get(variableName);
            return "LOAD R0, (" + globalLabel + ")";
        }
    }

    private static String r0ToVariable(String variableName) {
        Variable var = currentFunc == null ? null : currentFunc.findVariable(variableName);
        if (var != null) {
            int offset = var.addressingOffset;
            return "STORE R0, (R5" + (offset >= 0 ? "+" : "-") + " %D " + Math.abs(offset) + ")";
        } else {
            String globalLabel = globalVariableLabels.get(variableName);
            return "STORE R0, (" + globalLabel + ")";
        }
    }

    private static TypeExpression primaryExpression(Node node) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "IDN":
                    String idn = child.elements.get(2);
                    TypeExpression ret = null;
                    for (Map<String, TypeExpression> table : tables) {
                        if (table.containsKey(idn)) {
                            table.get(idn).idnName = idn;
                            ret = table.get(idn);
                        }
                    }
                    if (ret == null) error(node);
                    if (ret.fullType.isVariable()) {
                        variableLocationToR4(idn);
                        appendCode(variableToR0(idn));
                        appendCode("PUSH R0");
                    }
                    return ret;
                case "BROJ":
                    if (checkInt(child.elements.get(2)) == null) {
                        error(node);
                    }
                    String label = createNewConstant(checkInt(child.elements.get(2)));
                    appendCode("LOAD R0, (" + label + ")");
                    appendCode("PUSH R0");
                    return new TypeExpression(new FullType(new Type(false, PrimitiveType.INT)), false);
                case "ZNAK":
                    if (checkChar(child.elements.get(2)) == null) {
                        error(node);
                    }
                    appendCode("MOVE %D " + ((int) checkChar(child.elements.get(2))) + ", R0");
                    appendCode("PUSH R0");
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
        List<FullType> arguments = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<primarni_izraz>":
                    return primaryExpression(child);
                case "<postfiks_izraz>":
                    typeExpression = postfixExpression(child);
                    break;
                case "<izraz>":
                    appendCode("POP R0");
                    if (!checkIntCast(expression(child).fullType)) {
                        error(node);
                    }
                    break;
                case "L_ZAGRADA":
                    appendCode("PUSH R5");
                    break;
                case "D_UGL_ZAGRADA":
                    if (!Objects.requireNonNull(typeExpression).fullType.array) {
                        error(node);
                    }

                    appendCode("POP R0");
                    appendCode("SUB R4, R0, R4");
                    appendCode("LOAD R0, (R4)");
                    appendCode("PUSH R0");

                    return new TypeExpression(new FullType(typeExpression.fullType.type), !typeExpression.fullType.type.constant);
                case "<lista_argumenata>":
                    if (Objects.requireNonNull(typeExpression).fullType.arguments == null) {
                        error(node);
                    }
                    arguments = argumentList(child);
                    if (typeExpression.fullType.arguments.size() != arguments.size()) {
                        error(node);
                    }
                    for (int i = 0; i < arguments.size(); i++) {
                        if (!checkImplicitCast(arguments.get(i), typeExpression.fullType.arguments.get(i))) {
                            error(node);
                        }
                    }
                    callFunction(node, arguments);
                    return new TypeExpression(new FullType(typeExpression.fullType.type), false);
                case "D_ZAGRADA":
                    if (Objects.requireNonNull(typeExpression).fullType.arguments == null) {
                        error(node);
                    }
                    if (!typeExpression.fullType.arguments.isEmpty()) {
                        error(node);
                    }
                    callFunction(node, arguments);
                    return new TypeExpression(new FullType(typeExpression.fullType.type), false);
                case "OP_INC":
                case "OP_DEC":
                    if (!Objects.requireNonNull(typeExpression).lExpression || !checkIntCast(typeExpression.fullType)) {
                        error(node);
                    }
                    // TODO: implement for arrays
                    String idn = typeExpression.idnName;
                    appendCode(variableToR0(idn));
                    if (child.elements.get(0).equals("OP_INC")) appendCode("ADD R0, 1, R0");
                    else if (child.elements.get(0).equals("OP_DEC")) appendCode("SUB R0, 1, R0");
                    if (typeExpression.fullType.type.primitiveType == PrimitiveType.CHAR) {
                        appendCode("AND R0, 0FF, R0");
                    }
                    appendCode(r0ToVariable(idn));
                    return new TypeExpression(new FullType(new Type(false, PrimitiveType.INT)), false);
            }
        }
        return typeExpression;
    }

    private static void callFunction(Node node, List<FullType> arguments) {
        String functionName = node.children.get(0).children.get(0).children.get(0).elements.get(2);
        String label = "F_" + functionImplementations.get(functionName).functionName.toUpperCase();
        appendCode("CALL " + label);
        int argumentsSize = (arguments == null) ? 0 : arguments.size();
        appendCode("ADD SP, %D " + 4*argumentsSize + ", SP");
        appendCode("POP R5");
        appendCode("PUSH R6");
    }

    private static List<FullType> argumentList(Node node) {
        List<FullType> arguments = new LinkedList<>();
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<izraz_pridruzivanja>":
                    arguments.add(Objects.requireNonNull(assignmentExpression(child)).fullType);
                    // Append nothing since result is already on stack
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

                    appendCode("POP R0");
                    String idn = typeExpression.idnName;
                    appendCode(variableToR0(idn));
                    if (node.children.get(0).elements.get(0).equals("OP_INC")) appendCode("ADD R0, 1, R0");
                    else if (node.children.get(0).elements.get(0).equals("OP_DEC")) appendCode("SUB R0, 1, R0");
                    if (typeExpression.fullType.type.primitiveType == PrimitiveType.CHAR) {
                        appendCode("AND R0, 0FF, R0");
                    }
                    appendCode(r0ToVariable(idn));
                    appendCode("PUSH R0");

                    return new TypeExpression(new FullType(new Type(false, PrimitiveType.INT)), false);
                case "<cast_izraz>":
                    TypeExpression typeExp = castExpression(child);
                    if (!checkIntCast(typeExp.fullType)) {
                        error(node);
                    }

                    String operator = node.children.get(0).children.get(0).elements.get(0);
                    switch (operator) {
                        case "PLUS":
                            break;
                        case "MINUS":
                            appendCode("POP R0");
                            appendCode("XOR R0, -1, R0");
                            appendCode("ADD R0, 1, R0");
                            appendCode("PUSH R0");
                            break;
                        case "OP_TILDA":
                            appendCode("POP R0");
                            appendCode("XOR R0, -1, R0");
                            appendCode("PUSH R0");
                            break;
                        case "OP_NEG":
                            String label = generateRandomLabel();
                            appendCode("POP R0");
                            appendCode("MOVE 1, R1");
                            appendCode("CMP R0, 0");
                            appendCode("JR_EQ " + label);
                            appendCode("MOVE 0, R1");
                            appendCode("PUSH R1", label);
                            appendCode("MOVE R1, R0");
                            break;
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

                    if (typeExpression.fullType.type.primitiveType == PrimitiveType.CHAR) {
                        appendCode("POP R0");
                        appendCode("AND R0, 0FF, R0");
                        appendCode("PUSH R0");
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
        boolean operation = node.children.size() == 3;
        boolean notEvaluatingRight = operation && Arrays.asList("OP_I", "OP_ILI").contains(node.child(1).elem(0));

        for (Node child : node.children) {
            if (child.elements.get(0).equals(firstCase)) {
                TypeExpression typeExpression = firstFunction.apply(child);

                boolean codeAdded = false;
                if (operation) {
                    if (Arrays.asList("OP_PUTA", "OP_DIJELI", "OP_MOD").contains(node.child(1).elem(0))) {
                        codeAdded = true;
                        switch (node.child(1).elem(0)) {
                            case "OP_PUTA":
                                appendCode("CALL O_MUL");
                                break;
                            case "OP_DIJELI":
                                appendCode("CALL O_DIV");
                                break;
                            case "OP_MOD":
                                appendCode("CALL O_MOD");
                                break;
                        }
                        appendCode("ADD SP, %D 8, SP");
                        appendCode("MOVE R6, R0");
                    }
                    if (!codeAdded) {
                        appendCode("POP R1");
                        appendCode("POP R0");
                        switch (node.children.get(1).elements.get(0)) {
                            case "PLUS":
                                appendCode("ADD R0, R1, R0");
                                codeAdded = true;
                                break;
                            case "MINUS":
                                appendCode("SUB R0, R1, R0");
                                codeAdded = true;
                                break;
                            case "OP_BIN_I":
                                appendCode("AND R0, R1, R0");
                                codeAdded = true;
                                break;
                            case "OP_BIN_XILI":
                                appendCode("XOR R0, R1, R0");
                                codeAdded = true;
                                break;
                            case "OP_BIN_ILI":
                                appendCode("OR R0, R1, R0");
                                codeAdded = true;
                                break;
                        }
                    }
                    if (!codeAdded) {
                        String randomLabel = generateRandomLabel();
                        appendCode("MOVE 1, R2");
                        appendCode("CMP R0, R1");
                        switch (node.child(1).elem(0)) {
                            case "OP_EQ":
                                appendCode("JR_EQ " + randomLabel);
                                break;
                            case "OP_NEQ":
                                appendCode("JR_NEQ " + randomLabel);
                                break;
                            case "OP_LT":
                                appendCode("JR_SLT " + randomLabel);
                                break;
                            case "OP_LTE":
                                appendCode("JR_SLE " + randomLabel);
                                break;
                            case "OP_GT":
                                appendCode("JR_SGT " + randomLabel);
                                break;
                            case "OP_GTE":
                                appendCode("JR_SGE " + randomLabel);
                                break;
                            case "OP_I":
                                appendCode("AND R0, R1, R0");
                                appendCode("JR_NZ " + randomLabel);
                                break;
                            case "OP_ILI":
                                appendCode("OR R0, R1, R0");
                                appendCode("JR_NZ " + randomLabel);
                                break;
                        }
                        appendCode("MOVE 0, R2");
                        appendCode("MOVE R2, R0", randomLabel);
                    }
                    appendCode("PUSH R0");
                }

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

                if (operation && notEvaluatingRight) {
                    Node tmp = findParentWithName(node, "<izraz_pridruzivanja>");
                    tmp.pendingSkipEvaluationLabel = generateRandomLabel();
                    appendCode("CMP R0, 0");
                    String jumpOp = node.child(1).elem(0).equals("OP_I") ? "Z" : "NZ";
                    appendCode("JR_" + jumpOp + " " + tmp.pendingSkipEvaluationLabel);
                }
            }
        }
        throw new IllegalStateException();
    }

    private static TypeExpression multiplicativeExpression(Node node) {
        return simpleExpression(node, "<cast_izraz>", "<multiplikativni_izraz>", GeneratorKoda::castExpression, GeneratorKoda::multiplicativeExpression);
    }

    private static TypeExpression additiveExpression(Node node) {
        return simpleExpression(node, "<multiplikativni_izraz>", "<aditivni_izraz>", GeneratorKoda::multiplicativeExpression, GeneratorKoda::additiveExpression);
    }

    private static TypeExpression relationalExpression(Node node) {
        return simpleExpression(node, "<aditivni_izraz>", "<odnosni_izraz>", GeneratorKoda::additiveExpression, GeneratorKoda::relationalExpression);
    }

    private static TypeExpression equationalExpression(Node node) {
        return simpleExpression(node, "<odnosni_izraz>", "<jednakosni_izraz>", GeneratorKoda::relationalExpression, GeneratorKoda::equationalExpression);
    }

    private static TypeExpression binaryAndExpression(Node node) {
        return simpleExpression(node, "<jednakosni_izraz>", "<bin_i_izraz>", GeneratorKoda::equationalExpression, GeneratorKoda::binaryAndExpression);
    }

    private static TypeExpression binaryXorExpression(Node node) {
        return simpleExpression(node, "<bin_i_izraz>", "<bin_xili_izraz>", GeneratorKoda::binaryAndExpression, GeneratorKoda::binaryXorExpression);
    }

    private static TypeExpression binaryOrExpression(Node node) {
        return simpleExpression(node, "<bin_xili_izraz>", "<bin_ili_izraz>", GeneratorKoda::binaryXorExpression, GeneratorKoda::binaryOrExpression);
    }

    private static TypeExpression logAndExpression(Node node) {
        return simpleExpression(node, "<bin_ili_izraz>", "<log_i_izraz>", GeneratorKoda::binaryOrExpression, GeneratorKoda::logAndExpression);
    }

    private static TypeExpression logOrExpression(Node node) {
        return simpleExpression(node, "<log_i_izraz>", "<log_ili_izraz>", GeneratorKoda::logAndExpression, GeneratorKoda::logOrExpression);
    }

    private static TypeExpression assignmentExpression(Node node) {
        TypeExpression postfixExpression = null;
        TypeExpression typeExpression = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<log_ili_izraz>":
                    TypeExpression ret = logOrExpression(child);
                    if (node.pendingSkipEvaluationLabel != null) appendCode("MOVE R0, R0", node.pendingSkipEvaluationLabel);
                    node.pendingSkipEvaluationLabel = null;
                    return ret;
                case "<postfiks_izraz>":
                    postfixExpression = postfixExpression(child);
                    if (!postfixExpression.lExpression) {
                        error(node);
                    }
                    appendCode("POP R0");
                    typeExpression = new TypeExpression(postfixExpression.fullType, false);
                    break;
                case "<izraz_pridruzivanja>":
                    TypeExpression equationalExpression = assignmentExpression(child);
                    if (!checkImplicitCast(equationalExpression.fullType, Objects.requireNonNull(typeExpression).fullType)) {
                        error(node);
                    }

                    appendCode("POP R0");
                    String idn = postfixExpression.idnName;
                    appendCode(r0ToVariable(idn));
                    appendCode("PUSH R0");

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

    private static void complexCommand(Node node, boolean newBlock, LinkedHashSet<Variable> parameters, FunctionContext implementation) {
        if (newBlock) {
            tables.addFirst(new HashMap<>());
            parameters.forEach(p -> {
                boolean lValue = !p.fullType.array && p.fullType.arguments == null && !p.fullType.type.constant;
                tables.getFirst().put(p.name, new TypeExpression(p.fullType, lValue));
            });
        }
        currentFunc.putNewScope();

        if (currentFunc.lastLoop() != null) currentFunc.lastLoop().hasComplexCommand = true;

        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<lista_deklaracija>":
                    declarationList(child);
                    break;
                case "<lista_naredbi>":
                    commandList(child, implementation);
                    break;
            }
        }
        if (newBlock) {
            tables.removeFirst();
        }
        appendCode("ADD SP, %D " + currentFunc.getLastScopeMemSize() + ", SP");
        currentFunc.removeLastScope();
    }

    private static void commandList(Node node, FunctionContext implementation) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<lista_naredbi>":
                    commandList(child, implementation);
                    break;
                case "<naredba>":
                    command(child, true, implementation);
                    break;
            }
        }
    }

    private static void command(Node node, boolean newBlock, FunctionContext implementation) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<slozena_naredba>":
                    complexCommand(child, newBlock, new LinkedHashSet<>(), implementation);
                    break;
                case "<izraz_naredba>":
                    expressionCommand(child, implementation);
                    break;
                case "<naredba_grananja>":
                    branchCommand(child, implementation);
                    break;
                case "<naredba_petlje>":
                    loopCommand(child, implementation);
                    break;
                case "<naredba_skoka>":
                    jumpCommand(child, implementation);
                    break;
            }
        }
    }

    private static FullType expressionCommand(Node node, FunctionContext implementation) {
        for (Node child : node.children) {
            if ("<izraz>".equals(child.elements.get(0))) {
                return expression(child).fullType;
            }
        }
        return new FullType(new Type(false, PrimitiveType.INT));
    }

    private static void branchCommand(Node node, FunctionContext implementation) {
        boolean hasElse = node.children.size() == 7;
        String labelAfterIf = generateRandomLabel();
        String labelAfterElse = null;

        Node exprChild = node.child(2);
        if (!checkIntCast(expression(exprChild).fullType)) error(node);

        appendCode("POP R0");
        appendCode("CMP R0, 0");
        appendCode("JR_Z " + labelAfterIf);

        Node commandNode1 = node.child(4);
        tables.addFirst(new HashMap<>());
        command(commandNode1, false, implementation);
        tables.removeFirst();

        if (hasElse) {
            labelAfterElse = generateRandomLabel();
            appendCode("JR " + labelAfterElse);
            appendCode("MOVE R0, R0", labelAfterIf);

            Node commandNode2 = node.child(6);
            tables.addFirst(new HashMap<>());
            command(commandNode2, false, implementation);
            tables.removeFirst();

            appendCode("MOVE R0, R0", labelAfterElse);
        } else {
            appendCode("MOVE R0, R0", labelAfterIf);
        }
    }

    private static void loopCommand(Node node, FunctionContext implementation) {
        boolean isFor = node.child(0).elem(0).equals("KR_FOR");
        boolean isCompleteFor = node.children.size() == 7;
        String conditionCheckLabel = generateRandomLabel();
        String afterContinueLabel = generateRandomLabel();
        String afterLoopLabel = generateRandomLabel();

        currentFunc.createLoop();
        currentFunc.lastLoop().conditionCheckLabel = conditionCheckLabel;
        currentFunc.lastLoop().afterContinueLabel = afterContinueLabel;
        currentFunc.lastLoop().afterLoopLabel = afterLoopLabel;

        // FOR loop init
        if (isFor) {
            tables.addFirst(new HashMap<>());
            expressionCommand(node.child(2), implementation);
        }

        // Condition check
        appendCode("MOVE R0, R0", conditionCheckLabel);
        if (isFor) {
            FullType expressionCommand = expressionCommand(node.child(3), implementation);
            if (!checkIntCast(expressionCommand)) {
                error(node);
            }
        } else {
            TypeExpression expression = expression(node.child(2));
            checkIntCast(expression.fullType);
            tables.addFirst(new HashMap<>());
        }
        appendCode("POP R0");
        appendCode("CMP R0, 0");
        appendCode("JR_Z " + afterLoopLabel);

        // Actual code
        command(node.child(node.children.size() - 1), false, implementation);

        // FOR loop increment part
        appendCode("MOVE R0, R0", afterContinueLabel);
        if (isCompleteFor) {
            expression(node.child(4));
        }
        appendCode("JR " + conditionCheckLabel);

        // loop end
        appendCode("MOVE R0, R0", afterLoopLabel);
        currentFunc.exitLoop();

        tables.removeFirst();
    }

    private static void jumpCommand(Node node, FunctionContext implementation) {
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "KR_CONTINUE":
                    if (currentFunc.lastLoop() == null) {
                        error(node);
                    }
                    // TODO: check if memory leak is solved
                    if (currentFunc.lastLoop().hasComplexCommand) {
                        appendCode("ADD SP, %D " + currentFunc.getLastScopeMemSize() + ", SP");
                    }
                    appendCode("JR " + currentFunc.lastLoop().afterContinueLabel);
                    return;
                case "KR_BREAK":
                    if (currentFunc.lastLoop() == null) {
                        error(node);
                    }
                    if (currentFunc.lastLoop().hasComplexCommand) {
                        appendCode("ADD SP, %D " + currentFunc.getLastScopeMemSize() + ", SP");
                    }
                    appendCode("JR " + currentFunc.lastLoop().afterLoopLabel);
                    return;
                case "TOCKAZAREZ":
                    if (currentFunction == null || currentFunction.type.primitiveType != PrimitiveType.VOID) {
                        error(node);
                    }

                    appendCode("MOVE R5, SP");
                    appendCode("RET");

                    break;
                case "<izraz>":
                    TypeExpression expression = expression(child);
                    if (currentFunction == null || !checkImplicitCast(expression.fullType, new FullType(currentFunction.type))) {
                        error(node);
                    }

                    appendCode("POP R6");
                    appendCode("MOVE R5, SP");
                    appendCode("RET");

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

        currentFunc = new FunctionContext();
        currentFunc.functionLabel = generateRandomLabel();
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
                    currentFunc.functionName = functionName;
                    functionImplementations.put(functionName, currentFunc);
                    appendCode("MOVE SP, R5", "F_" + functionName.toUpperCase());
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
                    currentFunc.setParameters(parameters);
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
                    complexCommand(child, true, parameters, currentFunc);
                    currentFunction = oldFunction;
                    break;
            }
        }
        currentFunc = null;
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
        String idn = null;
        for (Node child : node.children) {
            switch (child.elements.get(0)) {
                case "<izravni_deklarator>":
                    Variable var = directDeclarator(child, type);
                    directDeclaratorType = var.fullType;
                    idn = var.name;

                    if (currentFunc != null) appendCode("SUB SP, 4, SP");

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

                    appendCode("POP R0");
                    appendCode(r0ToVariable(idn));

                    return;
            }
        }
        if (Objects.requireNonNull(directDeclaratorType).type.constant) {
            error(node);
        }
    }

    private static Variable directDeclarator(Node node, Type type) {
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
                    return new Variable(name, fullType);
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
                    return new Variable(name, fullType);
            }
        }
        if (type.primitiveType == PrimitiveType.VOID) {
            error(node);
        }
        if (tables.getFirst().put(name, new TypeExpression(fullType, lValue)) != null) {
            error(node);
        }
        Variable var = new Variable(name, fullType);
        if (node.children.size() == 1) {
            if (currentFunc != null) currentFunc.putNewVariable(var);
            else globalVariableLabels.put(name, createNewConstant(0));
        }
        return new Variable(name, fullType);
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







    private static class TypeExpression {
        FullType fullType;
        boolean lExpression;
        // Tells whether function is defined
        boolean defined;
        boolean constCharArray;

        String idnName;

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

    private static int countLeadingSpaces(String s) {
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
