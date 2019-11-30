package lab2.analizator;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

class Node {

    final String text;
    final List<Node> children;

    Node(String text) {
        this(text, new LinkedList<>());
    }

    Node(String text, List<Node> children) {
        this.text = Objects.requireNonNull(text);
        this.children = Objects.requireNonNull(children);
    }

    void addChildFirst(Node child) {
        children.add(0, Objects.requireNonNull(child));
    }

}
