package lab2.analizator;

public class LexicalToken {

    String uniformSign;
    int lineNumber;
    String originalCode;

    public LexicalToken(String uniformSign, int lineNumber, String originalCode) {
        this.uniformSign = uniformSign;
        this.lineNumber = lineNumber;
        this.originalCode = originalCode;
    }

    public static LexicalToken fromLine(String input) {
        input = input.trim();
        String[] parts = input.split(" ", 3);
        return new LexicalToken(parts[0], Integer.parseInt(parts[1]), parts[2]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LexicalToken that = (LexicalToken) o;

        if (lineNumber != that.lineNumber) return false;
        if (!uniformSign.equals(that.uniformSign)) return false;
        return originalCode.equals(that.originalCode);
    }

    @Override
    public int hashCode() {
        int result = uniformSign.hashCode();
        result = 31 * result + lineNumber;
        result = 31 * result + originalCode.hashCode();
        return result;
    }

}