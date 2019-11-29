package lab2;

import common.SprutEvaluator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static lab2.Sprut.*;

public class EvaluatorTest {

    @Test
    void test() throws IOException, InterruptedException {
        SprutEvaluator sprut = new SprutEvaluator(str -> {}, TESTCASES_DIR, GENERATOR_CLASS, ANALYZER_CLASS,
                ".*\\.in", ".*\\.san", ".*\\.out");
        Assertions.assertTrue(sprut.evaluateAll());
    }

}
