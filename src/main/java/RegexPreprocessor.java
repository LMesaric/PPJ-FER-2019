import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexPreprocessor {

    private Map<String, String> definitions = new HashMap<>();

    public String parse(String input) {
        Objects.requireNonNull(input, "Input can not be null.");
        return Arrays.stream(parse(input.split("\\r?\\n"))).reduce((s1, s2) -> s1 + '\n' + s2).get();
    }

    // modifies the input, return is here purely because of parse(String)
    public String[] parse(String[] input) {
        Objects.requireNonNull(input, "Input can not be null.");

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
