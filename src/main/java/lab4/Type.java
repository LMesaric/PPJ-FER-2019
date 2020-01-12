package lab4;

import java.util.Objects;

public class Type {
    boolean constant;
    PrimitiveType primitiveType;

    Type(boolean constant, PrimitiveType type) {
        this.constant = constant;
        this.primitiveType = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return constant == type.constant &&
                primitiveType == type.primitiveType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(constant, primitiveType);
    }
}