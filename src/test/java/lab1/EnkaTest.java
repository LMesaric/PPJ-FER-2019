package lab1;

import lab1.analizator.Enka;
import lab1.analizator.EnkaStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnkaTest {

    private static final Path path = Paths.get("./src/test/java/lab1/enkaTable.txt");

    @AfterAll
    static void deleteGeneratedFile() throws IOException {
        Files.deleteIfExists(path);
    }

    @Test
    void test() throws IOException {
        assertEquals(testEnka("ab|c|d", "c"), EnkaStatus.ACCEPTED);

        assertEquals(testEnka("((1|2|3)ab|c)c", "cc"), EnkaStatus.ACCEPTED);
        assertEquals(testEnka("((1|2|3)ab|c)c", "1abc"), EnkaStatus.ACCEPTED);
        assertEquals(testEnka("((1|2|3)ab|c)c", "2abc"), EnkaStatus.ACCEPTED);
        assertEquals(testEnka("((1|2|3)ab|c)c", "3abc"), EnkaStatus.ACCEPTED);
        assertEquals(testEnka("((1|2|3)ab|c)c", "bc"), EnkaStatus.DENIED);
        assertEquals(testEnka("((1|2|3)ab|c)c", "abc"), EnkaStatus.DENIED);
        assertEquals(testEnka("((1|2|3)ab|c)c", "1ab"), EnkaStatus.IN_PROGRESS);
        assertEquals(testEnka("((1|2|3)ab|c)c", "c"), EnkaStatus.IN_PROGRESS);

        assertEquals(testEnka("(a|b|\\n)((1|2|3)ab|c)c", "\ncc"), EnkaStatus.ACCEPTED);
        assertEquals(testEnka("(a|b|\\n)((1|2|3)ab|c)c", "a1abc"), EnkaStatus.ACCEPTED);

        assertEquals(testEnka("(a|b|\\_)((1|2|3)ab|c)c", " cc"), EnkaStatus.ACCEPTED);

        assertEquals(testEnka("(a|b|\\))((1|2|3)ab|c)c", ")cc"), EnkaStatus.ACCEPTED);
        assertEquals(testEnka("(a|b|\\))((1|2|3)ab|c)c", ")1abc"), EnkaStatus.ACCEPTED);

        assertEquals(testEnka("(a|b|\\()((1|2|3)ab|c)c", "(cc"), EnkaStatus.ACCEPTED);
        assertEquals(testEnka("(a|b|\\()((1|2|3)ab|c)c", "(1abc"), EnkaStatus.ACCEPTED);

        assertEquals(testEnka("1*2*", ""), EnkaStatus.ACCEPTED);
        assertEquals(testEnka("1*2*", "112"), EnkaStatus.ACCEPTED);
        assertEquals(testEnka("1*2*", "11"), EnkaStatus.ACCEPTED);
        assertEquals(testEnka("1*2*", "22"), EnkaStatus.ACCEPTED);
        assertEquals(testEnka("1*2*", "1"), EnkaStatus.ACCEPTED);
        assertEquals(testEnka("1*2*", "2"), EnkaStatus.ACCEPTED);

        assertEquals(testEnka("\\(|\\)|\\{|\\}|\\||\\*|\\\\|\\$|\\t|\\n|\\_|!|\"|#|%|&|'|+|,|-|.|/|0|1|2|3|4|5|6|7|8|9|:|;|<|=|>|?|@|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|[|]|^|_|`|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|~", "|"), EnkaStatus.ACCEPTED);

        assertEquals(testEnka("'(\\(|\\)|\\{|\\}|\\||\\*|\\\\|\\$|\\_|!|\"|#|%|&|+|,|-|.|/|0|1|2|3|4|5|6|7|8|9|:|;|<|=|>|?|@|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|[|]|^|_|`|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|~)'", "''"), EnkaStatus.DENIED);
    }

    private EnkaStatus testEnka(String expression, String simulate) throws IOException {
        generateTable(expression);
        Enka enka = buildEnka(path);
        return simulateEnka(enka, simulate);
    }

    private void generateTable(String expression) throws IOException {
        TableGenerator generator = new TableGenerator();
        String table = generator.buildTable(expression);
        Files.write(path, table.getBytes());
    }

    private static Enka buildEnka(Path path) throws IOException {
        String table = new String(Files.readAllBytes(path));
        Enka enka = new Enka();
        enka.buildFromTable(table);
        return enka;
    }

    private static EnkaStatus simulateEnka(Enka enka, String expression) {
        enka.reset();
        EnkaStatus status = enka.getLastStatus();
        for (char c : expression.toCharArray()) {
            status = enka.performTransition(c);
        }
        return status;
    }

}
