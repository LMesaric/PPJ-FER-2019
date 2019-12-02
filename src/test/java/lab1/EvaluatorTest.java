package lab1;

import common.SprutEvaluator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static lab1.Sprut.*;

class EvaluatorTest {

    @Test
    void test() throws IOException, InterruptedException {
        SprutEvaluator sprut = new SprutEvaluator(str -> {}, TESTCASES_DIR, GENERATOR_CLASS, ANALYZER_CLASS,
                ".*\\.in", ".*\\.lan", ".*\\.out");
        Assertions.assertTrue(sprut.evaluateAll());
    }

}
