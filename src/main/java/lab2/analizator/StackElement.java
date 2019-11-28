package lab2.analizator;

import java.util.Objects;

class StackElement {

    private final String symbol;
    private final int state;
    private final boolean type; // true for symbol, false for state

    public StackElement(String symbol) {
        this.symbol = symbol;
        state = 0;  // dummy
        type = true;
    }

    public StackElement(int state) {
        this.state = state;
        symbol = null;  // dummy
        type = false;
    }

    public String getSymbol() {
        if (!type) throw new RuntimeException("Cannot get symbol of state element: " + state);
        return symbol;
    }

    public int getState() {
        if (type) throw new RuntimeException("Cannot get state of symbol element: " + symbol);
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StackElement that = (StackElement) o;

        if (type != that.type) return false;
        if (type) return Objects.equals(symbol, that.symbol);
        else return state == that.state;
    }

    @Override
    public int hashCode() {
        if (type) return Objects.requireNonNull(symbol).hashCode();
        else return state;
    }
}
