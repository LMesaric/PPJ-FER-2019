package lab2;

import lab2.analizator.LexicalToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexicalTokenTest {

    @Test
    void testFromLine() {
        assertEquals(
                LexicalToken.fromLine("a 1 x x x"),
                new LexicalToken("a", 1, "x x x")
        );
        assertEquals(
                LexicalToken.fromLine("b 2 y y"),
                new LexicalToken("b", 2, "y y")
        );
        assertEquals(
                LexicalToken.fromLine("a 3 xx xx"),
                new LexicalToken("a", 3, "xx xx")
        );
        assertEquals(
                LexicalToken.fromLine("a 4 xx xx xx"),
                new LexicalToken("a", 4, "xx xx xx")
        );
        assertEquals(
                LexicalToken.fromLine("b 4 y"),
                new LexicalToken("b", 4, "y")
        );
    }

    @Test
    void testFromLineWithTrailing() {
        assertEquals(
                LexicalToken.fromLine("a 1 x x x\n"),
                new LexicalToken("a", 1, "x x x")
        );
        assertEquals(
                LexicalToken.fromLine("b 2 y y  \n"),
                new LexicalToken("b", 2, "y y")
        );
        assertEquals(
                LexicalToken.fromLine("a 3 xx xx\r\n"),
                new LexicalToken("a", 3, "xx xx")
        );
        assertEquals(
                LexicalToken.fromLine("a 4 xx xx xx \r\n"),
                new LexicalToken("a", 4, "xx xx xx")
        );
        assertEquals(
                LexicalToken.fromLine("b 4 y "),
                new LexicalToken("b", 4, "y")
        );
    }

}
