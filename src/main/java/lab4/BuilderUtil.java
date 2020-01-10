package lab4;

class BuilderUtil {

    private static final int NORMAL_INDENT = 12;

    static String buildLine(String text, String label) {
        if (label == null) label = "";
        int diff = NORMAL_INDENT - label.length();
        if (diff < 1) diff = 1;

        return String.format("%s%" + diff + "s%s", label, " ", text);
    }

    static StringBuilder appendLine(StringBuilder builder, String text) {
        return appendLine(builder, text, null);
    }

    static StringBuilder appendLine(StringBuilder builder, String text, String label) {
        if (label != null && (label = label.trim()).length() == 0)
            throw new IllegalArgumentException("Bad label: " + label);

        return builder.append(buildLine(text, label))
                .append("\n");
    }

}
