import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexPreprocessor {

    static String[] parse(String input) {
        Objects.requireNonNull(input, "Input can not be null.");
        return parse(input.split("\\r?\\n"));
    }

    // modifies the input, return is here purely because of parse(String)
    static String[] parse(String[] input) {
        Objects.requireNonNull(input, "Input can not be null.");

        Map<String, String> definitions = new HashMap<>();

        Function<String, Matcher> matcherFactory = Pattern.compile("(\\{[a-zA-Z]+})")::matcher;

        for (int i = 0; i < input.length; ++i) {
            String line = input[i];
            Matcher m = matcherFactory.apply(line);

            while (m.find()) {
                if (definitions.containsKey(m.group())) {
                    line = line.replace(m.group(), "(" + definitions.get(m.group()) + ")");
                }
            }

            if (line.length() > 1 && line.startsWith("{")) {
                String[] kv = line.split("\\s+");
                definitions.put(kv[0], kv[1]);
            }

            input[i] = line;
        }

        return input;
    }

}
