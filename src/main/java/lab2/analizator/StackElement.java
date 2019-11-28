package lab2.analizator;

import java.util.Objects;

class StackElement {

    final String symbol;
    final int state;

    StackElement(String symbol, int state) {
        this.symbol = Objects.requireNonNull(symbol);
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StackElement that = (StackElement) o;

        if (state != that.state) return false;
        return symbol.equals(that.symbol);
    }

    @Override
    public int hashCode() {
        int result = symbol.hashCode();
        result = 31 * result + state;
        return result;
    }

    @Override
    public String toString() {
        return "StackElement{" +
                "symbol='" + symbol + '\'' +
                ", state=" + state +
                '}';
    }

}
