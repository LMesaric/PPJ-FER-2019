package lab2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class Production {

    private final String nonterminalSymbol;

    private final List<String> right;

    Production(String nonterminalSymbol, List<String> right) {
        this.nonterminalSymbol = nonterminalSymbol;
        this.right = new LinkedList<>(right);
    }

    String getNonterminalSymbol() {
        return nonterminalSymbol;
    }

    List<String> getRight() {
        return right;
    }

    @Override
    public String toString() {
        return nonterminalSymbol + " -> " + Arrays.toString(right.toArray()).replace(",", "").replace("[", "")
                .replace("]", "");
    }

}
