package lab2;

import java.io.Serializable;

// Two exact copies of Reduce class exist purely because of the way online evaluator is set up.
public class Reduce implements Serializable {

    private static final long serialVersionUID = 4014673952837465316L;

    public final Production production;

    public Reduce(Production production) {
        this.production = production;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reduce reduce = (Reduce) o;
        return production.equals(reduce.production);
    }

    @Override
    public int hashCode() {
        return production.hashCode();
    }

}
