package lab2.analizator;

import java.io.Serializable;

// Two exact copies of Put class exist purely because of the way online evaluator is set up.
public class Put implements Serializable {

    private static final long serialVersionUID = 6718678492959180447L;

    public final int newState;

    public Put(int newState) {
        this.newState = newState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Put put = (Put) o;
        return newState == put.newState;
    }

    @Override
    public int hashCode() {
        return newState;
    }

}
