package lab2.analizator;

import lab2.Accept;
import lab2.Move;
import lab2.Production;
import lab2.Put;
import lab2.Reduce;

import java.util.*;
import java.util.function.Consumer;

class LR {

    private final ArrayList<LexicalToken> inputTokens;  // force get() in O(1)
    private final Map<Integer, Map<String, Object>> actionTable;
    private final Map<Integer, Map<String, Put>> newStateTable;
    private final Set<String> synchronizationalSymbols;

    private int currentTokenIndex = 0;
    private final int INDEX_OF_LINE_END;
    private final Deque<StackElement> stack = new ArrayDeque<>();

    private final Consumer<String> errorConsumer = System.err::println;

    LR(ArrayList<LexicalToken> inputTokens,
       Map<Integer, Map<String, Object>> actionTable,
       Map<Integer, Map<String, Put>> newStateTable,
       Set<String> synchronizationalSymbols) {
        this.inputTokens = inputTokens;
        this.actionTable = actionTable;
        this.newStateTable = newStateTable;
        this.synchronizationalSymbols = synchronizationalSymbols;
        this.INDEX_OF_LINE_END = inputTokens.size();
    }

    Node parse() {
        stack.push(new StackElement(Constants.STACK_END, 0));
        while (true) {
            if (parseOneIteration()) break;
        }
        return stack.getFirst().node;
    }

    private boolean parseOneIteration() {
        Object o = getFromActionTable(stack.getFirst().state, getCurrentInputSymbol());
        if (o == null) {
            return recoverFromError();
        }

        if (o instanceof Move) {
            Move m = (Move) o;
            stack.push(new StackElement(Objects.requireNonNull(getCurrentInputToken()), m.newState));
            currentTokenIndex++;
            return false;
        }

        if (o instanceof Reduce) {
            Production p = ((Reduce) o).production;
            List<String> right = p.getRight();

            // State is configured later.
            StackElement newElement = new StackElement(p.getNonterminalSymbol());

            if (right.size() == 0) {
                newElement.node.addChildFirst(new Node(Constants.EPSILON));
            } else {
                ListIterator<String> rightSideIter = right.listIterator(right.size());
                while (rightSideIter.hasPrevious()) {
                    String rightSideSymbol = rightSideIter.previous();
                    StackElement top = stack.pop();
                    if (!top.symbol.equals(rightSideSymbol)) {
                        throw new RuntimeException(
                                String.format("Inconsistent stack content! Production: %s\n  Expected: %s  Found: %s",
                                        p.toString(), rightSideSymbol, top.symbol));
                    }
                    newElement.node.addChildFirst(top.node);
                }
            }

            int oldState = stack.getFirst().state;
            Put put = getFromNewStateTable(oldState, p.getNonterminalSymbol());
            if (put == null) {
                throw new RuntimeException("Next state does not exist: "
                        + oldState + " " + p.getNonterminalSymbol());
            }
            newElement.state = put.newState;
            stack.push(newElement);
            return false;
        }

        return o instanceof Accept;
    }

    private boolean recoverFromError() {
        LexicalToken errorToken = getCurrentInputToken();
        errorConsumer.accept("Error detected!");
        if (errorToken == null) {
            int pos = inputTokens.size() - 1;
            int lineNum = pos < 0 ? 0 : inputTokens.get(pos).lineNumber;
            errorToken = new LexicalToken("EOF", lineNum, "EOF");
        }
        errorConsumer.accept("Line: " + errorToken.lineNumber);
        errorConsumer.accept("Acceptable symbols: "
                + String.join(", ", actionTable.get(stack.getFirst().state).keySet()));
        errorConsumer.accept("Received: " + errorToken.uniformSign);
        errorConsumer.accept("Original code: " + errorToken.originalCode);
        errorConsumer.accept("");

        for (; currentTokenIndex < INDEX_OF_LINE_END; currentTokenIndex++)
            if (synchronizationalSymbols.contains(getCurrentInputSymbol()))
                break;

        while (stack.size() > 1
                && null == getFromActionTable(stack.getFirst().state, getCurrentInputSymbol())) {
            stack.pop();
        }

        return stack.size() <= 0;
    }

    private LexicalToken getCurrentInputToken() {
        if (currentTokenIndex > INDEX_OF_LINE_END)
            throw new IllegalStateException("Index is too large: " + currentTokenIndex);
        else if (currentTokenIndex == INDEX_OF_LINE_END)
            return null;
        else
            return inputTokens.get(currentTokenIndex);
    }

    private String getCurrentInputSymbol() {
        LexicalToken token = getCurrentInputToken();
        if (token == null) return Constants.END;
        else return token.uniformSign;
    }

    private static <T> T getFromTable(Map<Integer, Map<String, T>> map, Integer state, String symbol) {
        Map<String, T> row = map.get(state);
        if (row == null) return null;
        return row.get(symbol);
    }

    private Object getFromActionTable(Integer state, String symbol) {
        return getFromTable(actionTable, state, symbol);
    }

    private Put getFromNewStateTable(Integer state, String symbol) {
        return getFromTable(newStateTable, state, symbol);
    }

}
