package lab2;

import java.io.Serializable;

// Two exact copies of Accept class exist purely because of the way online evaluator is set up.
public class Accept implements Serializable {

    private static final long serialVersionUID = -828272784335020914L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return 1;
    }

}
