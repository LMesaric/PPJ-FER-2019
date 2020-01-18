package lab4;

import java.util.Objects;

public class Variable {
    String name;
    FullType fullType;

    int addressingOffset;
    int memSize = 4;


    Variable(String name, FullType fullType) {
        this.name = name;
        this.fullType = fullType;
    }

    FullType getFullType() {
        return fullType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return name.equals(variable.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}