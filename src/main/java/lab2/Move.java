package lab2;

import java.io.Serializable;

// Two exact copies of Move class exist purely because of the way online evaluator is set up.
class Move implements Serializable {

    private static final long serialVersionUID = 2376271043341233867L;

    final int newState;

    Move(int newState) {
        this.newState = newState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        return newState == move.newState;
    }

    @Override
    public int hashCode() {
        return newState;
    }

}
