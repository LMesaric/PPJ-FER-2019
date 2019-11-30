package lab1;

import java.util.LinkedList;
import java.util.List;

class TableGenerator {

    private int stateId;

    private final StringBuilder table = new StringBuilder();

    private void reset() {
        stateId = 0;
        table.setLength(0);
    }

    String buildTable(String expression) {
        reset();
        build(expression);
        return table.toString();
    }

    private Pair build(String expression) {
        List<String> Choices = getChoices(expression);
        int left = generateStateId();
        int right = generateStateId();

        // Determine whether given expression should be separated
        if (Choices.size() > 1) {
            for (String choice : Choices) {
                Pair tmp = build(choice);
                epsilonLink(left, tmp.left);
                epsilonLink(tmp.right, right);
            }
        } else {
            linkTogether(left, right, expression);
        }

        return new Pair(left, right);
    }

    private int generateStateId() {
        return stateId++;
    }

    private List<String> getChoices(String expression) {
        List<String> Choices = new LinkedList<>();
        StringBuilder Choice = new StringBuilder();
        int parenthesis = 0;
        for (int index = 0; index < expression.length(); index++) {
            char c = expression.charAt(index);
            if (c == '\\') {
                // Escape next character
                Choice.append(c);
                index++;
                c = expression.charAt(index);
            } else if (c == '(') {
                parenthesis++;
            } else if (c == ')') {
                parenthesis--;
            } else if (c == '|' && parenthesis == 0) {
                Choices.add(Choice.toString());
                Choice.setLength(0);
                continue;
            }
            Choice.append(c);
        }
        Choices.add(Choice.toString());
        return Choices;
    }

    private void epsilonLink(int left, int right) {
        table.append(String.format("%d %d\n", left, right));
    }

    private void charLink(int left, int right, char link) {
        table.append(String.format("%d %d %d\n", (int) link, left, right));
    }

    private void linkTogether(int left, int right, String expression) {
        int last = left;
        for (int index = 0; index < expression.length(); index++) {
            char c = expression.charAt(index);
            int a, b;
            if (c == '\\') {
                if (index >= expression.length() - 1) break;
                index++;
                char next = expression.charAt(index);
                char link;
                if (next == 't') {
                    link = '\t';
                } else if (next == 'n') {
                    link = '\n';
                } else if (next == 'r') {
                    link = '\r';
                } else if (next == '_') {
                    link = ' ';
                } else {
                    link = next;
                }
                a = generateStateId();
                b = generateStateId();
                charLink(a, b, link);
            } else if (c == '(') {
                int closingBracket = indexOfClosingBracket(expression, index + 1);
                if (closingBracket == -1) {
                    throw new IllegalStateException("Closing bracket could not be found! Index = " + index);
                }
                Pair tmp = build(expression.substring(index + 1, closingBracket));
                a = tmp.left;
                b = tmp.right;
                index = closingBracket;
            } else {
                a = generateStateId();
                b = generateStateId();
                if (c == Constants.EPSILON) {
                    epsilonLink(a, b);
                } else {
                    charLink(a, b, c);
                }
            }
            if (index + 1 < expression.length() && expression.charAt(index + 1) == '*') {
                index++;
                epsilonLink(a, b);
                epsilonLink(b, a);
            }
            epsilonLink(last, a);
            last = b;
        }
        epsilonLink(last, right);
    }

    private int indexOfClosingBracket(String expression, int index) {
        int closingBracket = index;
        int brackets = 0;
        while (expression.charAt(closingBracket) != ')' || brackets != 0) {
            if (expression.charAt(closingBracket) == '(') brackets++;
            if (expression.charAt(closingBracket) == ')') brackets--;
            // Ignore escaped character
            if (expression.charAt(closingBracket) == '\\') closingBracket++;
            if (++closingBracket >= expression.length()) return -1;
        }
        return closingBracket;
    }

    private static class Pair {

        final int left;
        final int right;

        Pair(int left, int right) {
            this.left = left;
            this.right = right;
        }

    }

}
