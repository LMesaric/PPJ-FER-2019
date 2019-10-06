import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class RegexPreprocessorTest {

    @Test
    void parseTest() {
        assertArrayEquals(
            new String[]{
                "{variableOne} ((abc)|(efg))xyz",
                "{variableTwo} (((abc)|(efg))xyz)(((abc)|(efg))xyz)*",
                "{variableThree} ((((abc)|(efg))xyz)+)|\\{|a|\\}"
            },
            new RegexPreprocessor().parse(
                new String[]{
                    "{variableOne} ((abc)|(efg))xyz",
                    "{variableTwo} {variableOne}{variableOne}*",
                    "{variableThree} ({variableOne}+)|\\{|a|\\}"
                }
            )
        );

        assertArrayEquals(
            new String[]{
                "{a} elderberries",
                "{b} \\{a\\}"
            },
            new RegexPreprocessor().parse(
                new String[]{
                    "{a} elderberries",
                    "{b} \\{a\\}"
                }
            )
        );
    }

}
