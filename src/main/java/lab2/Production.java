package lab2;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

// Two exact copies of Production class exist purely because of the way online evaluator is set up.
public class Production implements Serializable {

    private static final long serialVersionUID = 8637858053063664899L;

    private final String nonterminalSymbol;
    private final List<String> right;

    public Production(String nonterminalSymbol, List<String> right) {
        this.nonterminalSymbol = nonterminalSymbol;
        this.right = new LinkedList<>(right);
    }

    public String getNonterminalSymbol() {
        return nonterminalSymbol;
    }

    public List<String> getRight() {
        return right;
    }

    @Override
    public String toString() {
        return nonterminalSymbol + " -> " + String.join(" ", right);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Production that = (Production) o;
        if (!nonterminalSymbol.equals(that.nonterminalSymbol)) return false;
        return right.equals(that.right);
    }

    @Override
    public int hashCode() {
        int result = nonterminalSymbol.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }

}
