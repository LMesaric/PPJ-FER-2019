package lab3;

import common.SprutEvaluator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static lab3.Sprut.ANALYZER_CLASS;
import static lab3.Sprut.TESTCASES_DIR;

class EvaluatorTest {

    // TODO: Enable test once lab4 is done
    // @Test
    void test() throws IOException, InterruptedException {
        SprutEvaluator sprut = new SprutEvaluator(str -> {}, TESTCASES_DIR, ANALYZER_CLASS,
                ".*\\.in", ".*\\.out");
        Assertions.assertTrue(sprut.evaluateAll());
    }

}
