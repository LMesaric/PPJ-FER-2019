package lab1;

import common.SprutEvaluator;

import java.io.IOException;

class Sprut {

    static final String GENERATOR_CLASS = "lab1.GLA";
    static final String ANALYZER_CLASS = "lab1.analizator.LA";
    static final String TESTCASES_DIR = "src/test/resources/testcases_GLA_LA";

    public static void main(String[] args) throws IOException, InterruptedException {
        SprutEvaluator sprut = new SprutEvaluator(System.out::println, TESTCASES_DIR, GENERATOR_CLASS, ANALYZER_CLASS,
                ".*\\.in", ".*\\.lan", ".*\\.out");
        sprut.evaluateAll();
    }

}
