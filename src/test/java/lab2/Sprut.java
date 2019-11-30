package lab2;

import common.SprutEvaluator;

import java.io.IOException;

class Sprut {

    static final String GENERATOR_CLASS = "lab2.GSA";
    static final String ANALYZER_CLASS = "lab2.analizator.SA";
    static final String TESTCASES_DIR = "src/test/resources/testcases_GSA_SA";

    public static void main(String[] args) throws IOException, InterruptedException {
        SprutEvaluator sprut = new SprutEvaluator(System.out::println, TESTCASES_DIR, GENERATOR_CLASS, ANALYZER_CLASS,
                ".*\\.in", ".*\\.san", ".*\\.out");
        sprut.evaluateAll();
    }

}
