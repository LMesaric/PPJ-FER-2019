import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RegexPreprocessorTest {

    @Test
    void parseTest() {
        assertArrayEquals(
                new String[]{
                        "{variableOne} ((abc)|(efg))xyz",
                        "{variableTwo} (((abc)|(efg))xyz)(((abc)|(efg))xyz)*",
                        "{variableThree} ((((abc)|(efg))xyz)+)|\\{|a|\\}"
                },
                RegexPreprocessor.parse(
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
                RegexPreprocessor.parse(
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
                RegexPreprocessor.parse(
                        new String[]{
                                "{a} elderberries",
                                "{b} \\{|{a}"
                        }
                )
        );

        assertArrayEquals(
                new String[]{
                        "{znamenka} 0|1|2|3|4|5|6|7|8|9",
                        "{hexZnamenka} (0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F",
                        "{broj} (0|1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9)*|0x((0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F)((0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F)*",
                        "{bjelina} \\t|\\n|\\_",
                        "{sviZnakovi} \\(|\\)|\\{|\\}|\\||\\*|\\\\|\\$|\\t|\\n|\\_|!|\"|#|%|&|'|+|,|-|.|/|0|1|2|3|4|5|6|7|8|9|:|;|<|=|>|?|@|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|[|]|^|_|`|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|~",
                        "%X S_pocetno S_komentar S_unarni",
                        "%L OPERAND OP_MINUS UMINUS LIJEVA_ZAGRADA DESNA_ZAGRADA",
                        "<S_pocetno>\\t|\\_",
                        "{",
                        "-",
                        "}",
                        "<S_pocetno>\\n",
                        "{",
                        "-",
                        "NOVI_REDAK",
                        "}",
                        "<S_pocetno>#\\|",
                        "{",
                        "-",
                        "UDJI_U_STANJE S_komentar",
                        "}",
                        "<S_komentar>\\|#",
                        "{",
                        "-",
                        "UDJI_U_STANJE S_pocetno",
                        "}",
                        "<S_komentar>\\n",
                        "{",
                        "-",
                        "NOVI_REDAK",
                        "}",
                        "<S_komentar>(\\(|\\)|\\{|\\}|\\||\\*|\\\\|\\$|\\t|\\n|\\_|!|\"|#|%|&|'|+|,|-|.|/|0|1|2|3|4|5|6|7|8|9|:|;|<|=|>|?|@|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|[|]|^|_|`|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|~)",
                        "{",
                        "-",
                        "}",
                        "<S_pocetno>((0|1|2|3|4|5|6|7|8|9)(0|1|2|3|4|5|6|7|8|9)*|0x((0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F)((0|1|2|3|4|5|6|7|8|9)|a|b|c|d|e|f|A|B|C|D|E|F)*)",
                        "{",
                        "OPERAND",
                        "}",
                        "<S_pocetno>\\(",
                        "{",
                        "LIJEVA_ZAGRADA",
                        "}",
                        "<S_pocetno>\\)",
                        "{",
                        "DESNA_ZAGRADA",
                        "}",
                        "<S_pocetno>-",
                        "{",
                        "OP_MINUS",
                        "}",
                        "<S_pocetno>-(\\t|\\n|\\_)*-",
                        "{",
                        "OP_MINUS",
                        "UDJI_U_STANJE S_unarni",
                        "VRATI_SE 1",
                        "}",
                        "<S_pocetno>\\((\\t|\\n|\\_)*-",
                        "{",
                        "LIJEVA_ZAGRADA",
                        "UDJI_U_STANJE S_unarni",
                        "VRATI_SE 1",
                        "}",
                        "<S_unarni>\\t|\\_",
                        "{",
                        "-",
                        "}",
                        "<S_unarni>\\n",
                        "{",
                        "-",
                        "NOVI_REDAK",
                        "}",
                        "<S_unarni>-",
                        "{",
                        "UMINUS",
                        "UDJI_U_STANJE S_pocetno",
                        "}",
                        "<S_unarni>-(\\t|\\n|\\_)*-",
                        "{",
                        "UMINUS",
                        "VRATI_SE 1",
                        "}"
                },
                RegexPreprocessor.parse(
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
