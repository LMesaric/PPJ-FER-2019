import java.util.*;
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
            String[] defDecl = input[i].split("\\s+");
            Matcher m = matcherFactory.apply(defDecl[1]);

            while (m.find()) {
                defDecl[1] = defDecl[1].replace(m.group(), "(" + definitions.get(m.group()) + ")");
            }

            definitions.put(defDecl[0], defDecl[1]);
            input[i] = defDecl[0] + ' ' + defDecl[1];
        }

        return input;
    }

}
