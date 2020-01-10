package lab4;

import common.SprutEvaluator;

import java.io.IOException;

public class Sprut {

    static final String TESTCASES_DIR = "src/test/resources/testcases_Frisc";
    static final String GENERATOR_CLASS = "lab4.GeneratorKoda";

    public static void main(String[] args) throws IOException, InterruptedException {
        SprutEvaluator sprut = new SprutEvaluator(TESTCASES_DIR, GENERATOR_CLASS,
                ".*\\.in", ".*\\.out", System.out::println);
        sprut.evaluateAll();
    }


}
