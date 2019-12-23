package lab3;

import common.SprutEvaluatorSingle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class EvaluatorTest {

    // TODO: Mark as a Test
    // @Test
    void test() throws IOException, InterruptedException {
        SprutEvaluatorSingle sprut = new SprutEvaluatorSingle(str -> {
        }, "src/test/resources/testcases_Semantics", "lab3.SemantickiAnalizator", ".*\\.in", ".*\\.out");
        Assertions.assertTrue(sprut.evaluateAll());
    }

}
