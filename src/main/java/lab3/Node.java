package lab3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class Node {

    final List<String> elements;
    final Node parent;
    final List<Node> children;

    Node(String text, Node parent) {
        this(text, parent, new ArrayList<>());
    }

    private Node(String text, Node parent, List<Node> children) {
        this.elements = Arrays.asList(Objects.requireNonNull(text).split(" "));
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

}
