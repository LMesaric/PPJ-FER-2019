package lab4;

import java.util.List;
import java.util.Objects;

public class FullType {
    boolean array;
    Type type;
    // Used if type is array
    int brElements;
    // Not null if type is function
    List<FullType> arguments = null;

    FullType(Type type) {
        this.type = Objects.requireNonNull(type);
    }

    FullType(Type type, int brElements) {
        this(type);
        this.brElements = brElements;
        this.array = true;
    }

    FullType(Type returnType, List<FullType> arguments) {
        this(returnType);
        this.arguments = Objects.requireNonNull(arguments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullType fullType = (FullType) o;
        return array == fullType.array &&
                brElements == fullType.brElements &&
                type.equals(fullType.type) &&
                Objects.equals(arguments, fullType.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(array, type, brElements, arguments);
    }
}