package lab3;

import org.junit.jupiter.api.Test;

import static lab3.SemantickiAnalizator.countLeadingSpaces;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LeadingSpaceCounterTest {

    @Test
    void testRoot() {
        assertEquals(0, countLeadingSpaces("<expr>"));
        assertEquals(0, countLeadingSpaces("<expr>  "));
        assertEquals(0, countLeadingSpaces("<expr>\n"));
        assertEquals(0, countLeadingSpaces("<expr>\r\n"));
        assertEquals(0, countLeadingSpaces("OP_MINUS 2 -"));
        assertEquals(0, countLeadingSpaces("OP_MINUS 2 -   "));
        assertEquals(0, countLeadingSpaces("OP_MINUS 2 -\n"));
        assertEquals(0, countLeadingSpaces("OP_MINUS 2 -\r\n"));
    }

    @Test
    void testLevelOne() {
        assertEquals(1, countLeadingSpaces(" <expr>"));
        assertEquals(1, countLeadingSpaces(" <expr>   "));
        assertEquals(1, countLeadingSpaces(" <expr>\n"));
        assertEquals(1, countLeadingSpaces(" <expr>\r\n"));
    }

    @Test
    void testLevelTwo() {
        assertEquals(2, countLeadingSpaces("  <expr>"));
        assertEquals(2, countLeadingSpaces("  <expr>   "));
        assertEquals(2, countLeadingSpaces("  <expr>\n"));
        assertEquals(2, countLeadingSpaces("  <expr>\r\n"));
    }

}