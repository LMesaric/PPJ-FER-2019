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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return nonterminalSymbol + " -> " + Arrays.toString(right.toArray()).replace(",", "").replace("[", "")
                .replace("]", "");
    }

}
