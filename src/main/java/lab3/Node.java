package lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Node {

    final String text;
    final Node parent;
    final List<Node> children;

    Node(String text, Node parent) {
        this(text, parent, new ArrayList<>());
    }

    Node(String text, Node parent, List<Node> children) {
        this.text = Objects.requireNonNull(text);
        this.parent = parent;
        this.children = Objects.requireNonNull(children);
    }

    Node addChildFirst(Node child) {
        children.add(0, Objects.requireNonNull(child));
        return child;
    }

    Node addChildFirst(String text) {
        return addChildFirst(new Node(Objects.requireNonNull(text), this));
    }

    Node addChildLast(Node child) {
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
