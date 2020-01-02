package lab3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("Duplicates")
class Node {

    private final Node parent;
    final List<String> elements;
    final List<Node> children;

    Node(String text, Node parent) {
        this(text, parent, new ArrayList<>());
    }

    private Node(String text, Node parent, List<Node> children) {
        this.elements = Arrays.asList(Objects.requireNonNull(text).trim().split(" ", 3));
        this.parent = parent;
        this.children = Objects.requireNonNull(children);
    }

    private Node addChildFirst(Node child) {
        children.add(0, Objects.requireNonNull(child));
        return child;
    }

    Node addChildFirst(String text) {
        return addChildFirst(new Node(Objects.requireNonNull(text), this));
    }

    private Node addChildLast(Node child) {
        children.add(Objects.requireNonNull(child));
        return child;
    }

    Node addChildLast(String text) {
        return addChildLast(new Node(Objects.requireNonNull(text), this));
    }

    Node getDistantParent(int numLevelsAbove) {
        Node ref = this;
        for (int i = 0; i < numLevelsAbove; i++)
            ref = ref.parent;
        return ref;
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();
        elements.forEach(e -> sb.append(e).append(" "));
        sb.append("::= ");
        children.forEach(e -> {
            if (e.elements.size() == 3) {
                sb.append(e.elements.get(0)).append("(").append(e.elements.get(1)).append(",").append(e.elements.get(2)).append(") ");
            } else {
                sb.append(e.elements.get(0)).append(" ");
            }
        });
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        elements.forEach(e -> sb.append(e).append(" "));
        return sb.toString().trim();
    }
}
