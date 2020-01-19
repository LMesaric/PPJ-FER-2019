package lab4;

import common.SprutEvaluator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static lab4.Sprut.GENERATOR_CLASS;
import static lab4.Sprut.TESTCASES_DIR;

class EvaluatorTest {

    @Test
    void test() throws IOException, InterruptedException {
        SprutEvaluator sprut = new SprutEvaluator(TESTCASES_DIR, GENERATOR_CLASS,
                ".*\\.in", ".*\\.out", System.out::println);
        Assertions.assertTrue(sprut.evaluateAll());
    }

}
