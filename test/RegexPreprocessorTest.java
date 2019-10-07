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

        assertArrayEquals(
            new String[]{
                "{a} elderberries",
                "{b} \\{|(elderberries)"
            },
            new RegexPreprocessor().parse(
                new String[]{
                    "{a} elderberries",
                    "{b} \\{|{a}"
                }
            )
        );

        assertEquals(
            "{znamenka} 0|1|2|3|4|5|6|7|8|9\n" +
            "{hexZnamenka} (0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F\n" +
            "{broj} (0|1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9)*|0x((0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F)((0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F)*\n" +
            "{bjelina} \\t|\\n|\\_\n" +
            "{sviZnakovi} \\(|\\)|\\{|\\}|\\||\\*|\\\\|\\$|\\t|\\n|\\_|!|\"|#|%|&|'|+|,|-|.|/|0|1|2|3|4|5|6|7|8|9|:|;|<|=|>|?|@|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|[|]|^|_|`|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|~\n" +
            "%X S_pocetno S_komentar S_unarni\n" +
            "%L OPERAND OP_MINUS UMINUS LIJEVA_ZAGRADA DESNA_ZAGRADA\n" +
            "<S_pocetno>\\t|\\_\n" +
            "{\n" +
            "-\n" +
            "}\n" +
            "<S_pocetno>\\n\n" +
            "{\n" +
            "-\n" +
            "NOVI_REDAK\n" +
            "}\n" +
            "<S_pocetno>#\\|\n" +
            "{\n" +
            "-\n" +
            "UDJI_U_STANJE S_komentar\n" +
            "}\n" +
            "<S_komentar>\\|#\n" +
            "{\n" +
            "-\n" +
            "UDJI_U_STANJE S_pocetno\n" +
            "}\n" +
            "<S_komentar>\\n\n" +
            "{\n" +
            "-\n" +
            "NOVI_REDAK\n" +
            "}\n" +
            "<S_komentar>(\\(|\\)|\\{|\\}|\\||\\*|\\\\|\\$|\\t|\\n|\\_|!|\"|#|%|&|'|+|,|-|.|/|0|1|2|3|4|5|6|7|8|9|:|;|<|=|>|?|@|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|[|]|^|_|`|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|~)\n" +
            "{\n" +
            "-\n" +
            "}\n" +
            "<S_pocetno>((0|1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9)*|0x((0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F)((0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F)*)\n" +
            "{\n" +
            "OPERAND\n" +
            "}\n" +
            "<S_pocetno>\\(\n" +
            "{\n" +
            "LIJEVA_ZAGRADA\n" +
            "}\n" +
            "<S_pocetno>\\)\n" +
            "{\n" +
            "DESNA_ZAGRADA\n" +
            "}\n" +
            "<S_pocetno>-\n" +
            "{\n" +
            "OP_MINUS\n" +
            "}\n" +
            "<S_pocetno>-(\\t|\\n|\\_)*-\n" +
            "{\n" +
            "OP_MINUS\n" +
            "UDJI_U_STANJE S_unarni\n" +
            "VRATI_SE 1\n" +
            "}\n" +
            "<S_pocetno>\\((\\t|\\n|\\_)*-\n" +
            "{\n" +
            "LIJEVA_ZAGRADA\n" +
            "UDJI_U_STANJE S_unarni\n" +
            "VRATI_SE 1\n" +
            "}\n" +
            "<S_unarni>\\t|\\_\n" +
            "{\n" +
            "-\n" +
            "}\n" +
            "<S_unarni>\\n\n" +
            "{\n" +
            "-\n" +
            "NOVI_REDAK\n" +
            "}\n" +
            "<S_unarni>-\n" +
            "{\n" +
            "UMINUS\n" +
            "UDJI_U_STANJE S_pocetno\n" +
            "}\n" +
            "<S_unarni>-(\\t|\\n|\\_)*-\n" +
            "{\n" +
            "UMINUS\n" +
            "VRATI_SE 1\n" +
            "}",
            new RegexPreprocessor().parse(
                "{znamenka} 0|1|2|3|4|5|6|7|8|9\n" +
                "{hexZnamenka} {znamenka}|a|b|c|d|e|f|A|B|C|D|E|F\n" +
                "{broj} {znamenka}{znamenka}*|0x{hexZnamenka}{hexZnamenka}*\n" +
                "{bjelina} \\t|\\n|\\_\n" +
                "{sviZnakovi} \\(|\\)|\\{|\\}|\\||\\*|\\\\|\\$|\\t|\\n|\\_|!|\"|#|%|&|'|+|,|-|.|/|0|1|2|3|4|5|6|7|8|9|:|;|<|=|>|?|@|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|[|]|^|_|`|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|~\n" +
                "%X S_pocetno S_komentar S_unarni\n" +
                "%L OPERAND OP_MINUS UMINUS LIJEVA_ZAGRADA DESNA_ZAGRADA\n" +
                "<S_pocetno>\\t|\\_\n" +
                "{\n" +
                "-\n" +
                "}\n" +
                "<S_pocetno>\\n\n" +
                "{\n" +
                "-\n" +
                "NOVI_REDAK\n" +
                "}\n" +
                "<S_pocetno>#\\|\n" +
                "{\n" +
                "-\n" +
                "UDJI_U_STANJE S_komentar\n" +
                "}\n" +
                "<S_komentar>\\|#\n" +
                "{\n" +
                "-\n" +
                "UDJI_U_STANJE S_pocetno\n" +
                "}\n" +
                "<S_komentar>\\n\n" +
                "{\n" +
                "-\n" +
                "NOVI_REDAK\n" +
                "}\n" +
                "<S_komentar>{sviZnakovi}\n" +
                "{\n" +
                "-\n" +
                "}\n" +
                "<S_pocetno>{broj}\n" +
                "{\n" +
                "OPERAND\n" +
                "}\n" +
                "<S_pocetno>\\(\n" +
                "{\n" +
                "LIJEVA_ZAGRADA\n" +
                "}\n" +
                "<S_pocetno>\\)\n" +
                "{\n" +
                "DESNA_ZAGRADA\n" +
                "}\n" +
                "<S_pocetno>-\n" +
                "{\n" +
                "OP_MINUS\n" +
                "}\n" +
                "<S_pocetno>-{bjelina}*-\n" +
                "{\n" +
                "OP_MINUS\n" +
                "UDJI_U_STANJE S_unarni\n" +
                "VRATI_SE 1\n" +
                "}\n" +
                "<S_pocetno>\\({bjelina}*-\n" +
                "{\n" +
                "LIJEVA_ZAGRADA\n" +
                "UDJI_U_STANJE S_unarni\n" +
                "VRATI_SE 1\n" +
                "}\n" +
                "<S_unarni>\\t|\\_\n" +
                "{\n" +
                "-\n" +
                "}\n" +
                "<S_unarni>\\n\n" +
                "{\n" +
                "-\n" +
                "NOVI_REDAK\n" +
                "}\n" +
                "<S_unarni>-\n" +
                "{\n" +
                "UMINUS\n" +
                "UDJI_U_STANJE S_pocetno\n" +
                "}\n" +
                "<S_unarni>-{bjelina}*-\n" +
                "{\n" +
                "UMINUS\n" +
                "VRATI_SE 1\n" +
                "}"
            )
        );
    }

}
